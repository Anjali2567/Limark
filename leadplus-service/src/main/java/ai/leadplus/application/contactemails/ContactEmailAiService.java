package ai.leadplus.application.contactemails;

import ai.leadplus.application.common.utils.FileReader;
import ai.leadplus.infrastructure.springai.SpringAiClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ContactEmailAiService {

    private static final String SYSTEM_PROMPT = FileReader.readFileContentFromClasspath("prompts/contact-email-generator.md");

    private final SpringAiClient springAiClient;

    public ContactEmailCompletion generateEmail(String userPrompt)  {
        return springAiClient.getChatCompletion(userPrompt, SYSTEM_PROMPT, ContactEmailCompletion.class);
    }
}
