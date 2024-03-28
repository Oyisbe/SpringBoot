package com.mysite.sbb.question;

import com.mysite.sbb.answer.Answer;
import com.mysite.sbb.user.SiteUser;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Getter
@Setter
//Setter 사용 지양 할 것 : ( 별도 메소드 구현 )
@Entity
public class Question {
    @Id
    //p.K 설정
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    //해당 속성에 자동으로 1씩 증가하여 저장됨
    private Integer id;

    @Column(length = 200)
    private String subject;

    @Column(columnDefinition = "Text")
    private String content;

    private LocalDateTime createDate;

    //질문 하나에 답변은 여러 개
    @OneToMany(mappedBy = "question", cascade = CascadeType.REMOVE)
    //질문에서 답변 참조 : question.getAnswerList()
    //mappedBy : 참조 엔티티의 속성 명 (Answer 앤티티에서 Question을 참조한 속성 전달 )
    private List<Answer> answerList;

    @ManyToOne
    private SiteUser author;

    private LocalDateTime modifyDate;

    @ManyToMany
    Set<SiteUser> voter;
}
