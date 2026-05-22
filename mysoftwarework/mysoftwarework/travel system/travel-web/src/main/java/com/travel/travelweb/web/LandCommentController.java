package com.travel.travelweb.web;

import com.travel.travelweb.config.LoginInterceptor;
import com.travel.travelweb.entity.LandComment;
import com.travel.travelweb.service.LandCommentService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/comments/landscape")
public class LandCommentController {

    private final LandCommentService landCommentService;

    public LandCommentController(LandCommentService landCommentService) {
        this.landCommentService = landCommentService;
    }

    @PostMapping
    public String add(
            @RequestParam String landscapeId,
            @RequestParam String content,
            HttpSession session,
            RedirectAttributes ra) {
        String uid = (String) session.getAttribute(LoginInterceptor.SESSION_USER_ID);
        try {
            landCommentService.add(landscapeId, uid, content);
        } catch (IllegalArgumentException e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/landscapes/" + landscapeId;
    }

    @PostMapping("/{commentId}/delete")
    public String delete(@PathVariable String commentId, HttpSession session, RedirectAttributes ra) {
        String uid = (String) session.getAttribute(LoginInterceptor.SESSION_USER_ID);
        String lid = landCommentService.findLandscapeId(commentId).orElse("");
        try {
            landCommentService.deleteOwn(commentId, uid);
            ra.addFlashAttribute("msg", "已删除评论");
        } catch (IllegalArgumentException e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return lid.isEmpty() ? "redirect:/landscapes" : "redirect:/landscapes/" + lid;
    }

    @PostMapping("/{commentId}/update")
    public String update(
            @PathVariable String commentId,
            @RequestParam String content,
            HttpSession session,
            RedirectAttributes ra) {
        String uid = (String) session.getAttribute(LoginInterceptor.SESSION_USER_ID);
        String lid = landCommentService.findLandscapeId(commentId).orElse("");
        try {
            landCommentService.updateOwn(commentId, uid, content);
            ra.addFlashAttribute("msg", "评论已更新");
        } catch (IllegalArgumentException e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return lid.isEmpty() ? "redirect:/landscapes" : "redirect:/landscapes/" + lid;
    }
}