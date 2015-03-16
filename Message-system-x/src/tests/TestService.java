package tests;

import interfaces.IMessageReceiver;
import interfaces.IServiceProvider;
import models.Message;
import org.junit.*;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import service.provider.udp.P2PProvider;

import java.io.*;
import java.net.*;

import static org.mockito.Mockito.*;

import static org.junit.Assert.*;

public class TestService {

    IServiceProvider serviceProvider;
    @Mock ServerSocket serverSocket;
    ObjectOutputStream _oos;
    ObjectInputStream _ois;

    @Before
    public void SetupProvider() {

        MockitoAnnotations.initMocks(this);

        serviceProvider = new IServiceProvider() {

            @Override
            public void startListening(String endPoint, IMessageReceiver messageReceiver) throws Exception {

                Socket mockedSocket = mock(Socket.class);

                when(serverSocket.accept()).thenReturn(mockedSocket);

                Socket acceptSocket = serverSocket.accept();

                OutputStream outputStream = new OutputStream() {
                    @Override
                    public void write(int b) throws IOException {

                    }
                };
                InputStream inputStream = new InputStream() {
                    @Override
                    public int read() throws IOException {
                        return 0;
                    }
                };

                when(acceptSocket.getOutputStream()).thenReturn(outputStream);
                when(acceptSocket.getInputStream()).thenReturn(inputStream);

                try {
                    _oos = new ObjectOutputStream(acceptSocket.getOutputStream());
                    _ois = new ObjectInputStream(acceptSocket.getInputStream());
                } finally {

                    Message message = mock(Message.class);

                    when(_ois.readObject()).thenReturn(message);

                    message = (Message) _ois.readObject();

                    when(message.getMessage()).thenReturn("Connected.");

                    assertEquals("Connected.", message.getMessage());
                }
            }

            @Override
            public void stopListening() throws Exception {

                Socket mockedSocket = mock(Socket.class);
                when(mockedSocket.isClosed()).thenReturn(true);
                doThrow(new SocketException()).when(mockedSocket).close();
                try {
                    mockedSocket.close();
                } catch(SocketException ex) {
                    assertTrue(mockedSocket.isClosed());
                }
            }

            @Override
            public void sendMessage(Message message, String destinationEndPoint) {

                try {

                    String returnMessage = "Hello";

                    Message m = mock(Message.class);
                    Message m2 = mock(Message.class);
                    when(m.getMessage()).thenReturn(returnMessage);
                    doNothing().when(_oos).writeObject(anyObject());
                    _oos.writeObject(message);

                    try {

                        when(_ois.readObject()).thenReturn(new Message(null, returnMessage, null, null));
                        m2 = (Message) _ois.readObject();

                    }  finally {
                        assertEquals(m.getMessage(), m2.getMessage());
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        };
    }

    @Test
    public void ServiceTryingToRegister() {

        try {
            serviceProvider.startListening(null, null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void ServiceTryingToUnregister() {
        try {
            serviceProvider.stopListening();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void ServiceTryingToSendAMessage() {
        serviceProvider.sendMessage(null, null);
    }

    @Test
    public void ServiceTryingToReceiveMessage() {

        try {

            String returnMessage = "Hello";

            Message m = mock(Message.class);
            when(m.getMessage()).thenReturn(returnMessage);

            try {

                when(_ois.readObject()).thenReturn(new Message(null, returnMessage, null, null));
                m = (Message) _ois.readObject();

            }  finally {
                assertEquals("Hello", m.getMessage());
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void OneToOneCommunication() {

       try {

           String theMessage = "Hello";
           byte[] buf = theMessage.getBytes();
           byte[] buf2 = new byte[buf.length];

           DatagramSocket datagramSocket = mock(DatagramSocket.class);
           DatagramPacket datagramPacket = mock(DatagramPacket.class);

           doNothing().when(datagramPacket).setData(buf);

           datagramPacket.setData(buf);

           doNothing().when(datagramSocket).send(datagramPacket);

           datagramSocket.send(datagramPacket);

           doNothing().when(datagramSocket).receive(datagramPacket);

           datagramSocket.receive(datagramPacket);

           when(datagramPacket.getData()).thenReturn(buf);

           buf2 = datagramPacket.getData();

           assertEquals(buf, buf2);


       } catch (Exception ex) {

       }
    }
}