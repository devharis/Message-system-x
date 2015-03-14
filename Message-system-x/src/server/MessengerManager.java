package server;

import interfaces.IMessageReceiver;
import interfaces.IServiceProvider;
import models.Client;
import service.provider.activemq.ActiveMQProvider;

import java.util.ArrayList;

/**
 * Created by devHaris on 2015-03-14.
 */
public class MessengerManager {

    private ArrayList<Client> clientList;
    private IServiceProvider _serviceProvider;

    // Constructor
    public MessengerManager(){
        this(new ActiveMQProvider());
    }

    public MessengerManager(IServiceProvider serviceProvider){
        _serviceProvider = serviceProvider;
    }

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
        _serviceProvider.startListening(null, new IMessageReceiver() {
            @Override
            public void onMessage(String message) {
                // GL HF
                System.out.println("Server: " + message);
            }
        });
    }
}
