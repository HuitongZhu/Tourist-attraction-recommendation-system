package com.travel.travelweb.web;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.travel.travelweb.config.LoginInterceptor;
import com.travel.travelweb.entity.LandCollect;
import com.travel.travelweb.entity.LandLike;
import com.travel.travelweb.entity.Landscape;
import com.travel.travelweb.entity.PostCollect;
import com.travel.travelweb.entity.PostLike;
import com.travel.travelweb.entity.RecommendationPost;
import com.travel.travelweb.repo.LandCollectRepository;
import com.travel.travelweb.repo.LandLikeRepository;
import com.travel.travelweb.repo.LandscapeRepository;
import com.travel.travelweb.repo.PostCollectRepository;
import com.travel.travelweb.repo.PostLikeRepository;
import com.travel.travelweb.repo.RecommendationPostRepository;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/my")
public class MyController {

    private final LandCollectRepository landCollectRepository;
    private final LandLikeRepository landLikeRepository;
    private final LandscapeRepository landscapeRepository;
    private final PostCollectRepository postCollectRepository;
    private final PostLikeRepository postLikeRepository;
    private final RecommendationPostRepository recommendationPostRepository;

    public MyController(
            LandCollectRepository landCollectRepository,
            LandLikeRepository landLikeRepository,
            LandscapeRepository landscapeRepository,
            PostCollectRepository postCollectRepository,
            PostLikeRepository postLikeRepository,
            RecommendationPostRepository recommendationPostRepository) {
        this.landCollectRepository = landCollectRepository;
        this.landLikeRepository = landLikeRepository;
        this.landscapeRepository = landscapeRepository;
        this.postCollectRepository = postCollectRepository;
        this.postLikeRepository = postLikeRepository;
        this.recommendationPostRepository = recommendationPostRepository;
    }

    @GetMapping("/collects")
    public String collects(HttpSession session, Model model) {
        String uid = (String) session.getAttribute(LoginInterceptor.SESSION_USER_ID);
        List<LandCollect> rows = landCollectRepository.findByUserIdOrderByCollectTimeDesc(uid);
        List<Landscape> landscapes = new ArrayList<>();
        for (LandCollect c : rows) {
            landscapeRepository.findById(c.getLandscapeId()).ifPresent(landscapes::add);
        }
        model.addAttribute("landscapes", landscapes);
        model.addAttribute("tab", "collect");
        model.addAttribute("navKey", "my");
        return "my-landmarks";
    }

    @GetMapping("/likes")
    public String likes(HttpSession session, Model model) {
        String uid = (String) session.getAttribute(LoginInterceptor.SESSION_USER_ID);
        List<LandLike> rows = landLikeRepository.findByUserIdOrderByLikeTimeDesc(uid);
        List<Landscape> landscapes = new ArrayList<>();
        for (LandLike like : rows) {
            Optional<Landscape> l = landscapeRepository.findById(like.getLandscapeId());
            l.ifPresent(landscapes::add);
        }
        model.addAttribute("landscapes", landscapes);
        model.addAttribute("tab", "like");
        model.addAttribute("navKey", "my");
        return "my-landmarks";
    }

    @GetMapping("/published")
    public String published(HttpSession session, Model model) {
        String uid = (String) session.getAttribute(LoginInterceptor.SESSION_USER_ID);
        List<Landscape> landscapes = landscapeRepository.findByUserId(uid);
        model.addAttribute("landscapes", landscapes);
        model.addAttribute("tab", "published");
        model.addAttribute("navKey", "my");
        return "my-landmarks";
    }

    @GetMapping("/post-collects")
    public String postCollects(HttpSession session, Model model) {
        String uid = (String) session.getAttribute(LoginInterceptor.SESSION_USER_ID);
        List<PostCollect> rows = postCollectRepository.findByUserIdOrderByCollectTimeDesc(uid);
        List<RecommendationPost> posts = new ArrayList<>();
        for (PostCollect c : rows) {
            recommendationPostRepository.findById(c.getRecomId()).ifPresent(posts::add);
        }
        model.addAttribute("posts", posts);
        model.addAttribute("landscapeTitles", getLandscapeTitles(posts));
        model.addAttribute("tab", "post_collect");
        model.addAttribute("navKey", "my");
        return "my-posts";
    }

    @GetMapping("/post-likes")
    public String postLikes(HttpSession session, Model model) {
        String uid = (String) session.getAttribute(LoginInterceptor.SESSION_USER_ID);
        List<PostLike> rows = postLikeRepository.findByUserIdOrderByLikeTimeDesc(uid);
        List<RecommendationPost> posts = new ArrayList<>();
        for (PostLike like : rows) {
            recommendationPostRepository.findById(like.getRecomId()).ifPresent(posts::add);
        }
        model.addAttribute("posts", posts);
        model.addAttribute("landscapeTitles", getLandscapeTitles(posts));
        model.addAttribute("tab", "post_like");
        model.addAttribute("navKey", "my");
        return "my-posts";
    }

    private Map<String, String> getLandscapeTitles(List<RecommendationPost> posts) {
        Map<String, String> landscapeTitles = new HashMap<>();
        for (RecommendationPost post : posts) {
            if (post.getLandscapeId() != null && !post.getLandscapeId().isBlank()) {
                landscapeRepository.findById(post.getLandscapeId())
                        .ifPresent(l -> {
                            if ("审核通过".equals(l.getAuditState())) {
                                landscapeTitles.put(post.getRecomId(), l.getTitle());
                            }
                        });
            }
        }
        return landscapeTitles;
    }
}
