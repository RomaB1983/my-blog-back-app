package ru.yandex.practicum.model;

import java.util.ArrayList;
import java.util.List;

public class Post {

    private Long id;
    private String title;
    private String text;

    private List<String> tags = new ArrayList<>();
    private int likesCount = 0;

    public Post() {
    }


    public Post(Long id, String title) {
        this.id = id;
        this.title = title;
    }

    // Геттеры и сеттеры
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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

    public int getLikesCount() {
        return likesCount;
    }

    public void setLikesCount(int likesCount) {
        this.likesCount = likesCount;
    }

}

