package service.provider.udp;

import interfaces.IMessageReceiver;
import interfaces.IServiceProvider;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

/**
 * Created by Fawk on 2015-03-15.
 */
public class P2PProvider implements IServiceProvider {

    boolean listening = false;

    DatagramSocket receiveSocket;
    DatagramSocket sendSocket;
    Thread listenThread;
    Thread sendThread;

    String listenEndPoint;
    String sendEndPoint;

    private final static String STOP_LISTENING = "/stop";

    @Override
    public void startListening(final String endPoint, final IMessageReceiver messageReceiver) throws Exception {

        listenEndPoint = endPoint;
        String[] ad = extractConnection(listenEndPoint);
        final String listenIp = ad[0];
        final int listenPort = Integer.parseInt(ad[1]);

        listening = true;

        listenThread = new Thread(new Runnable() {
            @Override
            public void run() {

                try {

                    // Bind socket to endpoint and port
                    receiveSocket = new DatagramSocket(listenPort);

                    // Listen loop
                    while(listening) {
                        // Waiting for packets

                        // Init receive buffer
                        byte[] receiveBuffer = new byte[200];
                        // Init the receive packet
                        DatagramPacket receivePacket;

                        receivePacket = new DatagramPacket(receiveBuffer, receiveBuffer.length,
                                InetAddress.getByName(listenIp), listenPort);


                        /* DEBUG INFO */
                        System.out.println(String.format("Listening on %s:%d", listenIp, listenPort));

                        // Receiving packet and storing in buffer
                        receiveSocket.receive(receivePacket);
                        // Received packet, sending back to whatever...

                        // Init string representation of the message
                        String message = new String(receivePacket.getData());

                        /* DEBUG INFO */
                        System.out.println(String.format("Received message: %s", message));

                        messageReceiver.onMessage(message);
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
    public void stopListening() throws Exception {
        listening = false;
        sendMessage(STOP_LISTENING, listenEndPoint);
    }

    @Override
    public void sendMessage(final String msgText, final String destinationEndPoint) {

        sendEndPoint = destinationEndPoint;
        String[] ad = extractConnection(destinationEndPoint);
        final String sendIp = ad[0];
        final int sendPort = Integer.parseInt(ad[1]);

        sendThread = new Thread(new Runnable() {
            @Override
            public void run() {

                try {

                    // Bind the socket to the destinationEndPoint and port
                    sendSocket = new DatagramSocket();
                    // Init the send buffer
                    byte[] sendBuffer = msgText.getBytes();
                    // Init the receive buffer
                    DatagramPacket sendPacket = new DatagramPacket(sendBuffer, sendBuffer.length,
                            InetAddress.getByName(sendIp), sendPort);
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

    private String[] extractConnection(String endPoint) {
        return endPoint.split(":");
    }
}
