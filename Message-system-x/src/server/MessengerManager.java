package server;

import factories.ServiceProviderFactory;
import interfaces.IMessageReceiver;
import interfaces.IServiceProvider;
import models.Client;
import models.Message;
import models.ProviderType;
import sun.audio.AudioPlayer;
import sun.audio.AudioStream;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

/**
 * This class is a GUI which creates the window and interface
 * for the messenger system server.
 *
 * @author Created by Haris Kljajic & Oskar Karlsson on 2015-03-13.
 * Linneaus University - [2DV104] Software Architecture
 */
public class MessengerManager extends JFrame implements ActionListener {

    // variables
    public boolean running = false;
    private ArrayList<Client> _clientList;
    private IServiceProvider _serviceProvider;

    // UI variables
    private JButton _connectBtn;
    private JButton _disconnectBtn;
    private JLabel _portLabel;
    private JLabel _ipLabel;
    private JTextField _portField;
    private JTextField _ipField;
    private JTextArea _chatArea;
    private JScrollPane _chatScroll;

    // constants
    public final static String RESOURCE_FOLDER = "resources/";
    private final static String MESSAGE_X = "Modern Message Server";
    private final static String CONNECT_BTN = "Start server";
    private final static String DISCONNECT_BTN = "Kill server";
    private final static String PORT_TEXT = "Port:";
    private final static String PORT_FIELD = "9090";
    private final static String IP_TEXT = "IP:";
    private final static String IP_FIELD = "127.0.0.1";
    private final static String ICON = "server-icon.png";
    private final static String SOUND = "sound.wav";

    /**
     * Default Constructor
     */
    public MessengerManager() throws IOException {
        this(ServiceProviderFactory.createServiceProvider(ProviderType.TcpServer));
        playAudio();
        setTitle(MESSAGE_X);
        setSize(430, 540);
        setIconImage(new ImageIcon(String.format("%s%s", RESOURCE_FOLDER, ICON)).getImage());
        setLocationRelativeTo(null);
        setResizable(false);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
    }

    /**
     * Dependency Injection Constructor
     * @param serviceProvider A type of service provider
     */
    public MessengerManager(IServiceProvider serviceProvider){
        _serviceProvider = serviceProvider;
    }

    /**
     * Program entry point.
     * @param args
     */
    public static void main(String[] args) throws IOException {
        // Init app
        MessengerManager messengerManager = new MessengerManager();
        messengerManager.Initialize();
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

        // Listener & action
        _connectBtn.addActionListener(this);
        _disconnectBtn.addActionListener(this);
        _connectBtn.setActionCommand(CONNECT_BTN);
        _disconnectBtn.setActionCommand(DISCONNECT_BTN);

        setVisible(true);
    }

    /**
     * Setup the UI components.
     */
    void initUIComponents(){
        _portLabel = new JLabel(PORT_TEXT);
        _portField = new JTextField(PORT_FIELD);
        _ipLabel = new JLabel(IP_TEXT);
        _ipField = new JTextField(IP_FIELD);
        _ipField.setEditable(false);
        _connectBtn = new JButton(CONNECT_BTN);
        _connectBtn.setBackground(new Color(50, 205, 50));
        _disconnectBtn = new JButton(DISCONNECT_BTN);
        _disconnectBtn.setBackground(new Color(255, 69, 0));
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
    }

    /**
     * Setup UI components positions in window.
     */
    void initUIPositions(){
        _portLabel.setBounds(10,10,50,20);
        _portField.setBounds(40,10,100,20);
        _ipLabel.setBounds(150, 10, 20, 20);
        _ipField.setBounds(170,10,240,20);
        _connectBtn.setBounds(10, 40, 110, 20);
        _disconnectBtn.setBounds(130,40,110,20);
        _chatScroll.setBounds(10, 70, 400, 430);
    }

    /**
     * Add UI components to the previously created window.
     */
    void addUIComponents(){
        Container chatContainer = getContentPane();
        chatContainer.setLayout(null);
        chatContainer.add(_portLabel);
        chatContainer.add(_portField);
        chatContainer.add(_ipLabel);
        chatContainer.add(_ipField);
        chatContainer.add(_connectBtn);
        chatContainer.add(_disconnectBtn);
        _disconnectBtn.setEnabled(false);
        chatContainer.add(_chatScroll);
    }

    /**
    * Starts to listen to incoming messages.
     */
    private void startListening() {
        try {
            _serviceProvider.startListening(_portField.getText(), new IMessageReceiver() {
                @Override
                public void onMessage(Message message) {

                    DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");

                    _chatArea.append(String.format("[%s][%s][%s] %s: %s \n",
                            message.getEndPoint(),
                            message.getMessageType().toString(),
                            dateFormat.format(message.getTime()),
                            message.getName(),
                            message.getMessage()));
                }
            });
            running = true;
        } catch (Exception e) {
            running = false;
            e.printStackTrace();
        }
    }

    /**
     * Stops listening to incoming messages.
     */
    private void stopListening(){

        running = false;
        try {
            _serviceProvider.stopListening();
            toggleUI();

        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Method helping to toggle UI components properties
     * depending on if connected or not.
     */
    private void toggleUI() {
        if(running) {
            _portField.setEditable(false);
            _ipField.setEditable(false);
            _connectBtn.setEnabled(false);
            _disconnectBtn.setEnabled(true);

        } else if (!running){
            _portField.setEditable(true);
            _ipField.setEditable(false);
            _connectBtn.setEnabled(true);
            _disconnectBtn.setEnabled(false);
        }
    }

    /**
     * Method helping to start audio.
     */
    private void playAudio() throws IOException {
        InputStream in = new FileInputStream(String.format("%s%s", RESOURCE_FOLDER, SOUND));
        AudioStream audioStream = new AudioStream(in);
        AudioPlayer.player.start(audioStream);
    }

    /**
     * Implemented to catch ActionListener event which
     * is set on buttons in the UI window.
     * @param e Event
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        String actionCommand = e.getActionCommand();

        if(actionCommand.equals(CONNECT_BTN)){
            _connectBtn.setActionCommand(DISCONNECT_BTN);
            startListening();
            toggleUI();
        }
        else if(actionCommand.equals(DISCONNECT_BTN)){
            _connectBtn.setActionCommand(CONNECT_BTN);
            stopListening();
            toggleUI();
        }
    }


}
