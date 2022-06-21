package com.practice.summerpractice;

import com.practice.summerpractice.entity.ExamDto;
import com.practice.summerpractice.entity.RulesDto;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class Controller {

    @GetMapping("/rules")
    public RulesDto getRules() {
        return new RulesDto();
    }

    @GetMapping("/exam")
    public ExamDto getExam() {
        return new ExamDto();
    }
}
