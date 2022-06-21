package com.practice.summerpractice.parser;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

@Component
public class ParserStartup implements ApplicationListener<ApplicationReadyEvent> {

    @Override
    public void onApplicationEvent(final ApplicationReadyEvent event) {
        // TODO проверить что нет теории в бд сейчас
        // спарсить из файла rules.json теорию
        // айди раздела - название раздела - подпункты
    }
}