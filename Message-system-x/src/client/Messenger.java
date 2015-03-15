package client;

import interfaces.IMessageReceiver;
import interfaces.IServiceProvider;
import service.provider.ActiveMQProvider;
import service.provider.ClientProvider;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

/**
 * Created by devHaris on 2015-03-13.
 */

public class Messenger extends JFrame implements ActionListener, KeyListener {

    // variables
    public boolean connected;
    private IServiceProvider _serviceProvider;

    // UI variables
    private JButton connectBtn;
    private JButton disconnectBtn;
    public static JTextField nameField;
    private JLabel logoLabel;
    private JLabel portLabel;
    private JLabel ipLabel;
    private JTextField portField;
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
    private final static String PORT_TEXT = "Port:";
    private final static String PORT_FIELD = "9090";
    private final static String IP_TEXT = "IP:";
    private final static String IP_FIELD = "127.0.0.1";
    private final static String NAME_TEXT = "Unikum";
    private final static String LOGO = "logo.png";
    private final static String ICON = "icon.png";

    // Constructor
    public Messenger(){
        this(new ClientProvider());

        setTitle(MESSAGE_X);
        setSize(430, 540);
        setIconImage(new ImageIcon(String.format("%s%s", RESOURCE_FOLDER, ICON)).getImage());
        setLocationRelativeTo(null);
        setResizable(false);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    public Messenger(IServiceProvider serviceProvider){
        _serviceProvider = serviceProvider;
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

        // Set focus, listener & action
        connectBtn.requestFocus();
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
        portLabel = new JLabel(PORT_TEXT);
        portField = new JTextField(PORT_FIELD);
        ipLabel = new JLabel(IP_TEXT);
        ipField = new JTextField(IP_FIELD);
        connectBtn = new JButton(CONNECT_BTN);
        connectBtn.setBackground(new Color(50, 205, 50));
        disconnectBtn = new JButton(DISCONNECT_BTN);
        disconnectBtn.setBackground(new Color(255, 69, 0));
        nameField = new JTextField(NAME_TEXT);
        messageField = new JTextField();
    }

    void initUIScrollBar(){
        chatArea = new JTextArea();
        chatArea.setLineWrap(true);
        chatArea.setEditable(false);
        chatScroll = new JScrollPane(chatArea);
        chatScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

        userPane = new JPanel();
        userScroll = new JScrollPane(userPane);
        userScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
    }

    void initUIPositions(){
        logoLabel.setBounds(5, 5, 400, 105);
        portLabel.setBounds(10,120,50,20);
        portField.setBounds(40,120,100,20);
        ipLabel.setBounds(150,120,20,20);
        ipField.setBounds(170,120,240,20);
        connectBtn.setBounds(10,145,110,20);
        disconnectBtn.setBounds(130,145,110,20);
        nameField.setBounds(250,145,160,20);
        chatScroll.setBounds(10,175,300,300);
        userScroll.setBounds(310,175,100,300);
        userPane.setBounds(userScroll.getX(), userScroll.getY(), 90, 290);
        messageField.setBounds(10,480,400,20);
    }

    void addUIComponents(){
        Container chatContainer = getContentPane();
        chatContainer.setLayout(null);
        chatContainer.add(logoLabel);
        chatContainer.add(portLabel);
        chatContainer.add(portField);
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

        String connectionString = String.format("%s:%s", ipField.getText(), portField.getText());

        _serviceProvider.startListening(connectionString, new IMessageReceiver() {
            @Override
            public void onMessage(String message) {
                chatArea.append(message);
                connected = true;
            }
        });

        //If connected
        nameField.setEditable(false);
        // Fake populate userlist
        userPane.add(new Button("Unikum"));
        userPane.add(new Button("Oskar"));
        userPane.add(new Button("Haris"));
        userPane.updateUI();
    }

    public void onDisconnect(){
        System.out.println("onDisconnect");
        _serviceProvider.stopListening();
        connected = false;
    }

    public void onSendMessage(String endPoint, String message){
        _serviceProvider.sendMessage(message, endPoint);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        String actionCommand = e.getActionCommand();

        if(actionCommand.equals(CONNECT_BTN)){
            System.out.println("Connected");

            connected = true;
            connectBtn.setEnabled(false);
            disconnectBtn.setEnabled(true);
            connectBtn.setActionCommand(DISCONNECT_BTN);
            onConnect();
        }
        else if(actionCommand.equals(DISCONNECT_BTN)){
            System.out.println("Disconnected");

            connected = false;
            connectBtn.setEnabled(true);
            disconnectBtn.setEnabled(false);
            nameField.setEditable(true);
            connectBtn.setActionCommand(CONNECT_BTN);
            onDisconnect();
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {
        if(nameField.getText().trim().isEmpty() || connected)
            connectBtn.setEnabled(false);
        else if(!nameField.getText().isEmpty())
            connectBtn.setEnabled(true);

        if(e.getKeyChar() == KeyEvent.VK_ENTER && connected){
            // Send message
            onSendMessage(null, messageField.getText());
            // Clear message field
            messageField.setText("");
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
