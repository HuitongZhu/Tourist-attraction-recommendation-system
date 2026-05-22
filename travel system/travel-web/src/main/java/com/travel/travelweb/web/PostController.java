package com.travel.travelweb.web;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.travel.travelweb.config.LoginInterceptor;
import com.travel.travelweb.entity.RecommendationPost;
import com.travel.travelweb.service.LandscapeService;
import com.travel.travelweb.service.PostInteractionService;
import com.travel.travelweb.service.PostService;

import jakarta.servlet.http.HttpSession;

@Controller
public class PostController {

    private final PostService postService;
    private final PostInteractionService postInteractionService;
    private final LandscapeService landscapeService;

    public PostController(PostService postService, PostInteractionService postInteractionService, LandscapeService landscapeService) {
        this.postService = postService;
        this.postInteractionService = postInteractionService;
        this.landscapeService = landscapeService;
    }

    @GetMapping("/posts")
    public String list(
            @RequestParam(required = false) String keyword,
            Model model) {
        String kw = keyword != null ? keyword.trim() : "";
        var posts = kw.isEmpty() ? postService.listApproved() : postService.searchApproved(kw);
        model.addAttribute("posts", posts);
        
        Map<String, String> landscapeTitles = new HashMap<>();
        for (var post : posts) {
            if (post.getLandscapeId() != null && !post.getLandscapeId().isBlank()) {
                String title = postService.getLandscapeTitle(post.getLandscapeId());
                if (title != null) {
                    landscapeTitles.put(post.getRecomId(), title);
                }
            }
        }
        model.addAttribute("landscapeTitles", landscapeTitles);
        
        model.addAttribute("keyword", kw);
        model.addAttribute("total", posts);
        model.addAttribute("navKey", "posts");
        return "post-list";
    }

    @GetMapping("/post/my")
    public String myPosts(HttpSession session, Model model) {
        String userId = (String) session.getAttribute(LoginInterceptor.SESSION_USER_ID);
        if (userId == null) {
            return "redirect:/login?next=/post/my";
        }
        var posts = postService.findByUserId(userId);
        model.addAttribute("posts", posts);
        
        Map<String, String> landscapeTitles = new HashMap<>();
        for (var post : posts) {
            if (post.getLandscapeId() != null && !post.getLandscapeId().isBlank()) {
                String title = postService.getLandscapeTitle(post.getLandscapeId());
                if (title != null) {
                    landscapeTitles.put(post.getRecomId(), title);
                }
            }
        }
        model.addAttribute("landscapeTitles", landscapeTitles);
        
        model.addAttribute("navKey", "my");
        model.addAttribute("tab", "posts");
        return "my-posts";
    }

    @GetMapping("/posts/new")
    public String create(Model model) {
        model.addAttribute("landscapes", landscapeService.listApproved());
        return "posts-new";
    }

    @GetMapping("/posts/{id}")
    public String detail(@PathVariable String id, Model model, HttpSession session) {
        String userId = (String) session.getAttribute(LoginInterceptor.SESSION_USER_ID);
        
        Optional<RecommendationPost> p = postService.findApproved(id);
        
        // 如果不是审核通过的帖子，检查是否是当前用户自己的帖子
        if (p.isEmpty() && userId != null) {
            p = postService.findById(id);
            // 如果找到了帖子，但不是当前用户的帖子，不允许查看
            if (p.isPresent() && !userId.equals(p.get().getUserId())) {
                return "redirect:/posts";
            }
        }
        
        if (p.isEmpty()) {
            return "redirect:/posts";
        }
        
        RecommendationPost post = p.get();
        model.addAttribute("post", post);
        
        // 获取关联景点名称
        String landscapeTitle = null;
        if (post.getLandscapeId() != null && !post.getLandscapeId().isBlank()) {
            landscapeTitle = postService.getLandscapeTitle(post.getLandscapeId());
        }
        model.addAttribute("landscapeTitle", landscapeTitle);
        
        model.addAttribute("comments", postService.comments(id));
        model.addAttribute("navKey", "posts");
        model.addAttribute("loginUserId", userId);
        if (userId != null) {
            model.addAttribute("liked", postInteractionService.liked(id, userId));
            model.addAttribute("collected", postInteractionService.collected(id, userId));
        }
        model.addAttribute("likeCount", postInteractionService.likeCount(id));
        return "post-detail";
    }

    @GetMapping("/admin/posts/detail/{id}")
    public String adminDetail(@PathVariable String id, Model model) {
        var p = postService.findById(id);
        if (p.isEmpty()) {
            return "redirect:/admin";
        }
        model.addAttribute("post", p.get());
        model.addAttribute("comments", postService.comments(id));
        return "admin-post-detail";
    }

    @PostMapping("/posts/new")
    public String save(@RequestParam String title, 
                       @RequestParam(required = false) String landscapeId, 
                       @RequestParam(required = false) String tag, 
                       @RequestParam String content, 
                       HttpSession session,
                       org.springframework.web.servlet.mvc.support.RedirectAttributes ra) {
        String userId = (String) session.getAttribute(LoginInterceptor.SESSION_USER_ID);
        if (userId == null) {
            return "redirect:/login?next=/posts/new";
        }
        System.out.println("=== 发布推荐帖 ===");
        System.out.println("userId: " + userId);
        System.out.println("title: " + title);
        System.out.println("landscapeId: " + landscapeId);
        System.out.println("tag: " + tag);
        System.out.println("content: " + content);
        try {
            postService.createPost(userId, title, landscapeId, tag, content);
            System.out.println("推荐帖发布成功！");
            return "redirect:/posts";
        } catch (IllegalArgumentException e) {
            System.out.println("发布失败: " + e.getMessage());
            ra.addFlashAttribute("msg", e.getMessage());
            return "redirect:/posts/new";
        }
    }

    @GetMapping("/posts/edit/{id}")
    public String edit(@PathVariable String id, Model model, HttpSession session) {
        String userId = (String) session.getAttribute(LoginInterceptor.SESSION_USER_ID);
        if (userId == null) {
            return "redirect:/login?next=/posts/edit/" + id;
        }
        Optional<RecommendationPost> p = postService.findById(id);
        if (p.isEmpty() || !userId.equals(p.get().getUserId())) {
            return "redirect:/post/my";
        }
        model.addAttribute("post", p.get());
        model.addAttribute("landscapes", landscapeService.listApproved());
        model.addAttribute("navKey", "my");
        model.addAttribute("tab", "posts");
        return "posts-edit";
    }

    @PostMapping("/posts/edit/{id}")
    public String update(@PathVariable String id, 
                         @RequestParam String title, 
                         @RequestParam(required = false) String landscapeId, 
                         @RequestParam(required = false) String tag, 
                         @RequestParam String content, 
                         HttpSession session) {
        String userId = (String) session.getAttribute(LoginInterceptor.SESSION_USER_ID);
        if (userId == null) {
            return "redirect:/login?next=/posts/edit/" + id;
        }
        try {
            postService.updatePost(id, userId, title, landscapeId, tag, content);
        } catch (IllegalArgumentException e) {
            return "redirect:/post/my";
        }
        return "redirect:/post/my";
    }

    @PostMapping("/posts/delete/{id}")
    public String delete(@PathVariable String id, HttpSession session) {
        String userId = (String) session.getAttribute(LoginInterceptor.SESSION_USER_ID);
        if (userId == null) {
            return "redirect:/login?next=/post/my";
        }
        try {
            postService.deletePost(id, userId);
        } catch (IllegalArgumentException e) {
            // 忽略错误
        }
        return "redirect:/post/my";
    }
}