package ch.so.agi.wgc.client;

import com.google.gwt.i18n.client.Messages;

public interface MyMessages extends Messages {
    @DefaultMessage("Concerned Themes")
    String concernedThemes();
    
    @DefaultMessage("Real estate {0} in {1}")
    String resultHeader(String number, String municipality);
}
