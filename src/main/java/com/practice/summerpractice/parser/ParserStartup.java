package com.practice.summerpractice.parser;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.practice.summerpractice.entity.ExamDto;
import com.practice.summerpractice.entity.Rule;
import com.practice.summerpractice.entity.RulesDto;
import com.practice.summerpractice.entity.Theme;
import lombok.SneakyThrows;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
public class ParserStartup implements ApplicationListener<ApplicationReadyEvent> {

    @SneakyThrows
    @Override
    public void onApplicationEvent(final ApplicationReadyEvent event) {
        RulesDto rulesDto = parseRules();
    }

    public static ExamDto parseExam(){
        // TODO fill
        return new ExamDto();
    }

    public static RulesDto parseRules() {
        FileReader fileReader = null;
        try {
            fileReader = new FileReader("C:\\summer 22\\traffic\\src\\main\\resources\\rulesList.json");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        assert fileReader != null;
        JsonObject object = new JsonParser().parse(fileReader).getAsJsonObject();
        Set<Map.Entry<String, JsonElement>> themesJson = object.getAsJsonObject().entrySet();
        RulesDto rulesDto = new RulesDto();
        List<Theme> themes = new LinkedList<>();
        for (Map.Entry<String, JsonElement> entry : themesJson) {
            themes.add(parseTheme(entry.getValue().getAsJsonObject()));
        }
        rulesDto.setThemes(themes);
        return rulesDto;
    }

    private static Theme parseTheme(JsonObject jsonObject) {
        Theme theme = new Theme();
        theme.setId(jsonObject.get("id").getAsInt());
        theme.setNumber(jsonObject.get("number").getAsString());
        theme.setName(jsonObject.get("name").getAsJsonObject().entrySet().toArray()[0].toString().substring(4));
        List<Rule> trafficRules = new LinkedList<>();
        Set<Map.Entry<String, JsonElement>> trafficRulesJson = jsonObject.get("item_traffic_rules").getAsJsonObject().entrySet();
        for (Map.Entry<String, JsonElement> entry : trafficRulesJson) {
            trafficRules.add(parseRule(entry.getValue().getAsJsonObject()));
        }
        theme.setTrafficRules(trafficRules);
        return theme;
    }

    private static Rule parseRule(JsonObject jsonObject) {
        Rule rule = new Rule();
        rule.setId(jsonObject.get("id").getAsInt());
        rule.setThemeId(jsonObject.get("topic_traffic_rule_id").getAsInt());
        rule.setNumber(jsonObject.get("number").toString());
        rule.setDescription(jsonObject.get("description").getAsJsonObject().entrySet().toArray()[0].toString().substring(4));
        rule.setContent(jsonObject.get("content").getAsJsonObject().entrySet().toArray()[0].toString().substring(4));
        return rule;
    }
}