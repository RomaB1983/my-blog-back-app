package ru.yandex.practicum.controller;


import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.dto.*;
import ru.yandex.practicum.service.PostService;

import java.util.List;

@RestController
@RequestMapping("/api/posts")
@CrossOrigin
public class PostController {

    private final PostService postService;

    public PostController(PostService postService) {
        this.postService = postService;
    }

    // 1. Получение ленты постов (с пагинацией и поиском)
    @GetMapping
    public ResponseEntity<PostsPageResponse> getPosts(
            @RequestParam String search,
            @RequestParam int pageNumber,
            @RequestParam int pageSize) {

        PostsPageResponse response = postService.getPosts(search, pageNumber, pageSize);
        return ResponseEntity.ok(response);
    }

    // 2. Получение поста по ID
    @GetMapping("/{id}")
    public ResponseEntity<PostResponse> getPost(@PathVariable Long id) {
        PostResponse response = postService.getPost(id);
        if (response == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(response);
    }

    // 3. Создание нового поста
    @PostMapping
    public ResponseEntity<PostResponse> createPost(@RequestBody PostRequest request) {
        PostResponse response = postService.createPost(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // 4. Обновление поста
    @PutMapping("/{id}")
    public ResponseEntity<PostResponse> updatePost(
            @PathVariable Long id,
            @RequestBody PostRequest request) {

        PostResponse response = postService.updatePost(id, request);
        if (response == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(response);
    }

    // 5. Удаление поста
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePost(@PathVariable Long id) {
        if (!postService.deletePost(id)) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok().build();
    }

    // 6. Увеличение количества лайков
    @PostMapping("/{id}/likes")
    public ResponseEntity<Integer> incrementLikes(@PathVariable Long id) {
        try {
            int newLikes = postService.incrementLikes(id);
            return ResponseEntity.ok(newLikes);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

   // 7. Получение комментариев к посту
    @GetMapping("/{postId}/comments")
    public ResponseEntity<List<CommentResponse>> getComments(@PathVariable Long postId) {
        List<CommentResponse> responses = postService.getComments(postId);
        return ResponseEntity.ok(responses);
    }

    // 8. Получение конкретного комментария
    @GetMapping("/{postId}/comments/{commentId}")
    public ResponseEntity<CommentResponse> getComment(
            @PathVariable Long postId,
            @PathVariable Long commentId) {

        CommentResponse response = postService.getComment(postId, commentId);
        if (response == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(response);
    }

    // 9. Добавление комментария к посту
    @PostMapping("/{postId}/comments")
    public ResponseEntity<CommentResponse> createComment(
            @PathVariable Long postId,
            @RequestBody CommentRequest request) {

        CommentResponse response = postService.addComment(postId, request);
        if (response == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // 10. Обновление комментария
    @PutMapping("/{postId}/comments/{commentId}")
    public ResponseEntity<CommentResponse> updateComment(
            @PathVariable Long postId,
            @PathVariable Long commentId,
            @RequestBody CommentRequest request) {


        CommentResponse response = postService.updateComment(postId, commentId, request);
        if (response == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(response);
    }

    // 11. Удаление комментария
    @DeleteMapping("/{postId}/comments/{commentId}")
    public ResponseEntity<Void> deleteComment(
            @PathVariable Long postId,
            @PathVariable Long commentId) {

        if (!postService.deleteComment(postId, commentId)) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok().build();
    }
}