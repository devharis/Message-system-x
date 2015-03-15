package interfaces;

import java.io.IOException;

/**
 * This interface should be implemented by the classes that are interested to provide communication mechanism between different services.
 * 
 * @author M. Usman Iftikhar & Yifan Ruan
 */
public interface IServiceProvider {
 
    /**
     * This method enables a service to listen for messages on the given endpoint.
     * @param endPoint
     * @param messageReceiver
     */
    public void startListening(String endPoint, IMessageReceiver messageReceiver) throws Exception;
    
    /**
     * This method stops a service for listening to the incoming messages. 
     */
    public void stopListening();
    
    /**
     * With this method, a service can send a message to the other service.
     * @param msgText
     * @param destinationEndPoint
     */
    public void sendMessage(String msgText, String destinationEndPoint);
}
