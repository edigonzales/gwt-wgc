package ch.so.agi.wgc.client.ui;

import static org.jboss.elemento.Elements.a;
import static org.jboss.elemento.Elements.body;
import static org.jboss.elemento.Elements.div;
import static org.jboss.elemento.Elements.img;
import static org.jboss.elemento.Elements.span;
import static org.jboss.elemento.EventType.bind;
import static org.jboss.elemento.EventType.click;

import java.util.List;

import org.gwtproject.event.shared.HandlerRegistration;
import org.jboss.elemento.Attachable;
import org.jboss.elemento.IsElement;

import static elemental2.dom.DomGlobal.console;
import static elemental2.dom.DomGlobal.alert;

import ch.so.agi.wgc.client.WgcMap;
import ch.so.agi.wgc.shared.BackgroundMapConfig;
import elemental2.dom.DomGlobal;
import elemental2.dom.Event;
import elemental2.dom.HTMLElement;
import elemental2.dom.HTMLImageElement;
import elemental2.dom.Location;
import elemental2.dom.MutationRecord;
import ol.layer.Tile;

public class BackgroundSwitcher implements IsElement<HTMLElement>, Attachable {

    private final HTMLElement root;
    private HandlerRegistration handlerRegistration;
    private WgcMap map;
    
    public BackgroundSwitcher(WgcMap map, List<BackgroundMapConfig> backgroundMapsConfig) {
        this.map = map;
        
        root = div().id("BackgroundSwitcher").element();
        root.appendChild(div().style("padding-bottom: 5px;").textContent("Hintergrundkarten").element());
        
        Location location = DomGlobal.window.location;
        for (int i = 0; i < backgroundMapsConfig.size(); i++) {
            String imageUrl = location.getProtocol() + "//" + location.getHost() + location.getPathname() + backgroundMapsConfig.get(i).getThumbnail();
            if (i != 0) {
                root.appendChild(span().style("padding-left: 10px;").element());
            }
            HTMLImageElement image = img().on(click, event -> click(event)).id(backgroundMapsConfig.get(i).getId()).attr("src", imageUrl)
                    .attr("width", "60").css("background-layer-item").element();
            root.appendChild(image);
        }
        Attachable.register(this, this);
    }   

    @Override
    public void attach(MutationRecord mutationRecord) {}

    @Override
    public HTMLElement element() {
        return root;
    }
    
    @Override
    public void detach(MutationRecord mutationRecord) {
        if (handlerRegistration != null) {
            handlerRegistration.removeHandler();
        }
    }
    
    private void click(Event ev) {
        HTMLElement el = (HTMLElement) ev.target;
        for (String layer : map.getBackgroundLayers()) {
            if (layer.equalsIgnoreCase(el.id)) {
                Tile tile = (Tile) map.getMapLayerById(el.id);
                tile.setVisible(true);
            } else {
                Tile tile = (Tile) map.getMapLayerById(layer);
                tile.setVisible(false);
            }    
        }
    }
}
