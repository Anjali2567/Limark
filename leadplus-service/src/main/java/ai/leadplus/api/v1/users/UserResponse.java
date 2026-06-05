package ai.leadplus.api.v1.users;

import ai.leadplus.application.users.IdentityProviderDto;
import ai.leadplus.application.users.UserDto;
import ai.leadplus.domain.users.UserRole;
import ai.leadplus.domain.users.UserStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserResponse {
    private Long id;
    private Long tenantId;
    private Long workspaceId;
    private String name;
    private String phoneNumber;
    private String company;
    private String email;
    private List<UserRole> roles;
    private UserStatus status;
    private List<IdentityProviderDto> identityProviders;
    private LocalDateTime createdAt;

    public static UserResponse fromDto(UserDto userDto) {
        return UserResponse.builder()
                .id(userDto.getId())
                .tenantId(userDto.getTenantId())
                .workspaceId(userDto.getWorkspaceId())
                .name(userDto.getName())
                .phoneNumber(userDto.getPhoneNumber())
                .company(userDto.getCompany())
                .email(userDto.getEmail())
                .roles(userDto.getRoles())
                .status(userDto.getStatus())
                .identityProviders(userDto.getIdentityProviders())
                .createdAt(userDto.getCreatedAt())
                .build();
    }
}
