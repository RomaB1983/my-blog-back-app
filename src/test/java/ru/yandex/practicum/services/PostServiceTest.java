package ru.yandex.practicum.services;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import ru.yandex.practicum.configuration.RepositoryConfig;
import ru.yandex.practicum.dto.PostRequest;
import ru.yandex.practicum.model.Post;
import ru.yandex.practicum.repository.PostRepository;
import ru.yandex.practicum.service.PostService;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = RepositoryConfig.class)
class PostServiceTest {

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private PostService postService;

    @Test
    void testSavePost_success() {
        Post post = new Post(1L, "Test");
        PostRequest postRequest = new PostRequest(post.getTitle());
        doReturn(post).when(postRepository).save(any());
        postService.createPost(postRequest);
        doReturn(Optional.of(post)).when(postRepository).findById(1L);
        Optional<Post> savedPost = postRepository.findById(1L);

        assertNotNull(savedPost.orElse(null), "Пост не должен быть пустым");
        assertEquals("Test", savedPost.get().getTitle(), "Наименование поста должно быть Test");
    }

    @Test
    void testUpdatePost_successe() {
        Post post = new Post(2L, "");
        PostRequest postRequest = new PostRequest(post.getTitle());
        doReturn(Optional.of(post)).when(postRepository).findById(2L);
        // Выполнение метода
        doReturn(post).when(postRepository).save(any());
        postService.createPost(postRequest);
        postRequest.setTitle("Test2");
        postService.updatePost(2L,postRequest);

        Optional<Post> savedPost = postRepository.findById(2L);

        assertNotNull(savedPost.orElse(null), "Пост не должен быть пустым");
        assertEquals("Test2", savedPost.get().getTitle(), "Наименование поста должно быть Test2");
    }
}