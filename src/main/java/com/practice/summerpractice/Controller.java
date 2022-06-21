package com.practice.summerpractice;

import com.practice.summerpractice.entity.ExamDto;
import com.practice.summerpractice.entity.RulesDto;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class Controller {

    @GetMapping("/rules")
    public RulesDto getRules() {
        // отдать все правила из бд
        return new RulesDto();
    }

    @GetMapping("/exam")
    public ExamDto getExam() {
        // todo сходить по ссылке https://api.testpdr.com/v1/exam-questions?is_training=false
        // спарсить задание - картинка(если нету то налл), варианты ответа, правильный ответ, обьяснение
        return new ExamDto();
    }
}
