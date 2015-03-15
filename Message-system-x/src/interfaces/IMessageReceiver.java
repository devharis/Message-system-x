package interfaces;

/**
 * This interface enables a tcp to listen for messages from tcp provider.
 * 
 * @author M. Usman Iftikhar & Yifan Ruan
 */
public interface IMessageReceiver {
    
    /**
     * The tcp provider will notify incoming messages through this method.
     * @param message
     */
    public void onMessage(String message);
}
