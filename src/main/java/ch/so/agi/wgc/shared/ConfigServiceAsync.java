package ch.so.agi.wgc.shared;

import com.google.gwt.user.client.rpc.AsyncCallback;

public interface ConfigServiceAsync {
    void configServer(AsyncCallback<ConfigResponse> callback)
            throws IllegalArgumentException;
}
