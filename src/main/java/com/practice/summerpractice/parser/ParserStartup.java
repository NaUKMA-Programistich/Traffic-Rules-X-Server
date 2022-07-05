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

    /**
     * Refills database and parses exam from it
     * @return ExamDto of parsed exam
     */
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

    /**
     * Parses rules from database
     * @return RulesDto of parsed rules
     */
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

    /**
     * Reads file from source
     * @param source - the file name
     * @return JsonObject got from reading file
     */
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

    /**
     * Parses theme and filters it for unneeded info
     * @param jsonObject - JsonObject unparsed Theme
     * @return Theme parsed from the specified JsonObject
     */
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

    /**
     * Parses rule and filters it for unneeded info
     * @param jsonObject - JsonObject unparsed Rule
     * @return Rule parsed from the specified JsonObject
     */
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

    /**
     * Parses content and filters it for links or info
     * @param content - String needed to be parsed
     * @return the specified string parsed
     */
    private static String parseContent(String content) {
        content = content.substring(4, content.length() - 1);
        content = content.replaceAll("\\{([^|}]*)\\|([^|}]*)\\|([^|}]*)}", "\n![$1|$2]($3)\n")
                .replaceAll("\\{([^|}]*)\\|([^|}]*)}", "{$1}")
                .replaceAll("[*]*Навчальне відео.*", "");
        return content;
    }

}