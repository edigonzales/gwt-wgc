package ch.so.agi.wgc.client.ui;

import static elemental2.dom.DomGlobal.console;
import static org.jboss.elemento.Elements.body;
import static org.jboss.elemento.Elements.div;
import static org.jboss.elemento.Elements.img;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import org.dominokit.domino.ui.dropdown.DropDownMenu;
import org.dominokit.domino.ui.forms.SuggestBox;
import org.dominokit.domino.ui.forms.SuggestBoxStore;
import org.dominokit.domino.ui.forms.SuggestItem;
import org.dominokit.domino.ui.forms.SuggestBox.DropDownPositionDown;
import org.dominokit.domino.ui.forms.SuggestBoxStore.SuggestionsHandler;
import org.dominokit.domino.ui.icons.Icon;
import org.dominokit.domino.ui.icons.Icons;
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
import elemental2.dom.DomGlobal;
import elemental2.dom.HTMLElement;
import elemental2.dom.HTMLInputElement;
import elemental2.dom.Headers;
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
import ol.style.Stroke;
import ol.style.Style;

public class SearchBox implements IsElement<HTMLElement>, Attachable {
    // TODO config
    private String SEARCH_SERVICE_URL = "https://geo.so.ch/api/search/v2/?filter=foreground,ch.so.agi.gemeindegrenzen,ch.so.agi.av.gebaeudeadressen.gebaeudeeingaenge,ch.so.agi.av.bodenbedeckung,ch.so.agi.av.grundstuecke.projektierte,ch.so.agi.av.grundstuecke.rechtskraeftig,ch.so.agi.av.nomenklatur.flurnamen,ch.so.agi.av.nomenklatur.gelaendenamen&searchtext=";    
    private String DATA_SERVICE_URL = "https://geo.so.ch/api/data/v1/";
            
    private final HTMLElement root;
    private HandlerRegistration handlerRegistration;
    private WgcMap map;
    private SuggestBox suggestBox;

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
                    List<SuggestItem<SearchResult>> suggestItems = new ArrayList<>();
                    JsPropertyMap<?> parsed = Js.cast(Global.JSON.parse(json));
                    JsArray<?> results = Js.cast(parsed.get("results"));
                    for (int i = 0; i < results.length; i++) {
                        JsPropertyMap<?> resultObj = Js.cast(results.getAt(i));
                                                
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
                            
                            Icon icon;
                            if (dataproductId.contains("gebaeudeadressen")) {
                                icon = Icons.ALL.mail_outline();
                            } else if (dataproductId.contains("grundstueck")) {
                                icon = Icons.ALL.home();
                            } else {
                                icon = Icons.ALL.place();
                            }
                            SuggestItem<SearchResult> suggestItem = SuggestItem.create(searchResult, searchResult.getLabel(), icon);
                            suggestItems.add(suggestItem);
                        }
                        // else if 'dataproduct'
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
                HTMLInputElement el =(HTMLInputElement) suggestBox.getInputElement().element();
                SearchResult searchResult = (SearchResult) searchValue;
                SuggestItem<SearchResult> suggestItem = SuggestItem.create(searchResult, el.value);
                handler.accept(suggestItem);
            }
        };

        suggestBox = SuggestBox.create("Suche: Adressen und Orte", dynamicStore);
        suggestBox.setId("SuggestBox");
        suggestBox.setIcon(Icons.ALL.search());
        suggestBox.setAutoSelect(false);
        suggestBox.setFocusColor(Color.RED);
        suggestBox.setFocusOnClose(false);

        suggestBox.getInputElement().setAttribute("autocomplete", "off");
        suggestBox.getInputElement().setAttribute("spellcheck", "false");
        DropDownMenu suggestionsMenu = suggestBox.getSuggestionsMenu();
        suggestionsMenu.setPosition(new DropDownPositionDown());

        suggestBox.addSelectionHandler(new SelectionHandler() {
            @Override
            public void onSelection(Object value) {
                SuggestItem<SearchResult> item = (SuggestItem<SearchResult>) value;
                SearchResult result = (SearchResult) item.getValue();
                
                RequestInit requestInit = RequestInit.create();
                Headers headers = new Headers();
                headers.append("Content-Type", "application/x-www-form-urlencoded"); // CORS and preflight...
                requestInit.setHeaders(headers);
                
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
                    List<SuggestItem<SearchResult>> suggestItems = new ArrayList<>();
                    JsPropertyMap<?> parsed = Js.cast(Global.JSON.parse(json));
                    
                    Feature[] features = (new GeoJson()).readFeatures(json);

                    // TODO
                    // andere Geometrietypen
                    
                    FeatureOptions featureOptions = OLFactory.createOptions();
                    featureOptions.setGeometry(features[0].getGeometry());
                    Feature feature = new Feature(featureOptions);

                    Style style = new Style();
                    Stroke stroke = new Stroke();
                    stroke.setWidth(8);
                    stroke.setColor(new ol.color.Color(230, 0, 0, 0.6));
                    style.setStroke(stroke);
                    feature.setStyle(style);

                    ol.source.Vector vectorSource = map.getHighlightLayer().getSource();
                    vectorSource.clear(false); // false=opt_fast resp. eben nicht. Keine Events, falls true?
                    vectorSource.addFeature(feature);

                    return null;
                }).catch_(error -> {
                    console.log(error);
                    return null;
                });
                
                
                
                List<Double> bbox = result.getBbox();                 
                Extent extent = new Extent(bbox.get(0), bbox.get(1), bbox.get(2), bbox.get(3));
                View view = map.getView();
                double resolution = view.getResolutionForExtent(extent);
                view.setZoom(Math.floor(view.getZoomForResolution(resolution)) - 1);
                double x = extent.getLowerLeftX() + extent.getWidth() / 2;
                double y = extent.getLowerLeftY() + extent.getHeight() / 2;
                view.setCenter(new Coordinate(x,y));                
            }
        });
        HTMLElement suggestBoxDiv = div().id("suggestBoxDiv").add(suggestBox).element();
        root.appendChild(suggestBoxDiv);
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

}
