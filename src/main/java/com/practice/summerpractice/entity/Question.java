package com.practice.summerpractice.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Question {
    private int id;
    private String name;
    private int ruleId;
    private String pictureLink;
    private List<Answer> answers;
    private int correctAnswerId;
}
