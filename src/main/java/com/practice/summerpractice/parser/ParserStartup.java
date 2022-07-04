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

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
public class ParserStartup implements ApplicationListener<ApplicationReadyEvent> {

    @SneakyThrows
    @Override
    public void onApplicationEvent(final ApplicationReadyEvent event) {
        RegisterData.main(event.getArgs());
        return;
    }

    public ExamDto parseExam() {
        try {
            RegisterData.fillExamDatabase();
        } catch (IOException e) {
            e.printStackTrace();
        }
        InputStream resourceAsStream = this.getClass().getClassLoader().getResourceAsStream("examExample.json");
        assert resourceAsStream != null;
        Reader fileReader = new InputStreamReader(resourceAsStream);
        JsonObject object = new JsonParser().parse(fileReader).getAsJsonObject();
        Gson gson = new Gson();
        return gson.fromJson(object, ExamDto.class);
    }

    public RulesDto parseFixedRules() {
        InputStream resourceAsStream = this.getClass().getClassLoader().getResourceAsStream("rulesList.json");
        assert resourceAsStream != null;
        Reader fileReader = new InputStreamReader(resourceAsStream);
        JsonObject object = new JsonParser().parse(fileReader).getAsJsonObject();
        Gson gson = new Gson();
        return gson.fromJson(object, RulesDto.class);
    }

    public RulesDto parseRules() {
        InputStream resourceAsStream = this.getClass().getClassLoader().getResourceAsStream("rulesList.json");
        assert resourceAsStream != null;
        Reader fileReader = new InputStreamReader(resourceAsStream);
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