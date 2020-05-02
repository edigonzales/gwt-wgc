package ch.so.agi.wgc.shared;

import com.google.gwt.user.client.rpc.IsSerializable;

public class BackgroundMapConfig implements IsSerializable {
    public BackgroundMapConfig() {}
    
    private String id;
    private String title;
    private String url;
    private String layer;
    private String requestEncoding = "REST";
    private String format = "image/png";
    private String matrixSet = "EPSG:2056";
    private String style = "default";
    private boolean active;
    private String thumbnail;

    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }
    public String getTitle() {
        return title;
    }
    public void setTitle(String title) {
        this.title = title;
    }
    public String getUrl() {
        return url;
    }
    public void setUrl(String url) {
        this.url = url;
    }
    public String getLayer() {
        return layer;
    }
    public void setLayer(String layer) {
        this.layer = layer;
    }
    public String getRequestEncoding() {
        return requestEncoding;
    }
    public void setRequestEncoding(String requestEncoding) {
        this.requestEncoding = requestEncoding;
    }
    public String getFormat() {
        return format;
    }
    public void setFormat(String format) {
        this.format = format;
    }
    public String getMatrixSet() {
        return matrixSet;
    }
    public void setMatrixSet(String matrixSet) {
        this.matrixSet = matrixSet;
    }
    public String getStyle() {
        return style;
    }
    public void setStyle(String style) {
        this.style = style;
    }
    public boolean isActive() {
        return active;
    }
    public void setActive(boolean active) {
        this.active = active;
    }
    public String getThumbnail() {
        return thumbnail;
    }
    public void setThumbnail(String thumbnail) {
        this.thumbnail = thumbnail;
    }
    @Override
    public String toString() {
        return "BackgroundMapConfig [id=" + id + ", title=" + title + ", url=" + url + ", layer=" + layer + "]";
    }
}
