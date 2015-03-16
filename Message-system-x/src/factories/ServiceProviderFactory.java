package factories;

import interfaces.IServiceProvider;
import models.ProviderType;
import service.provider.tcp.ClientProvider;
import service.provider.tcp.ServerProvider;
import service.provider.udp.P2PProvider;

/**
 * This class helps to choose a TCP/UDP provider.
 *
 * @author Created by Haris Kljajic & Oskar Karlsson on 2015-03-13.
 * Linneaus University - [2DV104] Software Architecture
 */
public class ServiceProviderFactory {

    /**
     * This method helps to choose a TCP/UDP provider from list of available TCP/UDP providers.
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
