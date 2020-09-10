package ch.so.agi.wgc.client.ui;

import static elemental2.dom.DomGlobal.console;
import static org.jboss.elemento.Elements.body;
import static org.jboss.elemento.Elements.div;
import static org.jboss.elemento.Elements.img;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import org.dominokit.domino.ui.dropdown.DropDownMenu;
import org.dominokit.domino.ui.forms.SuggestBox;
import org.dominokit.domino.ui.forms.SuggestBoxStore;
import org.dominokit.domino.ui.forms.SuggestItem;
import org.dominokit.domino.ui.forms.SuggestBox.DropDownPositionDown;
import org.dominokit.domino.ui.forms.SuggestBoxStore.SuggestionsHandler;
import org.dominokit.domino.ui.icons.Icon;
import org.dominokit.domino.ui.icons.Icons;
import org.dominokit.domino.ui.icons.MdiIcon;
import org.dominokit.domino.ui.style.Color;
import org.dominokit.domino.ui.utils.HasSelectionHandler.SelectionHandler;
import org.gwtproject.event.shared.HandlerRegistration;
import org.jboss.elemento.Attachable;
import org.jboss.elemento.IsElement;

import com.google.gwt.core.client.GWT;

import ch.so.agi.wgc.client.WgcMap;
import elemental2.core.Global;
import elemental2.core.JsArray;
import elemental2.core.JsNumber;
import elemental2.core.JsString;
import elemental2.core.JsBoolean;
import elemental2.dom.CSSProperties;
import elemental2.dom.CSSProperties.HeightUnionType;
import elemental2.dom.CustomEvent;
import elemental2.dom.CustomEventInit;
import elemental2.dom.DomGlobal;
import elemental2.dom.Event;
import elemental2.dom.EventInit;
import elemental2.dom.EventListener;
import elemental2.dom.HTMLElement;
import elemental2.dom.HTMLInputElement;
import elemental2.dom.Headers;
import elemental2.dom.KeyboardEvent;
import elemental2.dom.MutationRecord;
import elemental2.dom.RequestInit;
import jsinterop.base.Js;
import jsinterop.base.JsPropertyMap;
import ol.Collection;
import ol.Coordinate;
import ol.Extent;
import ol.Feature;
import ol.FeatureOptions;
import ol.OLFactory;
import ol.View;
import ol.format.GeoJson;
import ol.style.Circle;
import ol.style.CircleOptions;
import ol.style.Stroke;
import ol.style.Style;
import ol.style.StyleOptions;

public class SearchBox implements IsElement<HTMLElement>, Attachable {
    // TODO config
    private String SEARCH_SERVICE_URL = "https://geo.so.ch/api/search/v2/?filter=foreground,ch.so.agi.gemeindegrenzen,ch.so.agi.av.gebaeudeadressen.gebaeudeeingaenge,ch.so.agi.av.bodenbedeckung,ch.so.agi.av.grundstuecke.projektierte,ch.so.agi.av.grundstuecke.rechtskraeftig,ch.so.agi.av.nomenklatur.flurnamen,ch.so.agi.av.nomenklatur.gelaendenamen&searchtext=";    
    private String DATA_SERVICE_URL = "https://geo.so.ch/api/data/v1/";
    private String DATAPRODUCT_SERVICE_URL = "https://geo.so.ch/api/dataproduct/v1/weblayers";
    private String WMS_URL = "https://geo.so.ch/api/wms?SERVICE=WMS&REQUEST=GetMap&VERSION=1.3.0&FORMAT=image%2Fpng&TRANSPARENT=true&&STYLES=&SRS=EPSG%3A2056&CRS=EPSG%3A2056&TILED=false&DPI=96";
            
    private final HTMLElement root;
    private HTMLElement layerPanelContainer;
    private HandlerRegistration handlerRegistration;
    private WgcMap map;
    private SuggestBox suggestBox;
    
    private int ROOT_HEIGHT_INIT = 0;

    @SuppressWarnings("unchecked")
    public SearchBox(WgcMap map) {
        this.map = map;
        
        root = div().id("SearchBox").element();
        HTMLElement logoDiv = div().id("logoDiv")
                .add(img()
                        .attr("src", GWT.getHostPageBaseURL() + "logo.png")
                        .attr("alt", "Logo Kanton Solothurn").attr("width", "50%"))
                .element();
        root.appendChild(logoDiv);

        SuggestBoxStore dynamicStore = new SuggestBoxStore() {
            @Override
            public void filter(String value, SuggestionsHandler suggestionsHandler) {
                if (value.trim().length() == 0) {
                    return;
                }
                
                RequestInit requestInit = RequestInit.create();
                Headers headers = new Headers();
                headers.append("Content-Type", "application/x-www-form-urlencoded"); // CORS and preflight...
                requestInit.setHeaders(headers);
                
                console.log(SEARCH_SERVICE_URL + value.trim().toLowerCase());

                DomGlobal.fetch(SEARCH_SERVICE_URL + value.trim().toLowerCase(), requestInit)
                .then(response -> {
                    if (!response.ok) {
                        return null;
                    }
                    return response.text();
                })
                .then(json -> {
                    List<SuggestItem<SearchResult>> featureResults = new ArrayList<SuggestItem<SearchResult>>();
                    List<SuggestItem<SearchResult>> dataproductResults = new ArrayList<SuggestItem<SearchResult>>();

                    List<SuggestItem<SearchResult>> suggestItems = new ArrayList<>();
                    JsPropertyMap<?> parsed = Js.cast(Global.JSON.parse(json));
                    JsArray<?> results = Js.cast(parsed.get("results"));
                    for (int i = 0; i < results.length; i++) {
                        JsPropertyMap<?> resultObj = Js.cast(results.getAt(i));
                            
                        // TODO sort by feature (sub-feature) and dataproduct
                        // ah, durchmischt sind feature und dataproduct nie?
                        
                        // Grouping? https://github.com/DominoKit/domino-ui/blob/master/domino-ui/src/main/java/org/dominokit/domino/ui/forms/SelectOptionGroup.java#L25
                        
                        
                        
                        if (resultObj.has("feature")) {
                            JsPropertyMap feature = (JsPropertyMap) resultObj.get("feature");
                            String display = ((JsString) feature.get("display")).normalize();
                            String dataproductId = ((JsString) feature.get("dataproduct_id")).normalize();
                            String idFieldName = ((JsString) feature.get("id_field_name")).normalize();
                            int featureId = new Double(((JsNumber) feature.get("feature_id")).valueOf()).intValue();
                            List<Double> bbox = ((JsArray) feature.get("bbox")).asList();
 
                            SearchResult searchResult = new SearchResult();
                            searchResult.setLabel(display);
                            searchResult.setDataproductId(dataproductId);
                            searchResult.setIdFieldName(idFieldName);
                            searchResult.setFeatureId(featureId);
                            searchResult.setBbox(bbox);
                            searchResult.setType("feature");
                            
                            Icon icon;
                            if (dataproductId.contains("gebaeudeadressen")) {
                                icon = Icons.ALL.mail();
                            } else if (dataproductId.contains("grundstueck")) {
                                icon = Icons.ALL.home();
                            } else if (dataproductId.contains("flurname"))  {
                                icon = Icons.ALL.terrain();
                            } else {
                                icon = Icons.ALL.place();
                            }
                            
                            SuggestItem<SearchResult> suggestItem = SuggestItem.create(searchResult, searchResult.getLabel(), icon);
                            featureResults.add(suggestItem);
//                            suggestItems.add(suggestItem);                            
                            
                        } else if (resultObj.has("dataproduct")) {
                            JsPropertyMap dataproduct = (JsPropertyMap) resultObj.get("dataproduct");
                            String display = ((JsString) dataproduct.get("display")).normalize();
                            String dataproductId = ((JsString) dataproduct.get("dataproduct_id")).normalize();

                            SearchResult searchResult = new SearchResult();
                            searchResult.setLabel(display);
                            searchResult.setDataproductId(dataproductId);
                            searchResult.setType("dataproduct");

                            MdiIcon icon;
                            if (dataproduct.has("sublayers")) {
                                icon = Icons.ALL.layers_plus_mdi();  
                            } else {
                                icon = Icons.ALL.layers_mdi();
                            } 
                            
                            SuggestItem<SearchResult> suggestItem = SuggestItem.create(searchResult, searchResult.getLabel(), icon);                            
                            dataproductResults.add(suggestItem);
//                            suggestItems.add(suggestItem);
                        }
                    }
//                    SearchResult featureGroup = new SearchResult();
//                    featureGroup.setLabel("<b>Orte</b>");
//                    SuggestItem<SearchResult> featureGroupItem = SuggestItem.create(featureGroup, featureGroup.getLabel(), null);                            
//                    suggestItems.add(featureGroupItem);
                    
                    suggestItems.addAll(featureResults);
                    suggestItems.addAll(dataproductResults);

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
                HTMLInputElement el =(HTMLInputElement) suggestBox.getInputElement().element();
                SearchResult searchResult = (SearchResult) searchValue;
                SuggestItem<SearchResult> suggestItem = SuggestItem.create(searchResult, el.value);
                handler.accept(suggestItem);
            }
        };

        suggestBox = SuggestBox.create("Suche: Adressen, Orte und Karten", dynamicStore);
        suggestBox.setId("SuggestBox");
        suggestBox.addLeftAddOn(Icons.ALL.search());
        suggestBox.setAutoSelect(false);
        suggestBox.setFocusColor(Color.RED);
        suggestBox.setFocusOnClose(false);
        
        HTMLElement resetIcon = Icons.ALL.close().setId("SearchResetIcon").element();
        resetIcon.addEventListener("click", new EventListener() {
            @Override
            public void handleEvent(Event evt) {
                HTMLInputElement el =(HTMLInputElement) suggestBox.getInputElement().element();
                el.value = "";
                suggestBox.unfocus();
                ol.source.Vector vectorSource = map.getHighlightLayer().getSource();
                vectorSource.clear(false); 
            }
        });
        
        suggestBox.addRightAddOn(resetIcon);
                
        suggestBox.getInputElement().addEventListener("focus", new EventListener() {
            @Override
            public void handleEvent(Event evt) {
                ol.source.Vector vectorSource = map.getHighlightLayer().getSource();
                vectorSource.clear(false); 
            }
        });
        
        // TODO open suggestionsMenu when clicking in suggestBox and some
        // text is in there.
//        suggestBox.getInputElement().addClickListener(new EventListener() {
//            @Override
//            public void handleEvent(Event evt) {
//                console.log("click");
//                KeyboardEvent event = new KeyboardEvent("keydown");
//                suggestBox.element().dispatchEvent(event);
//            }
//        });

        suggestBox.getInputElement().setAttribute("autocomplete", "off");
        suggestBox.getInputElement().setAttribute("spellcheck", "false");
        DropDownMenu suggestionsMenu = suggestBox.getSuggestionsMenu();
        suggestionsMenu.setPosition(new DropDownPositionDown());
        suggestionsMenu.setSearchable(false);

        suggestBox.addSelectionHandler(new SelectionHandler() {
            @Override
            public void onSelection(Object value) {
                SuggestItem<SearchResult> item = (SuggestItem<SearchResult>) value;
                SearchResult result = (SearchResult) item.getValue();
                
                RequestInit requestInit = RequestInit.create();
                Headers headers = new Headers();
                headers.append("Content-Type", "application/x-www-form-urlencoded"); // CORS and preflight...
                requestInit.setHeaders(headers);
                
                if (result.getType().equalsIgnoreCase("feature")) {
                    String dataproductId = result.getDataproductId();
                    String idFieldName = result.getIdFieldName();
                    String featureId = String.valueOf(result.getFeatureId());
                    
                    DomGlobal.fetch(DATA_SERVICE_URL + dataproductId + "/?filter=[[\""+idFieldName+"\",\"=\","+featureId+"]]", requestInit)
                    .then(response -> {
                        if (!response.ok) {
                            return null;
                        }
                        return response.text();
                    })
                    .then(json -> {
                        Feature[] features = (new GeoJson()).readFeatures(json);

                        FeatureOptions featureOptions = OLFactory.createOptions();
                        featureOptions.setGeometry(features[0].getGeometry());
                        Feature feature = new Feature(featureOptions);

                        Stroke stroke = new Stroke();
                        stroke.setWidth(8);
                        stroke.setColor(new ol.color.Color(230, 0, 0, 0.6));

                        if (features[0].getGeometry().getType().equalsIgnoreCase("Point")) {
                            CircleOptions circleOptions = new CircleOptions();
                            circleOptions.setRadius(10);
                            circleOptions.setStroke(stroke);
                            StyleOptions styleOptions = new StyleOptions();
                            styleOptions.setImage(new Circle(circleOptions));
                            Style style = new Style(styleOptions);
                            feature.setStyle(style);
                        } else {
                            Style style = new Style();
                            style.setStroke(stroke);
                            feature.setStyle(style);
                        }

                        ol.source.Vector vectorSource = map.getHighlightLayer().getSource();
                        vectorSource.clear(false); // false=opt_fast resp. eben nicht. Keine Events, falls true?
                        vectorSource.addFeature(feature);
                        return null;
                    }).catch_(error -> {
                        console.log(error);
                        return null;
                    });
                    
                    // Zoom to feature.
                    List<Double> bbox = result.getBbox();                 
                    Extent extent = new Extent(bbox.get(0), bbox.get(1), bbox.get(2), bbox.get(3));
                    View view = map.getView();
                    double resolution = view.getResolutionForExtent(extent);
                    view.setZoom(Math.floor(view.getZoomForResolution(resolution)) - 1);
                    double x = extent.getLowerLeftX() + extent.getWidth() / 2;
                    double y = extent.getLowerLeftY() + extent.getHeight() / 2;
                    view.setCenter(new Coordinate(x,y));
                } else if (result.getType().equalsIgnoreCase("dataproduct")) {
                    String dataproductId = result.getDataproductId();
                    DomGlobal.fetch(DATAPRODUCT_SERVICE_URL + "?filter=" + dataproductId, requestInit)
                    .then(response -> {
                        if (!response.ok) {
                            return null;
                        }
                        return response.text();
                    })
                    .then(json -> {
                        JsPropertyMap<?> parsed = Js.cast(Global.JSON.parse(json));                        
                        JsPropertyMap<?> weblayer = (JsPropertyMap<?>) ((JsArray) parsed.get(dataproductId)).getAt(0);
                        console.log(weblayer.get("name"));
                        
                        
                        if (weblayer.has("sublayers")) {
                            console.log("add layer group");
                            
                            // TODO: besser
                            // Nur falls nicht bereits layer geladen sind.
                            
                            //root.appendChild(div().style("border-bottom: 1px solid rgb(233, 233, 233);").element());
                            
                            JsArray<?> sublayers = Js.cast(weblayer.get("sublayers"));
                            for(Object sublayerObj : sublayers.asList()) {
                                JsPropertyMap<?> sublayer = Js.cast(sublayerObj);
                                addLayer(sublayer);
                            }
                            // TODO: besser
//                            int height = root.clientHeight;
//                            height += 25;
//                            console.log(root.clientHeight);
//                            root.style.setProperty("height", String.valueOf(height) + "px"); 

                            

                        } else {
                            console.log("add single layer");
                        }
                                  
                        CustomEventInit eventInit = CustomEventInit.create();
                        eventInit.setDetail("fubar");
                        eventInit.setBubbles(true);
                        CustomEvent event = new CustomEvent("saved", eventInit);
                        root.dispatchEvent(event);

                        
                        
                        return null;
                    }).catch_(error -> {
                        console.log(error);
                        return null;
                    });

                }
                
            }
        });
        HTMLElement suggestBoxDiv = div().id("suggestBoxDiv").add(suggestBox).element();
        root.appendChild(suggestBoxDiv);
        
        layerPanelContainer = div().id("layer-panel-container").element();
        root.appendChild(layerPanelContainer);
        
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
    
    public void addLayer(JsPropertyMap<?> layer) {
        if (ROOT_HEIGHT_INIT == 0) {
            ROOT_HEIGHT_INIT = root.clientHeight;
        }
        
        String name = ((JsString) layer.get("name")).normalize();
        String title = ((JsString) layer.get("title")).normalize();
        double opacity = ((JsNumber) layer.get("opacity")).valueOf();
        Boolean visibility = (Boolean) layer.get("visibility");
                
        // TODO: nicht root direkt appenden
        layerPanelContainer.appendChild(div().css("layer-panel").id("layer-panel-"+name).textContent(title).element());
        
        // TODO layer-panel-container bekomme height von suggestbox minus anfangshöhe.
        // ROOT_HEIGHT_INIT
        
        {
            int height = root.clientHeight;
            height += 50;
            console.log(height);
            root.style.setProperty("height", String.valueOf(height) + "px");
        }
        {
            int height = layerPanelContainer.clientHeight;
            height += 50;
            console.log(ROOT_HEIGHT_INIT);

            if (height >= (root.clientHeight - ROOT_HEIGHT_INIT)) {
                height = root.clientHeight - ROOT_HEIGHT_INIT;
            } 
            layerPanelContainer.style.setProperty("height", String.valueOf(height) + "px");  
            
            // TODO noch max height setzen.
            // warum aber immer noch bissle mehr wheat ist, weiss ich nicht. Nur wenn noch kein scrollbar.
        }



    }

}
