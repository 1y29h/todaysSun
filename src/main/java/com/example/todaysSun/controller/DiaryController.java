package com.example.todaysSun.controller;

import com.example.todaysSun.dto.DiaryForm;
import com.example.todaysSun.entity.Diary;
import com.example.todaysSun.entity.Member;
import com.example.todaysSun.repository.DiaryRepository;
import com.example.todaysSun.repository.MemberRepository;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Controller
public class DiaryController {

    @Autowired
    private DiaryRepository diaryRepository;
    @Autowired
    private MemberRepository memberRepository;

    // /diaries → /diaries/list 리디렉션
    @GetMapping("/diaries")
    public String diariesRedirect() {
        return "redirect:/diaries/list";
    }

    // 일기 목록
    @GetMapping("/diaries/list")
    public String index(Model model, HttpSession session) {
        List<Diary> diaryList = diaryRepository.findAll(Sort.by(Sort.Direction.DESC, "date"));

        List<Map<String, Object>> enrichedList = diaryList.stream().map(diary -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", diary.getId());
            map.put("title", diary.getTitle());
            map.put("mood", diary.getMood());
            LocalDate date = diary.getDate();
            map.put("year", date.getYear());
            map.put("month", date.getMonthValue());
            map.put("day", date.getDayOfMonth());
            map.put("date", date.toString());

            String authorId = diary.getAuthor();
            String memberName = memberRepository.findByLoginId(authorId)
                    .map(Member::getName)
                    .orElse("(알 수 없음)");
            map.put("memberName", memberName);

            return map;
        }).collect(Collectors.toList());

        model.addAttribute("diaryList", enrichedList);
        return "diaries/index";
    }

    @GetMapping("/diaries/view/{id}")
    public String viewDiaryById(@PathVariable Long id, Model model, HttpSession session) {
        Diary diary = diaryRepository.findById(id).orElse(null);
        if (diary == null) return "redirect:/diaries/list";

        String loginId = (String) session.getAttribute("loginId");
        LocalDate date = diary.getDate();

        Map<String, Object> map = new HashMap<>();
        map.put("id", diary.getId());
        map.put("title", diary.getTitle());
        map.put("content", diary.getContent());
        map.put("mood", diary.getMood());
        map.put("date", date);
        map.put("isOwner", loginId != null && loginId.equals(diary.getAuthor()));
        map.put("memberName", memberRepository.findByLoginId(diary.getAuthor())
                .map(Member::getName).orElse("(알 수 없음)"));

        model.addAttribute("year", date.getYear());
        model.addAttribute("month", date.getMonthValue());
        model.addAttribute("diary", map);

        return "diaries/view";

    }


    // 월별 보기
    // 월별 보기
    @GetMapping("/diaries/{year}/{month}")
    public String viewMonth(@PathVariable int year, @PathVariable int month, Model model) {
        LocalDate start = LocalDate.of(year, month, 1);
        LocalDate end = start.withDayOfMonth(start.lengthOfMonth());
        List<Diary> diaries = diaryRepository.findByDateBetween(start, end);

        // 같은 날짜의 이모지를 모두 리스트로 수집
        Map<Integer, List<String>> emojiMap = new HashMap<>();
        for (Diary d : diaries) {
            int day = d.getDate().getDayOfMonth();
            emojiMap.computeIfAbsent(day, k -> new ArrayList<>()).add(d.getMood());
        }

        // day 정보를 모아서 Mustache로 전달
        List<Map<String, Object>> days = new ArrayList<>();
        for (int i = 1; i <= start.lengthOfMonth(); i++) {
            Map<String, Object> dayMap = new HashMap<>();
            dayMap.put("day", i);
            dayMap.put("date", LocalDate.of(year, month, i));
            if (emojiMap.containsKey(i)) {
                dayMap.put("emojiList", emojiMap.get(i));
            }
            days.add(dayMap);
        }

        model.addAttribute("year", year);
        model.addAttribute("month", month);
        model.addAttribute("days", days);

        LocalDate today = LocalDate.now();
        if (today.getYear() == year && today.getMonthValue() == month) {
            model.addAttribute("todayDay", today.getDayOfMonth());
        } else {
            model.addAttribute("todayDay", 1);
        }

        return "diaries/calendar";
    }

    // 날짜 기반 일기 조회
    @GetMapping("/diaries/{year}/{month}/{day}")
    public String showByDate(@PathVariable int year,
                             @PathVariable int month,
                             @PathVariable int day,
                             Model model,
                             HttpSession session) {
        LocalDate date = LocalDate.of(year, month, day);
        List<Diary> diaryList = diaryRepository.findAllByDate(date);

        String loginId = (String) session.getAttribute("loginId");

        List<Map<String, Object>> enrichedList = diaryList.stream().map(diary -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", diary.getId());
            map.put("title", diary.getTitle());
            map.put("content", diary.getContent());
            map.put("mood", diary.getMood());

            String memberName = memberRepository.findByLoginId(diary.getAuthor())
                    .map(Member::getName)
                    .orElse("(알 수 없음)");
            map.put("memberName", memberName);

            map.put("isOwner", loginId != null && loginId.equals(diary.getAuthor()));
            map.put("date", diary.getDate()); // date도 빠져있으면 템플릿에서 에러

            return map;
        }).collect(Collectors.toList());


        model.addAttribute("date", date);
        model.addAttribute("diaries", enrichedList); // This is a list of maps

        if (!diaryList.isEmpty()) {
            Diary top = diaryList.get(0);
            Map<String, Object> topMap = new HashMap<>();
            topMap.put("id", top.getId());
            topMap.put("title", top.getTitle());
            topMap.put("content", top.getContent());
            topMap.put("mood", top.getMood());
            topMap.put("date", top.getDate());

            String memberName = memberRepository.findByLoginId(top.getAuthor())
                    .map(Member::getName)
                    .orElse("(알 수 없음)");
            topMap.put("memberName", memberName);
            topMap.put("isOwner", loginId != null && loginId.equals(top.getAuthor()));

            model.addAttribute("diary", topMap); // This is the single diary map
        }
        else {
            model.addAttribute("diary", Map.of(
                    "title", "(제목 없음)",
                    "content", "(내용 없음)",
                    "mood", "😐",
                    "date", LocalDate.of(year, month, day),
                    "memberName", "(작성자 없음)",
                    "isOwner", false
            ));
        }

        return "diaries/show";

    }

    // 작성 폼
    @GetMapping("/diaries/new")
    public String newDiaryForm(@RequestParam(required = false) String date, Model model) {
        LocalDate localDate; // localDate 선언

        if (date != null && !date.isEmpty()) {
            localDate = LocalDate.parse(date); // 조건에 따라 초기화
            model.addAttribute("defaultDate", date);
        } else {
            localDate = LocalDate.now(); // 조건에 따라 초기화
            model.addAttribute("defaultDate", localDate.toString());
        }

        // year, month, day는 localDate가 초기화된 후에 사용
        model.addAttribute("year", localDate.getYear());
        model.addAttribute("month", localDate.getMonthValue());
        model.addAttribute("day", localDate.getDayOfMonth());

        List<String> quotes = List.of(
                "행복은 습관이다. 그것을 몸에 지니라. -허버드-",
                "고개 숙이지 마십시오. 세상을 똑바로 정면으로 바라보십시오. -헬렌 켈러-",
                "고난의 시기에 동요하지 않는 것, 이것은 진정 칭찬받을 만한 뛰어난 인물의 증거다. -베토벤-",
                "당신이 할 수 있다고 믿든 할 수 없다고 믿든, 믿는 대로 될 것이다. -헨리 포드-",
                "작은 기회로부터 종종 위대한 업적이 시작된다. -데모스테네스-"
        );
        model.addAttribute("randomQuote", quotes.get(new Random().nextInt(quotes.size())));

        // localDate를 사용하여 DiaryForm 생성
        model.addAttribute("diary", new DiaryForm(null, "", localDate.toString(), "", "😊", null));

        return "diaries/new";
    }

    // 작성 처리
    @PostMapping("/diaries/{year}/{month}/{day}")
    public String createByDate(@PathVariable int year,
                               @PathVariable int month,
                               @PathVariable int day,
                               DiaryForm form,
                               HttpSession session) {
        String loginId = (String) session.getAttribute("loginId");
        Diary diary = new Diary(
                null,
                form.getTitle(),
                LocalDate.parse(form.getDate()),
                form.getContent(),
                form.getMood(),
                loginId
        );
        Diary saved = diaryRepository.save(diary);
        return "redirect:/diaries/view/" + saved.getId(); // ✅ 특정 일기 보기로 리디렉션
    }

    // 수정 폼
    @GetMapping("/diaries/{id}/edit")
    public String edit(@PathVariable Long id, Model model) {
        Diary diary = diaryRepository.findById(id).orElse(null);
        if (diary == null) return "redirect:/diaries/list";

        model.addAttribute("diary", diary);
        model.addAttribute("year", diary.getDate().getYear());
        model.addAttribute("month", diary.getDate().getMonthValue());
        model.addAttribute("day", diary.getDate().getDayOfMonth());

        return "diaries/edit";
    }

    // 수정 처리
    @PostMapping("/diaries/{year}/{month}/{day}/update")
    public String updateByDate(@PathVariable int year,
                               @PathVariable int month,
                               @PathVariable int day,
                               DiaryForm form,
                               HttpSession session) {
        String loginId = (String) session.getAttribute("loginId");

        Diary diaryEntity = form.toEntity(loginId);
        Diary target = diaryRepository.findById(diaryEntity.getId()).orElse(null);
        if (target != null) {
            diaryRepository.save(diaryEntity);
        }
        return "redirect:/diaries/view/" + diaryEntity.getId();
    }

    // 삭제
    @GetMapping("/diaries/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes rttr) {
        Diary target = diaryRepository.findById(id).orElse(null);
        if (target != null) {
            diaryRepository.delete(target);
            rttr.addFlashAttribute("msg", "삭제 완료!");
        }
        return "redirect:/diaries/list";
    }

}