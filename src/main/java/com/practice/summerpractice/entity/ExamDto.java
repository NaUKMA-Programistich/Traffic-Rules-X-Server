package com.practice.summerpractice.entity;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class ExamDto {
    private List<Question> questionList;
}
