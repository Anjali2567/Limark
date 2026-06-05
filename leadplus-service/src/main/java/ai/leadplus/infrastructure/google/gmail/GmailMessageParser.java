package ai.leadplus.infrastructure.google.gmail;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class GmailMessageParser {

    public static String extractEmailBody(GmailModels.GmailMessagePayload payload) {
        if (payload == null) return null;

        if (payload.getBody() != null && payload.getBody().getData() != null) {
            return decode(payload.getBody().getData());
        }

        if (payload.getParts() != null) {
            for (GmailModels.GmailMessagePart part : payload.getParts()) {
                String body = extractFromPart(part);
                if (body != null) {
                    return body;
                }
            }
        }
        return null;
    }

    private static String extractFromPart(GmailModels.GmailMessagePart part) {
        if (part == null) return null;

        if ("text/plain".equalsIgnoreCase(part.getMimeType())
                && part.getBody() != null
                && part.getBody().getData() != null) {
            return decode(part.getBody().getData());
        }

        if ("text/html".equalsIgnoreCase(part.getMimeType())
                && part.getBody() != null
                && part.getBody().getData() != null) {
            return decode(part.getBody().getData());
        }

        if (part.getParts() != null) {
            for (GmailModels.GmailMessagePart nested : part.getParts()) {
                String body = extractFromPart(nested);
                if (body != null) {
                    return body;
                }
            }
        }

        return null;
    }

    private static String decode(String base64UrlData) {
        return new String(
                Base64.getUrlDecoder().decode(base64UrlData),
                StandardCharsets.UTF_8
        );
    }
}