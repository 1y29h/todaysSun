package com.example.todaysSun.dto;


import com.example.todaysSun.entity.Diary;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

import java.time.LocalDate;

@AllArgsConstructor
@ToString
@Getter
public class DiaryForm {
    private Long id;
    @NotBlank(message = "제목을 입력해주세요.")
    private String title;
    private String date;
    @NotBlank(message = "내용을 입력해주세요.")
    private String content;
    private String mood;
    private String author;

    public Diary toEntity(String loginId) {
        return new Diary(id, title, LocalDate.parse(date), content, mood, loginId);
    }

}
