package com.mysite.sbb.question;

import com.mysite.sbb.answer.AnswerForm;
import com.mysite.sbb.user.SiteUser;
import com.mysite.sbb.user.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;

// 해당 어노테이션을 통해 기존에는 Repo 에 Autowired 로 DI를 했지만,
// RequiredArgsConstructor 또한 DI 를 수앵하기에 굳이 Autowired 를 사용할 필요가 없다.

// 기존의 Test Case 작성 시, 인터페이스의 메소드 구현부만 있었으므로 사용하려면
// 별도로 Autowired를 통해 DI를 수행했었던 것과 다름.

@Slf4j
@Controller
@RequiredArgsConstructor
@RequestMapping ("/question")
public class QuestionController {

    private final QuestionService questionService;
    private final UserService userService;

//    @GetMapping("/list")
//    public String list(Model model){
//        List<Question> questionList = questionService.getList();
//        model.addAttribute("questionList",questionList);
//        return "question_list";
//    }

    @GetMapping("/list")
    public String list(Model model, @RequestParam(value = "page", defaultValue = "0") int page,
                       @RequestParam(value = "kw", defaultValue = "") String kw) {
        log.info("page:{}, kw:{}",page,kw);
        Page<Question> paging = questionService.getList(page,kw);
        model.addAttribute("paging", paging);
        model.addAttribute("kw", kw);
        return "question_list";
    }



    @GetMapping("/detail/{id}")
    public String detail(Model model, @PathVariable ("id") Integer id, AnswerForm answerForm)
    {
        Question question = questionService.getQuestion(id);
        model.addAttribute("question",question);
        return "question_detail";
    }

    @GetMapping("/create")
    @PreAuthorize("isAuthenticated()")
    public String questionCreate(QuestionForm questionForm){
        return "question_form";
    }

    @PostMapping("/create")
    @PreAuthorize("isAuthenticated()")
    public String questionCreate(@Valid QuestionForm questionForm, BindingResult bindingResult,
                                 Principal principal)
    {
        if(bindingResult.hasErrors()) {
            return "question_form";
        }

        SiteUser siteUser = userService.getUser(principal.getName());
        questionService.create(questionForm.getSubject(),questionForm.getContent(),siteUser);
        return "redirect:/question/list";
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/modify/{id}")
    public String questionModify(QuestionForm questionForm,
                                 @PathVariable("id") Integer id, Principal principal ){
        Question question = questionService.getQuestion(id);
        if(!question.getAuthor().getUsername().equals(principal.getName()))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"수정 권한이 없습니다.");
        questionForm.setSubject(question.getSubject());
        questionForm.setContent(question.getContent());
        return "question_form";
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/modify/{id}")
    public String questionModify(@Valid QuestionForm questionForm, BindingResult bindingResult,
                                 Principal principal, @PathVariable("id") Integer id)
    {
        if(bindingResult.hasErrors())
            return "question_form";
        Question question = questionService.getQuestion(id);

        if(!question.getAuthor().getUsername().equals(principal.getName()))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"수정 권한이 없습니다.");

        questionService.modify(question, questionForm.getSubject(), questionForm.getContent());

        return String.format("redirect:/question/detail/%s",id);
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/delete/{id}")
    public String questionDelete(Principal principal, @PathVariable("id") Integer id){
        Question question = questionService.getQuestion(id);
        if(!question.getAuthor().getUsername().equals(principal.getName())){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"삭제 권한이 없습니다.");
        }
        questionService.delete(question);
        return "redirect:/";
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/vote/{id}")
    public String questionVote(Principal principal, @PathVariable("id") Integer id){
        Question question = questionService.getQuestion(id);
        SiteUser siteUser = userService.getUser(principal.getName());
        questionService.vote(question,siteUser);
        return String.format("redirect:/question/detail/%s",id);
    }
}
