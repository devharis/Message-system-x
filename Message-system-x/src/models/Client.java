package models;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * Model used to read from incoming messages and writing to outgoing.
 *
 * @author Created by Haris Kljajic & Oskar Karlsson on 2015-03-13.
 * Linneaus University - [2DV104] Software Architecture
 */
public class Client {

    // variables
    private String _endPoint;
    private String _userName;
    private ObjectInputStream _inputStream;
    private ObjectOutputStream _outputStream;

    /**
     * Gets the endpoint.
     * @return _endPoint
     */
    public String getEndPoint() {
        return _endPoint;
    }

    /**
     * Gets the username.
     * @return _userName
     */
    public String getUserName() {
        return _userName;
    }

    /**
     * Sets the username.
     * @param userName Name of user
     */
    public void setUserName(String userName) {
        this._userName = userName;
    }

    /**
     * Gets the objects input stream.
     * @return _inputStream
     */
    public ObjectInputStream getOIS() {
        return _inputStream;
    }

    /**
     * Gets the objects output stream.
     * @return _outputStream
     */
    public ObjectOutputStream getOOS() {
        return _outputStream;
    }

    /**
     * Constructor taking params to make instantiation of variables.
     * easier and shorter.
     * @param endPoint Endpoint of package
     * @param oos Output stream
     * @param ois Input stream
     */
    public Client(String endPoint, ObjectOutputStream oos, ObjectInputStream ois) {
        _endPoint = endPoint;
        _outputStream = oos;
        _inputStream = ois;
    }
}