package com.practice.summerpractice.parser;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.zip.GZIPInputStream;

public class RegisterData {

    public static final String BEARER_TOKEN = "Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJodHRwczovL2FwaS50ZXN0cGRyLmNvbS92MS9yZWdpc3RlciIsImlhdCI6MTY1NTc5MzAwMCwiZXhwIjoxNjU3MDAyNjAwLCJuYmYiOjE2NTU3OTMwMDAsImp0aSI6ImNKaWJ4OEpocUh4UmFDQWEiLCJzdWIiOjExNjA4OCwicHJ2IjoiMjNiZDVjODk0OWY2MDBhZGIzOWU3MDFjNDAwODcyZGI3YTU5NzZmNyJ9.pXy46zuGsIL0aiwHH3TL9uofCg6j-mluRRTxqDDCCHk";

    static class QueryParams {
        private int question_id;
        private int questions_answer_id;
        private final boolean is_training = false;

        public QueryParams(int question_id, int questions_answer_id) {
            this.question_id = question_id;
            this.questions_answer_id = questions_answer_id;
        }
    }

    public static void main(String[] args) {
        try {
            fillQuestionsDatabase();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void fillRulesDatabase() throws IOException {
        URL url = new URL("https://pdr.infotech.gov.ua/_next/data/_dcYOFIuVtVZQqHCIerrL/theory/rules/1.json");
        HttpURLConnection http = (HttpURLConnection) url.openConnection();
        http.setRequestProperty("Accept", "application/json");

        String s = streamToString(http.getInputStream());
        JsonObject jsonObject = new JsonParser().parse(s).getAsJsonObject();
        jsonObject = jsonObject.get("pageProps").getAsJsonObject()
                .get("initialState").getAsJsonObject()
                .get("theory").getAsJsonObject()
                .get("rules").getAsJsonObject()
                .get("themes").getAsJsonObject();

        File rules = new File("C:\\summer 22\\traffic\\src\\main\\resources\\rulesList.json");
        FileWriter fooWriter = new FileWriter(rules, false);

        fooWriter.write(jsonObject.toString());
        fooWriter.close();
    }

    private static void fillQuestionsDatabase() throws IOException {
        cancelExam();

        byte[] out = getPostData();

        HttpURLConnection httpPost = getConn("https://api.testpdr.com/v1/exam-questions-answer-histories", "POST", true);

        OutputStream stream = httpPost.getOutputStream();
        stream.write(out);

        //System.out.println(httpPost.getHeaderFields().toString());
        httpPost.connect();
        System.out.println(httpPost.getResponseCode() + " LIKE WHY");

        String s2 = streamToString(new GZIPInputStream(httpPost.getInputStream()));
        System.out.println(s2);

        httpPost.disconnect();

        /*File questions = new File("C:\\summer 22\\traffic\\src\\main\\resources\\questions.json");
        FileWriter fooWriter = new FileWriter(questions, false);

        fooWriter.write("New Contents\n");
        fooWriter.close();*/
    }

    private static byte[] getPostData() throws IOException {
        JsonObject jsonObject = getExamPDR();

        JsonArray questions = jsonObject.get("data").getAsJsonObject()
                .get("questions").getAsJsonArray();
        JsonObject question = questions.get(0).getAsJsonObject();
        JsonArray questionAnswers = question.get("questions_answers").getAsJsonArray();
        JsonObject answer = questionAnswers.get(0).getAsJsonObject();
        int question_id = question.get("id").getAsInt();
        int answer_id = answer.get("id").getAsInt();

        String data = "{\"question_id\":" + question_id + ",\"questions_answer_id\":" + answer_id + ",\"is_training\":false}";

        return data.getBytes(StandardCharsets.UTF_8);
    }

    private static JsonObject getExamPDR() throws IOException {
        URL url = new URL("https://api.testpdr.com/v1/exam-questions?is_training=false");
        HttpURLConnection http = (HttpURLConnection) url.openConnection();
        http.setRequestProperty("Authorization", BEARER_TOKEN);
        http.setRequestProperty("Accept", "application/json");
        http.setRequestProperty("Accept-Encoding", "gzip");

        System.out.println(http.getResponseCode() + " " + http.getResponseMessage());
        String s = streamToString(new GZIPInputStream(http.getInputStream()));
        JsonObject jsonObject = new JsonParser().parse(s).getAsJsonObject();
        System.out.println(jsonObject);
        http.disconnect();
        return jsonObject;
    }

    private static void cancelExam() throws IOException {
        HttpURLConnection httpPost = getConn("https://api.testpdr.com/v1/cancel-exam", "POST", true);

        String data = "{\"is_training\":false}";
        byte[] out = data.getBytes(StandardCharsets.UTF_8);

        OutputStream stream = httpPost.getOutputStream();
        stream.write(out);

        httpPost.connect();

        System.out.println(httpPost.getResponseCode() == HttpServletResponse.SC_OK ? "SUCCESSFULLY RESTARTED TEST" : "DIDNT RESTART");
    }

    private static HttpURLConnection getConn(String url, String method, boolean doOutput) throws IOException {
        URL postUrl = new URL(url);
        HttpURLConnection httpPost = (HttpURLConnection) postUrl.openConnection();
        httpPost.setRequestMethod(method);
        httpPost.setDoOutput(doOutput);
        httpPost.setRequestProperty("Accept", "application/json");
        httpPost.setRequestProperty("Authorization", BEARER_TOKEN);
        httpPost.setRequestProperty("Accept-Encoding", "gzip");
        return httpPost;
    }

    private static String streamToString(InputStream inputStream) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

        StringBuilder sb = new StringBuilder();
        String line;

        while ((line = reader.readLine()) != null) {
            sb.append(line).append("\n");
        }

        reader.close();

        return sb.toString();
    }
}
