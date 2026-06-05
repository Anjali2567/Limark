package ai.leadplus.application.vendoragreement;

import ai.leadplus.application.aws.EmailService;
import ai.leadplus.application.exception.BadRequestException;
import ai.leadplus.application.exception.OTPExpiredException;
import ai.leadplus.application.exception.OTPInvalidException;
import ai.leadplus.application.exception.ResourceNotFoundException;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(org.mockito.junit.jupiter.MockitoExtension.class)
class VendorAgreementServiceTest {

    @Mock
    private VendorAgreementRepository vendorAgreementRepository;
    @Mock
    private AgreementRepository agreementRepository;
    @Mock
    private VendorService vendorService;
    @Mock
    private UserService userService;
    @Mock
    private EmailService emailService;

    @InjectMocks
    private VendorAgreementService service;

    private VendorAgreement vendorAgreement;
    private Agreement agreement;
    private VendorDto vendorDto;
    private UserDto userDto;

    private final Long VENDOR_ID = 1L;
    private final AgreementType TYPE = AgreementType.PRIVACY_POLICY;

    @BeforeEach
    void setup() {

        vendorAgreement = VendorAgreement.builder()
                .vendorId(VENDOR_ID)
                .agreementType(TYPE)
                .version(1)
                .signed(false)
                .build();

        agreement = Agreement.builder()
                .agreementType(TYPE)
                .version(1)
                .content("Hello {{NAME}}")
                .latest(true)
                .build();

        vendorDto = VendorDto.builder()
                .id(1L)
                .userId(1L)
                .companyName("ABC Pvt")
                .phoneNumber("123456")
                .website("abc.com")
                .build();

        userDto = UserDto.builder()
                .id(2L)
                .name("John")
                .email("john@mail.com")
                .build();
    }

    @Test
    void getAgreement_existingSameVersion_shouldReturnExisting() {

        when(vendorAgreementRepository.findByVendorIdAndAgreementType(VENDOR_ID, TYPE))
                .thenReturn(vendorAgreement);
        when(agreementRepository.findAgreementByAgreementTypeAndLatestIsTrue(TYPE))
                .thenReturn(agreement);
        when(vendorService.getVendorById(VENDOR_ID)).thenReturn(vendorDto);
        when(userService.getUserById(vendorDto.getUserId())).thenReturn(userDto);

        VendorAgreementDto result = service.getAgreementForVendorByType(VENDOR_ID, TYPE);

        assertThat(result).isNotNull();
        verify(vendorAgreementRepository, never()).save(any());
    }

    @Test
    void getAgreement_existingDifferentVersion_shouldUpdate() {

        vendorAgreement.setVersion(0);

        when(vendorAgreementRepository.findByVendorIdAndAgreementType(VENDOR_ID, TYPE))
                .thenReturn(vendorAgreement);
        when(agreementRepository.findAgreementByAgreementTypeAndLatestIsTrue(TYPE))
                .thenReturn(agreement);
        when(vendorService.getVendorById(VENDOR_ID)).thenReturn(vendorDto);
        when(userService.getUserById(vendorDto.getUserId())).thenReturn(userDto);
        when(vendorAgreementRepository.save(any()))
                .thenAnswer(i -> i.getArgument(0));

        VendorAgreementDto result = service.getAgreementForVendorByType(VENDOR_ID, TYPE);

        assertThat(result.getVersion()).isEqualTo(1);
        verify(vendorAgreementRepository).save(any());
    }

    @Test
    void getAgreement_newAgreement_shouldCreate() {

        when(vendorAgreementRepository.findByVendorIdAndAgreementType(VENDOR_ID, TYPE))
                .thenReturn(null);
        when(agreementRepository.findAgreementByAgreementTypeAndLatestIsTrue(TYPE))
                .thenReturn(agreement);
        when(vendorService.getVendorById(VENDOR_ID)).thenReturn(vendorDto);
        when(userService.getUserById(vendorDto.getUserId())).thenReturn(userDto);
        when(vendorAgreementRepository.save(any()))
                .thenAnswer(i -> i.getArgument(0));

        VendorAgreementDto result = service.getAgreementForVendorByType(VENDOR_ID, TYPE);

        assertThat(result).isNotNull();
        verify(vendorAgreementRepository).save(any());
    }

    @Test
    void sendOtp_success() {

        when(vendorAgreementRepository.findByVendorIdAndAgreementType(VENDOR_ID, TYPE))
                .thenReturn(vendorAgreement);
        when(vendorService.getVendorById(VENDOR_ID)).thenReturn(vendorDto);
        when(userService.getUserById(vendorDto.getUserId())).thenReturn(userDto);

        service.sendOTPToVendor(VENDOR_ID, TYPE);

        verify(vendorAgreementRepository).save(any());
        verify(emailService).sendOTPVerificationEmail(eq(userDto.getEmail()), eq(userDto.getName()), any());
    }

    @Test
    void sendOtp_noAgreement_shouldThrow() {

        when(vendorAgreementRepository.findByVendorIdAndAgreementType(VENDOR_ID, TYPE))
                .thenReturn(null);

        assertThatThrownBy(() -> service.sendOTPToVendor(VENDOR_ID, TYPE))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void sendOtp_alreadySigned_shouldThrow() {

        vendorAgreement.setSigned(true);

        when(vendorAgreementRepository.findByVendorIdAndAgreementType(VENDOR_ID, TYPE))
                .thenReturn(vendorAgreement);
        when(vendorService.getVendorById(VENDOR_ID)).thenReturn(vendorDto);
        when(userService.getUserById(vendorDto.getUserId())).thenReturn(userDto);

        assertThatThrownBy(() -> service.sendOTPToVendor(VENDOR_ID, TYPE))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    void verifyOtp_success_shouldSubmitAgreement() {

        Otp otp = Otp.builder()
                .otpCode("123456")
                .attempt(0)
                .generatedAt(LocalDateTime.now())
                .build();

        vendorAgreement.setOtp(otp);

        VendorAgreementDto dto = VendorAgreementDto.builder()
                .vendorId(VENDOR_ID)
                .agreementType(TYPE)
                .signedBy("John")
                .name("John")
                .title("CEO")
                .build();

        when(vendorAgreementRepository.findByVendorIdAndAgreementType(VENDOR_ID, TYPE))
                .thenReturn(vendorAgreement);

        service.verifyVendorOTP(dto, "123456");

        verify(vendorAgreementRepository, atLeastOnce()).save(any());
    }

    @Test
    void verifyOtp_invalid_shouldIncreaseAttempt() {

        Otp otp = Otp.builder()
                .otpCode("111111")
                .attempt(0)
                .generatedAt(LocalDateTime.now())
                .build();

        vendorAgreement.setOtp(otp);

        VendorAgreementDto dto = VendorAgreementDto.builder()
                .vendorId(VENDOR_ID)
                .agreementType(TYPE)
                .build();

        when(vendorAgreementRepository.findByVendorIdAndAgreementType(VENDOR_ID, TYPE))
                .thenReturn(vendorAgreement);

        assertThatThrownBy(() -> service.verifyVendorOTP(dto, "222222"))
                .isInstanceOf(OTPInvalidException.class);

        verify(vendorAgreementRepository).save(any());
    }

    @Test
    void verifyOtp_expired_shouldThrow() {

        Otp otp = Otp.builder()
                .otpCode("123456")
                .attempt(0)
                .generatedAt(LocalDateTime.now().minusHours(2))
                .build();

        vendorAgreement.setOtp(otp);

        VendorAgreementDto dto = VendorAgreementDto.builder()
                .vendorId(VENDOR_ID)
                .agreementType(TYPE)
                .build();

        when(vendorAgreementRepository.findByVendorIdAndAgreementType(VENDOR_ID, TYPE))
                .thenReturn(vendorAgreement);

        assertThatThrownBy(() -> service.verifyVendorOTP(dto, "123456"))
                .isInstanceOf(OTPExpiredException.class);
    }

    @Test
    void verifyOtp_attemptExceeded_shouldThrow() {

        Otp otp = Otp.builder()
                .otpCode("123456")
                .attempt(3)
                .generatedAt(LocalDateTime.now())
                .build();

        vendorAgreement.setOtp(otp);

        VendorAgreementDto dto = VendorAgreementDto.builder()
                .vendorId(VENDOR_ID)
                .agreementType(TYPE)
                .build();

        when(vendorAgreementRepository.findByVendorIdAndAgreementType(VENDOR_ID, TYPE))
                .thenReturn(vendorAgreement);

        assertThatThrownBy(() -> service.verifyVendorOTP(dto, "123456"))
                .isInstanceOf(OTPInvalidException.class);
    }

    @Test
    void submitAgreement_success() {

        VendorAgreementDto dto = VendorAgreementDto.builder()
                .signedBy("John")
                .name("John")
                .title("CEO")
                .build();

        service.submitAgreement(vendorAgreement, dto);

        verify(vendorAgreementRepository).save(vendorAgreement);
        assertThat(vendorAgreement.isSigned()).isTrue();
        assertThat(vendorAgreement.getOtp()).isNull();
    }

    @Test
    void submitAgreement_alreadySigned_shouldThrow() {

        vendorAgreement.setSigned(true);

        VendorAgreementDto dto = VendorAgreementDto.builder().build();

        assertThatThrownBy(() -> service.submitAgreement(vendorAgreement, dto))
                .isInstanceOf(BadRequestException.class);
    }
}
