package ai.leadplus.domain.common;

import ai.leadplus.domain.users.IdentityProvider;
import com.fasterxml.jackson.databind.type.TypeFactory;
import jakarta.persistence.Converter;

import java.util.List;

@Converter
public class IdentityProviderListConverter extends TypedJsonConverter<List<IdentityProvider>> {
    public IdentityProviderListConverter() {
        super(TypeFactory.defaultInstance().constructCollectionType(List.class, IdentityProvider.class));
    }
}
