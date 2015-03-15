package factories;

import interfaces.IServiceProvider;
import models.ProviderType;
import service.provider.tcp.ClientProvider;
import service.provider.tcp.ServerProvider;
import service.provider.udp.P2PProvider;

/**
 * This class helps to choose a tcp provider.
 * 
 * @author M. Usman Iftikhar & Yifan Ruan
 */
public class ServiceProviderFactory {

    /**
     * This method helps to choose a tcp provider from list of available tcp providers.
     * @return interfaces.IServiceProvider
     */
    public static IServiceProvider createServiceProvider(ProviderType providerType) {

        IServiceProvider serviceProvider = null;

        switch(providerType) {
            case TcpClient:
                serviceProvider = new ClientProvider();
                break;
            case TcpServer:
                serviceProvider = new ServerProvider();
                break;
            case UdpP2P:
                serviceProvider = new P2PProvider();
                break;
            case TcpP2P:
                // TODO: Implement TCP P2P protocols
                break;
        }

	    return serviceProvider;
    }
}
