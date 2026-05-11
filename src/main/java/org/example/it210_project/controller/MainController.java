package org.example.it210_project.controller;

import jakarta.servlet.http.HttpSession;
import org.example.it210_project.model.User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class MainController {

    // TRANG PROFILE DÙNG CHUNG
    @GetMapping("/profile")
    public String viewProfile(HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        if (user == null) return "redirect:/login";

        model.addAttribute("user", user);
        return "profile"; // Trả về file profile.html ở thư mục templates
    }

    // TRANG CHỦ MẶC ĐỊNH (Nếu khách chưa gõ gì)
    @GetMapping("/")
    public String index(HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) return "redirect:/login";

        // Tự động điều hướng dựa trên Role khi vừa vào web
        if (user.getRole().name().equals("ADMIN")) return "redirect:/admin/dashboard";
        if (user.getRole().name().equals("STAFF")) return "redirect:/staff/counter";
        return "redirect:/user/home";
    }
}