package ai.leadplus.domain.common;

import ai.leadplus.domain.vendors.SocialMedia;
import com.fasterxml.jackson.databind.type.TypeFactory;
import jakarta.persistence.Converter;

@Converter
public class SocialMediaConverter extends TypedJsonConverter<SocialMedia> {
    public SocialMediaConverter() {
        super(TypeFactory.defaultInstance().constructType(SocialMedia.class));
    }
}
