package models;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.sql.Timestamp;
import java.util.Calendar;

/**
 * Created by devHaris on 2015-03-14.
 */
public class Client {

    // Variables

    private String endPoint;
    private String userName;
    public boolean active;

    private ObjectInputStream inputStream;
    private ObjectOutputStream outputStream;

    // Properties

    public String getEndPoint() {
        return endPoint;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public ObjectInputStream getOIS() {
        return inputStream;
    }

    public ObjectOutputStream getOOS() {
        return outputStream;
    }

    // Methods and constructors
    public Client(String endPoint, ObjectOutputStream oos, ObjectInputStream ois) {
        this.endPoint = endPoint;
        this.outputStream = oos;
        this.inputStream = ois;
    }
}