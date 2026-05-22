package com.travel.travelweb.api.dto;

/**
 * App WebView 地图配置（与 Web 端 landscape-detail.html 使用同一 JS Key）
 */
public class AmapMapConfigResponse {
    /** 高德 JS API Key，对应 webapi.amap.com/maps */
    private String webJsKey = "ee7b7be648faf8d9200c001ecab389b0";
    private String jsVersion = "1.4.15";

    public AmapMapConfigResponse() {}

    public String getWebJsKey() { return webJsKey; }
    public void setWebJsKey(String webJsKey) { this.webJsKey = webJsKey; }
    public String getJsVersion() { return jsVersion; }
    public void setJsVersion(String jsVersion) { this.jsVersion = jsVersion; }
}
