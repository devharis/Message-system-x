package server;

import factories.ServiceProviderFactory;
import interfaces.IMessageReceiver;
import interfaces.IServiceProvider;
import models.Client;
import models.Message;
import models.ProviderType;
import service.provider.tcp.ServerProvider;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created by devHaris on 2015-03-14.
 */
public class MessengerManager extends JFrame implements ActionListener {

    // variables
    private ArrayList<Client> clientList;
    private IServiceProvider _serviceProvider;
    public boolean running = false;

    // UI variables
    private JButton connectBtn;
    private JButton disconnectBtn;
    private JLabel portLabel;
    private JLabel ipLabel;
    private JTextField portField;
    private JTextField ipField;
    private JTextArea chatArea;
    private JScrollPane chatScroll;

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

    // Constructor
    public MessengerManager(){
        this(ServiceProviderFactory.createServiceProvider(ProviderType.TcpServer));

        setTitle(MESSAGE_X);
        setSize(430, 540);
        setIconImage(new ImageIcon(String.format("%s%s", RESOURCE_FOLDER, ICON)).getImage());
        setLocationRelativeTo(null);
        setResizable(false);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    public MessengerManager(IServiceProvider serviceProvider){
        _serviceProvider = serviceProvider;
    }

    public static void main(String[] args) {
        // Init app
        MessengerManager messengerManager = new MessengerManager();
        messengerManager.Initialize();
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
        connectBtn.setActionCommand(CONNECT_BTN);
        disconnectBtn.setActionCommand(DISCONNECT_BTN);

        setVisible(true);
    }

    void initUIComponents(){
        portLabel = new JLabel(PORT_TEXT);
        portField = new JTextField(PORT_FIELD);
        ipLabel = new JLabel(IP_TEXT);
        ipField = new JTextField(IP_FIELD);
        ipField.setEditable(false);
        connectBtn = new JButton(CONNECT_BTN);
        connectBtn.setBackground(new Color(50, 205, 50));
        disconnectBtn = new JButton(DISCONNECT_BTN);
        disconnectBtn.setBackground(new Color(255, 69, 0));
    }

    void initUIScrollBar(){
        chatArea = new JTextArea();
        chatArea.setLineWrap(true);
        chatArea.setEditable(false);
        chatScroll = new JScrollPane(chatArea);
        chatScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
    }

    void initUIPositions(){
        portLabel.setBounds(10,10,50,20);
        portField.setBounds(40,10,100,20);
        ipLabel.setBounds(150,10,20,20);
        ipField.setBounds(170,10,240,20);
        connectBtn.setBounds(10,40,110,20);
        disconnectBtn.setBounds(130,40,110,20);
        chatScroll.setBounds(10,70,400,430);
    }

    void addUIComponents(){
        Container chatContainer = getContentPane();
        chatContainer.setLayout(null);
        chatContainer.add(portLabel);
        chatContainer.add(portField);
        chatContainer.add(ipLabel);
        chatContainer.add(ipField);
        chatContainer.add(connectBtn);
        chatContainer.add(disconnectBtn);
        disconnectBtn.setEnabled(false);
        chatContainer.add(chatScroll);
    }

    private void startListening() {
        try {
            _serviceProvider.startListening(portField.getText(), new IMessageReceiver() {
                @Override
                public void onMessage(Message message) {

                    DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");

                    chatArea.append(String.format("[%s][%s][%s] %s: %s \n",
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

    private void stopListening(){

        running = false;

        try {
            _serviceProvider.stopListening();

            // TOGGLE
            toggleUI();

        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        String actionCommand = e.getActionCommand();

        if(actionCommand.equals(CONNECT_BTN)){
            System.out.println("Connected");
            connectBtn.setActionCommand(DISCONNECT_BTN);
            startListening();
            toggleUI();
        }
        else if(actionCommand.equals(DISCONNECT_BTN)){
            System.out.println("Disconnected");
            connectBtn.setActionCommand(CONNECT_BTN);
            stopListening();
            toggleUI();
        }
    }

    private void toggleUI() {

        if(running) {

            portField.setEditable(false);
            ipField.setEditable(false);
            connectBtn.setEnabled(false);
            disconnectBtn.setEnabled(true);

        } else if (!running){

            portField.setEditable(true);
            ipField.setEditable(false);
            connectBtn.setEnabled(true);
            disconnectBtn.setEnabled(false);
        }
    }
}
