package ai.leadplus.application.users;

import ai.leadplus.domain.users.User;
import ai.leadplus.domain.users.UserRole;
import ai.leadplus.domain.users.UserStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserDto {
    private Long id;
    private Long workspaceId;
    private Long tenantId;
    private String name;
    private String phoneNumber;
    private String company;
    private String email;
    private String password;
    private List<UserRole> roles;
    private boolean draft;
    private boolean emailVerified;
    private UserStatus status;
    private String verificationToken;
    private String emailVerificationToken;
    private List<IdentityProviderDto> identityProviders;
    private boolean active;
    private LocalDateTime lastLoginAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static UserDto fromEntity(User user) {
        return UserDto.builder()
                .id(user.getId())
                .workspaceId(user.getWorkspaceId())
                .tenantId(user.getTenantId())
                .name(user.getName())
                .phoneNumber(user.getPhoneNumber())
                .company(user.getCompany())
                .email(user.getEmail())
                .password(user.getPassword())
                .roles(user.getRoles())
                .status(user.getStatus())
                .verificationToken(user.getVerificationToken())
                .emailVerificationToken(user.getEmailVerificationToken())
                .identityProviders(user.getIdentityProviders() != null ? user.getIdentityProviders().stream()
                        .map(IdentityProviderDto::fromEntity).toList() : null)
                .active(user.isActive())
                .draft(user.isDraft())
                .emailVerified(user.isEmailVerified())
                .lastLoginAt(user.getLastLoginAt())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }

    public User toEntity() {
        return User.builder()
                .id(id)
                .workspaceId(workspaceId)
                .tenantId(tenantId)
                .name(name)
                .phoneNumber(phoneNumber)
                .company(company)
                .email(email)
                .password(password)
                .roles(roles)
                .status(status)
                .verificationToken(verificationToken)
                .emailVerificationToken(emailVerificationToken)
                .identityProviders(identityProviders != null ? identityProviders.stream()
                        .map(IdentityProviderDto::toEntity).toList() : null)
                .active(active)
                .draft(draft)
                .emailVerified(emailVerified)
                .lastLoginAt(lastLoginAt)
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .build();
    }
}
