package server;

import interfaces.MessageReceiver;
import models.Client;
import service.provider.activemq.ActiveMQProvider;

import java.util.ArrayList;

/**
 * Created by devHaris on 2015-03-14.
 */
public class MessengerManager {

    private ArrayList<Client> clientList;
    private ActiveMQProvider activeMQProvider;

    public static void main(String[] args) {
        // Init server
        MessengerManager messenger = new MessengerManager();
        messenger.Initialize();
    }

    private void Initialize() {
        clientList = new ArrayList<Client>();
        onIncomingConnect();
    }

    private void onIncomingConnect(){
        activeMQProvider = new ActiveMQProvider();

        activeMQProvider.startListening("127.0.0.1", new MessageReceiver() {
            @Override
            public void onMessage(String message) {
                // GL HF
                System.out.println("Server: " + message);
            }
        });
    }
}
