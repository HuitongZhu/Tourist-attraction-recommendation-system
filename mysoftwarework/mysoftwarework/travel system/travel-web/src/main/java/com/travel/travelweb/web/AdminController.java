package com.travel.travelweb.web;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.travel.travelweb.config.LoginInterceptor;
import com.travel.travelweb.entity.Landscape;
import com.travel.travelweb.entity.OrdinaryUser;
import com.travel.travelweb.entity.RecommendationPost;
import com.travel.travelweb.entity.SysUser;
import com.travel.travelweb.repo.OrdinaryUserRepository;
import com.travel.travelweb.repo.RecommendationPostRepository;
import com.travel.travelweb.service.LandCommentService;
import com.travel.travelweb.service.LandscapeService;
import com.travel.travelweb.service.PostService;
import com.travel.travelweb.service.RecommendationPostService;
import com.travel.travelweb.service.UserService;

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

    public AdminController(LandscapeService landscapeService, UserService userService, 
                          RecommendationPostRepository postRepository,
                          OrdinaryUserRepository ordinaryUserRepository,
                          RecommendationPostService recommendationPostService,
                          LandCommentService landCommentService,
                          PostService postService) {
        this.landscapeService = landscapeService;
        this.userService = userService;
        this.postRepository = postRepository;
        this.ordinaryUserRepository = ordinaryUserRepository;
        this.recommendationPostService = recommendationPostService;
        this.landCommentService = landCommentService;
        this.postService = postService;
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
}
