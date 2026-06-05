package ai.leadplus.application.common.utils;

import ai.leadplus.application.common.RecipientDto;
import ai.leadplus.domain.common.Recipient;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;

public final class RecipientUtils {

    private RecipientUtils() {
    }

    public static List<RecipientDto> mergeRecipients(
            List<RecipientDto> first,
            List<RecipientDto> second
    ) {
        return Stream.of(
                        CollectionUtils.isEmpty(first) ? List.<RecipientDto>of() : first,
                        CollectionUtils.isEmpty(second) ? List.<RecipientDto>of() : second
                )
                .flatMap(List::stream)
                .collect(
                        java.util.stream.Collectors.toMap(
                                r -> normalizeEmail(r.getEmail()),
                                r -> r,
                                (r1, r2) -> r1
                        )
                )
                .values()
                .stream()
                .toList();
    }

    private static String normalizeEmail(String email) {
        return StringUtils.hasText(email)
                ? email.toLowerCase(Locale.ROOT)
                : null;
    }

    /**
     * Maps recipients to DTOs.
     * Null is preserved to differentiate from an explicit empty override.
     */
    public static List<RecipientDto> mapToDto(List<Recipient> recipients) {
        if (recipients == null) {
            return null;
        }
        return recipients.stream()
                .map(RecipientDto::fromEntity)
                .toList();
    }

    /**
     * Maps recipients to DTOs.
     * Null is preserved to differentiate from an explicit empty override.
     */
    public static List<Recipient> mapToEntity(List<RecipientDto> recipients) {
        if (recipients == null) {
            return null;
        }
        return recipients.stream()
                .map(RecipientDto::toEntity)
                .toList();
    }
}
