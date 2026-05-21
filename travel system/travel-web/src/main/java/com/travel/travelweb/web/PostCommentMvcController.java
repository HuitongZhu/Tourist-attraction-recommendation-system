package com.travel.travelweb.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.travel.travelweb.config.LoginInterceptor;
import com.travel.travelweb.service.PostService;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/comments/post")
public class PostCommentMvcController {

    private final PostService postService;

    public PostCommentMvcController(PostService postService) {
        this.postService = postService;
    }

    @PostMapping
    public String add(
            @RequestParam String recomId,
            @RequestParam String content,
            HttpSession session,
            RedirectAttributes ra) {
        String uid = (String) session.getAttribute(LoginInterceptor.SESSION_USER_ID);
        try {
            postService.addComment(recomId, uid, content);
        } catch (IllegalArgumentException e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/posts/" + recomId;
    }

    @PostMapping("/{commentId}/update")
    public String update(
            @PathVariable String commentId,
            @RequestParam String content,
            HttpSession session,
            RedirectAttributes ra) {
        String uid = (String) session.getAttribute(LoginInterceptor.SESSION_USER_ID);
        String rid = postService.findRecomIdForComment(commentId).orElse("");
        try {
            postService.updateCommentOwn(commentId, uid, content);
            ra.addFlashAttribute("msg", "评论已更新");
        } catch (IllegalArgumentException e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return rid.isEmpty() ? "redirect:/posts" : "redirect:/posts/" + rid;
    }

    @PostMapping("/{commentId}/delete")
    public String delete(@PathVariable String commentId, HttpSession session, RedirectAttributes ra) {
        String uid = (String) session.getAttribute(LoginInterceptor.SESSION_USER_ID);
        String rid = postService.findRecomIdForComment(commentId).orElse("");
        try {
            postService.deleteCommentOwn(commentId, uid);
            ra.addFlashAttribute("msg", "已删除评论");
        } catch (IllegalArgumentException e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return rid.isEmpty() ? "redirect:/posts" : "redirect:/posts/" + rid;
    }
}
