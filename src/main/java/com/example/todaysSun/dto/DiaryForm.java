package com.example.todaysSun.dto;


import com.example.todaysSun.entity.Diary;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

import java.time.LocalDate;

@AllArgsConstructor
@ToString
@Getter
public class DiaryForm {
    public Long id;
    public String title;
    public String date;
    public String content;
    public String mood;
    public String author;

    public Diary toEntity(String loginId) {
        return new Diary(id, title, LocalDate.parse(date), content, mood, loginId);
    }

}
