package tests;

import client.Configuration;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

/**
 * Created by Fawk on 2015-03-16.
 */
public class TestConfiguration {

    Configuration config;
    Socket sendSocket;

    @Before
    public void InitConfiguration() {
        config = mock(Configuration.class);
        sendSocket = mock(Socket.class);
    }

    @Test
    public void ConfigurationIsSetToCommunicationDelay() {

        try {
            final String theMessage = "Hello";

            when(config.getMessageDelay()).thenReturn(1000);
            final int delay = config.getMessageDelay();

            when(sendSocket.getOutputStream()).thenReturn(new OutputStream() {
                @Override
                public void write(int b) throws IOException {

                }
            });

            ObjectOutputStream oos = new ObjectOutputStream(sendSocket.getOutputStream());

            final long before = System.currentTimeMillis();

            doAnswer(new Answer() {
                @Override
                public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
                    final Thread t = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                Thread.currentThread().sleep(delay);

                                assertTrue(((System.currentTimeMillis() - before) / 1000) >= delay);

                            } catch (Exception ex) {}
                        }
                    });
                    t.start();
                    return theMessage;
                }
            }).when(oos).writeObject(anyObject());

            oos.writeObject(anyObject());

        } catch (Exception ex) {

        }
    }

    @Test
    public void ConfigurationIsSetToMessageLoss() {


    }

    @Test
    public void ConfigurationIsToSequenceProfile() {


    }

}
