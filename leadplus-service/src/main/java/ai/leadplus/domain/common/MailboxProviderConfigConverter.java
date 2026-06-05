package ai.leadplus.domain.common;

import ai.leadplus.domain.mailboxes.MailboxProviderConfig;
import com.fasterxml.jackson.databind.type.TypeFactory;
import jakarta.persistence.Converter;

@Converter
public class MailboxProviderConfigConverter extends TypedJsonConverter<MailboxProviderConfig> {
    public MailboxProviderConfigConverter() {
        super(TypeFactory.defaultInstance().constructType(MailboxProviderConfig.class));
    }
}
