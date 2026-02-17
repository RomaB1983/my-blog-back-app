package ru.yandex.practicum.dto;

import java.util.List;

public class PostRequest {
    private String title;
    private String text;
    private List<String> tags;

    public PostRequest(String title) {
        this.title = title;
    }

    public PostRequest() {
    }

    public PostRequest(String title, String text, List<String> tags) {
        this.title = title;
        this.text = text;
        this.tags = tags;
    }

    // Геттеры и сеттеры
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }
}
