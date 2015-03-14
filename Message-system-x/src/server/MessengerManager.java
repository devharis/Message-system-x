package server;

import models.Message;

import java.util.PriorityQueue;
import java.util.Queue;

/**
 * Created by devHaris on 2015-03-14.
 */
public class MessengerManager {

    private Queue<Message> queue;

    public static void main(String[] args) {
        // Init server
        MessengerManager messenger = new MessengerManager();
        messenger.Initialize();
    }

    private void Initialize() {
        queue = new PriorityQueue<Message>();
    }
}
