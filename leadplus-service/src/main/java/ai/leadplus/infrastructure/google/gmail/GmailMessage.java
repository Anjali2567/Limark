package ai.leadplus.infrastructure.google.gmail;

import lombok.Data;

@Data
public class GmailMessage {
    private String id;
    private String threadId;
    private GmailModels.GmailMessagePayload payload;
}