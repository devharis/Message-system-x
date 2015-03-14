package client;

import interfaces.MessageReceiver;
import service.provider.activemq.ActiveMQProvider;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Enumeration;
import java.util.ResourceBundle;

/**
 * Created by devHaris on 2015-03-13.
 */

public class Messenger extends JFrame implements ActionListener, KeyListener {

    // variables
    public boolean connected;

    private ActiveMQProvider activeMQProvider;
    private JButton connectBtn;
    private JButton disconnectBtn;
    private JTextField nameField;
    private JLabel logoLabel;
    private JLabel portLabel;
    private JLabel ipLabel;
    private JTextField portField;
    private JTextField messageField;
    private JTextField ipField;
    private JTextArea chatArea;
    private JScrollPane chatScroll;
    private JTextArea userArea;
    private JScrollPane userScroll;

    // constants
    public static int PORT = 12345;

    private final static String MESSAGE_X = "Message system X";
    private final static String CONNECT_BTN = "Connect";
    private final static String DISCONNECT_BTN = "Disconnect";
    private final static String PORT_TEXT = "Port:";
    private final static String PORT_FIELD = "56789";
    private final static String IP_TEXT = "IP:";
    private final static String IP_FIELD = "127.0.0.1";

    private final static String NAME_TEXT = "Unikum";

    public final static String RESOURCE_FOLDER = "resources/";
    private final static String LOGO = "logo.png";

    // Constructor
    public Messenger(){
        super(MESSAGE_X);
        setSize(430, 540);
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

        // Set focus, listener & action
        connectBtn.requestFocus();
        connectBtn.addActionListener(this);
        disconnectBtn.addActionListener(this);
        nameField.addKeyListener(this);
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

        userArea = new JTextArea();
        userArea.setLineWrap(true);
        userArea.setEditable(false);
        userScroll = new JScrollPane(userArea);
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
        // TODO: Init a MQProvider and register service
        activeMQProvider = new ActiveMQProvider();

        //If connected
        nameField.setEditable(false);
        activeMQProvider.startListening("127.0.0.1", new MessageReceiver() {
            @Override
            public void onMessage(String message) {
                // GL HF
                System.out.println(message);
            }
        });
    }

    public void onDisconnect(){
        // TODO: Destroy a MQProvider and de-register service
        System.out.println("onDisconnect");
        activeMQProvider.stopListening();
        activeMQProvider = null;
    }

    public void bindKeyEvent(){
        messageField.addKeyListener
        (
                new KeyListener()
                {
                    public void keyPressed(KeyEvent e)
                    {
                        int tipka = e.getKeyCode();
                        if(tipka == KeyEvent.VK_ENTER)
                        {
                            String poruka2 = messageField.getText(); //salji poruku

                            chatArea.append(nameField.getText()+ ">" + poruka2 + "\n");

                            chatArea.setCaretPosition(chatArea.getText().length());

                            onSendMessage("localhost", nameField.getText() + ">" + poruka2);

                            messageField.setText("");
                        }
                    }
                    public void keyReleased(KeyEvent e) { }
                    public void keyTyped(KeyEvent e) { }
                }
        );
    }

    public void onSendMessage(String endPoint, String message){
        // TODO: User sends a message, pass it to service
        activeMQProvider.sendMessage(message, endPoint);
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
        if(nameField.getText().isEmpty())
            connectBtn.setEnabled(false);
        else
            connectBtn.setEnabled(true);
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
