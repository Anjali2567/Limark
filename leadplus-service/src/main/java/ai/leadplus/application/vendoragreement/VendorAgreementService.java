package ai.leadplus.application.vendoragreement;

import ai.leadplus.application.aws.EmailService;
import ai.leadplus.application.exception.*;
import ai.leadplus.application.users.UserDto;
import ai.leadplus.application.users.UserService;
import ai.leadplus.application.vendors.VendorDto;
import ai.leadplus.application.vendors.VendorService;
import ai.leadplus.domain.agreement.Agreement;
import ai.leadplus.domain.agreement.AgreementRepository;
import ai.leadplus.domain.agreement.AgreementType;
import ai.leadplus.domain.vendoragreement.Otp;
import ai.leadplus.domain.vendoragreement.VendorAgreement;
import ai.leadplus.domain.vendoragreement.VendorAgreementRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class VendorAgreementService {
    private final VendorAgreementRepository vendorAgreementRepository;
    private final AgreementRepository agreementRepository;
    private final VendorService vendorService;
    private final UserService userService;
    private final EmailService emailService;

    private static final Integer LENGTH = 6;
    private static final Integer ATTEMPT = 3;
    private static final Integer INTERVAL = 90;
    private static final Integer OTP_EXPIRY_HOURS = 1;
    private final SecureRandom random = new SecureRandom();

    public VendorAgreementDto getAgreementForVendorByType(Long vendorId, AgreementType agreementType) {
        VendorAgreement existingAgreement = vendorAgreementRepository.findByVendorIdAndAgreementType(vendorId, agreementType);
        Agreement agreement = agreementRepository.findAgreementByAgreementTypeAndLatestIsTrue(agreementType);
        if (agreement == null) {
            throw new ResourceNotFoundException("No latest agreement found for type: " + agreementType);
        }
        VendorDto vendorDto = vendorService.getVendorById(vendorId);
        UserDto userDto = userService.getUserById(vendorDto.getUserId());
        if(existingAgreement!=null){
            if(Objects.equals(existingAgreement.getVersion(), agreement.getVersion())){
                return VendorAgreementDto.fromEntity(existingAgreement);
            }
            existingAgreement.setAgreementText(replacePlaceholders(vendorDto, userDto, agreement.getContent()));
            existingAgreement.setVersion(agreement.getVersion());
            return VendorAgreementDto.fromEntity(vendorAgreementRepository.save(existingAgreement));
        }
        String agreementData = replacePlaceholders(vendorDto, userDto, agreement.getContent());
        VendorAgreement newVendorAgreement = VendorAgreement.builder()
                .vendorId(vendorId)
                .agreementType(agreementType)
                .version(agreement.getVersion())
                .agreementText(agreementData)
                .build();
        VendorAgreement savedAgreement = vendorAgreementRepository.save(newVendorAgreement);
        return VendorAgreementDto.fromEntity(savedAgreement);
    }

    public void sendOTPToVendor(Long vendorId, AgreementType agreementType) {
        VendorAgreement existingAgreement = vendorAgreementRepository.findByVendorIdAndAgreementType(vendorId, agreementType);

        if (existingAgreement == null) {
            throw new ResourceNotFoundException("No agreement found for the vendor and agreement type");
        }

        VendorDto vendorDto = vendorService.getVendorById(vendorId);
        UserDto userDto = userService.getUserById(vendorDto.getUserId());

        if(existingAgreement.isSigned()){
            throw new BadRequestException("Agreement already signed");
        }
        if(existingAgreement.getOtp() != null){
            boolean shouldSend = existingAgreement.getOtp().getGeneratedAt().isBefore(LocalDateTime.now().minusSeconds(INTERVAL));
            if (!shouldSend) {
                throw new OtpResendTooSoonException(INTERVAL);
            }
        }
        String otpCode = generateOTP();
        Otp otp = Otp.builder()
                .otpCode(otpCode)
                .attempt(0)
                .build();
        existingAgreement.setOtp(otp);
        vendorAgreementRepository.save(existingAgreement);
        emailService.sendOTPVerificationEmail(userDto.getEmail(),userDto.getName(), otpCode);
    }

    public void verifyVendorOTP(VendorAgreementDto vendorAgreementDto, String otp) {
        VendorAgreement vendorAgreement = vendorAgreementRepository.findByVendorIdAndAgreementType(
                vendorAgreementDto.getVendorId(),
                vendorAgreementDto.getAgreementType());
        if (vendorAgreement == null || vendorAgreement.getOtp() == null || !StringUtils.hasText(vendorAgreement.getOtp().getOtpCode())) {
            throw new ResourceNotFoundException("No OTP found for the vendor and agreement type");
        }
        Otp existingOtp = vendorAgreement.getOtp();
        if (existingOtp.getAttempt() >= ATTEMPT) {
            throw new OTPInvalidException("Attempts exceeded, please request a new OTP");
        }
        if (vendorAgreement.getOtp().getGeneratedAt()
                .isBefore(LocalDateTime.now().minusHours(OTP_EXPIRY_HOURS))) {
            throw new OTPExpiredException("OTP expired, please request a new one");
        }
        if (!existingOtp.getOtpCode().equals(otp)) {
            existingOtp.setAttempt(existingOtp.getAttempt() + 1);
            vendorAgreement.setOtp(existingOtp);
            vendorAgreementRepository.save(vendorAgreement);
            throw new OTPInvalidException("Invalid OTP");
        }
        submitAgreement(vendorAgreement, vendorAgreementDto);
    }

    public void submitAgreement(VendorAgreement existingAgreement, VendorAgreementDto signingDetails) {
        if(existingAgreement.isSigned()){
            throw new BadRequestException("Agreement already signed");
        }
        existingAgreement.setSignedAt(LocalDateTime.now());
        existingAgreement.setSigned(true);
        existingAgreement.setOtp(null);
        existingAgreement.setVerified(true);
        existingAgreement.setSignedBy(signingDetails.getSignedBy());
        existingAgreement.setName(signingDetails.getName());
        existingAgreement.setTitle(signingDetails.getTitle());
        vendorAgreementRepository.save(existingAgreement);
    }


    private String replacePlaceholders(VendorDto vendorDto, UserDto userDto, String agreementTemplate) {
        if (vendorDto == null || agreementTemplate == null) {
            return agreementTemplate;
        }

        return agreementTemplate
                .replaceAll(regexPlaceholder("NAME"), quote(safeString(userDto.getName())))
                .replaceAll(regexPlaceholder("EMAIL"), quote(safeString(userDto.getEmail())))
                .replaceAll(regexPlaceholder("COMPANY_NAME"), quote(safeString(vendorDto.getCompanyName())))
                .replaceAll(regexPlaceholder("PHONE_NUMBER"), quote(safeString(vendorDto.getPhoneNumber())))
                .replaceAll(regexPlaceholder("WEBSITE"), quote(safeString(vendorDto.getWebsite())))
                .replaceAll(regexPlaceholder("DATE_TODAY"), quote(LocalDateTime.now().toLocalDate().toString()))
                .replaceAll(regexPlaceholder("ADDRESS_STREET"),
                        quote(safeString(vendorDto.getAddress() != null ? vendorDto.getAddress().getStreet() : null)))
                .replaceAll(regexPlaceholder("ADDRESS_CITY"),
                        quote(safeString(vendorDto.getAddress() != null ? vendorDto.getAddress().getCity() : null)))
                .replaceAll(regexPlaceholder("ADDRESS_STATE"),
                        quote(safeString(vendorDto.getAddress() != null ? vendorDto.getAddress().getState() : null)))
                .replaceAll(regexPlaceholder("ADDRESS_COUNTRY"),
                        quote(safeString(vendorDto.getAddress() != null ? vendorDto.getAddress().getCountry() : null)));
    }

    private String quote(String value) {
        return Matcher.quoteReplacement(value);
    }

    private String safeString(String value) {
        return StringUtils.hasText(value) ? value : "-";
    }

    private String generateOTP() {
        return random.ints(0, 10)
                .limit(LENGTH)
                .collect(StringBuilder::new, StringBuilder::append, StringBuilder::append)
                .toString();
    }

    private String regexPlaceholder(String key) {
        return "\\{\\{\\s*" + Pattern.quote(key) + "\\s*}}";
    }
}