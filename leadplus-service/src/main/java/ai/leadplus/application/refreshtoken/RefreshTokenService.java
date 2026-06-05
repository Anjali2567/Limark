package ai.leadplus.application.refreshtoken;

import ai.leadplus.application.auth.JwtAuthenticationDto;
import ai.leadplus.application.auth.JwtService;
import ai.leadplus.application.exception.RefreshTokenExpiredException;
import ai.leadplus.application.exception.RefreshTokenInvalidException;
import ai.leadplus.application.exception.ResourceNotFoundException;
import ai.leadplus.application.tenants.TenantService;
import ai.leadplus.application.users.IdentityProviderDto;
import ai.leadplus.application.users.UserDto;
import ai.leadplus.application.users.UserService;
import ai.leadplus.application.vendors.VendorService;
import ai.leadplus.application.workspaceuser.WorkspaceUserService;
import ai.leadplus.domain.refreshtoken.RefreshToken;
import ai.leadplus.domain.refreshtoken.RefreshTokenRepository;
import ai.leadplus.domain.users.IdentityProviderType;
import ai.leadplus.infrastructure.google.userinfo.GoogleUserInfoClient;
import ai.leadplus.infrastructure.google.userinfo.GoogleUserInfoResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final UserService userService;
    private final VendorService vendorService;
    private final WorkspaceUserService workspaceUserService;
    private final JwtService jwtService;
    private final TenantService tenantService;
    private final GoogleUserInfoClient googleUserInfoClient;

    @Value("${jwt.refresh-token.expiration-minutes}")
    private long refreshTokenExpirationMinutes;

    @Value("${jwt.refresh-token.remember-me.expiration-minutes}")
    private long rememberMeRefreshTokenExpirationMinutes;

    @Transactional
    public JwtAuthenticationDto createUser(UserDto userDto) {
        UserDto savedUser = userService.createUser(userDto);
        savedUser = tenantService.tenantWorkspaceCreation(savedUser);
        workspaceUserService.createWorkspaceOwner(savedUser, savedUser.getWorkspaceId());
        return generateTokens(savedUser);
    }

    @Transactional
    public JwtAuthenticationDto createVendor(UserDto userDto) {
        UserDto savedUser = userService.createVendorUser(userDto);
        savedUser = tenantService.tenantWorkspaceCreation(savedUser);
        vendorService.createVendorFromUser(savedUser);
        workspaceUserService.createWorkspaceOwner(savedUser, savedUser.getWorkspaceId());
        return generateTokens(savedUser);
    }

    @Transactional
    public JwtAuthenticationDto createUserTokens(UserDto userDto) {
        UserDto savedUser = userService.getUserById(userDto.getId());
        return generateTokens(savedUser);
    }

    private JwtAuthenticationDto generateTokens(UserDto userDto) {
        String accessToken = jwtService
                .generateToken(String.valueOf(userDto.getId()),
                        userDto.getWorkspaceId() != null ? String.valueOf(userDto.getWorkspaceId()) : null,
                        userDto.getTenantId() != null ? String.valueOf(userDto.getTenantId()) : null,
                        userDto.getName(),
                        userDto.getEmail(),
                        userDto.getRoles(),
                        userDto.isEmailVerified(),
                        true);
        RefreshTokenDto refreshTokenDto = createRefreshToken(String.valueOf(userDto.getId()), false);
        return JwtAuthenticationDto.builder()
                .accessToken(accessToken)
                .refreshToken(refreshTokenDto.getToken())
                .build();
    }

    @Transactional
    public RefreshTokenDto createRefreshToken(String userId, boolean rememberMe) {
        String token = UUID.randomUUID().toString();
        LocalDateTime now = LocalDateTime.now();

        long expiry = rememberMe ? rememberMeRefreshTokenExpirationMinutes : refreshTokenExpirationMinutes;
        RefreshToken refreshToken = RefreshToken.builder()
                .userId(Long.parseLong(userId))
                .token(token)
                .refreshCount(0)
                .expiresAt(now.plusMinutes(expiry))
                .lastUsedAt(now)
                .revoked(false)
                .build();

        refreshTokenRepository.save(refreshToken);
        log.info("Created refresh token for userId: {}", userId);

        return RefreshTokenDto.fromEntity(refreshToken);
    }

    @Transactional
    public JwtAuthenticationDto getJwtAuthenticationByRefreshToken(String token) {

        RefreshToken refreshToken = findRefreshToken(token);

        if (refreshToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            refreshToken.setRevoked(true);
            refreshTokenRepository.save(refreshToken);
            throw new RefreshTokenExpiredException("Refresh token is expired. Please login again.");
        }

        if (refreshToken.isRevoked()) {
            throw new RefreshTokenInvalidException("Refresh token is revoked. Please login again.");
        }

        refreshToken.setRefreshCount(refreshToken.getRefreshCount() + 1);
        refreshToken.setLastUsedAt(LocalDateTime.now());
        refreshTokenRepository.save(refreshToken);

        UserDto userDto = userService.getUserById(refreshToken.getUserId());

        String accessToken = jwtService
                .generateToken(String.valueOf(userDto.getId()),
                        userDto.getWorkspaceId() != null ? String.valueOf(userDto.getWorkspaceId()) : null,
                        userDto.getTenantId() != null ? String.valueOf(userDto.getTenantId()) : null,
                        userDto.getName(),
                        userDto.getEmail(),
                        userDto.getRoles(),
                        userDto.isEmailVerified(),
                        true);

        return JwtAuthenticationDto.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken.getToken())
                .build();
    }

    @Transactional
    public JwtAuthenticationDto authenticateOrRegisterViaGoogleOAuth(String accessToken) {
        GoogleUserInfoResponse userInfo = googleUserInfoClient.getVerifiedUserInfo(accessToken);
        String email = userInfo.getEmail();

        Optional<UserDto> existingUserOpt = userService.getOptionalUserByEmail(email);

        if (existingUserOpt.isPresent()) {
            return createUserTokens(existingUserOpt.get());
        }

        IdentityProviderDto providerDto = IdentityProviderDto.builder()
                .type(IdentityProviderType.GOOGLE)
                .personId(userInfo.getId())
                .build();

        UserDto userDto = UserDto.builder()
                .name(userInfo.getName())
                .identityProviders(List.of(providerDto))
                .email(email)
                .company(userInfo.getEmail().split("@")[1].split("\\.")[0])
                .emailVerified(true)
                .build();
        return createUser(userDto);
    }

    private RefreshToken findRefreshToken(String refreshToken) {
        return refreshTokenRepository.findByTokenAndRevokedFalse(refreshToken)
                .orElseThrow(() -> new ResourceNotFoundException("Invalid refresh token"));
    }
}
