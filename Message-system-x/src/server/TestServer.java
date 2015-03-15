package server;

import interfaces.IMessageReceiver;
import interfaces.IServiceProvider;
import service.provider.TestProvider;

/**
 * Created by Fawk on 2015-03-15.
 */
public class TestServer {

    public static void main(String[] args) {
        final IServiceProvider s = new TestProvider();
        try {
            s.startListening("127.0.0.1:12345", new IMessageReceiver() {
                @Override
                public void onMessage(String message) {
                    System.out.println(message);
                    s.sendMessage(message, "127.0.0.1:12346");
                }
            });
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

}
