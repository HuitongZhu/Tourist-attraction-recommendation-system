package com.travel.travelweb.web;

import java.util.HashMap;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.travel.travelweb.service.AmapService;

@RestController
public class AmapController {

    private final AmapService amapService;

    public AmapController(AmapService amapService) {
        this.amapService = amapService;
    }

    @GetMapping("/api/amap/geocode")
    public Map<String, Object> geocode(@RequestParam String address) {
        Map<String, Object> response = new HashMap<>();
        try {
            Map<String, Double> coords = amapService.getCoordinates(address);
            Double lat = coords.get("latitude");
            Double lng = coords.get("longitude");
            if (lat != null && lng != null) {
                response.put("success", true);
                response.put("latitude", lat);
                response.put("longitude", lng);
            } else {
                response.put("success", false);
                response.put("message", "无法获取坐标，请检查地址是否正确");
            }
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "地理编码服务异常: " + e.getMessage());
        }
        return response;
    }
}