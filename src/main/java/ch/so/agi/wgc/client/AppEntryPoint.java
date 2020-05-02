package ch.so.agi.wgc.client;

import static elemental2.dom.DomGlobal.console;
import static org.jboss.elemento.Elements.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.dominokit.domino.ui.style.ColorScheme;
import org.dominokit.domino.ui.themes.Theme;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;

import ch.so.agi.wgc.shared.BackgroundMapConfig;
import ch.so.agi.wgc.shared.ConfigResponse;
import ch.so.agi.wgc.shared.ConfigService;
import ch.so.agi.wgc.shared.ConfigServiceAsync;
import elemental2.dom.DomGlobal;
import elemental2.dom.Element;
import ol.Coordinate;
import ol.Map;
import ol.MapBrowserEvent;
import ol.MapEvent;
import ol.events.Event;

public class AppEntryPoint implements EntryPoint {
    private MyMessages messages = GWT.create(MyMessages.class);
    private final ConfigServiceAsync configService = GWT.create(ConfigService.class);
    
    private List<BackgroundMapConfig> backgroundMapsConfig;
    
    private NumberFormat fmtDefault = NumberFormat.getDecimalFormat();
    private NumberFormat fmtPercent = NumberFormat.getFormat("#0.0");
    
    private String MAP_DIV_ID = "map";

    private WgcMap map;
    
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
        
        Theme theme = new Theme(ColorScheme.WHITE);
        theme.apply();

        body().add(div().id(MAP_DIV_ID));

        map = new WgcMapBuilder()
                .setMapId(MAP_DIV_ID)
                .addBackgroundLayers(backgroundMapsConfig)
                .build();
        
        body().add(div().id("BackgroundSwitcher").add(span().textContent("Hintergrundkarten")).add(div().add(span().textContent("fubar"))));
        

//        // TODO
//        // Use elemental2: DomGlobal.window.location.getSearch()
//        // new URLSearchParams() not available in RC1 (?)        
//        String bgLayer = "";
//        if (Window.Location.getParameter("bgLayer") != null) {
//            bgLayer = Window.Location.getParameter("bgLayer").toString();
//        } else {
//            DomGlobal.window.alert("bgLayer missing");
//            console.error("bgLayer missing");
//            return;
//        }
//        List<String> layerList = new ArrayList<String>();
//        if (Window.Location.getParameter("layers") != null) {
//            String layers = Window.Location.getParameter("layers").toString();
//            layerList = Arrays.asList(layers.split(",", -1));
//        }
//        List<Double> opacityList = new ArrayList<Double>();
//        if (Window.Location.getParameter("layers_opacity") != null) {
//            String opacities = Window.Location.getParameter("layers_opacity").toString();
//            List<String> rawList = Arrays.asList(opacities.split(","));
//            for(int i=0; i<rawList.size(); i++) {
//                opacityList.add(Double.parseDouble(rawList.get(i)));
//            }
//        }
//        if (Window.Location.getParameter("E") != null && Window.Location.getParameter("N") != null) {
//            double easting = Double.valueOf(Window.Location.getParameter("E"));
//            double northing = Double.valueOf(Window.Location.getParameter("N"));
//            map.getView().setCenter(new Coordinate(easting,northing));
//        }
//        if (Window.Location.getParameter("zoom") != null) {
//            map.getView().setZoom(Double.valueOf(Window.Location.getParameter("zoom")));
//        }
//
//        map.setBackgroundLayer(bgLayer);
//        
//        for (int i=0; i<layerList.size(); i++) {
//            map.addForegroundLayer(layerList.get(i), opacityList.get(i));
//        }
        
        // TODO muss upgedated werden...
//        BigMapLink bigMapLink = new BigMapLink(map);
//        body().add(bigMapLink.element());
        
        // TODO getfeatureinfo
        // - url in config
        // - Den Rest selber zusammenstöpseln (und berechnen).
        // - fetch()
        map.addClickListener(new ol.event.EventListener<MapBrowserEvent>() {
            @Override
            public void onEvent(MapBrowserEvent event) {
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

                String baseUrlFeatureInfo = map.getBaseUrlFeatureInfo();
                List<String> foregroundLayers = map.getForegroundLayers();
                //console.log(foregroundLayers);
                String layers = String.join(",", foregroundLayers);
                String urlFeatureInfo = baseUrlFeatureInfo + "&layers=" + layers;
                urlFeatureInfo += "&query_layers=" + layers;
                urlFeatureInfo += "&bbox=" + minX + "," + minY + "," + maxX + "," + maxY;
                
                //console.log(urlFeatureInfo);
            }
        });        
                
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