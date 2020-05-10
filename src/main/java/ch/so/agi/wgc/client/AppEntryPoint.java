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
import org.dominokit.domino.ui.icons.Icons;
import org.dominokit.domino.ui.style.Color;
import org.dominokit.domino.ui.style.ColorScheme;
import org.dominokit.domino.ui.themes.Theme;
import org.dominokit.domino.ui.utils.HasSelectionHandler.SelectionHandler;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;

import ch.so.agi.wgc.client.ui.BackgroundSwitcher;
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
import ol.Map;
import ol.MapBrowserEvent;
import ol.MapEvent;
//import ol.events.Event;
import ol.layer.Tile;

import static org.jboss.elemento.Elements.*;
import static org.jboss.elemento.EventType.*;


public class AppEntryPoint implements EntryPoint {
    private MyMessages messages = GWT.create(MyMessages.class);
    private final ConfigServiceAsync configService = GWT.create(ConfigService.class);
    
    private List<BackgroundMapConfig> backgroundMapsConfig;
    private String SEARCH_SERVICE_URL = "https://api3.geo.admin.ch/rest/services/api/SearchServer?sr=2056&limit=15&type=locations&origins=address,parcel&searchText=";

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
        
        Theme theme = new Theme(ColorScheme.RED);
        theme.apply();

        body().add(div().id(MAP_DIV_ID));

        map = new WgcMapBuilder()
                .setMapId(MAP_DIV_ID)
                .addBackgroundLayers(backgroundMapsConfig)
                .build();
        
        body().add(new BackgroundSwitcher(map, backgroundMapsConfig));
        
        
        
        
        HTMLElement searchCard = div().id("SearchBox").element();
        body().add(searchCard);

        HTMLElement logoDiv = div().id("logoDiv")
                .add(img()
                        .attr("src", GWT.getHostPageBaseURL() + "logo.png")
                        .attr("alt", "Logo Kanton Solothurn").attr("width", "50%"))
                .element();
        searchCard.appendChild(logoDiv);

        SuggestBoxStore dynamicStore = new SuggestBoxStore() {
            @Override
            public void filter(String value, SuggestionsHandler suggestionsHandler) {
                if (value.trim().length() == 0) {
                    return;
                }
                
                // fetch(url, init) -> https://www.javadoc.io/doc/com.google.elemental2/elemental2-dom/1.0.0-RC1/elemental2/dom/RequestInit.html
                // abort -> https://github.com/react4j/react4j-flux-challenge/blob/b9b28250fd3f954c690f874605f67e2a24a7274d/src/main/java/react4j/sithtracker/model/SithPlaceholder.java
                DomGlobal.fetch(SEARCH_SERVICE_URL + value.trim().toLowerCase())
                .then(response -> {
                    if (!response.ok) {
                        return null;
                    }
                    return response.text();
                })
                .then(json -> {
                    List<SuggestItem<SearchResult>> suggestItems = new ArrayList<>();
                    JsPropertyMap<?> parsed = Js.cast(Global.JSON.parse(json));
                    JsArray<?> results = Js.cast(parsed.get("results"));
                    for (int i = 0; i < results.length; i++) {
                        JsPropertyMap<?> feature = Js.cast(results.getAt(i));
                        JsPropertyMap<?> attrs = Js.cast(feature.get("attrs"));

                        SearchResult searchResult = new SearchResult();
                        searchResult.setLabel(((JsString) attrs.get("label")).normalize());
                        searchResult.setOrigin(((JsString) attrs.get("origin")).normalize());
                        searchResult.setBbox(((JsString) attrs.get("geom_st_box2d")).normalize());
                        searchResult.setEasting(((JsNumber) attrs.get("y")).valueOf());
                        searchResult.setNorthing(((JsNumber) attrs.get("x")).valueOf());
                        
//                      // TODO icon type depending on address and parcel ?
                        SuggestItem<SearchResult> suggestItem = SuggestItem.create(searchResult, searchResult.getLabel(),
                                Icons.ALL.place());
                        suggestItems.add(suggestItem);
                    }
                    suggestionsHandler.onSuggestionsReady(suggestItems);
                    return null;
                }).catch_(error -> {
                    console.log(error);
                    return null;
                });
            }

            @Override
            public void find(Object searchValue, Consumer handler) {
                if (searchValue == null) {
                    return;
                }
                SearchResult searchResult = (SearchResult) searchValue;
                SuggestItem<SearchResult> suggestItem = SuggestItem.create(searchResult, null);
                handler.accept(suggestItem);
            }
        };

        SuggestBox suggestBox = SuggestBox.create("Suche: Adressen und Orte", dynamicStore);
        suggestBox.setIcon(Icons.ALL.search());
//        suggestBox.setHighlightColor(Color.RED);
        suggestBox.setFocusColor(Color.RED);
        suggestBox.getInputElement().setAttribute("autocomplete", "off");
        suggestBox.getInputElement().setAttribute("spellcheck", "false");
        DropDownMenu suggestionsMenu = suggestBox.getSuggestionsMenu();
        suggestionsMenu.setPosition(new DropDownPositionDown());
        
        suggestBox.addSelectionHandler(new SelectionHandler() {
            @Override
            public void onSelection(Object value) {
//                loader.stop();
//                resetGui();

                SuggestItem<SearchResult> item = (SuggestItem<SearchResult>) value;
                SearchResult result = (SearchResult) item.getValue();
                
                String[] coords = result.getBbox().substring(4,result.getBbox().length()-1).split(",");
                String[] coordLL = coords[0].split(" ");
                String[] coordUR = coords[1].split(" ");
//                Extent extent = new Extent(Double.valueOf(coordLL[0]).doubleValue(), Double.valueOf(coordLL[1]).doubleValue(), 
//                Double.valueOf(coordUR[0]).doubleValue(), Double.valueOf(coordUR[1]).doubleValue());
//                
//                double easting = Double.valueOf(result.getEasting()).doubleValue();
//                double northing = Double.valueOf(result.getNorthing()).doubleValue();
//                
//                Coordinate coordinate = new Coordinate(easting, northing);
//                sendCoordinateToServer(coordinate.toStringXY(3), null);
                
                // TODO: remove focus
                // -> Tried a lot but failed. Ask the authors.
            }
        });

        HTMLElement suggestBoxDiv = div().id("suggestBoxDiv").add(suggestBox).element();
        searchCard.appendChild(suggestBoxDiv);

        
        

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