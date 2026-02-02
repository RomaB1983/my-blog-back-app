package ru.yandex.practicum.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import ru.yandex.practicum.dto.*;
import ru.yandex.practicum.model.Comment;
import ru.yandex.practicum.model.Post;
import ru.yandex.practicum.repository.PostRepository;
import ru.yandex.practicum.service.utils.FileUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;

@Service
public class PostService {

    private final PostRepository postRepository;

    public PostService(PostRepository postRepository) {
        this.postRepository = postRepository;
    }

    // 1. Получить ленту постов с поиском, пагинацией
    public PostsPageResponse getPosts(String search, int pageNumber, int pageSize) {
        var page = postRepository.findPosts(search, pageNumber, pageSize);
        page.getPosts().forEach(p -> {
            if (p.getText() != null && !p.getText().isEmpty() && p.getText().length() > 128) {
                p.setText(p.getText().substring(0, 127) + "...");
            }
        });
        return page;
    }

    // 2. Получить пост по ID
    public PostResponse getPost(Long id) {
        Optional<Post> postOpt = postRepository.findById(id);
        if (postOpt.isEmpty()) {
            return null;
        }

        Post post = postOpt.get();
        int commentsCount = postRepository.getCommentsCount(id);

        return new PostResponse(
                post.getId(),
                post.getTitle(),
                post.getText(),
                post.getTags(),
                post.getLikesCount(),
                commentsCount
        );
    }

    // 3. Создать новый пост
    @Transactional
    public PostResponse createPost(PostRequest request) {
        Post post = new Post();
        post.setTitle(request.getTitle());
        post.setText(request.getText());
        post.setTags(request.getTags());
        post.setLikesCount(0);

        Post savedPost = postRepository.save(post);
        int commentsCount = 0;

        return new PostResponse(
                savedPost.getId(),
                savedPost.getTitle(),
                savedPost.getText(),
                savedPost.getTags(),
                savedPost.getLikesCount(),
                commentsCount
        );
    }

    // 4. Обновить пост
    @Transactional
    public PostResponse updatePost(Long id, PostRequest request) {
        Optional<Post> postOpt = postRepository.findById(id);
        if (postOpt.isEmpty()) {
            return null;
        }

        Post post = postOpt.get();
        post.setTitle(request.getTitle());
        post.setText(request.getText());
        post.setTags(request.getTags());

        postRepository.update(post);
        return new PostResponse(
                id,
                request.getTitle(),
                request.getText(),
                request.getTags(),
                post.getLikesCount(),
                postRepository.getCommentsCount(id)
        );
    }

    // 5. Удалить пост
    @Transactional
    public boolean deletePost(Long id) {
        return postRepository.deleteById(id);
    }

    // 6. Увеличить количество лайков
    @Transactional
    public int incrementLikes(Long id) {
        return postRepository.incrementLikes(id);
    }

    // 7. Получить список комментариев к посту
    public List<CommentResponse> getComments(Long postId) {
        List<Comment> comments = postRepository.findCommentsByPostId(postId);
        return comments.stream()
                .map(comment -> new CommentResponse(
                        comment.getId(),
                        comment.getText(),
                        comment.getPostId()
                ))
                .collect(java.util.stream.Collectors.toList());
    }

    // 8. Получить конкретный комментарий
    public CommentResponse getComment(Long postId, Long commentId) {
        Optional<Comment> commentOpt = postRepository.findCommentById(commentId);
        if (commentOpt.isEmpty() || !commentOpt.get().getPostId().equals(postId)) {
            return null;
        }

        Comment comment = commentOpt.get();
        return new CommentResponse(comment.getId(), comment.getText(), comment.getPostId());
    }

    // 9. Добавить комментарий к посту
    @Transactional
    public CommentResponse addComment(Long postId, CommentRequest request) {
        if (!postRepository.existsById(postId)) {
            return null;
        }

        Comment comment = new Comment();
        comment.setPostId(postId);
        comment.setText(request.getText());

        Long commentId = postRepository.insertComment(comment);
        return new CommentResponse(commentId, request.getText(), postId);
    }

    // 10. Обновить комментарий
    @Transactional
    public CommentResponse updateComment(Long postId, Long commentId, CommentRequest request) {
        Optional<Comment> commentOpt = postRepository.findCommentById(commentId);
        if (commentOpt.isEmpty() || !commentOpt.get().getPostId().equals(postId)) {
            return null;
        }

        Comment comment = commentOpt.get();
        comment.setText(request.getText());
        postRepository.updateComment(comment);

        return new CommentResponse(commentId, request.getText(), postId);
    }

    // 11. Удалить комментарий
    @Transactional
    public boolean deleteComment(Long postId, Long commentId) {
        Optional<Comment> commentOpt = postRepository.findCommentById(commentId);
        if (commentOpt.isEmpty() || !commentOpt.get().getPostId().equals(postId)) {
            return false;
        }
        return postRepository.deleteComment(commentId);
    }

}