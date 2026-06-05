package ai.leadplus.domain.users;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum UserRole {
    CUSTOMER( "Customer"),
    VENDOR( "Vendor"),
    GUEST("Guest"),
    USER("User"),
    ADMIN("Admin"),
    TENANT_OWNER("Tenant Owner");

    private final String value;
}