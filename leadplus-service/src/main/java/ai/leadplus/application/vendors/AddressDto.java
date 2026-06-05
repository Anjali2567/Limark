package ai.leadplus.application.vendors;

import ai.leadplus.domain.vendors.Address;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddressDto {

    private String street;
    private String city;
    private String state;
    private String postalCode;
    private String country;

    public static AddressDto fromEntity(Address address) {
        if (address == null) return null;

        return AddressDto.builder()
                .street(address.getStreet())
                .city(address.getCity())
                .state(address.getState())
                .postalCode(address.getPostalCode())
                .country(address.getCountry())
                .build();
    }

    public Address toEntity() {
        return Address.builder()
                .street(street)
                .city(city)
                .state(state)
                .postalCode(postalCode)
                .country(country)
                .build();
    }
}