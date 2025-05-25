package com.example.todaysSun.controller;

import com.example.todaysSun.repository.DiaryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class GatherController {

    @Autowired
    private DiaryRepository diaryRepository;

    @GetMapping("/gather")
    public String showGather(Model model) {
        List<Integer> years = diaryRepository.findDistinctYears();
        List<Map<String, Object>> yearList = new ArrayList<>();

        for (Integer year : years) {
            Map<String, Object> yearData = new HashMap<>();
            yearData.put("year", year);

            List<Map<String, Object>> months = new ArrayList<>();
            for (int m = 1; m <= 12; m++) {
                Map<String, Object> monthMap = new HashMap<>();
                monthMap.put("year", year);
                monthMap.put("month", m);
                months.add(monthMap);
            }

            yearData.put("months", months);
            yearList.add(yearData);
        }

        model.addAttribute("years", yearList);
        return "gather/index";
    }
}
