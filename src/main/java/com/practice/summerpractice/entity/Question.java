package com.practice.summerpractice.entity;

import lombok.Data;

import java.awt.*;
import java.util.List;

@Data
public class Question {
    private int id;
    private int number;
    private String name;
    private String explanation;
    private String ruleId;
    private Image picture;
    private List<Answer> answers;
}
