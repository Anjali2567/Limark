package ai.leadplus.domain.common;

import ai.leadplus.domain.campaigns.SendingWindow;
import com.fasterxml.jackson.databind.type.TypeFactory;
import jakarta.persistence.Converter;

@Converter
public class SendingWindowConverter extends TypedJsonConverter<SendingWindow> {
    public SendingWindowConverter() {
        super(TypeFactory.defaultInstance().constructType(SendingWindow.class));
    }
}
