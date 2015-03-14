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
    private JPanel connectPane;
    private JPanel mainPane;
    private JPanel userListPane;
    private JList messageList;
    private JButton connectBtn;
    private JTextField nameField;
    private JLabel nameLabel;
    private Configuration config;

    // constants
    private final static String MESSAGE_X = "Message system X";
    private final static String CONNECT_BTN = "Connect";
    private final static String DISCONNECT_BTN = "Disconnect";
    private final static String NAME_LABEL = "Enter your name: ";
    private final static String WELCOME_LABEL = "Welcome to Modern Message! Connect and start chat!";

    public final static String RESOURCE_FOLDER = "resources/";
    private final static String LOGO = "logo.png";

    private final static String CONFIG_NETWORK_DELAY = "networkDelay";

    // Vi behöver en port som allt ska använda, sätter 12345 så länge.
    public final static int PORT = 12345;

    // Constructor
    public Messenger(){
        super(MESSAGE_X);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    public static void main(String[] args) {
        // Init app
        Messenger messenger = new Messenger();
        messenger.Initialize();
    }

    public void Initialize(){
        // TODO: Create the UI or console.

        // Load configuration, loads from file see class
        //config = new Configuration();
        Border border;

        // Creating window, container and a pane
        Container container = this.getContentPane(); // inherit main frame
        container.removeAll();

        connectPane = new JPanel();
        container.add(connectPane);    // JPanel containers default to FlowLayout

        // Init widgets
        JLabel welcomeLabel = new JLabel();
        connectBtn = new JButton(CONNECT_BTN);
        connectBtn.setEnabled(false);
        nameLabel = new JLabel(NAME_LABEL);
        nameField = new JTextField(10);

        connectPane.add(new JLabel(new ImageIcon(String.format("%s%s", RESOURCE_FOLDER, LOGO))));
        connectBtn.setName(CONNECT_BTN);
        connectBtn.setPreferredSize(new Dimension(210, 20));

        welcomeLabel.setText(WELCOME_LABEL);
        welcomeLabel.setPreferredSize(new Dimension(310, 60));
        welcomeLabel.setSize(100, 300);

        // Add to UI
        connectPane.add(welcomeLabel);
        connectPane.add(nameLabel);
        connectPane.add(nameField);
        connectPane.add(connectBtn);

        // Set focus, listener & action
        connectBtn.requestFocus();
        connectBtn.addActionListener(this);
        nameField.addKeyListener(this);
        connectBtn.setActionCommand(CONNECT_BTN);

        // make frame visible
        setVisible(true);
        // Sets position and size for window
        setBounds(100,100,350,500);
        // Turn off resizing
        setResizable(false);
    }

    public void MainPane() {
        Container container = this.getContentPane(); // inherit main frame
        container.removeAll();

        mainPane = new JPanel();
        mainPane.setSize(container.getSize());
        container.add(mainPane);

        JPanel messagesPanel = new JPanel();
        messagesPanel.setAutoscrolls(true);
        messagesPanel.setSize(messagesPanel.getWidth() - 20, container.getHeight());
        messagesPanel.setPreferredSize(new Dimension(container.getWidth() - 20, container.getHeight() - 100));

        JTextArea  messageList  = new JTextArea();
        messageList.setText("Noob: asdasdasdasdasdasdasdasdasdasdasdassadasdasdasdasdasdasdasdasdasdasdasdasdasdasdasdas");
        messageList.setLineWrap(true);

        connectBtn.setPreferredSize(new Dimension(310, 20));
        mainPane.add(connectBtn);
        messagesPanel.add(messageList);
        mainPane.add(messagesPanel);
    }

    public void onConnect(){
        // TODO: Init a MQProvider and register service
        activeMQProvider = new ActiveMQProvider();

        onSendMessage("127.0.0.1", "This is from client");


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

            connectBtn.setText(DISCONNECT_BTN);
            connectBtn.setActionCommand(DISCONNECT_BTN);

            MainPane();

            onConnect();
        }
        else if(actionCommand.equals(DISCONNECT_BTN)){
            System.out.println("Disconnected");

            connected = false;

            connectBtn.setText(CONNECT_BTN);
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
