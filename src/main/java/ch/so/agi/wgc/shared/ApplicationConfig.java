package ch.so.agi.wgc.shared;

import java.util.ArrayList;
import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;

import com.google.gwt.user.client.rpc.IsSerializable;

@Component
@EnableConfigurationProperties
@ConfigurationProperties(prefix = "app")
public class ApplicationConfig implements IsSerializable {
    private List<BackgroundMapConfig> backgroundMaps = new ArrayList<BackgroundMapConfig>();
    
    public ApplicationConfig() {}
    
    public List<BackgroundMapConfig> getBackgroundMaps() {
        return backgroundMaps;
    }

    public void setBackgroundMaps(List<BackgroundMapConfig> backgroundMaps) {
        this.backgroundMaps = backgroundMaps;
    }
}
