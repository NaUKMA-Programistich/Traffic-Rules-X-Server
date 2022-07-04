package com.practice.summerpractice.entity;

import lombok.Data;

import java.util.List;

@Data
public class Theme {
    private int id;
    private String number;
    private String name;
    private List<Rule> trafficRules;
}
