package ai.leadplus.domain.common;

import ai.leadplus.domain.vendors.Address;
import com.fasterxml.jackson.databind.type.TypeFactory;
import jakarta.persistence.Converter;

@Converter
public class AddressConverter extends TypedJsonConverter<Address> {
    public AddressConverter() {
        super(TypeFactory.defaultInstance().constructType(Address.class));
    }
}
