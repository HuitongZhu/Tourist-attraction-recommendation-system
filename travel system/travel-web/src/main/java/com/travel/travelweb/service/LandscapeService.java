package com.travel.travelweb.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.travel.travelweb.entity.Landscape;
import com.travel.travelweb.repo.LandCollectRepository;
import com.travel.travelweb.repo.LandCommentRepository;
import com.travel.travelweb.repo.LandLikeRepository;
import com.travel.travelweb.repo.LandscapeRepository;
import com.travel.travelweb.repo.PostCollectRepository;
import com.travel.travelweb.repo.PostCommentRepository;
import com.travel.travelweb.repo.PostLikeRepository;
import com.travel.travelweb.repo.RecommendationPostRepository;
import com.travel.travelweb.util.IdGenerator;

@Service
public class LandscapeService {

    public static final String AUDIT_APPROVED = "审核通过";
    public static final String AUDIT_PENDING = "待审核";
    public static final String AUDIT_REJECTED = "审核未通过";

    private final LandscapeRepository landscapeRepository;
    private final LandLikeRepository landLikeRepository;
    private final LandCommentRepository landCommentRepository;
    private final LandCollectRepository landCollectRepository;
    private final RecommendationPostRepository postRepository;
    private final PostCommentRepository postCommentRepository;
    private final PostLikeRepository postLikeRepository;
    private final PostCollectRepository postCollectRepository;

    public LandscapeService(LandscapeRepository landscapeRepository, LandLikeRepository landLikeRepository,
                           LandCommentRepository landCommentRepository,
                           LandCollectRepository landCollectRepository,
                           RecommendationPostRepository postRepository,
                           PostCommentRepository postCommentRepository,
                           PostLikeRepository postLikeRepository,
                           PostCollectRepository postCollectRepository) {
        this.landscapeRepository = landscapeRepository;
        this.landLikeRepository = landLikeRepository;
        this.landCommentRepository = landCommentRepository;
        this.landCollectRepository = landCollectRepository;
        this.postRepository = postRepository;
        this.postCommentRepository = postCommentRepository;
        this.postLikeRepository = postLikeRepository;
        this.postCollectRepository = postCollectRepository;
    }

    public List<Landscape> homeLandscapes(int limit) {
        return landscapeRepository.findByAuditStateOrderByPublishTimeDesc(
                AUDIT_APPROVED, PageRequest.of(0, limit));
    }

    public List<Landscape> listApproved() {
        return landscapeRepository.findByAuditStateOrderByPublishTimeDesc(
                AUDIT_APPROVED, PageRequest.of(0, Integer.MAX_VALUE));
    }

    public Optional<Landscape> findApproved(String id) {
        return landscapeRepository.findById(id).filter(l -> AUDIT_APPROVED.equals(l.getAuditState()));
    }

    public List<Landscape> search(String keyword, String city, String level, String sort) {
        String kw = keyword != null ? keyword.trim() : "";
        String c = city != null ? city.trim() : "all";
        String lv = level != null ? level.trim() : "all";
        List<Landscape> list = landscapeRepository.searchApproved(AUDIT_APPROVED,
                kw.isEmpty() ? null : kw,
                c.isEmpty() ? "all" : c,
                lv.isEmpty() ? "all" : lv);
        if (!"hot".equals(sort)) {
            if ("level".equals(sort)) {
                list = new ArrayList<>(list);
                list.sort(Comparator.comparing(Landscape::getLevel, Comparator.nullsLast(String::compareTo)).reversed());
            }
            return list;
        }
        List<String> ids = list.stream().map(Landscape::getLandscapeId).toList();
        Map<String, Long> counts = likeCounts(ids);
        List<Landscape> sorted = new ArrayList<>(list);
        sorted.sort(Comparator
                .comparing((Landscape l) -> counts.getOrDefault(l.getLandscapeId(), 0L)).reversed()
                .thenComparing(Landscape::getPublishTime, Comparator.nullsLast(Comparator.naturalOrder())).reversed());
        return sorted;
    }

    public Map<String, Long> likeCounts(List<String> ids) {
        if (ids.isEmpty()) {
            return Map.of();
        }
        Map<String, Long> map = new HashMap<>();
        for (Object[] row : landLikeRepository.countByLandscapeIdIn(ids)) {
            map.put((String) row[0], ((Number) row[1]).longValue());
        }
        return map;
    }

    public long likeCount(String landscapeId) {
        return landLikeRepository.countByLandscapeId(landscapeId);
    }

    public List<Landscape> related(String address, String excludeId, int limit) {
        if (address == null || address.isBlank()) {
            return List.of();
        }
        String cityHint = address.length() >= 2 ? address.substring(0, 2) : address;
        return search(null, cityHint, "all", "hot").stream()
                .filter(l -> !l.getLandscapeId().equals(excludeId))
                .limit(limit)
                .collect(Collectors.toList());
    }

    private static final String UPLOAD_DIR = "uploads/landscapes/";

    private String saveImage(MultipartFile image) throws IOException {
        if (image == null || image.isEmpty()) {
            return null;
        }
        Path uploadPath = Paths.get(UPLOAD_DIR);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }
        String originalFilename = image.getOriginalFilename();
        String extension = originalFilename != null && originalFilename.contains(".") 
            ? originalFilename.substring(originalFilename.lastIndexOf(".")) 
            : ".png";
        String newFilename = UUID.randomUUID().toString() + extension;
        Path filePath = uploadPath.resolve(newFilename);
        Files.copy(image.getInputStream(), filePath);
        return "/" + UPLOAD_DIR + newFilename;
    }

    @Transactional
    public String createLandscape(String userId, String title, String content, String address,
                                  Double latitude, Double longitude, String tel, String openingTime, String level,
                                  MultipartFile image) throws IOException {
        // 验证标题
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("景点标题不能为空");
        }
        
        // 检查用户是否已发布过同名景点（包括审核中和已通过）
        int userTitleCount = landscapeRepository.countByTitleAndUserId(title.trim(), userId);
        if (userTitleCount > 0) {
            throw new IllegalArgumentException("您已发布过同名景点");
        }
        
        // 检查系统中是否已存在同名景点（防止重复）
        int totalTitleCount = landscapeRepository.countByTitle(title.trim());
        if (totalTitleCount > 0) {
            throw new IllegalArgumentException("该景点已存在");
        }
        
        // 检查用户是否已发布过相同地址的景点
        if (address != null && !address.isBlank()) {
            int userAddressCount = landscapeRepository.countByAddressContainingAndUserId(address.trim(), userId);
            if (userAddressCount > 0) {
                throw new IllegalArgumentException("您已发布过该地址的景点");
            }
            
            // 检查系统中是否已存在相同地址的景点
            int totalAddressCount = landscapeRepository.countByAddressContaining(address.trim());
            if (totalAddressCount > 0) {
                throw new IllegalArgumentException("该地址的景点信息已存在");
            }
        }
        
        // 验证内容长度，避免超过数据库字段限制
        String trimmedContent = content;
        if (content != null && content.length() > 60000) {
            trimmedContent = content.substring(0, 60000);
        }
        
        Landscape l = new Landscape();
        l.setLandscapeId(IdGenerator.next("LS"));
        l.setUserId(userId);
        l.setTitle(title.trim());
        l.setContent(trimmedContent);
        l.setAddress(address);
        l.setLatitude(latitude);
        l.setLongitude(longitude);
        l.setLandscapeTel(tel);
        l.setOpeningTime(openingTime);
        l.setLevel(level);
        l.setImagePath(saveImage(image));
        l.setAuditState(AUDIT_PENDING);
        l.setPublishTime(LocalDateTime.now());
        landscapeRepository.save(l);
        return l.getLandscapeId();
    }

    public List<Landscape> findAllForAdmin(String auditState, String keyword) {
        return landscapeRepository.searchForAdmin(auditState, keyword);
    }

    public Optional<Landscape> findById(String id) {
        return landscapeRepository.findById(id);
    }

    @Transactional
    public boolean updateAuditState(String id, String auditState) {
        return landscapeRepository.findById(id).map(l -> {
            l.setAuditState(auditState);
            l.setAuditTime(LocalDateTime.now());
            landscapeRepository.save(l);
            return true;
        }).orElse(false);
    }

    private void deleteImage(String imagePath) {
        if (imagePath != null && !imagePath.isBlank()) {
            try {
                Path filePath = Paths.get(imagePath);
                Files.deleteIfExists(filePath);
            } catch (IOException e) {
                // 忽略删除错误
            }
        }
    }

    @Transactional
    public void updateLandscape(String landscapeId, String userId, String title, String content, 
                                String address, Double latitude, Double longitude, String tel, String openingTime, String level,
                                MultipartFile image) throws IOException {
        Landscape l = landscapeRepository.findById(landscapeId)
                .orElseThrow(() -> new IllegalArgumentException("景点不存在"));
        if (!userId.equals(l.getUserId())) {
            throw new IllegalArgumentException("无权修改该景点");
        }
        if (title != null && !title.isBlank()) {
            l.setTitle(title.trim());
        }
        if (content != null && !content.isBlank()) {
            // 验证内容长度
            String trimmedContent = content.length() > 60000 ? content.substring(0, 60000) : content;
            l.setContent(trimmedContent.trim());
        }
        if (address != null && !address.isBlank()) {
            l.setAddress(address.trim());
        }
        if (latitude != null) {
            l.setLatitude(latitude);
        }
        if (longitude != null) {
            l.setLongitude(longitude);
        }
        if (tel != null) {
            l.setLandscapeTel(tel.trim());
        }
        if (openingTime != null) {
            l.setOpeningTime(openingTime.trim());
        }
        if (level != null && !level.isBlank()) {
            l.setLevel(level);
        }
        if (image != null && !image.isEmpty()) {
            deleteImage(l.getImagePath());
            l.setImagePath(saveImage(image));
        }
        l.setAuditState(AUDIT_PENDING);
        landscapeRepository.save(l);
    }

    @Transactional
    public void deleteLandscape(String landscapeId, String userId) {
        Landscape l = landscapeRepository.findById(landscapeId)
                .orElseThrow(() -> new IllegalArgumentException("景点不存在"));
        if (!userId.equals(l.getUserId())) {
            throw new IllegalArgumentException("无权删除该景点");
        }
        // 删除图片文件
        deleteImage(l.getImagePath());
        // 删除关联的推荐帖及其评论、点赞、收藏
        postRepository.deleteByLandscapeId(landscapeId);
        // 删除景点的收藏、评论和点赞
        landCollectRepository.deleteByLandscapeId(landscapeId);
        landLikeRepository.deleteByLandscapeId(landscapeId);
        landCommentRepository.deleteByLandscapeId(landscapeId);
        // 删除景点
        landscapeRepository.delete(l);
    }

    @Transactional
    public boolean deleteLandscapeByAdmin(String landscapeId) {
        return landscapeRepository.findById(landscapeId).map(l -> {
            // 删除图片文件
            deleteImage(l.getImagePath());
            // 先删除关联的推荐帖及其评论、点赞、收藏
            postRepository.deleteByLandscapeId(landscapeId);
            // 删除景点的收藏、评论和点赞
            landCollectRepository.deleteByLandscapeId(landscapeId);
            landLikeRepository.deleteByLandscapeId(landscapeId);
            landCommentRepository.deleteByLandscapeId(landscapeId);
            // 删除景点
            landscapeRepository.delete(l);
            return true;
        }).orElse(false);
    }
}
