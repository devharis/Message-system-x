package client;

import interfaces.IMessageReceiver;
import interfaces.IServiceProvider;
import service.provider.TestProvider;

import java.io.BufferedReader;
import java.io.InputStreamReader;

/**
 * Created by Fawk on 2015-03-15.
 */
public class TestClient {

    public static void main(String[] args) {
        IServiceProvider s = new TestProvider();

        try {

            s.startListening("127.0.0.1:12346", new IMessageReceiver() {
                @Override
                public void onMessage(String message) {
                    System.out.println(message);
                }
            });

            BufferedReader console = new BufferedReader(new InputStreamReader(System.in));
            while(true) {
                String text = console.readLine();
                s.sendMessage(text, "127.0.0.1:12345");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
