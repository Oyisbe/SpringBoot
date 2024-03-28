package com.mysite.sbb.question;

import com.mysite.sbb.DataNotFoundException;
import com.mysite.sbb.answer.Answer;
import com.mysite.sbb.user.SiteUser;
import jakarta.persistence.criteria.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class QuestionService {

    private final QuestionRepository questionRepository;

    public List<Question> getList(){
        return this.questionRepository.findAll();
    }

    public Question getQuestion(Integer id){
        Optional<Question> question = questionRepository.findById(id);

        if(question.isPresent()){
            return question.get();
        }else{
            throw new DataNotFoundException("Data not found.");
        }
    }

    public void create(String subject, String content, SiteUser user){
        Question q = new Question();
        q.setSubject(subject);
        q.setContent(content);
        q.setCreateDate(LocalDateTime.now());
        q.setAuthor(user);
        questionRepository.save(q);
    }

    public void modify(Question question, String subject, String content)
    {
        question.setSubject(subject);
        question.setContent(content);
        question.setModifyDate(LocalDateTime.now());
        questionRepository.save(question);
    }

//    public Page<Question> getList(int page) {
//        Pageable pageable = PageRequest.of(page, 10);
//        return this.questionRepository.findAll(pageable);
//    }

    public Page<Question> getList(int page, String kw){
        List<Sort.Order> sorts = new ArrayList<>();
        sorts.add(Sort.Order.desc("createDate"));
        Pageable pageable = PageRequest.of(page,10,Sort.by(sorts));
        Specification<Question> spec = search(kw);
//        return questionRepository.findAll(spec, pageable);
        return this.questionRepository.findAllByKeyword(kw, pageable);
    }

    public void delete(Question question){
        questionRepository.delete(question);
    }

    public void vote(Question question, SiteUser siteUser){
        question.getVoter().add(siteUser);
        questionRepository.save(question);
    }

    private Specification<Question> search(String kw)
    {
        return new Specification<>() {
            private static final long serialVerLONGID = 1L;
            @Override
            public Predicate toPredicate(Root<Question> q, CriteriaQuery<?> query, CriteriaBuilder cb) {
                query.distinct(true);
                Join<Question, SiteUser> u1 = q.join("author", JoinType.LEFT);
                Join<Question, Answer> a = q.join("answerList", JoinType.LEFT);
                Join<Answer,SiteUser> u2 = a.join("author",JoinType.LEFT);
                return cb.or(cb.like(q.get("subject"),"%"+kw+"%"),
                        cb.like(q.get("content"),"%"+kw+"%"),
                        cb.like(u1.get("username"),"%"+kw+"%"),
                        cb.like(a.get("content"),"%"+kw+"%"),
                        cb.like(u2.get("username"),"%"+kw+"%"));
            }
        };
    }
}
