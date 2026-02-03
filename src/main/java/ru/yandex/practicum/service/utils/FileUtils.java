package ru.yandex.practicum.service.utils;

public class FileUtils {
    public static String getFileExtension(String filename) {
        int dotIndex = filename.lastIndexOf('.');
        if (dotIndex == -1 || dotIndex == 0 || dotIndex == filename.length() - 1) {
            return "";
        }
        return filename.substring(dotIndex + 1).toLowerCase();
    }

    public static boolean isAllowedExtension(String extension) {
        return extension.equals("jpg");
    }
}
