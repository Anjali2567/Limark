package ai.leadplus.infrastructure.google.gmail;

import lombok.Data;

import java.util.List;

@Data
public class GmailThreadResponse {
    private String id;
    private List<GmailMessage> messages;
}