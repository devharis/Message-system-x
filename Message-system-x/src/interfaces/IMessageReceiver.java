package interfaces;

/**
 * This interface enables a service to listen for messages from service provider.
 * 
 * @author M. Usman Iftikhar & Yifan Ruan
 */
public interface IMessageReceiver {
    
    /**
     * The service provider will notify incoming messages through this method.
     * @param message
     */
    public void onMessage(String message);
}
