package com.example.todaysSun.controller;

import com.example.todaysSun.service.LoginService;
import com.example.todaysSun.dto.LoginForm;
import com.example.todaysSun.entity.Member;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Slf4j
@Controller
@RequiredArgsConstructor
public class LoginController {

    private final LoginService loginService;

    @ModelAttribute
    public void addCommonAttributes(Model model, HttpServletRequest request) {
        model.addAttribute("bootstrapCssPath", request.getContextPath() + "/css/bootstrap.min.css");
        model.addAttribute("contextPath", request.getContextPath());
    }

    @GetMapping("/login")
    public String loginForm(@ModelAttribute("loginForm") LoginForm form, Model model, HttpServletRequest request) {
        addCommonAttributes(model, request);

        model.addAttribute("pageTitle", "로그인");
        model.addAttribute("formAction", "/login");
        model.addAttribute("labelLoginId", "아이디");
        model.addAttribute("labelPassword", "비밀번호");
        model.addAttribute("buttonLogin", "로그인");
        model.addAttribute("buttonCancel", "취소");
        model.addAttribute("cancelUrl", "/");

        model.addAttribute("loginId", form.getLoginId() != null ? form.getLoginId() : "");

        return "login/loginForm";
    }

    @PostMapping("/login")
    public String login(@Valid @ModelAttribute("loginForm") LoginForm form,
                        BindingResult bindingResult,
                        HttpServletResponse response,
                        Model model,
                        HttpServletRequest request) {

        addCommonAttributes(model, request);
        model.addAttribute("pageTitle", "로그인");
        model.addAttribute("formAction", "/login");
        model.addAttribute("labelLoginId", "아이디");
        model.addAttribute("labelPassword", "비밀번호");
        model.addAttribute("buttonLogin", "로그인");
        model.addAttribute("buttonCancel", "취소");
        model.addAttribute("cancelUrl", "/");

        model.addAttribute("loginId", form.getLoginId() != null ? form.getLoginId() : "");

        if (bindingResult.hasErrors()) {
            if (bindingResult.hasFieldErrors("loginId")) {
                model.addAttribute("hasLoginIdError", true);
                model.addAttribute("loginIdError", bindingResult.getFieldError("loginId").getDefaultMessage());
            }
            if (bindingResult.hasFieldErrors("password")) {
                model.addAttribute("hasPasswordError", true);
                model.addAttribute("passwordError", bindingResult.getFieldError("password").getDefaultMessage());
            }
            return "login/loginForm";
        }

        Member loginMember = loginService.login(form.getLoginId(), form.getPassword());
        if (loginMember == null) {
            model.addAttribute("globalErrorMessage", "아이디 또는 비밀번호가 일치하지 않습니다.");
            return "login/loginForm";
        }

        request.getSession().setAttribute("loginId", loginMember.getLoginId());
        request.getSession().setAttribute("loginUserName", loginMember.getName());

        Cookie idCookie = new Cookie("memberId", String.valueOf(loginMember.getId()));
        response.addCookie(idCookie);

        return "redirect:/";
    }

    @PostMapping("/logout")
    public String logout(HttpServletResponse response) {
        expireCookie(response, "memberId");
        return "redirect:/";
    }

    private void expireCookie(HttpServletResponse response, String cookieName) {
        Cookie cookie = new Cookie(cookieName, null);
        cookie.setMaxAge(0);
        response.addCookie(cookie);
    }

}