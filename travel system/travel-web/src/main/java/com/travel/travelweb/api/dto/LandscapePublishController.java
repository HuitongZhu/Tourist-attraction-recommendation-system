package com.travel.travelweb.api.dto;

import java.io.IOException;
import java.util.Map;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.travel.travelweb.api.ApiResponse;
import com.travel.travelweb.entity.Landscape;
import com.travel.travelweb.service.AmapService;
import com.travel.travelweb.service.LandscapeService;

/**
 * 安卓发布景点（multipart），服务端自动补全经纬度
 */
@RestController
@RequestMapping("/api/app")
public class LandscapePublishController {

    private final LandscapeService landscapeService;
    private final AmapService amapService;

    public LandscapePublishController(LandscapeService landscapeService, AmapService amapService) {
        this.landscapeService = landscapeService;
        this.amapService = amapService;
    }

    @PostMapping(value = "/landscapes/publish", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<LandscapeBackendResponse>> publishLandscape(
            @RequestHeader(value = "X-User-Id", required = false) String userId,
            @RequestParam String title,
            @RequestParam String address,
            @RequestParam(required = false) String content,
            @RequestParam(required = false) Double latitude,
            @RequestParam(required = false) Double longitude,
            @RequestParam(required = false) String tel,
            @RequestParam(required = false) String openingTime,
            @RequestParam(required = false) String level,
            @RequestPart(value = "image", required = false) MultipartFile image) {
        if (userId == null || userId.isBlank()) {
            return ResponseEntity.status(401).body(ApiResponse.error(401, "用户未登录"));
        }
        if (title == null || title.isBlank()) {
            return ResponseEntity.badRequest().body(ApiResponse.error("景点名称不能为空"));
        }
        if (address == null || address.isBlank()) {
            return ResponseEntity.badRequest().body(ApiResponse.error("景点地点不能为空"));
        }

        Double lat = latitude;
        Double lng = longitude;
        if (lat == null || lng == null || lat == 0.0 || lng == 0.0) {
            Map<String, Double> coords = amapService.getCoordinates(address.trim());
            lat = coords.get("latitude");
            lng = coords.get("longitude");
        }
        if (lat == null || lng == null) {
            return ResponseEntity.ok(ApiResponse.error("无法解析地址坐标，请填写更详细的地点或稍后重试"));
        }

        try {
            String id = landscapeService.createLandscape(
                    userId,
                    title.trim(),
                    content != null ? content.trim() : "",
                    address.trim(),
                    lat,
                    lng,
                    tel,
                    openingTime,
                    level,
                    image);
            return landscapeService.findById(id)
                    .map(l -> ResponseEntity.ok(ApiResponse.success(toResponse(l))))
                    .orElse(ResponseEntity.ok(ApiResponse.error("创建成功但查询失败")));
        } catch (IOException e) {
            return ResponseEntity.status(500).body(ApiResponse.error(500, "发布失败"));
        }
    }

    private LandscapeBackendResponse toResponse(Landscape l) {
        LandscapeBackendResponse r = new LandscapeBackendResponse();
        r.setLandscapeId(l.getLandscapeId());
        r.setUserId(l.getUserId());
        r.setTitle(l.getTitle());
        r.setContent(l.getContent());
        r.setAddress(l.getAddress());
        r.setLatitude(l.getLatitude());
        r.setLongitude(l.getLongitude());
        r.setTel(l.getLandscapeTel());
        r.setOpeningTime(l.getOpeningTime());
        r.setLevel(l.getLevel());
        r.setImagePath(l.getImagePath());
        r.setAuditState(l.getAuditState());
        r.setPublishTime(l.getPublishTime());
        r.setAuditTime(l.getAuditTime());
        return r;
    }
}
