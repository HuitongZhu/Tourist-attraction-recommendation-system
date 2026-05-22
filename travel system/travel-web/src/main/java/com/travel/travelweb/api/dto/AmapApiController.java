package com.travel.travelweb.api.dto;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.travel.travelweb.api.ApiResponse;
import com.travel.travelweb.service.AmapService;

/**
 * 高德地图 API（dto 包，供安卓端与 Web 共用 AmapService）
 */
@RestController
@RequestMapping("/api/amap")
public class AmapApiController {

    private static final String STATIC_MAP_KEY = "e5ca958a2702e97a1fe428b536c98fdb";

    private final AmapService amapService;

    public AmapApiController(AmapService amapService) {
        this.amapService = amapService;
    }

    /** 与 web.AmapController 相同逻辑，额外提供 ApiResponse 格式备用 */
    @GetMapping("/geocode-api")
    public ResponseEntity<ApiResponse<GeocodeResponse>> geocodeApi(@RequestParam String address) {
        Map<String, Double> coords = amapService.getCoordinates(address);
        Double lat = coords.get("latitude");
        Double lng = coords.get("longitude");
        if (lat != null && lng != null) {
            return ResponseEntity.ok(ApiResponse.success(new GeocodeResponse(lat, lng, address)));
        }
        return ResponseEntity.ok(ApiResponse.error("无法获取坐标，请检查地址是否正确"));
    }

    @GetMapping("/map-config")
    public ResponseEntity<ApiResponse<AmapMapConfigResponse>> mapConfig() {
        return ResponseEntity.ok(ApiResponse.success(new AmapMapConfigResponse()));
    }

    @GetMapping("/static-map-url")
    public ResponseEntity<ApiResponse<String>> staticMapUrl(
            @RequestParam double latitude,
            @RequestParam double longitude) {
        String url = String.format(
                "https://restapi.amap.com/v3/staticmap?location=%s,%s&zoom=15&size=750*300&scale=2"
                        + "&markers=mid,,A:%s,%s&key=%s",
                longitude, latitude, longitude, latitude, STATIC_MAP_KEY);
        return ResponseEntity.ok(ApiResponse.success(url));
    }

}
