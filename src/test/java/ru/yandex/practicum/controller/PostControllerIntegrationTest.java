package ru.yandex.practicum.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.yandex.practicum.dto.PostRequest;
import ru.yandex.practicum.model.Post;
import ru.yandex.practicum.repository.PostRepository;
import ru.yandex.practicum.service.PostService;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest({PostController.class, PostService.class})
class PostControllerIntegrationTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PostController postController;

    @MockitoBean
    private PostRepository postRepository;

    @Test
    public void testGetPost() throws Exception {
        Post post = new Post(1L, "Test");
        PostRequest postRequest = new PostRequest(post.getTitle());
        doReturn(post).when(postRepository).save(any());
        postController.createPost(postRequest);
        doReturn(Optional.of(post)).when(postRepository).findById(1L);
        mockMvc.perform(get("/api/posts/1"))
                .andExpect(status().isOk());
    }

}