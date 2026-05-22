package com.travel.travelweb.service;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class AmapService {

    private static final Logger logger = LoggerFactory.getLogger(AmapService.class);
    
    private static final String API_KEY = "e5ca958a2702e97a1fe428b536c98fdb";
    private static final String GEOCODE_URL = "https://restapi.amap.com/v3/geocode/geo";

    private final RestTemplate restTemplate;

    public AmapService() {
        this.restTemplate = new RestTemplate();
    }

    public Map<String, Double> getCoordinates(String address) {
        Map<String, Double> result = new HashMap<>();
        
        try {
            String url = GEOCODE_URL + "?address=" + java.net.URLEncoder.encode(address, "UTF-8") 
                      + "&key=" + API_KEY + "&output=JSON";
            
            logger.info("调用高德地理编码API: {}", url);
            
            String response = restTemplate.getForObject(url, String.class);
            
            logger.info("高德API响应: {}", response);
            
            if (response != null) {
                if (response.contains("\"status\":\"1\"")) {
                    int locationStart = response.indexOf("\"location\":\"") + 12;
                    int locationEnd = response.indexOf("\"", locationStart);
                    
                    if (locationStart > 0 && locationEnd > locationStart) {
                        String location = response.substring(locationStart, locationEnd);
                        String[] coords = location.split(",");
                        
                        if (coords.length == 2) {
                            result.put("longitude", Double.parseDouble(coords[0].trim()));
                            result.put("latitude", Double.parseDouble(coords[1].trim()));
                            logger.info("成功获取坐标: 经度={}, 纬度={}", result.get("longitude"), result.get("latitude"));
                        } else {
                            logger.error("坐标格式错误: {}", location);
                        }
                    } else {
                        logger.error("无法解析location字段");
                    }
                } else {
                    int infoStart = response.indexOf("\"info\":\"") + 7;
                    int infoEnd = response.indexOf("\"", infoStart);
                    String info = infoStart > 0 && infoEnd > infoStart ? response.substring(infoStart, infoEnd) : "未知错误";
                    
                    int infocodeStart = response.indexOf("\"infocode\":\"") + 12;
                    int infocodeEnd = response.indexOf("\"", infocodeStart);
                    String infocode = infocodeStart > 0 && infocodeEnd > infocodeStart ? response.substring(infocodeStart, infocodeEnd) : "未知";
                    
                    logger.error("高德API返回错误 - info: {}, infocode: {}", info, infocode);
                }
            } else {
                logger.error("高德API响应为空");
            }
        } catch (Exception e) {
            logger.error("调用高德API失败", e);
        }
        
        return result;
    }
}