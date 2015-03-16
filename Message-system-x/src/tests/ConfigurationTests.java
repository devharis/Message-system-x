package tests;

import client.Configuration;
import models.Message;
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
import java.util.Random;

import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

/**
 * Created by Fawk on 2015-03-16.
 */
public class ConfigurationTests {

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
            verify(oos).writeObject(null);

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

            oos.writeObject(anyObject());

        } catch (Exception ex) {

        }
    }

    @Test
    public void ConfigurationIsSetToMessageLoss() {

        String message = "Hello.";
        String newMessage = "";

        when(config.isMessageLoss()).thenReturn(true);
        final boolean isMessageLoss = config.isMessageLoss();

        if(isMessageLoss) {

            Random random = new Random();
            int first = random.nextInt(message.length());
            String split = message.substring(0, first);
            int second = random.nextInt(split.length() <= 0 ? 1 : split.length());
            int bound = split.length()-second;
            newMessage = message.substring(bound <= 0 ? 1 : bound);

            assertNotEquals(message, newMessage);
        }
    }

    @Test
    public void ConfigurationIsToSequenceProfile() {

        String message = "Hello.";

        int _sequenceCounter = 0;
        int _sequenceRangeIndex = 0;

        when(config.isSequenceLoss()).thenReturn(true);
        final boolean isSequenceLoss = config.isSequenceLoss();
        when(config.getSequenceLimit()).thenReturn(10);
        final int sequenceLimit = config.getSequenceLimit();
        when(config.getSequenceRange()).thenReturn(new int[]{40, 60});
        final int[] sequenceRange = config.getSequenceRange();

        int calls = 0;
        int failures = 0;
        int successes = 0;

        int loop = 50;

        for(int i = 0; i < loop; i++) {

            if (isSequenceLoss) {

                _sequenceCounter++;

                if (_sequenceCounter <= sequenceLimit) {
                    if (_sequenceCounter == sequenceLimit) {
                        _sequenceCounter = 0;
                        _sequenceRangeIndex = _sequenceRangeIndex == 0 ? 1 : 0;
                    }

                    int failureRate = sequenceRange[_sequenceRangeIndex];
                    int roll = (int) (Math.random() * 100);

                    calls++;

                    if ((roll - failureRate) <= 0) {
                        failures++;
                    } else {
                        successes++;
                    }
                }
            }
        }

        assertEquals(calls, loop);

        if(successes > failures) {
            assertTrue((loop - successes) == failures);
        } else {
            assertTrue((loop - failures) == successes);
        }
    }
}
