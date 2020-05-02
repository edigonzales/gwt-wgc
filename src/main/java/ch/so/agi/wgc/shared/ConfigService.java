package ch.so.agi.wgc.shared;

import java.io.IOException;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

@RemoteServiceRelativePath("config")
public interface ConfigService extends RemoteService {
    ConfigResponse configServer() throws IllegalArgumentException, IOException;
}
