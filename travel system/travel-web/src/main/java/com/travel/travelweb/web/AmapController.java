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
        
        Map<String, Double> coords = amapService.getCoordinates(address);
        
        if (coords.containsKey("latitude") && coords.containsKey("longitude")) {
            response.put("success", true);
            response.put("latitude", coords.get("latitude"));
            response.put("longitude", coords.get("longitude"));
        } else {
            response.put("success", false);
            response.put("message", "无法获取坐标，请检查地址是否正确");
        }
        
        return response;
    }
}