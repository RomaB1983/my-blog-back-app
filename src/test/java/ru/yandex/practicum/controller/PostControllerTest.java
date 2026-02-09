package ru.yandex.practicum.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import ru.yandex.practicum.dto.PostRequest;
import ru.yandex.practicum.model.Post;
import ru.yandex.practicum.repository.PostRepository;
import ru.yandex.practicum.service.PostService;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;

@SpringBootTest(classes = {PostController.class, PostService.class})
class PostControllerTest {

    @Autowired
    private PostController postController;

    @MockitoBean
    private PostRepository postRepository;

    @Test
    void testSavePost_success() {
        Post post = new Post(1L, "Test");
        PostRequest postRequest = new PostRequest(post.getTitle());
        doReturn(post).when(postRepository).save(any());
        postController.createPost(postRequest);
        doReturn(Optional.of(post)).when(postRepository).findById(1L);
        Optional<Post> savedPost = postRepository.findById(1L);

        assertNotNull(savedPost.orElse(null), "Пост не должен быть пустым");
        assertEquals("Test", savedPost.get().getTitle(), "Наименование поста должно быть Test");
    }

    @Test
    void testUpdatePost_success() {
        Post post = new Post(2L, "");
        PostRequest postRequest = new PostRequest(post.getTitle());
        doReturn(Optional.of(post)).when(postRepository).findById(2L);
        // Выполнение метода
        doReturn(post).when(postRepository).save(any());
        postController.createPost(postRequest);
        postRequest.setTitle("Test");
        postController.updatePost(2L, postRequest);

        Optional<Post> savedPost = postRepository.findById(2L);

        assertNotNull(savedPost.orElse(null), "Пост не должен быть пустым");
        assertEquals("Test", savedPost.get().getTitle(), "Наименование поста должно быть Test");
    }
}