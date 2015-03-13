package client;

import interfaces.MessageReceiver;
import service.provider.activemq.ActiveMQProvider;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

/**
 * Created by devHaris on 2015-03-13.
 */

public class Messenger extends JFrame implements ActionListener, KeyListener {

    // variables
    public boolean connected;

    private ActiveMQProvider activeMQProvider;
    private JPanel connectPane;
    private JButton connectBtn;
    private JTextField nameField;
    private JLabel nameLabel;

    // constants
    private final static String MESSAGE_X = "Message system X";
    private final static String CONNECT_BTN = "Connect";
    private final static String DISCONNECT_BTN = "Disconnect";
    private final static String NAME_LABEL = "Enter your name: ";
    private final static String WELCOME_LABEL = "Welcome to Modern Message! Connect and start chat!";

    private final static String RESOURCE_FOLDER = "resources/";
    private final static String LOGO = "logo.png";

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

        // Creating window, container and a pane
        Container container = this.getContentPane(); // inherit main frame
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

    public void onConnect(){
        // TODO: Init a MQProvider and register service
        activeMQProvider = new ActiveMQProvider();

        activeMQProvider.startListening("127.0.0.1", new MessageReceiver() {
            @Override
            public void onMessage(String message) {
                // GL HF
            }
        });
    }

    public void onDisconnect(){
        // TODO: Destroy a MQProvider and de-register service
    }

    public void onSendMessage(){
        // TODO: User sends a message, pass it to service
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        String actionCommand = e.getActionCommand();

        if(actionCommand.equals(CONNECT_BTN)){
            System.out.println("Connected");

            connected = true;

            connectBtn.setText(DISCONNECT_BTN);
            connectBtn.setActionCommand(DISCONNECT_BTN);
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
