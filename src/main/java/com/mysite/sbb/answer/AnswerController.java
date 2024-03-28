package com.mysite.sbb.answer;

import com.mysite.sbb.question.Question;
import com.mysite.sbb.question.QuestionService;
import com.mysite.sbb.user.SiteUser;
import com.mysite.sbb.user.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.security.Principal;

@Controller
@RequestMapping("/answer")
@RequiredArgsConstructor
public class AnswerController {
    private final QuestionService questionService;
    private final AnswerService answerService;
    private final UserService userService;

//    @PostMapping("/create/{id}")
//    public String createAnswer(Model model, @PathVariable("id") Integer id,
//                               @RequestParam(value="content") String content)
//    {
//        Question question = questionService.getQuestion(id);
//        answerService.create(question, content);
//        return String.format("redirect:/question/detail/%s",id);
//    }
    @PostMapping("/create/{id}")
    @PreAuthorize("isAuthenticated()")
    public String CreateAnswer(Model model, @PathVariable("id") Integer id,
                               @Valid AnswerForm answerForm, BindingResult bindingResult,
                               Principal principal)
    {
        Question question = questionService.getQuestion(id);
        SiteUser siteUser = userService.getUser(principal.getName());
        if(bindingResult.hasErrors()){
            model.addAttribute("question",question);
            return "question_detail";
        }

        Answer answer = answerService.create(question,answerForm.getContent(),siteUser);
        return String.format("redirect:/question/detail/%s#answer_%s",
                answer.getQuestion().getId(), answer.getId());
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/modify/{id}")
    public String answerModify(AnswerForm answerForm, @PathVariable("id") Integer id,
                               Principal principal){
        Answer answer = answerService.getAnswer(id);
        if(!answer.getAuthor().getUsername().equals(principal.getName()))
        {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "수정 권한이 없습니다.");
        }
        answerForm.setContent(answerForm.getContent());
        return "answer_form";
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/modify/{id}")
    public String answerModify(@Valid AnswerForm answerForm, BindingResult bindingResult,
                               @PathVariable("id") Integer id, Principal principal){
        if(bindingResult.hasErrors())
            return "answer_form";
        Answer answer = answerService.getAnswer(id);
        if(!answer.getAuthor().getUsername().equals(principal.getName()))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "수정 권한이 없습니다.");
        answerService.modify(answer, answerForm.getContent());
        return String.format("redirect:/question/detail/%s#answer_%s",answer.getQuestion().getId(),
                answer.getId());
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/delete/{id}")
    public String answerModify(Principal principal, @PathVariable("id")
                               Integer id){
        Answer answer = answerService.getAnswer(id);
        if(!answer.getAuthor().getUsername().equals(principal.getName())){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "삭제 권한이 없습니다.");
        }
        answerService.delete(answer);
        return String.format("redirect:/question/detail/%s",
                answer.getQuestion().getId());
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/vote/{id}")
    public String answerVote(Principal principal, @PathVariable("id") Integer id ){
        Answer answer = answerService.getAnswer(id);
        SiteUser siteuser = userService.getUser(principal.getName());

        return String.format("redirect:/question/detail/%s#answer_%s",answer.getQuestion().getId(),
                answer.getId());
    }
}
