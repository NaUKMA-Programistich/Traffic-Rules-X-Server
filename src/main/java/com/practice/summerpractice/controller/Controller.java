package com.practice.summerpractice.controller;

import com.practice.summerpractice.entity.ExamDto;
import com.practice.summerpractice.entity.RulesDto;
import com.practice.summerpractice.parser.ParserStartup;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class Controller {

    ParserStartup parserStartup = new ParserStartup();

    @ResponseBody
    @GetMapping("/rules")
    public RulesDto getRules() {
        return parserStartup.parseRules();
    }

    @ResponseBody
    @GetMapping("/exam")
    public ExamDto getExam() {
        return parserStartup.parseExam();
    }
}
