package ru.yandex.practicum.model;

public class PagenablePost extends Post {
    private int totalCount;
    private int commentsCount;

    public PagenablePost(Long id, String title) {
        super(id, title);
    }

    public PagenablePost() {

    }

    public int getCommentsCount() {
        return commentsCount;
    }

    public void setCommentsCount(int commentsCount) {
        this.commentsCount = commentsCount;
    }

    public int getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(int totalCount) {
        this.totalCount = totalCount;
    }
}
