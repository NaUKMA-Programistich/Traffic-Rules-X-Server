package com.practice.summerpractice.entity;

import lombok.Data;

@Data
public class Rule {
    private int id;
    private int themeId;
    private String number;
    private String description;
    private String content;
}
