package service.provider.tcp;

import client.Configuration;
import client.Messenger;
import interfaces.IMessageReceiver;
import interfaces.IServiceProvider;
import models.Message;
import models.MessageType;

import javax.annotation.Resource;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ResourceBundle;

/**
 * Created by devHaris on 2015-03-15.
 */
public class ClientProvider implements IServiceProvider {

    private Thread connectThread;
    private Thread listenThread;

    private Socket _socketConnection;
    private ObjectInputStream _incomingObj;
    private ObjectOutputStream _outgoingObj;
    private final static String DISCONNECT = "/disconnect";
    private final static String CONNECT_FAIL = "Could not connect to server \n";
    private String _ip;
    private int _port;
    private boolean connected = false;

    @Override
    public void startListening(String endPoint, IMessageReceiver messageReceiver) throws Exception {
        try {
            extractConnection(endPoint);
            _socketConnection = new Socket(_ip, _port);
        } catch (IOException e) {

            // Server is offline
            messageReceiver.onMessage(new Message(null, CONNECT_FAIL, null, MessageType.SINGLE));
            throw new Exception(e);
        }

        establishConnection(messageReceiver);

        connectThread.start();
    }

    private void establishConnection(final IMessageReceiver messageReceiver){
        connectThread = new Thread(new Runnable() {
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
                            MessageType.SINGLE), null);

                    connected = true;

                    awaitMessage(messageReceiver);
                    listenThread.start();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }, "Client Connect Thread");
    }

    private void awaitMessage(final IMessageReceiver messageReceiver){
        listenThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    // Init message variable and while loop
                    Message message;
                    while(!((message = (Message)_incomingObj.readObject()).getMessageType().equals(MessageType.COMMAND))) {
                        // Display on client UI
                        messageReceiver.onMessage(message);
                    }

                } catch(Exception ex) {
                    throw new RuntimeException(ex);
                }
            }
        }, "Client Listen Thread");
    }

    private void extractConnection(String endPoint) {
        _ip = endPoint.split(":")[0];
        _port = Integer.parseInt(endPoint.split(":")[1]);
    }

    @Override
    public void stopListening() {
        try {
            if (connected) {
                sendMessage(new Message(
                        Messenger.nameField.getText(),
                        DISCONNECT,
                        _socketConnection.getInetAddress().getHostAddress(),
                        MessageType.COMMAND), null);
                _incomingObj.close();
                _outgoingObj.close();
            }
        } catch(IOException ex) {
            ex.printStackTrace();
        } finally {
            connected = false;
        }
    }

    @Override
    public void sendMessage(Message message, String destinationEndPoint) {
        try {
            _outgoingObj.writeObject(message);
            _outgoingObj.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
