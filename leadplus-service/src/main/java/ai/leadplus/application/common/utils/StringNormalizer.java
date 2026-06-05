package ai.leadplus.application.common.utils;

import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public final class StringNormalizer {


    public static String normalize(String input) {
        if (!StringUtils.hasText(input)) {
            return "";
        }

        String normalized = input.trim().replaceAll("\\s+", " ");

        normalized = normalized.toLowerCase();

        return Character.toUpperCase(normalized.charAt(0)) + normalized.substring(1);
    }

    public static String normalizeEachWord(String input) {
        if (!StringUtils.hasText(input)) {
            return "";
        }

        String normalized = input.trim().replaceAll("\\s+", " ").toLowerCase();

        return Arrays.stream(normalized.split(" "))
                .map(word -> Character.toUpperCase(word.charAt(0)) + word.substring(1))
                .collect(Collectors.joining(" "));
    }

    public static String extractAndCapitalizeLastWord(String input) {
        if (!StringUtils.hasText(input)) {
            return "";
        }
        String trimmed = input.trim().replaceAll("\\s+", " ");
        String[] words = trimmed.split(" ");
        String lastWord = words[words.length - 1].toLowerCase();

        return Character.toUpperCase(lastWord.charAt(0)) + lastWord.substring(1);
    }

}
