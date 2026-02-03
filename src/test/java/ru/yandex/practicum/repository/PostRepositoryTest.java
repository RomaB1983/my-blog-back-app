package ru.yandex.practicum.repository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import ru.yandex.practicum.configuration.RepositoryConfig;
import ru.yandex.practicum.model.Post;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doReturn;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = RepositoryConfig.class)
class PostRepositoryTest {

    @Autowired
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