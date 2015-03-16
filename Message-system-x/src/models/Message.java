package models;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.Calendar;

/**
 * Created by devHaris on 2015-03-14.
 */
public class Message implements Serializable {

    private String _name;
    private String _message;
    private String _endPoint;
    private MessageType _messageType;
    private Timestamp _time;

    private static final long serialVersionUID = -4507489610617393544L;

    public Message(String name, String message, String endPoint, MessageType messageType){
        Calendar calendar = Calendar.getInstance();
        java.util.Date now = calendar.getTime();

        _name = name;
        _message = message;
        _time = new java.sql.Timestamp(now.getTime());
        _endPoint = endPoint;
        _messageType = messageType;
    }

    public String getName() {
        return _name;
    }

    public String getMessage() {
        return _message;
    }

    public Timestamp getTime() {
        return _time;
    }

    public MessageType getMessageType() {
        return _messageType;
    }

    public String getEndPoint() {
        return _endPoint;
    }
}
