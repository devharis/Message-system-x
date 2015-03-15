package service.provider;

import client.Messenger;
import com.sun.xml.internal.ws.policy.privateutil.PolicyUtils;
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

    private Socket _socketConnection;
    private ObjectInputStream _incomingObj;
    private ObjectOutputStream _outgoingObj;
    private final static String DISCONNECT = "/disconnect";
    private String _ip;
    private int _port;

    @Override
    public void startListening(String endPoint, final IMessageReceiver messageReceiver) {
        try {
            ExtractConnection(endPoint);
            _socketConnection = new Socket(_ip, _port);
        } catch (IOException e) {
            e.printStackTrace();
        }

        Thread connectThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    _outgoingObj = new ObjectOutputStream(_socketConnection.getOutputStream());
                    _incomingObj = new ObjectInputStream(_socketConnection.getInputStream());

                    messageReceiver.onMessage(_incomingObj.readObject().toString());
                    sendMessage(Messenger.nameField.getText(), null);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, "Client Connect Thread");
        connectThread.start();

        Thread listenThread = new Thread(new Runnable() {
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
        listenThread.start();
    }

    private void ExtractConnection(String endPoint) {
        _ip = endPoint.split(":")[0];
        _port = Integer.parseInt(endPoint.split(":")[1]);
    }

    @Override
    public void stopListening() {
        try{
            sendMessage(DISCONNECT, null);
        } catch (Exception ex){
            ex.printStackTrace();
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
