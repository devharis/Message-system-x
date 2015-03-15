package service.provider.tcp;

import client.Messenger;

import interfaces.IMessageReceiver;
import interfaces.IServiceProvider;
import models.Client;

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

    private String _ipAdress;

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
        sendMessage("Server has disconnected \n", BROADCAST);

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
    public void sendMessage(final String msgText, final String destinationEndPoint) {

        sendMessageEx(msgText, destinationEndPoint);
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
                    _ipAdress = serverSocket.getInetAddress().getHostAddress();
                    while(accepting) {

                        messageReceiver.onMessage("Server is listening intimately... \n");

                        // Try to accept a client
                        acceptSocket = serverSocket.accept();

                        // Init object streams
                        ObjectOutputStream oos = new ObjectOutputStream(acceptSocket.getOutputStream());
                        ObjectInputStream ois = new ObjectInputStream(acceptSocket.getInputStream());

                        // Client connected
                        final Client client = new Client(acceptSocket.getInetAddress().toString(), oos, ois);

                        client.getOOS().writeObject("Client Connected! \n");
                        client.getOOS().flush();

                        // Get username from client and set it
                        String userName = (String) client.getOIS().readObject();

                        client.setUserName(userName);
                        // Display on server UI
                        messageReceiver.onMessage(String.format("User: %s connected with ip: %s \n",
                                client.getUserName(), client.getEndPoint()));

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
                    String message;
                    while((!(message = (String)client.getOIS().readObject()).equals(DISCONNECT))
                            && client.active) {

                        sendMessage(String.format("%s: %s \n", client.getUserName(), message), BROADCAST);

                        // Display on server UI
                        messageReceiver.onMessage(
                                String.format("%s said: %s \n", client.getUserName(), message)
                        );
                    }

                    // Client disconnect was called
                    sendMessage(String.format("%s disconnected \n", client.getUserName()), BROADCAST);

                    try {

                        // Close client streams
                        client.getOIS().close();
                        client.getOOS().close();

                    } finally {

                        // Remove client from broadcast list
                        clients.remove(client);

                        // Display on server UI
                        messageReceiver.onMessage(String.format("%s disconnected \n", client.getUserName()));
                    }

                } catch(Exception ex) {
                    throw new RuntimeException(ex);
                }
            }

        });

        clients.add(client);
        receiveThread.start();
    }

    private void sendMessageEx(String message, String endPoint) {
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

    public String getIpAdress() {
        return _ipAdress;
    }
}
