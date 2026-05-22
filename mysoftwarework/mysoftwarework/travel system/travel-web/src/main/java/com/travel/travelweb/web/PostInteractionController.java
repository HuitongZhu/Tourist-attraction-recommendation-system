package com.travel.travelweb.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.travel.travelweb.config.LoginInterceptor;
import com.travel.travelweb.service.PostInteractionService;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/interactions/post")
public class PostInteractionController {

    private final PostInteractionService postInteractionService;

    public PostInteractionController(PostInteractionService postInteractionService) {
        this.postInteractionService = postInteractionService;
    }

    @PostMapping("/{id}/like")
    public String toggleLike(@PathVariable String id, HttpSession session) {
        String uid = (String) session.getAttribute(LoginInterceptor.SESSION_USER_ID);
        postInteractionService.toggleLike(id, uid);
        return "redirect:/posts/" + id;
    }

    @PostMapping("/{id}/collect")
    public String toggleCollect(@PathVariable String id, HttpSession session) {
        String uid = (String) session.getAttribute(LoginInterceptor.SESSION_USER_ID);
        postInteractionService.toggleCollect(id, uid);
        return "redirect:/posts/" + id;
    }
}