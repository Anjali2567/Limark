package ai.leadplus.application.agreement;

import ai.leadplus.application.exception.ResourceNotFoundException;
import ai.leadplus.domain.agreement.Agreement;
import ai.leadplus.domain.agreement.AgreementRepository;
import ai.leadplus.domain.agreement.AgreementType;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AgreementService {

    private final AgreementRepository agreementRepository;

    public Page<AgreementDto> getAllAgreementVersions(AgreementType agreementType, Pageable pageable) {
        Page<Agreement> agreements = (agreementType == null)
                ? agreementRepository.findAll(pageable)
                : agreementRepository.findAllByAgreementType(agreementType, pageable);
        return agreements.map(AgreementDto::toDto);
    }

    public AgreementDto getAgreementById(Long agreementId) {
        return AgreementDto.toDto(findById(agreementId));
    }

    public AgreementDto updateAgreementByType(AgreementType agreementType, String content) {
        Agreement currentAgreement = agreementRepository.findAgreementByAgreementTypeAndLatestIsTrue(agreementType);

        if (currentAgreement == null) {
            throw new ResourceNotFoundException("No current agreement found for type " + agreementType);
        }

        currentAgreement.setLatest(false);
        agreementRepository.save(currentAgreement);
        Agreement newAgreement = Agreement.builder()
                .version(currentAgreement.getVersion() + 1)
                .latest(true)
                .content(content)
                .agreementType(agreementType)
                .build();
        return AgreementDto.toDto(agreementRepository.save(newAgreement));
    }

    private Agreement findById(Long id) {
        return agreementRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Agreement not found with id " + id
                ));
    }
}
