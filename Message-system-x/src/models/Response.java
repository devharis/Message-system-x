package models;

import models.base.AbstractMessage;

/**
 * Created by devHaris on 2015-03-14.
 */
public class Response extends AbstractMessage {

    public int requestId(){
        return 0;
    }

    public Class<?> returnType(){
        return null;
    }

    public Object returnValue (){
        return null;
    }

    @Override
    public int id() {
        return 0;
    }

    @Override
    public String endPoint() {
        return null;
    }

    @Override
    public String msgType() {
        return null;
    }
}
