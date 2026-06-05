package ai.leadplus.api.v1.vendoragreement;

import ai.leadplus.application.vendoragreement.VendorAgreementDto;
import ai.leadplus.application.vendoragreement.VendorAgreementService;
import ai.leadplus.application.vendors.VendorService;
import ai.leadplus.domain.agreement.AgreementType;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/v1/vendor-agreements")
@RequiredArgsConstructor
@Tag(name = "VendorAgreements", description = "Vendor Agreement APIs")
public class VendorAgreementController {
    private final VendorAgreementService vendorAgreementService;
    private final VendorService vendorService;

    @Operation(summary = "Get Vendor Agreement by Type")
    @GetMapping("/{vendorId}")
    public ResponseEntity<VendorAgreementResponse> getVendorAgreement(@PathVariable Long vendorId, @RequestParam AgreementType agreementType) {
        vendorService.validateAuthenticatedVendorId(vendorId);
        log.info("[GET] Request to fetch Vendor Agreement for vendorId={} with type={}", vendorId, agreementType);
        VendorAgreementDto vendorAgreementDto = vendorAgreementService.getAgreementForVendorByType(vendorId, agreementType);
        log.info("[GET] Fetched Vendor Agreement for vendorId={} with type={}", vendorId, agreementType);
        return ResponseEntity.ok(VendorAgreementResponse.fromDto(vendorAgreementDto));
    }

    @Operation(summary = "Send OTP")
    @PostMapping("/otp")
    public ResponseEntity<Void> sendOtp(@RequestParam Long vendorId, @RequestParam AgreementType agreementType) {
        vendorService.validateAuthenticatedVendorId(vendorId);
        log.info("[POST] Request to send OTP for vendorId={} with type={}", vendorId, agreementType);
        vendorAgreementService.sendOTPToVendor(vendorId, agreementType);
        log.info("[POST] OTP sent successfully to vendorId={} for type={}", vendorId, agreementType);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Verify OTP")
    @PostMapping("/verify-otp")
    public ResponseEntity<Void> verifyOTP(@RequestBody VendorAgreementRequest vendorAgreementRequest) {
        vendorService.validateAuthenticatedVendorId(vendorAgreementRequest.getVendorId());
        log.info("[POST] Request to verify OTP for vendorId={} with type={}",
                vendorAgreementRequest.getVendorId(),
                vendorAgreementRequest.getAgreementType());
        vendorAgreementService.verifyVendorOTP(vendorAgreementRequest.toDto(), vendorAgreementRequest.getOtp());
        log.info("[POST] OTP verification successful for vendorId={} with type={}",
                vendorAgreementRequest.getVendorId(),
                vendorAgreementRequest.getAgreementType());
        return ResponseEntity.noContent().build();
    }
}
