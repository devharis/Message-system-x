package service.provider.activemq;

import client.Messenger;
import com.sun.xml.internal.ws.policy.privateutil.PolicyUtils;
import interfaces.MessageReceiver;
import interfaces.ServiceProvider;
import sun.misc.resources.Messages_es;

import javax.xml.ws.Endpoint;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

/**
 * Created by devHaris on 2015-03-11.
 */
public class ActiveMQProvider implements ServiceProvider {

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

    @Override
    public void startListening(final String endPoint, final MessageReceiver messageReceiver) {

        listening = true;

        listenThread = new Thread(new Runnable() {
            @Override
            public void run() {

                try {
                    // Bind socket to endpoint and port
                    receiveSocket = new DatagramSocket(Messenger.PORT, InetAddress.getByName(endPoint));
                    // Init receive buffer
                    byte[] receiveBuffer = new byte[MESSAGE_LIMIT];
                    // Init the receive packet
                    DatagramPacket receivePacket = new DatagramPacket(receiveBuffer, receiveBuffer.length);

                    // Listen loop
                    while(listening) {
                        // Waiting for packets

                        /* DEBUG INFO */
                        System.out.println(String.format("Listening on %s:%d", endPoint, Messenger.PORT));

                        // Receiving packet and storing in buffer
                        receiveSocket.receive(receivePacket);
                        // Received packet, sending back to whatever...

                        /* DEBUG INFO */
                        System.out.println(String.format("Received message: %s", receiveBuffer.toString()));

                        messageReceiver.onMessage(receiveBuffer.toString());
                    }

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
        receiveSocket.close();
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
                    sendSocket = new DatagramSocket(Messenger.PORT, InetAddress.getByName(destinationEndPoint));
                    // Init the send buffer
                    byte[] sendBuffer = msgText.getBytes();
                    // Init the receive buffer
                    DatagramPacket sendPacket = new DatagramPacket(sendBuffer, sendBuffer.length);
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
