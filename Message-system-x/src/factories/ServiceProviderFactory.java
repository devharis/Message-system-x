package factories;

import interfaces.IServiceProvider;
import service.provider.tcp.ServerProvider;

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
    public static IServiceProvider createServiceProvider() {

	    return new ServerProvider();
    }
}
