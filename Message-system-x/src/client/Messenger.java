package client;

import factories.ServiceProviderFactory;
import interfaces.IMessageReceiver;
import interfaces.IServiceProvider;
import models.Message;
import models.MessageType;
import models.ProviderType;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.IOException;
import java.io.InputStream;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Properties;

/**
 * Created by devHaris on 2015-03-13.
 */

public class Messenger extends JFrame implements ActionListener, KeyListener {

    // variables
    public boolean connected;
    private IServiceProvider _serviceProvider;
    private String endPoint;
    private String _connectionString;

    // UI variables
    private JButton connectBtn;
    private JButton disconnectBtn;
    public static JTextField nameField;
    private JLabel logoLabel;
    private JLabel incPortLabel;
    private JLabel outPortLabel;
    private JLabel ipLabel;
    private JTextField incPortField;
    private JTextField outPortField;
    private JTextField messageField;
    private JTextField ipField;
    private JTextArea chatArea;
    private JScrollPane chatScroll;
    private JPanel userPane;
    private JScrollPane userScroll;

    // constants
    public final static String RESOURCE_FOLDER = "resources/";

    private final static String MESSAGE_X = "Modern Message Client";
    private final static String CONNECT_BTN = "Connect";
    private final static String DISCONNECT_BTN = "Disconnect";
    private final static String INC_PORT_TEXT = "Inc Port:";
    private final static String INC_PORT_FIELD = "9090";
    private final static String OUT_PORT_TEXT = "Out Port:";
    private final static String OUT_PORT_FIELD = "9091";
    private final static String IP_TEXT = "IP:";
    private final static String IP_FIELD = "127.0.0.1";
    private final static String NAME_TEXT = "Unikum";
    private final static String LOGO = "logo.png";
    private final static String ICON = "icon.png";

    private ProviderType providerType;
    public static int messageDelay;
    public static boolean messageLoss;
    public static boolean sequenceLoss;
    public static int sequenceLimit;
    public static int[] sequenceRange;

    // Constructor
    public Messenger() {

        Configuration config = new Configuration();

        providerType = config.getProviderType();
        messageDelay = config.getMessageDelay();
        messageLoss = config.isMessageLoss();
        sequenceLoss = config.isSequenceLoss();
        sequenceLimit = config.getSequenceLimit();
        sequenceRange = config.getSequenceRange();

        _serviceProvider = ServiceProviderFactory.createServiceProvider(providerType);

        setTitle(MESSAGE_X);
        setSize(430, 540);
        setIconImage(new ImageIcon(String.format("%s%s", RESOURCE_FOLDER, ICON)).getImage());
        setLocationRelativeTo(null);
        setResizable(false);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    public static void main(String[] args) {
        // Init app
        Messenger messenger = new Messenger();
        messenger.Initialize();
    }

    public void Initialize(){
        // Creating window, container and a pane
        Container container = this.getContentPane(); // inherit main frame
        container.removeAll();

        // Add to UI
        initUIComponents();
        initUIScrollBar();
        initUIPositions();
        addUIComponents();

        // Set listener & action
        connectBtn.addActionListener(this);
        disconnectBtn.addActionListener(this);
        nameField.addKeyListener(this);
        messageField.addKeyListener(this);
        connectBtn.setActionCommand(CONNECT_BTN);
        disconnectBtn.setActionCommand(DISCONNECT_BTN);

        setVisible(true);
    }

    void initUIComponents(){
        logoLabel = new JLabel(new ImageIcon(String.format("%s%s", RESOURCE_FOLDER, LOGO)));
        incPortLabel = new JLabel(INC_PORT_TEXT);
        incPortField = new JTextField(INC_PORT_FIELD);
        outPortLabel = new JLabel(OUT_PORT_TEXT);
        outPortField = new JTextField(OUT_PORT_FIELD);
        ipLabel = new JLabel(IP_TEXT);
        ipField = new JTextField(IP_FIELD);
        connectBtn = new JButton(CONNECT_BTN);
        connectBtn.setBackground(new Color(50, 205, 50));
        disconnectBtn = new JButton(DISCONNECT_BTN);
        disconnectBtn.setBackground(new Color(255, 69, 0));
        nameField = new JTextField(NAME_TEXT);
        messageField = new JTextField();

        if(providerType != ProviderType.UdpP2P)
            outPortField.setEditable(false);
    }

    void initUIScrollBar(){
        chatArea = new JTextArea();
        chatArea.setLineWrap(true);
        chatArea.setEditable(false);
        chatScroll = new JScrollPane(chatArea);
        chatScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

        userPane = new JPanel();
        userPane.setLayout(new GridLayout(50, 1));
        userScroll = new JScrollPane(userPane);
        userScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
    }

    void initUIPositions(){
        logoLabel.setBounds(5, 5, 400, 105);
        incPortLabel.setBounds(10, 120, 50, 20);
        incPortField.setBounds(60, 120, 60, 20);
        outPortLabel.setBounds(129, 120, 50, 20);
        outPortField.setBounds(180, 120, 60, 20);
        ipLabel.setBounds(250,120,20,20);
        ipField.setBounds(270, 120, 140, 20);
        connectBtn.setBounds(10,145,110,20);
        disconnectBtn.setBounds(130,145,110,20);
        nameField.setBounds(250,145,160,20);
        chatScroll.setBounds(10,175,300,300);
        userScroll.setBounds(310, 175, 100, 300);
        userPane.setBounds(userScroll.getX(), userScroll.getY(), 90, 290);
        messageField.setBounds(10, 480, 400, 20);
    }

    void addUIComponents(){
        Container chatContainer = getContentPane();
        chatContainer.setLayout(null);
        chatContainer.add(logoLabel);
        chatContainer.add(incPortLabel);
        chatContainer.add(incPortField);
        chatContainer.add(outPortLabel);
        chatContainer.add(outPortField);
        chatContainer.add(ipLabel);
        chatContainer.add(ipField);
        chatContainer.add(connectBtn);
        chatContainer.add(disconnectBtn);
        disconnectBtn.setEnabled(false);
        chatContainer.add(nameField);
        chatContainer.add(chatScroll);
        chatContainer.add(userScroll);
        chatContainer.add(messageField);
    }

    public void onConnect(){
        // Create a connection string from ip and port
        _connectionString = String.format("%s:%s", ipField.getText(), incPortField.getText());
        try {
            connected = true;
            toggleUI();
            _serviceProvider.startListening(_connectionString, new IMessageReceiver() {
                @Override
                public void onMessage(Message message) {

                    DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");

                    if(message.getMessageType().equals(MessageType.SINGLE)) {
                        endPoint = message.getEndPoint();
                        chatArea.append(String.format("[%s] %s", dateFormat.format(message.getTime()), message.getMessage()));
                    } else {
                        chatArea.append(String.format("[%s] %s: %s\n", dateFormat.format(message.getTime()), message.getName(), message.getMessage()));
                    }
                }
            });
        } catch (Exception e) {
            connected = false;
            toggleUI();
            return;
        }

        // Fake populate userlist
        userPane.setSize(90, userScroll.getHeight());
        Button button = new Button("Unikum");
        button.setSize(90, 10);
        userPane.add(button);
        Button button1 = new Button("Oskar");
        button.setSize(90, 10);
        userPane.add(button1);

        userPane.updateUI();
    }

    private void toggleUI() {
        if(connected){
            nameField.setEditable(false);
            incPortField.setEditable(false);
            ipField.setEditable(false);
            connectBtn.setEnabled(false);
            disconnectBtn.setEnabled(true);
        } else if (!connected){
            nameField.setEditable(true);
            incPortField.setEditable(true);
            ipField.setEditable(true);
            connectBtn.setEnabled(true);
            disconnectBtn.setEnabled(false);
        }
    }

    public void onDisconnect(){
        System.out.println("onDisconnect");
        try {
            _serviceProvider.stopListening();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            connected = false;
            toggleUI();
        }

    }

    public void onSendMessage(Message message, String endPoint) {
        switch (providerType){
            case UdpP2P:
                DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
                chatArea.append(String.format("[%s] %s: %s\n", dateFormat.format(message.getTime()), message.getName(), message.getMessage()));
                _serviceProvider.sendMessage(message, String.format("%s:%s", _connectionString.split(":")[0], outPortField.getText()));
                break;
            case TcpClient:
                _serviceProvider.sendMessage(message, endPoint);
                break;
            default:
                // Error
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        String actionCommand = e.getActionCommand();

        if(actionCommand.equals(CONNECT_BTN)){
            System.out.println("Connected");

            connectBtn.setActionCommand(DISCONNECT_BTN);
            onConnect();
        }
        else if(actionCommand.equals(DISCONNECT_BTN)){
            System.out.println("Disconnected");

            connectBtn.setActionCommand(CONNECT_BTN);
            onDisconnect();
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {
        switch (e.getKeyChar()){
            case KeyEvent.VK_ENTER:
                // Send message
                onSendMessage(new Message(
                        nameField.getText(),
                        messageField.getText(),
                        endPoint,
                        MessageType.BROADCAST), _connectionString);
                // Clear message field
                messageField.setText("");
                break;
            default:
                if(nameField.getText().trim().isEmpty() || connected)
                    connectBtn.setEnabled(false);
                else if(!nameField.getText().trim().isEmpty() || !connected)
                    connectBtn.setEnabled(true);
        }
    }

    @Override
    public void keyPressed(KeyEvent e) {
        // Not used.
    }

    @Override
    public void keyReleased(KeyEvent e) {
        // Not used.
    }
}
