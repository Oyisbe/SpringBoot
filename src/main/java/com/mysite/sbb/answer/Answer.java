package com.mysite.sbb.answer;

import com.mysite.sbb.question.Question;
import com.mysite.sbb.user.SiteUser;
import groovyjarjarpicocli.CommandLine;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Set;

@Getter
@Setter
@Entity
public class Answer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(columnDefinition = "TEXT")
    private String content;

    private LocalDateTime createDate;

    @ManyToOne
//  질문은 하나, 답변은 많은 N:1의 관계 ( 질문 엔티티와 연결된 속성 )
//  Answer의 question 과 Question 엔티티가 서로 연결됨 - foreign key
//  답변을 통해 질문을 얻고싶다면? answer.getQuestion.getSubject()
    private Question question;

    @ManyToOne
    private SiteUser author;

    private LocalDateTime modifyDate;

    @ManyToMany
    Set<SiteUser> voter;
}


