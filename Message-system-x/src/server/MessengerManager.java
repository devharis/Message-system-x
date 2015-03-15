package server;

import interfaces.IMessageReceiver;
import interfaces.IServiceProvider;
import models.Client;
import service.provider.ActiveMQProvider;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

/**
 * Created by devHaris on 2015-03-14.
 */
public class MessengerManager extends JFrame implements ActionListener {

    // variables
    private ArrayList<Client> clientList;
    private IServiceProvider _serviceProvider;

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
    private final static String MESSAGE_X = "Modern Message Server";
    private final static String CONNECT_BTN = "Start server";
    private final static String DISCONNECT_BTN = "Kill server";
    private final static String PORT_TEXT = "Port:";
    private final static String PORT_FIELD = "56789";
    private final static String IP_TEXT = "IP:";
    private final static String IP_FIELD = "";

    // Constructor
    public MessengerManager(){
        this(new ActiveMQProvider());

        setTitle(MESSAGE_X);
        setSize(430, 540);
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

    private void startListening(){
        _serviceProvider.startListening(portField.getText(), new IMessageReceiver() {
            @Override
            public void onMessage(String message) {
                chatArea.append(message);
            }
        });
    }

    private void stopListening(){
        _serviceProvider.stopListening();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        String actionCommand = e.getActionCommand();

        if(actionCommand.equals(CONNECT_BTN)){
            System.out.println("Connected");
            connectBtn.setEnabled(false);
            disconnectBtn.setEnabled(true);
            connectBtn.setActionCommand(DISCONNECT_BTN);
            startListening();
        }
        else if(actionCommand.equals(DISCONNECT_BTN)){
            System.out.println("Disconnected");
            connectBtn.setEnabled(true);
            disconnectBtn.setEnabled(false);
            connectBtn.setActionCommand(CONNECT_BTN);
            stopListening();
        }
    }
}
