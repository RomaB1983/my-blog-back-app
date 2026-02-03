package ru.yandex.practicum.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import ru.yandex.practicum.service.utils.FileUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
public class FileService {
    private final String UPLOAD_DIR = "uploads/";

    // 1. Обновить изображение поста
    public String updateImage(Long id, MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Файл изображения не может быть пустым");
        }

        String originalFilename = file.getOriginalFilename();
        String extension = FileUtils.getFileExtension(originalFilename);
        if (!FileUtils.isAllowedExtension(extension)) {
            throw new IllegalArgumentException("Недопустимое расширение файла: " + extension);
        }
        try {
            Path uploadDir = Paths.get(UPLOAD_DIR);
            if (!Files.exists(uploadDir)) {
                Files.createDirectories(uploadDir);
            }

            // Сохраняем файл
            String relativePath = id + "." + extension;
            Path filePath = uploadDir.resolve(relativePath);
            file.transferTo(filePath);

            return relativePath;
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    // 2. Получить изображение поста
    public byte[] getImage(Long id) {

        Path filePath = Paths.get(UPLOAD_DIR).resolve(id + ".jpg").normalize();
        if (Files.exists(filePath)) {
            try {
                return Files.readAllBytes(filePath);
            } catch (IOException e) {
                throw new RuntimeException(e.getMessage(), e);
            }
        } else {
            return new byte[0];
        }
    }

}
