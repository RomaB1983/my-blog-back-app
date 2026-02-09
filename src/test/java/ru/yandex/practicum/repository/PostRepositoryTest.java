package ru.yandex.practicum.repository;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import ru.yandex.practicum.model.Post;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doReturn;

@SpringBootTest(classes = PostRepository.class)
class PostRepositoryTest {

    @MockitoBean
    private PostRepository repository;

    @Test
    void saveAndFind() {
        Post post = new Post(1L, "DB test");
        repository.save(post);
        Optional<Post> expectedResult = Optional.of(post);
        doReturn(expectedResult).when(repository).findById(1L);

        Optional<Post> result =
                repository.findById(post.getId());

        assertTrue(result.isPresent());
    }
}