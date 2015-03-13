package client;

import service.provider.activemq.ActiveMQProvider;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Created by devHaris on 2015-03-13.
 */

public class Messenger extends JFrame implements ActionListener {

    // variables
    private ActiveMQProvider activeMQProvider;
    JPanel pane;
    JButton connectBtn;

    // constants
    private final static String MESSAGE_X = "Message system X";
    private final static String CONNECT_BTN = "Connect";
    private final static String DISCONNECT_BTN = "Disconnect";

    public Messenger(){
        super(MESSAGE_X);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        connectBtn = new JButton(CONNECT_BTN);
        Container container = this.getContentPane(); // inherit main frame
        pane = new JPanel();
        container.add(pane);    // JPanel containers default to FlowLayout

        pane.add(new JLabel(new ImageIcon("resources/test.png")));

        connectBtn.setMnemonic('C'); // associate hotkey to button
        pane.add(connectBtn);
        connectBtn.setName(CONNECT_BTN);

        connectBtn.requestFocus();
        connectBtn.addActionListener(this);
        connectBtn.setActionCommand(CONNECT_BTN);

        // make frame visible
        setVisible(true);
        // Sets position and size for window
        setBounds(100,100,350,500);
        // Turn off resizing
        setResizable(false);
    }

    public static void main(String[] args) {
        // Init app
        Messenger messenger = new Messenger();
        messenger.Initialize();
    }

    public void Initialize(){
        // TODO: Create the UI or console.

        // TODO: Init a loop which awaits actions
    }

    public void onConnect(){
        // TODO: Init a MQProvider and register service
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
            connectBtn.setText(DISCONNECT_BTN);
            connectBtn.setActionCommand(DISCONNECT_BTN);
            onConnect();
        }
        else if(actionCommand.equals(DISCONNECT_BTN)){
            System.out.println("Disconnected");
            connectBtn.setText(CONNECT_BTN);
            connectBtn.setActionCommand(CONNECT_BTN);
            onDisconnect();
        }
    }
}
