package ru.yandex.practicum.configuration;

import org.mockito.Mockito;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import ru.yandex.practicum.repository.PostRepository;
import ru.yandex.practicum.service.PostService;

@Configuration
@ComponentScan("ru.yandex.practicum.repository")
public abstract class RepositoryConfig {
    @Bean
    @Primary
    public PostRepository postRepository() {
        return Mockito.mock(PostRepository.class);
    }

   @Bean
    @Primary
    public PostService postService(PostRepository postRepository) {
        return new PostService(postRepository);
    }

}