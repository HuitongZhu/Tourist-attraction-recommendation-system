package com.travel.travelweb.web;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.travel.travelweb.config.LoginInterceptor;
import com.travel.travelweb.entity.Landscape;
import com.travel.travelweb.repo.LandscapeRepository;
import com.travel.travelweb.service.LandCommentService;
import com.travel.travelweb.service.LandInteractionService;
import com.travel.travelweb.service.LandscapeService;

import jakarta.servlet.http.HttpSession;

@Controller
public class LandscapeController {

    private final LandscapeRepository landscapeRepository;
    private final LandscapeService landscapeService;
    private final LandInteractionService landInteractionService;
    private final LandCommentService landCommentService;

    public LandscapeController(
            LandscapeRepository landscapeRepository,
            LandscapeService landscapeService,
            LandInteractionService landInteractionService,
            LandCommentService landCommentService) {
        this.landscapeRepository = landscapeRepository;
        this.landscapeService = landscapeService;
        this.landInteractionService = landInteractionService;
        this.landCommentService = landCommentService;
    }

    @GetMapping("/landscapes")
    public String search(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false, defaultValue = "all") String city,
            @RequestParam(required = false, defaultValue = "all") String level,
            @RequestParam(required = false, defaultValue = "hot") String sort,
            Model model) {
        List<Landscape> list = landscapeService.search(keyword, city, level, sort);
        List<String> ids = list.stream().map(Landscape::getLandscapeId).toList();
        Map<String, Long> likeMap = ids.isEmpty() ? new HashMap<>() : new HashMap<>();
        if (!ids.isEmpty()) {
            likeMap.putAll(landscapeService.likeCounts(ids));
        }
        model.addAttribute("landscapes", list);
        model.addAttribute("likeMap", likeMap);
        model.addAttribute("keyword", keyword != null ? keyword : "");
        model.addAttribute("city", city);
        model.addAttribute("level", level);
        model.addAttribute("sort", sort);
        model.addAttribute("total", list.size());
        model.addAttribute("navKey", "landscapes");
        return "landscape-search";
    }

    @GetMapping("/landscapes/{id}")
    public String detail(@PathVariable String id, HttpSession session, Model model) {
        Optional<Landscape> raw = landscapeRepository.findById(id);
        if (raw.isEmpty()) {
            return "redirect:/landscapes";
        }
        Landscape l = raw.get();
        String uid = session != null ? (String) session.getAttribute(LoginInterceptor.SESSION_USER_ID) : null;
        boolean approved = LandscapeService.AUDIT_APPROVED.equals(l.getAuditState());
        boolean owner = uid != null && uid.equals(l.getUserId());
        if (!approved && !owner) {
            return "redirect:/landscapes";
        }
        model.addAttribute("landscape", l);
        model.addAttribute("likeCount", landscapeService.likeCount(id));
        model.addAttribute("collectCount", landscapeService.collectCount(id));
        model.addAttribute("comments", landCommentService.listForLandscape(id));
        model.addAttribute("related", landscapeService.related(l.getAddress(), id, 4));
        model.addAttribute("pendingAudit", !approved);
        model.addAttribute("liked", uid != null && landInteractionService.liked(id, uid));
        model.addAttribute("collected", uid != null && landInteractionService.collected(id, uid));
        model.addAttribute("loginUserId", uid);
        model.addAttribute("navKey", "landscapes");
        return "landscape-detail";
    }

    @GetMapping("/admin/landscapes/detail/{id}")
    public String adminDetail(@PathVariable String id, Model model) {
        var raw = landscapeRepository.findById(id);
        if (raw.isEmpty()) {
            return "redirect:/admin";
        }
        Landscape l = raw.get();
        model.addAttribute("landscape", l);
        model.addAttribute("likeCount", landscapeService.likeCount(id));
        model.addAttribute("collectCount", landscapeService.collectCount(id));
        model.addAttribute("comments", landCommentService.listForLandscape(id));
        model.addAttribute("related", landscapeService.related(l.getAddress(), id, 4));
        return "admin-landscape-detail";
    }

    @GetMapping("/landscapes/new")
    public String newForm(Model model) {
        model.addAttribute("navKey", "landscapes");
        return "landscape-new";
    }

    @PostMapping("/landscapes/new")
    public String create(
            HttpSession session,
            @RequestParam String title,
            @RequestParam String content,
            @RequestParam String address,
            @RequestParam(required = false) Double latitude,
            @RequestParam(required = false) Double longitude,
            @RequestParam String landscapeTel,
            @RequestParam String openingTime,
            @RequestParam String level,
            @RequestParam(required = false) MultipartFile image,
            RedirectAttributes ra) {
        String uid = (String) session.getAttribute(LoginInterceptor.SESSION_USER_ID);
        try {
            String id = landscapeService.createLandscape(uid, title, content, address, latitude, longitude, landscapeTel, openingTime, level, image);
            ra.addFlashAttribute("msg", "提交成功，等待管理员审核");
            return "redirect:/landscapes/" + id;
        } catch (IllegalArgumentException e) {
            ra.addFlashAttribute("msg", e.getMessage());
            return "redirect:/landscapes/new";
        } catch (IOException e) {
            ra.addFlashAttribute("msg", "图片上传失败，请重试");
            return "redirect:/landscapes/new";
        }
    }

    @GetMapping("/landscapes/edit/{id}")
    public String edit(@PathVariable String id, Model model, HttpSession session) {
        String userId = (String) session.getAttribute(LoginInterceptor.SESSION_USER_ID);
        if (userId == null) {
            return "redirect:/login?next=/landscapes/edit/" + id;
        }
        Optional<Landscape> l = landscapeService.findById(id);
        if (l.isEmpty() || !userId.equals(l.get().getUserId())) {
            return "redirect:/my/published";
        }
        model.addAttribute("landscape", l.get());
        model.addAttribute("navKey", "my");
        model.addAttribute("tab", "published");
        return "landscape-edit";
    }

    @PostMapping("/landscapes/edit/{id}")
    public String update(
            @PathVariable String id,
            @RequestParam String title,
            @RequestParam String content,
            @RequestParam String address,
            @RequestParam(required = false) Double latitude,
            @RequestParam(required = false) Double longitude,
            @RequestParam String landscapeTel,
            @RequestParam String openingTime,
            @RequestParam String level,
            @RequestParam(required = false) MultipartFile image,
            HttpSession session,
            RedirectAttributes ra) {
        String userId = (String) session.getAttribute(LoginInterceptor.SESSION_USER_ID);
        if (userId == null) {
            return "redirect:/login?next=/landscapes/edit/" + id;
        }
        try {
            landscapeService.updateLandscape(id, userId, title, content, address, latitude, longitude, landscapeTel, openingTime, level, image);
            ra.addFlashAttribute("msg", "修改成功，等待管理员审核");
        } catch (IllegalArgumentException e) {
            ra.addFlashAttribute("msg", e.getMessage());
        } catch (IOException e) {
            ra.addFlashAttribute("msg", "图片上传失败，请重试");
        }
        return "redirect:/my/published";
    }

    @PostMapping("/landscapes/delete/{id}")
    public String delete(@PathVariable String id, HttpSession session, RedirectAttributes ra) {
        String userId = (String) session.getAttribute(LoginInterceptor.SESSION_USER_ID);
        if (userId == null) {
            return "redirect:/login?next=/my/published";
        }
        try {
            landscapeService.deleteLandscape(id, userId);
        } catch (IllegalArgumentException e) {
            ra.addFlashAttribute("msg", e.getMessage());
        } catch (DataIntegrityViolationException e) {
            ra.addFlashAttribute("msg", "删除失败，该景点存在关联数据（推荐帖、点赞、评论等），无法直接删除");
        }
        return "redirect:/my/published";
    }
}