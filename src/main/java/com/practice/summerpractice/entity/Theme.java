package com.practice.summerpractice.entity;

import com.google.gson.annotations.SerializedName;
import lombok.Data;

import java.util.List;

@Data
public class Theme {
    private int id;
    private String number;
    private String name;

    @SerializedName("item_traffic_rules")
    private List<Rule> trafficRules;
}
