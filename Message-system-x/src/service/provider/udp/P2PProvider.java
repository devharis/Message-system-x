package service.provider.udp;

import client.Messenger;
import interfaces.IMessageReceiver;
import interfaces.IServiceProvider;
import models.Message;
import models.MessageType;

import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Random;

/**
 * This is one of the providers which provides a peer-to-peer
 * connection utilizing UDP. This provider acts as client and server.
 *
 * @author Created by Haris Kljajic & Oskar Karlsson on 2015-03-13.
 * Linneaus University - [2DV104] Software Architecture
 */
public class P2PProvider implements IServiceProvider {

    // variables
    private boolean _listening = false;
    private DatagramSocket _receiveSocket;
    private DatagramSocket _sendSocket;
    private String _ip;
    private int _port;
    private Thread _listenThread;
    private Thread _sendThread;
    private String _listenEndPoint;
    private String _sendEndPoint;
    private int _sequenceCounter = 0;
    private int _sequenceRangeIndex = 0;

    // constants
    private final static String STOP_LISTENING = "/stop";
    private final static String ADDRESS_SEPARATOR = ":";

    /**
     * This method creates a separate thread on which it awaits
     * an incoming request.
     * @param endPoint Uses endpoint to create DatagramSocket with port
     * @param messageReceiver Provided to use onMessage to receive message from server.
     * @throws Exception
     */
    @Override
    public void startListening(final String endPoint, final IMessageReceiver messageReceiver) throws Exception {

        _listenEndPoint = endPoint;
        extractConnection(_listenEndPoint);

        _listening = true;

        _listenThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    // Bind socket to endpoint and port
                    _receiveSocket = new DatagramSocket(_port);

                    // Listen loop
                    while(_listening) {
                        // Init receive buffer
                        byte[] data = new byte[4];

                        // Init the receive packet
                        DatagramPacket receivePacket = new DatagramPacket(data, data.length );

                        // Receiving packet and storing in buffer
                        _receiveSocket.receive(receivePacket);
                        // Received packet, sending back to whatever...

                        int len = 0;
                        // byte[] -> int
                        for (int i = 0; i < 4; ++i) {
                            len |= (data[3-i] & 0xff) << (i << 3);
                        }

                        byte[] buffer = new byte[len];
                        receivePacket = new DatagramPacket(buffer, buffer.length );
                        _receiveSocket.receive(receivePacket);

                        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(buffer);
                        ObjectInputStream objectInputStream = new ObjectInputStream(byteArrayInputStream);
                        Message message = (Message)objectInputStream.readObject();

                        Message newMessage = new Message(
                                message.getName(),
                                String.format("%s\n", message.getMessage()),
                                message.getEndPoint(),
                                MessageType.BROADCAST);

                        messageReceiver.onMessage(newMessage);
                    }

                } catch (Exception ex) {
                    ex.printStackTrace();
                    _receiveSocket = null;
                }
            }
        });

        _listenThread.start();
    }

    /**
     * Stop listening to incoming messages.
     * Sending a message with disconnection to endpoint.
     * @throws Exception
     */
    @Override
    public void stopListening() throws Exception {
        _listening = false;
        sendMessage(new Message(
                null,
                STOP_LISTENING,
                _receiveSocket.getInetAddress().getHostAddress(),
                MessageType.SINGLE), _listenEndPoint);
    }

    /**
     * Creates a separate thread which is used to communicate with the
     * other peer endpoint.  Emulates interruptions in message sent.
     * @param message
     * @param destinationEndPoint
     */
    @Override
    public void sendMessage(final Message message, final String destinationEndPoint) {

        _sendEndPoint = destinationEndPoint;
        extractConnection(destinationEndPoint);

        _sendThread = new Thread(new Runnable() {
            @Override
            public void run() {

                try {
                    String newMessage = message.getMessage();

                    // Emulates interruptions depending on configuration
                    newMessage = emulateInterruption(message, newMessage, Thread.currentThread());
                    if(newMessage.equals(null))
                        return;

                    // Write object to stream
                    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                    ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
                    //oos.writeObject(message);
                    objectOutputStream.writeObject(new Message(message.getName(), newMessage, message.getEndPoint(), message.getMessageType()));
                    objectOutputStream.flush();

                    // get the byte array of the object
                    byte[] buf = byteArrayOutputStream.toByteArray();

                    int number = buf.length;
                    byte[] data = new byte[4];

                    // int -> byte[]
                    for (int i = 0; i < 4; ++i) {
                        int shift = i << 3; // i * 8
                        data[3-i] = (byte)((number & (0xff << shift)) >>> shift);
                    }

                    // Bind the socket to the destinationEndPoint and port
                    _sendSocket = new DatagramSocket();

                    DatagramPacket sendPacket = new DatagramPacket(data, 4, InetAddress.getByName(_ip), _port);

                    // Send the object size
                    _sendSocket.send(sendPacket);

                    // now send the object
                    sendPacket = new DatagramPacket(buf, buf.length, InetAddress.getByName(_ip), _port);
                    _sendSocket.send(sendPacket);

                    // Send is complete, close the socket
                    _sendSocket.close();
                    _sendSocket = null;

                } catch(Exception ex) {
                    ex.printStackTrace();
                    _sendSocket.close();
                    _sendSocket = null;
                }
            }
        });
        _sendThread.start();
    }

    /**
     * This method is a helper to extract address to separate ip and port.
     * @param endPoint The incoming full address
     */
    private void extractConnection(String endPoint) {
        _ip = endPoint.split(ADDRESS_SEPARATOR)[0];
        _port = Integer.parseInt(endPoint.split(ADDRESS_SEPARATOR)[1]);
    }

    /**
     * Emulates interruption using configuration from messengerBundle, loaded in Configuration.
     */
    public String emulateInterruption(Message message, String newMessage, Thread currentThread) throws InterruptedException {
        if(Messenger.messageDelay > 0)
            currentThread.sleep(Messenger.messageDelay);

        if(Messenger.messageLoss) {

            Random random = new Random();
            int first = random.nextInt(message.getMessage().length());
            String split = message.getMessage().substring(0, first);
            int second = random.nextInt(split.length() <= 0 ? 1 : split.length());
            int bound = split.length()-second;
            newMessage = message.getMessage().substring(bound <= 0 ? 1 : bound);
        }

        if(Messenger.sequenceLoss && !message.getMessageType().equals(MessageType.SINGLE)) {

            _sequenceCounter++;

            if(_sequenceCounter <= Messenger.sequenceLimit) {
                if(_sequenceCounter == Messenger.sequenceLimit) {
                    _sequenceCounter = 0;
                    _sequenceRangeIndex = _sequenceRangeIndex == 0 ? 1 : 0;
                }

                int failureRate = Messenger.sequenceRange[_sequenceRangeIndex];
                int roll = (int)(Math.random() * 100);

                if((roll - failureRate) <= 0)
                    return null;
            }
        }
        return newMessage;
    }
}
