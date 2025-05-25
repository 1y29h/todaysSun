package com.example.todaysSun.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDate;

@AllArgsConstructor
@NoArgsConstructor
@ToString
@Getter
@Entity
public class Diary {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)


    private Long id;

    private String title;

    @Column(name = "date")
    private LocalDate date;

    @Lob
    @Column(columnDefinition = "CLOB")
    private String content;

    private String mood;

    private String author;

}
