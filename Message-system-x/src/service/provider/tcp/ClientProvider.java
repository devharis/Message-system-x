package service.provider.tcp;

import client.Messenger;
import interfaces.IMessageReceiver;
import interfaces.IServiceProvider;
import models.Message;
import models.MessageType;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Random;

/**
 * This is one of the providers which provides a client-server
 * connection utilizing TCP. This provider acts as client.
 *
 * @author Created by Haris Kljajic & Oskar Karlsson on 2015-03-13.
 * Linneaus University - [2DV104] Software Architecture
 */
public class ClientProvider implements IServiceProvider {

    // variables
    private boolean _connected = false;
    private String _ip;
    private int _port;
    private Thread _connectThread;
    private Thread _listenThread;
    private Socket _socketConnection;
    private ObjectInputStream _incomingObj;
    private ObjectOutputStream _outgoingObj;
    private int _sequenceCounter = 0;
    private int _sequenceRangeIndex = 0;

    // constants
    private final static String DISCONNECT = "/disconnect";
    private final static String CONNECT_FAIL = "Could not connect to server \n";
    private final static String EMPTY_STRING = null;
    private final static String CLIENT_CONN_THREAD = "Client Connect Thread";
    private final static String CLIENT_LISTEN_THREAD = "Client Listen Thread";
    private final static String ADDRESS_SEPARATOR = ":";

    /**
     * This methods purpose is to create a socket binding
     * by establishing a connection to the server. Then it will listen
     * for incoming messages from the server.
     * @param endPoint Address of the endpoint.
     * @param messageReceiver Provided to use onMessage to receive message from server.
     * @throws Exception
     */
    @Override
    public void startListening(String endPoint, IMessageReceiver messageReceiver) throws Exception {
        try {
            extractConnection(endPoint);
            _socketConnection = new Socket(_ip, _port);
        } catch (IOException e) {
            // Server is offline
            messageReceiver.onMessage(new Message(EMPTY_STRING, CONNECT_FAIL, EMPTY_STRING, MessageType.SINGLE));
            throw new Exception(e);
        }
        establishConnection(messageReceiver);
        _connectThread.start();
    }

    /**
     * Creates a separate thread which only task is to connect to the server by
     * sending a connect message to it.
     * @param messageReceiver Provided to use onMessage to receive message from server.
     */
    private void establishConnection(final IMessageReceiver messageReceiver){

        _connectThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    _outgoingObj = new ObjectOutputStream(_socketConnection.getOutputStream());
                    _incomingObj = new ObjectInputStream(_socketConnection.getInputStream());

                    messageReceiver.onMessage((Message)_incomingObj.readObject());
                    sendMessage(new Message(
                            Messenger.nameField.getText(),
                            Messenger.nameField.getText(),
                            _socketConnection.getInetAddress().getHostAddress(),
                            MessageType.SINGLE), EMPTY_STRING);

                    _connected = true;

                    awaitMessage(messageReceiver);
                    _listenThread.start();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }, CLIENT_CONN_THREAD);
    }

    /**
     * Creates a separate thread which only task is to write messages to the server.
     * @param messageReceiver Provided to use onMessage to receive message from server.
     */
    private void awaitMessage(final IMessageReceiver messageReceiver){
        _listenThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    // Init message variable and while loop
                    Message message;
                    while(!((message = (Message)_incomingObj.readObject()).getMessageType().equals(MessageType.COMMAND))) {
                        messageReceiver.onMessage(message);
                    }
                } catch(Exception ex) {
                    throw new RuntimeException(ex);
                }
            }
        }, CLIENT_LISTEN_THREAD);
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
     * Stops listening to the server by first sending a message with a command
     * then closing streams.
     */
    @Override
    public void stopListening() {
        try {
            if (_connected) {
                sendMessage(new Message(
                        Messenger.nameField.getText(),
                        DISCONNECT,
                        _socketConnection.getInetAddress().getHostAddress(),
                        MessageType.COMMAND), EMPTY_STRING);
                _incomingObj.close();
                _outgoingObj.close();
            }
        } catch(IOException ex) {
            ex.printStackTrace();
        } finally {
            _connected = false;
        }
    }

    /**
     * Sends a message from the client to the server.
     * Emulates interruptions in message sent.
     */
    @Override
    public void sendMessage(final Message message, final String destinationEndPoint) {

        Thread sendThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    String newMessage = message.getMessage();

                    // Emulates interruptions depending on configuration
                    newMessage = emulateInterruption(message, newMessage, Thread.currentThread());
                    if(newMessage.equals(null))
                        return;

                    _outgoingObj.writeObject(new Message(
                            message.getName(),
                            newMessage,
                            message.getEndPoint(),
                            message.getMessageType()));

                    _outgoingObj.flush();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        });
        sendThread.start();
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
