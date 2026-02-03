package ru.yandex.practicum.dto;

import java.util.List;

public class PostResponse {
    private final Long id;
    private final String title;
    private  String text;
    private final List<String> tags;
    private final Integer likesCount;
    private final Integer commentsCount;

    // Конструктор
    public PostResponse(Long id, String title, String text, List<String> tags,
                        Integer likesCount, Integer commentsCount) {
        this.id = id;
        this.title = title;
        this.text = text;
        this.tags = tags;
        this.likesCount = likesCount;
        this.commentsCount = commentsCount;
    }

    // Геттеры (сеттеры не нужны для ответа)
    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
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

    public Integer getLikesCount() {
        return likesCount;
    }

    public Integer getCommentsCount() {
        return commentsCount;
    }
}
