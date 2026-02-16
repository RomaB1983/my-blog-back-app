package ru.yandex.practicum.controller;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.yandex.practicum.dto.*;
import ru.yandex.practicum.service.PostService;
import tools.jackson.databind.ObjectMapper;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;


@WebMvcTest(PostController.class)
class PostControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PostService postService;

    @Autowired
    ObjectMapper objectMapper;

    // 1. Тест получения ленты постов
    @Test
    void testGetPosts() throws Exception {

        PostsPageResponse response = new PostsPageResponse();
        when(postService.getPosts("test", 0, 10)).thenReturn(response);

        mockMvc.perform(get("/api/posts")
                        .param("search", "test")
                        .param("pageNumber", "0")
                        .param("pageSize", "10"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        ;
    }

    // 2. Тест получения поста по ID (успешный случай)
    @Test
    void testGetPost_Success() throws Exception {

        PostResponse response = new PostResponse(1L, "Post");

        when(postService.getPost(1L)).thenReturn(response);

        mockMvc.perform(get("/api/posts/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("Post"));
    }

    // 3. Тест получения поста по ID (пост не найден)
    @Test
    void testGetPost_NotFound() throws Exception {

        when(postService.getPost(999L)).thenReturn(null);

        mockMvc.perform(get("/api/posts/999"))
                .andExpect(status().isNotFound());
    }

    // 4. Тест создания нового поста
    @Test
    void testCreatePost() throws Exception {

        PostRequest request = new PostRequest("New Post");

        PostResponse response = new PostResponse(1L, "New Post");
        when(postService.createPost(any(PostRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/posts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("New Post"));
    }

    // 5. Тест обновления поста (успешный случай)
    @Test
    void testUpdatePost_Success() throws Exception {

        PostRequest request = new PostRequest("Updated Title");

        PostResponse response = new PostResponse(1L, "Updated Title");

        when(postService.updatePost(eq(1L), any(PostRequest.class)))
                .thenReturn(response);

        mockMvc.perform(put("/api/posts/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("Updated Title"));
    }

    // 6. Тест обновления поста (пост не найден)
    @Test
    void testUpdatePost_NotFound() throws Exception {

        PostRequest request = new PostRequest();
        when(postService.updatePost(999L, request)).thenReturn(null);


        mockMvc.perform(put("/api/posts/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    // 7. Тест удаления поста (успешный случай)
    @Test
    void testDeletePost_Success() throws Exception {

        when(postService.deletePost(1L)).thenReturn(true);


        mockMvc.perform(delete("/api/posts/1"))
                .andExpect(status().isOk());
    }

    // 8. Тест удаления поста (пост не найден)
    @Test
    void testDeletePost_NotFound() throws Exception {

        when(postService.deletePost(999L)).thenReturn(false);


        mockMvc.perform(delete("/api/posts/999"))
                .andExpect(status().isNotFound());
    }

    // 9. Тест увеличения лайков (успешный случай)
    @Test
    void testIncrementLikes_Success() throws Exception {

        when(postService.incrementLikes(1L)).thenReturn(5);


        mockMvc.perform(post("/api/posts/1/likes"))
                .andExpect(status().isOk())
                .andExpect(content().string("5"));
    }

    // 10. Тест увеличения лайков (пост не найден — исключение)
    @Test
    void testIncrementLikes_NotFound() throws Exception {

        doThrow(new RuntimeException()).when(postService).incrementLikes(999L);


        mockMvc.perform(post("/api/posts/999/likes"))
                .andExpect(status().isNotFound());
    }

    // 11. Тест получения комментариев к посту
    @Test
    void testGetComments() throws Exception {

        Long postId = 99L;
        CommentResponse comment1 = new CommentResponse(1L, "First Comment", postId);

        CommentResponse comment2 = new CommentResponse(2L, "Second Comment", postId);

        List<CommentResponse> comments = Arrays.asList(comment1, comment2);
        when(postService.getComments(postId)).thenReturn(comments);


        mockMvc.perform(get("/api/posts/{id}/comments", postId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].text").value("First Comment"))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].text").value("Second Comment"));
    }


    // 12. Тест получения конкретного комментария (успешный случай) — продолжение
    @Test
    void testGetComment_Success() throws Exception {

        Long postId = 1L;
        Long commentId = 1L;

        CommentResponse response = new CommentResponse(commentId, "Test comment", postId);
        when(postService.getComment(postId, commentId)).thenReturn(response);


        mockMvc.perform(get("/api/posts/{postId}/comments/{commentId}", postId, commentId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(commentId))
                .andExpect(jsonPath("$.text").value("Test comment"));
    }

    // 13. Тест получения конкретного комментария (комментарий не найден)
    @Test
    void testGetComment_NotFound() throws Exception {

        Long postId = 1L;
        Long commentId = 999L;

        when(postService.getComment(postId, commentId)).thenReturn(null);


        mockMvc.perform(get("/api/posts/{postId}/comments/{commentId}", postId, commentId))
                .andExpect(status().isNotFound());
    }

    // 14. Тест добавления комментария к посту
    @Test
    void testCreateComment() throws Exception {

        Long postId = 1L;

        CommentRequest request = new CommentRequest("New comment");

        CommentResponse response = new CommentResponse(1L, "New comment", postId);

        when(postService.addComment(eq(postId), any(CommentRequest.class)))
                .thenReturn(response);


        mockMvc.perform(post("/api/posts/{postId}/comments", postId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.text").value("New comment"));
    }

    // 15. Тест добавления комментария (пост не найден)
    @Test
    void testCreateComment_PostNotFound() throws Exception {

        Long postId = 999L;

        CommentRequest request = new CommentRequest("New comment");
        when(postService.addComment(postId, request)).thenReturn(null);


        mockMvc.perform(post("/api/posts/{postId}/comments", postId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    // 16. Тест обновления комментария (успешный случай)
    @Test
    void testUpdateComment_Success() throws Exception {

        Long postId = 1L;
        Long commentId = 1L;

        CommentRequest request = new CommentRequest("Updated comment");

        CommentResponse response = new CommentResponse(commentId, "Updated comment", postId);

        when(postService.updateComment(eq(postId), eq(commentId), any(CommentRequest.class)))
                .thenReturn(response);


        mockMvc.perform(put("/api/posts/{postId}/comments/{commentId}", postId, commentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(commentId))
                .andExpect(jsonPath("$.text").value("Updated comment"));
    }

    // 17. Тест обновления комментария (комментарий не найден)
    @Test
    void testUpdateComment_NotFound() throws Exception {

        Long postId = 1L;
        Long commentId = 999L;

        CommentRequest request = new CommentRequest("Updated comment");
        when(postService.updateComment(postId, commentId, request)).thenReturn(null);


        mockMvc.perform(put("/api/posts/{postId}/comments/{commentId}", postId, commentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    // 18. Тест удаления комментария (успешный случай)
    @Test
    void testDeleteComment_Success() throws Exception {

        Long postId = 1L;
        Long commentId = 1L;

        when(postService.deleteComment(postId, commentId)).thenReturn(true);


        mockMvc.perform(delete("/api/posts/{postId}/comments/{commentId}", postId, commentId))
                .andExpect(status().isOk());
    }

    // 19. Тест удаления комментария (комментарий не найден)
    @Test
    void testDeleteComment_NotFound() throws Exception {

        Long postId = 1L;
        Long commentId = 999L;

        when(postService.deleteComment(postId, commentId)).thenReturn(false);


        mockMvc.perform(delete("/api/posts/{postId}/comments/{commentId}", postId, commentId))
                .andExpect(status().isNotFound());
    }
}