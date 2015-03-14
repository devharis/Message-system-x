package models;

import java.sql.Timestamp;
import java.util.Calendar;

/**
 * Created by devHaris on 2015-03-14.
 */
public class Message {

    private String _name;
    private String _message;
    private Timestamp _time;

    public Message(String name, String message){
        Calendar calendar = Calendar.getInstance();
        java.util.Date now = calendar.getTime();

        _name = name;
        _message = message;
        _time = new java.sql.Timestamp(now.getTime());
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
}
