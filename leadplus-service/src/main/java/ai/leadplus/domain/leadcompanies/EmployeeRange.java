package ai.leadplus.domain.leadcompanies;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public enum EmployeeRange {
    RANGE_0_500("0-500", 0, 500),
    RANGE_501_1000("501-1000", 501, 1000),
    RANGE_1001_5000("1001-5000", 1001, 5000),
    RANGE_5001_10000("5001-10000", 5001, 10000),
    RANGE_10001_PLUS("10001+", 10001, Integer.MAX_VALUE);

    private final String label;
    private final int min;
    private final int max;

    EmployeeRange(String label, int min, int max) {
        this.label = label;
        this.min = min;
        this.max = max;
    }

    @JsonValue
    public String getLabel() {
        return label;
    }

    @JsonCreator
    public static EmployeeRange fromString(String input) {
        if (input == null || input.isBlank()) {
            return null;
        }

        String normalized = input.replace(",", "");

        Pattern pattern = Pattern.compile("(\\d+)");
        Matcher matcher = pattern.matcher(normalized);

        if (!matcher.find()) {
            return null;
        }

        int firstNumber = Integer.parseInt(matcher.group(1));

        for (EmployeeRange range : values()) {
            if (firstNumber >= range.min && firstNumber <= range.max) {
                return range;
            }
        }

        return null;
    }
}
