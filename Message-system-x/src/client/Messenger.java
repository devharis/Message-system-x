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
import java.text.DateFormat;
import java.text.SimpleDateFormat;

/**
 * Created by Haris Kljajic & Oskar Karlsson on 2015-03-13.
 * Linneaus University - [2DV104] Software Architecture
 */
public class Messenger extends JFrame implements ActionListener, KeyListener {

    // variables
    public boolean connected;
    private IServiceProvider _serviceProvider;
    private String _endPoint;
    private String _connectionString;
    private ProviderType _providerType;

    // statics
    public static int messageDelay;
    public static boolean messageLoss;
    public static boolean sequenceLoss;
    public static int sequenceLimit;
    public static int[] sequenceRange;

    // UI variables
    public static JTextField nameField;
    private JButton _connectBtn;
    private JButton _disconnectBtn;
    private JLabel _logoLabel;
    private JLabel _incPortLabel;
    private JLabel _outPortLabel;
    private JLabel _ipLabel;
    private JTextField _incPortField;
    private JTextField _outPortField;
    private JTextField _messageField;
    private JTextField _ipField;
    private JTextArea _chatArea;
    private JScrollPane _chatScroll;
    private JPanel _userPane;
    private JScrollPane _userScroll;

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

    /**
     * Default Constructor
     */
    public Messenger() {
        Configuration config = new Configuration();

        _providerType = config.getProviderType();
        messageDelay = config.getMessageDelay();
        messageLoss = config.isMessageLoss();
        sequenceLoss = config.isSequenceLoss();
        sequenceLimit = config.getSequenceLimit();
        sequenceRange = config.getSequenceRange();

        _serviceProvider = ServiceProviderFactory.createServiceProvider(_providerType);

        setTitle(MESSAGE_X);
        setSize(430, 540);
        setIconImage(new ImageIcon(String.format("%s%s", RESOURCE_FOLDER, ICON)).getImage());
        setLocationRelativeTo(null);
        setResizable(false);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
    }

    /**
     * Dependency Injection Constructor
     * @param serviceProvider
     */
    public Messenger(IServiceProvider serviceProvider) {
        _serviceProvider = serviceProvider;
    }

    /**
     * Program entry point.
     * @param args
     */
    public static void main(String[] args) {
        // Init app
        Messenger messenger = new Messenger();
        messenger.Initialize();
    }

    /**
     * Initializes a window and setup its UI.
     * Binds UI components to events and shows the window.
     */
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
        _connectBtn.addActionListener(this);
        _disconnectBtn.addActionListener(this);
        nameField.addKeyListener(this);
        _messageField.addKeyListener(this);
        _connectBtn.setActionCommand(CONNECT_BTN);
        _disconnectBtn.setActionCommand(DISCONNECT_BTN);

        setVisible(true);
    }

    /**
     * Setup the UI components.
     */
    void initUIComponents(){
        _logoLabel = new JLabel(new ImageIcon(String.format("%s%s", RESOURCE_FOLDER, LOGO)));
        _incPortLabel = new JLabel(INC_PORT_TEXT);
        _incPortField = new JTextField(INC_PORT_FIELD);
        _outPortLabel = new JLabel(OUT_PORT_TEXT);
        _outPortField = new JTextField(OUT_PORT_FIELD);
        _ipLabel = new JLabel(IP_TEXT);
        _ipField = new JTextField(IP_FIELD);
        _connectBtn = new JButton(CONNECT_BTN);
        _connectBtn.setBackground(new Color(50, 205, 50));
        _disconnectBtn = new JButton(DISCONNECT_BTN);
        _disconnectBtn.setBackground(new Color(255, 69, 0));
        nameField = new JTextField(NAME_TEXT);
        _messageField = new JTextField();

        if(_providerType != ProviderType.UdpP2P)
            _outPortField.setEditable(false);
    }

    /**
     * Setup scroll bar component.
     */
    void initUIScrollBar(){
        _chatArea = new JTextArea();
        _chatArea.setLineWrap(true);
        _chatArea.setEditable(false);
        _chatScroll = new JScrollPane(_chatArea);
        _chatScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

        _userPane = new JPanel();
        _userPane.setLayout(new GridLayout(50, 1));
        _userScroll = new JScrollPane(_userPane);
        _userScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
    }

    /**
     * Setup UI components positions in window.
     */
    void initUIPositions(){
        _logoLabel.setBounds(5, 5, 400, 105);
        _incPortLabel.setBounds(10, 120, 50, 20);
        _incPortField.setBounds(60, 120, 60, 20);
        _outPortLabel.setBounds(129, 120, 50, 20);
        _outPortField.setBounds(180, 120, 60, 20);
        _ipLabel.setBounds(250,120,20,20);
        _ipField.setBounds(270, 120, 140, 20);
        _connectBtn.setBounds(10,145,110,20);
        _disconnectBtn.setBounds(130,145,110,20);
        nameField.setBounds(250,145,160,20);
        _chatScroll.setBounds(10,175,300,300);
        _userScroll.setBounds(310, 175, 100, 300);
        _userPane.setBounds(_userScroll.getX(), _userScroll.getY(), 90, 290);
        _messageField.setBounds(10, 480, 400, 20);
    }

    /**
     * Add UI components to the previously created window.
     */
    void addUIComponents(){
        Container chatContainer = getContentPane();
        chatContainer.setLayout(null);
        chatContainer.add(_logoLabel);
        chatContainer.add(_incPortLabel);
        chatContainer.add(_incPortField);
        chatContainer.add(_outPortLabel);
        chatContainer.add(_outPortField);
        chatContainer.add(_ipLabel);
        chatContainer.add(_ipField);
        chatContainer.add(_connectBtn);
        chatContainer.add(_disconnectBtn);
        _disconnectBtn.setEnabled(false);
        chatContainer.add(nameField);
        chatContainer.add(_chatScroll);
        chatContainer.add(_userScroll);
        chatContainer.add(_messageField);
    }

    /**
     * onConnect is used to establish a connection to a service, e.g server
     * or peer-to-peer endpoint.
     *
     * It uses MessageType to separate either of the two setups.
     */
    public void onConnect(){
        // Create a connection string from ip and port
        _connectionString = String.format("%s:%s", _ipField.getText(), _incPortField.getText());

        try {
            connected = true;
            toggleUI();
            _serviceProvider.startListening(_connectionString, new IMessageReceiver() {
                @Override
                public void onMessage(Message message) {

                    DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");

                    if(message.getMessageType().equals(MessageType.SINGLE)) {
                        _endPoint = message.getEndPoint();
                        _chatArea.append(String.format("[%s] %s", dateFormat.format(message.getTime()), message.getMessage()));
                    } else {
                        _chatArea.append(String.format("[%s] %s: %s\n", dateFormat.format(message.getTime()), message.getName(), message.getMessage()));
                    }
                }
            });
        } catch (Exception e) {
            connected = false;
            toggleUI();
            return;
        }

        // TODO: FIX THIS SHIT BEFORE DONE!!!!
        _userPane.setSize(90, _userScroll.getHeight());
        Button button = new Button("Unikum");
        button.setSize(90, 10);
        _userPane.add(button);
        Button button1 = new Button("Oskar");
        button.setSize(90, 10);
        _userPane.add(button1);
        _userPane.updateUI();
    }

    /**
     * Method helping to toggle UI components properties
     * depending on if connected or not.
     */
    private void toggleUI() {
        if(connected){
            nameField.setEditable(false);
            _incPortField.setEditable(false);
            _ipField.setEditable(false);
            _connectBtn.setEnabled(false);
            _disconnectBtn.setEnabled(true);
        } else if (!connected){
            nameField.setEditable(true);
            _incPortField.setEditable(true);
            _ipField.setEditable(true);
            _connectBtn.setEnabled(true);
            _disconnectBtn.setEnabled(false);
        }
    }

    /**
     * onDisconnect is used to disconnect from endpoint.
     * Simply by stop listening to it.
     */
    public void onDisconnect(){
        try {
            _serviceProvider.stopListening();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            connected = false;
            toggleUI();
        }

    }

    /**
     * This method is simply used to send a message to a specific endpoint, e.g server
     * or peer. Depending on the configuration setup it uses different solutions to send.
     * @param message
     * @param endPoint
     */
    public void onSendMessage(Message message, String endPoint) {
        switch (_providerType){
            case UdpP2P:
                DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
                _chatArea.append(String.format("[%s] %s: %s\n", dateFormat.format(message.getTime()), message.getName(), message.getMessage()));
                _serviceProvider.sendMessage(message, String.format("%s:%s", _connectionString.split(":")[0], _outPortField.getText()));
                break;
            case TcpClient:
                _serviceProvider.sendMessage(message, endPoint);
                break;
            default:
                // Error
        }
    }

    /**
     * Implemented to catch ActionListener event which
     * is set on buttons in the UI window.
     * @param e
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        String actionCommand = e.getActionCommand();

        if(actionCommand.equals(CONNECT_BTN)){
            _connectBtn.setActionCommand(DISCONNECT_BTN);
            onConnect();
        }
        else if(actionCommand.equals(DISCONNECT_BTN)){
            _connectBtn.setActionCommand(CONNECT_BTN);
            onDisconnect();
        }
    }

    /**
     * Implemented to catch KeyListener event which
     * is set on text area chat to listen to Enter.
     * Sending away a message.
     * @param e
     */
    @Override
    public void keyTyped(KeyEvent e) {
        switch (e.getKeyChar()){
            case KeyEvent.VK_ENTER:
                // Send message
                onSendMessage(new Message(
                        nameField.getText(),
                        _messageField.getText(),
                        _endPoint,
                        MessageType.BROADCAST), _connectionString);

                // Clear message field
                _messageField.setText("");
                break;
            default:
                if(nameField.getText().trim().isEmpty() || connected)
                    _connectBtn.setEnabled(false);
                else if(!nameField.getText().trim().isEmpty() || !connected)
                    _connectBtn.setEnabled(true);
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
