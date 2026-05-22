package com.travel.travelweb.web;

import com.travel.travelweb.config.LoginInterceptor;
import com.travel.travelweb.service.LandInteractionService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/interactions/landscape")
public class LandInteractionController {

    private final LandInteractionService landInteractionService;

    public LandInteractionController(LandInteractionService landInteractionService) {
        this.landInteractionService = landInteractionService;
    }

    @PostMapping("/{id}/like")
    public String toggleLike(@PathVariable String id, HttpSession session) {
        String uid = (String) session.getAttribute(LoginInterceptor.SESSION_USER_ID);
        landInteractionService.toggleLike(id, uid);
        return "redirect:/landscapes/" + id;
    }

    @PostMapping("/{id}/collect")
    public String toggleCollect(@PathVariable String id, HttpSession session) {
        String uid = (String) session.getAttribute(LoginInterceptor.SESSION_USER_ID);
        landInteractionService.toggleCollect(id, uid);
        return "redirect:/landscapes/" + id;
    }
}
