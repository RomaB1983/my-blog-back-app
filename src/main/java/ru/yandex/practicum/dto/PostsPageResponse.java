package ru.yandex.practicum.dto;

import java.util.List;

public class PostsPageResponse {
    private  List<PostResponse> posts;
    private boolean hasPrev;
    private  boolean hasNext;
    private  int lastPage;

    // Конструктор
    public PostsPageResponse(List<PostResponse> posts, boolean hasPrev, boolean hasNext, int lastPage) {
        this.posts = posts;
        this.hasPrev = hasPrev;
        this.hasNext = hasNext;
        this.lastPage = lastPage;
    }

    public PostsPageResponse() {

    }

    // Геттеры
    public List<PostResponse> getPosts() {
        return posts;
    }

    public boolean isHasPrev() {
        return hasPrev;
    }

    public boolean isHasNext() {
        return hasNext;
    }

    public int getLastPage() {
        return lastPage;
    }
}
