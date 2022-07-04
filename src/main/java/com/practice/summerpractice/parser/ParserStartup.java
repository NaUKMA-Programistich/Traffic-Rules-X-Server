package com.practice.summerpractice.parser;

import com.google.gson.Gson;
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

import java.io.*;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
public class ParserStartup implements ApplicationListener<ApplicationReadyEvent> {

    @SneakyThrows
    @Override
    public void onApplicationEvent(final ApplicationReadyEvent event) {
        new RegisterData().fillRulesDatabase();
    }

    public ExamDto parseExam() {
        try {
            RegisterData.fillExamDatabase();
        } catch (IOException e) {
            e.printStackTrace();
        }
        JsonObject object = readFile("src/main/resources/examExample.json");
        Gson gson = new Gson();
        return gson.fromJson(object, ExamDto.class);
    }

    public RulesDto parseRules() {
        JsonObject object =  readFile("src/main/resources/rulesList.json");
        Set<Map.Entry<String, JsonElement>> themesJson = object.getAsJsonObject().entrySet();
        RulesDto rulesDto = new RulesDto();
        List<Theme> themes = new LinkedList<>();
        for (Map.Entry<String, JsonElement> entry : themesJson) {
            themes.add(parseTheme(entry.getValue().getAsJsonObject()));
        }
        rulesDto.setThemes(themes);
        return rulesDto;
    }

    private JsonObject readFile(String source) {
        FileInputStream fileInputStream = null;
        try {
            fileInputStream = new FileInputStream(source);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        assert fileInputStream != null;
        Reader fileReader = new InputStreamReader(fileInputStream);
        return new JsonParser().parse(fileReader).getAsJsonObject();
    }

    private static Theme parseTheme(JsonObject jsonObject) {
        Theme theme = new Theme();
        theme.setId(jsonObject.get("id").getAsInt());
        theme.setNumber(jsonObject.get("number").getAsString());
        theme.setName(jsonObject.get("name").getAsJsonObject().entrySet().toArray()[0].toString().substring(4).replace("\"", ""));
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
        rule.setNumber(jsonObject.get("number").toString().replace("\"", ""));
        String desc = jsonObject.get("description").getAsJsonObject().entrySet().toArray()[0].toString();
        rule.setDescription(desc.substring(4, desc.length() - 1));
        String content = jsonObject.get("content").getAsJsonObject().entrySet().toArray()[0].toString();
        rule.setContent(parseContent(content));
        return rule;
    }

    private static String parseContent(String content) {
        content = content.substring(4, content.length() - 1);
        content = content.replaceAll("\\{([^|]*)\\|([^|]*)\\|([^|]*)}", "![$1|$2]($3)")
                .replaceAll("\\{([^|]*)\\|([^|]*)}", "{$1}")
                .replaceAll("[*]*Навчальне відео.*", "");
        return content;
    }

}