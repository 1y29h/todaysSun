package com.example.todaysSun.controller;

import com.example.todaysSun.entity.Member;
import com.example.todaysSun.repository.MemberRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.Locale;

@Controller
@RequiredArgsConstructor
@RequestMapping("/members")
public class MemberController {

    private final MemberRepository memberRepository;

    @ModelAttribute
    public void addCommonAttributes(Model model, HttpServletRequest request) {
        model.addAttribute("bootstrapCssPath", request.getContextPath() + "/css/bootstrap.min.css");
        model.addAttribute("contextPath", request.getContextPath());
    }

    @GetMapping("/add")
    public String showAddMemberForm(Model model, HttpServletRequest request, Locale locale) {
        model.addAttribute("member", new Member()); // <- 이거 추가

        model.addAttribute("loginId", "");
        model.addAttribute("password", "");
        model.addAttribute("name", "");

        model.addAttribute("hasLoginIdError", false);
        model.addAttribute("hasPasswordError", false);
        model.addAttribute("hasNameError", false);

        model.addAttribute("loginIdError", null);
        model.addAttribute("passwordError", null);
        model.addAttribute("nameError", null);
        model.addAttribute("globalErrorMessage", null);

        model.addAttribute("bootstrapCssPath", request.getContextPath() + "/css/bootstrap.min.css");
        model.addAttribute("contextPath", request.getContextPath());
        model.addAttribute("formAction", "/members/add");
        model.addAttribute("cancelUrl", "/");

        model.addAttribute("pageTitle", "회원 가입");
        model.addAttribute("formTitle", "회원 정보 입력");
        model.addAttribute("labelLoginId", "로그인 ID");
        model.addAttribute("labelPassword", "비밀번호");
        model.addAttribute("labelName", "이름");
        model.addAttribute("buttonSubmit", "가입");
        model.addAttribute("buttonCancel", "취소");

        return "members/addMemberForm"; // <- 파일 경로와 일치시켜
    }



    @PostMapping("/add")
    public String save(@Valid @ModelAttribute("member") Member member,
                       BindingResult bindingResult,
                       Model model,
                       HttpServletRequest request) {

        addCommonAttributes(model, request);
        setFormAttributes(model, member);

        if (bindingResult.hasErrors()) {
            if (bindingResult.hasFieldErrors("loginId")) {
                model.addAttribute("hasLoginIdError", true);
                model.addAttribute("loginIdError", bindingResult.getFieldError("loginId").getDefaultMessage());
            }
            if (bindingResult.hasFieldErrors("password")) {
                model.addAttribute("hasPasswordError", true);
                model.addAttribute("passwordError", bindingResult.getFieldError("password").getDefaultMessage());
            }
            if (bindingResult.hasFieldErrors("name")) {
                model.addAttribute("hasNameError", true);
                model.addAttribute("nameError", bindingResult.getFieldError("name").getDefaultMessage());
            }
            return "members/addMemberForm";
        }

        memberRepository.save(member);
        return "redirect:/";
    }

    private void setFormAttributes(Model model, Member member) {
        model.addAttribute("pageTitle", "회원가입");
        model.addAttribute("formTitle", "회원 가입");
        model.addAttribute("formAction", "/members/add");
        model.addAttribute("labelLoginId", "아이디");
        model.addAttribute("labelPassword", "비밀번호");
        model.addAttribute("labelName", "이름");
        model.addAttribute("buttonSubmit", "가입");
        model.addAttribute("buttonCancel", "취소");
        model.addAttribute("cancelUrl", "/");

        // Mustache에 개별 값으로 전달
        model.addAttribute("loginId", member.getLoginId());
        model.addAttribute("password", member.getPassword());
        model.addAttribute("name", member.getName());
    }
}
