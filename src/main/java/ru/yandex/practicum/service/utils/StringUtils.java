package ru.yandex.practicum.service.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class StringUtils {
    /**
     * Разделяет входную строку на две:
     * - строку c заколовками (слова без # склеиваются через пробел)
     * - строку с тегами (склеиваются через запятую)
     *
     * @param input входная строка
     * @return мапу с двумя элементами: строка тэгов, строка заголовков
     */
    public static Map<String, String> splitByHash(String input) {
        if (input == null || input.trim().isEmpty()) {
            return Map.of("tags", "", "titles", "");
        }
        // Удаляем лишние пробелы и непечатаемые символы.
        // Разбиваем по пробелам
        String[] parts = input.replaceAll("\\s+", " ").trim().split(" ");

        List<String> hashList = new ArrayList<>();
        List<String> othersList = new ArrayList<>();

        for (String part : parts) {
            if (part.startsWith("#")) {
                hashList.add(part);
            } else {
                othersList.add(part);
            }
        }

        return Map.of(
                "tags", String.join("'",hashList),
                "titles", String.join("",othersList)
        );
    }
}