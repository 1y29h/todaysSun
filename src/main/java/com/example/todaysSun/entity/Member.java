package com.example.todaysSun.entity;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class Member {

    private Long id;

    @NotBlank(message = "비어 있을 수 없습니다.")
    private String loginId; //로그인 ID
    @NotBlank(message = "비어 있을 수 없습니다.")
    private String name; //사용자 이름
    @NotBlank(message = "비어 있을 수 없습니다.")
    private String password;

}