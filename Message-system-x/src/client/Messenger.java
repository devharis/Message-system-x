package client;

import service.provider.activemq.ActiveMQProvider;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Created by devHaris on 2015-03-13.
 */

public class Messenger {

    // variables
    private ActiveMQProvider activeMQProvider;

    public static void main(String[] args) {
        // Init app
        Messenger messenger = new Messenger();
        messenger.Initialize();
    }

    public void Initialize(){
        // TODO: Create the UI or console.

        // TODO: Init a loop which awaits actions
    }

    public void onConnect(){
        // TODO: Init a MQProvider and register service
    }

    public void onDisconnect(){
        // TODO: Destroy a MQProvider and de-register service
    }

    public void onSendMessage(){
        // TODO: User sends a message, pass it to service
    }
}
