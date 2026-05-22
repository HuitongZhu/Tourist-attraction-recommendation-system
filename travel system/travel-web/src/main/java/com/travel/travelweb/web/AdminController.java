package com.travel.travelweb.web;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.travel.travelweb.config.LoginInterceptor;
import com.travel.travelweb.entity.LandComment;
import com.travel.travelweb.entity.Landscape;
import com.travel.travelweb.entity.OrdinaryUser;
import com.travel.travelweb.entity.PostComment;
import com.travel.travelweb.entity.RecommendationPost;
import com.travel.travelweb.entity.SysUser;
import com.travel.travelweb.repo.LandCommentRepository;
import com.travel.travelweb.repo.LandscapeRepository;
import com.travel.travelweb.repo.OrdinaryUserRepository;
import com.travel.travelweb.repo.PostCommentRepository;
import com.travel.travelweb.repo.RecommendationPostRepository;
import com.travel.travelweb.repo.SysUserRepository;
import com.travel.travelweb.service.LandCommentService;
import com.travel.travelweb.service.LandscapeService;
import com.travel.travelweb.service.PostService;
import com.travel.travelweb.service.RecommendationPostService;
import com.travel.travelweb.service.UserService;
import com.travel.travelweb.web.dto.CommentView;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final LandscapeService landscapeService;
    private final UserService userService;
    private final RecommendationPostRepository postRepository;
    private final OrdinaryUserRepository ordinaryUserRepository;
    private final RecommendationPostService recommendationPostService;
    private final LandCommentService landCommentService;
    private final PostService postService;
    private final LandCommentRepository landCommentRepository;
    private final PostCommentRepository postCommentRepository;
    private final LandscapeRepository landscapeRepository;
    private final SysUserRepository sysUserRepository;

    public AdminController(LandscapeService landscapeService, UserService userService, 
                          RecommendationPostRepository postRepository,
                          OrdinaryUserRepository ordinaryUserRepository,
                          RecommendationPostService recommendationPostService,
                          LandCommentService landCommentService,
                          PostService postService,
                          LandCommentRepository landCommentRepository,
                          PostCommentRepository postCommentRepository,
                          LandscapeRepository landscapeRepository,
                          SysUserRepository sysUserRepository) {
        this.landscapeService = landscapeService;
        this.userService = userService;
        this.postRepository = postRepository;
        this.ordinaryUserRepository = ordinaryUserRepository;
        this.recommendationPostService = recommendationPostService;
        this.landCommentService = landCommentService;
        this.postService = postService;
        this.landCommentRepository = landCommentRepository;
        this.postCommentRepository = postCommentRepository;
        this.landscapeRepository = landscapeRepository;
        this.sysUserRepository = sysUserRepository;
    }

    @GetMapping("")
    public String dashboard(HttpSession session, Model model,
                           @RequestParam(required = false) String userKeyword) {
        String userType = (String) session.getAttribute(LoginInterceptor.SESSION_USER_TYPE);
        if (!"1".equals(userType)) {
            return "redirect:/";
        }
        String userName = (String) session.getAttribute(LoginInterceptor.SESSION_USER_NAME);
        model.addAttribute("userName", userName);
        
        String userId = (String) session.getAttribute(LoginInterceptor.SESSION_USER_ID);
        userService.findById(userId).ifPresent(adminUser -> {
            model.addAttribute("adminUser", adminUser);
        });
        
        List<SysUser> users = userService.findOrdinaryUsers(userKeyword);
        model.addAttribute("users", users);
        model.addAttribute("userKeyword", userKeyword);
        
        List<Landscape> landscapes = landscapeService.findAllForAdmin(null, null);
        model.addAttribute("landscapes", landscapes);
        
        List<RecommendationPost> posts = postRepository.searchPosts(null, null);
        for (RecommendationPost post : posts) {
            String state = post.getAuditState();
            if (state == null || state.isEmpty() || !state.equals("审核通过") && !state.equals("审核未通过")) {
                post.setAuditState("待审核");
            }
        }
        model.addAttribute("posts", posts);
        
        return "admin-dashboard";
    }

    @PostMapping("/users/delete")
    @ResponseBody
    public String deleteUser(@RequestParam String id) {
        boolean success = userService.deleteUser(id);
        return success ? "success" : "fail";
    }

    @GetMapping("/users/detail")
    @ResponseBody
    public OrdinaryUser getUserDetail(@RequestParam String id) {
        return ordinaryUserRepository.findById(id).orElse(new OrdinaryUser());
    }

    @PostMapping("/users/update")
    @ResponseBody
    public String updateUser(@RequestParam String id, 
                             @RequestParam String userName,
                             @RequestParam(required = false) String realName,
                             @RequestParam(required = false) String phoneNumber,
                             @RequestParam(required = false) String idNumber,
                             @RequestParam(required = false) String gender,
                             @RequestParam(required = false) String birthday) {
        userService.updateUserName(id, userName);
        
        ordinaryUserRepository.findById(id).ifPresent(user -> {
            if (realName != null) user.setRealName(realName);
            if (phoneNumber != null) user.setPhoneNumber(phoneNumber);
            if (idNumber != null) user.setIdNumber(idNumber);
            if (gender != null) user.setGender(gender);
            if (birthday != null) user.setBirthday(birthday);
            ordinaryUserRepository.save(user);
        });
        
        return "success";
    }

    @GetMapping("/landscapes")
    public String landscapes(HttpSession session, Model model,
                            @RequestParam(required = false) String keyword,
                            @RequestParam(required = false) String auditState) {
        String userType = (String) session.getAttribute(LoginInterceptor.SESSION_USER_TYPE);
        if (!"1".equals(userType)) {
            return "redirect:/";
        }
        String userName = (String) session.getAttribute(LoginInterceptor.SESSION_USER_NAME);
        model.addAttribute("userName", userName);
        
        List<Landscape> landscapes = landscapeService.findAllForAdmin(auditState, keyword);
        model.addAttribute("landscapes", landscapes);
        model.addAttribute("keyword", keyword);
        model.addAttribute("auditState", auditState);
        
        return "admin-landscapes";
    }

    @PostMapping("/landscapes/audit")
    @ResponseBody
    public String auditLandscape(@RequestParam String id, @RequestParam String action) {
        String auditState = "pass".equals(action) ? LandscapeService.AUDIT_APPROVED : LandscapeService.AUDIT_REJECTED;
        boolean success = landscapeService.updateAuditState(id, auditState);
        return success ? "success" : "fail";
    }

    @PostMapping("/posts/audit")
    @ResponseBody
    public String auditPost(@RequestParam String id, @RequestParam String action) {
        return postRepository.findById(id).map(post -> {
            post.setAuditState("pass".equals(action) ? "审核通过" : "审核未通过");
            postRepository.save(post);
            return "success";
        }).orElse("fail");
    }

    @PostMapping("/landscapes/delete")
    @ResponseBody
    public String deleteLandscape(@RequestParam String id) {
        boolean success = landscapeService.deleteLandscapeByAdmin(id);
        return success ? "success" : "fail";
    }

    @PostMapping("/posts/delete")
    @ResponseBody
    public String deletePost(@RequestParam String id) {
        boolean success = recommendationPostService.deletePost(id);
        return success ? "success" : "fail";
    }

    @PostMapping("/landcomments/delete")
    @ResponseBody
    public String deleteLandComment(@RequestParam String id) {
        boolean success = landCommentService.deleteByAdmin(id);
        return success ? "success" : "fail";
    }

    @PostMapping("/postcomments/delete")
    @ResponseBody
    public String deletePostComment(@RequestParam String id) {
        boolean success = postService.deleteCommentByAdmin(id);
        return success ? "success" : "fail";
    }

    @GetMapping("/landscapes/search")
    @ResponseBody
    public String searchLandscapes(@RequestParam(required = false) String keyword,
                                   @RequestParam(required = false) String auditState) {
        List<Landscape> landscapes = landscapeService.findAllForAdmin(auditState, keyword);
        
        StringBuilder html = new StringBuilder();
        if (landscapes.isEmpty()) {
            html.append("<div class=\"empty-state\">");
            html.append("<i class=\"layui-icon layui-icon-empty\" style=\"font-size: 48px; display: block; margin-bottom: 15px;\"></i>");
            html.append("<p>暂无景点信息</p>");
            html.append("</div>");
        } else {
            for (Landscape landscape : landscapes) {
                String state = landscape.getAuditState();
                boolean isPending = state == null || state.isEmpty() || state.equals("待审核");
                
                html.append("<div class=\"landscape-card\">");
                html.append("<div class=\"landscape-header\"><div>");
                html.append("<span class=\"landscape-title\">").append(escapeHtml(landscape.getTitle())).append("</span>");
                
                if (isPending) {
                    html.append("<span class=\"audit-tag pending\">待审核</span>");
                } else if ("审核通过".equals(state)) {
                    html.append("<span class=\"audit-tag approved\">审核通过</span>");
                } else if ("审核未通过".equals(state)) {
                    html.append("<span class=\"audit-tag rejected\">审核未通过</span>");
                }
                
                html.append("</div></div>");
                html.append("<div class=\"landscape-meta\">");
                html.append("<span>地址：").append(escapeHtml(landscape.getAddress())).append("</span>");
                html.append("<span>提交时间：").append(landscape.getPublishTime() != null ? landscape.getPublishTime().toString() : "").append("</span>");
                if (landscape.getLevel() != null) {
                    html.append("<span>等级：").append(escapeHtml(landscape.getLevel())).append("</span>");
                }
                html.append("</div>");
                html.append("<div class=\"landscape-content\">").append(escapeHtml(landscape.getContent())).append("</div>");
                html.append("<div class=\"landscape-actions\">");
                html.append("<button class=\"action-btn view-btn\" onclick=\"viewLandscapeDetail('").append(landscape.getLandscapeId()).append("')\">查看详情</button>");
                
                if (isPending) {
                    html.append("<button class=\"action-btn pass-btn\" onclick=\"auditLandscape('").append(landscape.getLandscapeId()).append("', 'pass')\">通过</button>");
                    html.append("<button class=\"action-btn reject-btn\" onclick=\"auditLandscape('").append(landscape.getLandscapeId()).append("', 'reject')\">拒绝</button>");
                }
                
                html.append("<button class=\"action-btn delete-btn\" onclick=\"deleteLandscape('").append(landscape.getLandscapeId()).append("')\">删除</button>");
                html.append("</div></div>");
            }
        }
        
        return html.toString();
    }

    @GetMapping("/posts/search")
    @ResponseBody
    public String searchPosts(@RequestParam(required = false) String keyword,
                              @RequestParam(required = false) String auditState) {
        List<RecommendationPost> posts = postRepository.searchPosts(auditState, keyword);
        for (RecommendationPost post : posts) {
            String state = post.getAuditState();
            if (state == null || state.isEmpty() || !state.equals("审核通过") && !state.equals("审核未通过")) {
                post.setAuditState("待审核");
            }
        }
        
        StringBuilder html = new StringBuilder();
        if (posts.isEmpty()) {
            html.append("<div class=\"empty-state\">");
            html.append("<i class=\"layui-icon layui-icon-edit\" style=\"font-size: 48px; display: block; margin-bottom: 15px;\"></i>");
            html.append("<p>暂无推荐帖信息</p>");
            html.append("</div>");
        } else {
            for (RecommendationPost post : posts) {
                String state = post.getAuditState();
                boolean isPending = state == null || state.isEmpty() || state.equals("待审核");
                
                html.append("<div class=\"landscape-card\">");
                html.append("<div class=\"landscape-header\"><div>");
                html.append("<span class=\"landscape-title\">").append(escapeHtml(post.getTitle())).append("</span>");
                
                if (isPending) {
                    html.append("<span class=\"audit-tag pending\">待审核</span>");
                } else if ("审核通过".equals(state)) {
                    html.append("<span class=\"audit-tag approved\">审核通过</span>");
                } else if ("审核未通过".equals(state)) {
                    html.append("<span class=\"audit-tag rejected\">审核未通过</span>");
                }
                
                html.append("</div></div>");
                html.append("<div class=\"landscape-meta\">");
                html.append("<span>发表用户：").append(escapeHtml(post.getUserId())).append("</span>");
                html.append("<span>发表时间：").append(post.getPublishTime() != null ? post.getPublishTime().toString() : "").append("</span>");
                html.append("</div>");
                html.append("<div class=\"landscape-content\">").append(escapeHtml(post.getContent())).append("</div>");
                html.append("<div class=\"landscape-actions\">");
                html.append("<button class=\"action-btn view-btn\" onclick=\"viewPostDetail('").append(post.getRecomId()).append("')\">查看详情</button>");
                
                if (isPending) {
                    html.append("<button class=\"action-btn pass-btn\" onclick=\"auditPost('").append(post.getRecomId()).append("', 'pass')\">通过</button>");
                    html.append("<button class=\"action-btn reject-btn\" onclick=\"auditPost('").append(post.getRecomId()).append("', 'reject')\">拒绝</button>");
                }
                
                html.append("<button class=\"action-btn delete-btn\" onclick=\"deletePost('").append(post.getRecomId()).append("')\">删除</button>");
                html.append("</div></div>");
            }
        }
        
        return html.toString();
    }

    private String escapeHtml(String text) {
        if (text == null) return "";
        return text.replace("&", "&amp;")
                   .replace("<", "&lt;")
                   .replace(">", "&gt;")
                   .replace("\"", "&quot;")
                   .replace("'", "&#39;");
    }

    @GetMapping("/comments/search")
    @ResponseBody
    public String searchComments(@RequestParam(required = false) String keyword) {
        StringBuilder html = new StringBuilder();
        
        try {
            List<PostComment> postComments = postCommentRepository.findAll();
            List<LandComment> landComments = landCommentRepository.findAll();
            
            Map<String, String> postTitleMap = new HashMap<>();
            for (RecommendationPost p : postRepository.findAll()) {
                postTitleMap.put(p.getRecomId(), p.getTitle());
            }
            
            Map<String, String> landscapeTitleMap = new HashMap<>();
            for (Landscape l : landscapeRepository.findAll()) {
                landscapeTitleMap.put(l.getLandscapeId(), l.getTitle());
            }
            
            Map<String, String> userNameMap = new HashMap<>();
            for (SysUser u : sysUserRepository.findAll()) {
                userNameMap.put(u.getUserId(), u.getUserName() != null ? u.getUserName() : u.getUserId());
            }
            
            List<CommentView> allComments = new ArrayList<>();
            
            for (PostComment c : postComments) {
                if (c.getCommentId() == null || c.getContent() == null) continue;
                
                boolean matchesKeyword = true;
                if (keyword != null && !keyword.trim().isEmpty()) {
                    String lowerKeyword = keyword.toLowerCase().trim();
                    String content = c.getContent() != null ? c.getContent().toLowerCase() : "";
                    String userId = c.getUserId();
                    String userNameVal = userId != null ? userNameMap.getOrDefault(userId, "") : "";
                    String userName = userNameVal != null ? userNameVal.toLowerCase() : "";
                    String recomId = c.getRecomId();
                    String refTitleVal = recomId != null ? postTitleMap.getOrDefault(recomId, "") : "";
                    String refTitle = refTitleVal != null ? refTitleVal.toLowerCase() : "";
                    matchesKeyword = content.contains(lowerKeyword) || 
                                   userName.contains(lowerKeyword) || 
                                   refTitle.contains(lowerKeyword);
                }
                
                if (matchesKeyword) {
                    String userId = c.getUserId();
                    allComments.add(new CommentView(
                            c.getCommentId(),
                            userId,
                            userNameMap.getOrDefault(userId, userId != null ? userId : "未知用户"),
                            c.getContent(),
                            c.getPublishTime(),
                            "post",
                            c.getRecomId(),
                            postTitleMap.getOrDefault(c.getRecomId(), "未知推荐帖")
                    ));
                }
            }
            
            for (LandComment c : landComments) {
                if (c.getCommentId() == null || c.getContent() == null) continue;
                
                boolean matchesKeyword = true;
                if (keyword != null && !keyword.trim().isEmpty()) {
                    String lowerKeyword = keyword.toLowerCase().trim();
                    String content = c.getContent() != null ? c.getContent().toLowerCase() : "";
                    String userId = c.getUserId();
                    String userNameVal = userId != null ? userNameMap.getOrDefault(userId, "") : "";
                    String userName = userNameVal != null ? userNameVal.toLowerCase() : "";
                    String landscapeId = c.getLandscapeId();
                    String refTitleVal = landscapeId != null ? landscapeTitleMap.getOrDefault(landscapeId, "") : "";
                    String refTitle = refTitleVal != null ? refTitleVal.toLowerCase() : "";
                    matchesKeyword = content.contains(lowerKeyword) || 
                                   userName.contains(lowerKeyword) || 
                                   refTitle.contains(lowerKeyword);
                }
                
                if (matchesKeyword) {
                    String userId = c.getUserId();
                    allComments.add(new CommentView(
                            c.getCommentId(),
                            userId,
                            userNameMap.getOrDefault(userId, userId != null ? userId : "未知用户"),
                            c.getContent(),
                            c.getPublishTime(),
                            "landscape",
                            c.getLandscapeId(),
                            landscapeTitleMap.getOrDefault(c.getLandscapeId(), "未知景点")
                    ));
                }
            }
            
            allComments.sort((a, b) -> {
                if (a.getPublishTime() == null && b.getPublishTime() == null) return 0;
                if (a.getPublishTime() == null) return 1;
                if (b.getPublishTime() == null) return -1;
                return b.getPublishTime().compareTo(a.getPublishTime());
            });
            
            if (allComments.isEmpty()) {
                html.append("<div class=\"empty-state\">");
                html.append("<i class=\"layui-icon layui-icon-chat\" style=\"font-size: 48px; display: block; margin-bottom: 15px;\"></i>");
                html.append("<p>暂无评论信息</p>");
                html.append("</div>");
            } else {
                for (CommentView comment : allComments) {
                    String refTypeName = "landscape".equals(comment.getRefType()) ? "景点" : "推荐帖";
                    String content = comment.getContent() != null ? escapeHtml(comment.getContent()) : "";
                    String refTitle = comment.getRefTitle() != null ? escapeHtml(comment.getRefTitle()) : "";
                    String userName = comment.getUserName() != null ? escapeHtml(comment.getUserName()) : "";
                    String timeStr = comment.getPublishTime() != null ? 
                        java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").format(comment.getPublishTime()) : "";
                    
                    html.append("<div class=\"landscape-card\" id=\"comment-card-").append(comment.getCommentId()).append("\">");
                    html.append("<div class=\"landscape-header\"><div>");
                    html.append("<span class=\"landscape-title\">").append(content).append("</span>");
                    html.append("</div></div>");
                    html.append("<div class=\"landscape-meta\">");
                    html.append("<span>").append(refTypeName).append("：").append(refTitle).append("</span>");
                    html.append("<span>发表用户：").append(userName).append("</span>");
                    html.append("<span>发表时间：").append(timeStr).append("</span>");
                    html.append("</div>");
                    html.append("<div class=\"landscape-actions\">");
                    html.append("<button class=\"action-btn delete-btn\" onclick=\"deleteComment('").append(comment.getCommentId()).append("', '").append(comment.getRefType()).append("')\">删除</button>");
                    html.append("</div></div>");
                }
            }
        } catch (Exception e) {
            html.append("<div class=\"empty-state\">");
            html.append("<i class=\"layui-icon layui-icon-chat\" style=\"font-size: 48px; display: block; margin-bottom: 15px;\"></i>");
            html.append("<p>加载评论失败: ").append(e.getMessage()).append("</p>");
            html.append("</div>");
        }

        return html.toString();
    }

    @PostMapping("/comments/delete")
    @ResponseBody
    public String deleteComment(@RequestParam String id, @RequestParam String type) {
        boolean success;
        if ("landscape".equals(type)) {
            success = landCommentService.deleteByAdmin(id);
        } else {
            success = postService.deleteCommentByAdmin(id);
        }
        return success ? "success" : "fail";
    }
}
