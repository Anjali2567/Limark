package ai.leadplus.domain.common;

import ai.leadplus.domain.vendoragreement.Otp;
import com.fasterxml.jackson.databind.type.TypeFactory;
import jakarta.persistence.Converter;

@Converter
public class OtpConverter extends TypedJsonConverter<Otp> {
    public OtpConverter() {
        super(TypeFactory.defaultInstance().constructType(Otp.class));
    }
}
