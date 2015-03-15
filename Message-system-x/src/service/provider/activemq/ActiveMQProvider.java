package service.provider.activemq;

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
public class ActiveMQProvider implements IServiceProvider {

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
        SetupServer(Messenger.PORT, messageReceiver);
    }

    @Override
    public void stopListening() {

        // CLEAN UP
        accepting = false;
        sendMessage("Server has disconnected", BROADCAST);

        try {
            serverSocket.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        for(Client c : clients) {
            try {
                c.active = false;
                c.getOOS().close();
                c.getOIS().close();
            } catch(Exception ex) {
                // Silent
            }
        }
    }

    @Override
    public void sendMessage(final String msgText, final String destinationEndPoint) {

        SendMessageEx(msgText, destinationEndPoint);
    }

    public void SetupServer(final int port, final IMessageReceiver messageReceiver) {

        accepting = true;
        acceptThread = new Thread(new Runnable() {

            @Override
            public void run() {
                try {

                    // Init the server socket
                    serverSocket = new ServerSocket(port);

                    while(accepting) {

                        messageReceiver.onMessage("Server is listening intimately...");

                        // Try to accept a client
                        acceptSocket = serverSocket.accept();

                        // Client connected
                        final Client client = new Client(acceptSocket.getInetAddress().toString(),
                                new ObjectInputStream(acceptSocket.getInputStream()),
                                new ObjectOutputStream(acceptSocket.getOutputStream()));

                        client.getOOS().writeObject("Connected.");
                        client.getOOS().flush();

                        // Get username from client and set it
                        String userName = (String) client.getOIS().readObject();

                        // Display on server UI
                        messageReceiver.onMessage(String.format("User: %s connected with ip: %s",
                                client.getUserName(), client.getEndPoint()));

                        client.setUserName(userName);

                        // Check if client limit has been reached
                        if(clients.size() == MAX_CLIENTS) {

                            // Reject the client
                            client.getOOS().writeObject("Max number of clients already connected. Try again later.");
                            client.getOOS().flush();

                        } else if(IsNameTaken(client.getUserName())) {

                            // Reject the client, because name was taken
                            client.getOOS().writeObject("The specified name was already taken, please choose another one and connect again.");
                            client.getOOS().flush();

                        } else {

                            // Client is accepted
                            client.active = true;

                            // Init the receive thread for client
                            Thread receiveThread = new Thread(new Runnable() {

                                @Override
                                public void run() {
                                    try {

                                        // Init message variable and while loop
                                        String message;
                                        while(((message = (String)client.getOIS().readObject()) != DISCONNECT)
                                                && client.active) {

                                            sendMessage(String.format("%s: %s", client.getUserName(), message), BROADCAST);

                                            // Display on server UI
                                            messageReceiver.onMessage(
                                                    String.format("%s said: %s", client.getUserName(), message)
                                            );
                                        }

                                    } catch(Exception ex) {
                                        ex.printStackTrace();
                                    }
                                }

                            });

                            clients.add(client);
                            receiveThread.start();
                        }
                    }

                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });

        acceptThread.start();
    }

    private void SendMessageEx(String message, String endPoint) {
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
            ex.printStackTrace();
        }
    }

    private boolean IsNameTaken(String userName) {

        for (Client c : clients) {
            if(c.getUserName().equals(userName)) {
                return true;
            }
        }
        return false;
    }
}
