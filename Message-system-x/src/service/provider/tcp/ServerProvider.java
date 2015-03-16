package service.provider.tcp;

import interfaces.IMessageReceiver;
import interfaces.IServiceProvider;
import models.Client;
import models.Message;
import models.MessageType;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

/**
 * This is one of the providers which provides a client-server
 * connection utilizing TCP. This provider acts as server.
 *
 * @author Created by Haris Kljajic & Oskar Karlsson on 2015-03-13.
 * Linneaus University - [2DV104] Software Architecture
 */
public class ServerProvider implements IServiceProvider {

    // variables
    private volatile boolean _accepting = false;
    private ServerSocket _serverSocket;
    private Thread _acceptThread;
    private Thread _receiveThread;
    private ArrayList<Client> _clients = new ArrayList<Client>(MAX_CLIENTS);
    private Socket _acceptSocket;

    // constants
    private final static int MAX_CLIENTS = 100;
    private final static String EMPTY_STRING = null;
    private final static String BROADCAST = "255.255.255.255";
    private final static String SERVER_DISCONNECT = "Server has disconnected \n";
    private final static String PROVIDER_NAME = "Server";
    private final static String SERVER_LISTENING = "Server is listening...";
    private final static String SERVER_ADDRESS = "127.0.0.1";
    private final static String CLIENT_CONNECTED = "Client Connected! \n";
    private final static String SERVER_MAX_CONNECTION = "Max number of clients already connected. Try again later. \n";
    private final static String NAME_TAKEN = "The specified name was already taken, please choose another one and connect again. \n";

    /**
     * This method takes an endpoint and message receiver and passes
     * them to startAccepting to initialize a listener.
     * @param endPoint Used for socket
     * @param messageReceiver Provided to use onMessage to receive message from server.
     */
    @Override
    public void startListening(final String endPoint, final IMessageReceiver messageReceiver) {
        try {
            int port = Integer.parseInt(endPoint);
            startAccepting(port, messageReceiver);
        } catch (RuntimeException ex) {
            throw new RuntimeException(ex);
        }
    }

    /**
     * Occurs when server stops. It closes the established socket connection.
     */
    @Override
    public void stopListening() {
        _accepting = false;
        sendMessage(new Message(EMPTY_STRING, SERVER_DISCONNECT, EMPTY_STRING, MessageType.BROADCAST), BROADCAST);

        try {
            // Close server socket
            _serverSocket.close();

        } catch (IOException ex) {
            closeClients();
            throw new RuntimeException(ex);
        }
    }

    /**
     * Iterates through all clients to close every stream.
     */
    private void closeClients() {
        for(Client clientList : _clients) {
            try {
                // Close streams for every client
                clientList.active = false;
                clientList.getOOS().close();
                clientList.getOIS().close();

            } catch(Exception e) {
                continue;
            }
        }
    }

    /**
     * This method is used when server starts to initialize a listener for incoming
     * connections and messages from clients. It creates a separate thread for it.
     * @param port Used for socket
     * @param messageReceiver Provided to use onMessage to receive message from server.
     */
    private void startAccepting(final int port, final IMessageReceiver messageReceiver) {

        _accepting = true;
        _acceptThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    // Init the server socket
                    _serverSocket = new ServerSocket(port);
                    while(_accepting) {
                        messageReceiver.onMessage(new Message(PROVIDER_NAME, SERVER_LISTENING, SERVER_ADDRESS, MessageType.INTERNAL));

                        // Try to accept a client
                        _acceptSocket = _serverSocket.accept();

                        // Init object streams
                        ObjectOutputStream oos = new ObjectOutputStream(_acceptSocket.getOutputStream());
                        ObjectInputStream ois = new ObjectInputStream(_acceptSocket.getInputStream());

                        // Client connected
                        final Client client = new Client(_acceptSocket.getInetAddress().getHostAddress(), oos, ois);

                        client.getOOS().writeObject(new Message(EMPTY_STRING, CLIENT_CONNECTED,
                                _acceptSocket.getInetAddress().getHostAddress(), MessageType.SINGLE));
                        client.getOOS().flush();

                        // Get username from client and set it
                        Message message = (Message) client.getOIS().readObject();

                        client.setUserName(message.getName());
                        // Display on server UI
                        messageReceiver.onMessage(new Message(message.getName(),
                                String.format("User: %s connected with ip: %s \n", client.getUserName(), client.getEndPoint()),
                                _acceptSocket.getInetAddress().getHostAddress(), MessageType.INTERNAL));

                        // Check if client limit has been reached
                        if(_clients.size() == MAX_CLIENTS) {
                            // Reject the client
                            client.getOOS().writeObject(SERVER_MAX_CONNECTION);
                            client.getOOS().flush();
                        } else if(isNameTaken(client.getUserName())) {
                            // Reject the client, because name was taken
                            client.getOOS().writeObject(NAME_TAKEN);
                            client.getOOS().flush();
                        } else {
                            // Client is accepted, init listen thread for client
                            startResponding(client, messageReceiver);
                        }
                    }
                } catch (Exception ex) {
                    // Socket was forced to close
                } finally {
                    stopListening();
                }
            }
        });

        _acceptThread.start();
    }

    /**
     * Creates a separate thread which will communicate with the incoming clients.
     * Used for responding to successful connection and received messages from other clients.
     * @param client Current client connecting
     * @param messageReceiver Provided to use onMessage to receive message from server.
     */
    private void startResponding(final Client client, final IMessageReceiver messageReceiver) {

        // Set client to active
        client.active = true;

        // Init the receive thread for client
        _receiveThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {

                    // Init message variable and while loop
                    Message message;
                    while(!((message = (Message)client.getOIS().readObject()).getMessageType().equals(MessageType.COMMAND))
                            && client.active) {

                        sendMessage(message, BROADCAST);
                        // Display on server UI
                        messageReceiver.onMessage(new Message(
                                message.getName(),
                                String.format("%s said: %s \n", message.getName(), message.getMessage()),
                                message.getEndPoint(),
                                MessageType.INTERNAL));
                    }

                    // Client disconnect was called
                    sendMessage(new Message(message.getName(),
                            String.format("%s disconnected \n", message.getName()),
                            message.getEndPoint(),
                            MessageType.BROADCAST), BROADCAST);

                    try {
                        // Close client streams
                        client.getOIS().close();
                        client.getOOS().close();

                    } finally {

                        // Remove client from broadcast list
                        _clients.remove(client);

                        // Display on server UI
                        messageReceiver.onMessage(new Message(
                                message.getName(),
                                String.format("%s disconnected \n", message.getName()),
                                message.getEndPoint(),
                                MessageType.INTERNAL));
                    }
                } catch(Exception ex) {
                    throw new RuntimeException(ex);
                }
            }
        });

        _clients.add(client);
        _receiveThread.start();
    }

    /**
     * Iterates through all clients to pass the message.
     * @param message Message which will be sent
     * @param endPoint Clients with this endpoint
     */
    private void sendMessageToListeners(Message message, String endPoint) {
        try {
            if(endPoint.equals(BROADCAST)) {
                for (Client client : _clients) {
                    client.getOOS().writeObject(message);
                    client.getOOS().flush();
                }
            } else {
                for (Client client : _clients) {
                    if(client.getEndPoint().equals(endPoint)) {
                        client.getOOS().writeObject(message);
                        client.getOOS().flush();
                        break;
                    }
                }
            }
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    /**
     * Method is a helper for checking name is already taken.
     * @param userName Username to check for
     * @return Boolean
     */
    private boolean isNameTaken(String userName) {

        for (Client c : _clients) {
            if(c.getUserName().equals(userName)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Method for sending message with a destination.
     * @param message  Message which will be sent
     * @param destinationEndPoint Clients with this endpoint
     */
    @Override
    public void sendMessage(final Message message, final String destinationEndPoint) {
        sendMessageToListeners(message, destinationEndPoint);
    }
}
