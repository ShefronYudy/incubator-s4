package org.apache.s4.core;

import org.apache.s4.base.Event;

public interface RemoteSenders {

    public abstract void send(String hashKey, Event event);

}
