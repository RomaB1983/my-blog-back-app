package ru.yandex.practicum.controller;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import ru.yandex.practicum.dto.*;
import ru.yandex.practicum.service.PostService;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PostControllerUnitTest {

    @Mock
    private PostService postService;

    @InjectMocks
    private PostController postController;

    // 1. Тест получения ленты постов
    @Test
    void testGetPosts() {

        String search = "test";
        int pageNumber = 0;
        int pageSize = 10;
        PostsPageResponse expectedResponse = new PostsPageResponse();
        when(postService.getPosts(search, pageNumber, pageSize))
                .thenReturn(expectedResponse);


        ResponseEntity<PostsPageResponse> result = postController.getPosts(search, pageNumber, pageSize);


        assertNotNull(result);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(expectedResponse, result.getBody());
    }

    // 2. Тест получения поста по ID (успешный случай)
    @Test
    void testGetPost_Success() {

        PostResponse expectedResponse = new PostResponse(1L, "Post");
        when(postService.getPost(1L)).thenReturn(expectedResponse);


        ResponseEntity<PostResponse> result = postController.getPost(1L);

        assertNotNull(result);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(expectedResponse, result.getBody());
    }

    // 3. Тест получения поста по ID (пост не найден)
    @Test
    void testGetPost_NotFound() {

        Long id = 999L;
        when(postService.getPost(id)).thenReturn(null);


        ResponseEntity<PostResponse> result = postController.getPost(id);


        assertNotNull(result);
        assertEquals(HttpStatus.NOT_FOUND, result.getStatusCode());
    }

    // 4. Тест создания нового поста
    @Test
    void testCreatePost() {

        PostRequest request = new PostRequest("New post");

        PostResponse expectedResponse = new PostResponse(1L, "New Post");

        when(postService.createPost(request)).thenReturn(expectedResponse);


        ResponseEntity<PostResponse> result = postController.createPost(request);


        assertNotNull(result);
        assertEquals(HttpStatus.CREATED, result.getStatusCode());
        assertEquals(expectedResponse, result.getBody());
    }

    // 5. Тест обновления поста (успешный случай)
    @Test
    void testUpdatePost_Success() {

        PostRequest request = new PostRequest("Updated Title");

        PostResponse expectedResponse = new PostResponse(1L, "Updated Title");

        when(postService.updatePost(1L, request)).thenReturn(expectedResponse);


        ResponseEntity<PostResponse> result = postController.updatePost(1L, request);


        assertNotNull(result);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(expectedResponse, result.getBody());
    }

    // 6. Тест обновления поста (пост не найден)
    @Test
    void testUpdatePost_NotFound() {

        Long id = 999L;
        PostRequest request = new PostRequest("New Post");
        when(postService.updatePost(id, request)).thenReturn(null);


        ResponseEntity<PostResponse> result = postController.updatePost(id, request);


        assertNotNull(result);
        assertEquals(HttpStatus.NOT_FOUND, result.getStatusCode());
    }

    // 7. Тест удаления поста (успешный случай)
    @Test
    void testDeletePost_Success() {

        Long id = 1L;
        when(postService.deletePost(id)).thenReturn(true);


        ResponseEntity<Void> result = postController.deletePost(id);


        assertNotNull(result);
        assertEquals(HttpStatus.OK, result.getStatusCode());
    }

    // 8. Тест удаления поста (пост не найден)
    @Test
    void testDeletePost_NotFound() {

        Long id = 999L;
        when(postService.deletePost(id)).thenReturn(false);


        ResponseEntity<Void> result = postController.deletePost(id);


        assertNotNull(result);
        assertEquals(HttpStatus.NOT_FOUND, result.getStatusCode());
    }

    // 9. Тест увеличения лайков (успешный случай)
    @Test
    void testIncrementLikes_Success() {

        Long id = 1L;
        int newLikes = 5;
        when(postService.incrementLikes(id)).thenReturn(newLikes);


        ResponseEntity<Integer> result = postController.incrementLikes(id);


        assertNotNull(result.getBody());
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(newLikes, result.getBody().intValue());
    }

    // 10. Тест увеличения лайков (пост не найден — исключение)
    @Test
    void testIncrementLikes_NotFound() {

        Long id = 999L;
        doThrow(new RuntimeException()).when(postService).incrementLikes(id);


        ResponseEntity<Integer> result = postController.incrementLikes(id);


        assertNotNull(result);
        assertEquals(HttpStatus.NOT_FOUND, result.getStatusCode());
    }

    // 11. Тест получения комментариев к посту
    @Test
    void testGetComments() {

        Long postId = 1L;
        CommentResponse comment1 = new CommentResponse(1L, "First Comment", postId);

        CommentResponse comment2 = new CommentResponse(2L, "Second Comment", postId);

        List<CommentResponse> expectedComments = Arrays.asList(comment1, comment2);
        when(postService.getComments(postId)).thenReturn(expectedComments);


        ResponseEntity<List<CommentResponse>> result = postController.getComments(postId);


        assertNotNull(result);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(expectedComments, result.getBody());
    }


    // 12. Тест получения конкретного комментария (успешный случай) — продолжение
    @Test
    void testGetComment_Success() {

        Long postId = 1L;
        Long commentId = 1L;

        CommentResponse expectedResponse = new CommentResponse(1L, "Test Comment", 99L);

        when(postService.getComment(postId, commentId)).thenReturn(expectedResponse);


        ResponseEntity<CommentResponse> result = postController.getComment(postId, commentId);


        assertNotNull(result);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(expectedResponse, result.getBody());
        verify(postService, times(1)).getComment(postId, commentId);
    }

    // 13. Тест получения конкретного комментария (комментарий не найден)
    @Test
    void testGetComment_NotFound() {

        when(postService.getComment(1L, 999L)).thenReturn(null);


        ResponseEntity<CommentResponse> result = postController.getComment(1L, 999L);


        assertNotNull(result);
        assertEquals(HttpStatus.NOT_FOUND, result.getStatusCode());
        assertNull(result.getBody());
    }

    // 14. Тест добавления комментария к посту
    @Test
    void testAddComment() {

        Long postId = 1L;

        CommentRequest request = new CommentRequest("Test Comment");

        CommentResponse expectedResponse = new CommentResponse(1L, "Test Comment", 99L);

        when(postService.addComment(eq(postId), any(CommentRequest.class)))
                .thenReturn(expectedResponse);


        ResponseEntity<CommentResponse> result = postController.createComment(postId, request);


        assertNotNull(result);
        assertEquals(HttpStatus.CREATED, result.getStatusCode());
        assertEquals(expectedResponse, result.getBody());
        verify(postService, times(1)).addComment(eq(postId), any(CommentRequest.class));
    }

    // 15. Тест добавления комментария (пост не найден)
    @Test
    void testAddComment_PostNotFound() {

        Long postId = 999L;

        CommentRequest request = new CommentRequest("Test Comment");
        when(postService.addComment(postId, request)).thenReturn(null);


        ResponseEntity<CommentResponse> result = postController.createComment(postId, request);


        assertNotNull(result);
        assertEquals(HttpStatus.NOT_FOUND, result.getStatusCode());
    }

    // 16. Тест обновления комментария (успешный случай)
    @Test
    void testUpdateComment_Success() {

        Long postId = 1L;
        Long commentId = 1L;

        CommentRequest request = new CommentRequest("Update Comment");

        CommentResponse expectedResponse = new CommentResponse(1L, "Update Comment", 99L);

        when(postService.updateComment(postId, commentId, request))
                .thenReturn(expectedResponse);


        ResponseEntity<CommentResponse> result = postController.updateComment(postId, commentId, request);


        assertNotNull(result);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(expectedResponse, result.getBody());
        verify(postService, times(1))
                .updateComment(postId, commentId, request);
    }

    // 17. Тест обновления комментария (комментарий не найден)
    @Test
    void testUpdateComment_NotFound() {

        Long postId = 1L;
        Long commentId = 999L;

        CommentRequest request = new CommentRequest("Update Comment");
        when(postService.updateComment(postId, commentId, request)).thenReturn(null);


        ResponseEntity<CommentResponse> result = postController.updateComment(postId, commentId, request);


        assertNotNull(result);
        assertEquals(HttpStatus.NOT_FOUND, result.getStatusCode());
    }

    // 18. Тест удаления комментария (успешный случай)
    @Test
    void testDeleteComment_Success() {

        Long postId = 1L;
        Long commentId = 1L;

        when(postService.deleteComment(postId, commentId)).thenReturn(true);


        ResponseEntity<Void> result = postController.deleteComment(postId, commentId);


        assertNotNull(result);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNull(result.getBody());
        verify(postService, times(1)).deleteComment(postId, commentId);
    }

    // 19. Тест удаления комментария (комментарий не найден)
    @Test
    void testDeleteComment_NotFound() {

        Long postId = 1L;
        Long commentId = 999L;

        when(postService.deleteComment(postId, commentId)).thenReturn(false);


        ResponseEntity<Void> result = postController.deleteComment(postId, commentId);


        assertNotNull(result);
        assertEquals(HttpStatus.NOT_FOUND, result.getStatusCode());
    }

}