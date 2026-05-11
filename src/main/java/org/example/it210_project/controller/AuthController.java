package org.example.it210_project.controller;

import jakarta.servlet.http.HttpSession;
import org.example.it210_project.model.User;
import org.example.it210_project.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Optional;

@Controller
public class AuthController {
    @Autowired private UserService userService;

    @GetMapping("/login")
    public String loginPage() {
        return "auth/login";
    }

    @PostMapping("/login")
    public String handleLogin(@RequestParam String username,
                              @RequestParam String password,
                              HttpSession session,
                              Model model) {
        if (username == null || username.isBlank()) {
            model.addAttribute("errUser", "Ten dang nhap khong duoc de trong");
            model.addAttribute("username", username);
            return "auth/login";
        }
        if (password == null || password.isBlank()) {
            model.addAttribute("errPass", "Mat khau khong duoc de trong");
            model.addAttribute("username", username);
            return "auth/login";
        }

        Optional<User> userOpt = userService.login(username, password);
        if (userOpt.isPresent()) {
            session.setAttribute("user", userOpt.get());
            if (userOpt.get().getRole() == User.Role.ADMIN) return "redirect:/admin/dashboard";
            if (userOpt.get().getRole() == User.Role.STAFF) return "redirect:/staff/counter";
            return "redirect:/user/home";
        }

        model.addAttribute("err", "Sai tai khoan hoac mat khau");
        model.addAttribute("username", username);
        return "auth/login";
    }

    @GetMapping("/register")
    public String registerPage(Model model) {
        model.addAttribute("user", new User());
        return "auth/register";
    }

    @PostMapping("/register")
    public String handleRegister(@ModelAttribute User user,
                                 @RequestParam String phoneNumber,
                                 HttpSession session,
                                 Model model) {
        try {
            boolean hasError = false;
            if (user.getFullName() == null || user.getFullName().isBlank()) {
                model.addAttribute("errName", "Ho ten khong duoc de trong");
                hasError = true;
            }
            if (user.getUsername() == null || user.getUsername().isBlank()) {
                model.addAttribute("errUser", "Ten dang nhap khong duoc de trong");
                hasError = true;
            }
            if (user.getEmail() == null || user.getEmail().isBlank()) {
                model.addAttribute("errEmail", "Email khong duoc de trong");
                hasError = true;
            }
            if (user.getPassword() == null || user.getPassword().isBlank()) {
                model.addAttribute("errPass", "Mat khau khong duoc de trong");
                hasError = true;
            } else if (user.getPassword().length() < 6) {
                model.addAttribute("errPass", "Mat khau toi thieu 6 ky tu");
                hasError = true;
            }
            if (phoneNumber == null || !phoneNumber.matches("\\d{10}")) {
                model.addAttribute("errPhone", "So dien thoai phai du 10 chu so");
                hasError = true;
            }
            if (hasError) {
                model.addAttribute("phoneNumber", phoneNumber);
                return "auth/register";
            }

            User savedUser = userService.register(user, phoneNumber);
            session.setAttribute("user", savedUser);
            return "redirect:/login";
        } catch (Exception e) {
            model.addAttribute("err", "Ten dang nhap hoac email da ton tai, hoac loi he thong!");
            model.addAttribute("phoneNumber", phoneNumber);
            return "auth/register";
        }
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }
}
