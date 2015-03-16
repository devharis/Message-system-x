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

    private int sequenceCounter = 0;
    private int sequenceRangeIndex = 0;

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
                        byte[] data = new byte[4];

                        // Init the receive packet
                        DatagramPacket receivePacket = new DatagramPacket(data, data.length );

                        // Receiving packet and storing in buffer
                        receiveSocket.receive(receivePacket);
                        // Received packet, sending back to whatever...

                        int len = 0;
                        // byte[] -> int
                        for (int i = 0; i < 4; ++i) {
                            len |= (data[3-i] & 0xff) << (i << 3);
                        }

                        byte[] buffer = new byte[len];
                        receivePacket = new DatagramPacket(buffer, buffer.length );
                        receiveSocket.receive(receivePacket);

                        ByteArrayInputStream baos = new ByteArrayInputStream(buffer);
                        ObjectInputStream oos = new ObjectInputStream(baos);
                        Message message = (Message)oos.readObject();

                        Message newMessage = new Message(
                                message.getName(),
                                String.format("%s\n", message.getMessage()),
                                message.getEndPoint(),
                                MessageType.BROADCAST);

                        messageReceiver.onMessage(newMessage);
                    }

                } catch (Exception ex) {
                    ex.printStackTrace();
                    receiveSocket = null;
                }
            }
        });

        listenThread.start();
    }

    @Override
    public void stopListening() throws Exception {
        listening = false;
        sendMessage(new Message(
                null,
                STOP_LISTENING,
                receiveSocket.getInetAddress().getHostAddress(),
                MessageType.SINGLE) ,listenEndPoint);
    }

    @Override
    public void sendMessage(final Message message, final String destinationEndPoint) {

        sendEndPoint = destinationEndPoint;
        String[] ad = extractConnection(destinationEndPoint);
        final String sendIp = ad[0];
        final int sendPort = (Integer.parseInt(ad[1]));

        sendThread = new Thread(new Runnable() {
            @Override
            public void run() {

                try {
                    String newMessage = message.getMessage();

                    if(Messenger.messageDelay > 0)
                        Thread.currentThread().sleep(Messenger.messageDelay);

                    if(Messenger.messageLoss) {

                        Random random = new Random();
                        int first = random.nextInt(message.getMessage().length());
                        String split = message.getMessage().substring(0, first);
                        int second = random.nextInt(split.length() <= 0 ? 1 : split.length());
                        int bound = split.length()-second;
                        newMessage = message.getMessage().substring(bound <= 0 ? 1 : bound);
                    }

                    if(Messenger.sequenceLoss) {
                        sequenceCounter++;
                        if(sequenceCounter == Messenger.sequenceLimit) {
                            sequenceCounter = 0;
                            sequenceRangeIndex = sequenceRangeIndex == 0 ? 1 : 0;

                            int failureRate = Messenger.sequenceRange[sequenceRangeIndex];
                            int roll = (int)(Math.random() * 100);

                            if((roll - failureRate) <= 0)
                                return;
                        }
                    }

                    // Write object to stream
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    ObjectOutputStream oos = new ObjectOutputStream(baos);
                    //oos.writeObject(message);
                    oos.writeObject(new Message(message.getName(), newMessage, message.getEndPoint(), message.getMessageType()));
                    oos.flush();

                    // get the byte array of the object
                    byte[] buf = baos.toByteArray();

                    int number = buf.length;
                    byte[] data = new byte[4];

                    // int -> byte[]
                    for (int i = 0; i < 4; ++i) {
                        int shift = i << 3; // i * 8
                        data[3-i] = (byte)((number & (0xff << shift)) >>> shift);
                    }

                    // Bind the socket to the destinationEndPoint and port
                    sendSocket = new DatagramSocket();

                    DatagramPacket sendPacket = new DatagramPacket(data, 4, InetAddress.getByName(sendIp), sendPort);

                    // Send the object size
                    sendSocket.send(sendPacket);

                    // now send the object
                    sendPacket = new DatagramPacket(buf, buf.length, InetAddress.getByName(sendIp), sendPort);
                    sendSocket.send(sendPacket);

                    // Send is complete, close the socket
                    sendSocket.close();
                    sendSocket = null;

                } catch(Exception ex) {
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
