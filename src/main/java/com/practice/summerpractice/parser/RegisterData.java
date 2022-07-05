package com.practice.summerpractice.parser;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.practice.summerpractice.entity.*;
import com.sun.tools.javac.Main;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.zip.GZIPInputStream;

public class RegisterData {

    public static final String BEARER_TOKEN = "Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJodHRwczovL2FwaS50ZXN0cGRyLmNvbS92MS9sb2dpbi1zb2NpYWwiLCJpYXQiOjE2NTcwMDM1ODgsImV4cCI6MTY1ODIxMzE4OCwibmJmIjoxNjU3MDAzNTg4LCJqdGkiOiJlenZoMkxxN2tSZDZhRmdiIiwic3ViIjoxMTYwODgsInBydiI6IjIzYmQ1Yzg5NDlmNjAwYWRiMzllNzAxYzQwMDg3MmRiN2E1OTc2ZjcifQ.DpGPneSoBTcZGxFZKLYoDNXG3xBTltYoct70W-IHU8M";
    private static final java.util.logging.Logger log = java.util.logging.Logger.getLogger(Main.class.getName());

    public static void main(String[] args) {
        try {
            new RegisterData().fillRulesDatabase();
            fillExamDatabase();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Fills database with rules
     * @throws IOException - if an I/O or connection error occurs
     */
    public void fillRulesDatabase() throws IOException {
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

        writeRulesFile(jsonObject);
    }

    /**
     * Fills database with exam consisting of 20 random questions
     * @throws IOException - if an I/O or connection error occurs
     */
    public static void fillExamDatabase() throws IOException {
        resetExam();
        JsonObject examPDR = getExamPDR();
        HashMap<Integer, byte[]> requests = getAllCorrectAnswerRequests(examPDR);
        List<Question> questionList = new LinkedList<>();
        for (int i = 0; i < 20; i++) {
            HttpURLConnection httpPost = getConn("https://api.testpdr.com/v1/exam-questions-answer-histories", "POST", true);
            httpPost.setRequestProperty("Content-Type", "application/json");
            connectCall(requests, i, httpPost);
            JsonObject correctAnswerResponse = getAnswerResponse(httpPost);

            Question question = getNumberedQuestion(examPDR, i, correctAnswerResponse);
            questionList.add(question);

            if (!correctAnswerResponse.get("data").getAsJsonObject()
                    .get("exam").isJsonNull()) {
                resetExam();
                examPDR = getExamPDR();
                requests = getAllCorrectAnswerRequests(examPDR);
            }
            httpPost.disconnect();
        }
        writeExamFile(new ExamDto(questionList));
        log.info("Finished registering exam, ready to use");
    }

    /**
     * Writes rules to file
     * @param jsonObject - JsonObject of rules
     * @throws IOException - if an I/O error occurs
     */
    private void writeRulesFile(JsonObject jsonObject) throws IOException {
        File rules = new File("src/main/resources/rulesList.json");
        FileWriter fooWriter = new FileWriter(rules, false);

        fooWriter.write(jsonObject.toString());
        fooWriter.close();
        log.info("Rules file finished creating");
    }

    /**
     * Writes parsed exam to file
     * @param examDto - ExamDto parsed exam
     * @throws IOException - if an I/O error occurs
     */
    private static void writeExamFile(ExamDto examDto) throws IOException {
        File rules = new File("src/main/resources/examExample.json");
        FileWriter fooWriter = new FileWriter(rules, false);

        Gson gson = new Gson();
        fooWriter.write(gson.toJson(examDto));
        fooWriter.close();
    }

    /**
     * Gets connection and returns it
     * @param url - URL to connect
     * @param method - String requestMethod for connection
     * @param doOutput - boolean if it needs to do output
     * @return HttpURLConnection of set URL and method
     * @throws IOException - if a connection error occures
     */
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

    /**
     * Connects HttpURLConnection with given request
     * @param requests - HashMap of requests
     * @param i - int key associated with needed request
     * @param httpPost - HttpURLConnection connection which needs to be connected
     * @throws IOException - if an I/O or connection error occurs
     */
    private static void connectCall(HashMap<Integer, byte[]> requests, int i, HttpURLConnection httpPost) throws IOException {
        byte[] out = requests.get(i);
        try (OutputStream stream = httpPost.getOutputStream()) {
            stream.write(out, 0, out.length);
        }
        httpPost.connect();
    }

    /**
     * Gets exam by connecting through api
     * @return JsonObject containing unparsed ExamDto
     * @throws IOException - if an I/O or connection error occurs
     */
    private static JsonObject getExamPDR() throws IOException {
        HttpURLConnection http = getConn("https://api.testpdr.com/v1/exam-questions?is_training=false", "GET", false);

        String s = streamToString(new GZIPInputStream(http.getInputStream()));
        JsonObject jsonObject = new JsonParser().parse(s).getAsJsonObject();
        log.info("Getting exam response code: " + http.getResponseCode() + " " + http.getResponseMessage() + "\n      Response content : " + jsonObject);

        http.disconnect();
        return jsonObject;
    }

    /**
     * Extracts question from unparsed exam and adds correct answer
     * @param examPDR - JsonObject unparsed exam
     * @param i - int number of question
     * @param answerJson - JsonObject correct answer
     * @return Question containing correct answer
     */
    private static Question getNumberedQuestion(JsonObject examPDR, int i, JsonObject answerJson) {
        JsonObject jsonQuestion = examPDR.get("data").getAsJsonObject()
                .get("questions").getAsJsonArray()
                .get(i).getAsJsonObject();

        int id = jsonQuestion.get("id").getAsInt();
        String name = jsonQuestion.get("name").getAsJsonObject()
                .get("uk").getAsString();
        String explanation = jsonQuestion.get("explanation").getAsJsonObject().get("uk").isJsonNull() ? null :
                jsonQuestion.get("explanation").getAsJsonObject()
                        .get("uk").getAsString();
        String picture = jsonQuestion.get("picture").isJsonNull() ? null : jsonQuestion.get("picture").getAsString();

        JsonArray jsonAnswers = jsonQuestion.get("questions_answers").getAsJsonArray();
        List<Answer> answers = new LinkedList<>();
        for (int j = 0; j < jsonAnswers.size(); j++) {
            Answer answer = extractAnswerFromJson(jsonAnswers, j);
            answers.add(answer);
        }

        int correctAnswerId = answerJson.get("data").getAsJsonObject()
                .get("question_answer_history").getAsJsonObject()
                .get("correct_answer_id").getAsInt();
        return new Question(id, name, explanation, picture, answers, correctAnswerId);
    }

    /**
     * Gets response for HttpURLConnection request
     * @param httpPost - HttpURLConnection connection
     * @return JsonObject response
     * @throws IOException - if an I/O or connection error occurs
     */
    private static JsonObject getAnswerResponse(HttpURLConnection httpPost) throws IOException {
        BufferedReader reader;
        String line;
        StringBuilder responseContent = new StringBuilder();

        int status = httpPost.getResponseCode();
        if (status >= 300) {
            reader = new BufferedReader(new InputStreamReader(httpPost.getErrorStream()));
        } else {
            reader = new BufferedReader(new InputStreamReader(httpPost.getInputStream()));
        }
        while ((line = reader.readLine()) != null) {
            responseContent.append(line);
        }
        reader.close();
        log.info("Question answer response code: " + status + "\n      Response content: " + responseContent);

        return new JsonParser().parse(responseContent.toString()).getAsJsonObject();
    }

    /**
     * Extracts an answer from an array of answers
     * @param jsonAnswers - JsonArray of answers
     * @param j - int number of answer
     * @return Answer numbered answer
     */
    private static Answer extractAnswerFromJson(JsonArray jsonAnswers, int j) {
        JsonObject jsonAnswer = jsonAnswers.get(j).getAsJsonObject();
        int answerId = jsonAnswer.get("id").getAsInt();
        String answerName = jsonAnswer.get("name").getAsJsonObject().get("uk").getAsString();
        answerName = answerName.substring(0, answerName.length() - 1);
        return new Answer(answerId, answerName);
    }

    /**
     * Extracts a map of correct answer requests from exam
     * @param examPDR - JsonObject of unparsed exam
     * @return HashMap of Integer keys - answer numbers - and byte[] values - answer request content
     */
    private static HashMap<Integer, byte[]> getAllCorrectAnswerRequests(JsonObject examPDR) {
        HashMap<Integer, byte[]> responses = new HashMap<>();
        JsonArray questions = examPDR.get("data").getAsJsonObject()
                .get("questions").getAsJsonArray();

        for (int i = 0; i < 20; i++) {
            responses.put(i, getAnswerRequest(questions, i));
        }
        return responses;
    }

    /**
     * Extracts answer connection request content from exam questions
     * @param questions - JsonArray of questions
     * @param i - int question number from array
     * @return byte[] of request content
     */
    private static byte[] getAnswerRequest(JsonArray questions, int i) {
        JsonObject question = questions.get(i).getAsJsonObject();
        JsonArray questionAnswers = question.get("questions_answers").getAsJsonArray();
        JsonObject answer = questionAnswers.get(0).getAsJsonObject();
        int question_id = question.get("id").getAsInt();
        int answer_id = answer.get("id").getAsInt();

        String data = "{\"question_id\":" + question_id + ",\"questions_answer_id\":" + answer_id + ",\"is_training\":false}";
        return data.getBytes(StandardCharsets.UTF_8);
    }

    /**
     * Resets exam by connecting through api
     * @throws IOException - if an I/O or connection error occurs
     */
    private static void resetExam() throws IOException {
        HttpURLConnection httpPost = getConn("https://api.testpdr.com/v1/cancel-exam", "POST", true);

        String data = "{\"is_training\":false}";
        byte[] out = data.getBytes(StandardCharsets.UTF_8);
        OutputStream stream = httpPost.getOutputStream();
        stream.write(out);

        httpPost.connect();
        log.info(httpPost.getResponseCode() == HttpServletResponse.SC_OK ? "SUCCESSFULLY RESTARTED TEST" : "DIDNT RESTART");
    }

    /**
     * Converts an InputStream to String type
     * @param inputStream - InputStream, from url connection
     * @return String containing contents of InputStream
     * @throws IOException - if an I/O error occurs
     */
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
