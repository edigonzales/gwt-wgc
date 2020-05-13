package ch.so.agi.wgc.client;

import static elemental2.dom.DomGlobal.console;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.google.gwt.user.client.Window;

import elemental2.dom.DomGlobal;
import ol.MapOptions;
import ol.OLFactory;
import ol.layer.Base;
import ol.layer.LayerOptions;
import ol.layer.Vector;
import ol.source.ImageWms;
import ol.source.ImageWmsOptions;
import ol.source.ImageWmsParams;

public class WgcMap extends ol.Map {
    private String TITLE_ATTR_NAME = "title";
    private String ID_ATTR_NAME = "id";
    private String LAYER_TYPE_ATTR_NAME = "layerType";
    private String BACKGROUND_LAYER_ATTR_VALUE = "background";
    private String FOREGROUND_LAYER_ATTR_VALUE = "foreground";
    private String HIGHLIGHT_ATTR_NAME = "highlight";

    private String baseUrlWms;
    private String baseUrlFeatureInfo;
    private String baseUrlBigMap;
        
    private List<String> foregroundLayers = new ArrayList<String>();
    private List<Double> foregroundLayerOpacities = new ArrayList<Double>();
    
    public WgcMap(MapOptions mapOptions, String baseUrlWms, String baseUrlFeatureInfo, String baseUrlBigMap) {
        super(mapOptions);
        this.baseUrlWms = baseUrlWms;
        this.baseUrlFeatureInfo = baseUrlFeatureInfo;
        this.baseUrlBigMap = baseUrlBigMap;
    }
    
    // TODO: 
    // Liefert nur "id" des Layers zurück.
    // Um den Layer zu bekommen, muss anschliessend
    // die 'getMapLayerById' Methode verwendet
    // werden.
    
    // Wer braucht das?
    // - BackgroundSwitcher
    public List<String> getBackgroundLayers() {
        List<String> backgroundLayers = new ArrayList<String>();
        ol.Collection<Base> layers = this.getLayers();
        for (int i = 0; i < layers.getLength(); i++) {
            Base item = layers.item(i);
            if (item.get(LAYER_TYPE_ATTR_NAME) != null && item.get(LAYER_TYPE_ATTR_NAME).toString().equalsIgnoreCase(BACKGROUND_LAYER_ATTR_VALUE)) {
                backgroundLayers.add(item.get(ID_ATTR_NAME));
            }
        }
        return backgroundLayers;
    }
    
    public ol.layer.Vector getHighlightLayer() {
        return (Vector) getMapLayerById(HIGHLIGHT_ATTR_NAME);
    }

//    public void setVisibleBackgroundLayer(String id) {
//        Base layer = this.getMapLayerById(id);
//        if (layer != null) {
//            layer.setVisible(true);
//            backgroundLayer = id;
//        } else {
//            DomGlobal.window.alert("Backgroundlayer '" + id + "' not found.");
//        }
//    }
//    
//    public String getVisibleBackgroundLayer() {
//        return backgroundLayer;
//    }
    
    
//    public void addForegroundLayer(String id, double opacity) {
//        ImageWmsParams imageWMSParams = OLFactory.createOptions();
//        imageWMSParams.setLayers(id);
//        imageWMSParams.set("FORMAT", "image/png");
//        imageWMSParams.set("TRANSPARENT", "true");
//
//        ImageWmsOptions imageWMSOptions = OLFactory.createOptions();
//        imageWMSOptions.setUrl(baseUrlWms);
//        imageWMSOptions.setRatio(1.2f);
//        imageWMSOptions.setParams(imageWMSParams);
//
//        ImageWms imageWMSSource = new ImageWms(imageWMSOptions);
//
//        LayerOptions layerOptions = OLFactory.createOptions();
//        layerOptions.setSource(imageWMSSource);
//
//        ol.layer.Image wmsLayer = new ol.layer.Image(layerOptions);
//        wmsLayer.set(ID_ATTR_NAME, id);
//        wmsLayer.setOpacity(opacity);
//        
//        this.addLayer(wmsLayer);
//        
//        this.foregroundLayers.add(id);
//        this.foregroundLayerOpacities.add(opacity);
//    } 

//    public String getBaseUrlFeatureInfo() {
//        return baseUrlFeatureInfo;
//    }
    

//    public List<String> getForegroundLayers() {
//        return foregroundLayers;
//    }
//    
//    public List<Double> getForgroundLayerOpacities() {
//        return foregroundLayerOpacities;
//    }
    
    
    // Get Openlayers map layer by id.
    public Base getMapLayerById(String id) {
        ol.Collection<Base> layers = this.getLayers();
        for (int i = 0; i < layers.getLength(); i++) {
            Base item = layers.item(i);
            try {
                String layerId = item.get(ID_ATTR_NAME);                
                if (layerId == null) {
                    continue;
                }
                if (layerId.equalsIgnoreCase(id)) {
                    return item;
                }
            } catch (Exception e) {
                console.log(e.getMessage());
                console.log("should not reach here");
            }
        }
        return null;
    }
}
