package ru.yandex.practicum.configuration;

import org.mockito.Mockito;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import ru.yandex.practicum.controller.PostController;
import ru.yandex.practicum.service.FileService;
import ru.yandex.practicum.service.PostService;

@Configuration
@ComponentScan("ru.yandex.practicum.controller")
public abstract class ControllerConfig {

    @Bean
    @Primary
    public FileService fileService() {
        return Mockito.mock(FileService.class);
    }

    @Bean
    public PostController postController(PostService postService) {
        return new PostController(postService);
    }


}