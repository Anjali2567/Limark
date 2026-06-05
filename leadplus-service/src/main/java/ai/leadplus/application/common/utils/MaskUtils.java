package ai.leadplus.application.common.utils;

public final class MaskUtils {

    private MaskUtils() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static String maskKeepFirstAndLast(String value) {
        if (value == null || value.length() <= 2) {
            return value;
        }
        int middleLen = value.length() - 2;
        return value.charAt(0) + "*".repeat(middleLen) + value.charAt(value.length() - 1);
    }
}
