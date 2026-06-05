package ai.leadplus.application.agreement;

import ai.leadplus.application.exception.ResourceNotFoundException;
import ai.leadplus.domain.agreement.Agreement;
import ai.leadplus.domain.agreement.AgreementRepository;
import ai.leadplus.domain.agreement.AgreementType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VendorAgreementServiceTest {

    @Mock
    private AgreementRepository agreementRepository;

    @InjectMocks
    private AgreementService agreementService;

    private Agreement agreement;

    @BeforeEach
    void setUp() {
        agreement = Agreement.builder()
                .id(1L)
                .agreementType(AgreementType.PRIVACY_POLICY)
                .content("Agreement Content")
                .version(1)
                .latest(true)
                .build();
    }

    @Test
    void getAllAgreementVersions_shouldReturnAll_whenTypeIsNull() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Agreement> page = new PageImpl<>(List.of(agreement));

        when(agreementRepository.findAll(pageable)).thenReturn(page);

        Page<AgreementDto> result = agreementService.getAllAgreementVersions(null, pageable);

        assertThat(result.getTotalElements()).isEqualTo(1);
        verify(agreementRepository).findAll(pageable);
    }

    @Test
    void getAllAgreementVersions_shouldReturnFiltered_whenTypeProvided() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Agreement> page = new PageImpl<>(List.of(agreement));

        when(agreementRepository.findAllByAgreementType(
                AgreementType.PRIVACY_POLICY, pageable))
                .thenReturn(page);

        Page<AgreementDto> result = agreementService.getAllAgreementVersions(
                AgreementType.PRIVACY_POLICY, pageable);

        assertThat(result.getTotalElements()).isEqualTo(1);
        verify(agreementRepository)
                .findAllByAgreementType(AgreementType.PRIVACY_POLICY, pageable);
    }

    @Test
    void getAgreementById_shouldReturnAgreement() {
        when(agreementRepository.findById(1L)).thenReturn(Optional.of(agreement));

        AgreementDto result = agreementService.getAgreementById(1L);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getContent()).isEqualTo("Agreement Content");
    }

    @Test
    void getAgreementById_shouldThrow_whenNotFound() {
        when(agreementRepository.findById(2L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> agreementService.getAgreementById(2L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Agreement not found with id 2");
    }

    @Test
    void updateAgreementByType_shouldCreateNewVersionAndMarkOldAsNotLatest() {
        Agreement current = Agreement.builder()
                .id(1L)
                .agreementType(AgreementType.PRIVACY_POLICY)
                .content("Old Content")
                .version(1)
                .latest(true)
                .build();

        when(agreementRepository.findAgreementByAgreementTypeAndLatestIsTrue(
                AgreementType.PRIVACY_POLICY))
                .thenReturn(current);

        when(agreementRepository.save(any(Agreement.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        AgreementDto result = agreementService.updateAgreementByType(
                AgreementType.PRIVACY_POLICY,
                "New Content"
        );

        assertThat(current.isLatest()).isFalse();

        assertThat(result.getVersion()).isEqualTo(2);
        assertThat(result.getContent()).isEqualTo("New Content");
        assertThat(result.isLatest()).isTrue();
    }

    @Test
    void updateAgreementByType_shouldCallSaveTwice() {
        when(agreementRepository.findAgreementByAgreementTypeAndLatestIsTrue(any()))
                .thenReturn(agreement);

        when(agreementRepository.save(any()))
                .thenAnswer(i -> i.getArgument(0));

        agreementService.updateAgreementByType(
                AgreementType.PRIVACY_POLICY,
                "Updated Content"
        );

        verify(agreementRepository, times(2)).save(any());
    }
}
