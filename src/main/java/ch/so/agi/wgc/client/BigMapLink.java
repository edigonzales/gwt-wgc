package ch.so.agi.wgc.client;

import org.jboss.elemento.Attachable;
import org.jboss.elemento.IsElement;

import elemental2.dom.DomGlobal;
import elemental2.dom.HTMLElement;
import elemental2.dom.Location;
import elemental2.dom.MutationRecord;

import static org.jboss.elemento.Elements.*;
import static elemental2.dom.DomGlobal.console;


public class BigMapLink implements IsElement<HTMLElement>, Attachable {
    
    private final HTMLElement root;
    
    public BigMapLink(WgcMap map) {
        Location location = DomGlobal.window.location;
        String imageUrl = location.getProtocol() + "//" + location.getHost() + location.getPathname() + "favicon-16x16.png";
        root = a().id("bigMapLink").css("bigMapLink").attr("href", map.createBigMapUrl()).attr("target", "_blank").add(div().add(img().attr("src", imageUrl)).add(span().style("margin-left: 5px;")).add(span().textContent("View on geo.so.ch/map"))).element();
    }
    
    @Override
    public void attach(MutationRecord mutationRecord) {
        
    }

    @Override
    public HTMLElement element() {
        return root;
    }
    

}
