package models;

import models.base.AbstractMessage;

/**
 * Created by devHaris on 2015-03-14.
 */
public class Request extends AbstractMessage {

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
