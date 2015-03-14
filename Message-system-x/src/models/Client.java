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

    public String getName() {
        return _name;
    }

    public String getIp() {
        return _ip;
    }
}
