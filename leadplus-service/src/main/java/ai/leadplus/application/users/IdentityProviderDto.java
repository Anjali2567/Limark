package ai.leadplus.application.users;

import ai.leadplus.domain.users.IdentityProviderType;
import ai.leadplus.domain.users.IdentityProvider;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class IdentityProviderDto {
    private IdentityProviderType type;
    private String personId;

    public static IdentityProviderDto fromEntity(IdentityProvider identityProvider){
        return IdentityProviderDto.builder()
                .type(identityProvider.getType())
                .personId(identityProvider.getPersonId())
                .build();
    }

    public IdentityProvider toEntity() {
        return IdentityProvider.builder()
                .type(type)
                .personId(personId)
                .build();
    }
}
