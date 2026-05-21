package com.travel.travelweb.web;

import com.travel.travelweb.entity.Landscape;
import com.travel.travelweb.entity.RecommendationPost;
import com.travel.travelweb.service.LandscapeService;
import com.travel.travelweb.service.PostService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
public class HomeController {

    private final LandscapeService landscapeService;
    private final PostService postService;

    public HomeController(LandscapeService landscapeService, PostService postService) {
        this.landscapeService = landscapeService;
        this.postService = postService;
    }

    @GetMapping("/")
    public String index(Model model) {
        List<Landscape> landscapes = landscapeService.homeLandscapes(8);
        List<RecommendationPost> posts = postService.homePosts(6);
        model.addAttribute("landscapes", landscapes);
        model.addAttribute("posts", posts);
        model.addAttribute("navKey", "home");
        return "index";
    }
}
