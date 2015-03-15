package models;

import models.base.AbstractMessage;

import java.lang.reflect.Parameter;

/**
 * Created by devHaris on 2015-03-14.
 */
public class Request extends AbstractMessage {

    public String serviceType;
    public String opName;
    public Parameter params[];

    public String getServiceType() {
        return serviceType;
    }

    public String getOpName() {
        return opName;
    }

    public Parameter[] getParams() {
        return params;
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
