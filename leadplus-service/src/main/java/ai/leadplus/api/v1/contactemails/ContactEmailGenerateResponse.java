package ai.leadplus.api.v1.contactemails;

import ai.leadplus.application.contactemails.ContactEmailCompletion;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContactEmailGenerateResponse {

    private String subject;
    private String body;

    public static ContactEmailGenerateResponse fromCompletion(ContactEmailCompletion completion) {
        return ContactEmailGenerateResponse.builder()
                .subject(completion.getSubject())
                .body(completion.getBody())
                .build();
    }
}

