package com.journaldev.jsf.model;

import java.util.List;

public class Vote {

    private String title;
    private String question;
    private List<String> options;

    public Vote(String title, String question, List<String> options) {
        this.title = title;
        this.question = question;
        this.options = options;
    }


    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public List<String> getOptions() {
        return options;
    }

    public void setOptions(List<String> options) {
        this.options = options;
    }

    @Override
    public String toString() {
        return "Vote{" +
                "title='" + title + '\'' +
                ", question='" + question + '\'' +
                ", options=" + options +
                '}';
    }
}
