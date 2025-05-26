package com.example.todaysSun.controller;

import com.example.todaysSun.entity.Member;
import com.example.todaysSun.repository.MemberRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.util.Locale;
import java.util.Optional;

@Slf4j
@Controller
@RequiredArgsConstructor
public class HomeController {

    private final MemberRepository memberRepository;
    private final MessageSource messageSource;

    @ModelAttribute
    public void addCommonAttributes(Model model, HttpServletRequest request) {
        model.addAttribute("bootstrapCssPath", request.getContextPath() + "/css/bootstrap.min.css");
        model.addAttribute("contextPath", request.getContextPath());
        model.addAttribute("itemsUrl", request.getContextPath() + "/items");
    }

    private String getMessage(String code, Locale locale) {
        return messageSource.getMessage(code, null, locale);
    }

    // HomeController 안에 아래 메서드를 추가하면 해결됨
    private void setHomeAttributes(Model model, Locale locale) {
        model.addAttribute("homeTitle", getMessage("page.home.title", locale));
        model.addAttribute("addMemberUrl", "/members/add");
        model.addAttribute("memberRegistrationText", getMessage("page.home.memberRegistration", locale));
        model.addAttribute("loginUrl", "/login");
        model.addAttribute("loginText", getMessage("page.home.login", locale));
    }


    @GetMapping("/")
    public String homeLogin(@CookieValue(name = "memberId", required = false) Long memberId,
                            Model model,
                            Locale locale) {

        log.info("HomeController accessed with memberId: {}", memberId);

        // 비로그인 사용자
        if (memberId == null) {
            log.info("No memberId found, showing home page");
            setHomeAttributes(model, locale);
            return "home";
        }

        // 로그인 ID로 사용자 찾기
        Optional<Member> optionalMember = memberRepository.findById(memberId);
        if (optionalMember.isEmpty()) {
            log.warn("Member not found for id: {}, redirecting to home", memberId);
            return "redirect:/";
        }

        Member loginMember = optionalMember.get();
        log.info("Found member: {}", loginMember);

        return "redirect:/diaries/list";
    }


}