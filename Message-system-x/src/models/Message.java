package models;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.Calendar;

/**
 * Model used for sending messages between endpoints.
 *
 * @author Created by Haris Kljajic & Oskar Karlsson on 2015-03-13.
 * Linneaus University - [2DV104] Software Architecture
 */
public class Message implements Serializable {

    // variables
    private String _name;
    private String _message;
    private String _endPoint;
    private MessageType _messageType;
    private Timestamp _time;

    // constants
    private static final long serialVersionUID = -4507489610617393544L;

    /**
     * Gets a name.
     * @return _name
     */
    public String getName() {
        return _name;
    }

    /**
     * Gets a message.
     * @return _message
     */
    public String getMessage() {
        return _message;
    }

    /**
     * Gets a time.
     * @return _time
     */
    public Timestamp getTime() {
        return _time;
    }

    /**
     * Gets a message type.
     * @return _messageType
     */
    public MessageType getMessageType() {
        return _messageType;
    }

    /**
     * Gets a endpoint.
     * @return _endPoint
     */
    public String getEndPoint() {
        return _endPoint;
    }

    /**
     * Constructor taking params to make instantiation of variables.
     * @param name Name of user
     * @param message Message content
     * @param endPoint End point of package
     * @param messageType Type of message
     */
    public Message(String name, String message, String endPoint, MessageType messageType){
        Calendar calendar = Calendar.getInstance();
        java.util.Date now = calendar.getTime();

        _name = name;
        _message = message;
        _time = new java.sql.Timestamp(now.getTime());
        _endPoint = endPoint;
        _messageType = messageType;
    }
}
