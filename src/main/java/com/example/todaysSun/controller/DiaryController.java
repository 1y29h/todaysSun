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
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import org.springframework.validation.BindingResult;
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

        boolean isAuthor = loginId != null && loginId.equals(diary.getAuthor());
        model.addAttribute("isAuthor", isAuthor);

        return "diaries/view";

    }

    // 월별 보기
    @GetMapping("/diaries/{year}/{month}")
    public String viewMonth(@PathVariable int year, @PathVariable int month, Model model) {
        LocalDate start = LocalDate.of(year, month, 1);
        LocalDate end = start.withDayOfMonth(start.lengthOfMonth());
        List<Diary> diaries = diaryRepository.findByDateBetween(start, end);

        // 날짜별 일기들(id + mood)
        Map<Integer, List<Map<String, Object>>> diaryMap = new HashMap<>();

        for (Diary d : diaries) {
            int day = d.getDate().getDayOfMonth();
            diaryMap.computeIfAbsent(day, k -> new ArrayList<>()).add(
                    Map.of("id", d.getId(), "mood", d.getMood())
            );
        }

        List<Map<String, Object>> days = new ArrayList<>();
        for (int i = 1; i <= start.lengthOfMonth(); i++) {
            Map<String, Object> dayMap = new HashMap<>();
            dayMap.put("day", i);
            dayMap.put("date", LocalDate.of(year, month, i));
            dayMap.put("year", year); // 템플릿에서 안전하게 사용하기 위해 추가
            dayMap.put("month", month);

            if (diaryMap.containsKey(i)) {
                dayMap.put("diaries", diaryMap.get(i));  // List of {id, mood}
            }

            days.add(dayMap);
        }

        model.addAttribute("year", year);
        model.addAttribute("month", month);
        model.addAttribute("days", days);

        LocalDate today = LocalDate.now();
        model.addAttribute("todayDay", (today.getYear() == year && today.getMonthValue() == month) ? today.getDayOfMonth() : 1);

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
                "작은 기회로부터 종종 위대한 업적이 시작된다. -데모스테네스-",
                "내가 꾸준히 실천하고 있는 행복한 습관은 무엇인가요?",
                "오늘 내가 당당하게 마주한 일은 어떤 것이었나요?",
                "최근 힘들었던 순간 속에서 내가 지켜낸 나만의 원칙은 무엇인가요?",
                "지금 내가 믿고 싶은 나의 가능성은 어떤 모습인가요?",
                "작지만 내 인생에 영향을 준 기회가 있었나요?",
                "성공은 최선을 다한 결과일 뿐, 결코 우연이 아니다. -콜린 파월-",
                "오늘 내가 최선을 다한 일은 무엇이었나요?",
                "자신을 이기는 것이 가장 위대한 승리다. -플라톤-",
                "오늘 나 자신과 싸워 이긴 순간은 언제였나요?",
                "실패는 성공으로 가는 또 다른 기회다. -헨리 포드-",
                "최근의 실패로부터 내가 배운 점은 무엇인가요?",
                "가장 어두운 밤도 끝나고 해는 뜬다. -빅터 위고-",
                "요즘 내가 희망을 느끼게 된 계기는 무엇이었나요?",
                "사람은 행복하기로 마음먹은 만큼 행복하다. -에이브러햄 링컨-",
                "행복을 선택하기 위해 오늘 내가 한 작은 선택은 무엇인가요?",
                "천 마디 말보다 하나의 행동이 더 낫다. -벤저민 프랭클린-",
                "오늘 내가 실천한 의미 있는 행동은 무엇이었나요?",
                "성장은 불편함 속에서 일어난다. -로이 베넷-",
                "최근 나를 불편하게 했지만 성장하게 만든 경험은?",
                "진짜 용기는 두려움 속에서도 행동하는 것이다. -마크 트웨인-",
                "두려움을 안고도 내가 시도한 일이 있었나요?",
                "시간은 우리가 가진 가장 공평한 자산이다. -짐 론-",
                "오늘 하루를 내가 가장 가치 있게 쓴 순간은?",
                "남과 비교하지 말고 어제의 나와 비교하라. -익명-",
                "어제보다 더 나아진 나의 모습은 무엇인가요?"
        );
        model.addAttribute("randomQuote", quotes.get(new Random().nextInt(quotes.size())));

        // localDate를 사용하여 DiaryForm 생성
        model.addAttribute("diary", new DiaryForm(null, "", localDate.toString(), "", "😊", null));

        return "diaries/new";
    }

    @PostMapping("/diaries/{year}/{month}/{day}")
    public String createByDate(@PathVariable int year,
                               @PathVariable int month,
                               @PathVariable int day,
                               @Valid @ModelAttribute("diary") DiaryForm form,
                               BindingResult bindingResult,
                               HttpSession session,
                               Model model) {

        if (bindingResult.hasErrors()) {
            model.addAttribute("year", year);
            model.addAttribute("month", month);
            model.addAttribute("day", day);
            model.addAttribute("randomQuote", "오늘도 좋은 하루 보내세요 :)");

            if (bindingResult.hasFieldErrors("title")) {
                model.addAttribute("titleError", bindingResult.getFieldError("title").getDefaultMessage());
            }
            if (bindingResult.hasFieldErrors("date")) {
                model.addAttribute("dateError", bindingResult.getFieldError("date").getDefaultMessage());
            }
            if (bindingResult.hasFieldErrors("mood")) {
                model.addAttribute("moodError", bindingResult.getFieldError("mood").getDefaultMessage());
            }
            if (bindingResult.hasFieldErrors("content")) {
                model.addAttribute("contentError", bindingResult.getFieldError("content").getDefaultMessage());
            }

            return "diaries/new";
        }

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
        return "redirect:/diaries/view/" + saved.getId();
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