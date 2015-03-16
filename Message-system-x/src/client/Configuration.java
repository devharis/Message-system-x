package client;

import models.ProviderType;

import java.io.*;
import java.util.Properties;

/**
 * Created by Fawk on 2015-03-13.
 */
public class Configuration {

    private final static String PROP_NAME = "/messengerBundle.properties";

    public ProviderType getProviderType() {
        return _providerType;
    }

    private ProviderType _providerType;

    public boolean isMessageLoss() {
        return messageLoss;
    }

    public int getMessageDelay() {
        return messageDelay;
    }

    private boolean messageLoss;
    private int messageDelay;
    private boolean sequenceLoss;

    public int getSequenceLimit() {
        return sequenceLimit;
    }

    public int[] getSequenceRange() {
        return sequenceRange;
    }

    public boolean isSequenceLoss() {
        return sequenceLoss;
    }

    private int sequenceLimit;
    private int[] sequenceRange;

    public Configuration() {

        Properties properties = new Properties();
        try {
            InputStream is = Messenger.class.getResourceAsStream(PROP_NAME);
            properties.load(is);
        } catch (IOException e) {
            System.out.println("Properties file is missing in src folder! Please add messengerBundle.properties!");
            System.exit(0);
        }

        boolean clientServer = Boolean.valueOf(properties.getProperty("clientServer"));
        boolean peer2peer = Boolean.valueOf(properties.getProperty("peer2peer"));

        if(!(clientServer && peer2peer || !clientServer && !peer2peer)) {
            _providerType = clientServer ? ProviderType.TcpClient : ProviderType.UdpP2P;
        } else {
            System.out.println("Both clientServer and peer2peer can't be the same!");
        }

        messageDelay = Integer.parseInt(properties.getProperty("messageDelay"));
        messageLoss = Boolean.valueOf(properties.getProperty("messageLoss"));
        sequenceLoss = Boolean.valueOf(properties.getProperty("sequenceLoss"));
        sequenceLimit = Integer.parseInt(properties.getProperty("sequenceLimit"));
        String[] range = properties.getProperty("sequenceRange").split("-");
        if(range.length == 2) {
            sequenceRange = new int[]{Integer.parseInt(range[0]), Integer.parseInt(range[1])};
        }
    }
}
