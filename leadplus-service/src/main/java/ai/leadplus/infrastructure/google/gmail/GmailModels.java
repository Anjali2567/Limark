package ai.leadplus.infrastructure.google.gmail;

import lombok.Data;
import java.util.List;

@Data
public class GmailModels {

    @Data
    public static class GmailHeader {
        private String name;
        private String value;
    }

    @Data
    public static class GmailMessageBody {
        private String data;
    }

    @Data
    public static class GmailMessagePart {
        private String mimeType;
        private GmailMessageBody body;
        private List<GmailMessagePart> parts;
    }

    @Data
    public static class GmailMessagePayload {
        private String mimeType;
        private List<GmailHeader> headers;
        private GmailMessageBody body;
        private List<GmailMessagePart> parts;
    }
}
