package ai.leadplus.application.common.utils;

import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class HtmlUtils {

    public static String convertToHtml(String plainText) {
        if (!StringUtils.hasText(plainText)) {
            return "";
        }

        String htmlEscaped = escapeHtmlCharacters(plainText);
        String normalized = normalizeLineBreaks(htmlEscaped);

        return normalized.replace("\n", "<br>");
    }

    public static String convertToHtml(String plainText, String unsubscribeUrl) {
        String body = convertToHtml(plainText);

        return addUnsubscribeFooter(body, unsubscribeUrl);
    }

    public static String addUnsubscribeFooter(String htmlContent, String unsubscribeUrl) {
        if (!StringUtils.hasText(unsubscribeUrl)) {
            return htmlContent;
        }

        String footer =
                "<br><br>" +
                        "If you no longer wish to receive these updates, you can " +
                        "<a href=\"" + unsubscribeUrl + "\">unsubscribe</a> here";
        return htmlContent + footer;
    }

    private static String escapeHtmlCharacters(String text) {
        return text.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }

    private static String normalizeLineBreaks(String text) {
        return text.replace("\r\n", "\n")
                .replace("\r", "\n");
    }
}
