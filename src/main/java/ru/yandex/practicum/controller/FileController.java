package ru.yandex.practicum.controller;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ru.yandex.practicum.service.FileService;

@RestController
@RequestMapping("/api/posts")
@CrossOrigin
public class FileController {
    private final FileService fileService;

    public FileController( FileService fileService) {
        this.fileService = fileService;
    }
    // 1. Загрузка изображения для поста
    @PutMapping("/{id}/image")
    public ResponseEntity<Void> updateImage(
            @PathVariable Long id,
            @RequestParam("image") MultipartFile file) {
        if (fileService.updateImage(id, file) != null) {
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }

    // 2. Получение изображения поста
    @GetMapping("/{id}/image")
    public ResponseEntity<byte[]> getImage(@PathVariable Long id) {
        byte[] image = fileService.getImage(id);
        if (image.length == 0) {
            return ResponseEntity.notFound().build();
        }
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.IMAGE_JPEG);

        return new ResponseEntity<>(image, headers, HttpStatus.OK);
    }

}
