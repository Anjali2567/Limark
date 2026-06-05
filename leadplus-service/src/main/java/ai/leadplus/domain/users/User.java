package ai.leadplus.domain.users;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Convert;
import jakarta.persistence.Column;
import jakarta.persistence.Enumerated;
import jakarta.persistence.EnumType;
import ai.leadplus.domain.common.IdentityProviderListConverter;
import ai.leadplus.domain.common.UserRoleListConverter;
import jakarta.persistence.EntityListeners;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import jakarta.persistence.Id;
import jakarta.persistence.GenerationType;
import jakarta.persistence.GeneratedValue;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
@Entity
@Table(name = "tenant_user")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long workspaceId;
    private Long tenantId;
    private String name;
    private String phoneNumber;
    private String company;
    private String email;
    private String password;
    @Convert(converter = UserRoleListConverter.class)
    @Column(name = "roles", columnDefinition = "varchar[]")
    private List<UserRole> roles;
    private boolean draft;
    private boolean emailVerified;
    @Enumerated(EnumType.STRING)
    private UserStatus status;
    private String verificationToken;
    private String emailVerificationToken;
    @Convert(converter = IdentityProviderListConverter.class)
    private List<IdentityProvider> identityProviders;
    private boolean active;
    private LocalDateTime lastLoginAt;
    @CreatedDate
    private LocalDateTime createdAt;
    @LastModifiedDate
    private LocalDateTime updatedAt;
}
