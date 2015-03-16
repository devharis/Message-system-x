package client;

import models.ProviderType;

import java.io.*;
import java.util.Properties;

/**
 * This class is used to separate all configurations
 * for the system to one independent part. It's used to
 * read configurations and provide them.
 *
 * @author Created by Haris Kljajic & Oskar Karlsson on 2015-03-13.
 * Linneaus University - [2DV104] Software Architecture
 */
public class Configuration {

    // variables
    private Properties _properties;
    private ProviderType _providerType;
    private boolean _messageLoss;
    private boolean _sequenceLoss;
    private int _messageDelay;
    private int _sequenceLimit;
    private int[] _sequenceRange;

    // constants
    private final static String FILE_MISSING_ERROR = "Properties file is missing in src folder! Please add messengerBundle.properties!";
    private final static String FILE_ERROR = "Both clientServer and peer2peer can't be the same!";
    private final static String PROP_NAME = "/messengerBundle.properties";

    /**
     * Gets the type of set provider.
     * @return _providerType
     */
    public ProviderType getProviderType() {
        return _providerType;
    }

    /**
     * Checks if message loss is activated.
     * @return _messageLoss
     */
    public boolean isMessageLoss() {
        return _messageLoss;
    }

    /**
     * Gets the message delay.
     * @return _messageDelay
     */
    public int getMessageDelay() {
        return _messageDelay;
    }

    /**
     * Gets the sequence limit.
     * @return _sequenceLimit
     */
    public int getSequenceLimit() {
        return _sequenceLimit;
    }

    /**
     * Gets the sequence range.
     * @return _sequenceRange
     */
    public int[] getSequenceRange() {
        return _sequenceRange;
    }

    /**
     * Checks if sequence loss is activated.
     * @return _sequenceLoss
     */
    public boolean isSequenceLoss() {
        return _sequenceLoss;
    }

    /**
     * Default constructor which reads application setup from file.
     * Provides application with configuration data.
     */
    public Configuration() {

        _properties = new Properties();

        try {
            InputStream inputStream = Messenger.class.getResourceAsStream(PROP_NAME);
            _properties.load(inputStream);
        } catch (IOException e) {
            System.out.println(FILE_MISSING_ERROR);
            System.exit(0);
        }

        boolean clientServer = Boolean.valueOf(_properties.getProperty("clientServer"));
        boolean peer2peer = Boolean.valueOf(_properties.getProperty("peer2peer"));

        if(!(clientServer && peer2peer || !clientServer && !peer2peer)) {
            _providerType = clientServer ? ProviderType.TcpClient : ProviderType.UdpP2P;
        } else {
            System.out.println(FILE_ERROR);
        }

        _messageDelay = Integer.parseInt(_properties.getProperty("messageDelay"));
        _messageLoss = Boolean.valueOf(_properties.getProperty("messageLoss"));
        _sequenceLoss = Boolean.valueOf(_properties.getProperty("sequenceLoss"));
        _sequenceLimit = Integer.parseInt(_properties.getProperty("sequenceLimit"));

        String[] range = _properties.getProperty("sequenceRange").split("-");
        if(range.length == 2) {
            _sequenceRange = new int[]{Integer.parseInt(range[0]), Integer.parseInt(range[1])};
        }
    }
}
