package service.provider;

import interfaces.IMessageReceiver;
import interfaces.IServiceProvider;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by Fawk on 2015-03-15.
 */
public class P2PProvider implements IServiceProvider {

    Thread connectThread;
    Thread listenThread;

    ServerSocket acceptSocket;
    Socket receiveSocket;
    Socket connectSocket;

    ObjectInputStream objectInputStream;
    ObjectOutputStream objectOutputStream;

    String _ip;
    int _port;

    boolean connected;

    private final static String CONNECTED = "Connected.";

    @Override
    public void startListening(String endPoint, final IMessageReceiver messageReceiver) throws Exception {

        extractConnection(endPoint);

        connectThread = new Thread(new Runnable() {

            @Override
            public void run() {

                try {

                    acceptSocket = new ServerSocket(_port);

                    listenThread = new Thread(new Runnable() {

                        @Override
                        public void run() {

                            try {

                                receiveSocket = acceptSocket.accept();
                                // Connected, I think?

                                objectOutputStream = new ObjectOutputStream(receiveSocket.getOutputStream());
                                objectInputStream = new ObjectInputStream(receiveSocket.getInputStream());

                                connected = true;

                                // Is there a message waiting?
                                String message;

                                while(!(message = (String)objectInputStream.readObject()).equals("BYE")) {

                                    if(message.equals(CONNECTED)) {

                                        // A connection was established, connect back to the endpoint
                                        connect(receiveSocket.getInetAddress().getHostAddress(),
                                                receiveSocket.getPort(),
                                                messageReceiver);

                                    }
                                    // Do something with the message
                                    messageReceiver.onMessage(message);
                                }

                                // Doesn't want to talk anymore

                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    });

                    listenThread.start();

                } catch(Exception e) {
                    e.printStackTrace();
                }
            }
        });

        connectThread.start();
    }

    @Override
    public void stopListening() throws Exception {

    }

    @Override
    public void sendMessage(String msgText, String destinationEndPoint) {

        try {

            if(connected) {

                objectOutputStream.writeObject(msgText);
                objectOutputStream.flush();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void connect(final String ip, final int port, final IMessageReceiver messageReceiver) {

        Thread t = new Thread(new Runnable() {

            @Override
            public void run() {

                try {

                    // Does listener exist on this ip and port?
                    connectSocket = new Socket(ip, port);

                    // It did, write to it
                    ObjectOutputStream o = new ObjectOutputStream(connectSocket.getOutputStream());
                    o.writeObject("Connected.");
                    o.flush();

                    // Start listening
                    startListening(String.format("%s:%s", ip, port), messageReceiver);

                } catch (Exception e1) {

                    try {

                        // There was no listener on this ip and port, create your own
                        startListening(String.format("%s:%s", ip, port), messageReceiver);

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        t.start();
    }

    private void extractConnection(String endPoint) {
        _ip = endPoint.split(":")[0];
        _port = Integer.parseInt(endPoint.split(":")[1]);
    }

}
