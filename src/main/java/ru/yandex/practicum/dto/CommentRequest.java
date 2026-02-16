package ru.yandex.practicum.dto;

public class CommentRequest {
    private final String text;

    public CommentRequest(String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }
}
