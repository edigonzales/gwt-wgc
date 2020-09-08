package ch.so.agi.wgc.client;

import static elemental2.dom.DomGlobal.console;
import static org.jboss.elemento.Elements.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.dominokit.domino.ui.forms.SuggestBox.DropDownPositionDown;
import org.dominokit.domino.ui.dropdown.DropDownMenu;
import org.dominokit.domino.ui.dropdown.DropDownPosition;
import org.dominokit.domino.ui.forms.SuggestBox;
import org.dominokit.domino.ui.forms.SuggestBoxStore;
import org.dominokit.domino.ui.forms.SuggestItem;
import org.dominokit.domino.ui.icons.Icon;
import org.dominokit.domino.ui.icons.Icons;
import org.dominokit.domino.ui.style.Color;
import org.dominokit.domino.ui.style.ColorScheme;
import org.dominokit.domino.ui.themes.Theme;
import org.dominokit.domino.ui.utils.HasChangeHandlers.ChangeHandler;
import org.dominokit.domino.ui.utils.HasSelectionHandler.SelectionHandler;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;

import ch.so.agi.wgc.client.ui.BackgroundSwitcher;
import ch.so.agi.wgc.client.ui.SearchBox;
import ch.so.agi.wgc.shared.BackgroundMapConfig;
import ch.so.agi.wgc.shared.ConfigResponse;
import ch.so.agi.wgc.shared.ConfigService;
import ch.so.agi.wgc.shared.ConfigServiceAsync;
import elemental2.core.Global;
import elemental2.core.JsArray;
import elemental2.dom.DomGlobal;
import elemental2.dom.Element;
import elemental2.dom.EventListener;
import elemental2.dom.HTMLElement;
import elemental2.dom.HTMLInputElement;
import elemental2.dom.Headers;
import elemental2.dom.RequestInit;
import elemental2.core.Global;
import elemental2.core.JsArray;
import elemental2.core.JsString;
import elemental2.core.JsNumber;
import elemental2.dom.CSSProperties;
import elemental2.dom.DomGlobal;
import elemental2.dom.Event;
import elemental2.dom.EventListener;
import elemental2.dom.HTMLDivElement;
import elemental2.dom.HTMLElement;
import jsinterop.base.Js;
import jsinterop.base.JsPropertyMap;
import ol.Coordinate;
import ol.Extent;
import ol.Map;
import ol.MapBrowserEvent;
import ol.MapEvent;
import ol.View;
//import ol.events.Event;
import ol.layer.Tile;

import static org.jboss.elemento.Elements.*;
import static org.jboss.elemento.EventType.*;


public class AppEntryPoint implements EntryPoint {
    private MyMessages messages = GWT.create(MyMessages.class);
    private final ConfigServiceAsync configService = GWT.create(ConfigService.class);
    
    private List<BackgroundMapConfig> backgroundMapsConfig;
//    private String SEARCH_SERVICE_URL = "https://api3.geo.admin.ch/rest/services/api/SearchServer?sr=2056&limit=15&type=locations&origins=address,parcel&searchText=";
    private String SEARCH_SERVICE_URL = "https://geo.so.ch/api/search/v2/?filter=foreground,ch.so.agi.gemeindegrenzen,ch.so.agi.av.gebaeudeadressen.gebaeudeeingaenge,ch.so.agi.av.bodenbedeckung,ch.so.agi.av.grundstuecke.projektierte,ch.so.agi.av.grundstuecke.rechtskraeftig,ch.so.agi.av.nomenklatur.flurnamen,ch.so.agi.av.nomenklatur.gelaendenamen&searchtext=";    

    private NumberFormat fmtDefault = NumberFormat.getDecimalFormat();
    private NumberFormat fmtPercent = NumberFormat.getFormat("#0.0");
    
    private String MAP_DIV_ID = "map";

    private WgcMap map;
    
    SuggestBox suggestBox;
    
    public void onModuleLoad() {
        configService.configServer(new AsyncCallback<ConfigResponse>() {
            @Override
            public void onFailure(Throwable caught) {
                console.error(caught.getMessage());
                DomGlobal.window.alert(caught.getMessage());
            }

            @Override
            public void onSuccess(ConfigResponse result) {
                backgroundMapsConfig = result.getBackgroundMaps();                
                init();
            }
        });
    }

    @SuppressWarnings("unchecked")
    private void init() {      
        console.log("init");
        
        Theme theme = new Theme(ColorScheme.RED);
        theme.apply();
              
        body().add(div().id(MAP_DIV_ID));

        map = new WgcMapBuilder()
                .setMapId(MAP_DIV_ID)
                .addBackgroundLayers(backgroundMapsConfig)
                .build();
        
        body().add(new BackgroundSwitcher(map, backgroundMapsConfig));
        
        body().add(new SearchBox(map));
        
        // TODO getfeatureinfo
        // - url in config
        // - Den Rest selber zusammenstöpseln (und berechnen).
        // - fetch()
        map.addClickListener(new ol.event.EventListener<MapBrowserEvent>() {
            @Override
            public void onEvent(MapBrowserEvent event) {
                ol.source.Vector vectorSource = map.getHighlightLayer().getSource();
                vectorSource.clear(false);
                
                
                //console.log(event.getCoordinate().toString());
                
                double resolution = map.getView().getResolution();
                //console.log(map.getView().getResolution());

                // 50/51/101-Ansatz ist anscheinend bei OpenLayers normal.
                // -> siehe baseUrlFeatureInfo resp. ein Original-Request
                // im Web GIS Client.
                double minX = event.getCoordinate().getX() - 50 * resolution;
                double maxX = event.getCoordinate().getX() + 51 * resolution;
                double minY = event.getCoordinate().getY() - 50 * resolution;
                double maxY = event.getCoordinate().getY() + 51 * resolution;

//                String baseUrlFeatureInfo = map.getBaseUrlFeatureInfo();
//                List<String> foregroundLayers = map.getForegroundLayers();
//                //console.log(foregroundLayers);
//                String layers = String.join(",", foregroundLayers);
//                String urlFeatureInfo = baseUrlFeatureInfo + "&layers=" + layers;
//                urlFeatureInfo += "&query_layers=" + layers;
//                urlFeatureInfo += "&bbox=" + minX + "," + minY + "," + maxX + "," + maxY;
                
                //console.log(urlFeatureInfo);
            }
        });        
            
//        HTMLElement tocElement = div().id("toc").textContent("TOC").element();
//
//        tocElement.addEventListener("saved", new EventListener() {
//            @Override
//            public void handleEvent(Event evt) {
//                console.log("FUUUUUUBAR");
//            }
//        });
//        
//        
//        body().element().addEventListener("saved", new EventListener() {
//
//            @Override
//            public void handleEvent(Event evt) {
//                console.log("FUUUUUUBAR2");
//                
//            }
//        });
//        
//        body().add(tocElement);

        
        
        
        
//        map.addMoveEndListener(new ol.event.EventListener<MapEvent>() {
//            @Override
//            public void onEvent(MapEvent event) {
//                ol.View view = map.getView();
//                
//                ol.Extent extent = view.calculateExtent(map.getSize());
//                double easting = extent.getLowerLeftX() + (extent.getUpperRightX() - extent.getLowerLeftX()) / 2;
//                double northing = extent.getLowerLeftY() + (extent.getUpperRightY() - extent.getLowerLeftY()) / 2;
//                
//                String newUrl = Window.Location.getProtocol() + "//" + Window.Location.getHost() + Window.Location.getPath();
//                newUrl += "?bgLayer=" + map.getBackgroundLayer();
//                newUrl += "&layers=" + String.join(",", map.getForegroundLayers());
//                newUrl += "&layers_opacity=" + map.getForgroundLayerOpacities().stream().map(String::valueOf).collect(Collectors.joining(","));
//                newUrl += "&E=" + String.valueOf(easting);
//                newUrl += "&N=" + String.valueOf(northing);
//                newUrl += "&zoom=" + String.valueOf(view.getZoom());
//
//                updateURLWithoutReloading(newUrl);
//                
//                Element bigMapLinkElement = DomGlobal.document.getElementById("bigMapLink");
//                bigMapLinkElement.removeAttribute("href");
//                bigMapLinkElement.setAttribute("href", map.createBigMapUrl());
//            }
//        });
        
    }

   private static native void updateURLWithoutReloading(String newUrl) /*-{
        $wnd.history.pushState(newUrl, "", newUrl);
    }-*/;
}