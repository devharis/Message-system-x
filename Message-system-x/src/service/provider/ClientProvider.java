package service.provider;

import client.Messenger;
import interfaces.IMessageReceiver;
import interfaces.IServiceProvider;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

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
    private String _ip;
    private int _port;
    private boolean connected = false;

    @Override
    public void startListening(String endPoint, IMessageReceiver messageReceiver) {
        try {
            extractConnection(endPoint);
            _socketConnection = new Socket(_ip, _port);
        } catch (IOException e) {

            // Server is offline
            messageReceiver.onMessage("Could not connect to server");

            e.printStackTrace();
        }

        establishConnection(messageReceiver);
        connectThread.start();
    }

    private void establishConnection(final IMessageReceiver messageReceiver) {
        connectThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    _outgoingObj = new ObjectOutputStream(_socketConnection.getOutputStream());
                    _incomingObj = new ObjectInputStream(_socketConnection.getInputStream());

                    messageReceiver.onMessage(_incomingObj.readObject().toString());
                    sendMessage(Messenger.nameField.getText(), null);

                    connected = true;

                    awaitMessage(messageReceiver);
                } catch (Exception e) {
                    e.printStackTrace();
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
                    String message;
                    while(((message = (String)_incomingObj.readObject()) != DISCONNECT)) {
                        // Display on client UI
                        messageReceiver.onMessage(message);
                    }

                } catch(Exception ex) {
                    ex.printStackTrace();
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
                sendMessage(DISCONNECT, null);
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
    public void sendMessage(String msgText, String destinationEndPoint) {
        try {
            _outgoingObj.writeObject(msgText);
            _outgoingObj.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
