package ai.leadplus.api.common;

import ai.leadplus.application.users.UserDto;
import ai.leadplus.application.vendors.VendorDto;
import ai.leadplus.application.vendors.VendorService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class VendorValidator {

    private final UserValidator userValidator;
    private final VendorService vendorService;

    public VendorDto getAuthenticatedVendor() {
        UserDto userDto = userValidator.getAuthenticatedUser();
        return vendorService.getVendorByUserId(userDto.getId());
    }
}
