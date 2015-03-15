package interfaces;

import models.Message;

/**
 * This interface should be implemented by the classes that are interested to provide communication mechanism between different services.
 * 
 * @author M. Usman Iftikhar & Yifan Ruan
 */
public interface IServiceProvider {
 
    /**
     * This method enables a tcp to listen for messages on the given endpoint.
     * @param endPoint
     * @param messageReceiver
     */
    public void startListening(String endPoint, IMessageReceiver messageReceiver) throws Exception;
    
    /**
     * This method stops a tcp for listening to the incoming messages.
     */
    public void stopListening() throws Exception;
    
    /**
     * With this method, a tcp can send a message to the other tcp.
     * @param message
     * @param destinationEndPoint
     */
    public void sendMessage(Message message, String destinationEndPoint);
}
