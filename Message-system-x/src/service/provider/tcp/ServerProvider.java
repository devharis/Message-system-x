package service.provider.tcp;

import client.Messenger;

import interfaces.IMessageReceiver;
import interfaces.IServiceProvider;
import models.*;

import java.io.Closeable;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.*;
import java.util.ArrayList;

/**
 * Created by devHaris on 2015-03-11.
 */
public class ServerProvider implements IServiceProvider {

    // Just an holder for information regarding each client

    // Max clients constant
    private final static int MAX_CLIENTS = 100;
    // Server socket
    ServerSocket serverSocket;
    // Accept thread
    Thread acceptThread;
    // List of clients
    ArrayList<Client> clients = new ArrayList<Client>(MAX_CLIENTS);
    // Accept socket
    Socket acceptSocket;
    // Accepting step out variable
    private volatile boolean accepting = false;
    // Disconnect trigger
    private final static String DISCONNECT = "/disconnect";
    // Broadcast identifier
    private final static String BROADCAST = "255.255.255.255";

    @Override
    public void startListening(final String endPoint, final IMessageReceiver messageReceiver) {
        try {
            int port = Integer.parseInt(endPoint);
            setupServer(port, messageReceiver);
        } catch (RuntimeException ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public void stopListening() {

        // CLEAN UP
        accepting = false;
        sendMessage(new Message(null, "Server has disconnected \n", null, MessageType.BROADCAST), BROADCAST);

        try {
            // Close server socket
            serverSocket.close();

        } catch (IOException ex) {

            for(Client c : clients) {

                try {

                    // Close streams for every client
                    c.active = false;
                    c.getOOS().close();
                    c.getOIS().close();

                } catch(Exception e) {
                    continue;
                }
            }

            throw new RuntimeException(ex);
        }
    }

    @Override
    public void sendMessage(final Message message, final String destinationEndPoint) {

        sendMessageEx(message, destinationEndPoint);
    }

    public void setupServer(final int port, final IMessageReceiver messageReceiver) {

        try {
            beginAccepting(port, messageReceiver);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    private void beginAccepting(final int port, final IMessageReceiver messageReceiver) {

        accepting = true;

        acceptThread = new Thread(new Runnable() {

            @Override
            public void run() {
                try {

                    // Init the server socket
                    serverSocket = new ServerSocket(port);
                    while(accepting) {

                        messageReceiver.onMessage(new Message("Server", "Server is listening...", "127.0.0.1", MessageType.INTERNAL));

                        // Try to accept a client
                        acceptSocket = serverSocket.accept();

                        // Init object streams
                        ObjectOutputStream oos = new ObjectOutputStream(acceptSocket.getOutputStream());
                        ObjectInputStream ois = new ObjectInputStream(acceptSocket.getInputStream());

                        // Client connected
                        final Client client = new Client(acceptSocket.getInetAddress().getHostAddress(), oos, ois);

                        client.getOOS().writeObject(new Message(null, "Client Connected! \n",
                                acceptSocket.getInetAddress().getHostAddress(), MessageType.SINGLE));
                        client.getOOS().flush();

                        // Get username from client and set it
                        Message message = (Message) client.getOIS().readObject();

                        client.setUserName(message.getName());
                        // Display on server UI
                        messageReceiver.onMessage(new Message(message.getName(),
                                String.format("User: %s connected with ip: %s \n", client.getUserName(), client.getEndPoint()),
                                acceptSocket.getInetAddress().getHostAddress(), MessageType.INTERNAL));

                        // Check if client limit has been reached
                        if(clients.size() == MAX_CLIENTS) {

                            // Reject the client
                            client.getOOS().writeObject("Max number of clients already connected. Try again later. \n");
                            client.getOOS().flush();

                        } else if(isNameTaken(client.getUserName())) {

                            // Reject the client, because name was taken
                            client.getOOS().writeObject("The specified name was already taken, please choose another one and connect again. \n");
                            client.getOOS().flush();

                        } else {

                            // Client is accepted, init listen thread for client
                            initClientThread(client, messageReceiver);
                        }
                    }

                } catch (Exception ex) {
                    // Socket was forced to close
                } finally {
                    stopListening();
                }
            }
        });

        acceptThread.start();
    }

    private void initClientThread(final Client client, final IMessageReceiver messageReceiver) {

        // Set client to active
        client.active = true;

        // Init the receive thread for client
        Thread receiveThread = new Thread(new Runnable() {

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
                        clients.remove(client);

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

        clients.add(client);
        receiveThread.start();
    }

    private void sendMessageEx(Message message, String endPoint) {
        try {
            if(endPoint.equals(BROADCAST)) {
                for (Client c : clients) {
                    c.getOOS().writeObject(message);
                    c.getOOS().flush();
                }
            } else {
                for (Client c : clients) {
                    if(c.getEndPoint().equals(endPoint)) {
                        c.getOOS().writeObject(message);
                        c.getOOS().flush();
                        break;
                    }
                }
            }
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    private boolean isNameTaken(String userName) {

        for (Client c : clients) {
            if(c.getUserName().equals(userName)) {
                return true;
            }
        }
        return false;
    }
}
