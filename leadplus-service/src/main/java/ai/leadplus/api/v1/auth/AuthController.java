package ai.leadplus.api.v1.auth;

import ai.leadplus.api.v1.users.UserSignUpRequest;
import ai.leadplus.application.auth.AuthenticationService;
import ai.leadplus.application.auth.JwtAuthenticationDto;
import ai.leadplus.application.refreshtoken.RefreshTokenService;
import ai.leadplus.application.users.UserDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Authentication APIs")
public class AuthController {

    private final RefreshTokenService refreshTokenService;
    private final AuthenticationService authenticationService;

    @Operation(summary = "Register New User")
    @SecurityRequirements()
    @PostMapping("/sign-up")
    public ResponseEntity<JwtAuthenticationResponse> createUserAccount(@Valid @RequestBody UserSignUpRequest userSignUpRequest) {
        log.info("[POST] Request received to create user");
        JwtAuthenticationDto refreshTokenDto = refreshTokenService.createUser(userSignUpRequest.toDto());
        JwtAuthenticationResponse jwtAuthenticationResponse = JwtAuthenticationResponse.toResponse(refreshTokenDto);
        log.info("[POST] Successfully created user");
        return ResponseEntity.ok(jwtAuthenticationResponse);
    }

    @Operation(summary = "Register Vendor")
    @SecurityRequirements()
    @PostMapping("/sign-up/vendor")
    public ResponseEntity<JwtAuthenticationResponse> createVendorAccount(@Valid @RequestBody UserSignUpRequest userSignUpRequest) {
        log.info("[POST] Request received to create Vendor");
        JwtAuthenticationDto refreshTokenDto = refreshTokenService.createVendor(userSignUpRequest.toDto());
        JwtAuthenticationResponse jwtAuthenticationResponse = JwtAuthenticationResponse.toResponse(refreshTokenDto);
        log.info("[POST] Successfully create Vendor");
        return ResponseEntity.ok(jwtAuthenticationResponse);
    }

    @Operation(summary = "Get JWTAuth by Refresh Token")
    @SecurityRequirements()
    @PostMapping("/refresh")
    public ResponseEntity<JwtAuthenticationResponse> refreshAccessToken(@RequestParam("refreshToken") String refreshToken) {
        log.info("[POST] Request received to create JWTAuth By Refresh Token");
        JwtAuthenticationDto refreshTokenDto = refreshTokenService.getJwtAuthenticationByRefreshToken(refreshToken);
        JwtAuthenticationResponse jwtAuthenticationResponse = JwtAuthenticationResponse.toResponse(refreshTokenDto);
        log.info("[POST] Successfully created JWTAuth By Refresh Token");
        return ResponseEntity.ok(jwtAuthenticationResponse);
    }

    @Operation(summary = "Send Forgot Password Link")
    @SecurityRequirements()
    @PostMapping("/forgot-password/request")
    public ResponseEntity<Void> forgotPassword(@RequestBody @Valid ForgotPasswordRequest forgotPasswordRequest) {
        log.info("[POST] Request received to request for forgot password");
        authenticationService.sendForgotPasswordLink(forgotPasswordRequest.getEmail());
        log.info("[POST] Successfully sent forgot password link");
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Verify Forgot Password Link")
    @SecurityRequirements()
    @PostMapping("/forgot-password")
    public ResponseEntity<JwtAuthenticationResponse> forgotPassword(@Valid @RequestBody VerifyForgotPasswordRequest verifyForgotPasswordRequest) {
        log.info("[POST] Request received to reset password via verification link");
        UserDto userDto = authenticationService.verifyForgotPassword(verifyForgotPasswordRequest.toDto());
        JwtAuthenticationDto refreshTokenDto = refreshTokenService.createUserTokens(userDto);
        JwtAuthenticationResponse jwtAuthenticationResponse = JwtAuthenticationResponse.toResponse(refreshTokenDto);
        log.info("[POST] Successfully reset the password");
        return ResponseEntity.ok(jwtAuthenticationResponse);
    }

    @Operation(summary = "Google SignUp")
    @SecurityRequirements()
    @PostMapping("/google")
    public ResponseEntity<JwtAuthenticationResponse> authenticateOrRegisterCustomerViaGoogleOAuth(@Valid @RequestBody GoogleAuthRequest googleAuthRequest) {
        log.info("[POST] Received request to create Or Login new customer via google OAuth");
        JwtAuthenticationDto refreshTokenDto = refreshTokenService.authenticateOrRegisterViaGoogleOAuth(googleAuthRequest.getToken());
        JwtAuthenticationResponse jwtAuthenticationResponse = JwtAuthenticationResponse.toResponse(refreshTokenDto);
        log.info("[POST] Successfully Created or Login Customer");
        return ResponseEntity.ok(jwtAuthenticationResponse);
    }

    @Operation(summary = "Verify Email Link")
    @SecurityRequirements()
    @PostMapping("/verify-email")
    public ResponseEntity<Void> verifyEmail(@Valid @RequestBody EmailVerifyRequest emailVerifyRequest) {
        authenticationService.verifyEmail(emailVerifyRequest.getToken());
        return ResponseEntity.noContent().build();
    }
}
