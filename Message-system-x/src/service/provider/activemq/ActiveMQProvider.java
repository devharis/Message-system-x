package service.provider.activemq;

import com.sun.xml.internal.ws.policy.privateutil.PolicyUtils;
import interfaces.MessageReceiver;
import interfaces.ServiceProvider;

/**
 * Created by devHaris on 2015-03-11.
 */
public class ActiveMQProvider implements ServiceProvider {
    @Override
    public void startListening(String endPoint, MessageReceiver messageReceiver) {

    }

    @Override
    public void stopListening() {

    }

    @Override
    public void sendMessage(String msgText, String destinationEndPoint) {

    }
}
