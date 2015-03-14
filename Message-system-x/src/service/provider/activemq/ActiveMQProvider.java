package service.provider.activemq;

import client.Messenger;
import interfaces.IMessageReceiver;
import interfaces.IServiceProvider;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

/**
 * Created by devHaris on 2015-03-11.
 */
public class ActiveMQProvider implements IServiceProvider {

    // Listen Thread
    Thread listenThread;
    // Receiving Socket
    DatagramSocket receiveSocket;
    // Send Thread
    Thread sendThread;
    // Send Socket
    DatagramSocket sendSocket;
    // Listening step out variable
    private volatile boolean listening = false;
    // Max message length
    private final static int MESSAGE_LIMIT = 200;
    // Server endpoint
    private String __server;

    @Override
    public void startListening(final String endPoint, final IMessageReceiver IMessageReceiver) {
        listening = true;

        listenThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    __server = endPoint;

                    // Bind socket to endpoint and port
                    receiveSocket = new DatagramSocket(Messenger.PORT);

                    // Listen loop
                    while(listening) {
                        // Waiting for packets

                        // Init receive buffer
                        byte[] receiveBuffer = new byte[MESSAGE_LIMIT];
                        // Init the receive packet
                        DatagramPacket receivePacket;

                        // Check if server or client
                        if(endPoint == null) {
                            // Endpoint is null -> server only, listening to any connection
                            receivePacket = new DatagramPacket(receiveBuffer, receiveBuffer.length);
                        } else {
                            // Is client, listen only to server
                            receivePacket = new DatagramPacket(receiveBuffer, receiveBuffer.length,
                                    InetAddress.getByName(__server), Messenger.PORT);
                        }

                        /* DEBUG INFO */
                        System.out.println(String.format("Listening on %s:%d", endPoint, Messenger.PORT));

                        // Receiving packet and storing in buffer
                        receiveSocket.receive(receivePacket);
                        // Received packet, sending back to whatever...

                        // Init string representation of the message
                        String message = new String(receivePacket.getData());

                        /* DEBUG INFO */
                        System.out.println(String.format("Received message: %s", message));

                        IMessageReceiver.onMessage(message);
                    }

                    /* DEBUG INFO */
                    System.out.println(String.format("Stopped listening!"));

                } catch (IOException ex) {
                    ex.printStackTrace();
                    receiveSocket = null;
                    /* DEBUG INFO */
                    System.out.println(String.format("Stopped listening!"));
                }
            }
        });

        listenThread.start();
    }

    @Override
    public void stopListening() {
        listening = false;

        // Send disconnect message to server
        sendMessage("/disconnect", __server);

        // Check if server is not localhost, then we need to step out of the listen thread, by closing the socket
        if(!__server.equals("127.0.0.1")) {
            // Close socket to cancel .receive
            receiveSocket.close();
        }
    }

    @Override
    public void sendMessage(final String msgText, final String destinationEndPoint) {

        sendThread = new Thread(new Runnable() {
            @Override
            public void run() {

                try {

                    // TODO: Include host in sendBuffer like this: 9 127.0.0.1 7  message
                    // TODO: Where 9 is length of endpoint representation and 7 is the length of the message
                    // TODO: Do on receiving end as well with 2 byte read, read of endpoint length + 1, 3 byte read, message length read.

                    // Bind the socket to the destinationEndPoint and port
                    sendSocket = new DatagramSocket();
                    // Init the send buffer
                    byte[] sendBuffer = msgText.getBytes();
                    // Init the receive buffer
                    DatagramPacket sendPacket = new DatagramPacket(sendBuffer, sendBuffer.length,
                            InetAddress.getByName(destinationEndPoint), Messenger.PORT);
                    // Send the message
                    sendSocket.send(sendPacket);

                    // Send is complete, close the socket
                    sendSocket.close();
                    sendSocket = null;

                } catch(IOException ex) {
                    ex.printStackTrace();
                    sendSocket.close();
                    sendSocket = null;
                }
            }
        });

        sendThread.start();
    }
}
