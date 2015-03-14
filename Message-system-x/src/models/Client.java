package models;

import java.sql.Timestamp;
import java.util.Calendar;

/**
 * Created by devHaris on 2015-03-14.
 */
public class Client {

    private String _name;
    private String _ip;

    public Client(String name, String ip){
        _name = name;
        _ip = ip;
    }

    public String get_name() {
        return _name;
    }

    public String get_ip() {
        return _ip;
    }
}
