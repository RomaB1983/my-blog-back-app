package ru.yandex.practicum.model;

public class Comment {
    private Long id;
    private Long postId;

    private String text;

    public Comment() {
    }

    public Comment(Long id, Long postId, String text) {
        this.id = id;
        this.postId = postId;
        this.text = text;
    }

    // Геттеры и сеттеры
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getPostId() { return postId; }
    public void setPostId(Long postId) { this.postId = postId; }

    public String getText() { return text; }
    public void setText(String text) { this.text = text; }
}
