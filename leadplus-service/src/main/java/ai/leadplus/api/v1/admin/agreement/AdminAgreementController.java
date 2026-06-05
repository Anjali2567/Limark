package ai.leadplus.api.v1.admin.agreement;

import ai.leadplus.application.agreement.AgreementDto;
import ai.leadplus.application.agreement.AgreementService;
import ai.leadplus.application.users.UserDto;
import ai.leadplus.application.users.UserService;
import ai.leadplus.domain.agreement.AgreementType;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;


@Slf4j
@RestController
@RequestMapping("/v1/admin/agreements")
@RequiredArgsConstructor
@Tag(name = "Agreement", description = "Agreement APIs")
public class AdminAgreementController {
    private final AgreementService agreementService;
    private final UserService userService;

    @Operation(summary = "Get All Agreement Versions")
    @GetMapping
    public ResponseEntity<Page<AgreementResponse>> getAllAgreementVersions(@RequestParam(required = false) AgreementType agreementType, Pageable pageable) {
        log.info("[GET] Received request to get All Agreement Versions");
        Page<AgreementDto> agreementPage = agreementService.getAllAgreementVersions(agreementType, pageable);
        Map<Long, UserDto> userMap = getCreatedUsers(agreementPage);

        Page<AgreementResponse> agreementResponsePage = agreementPage.map(agreement -> {
            UserDto creatorDto = userMap.getOrDefault(agreement.getCreatedBy(), null);
            return AgreementResponse.fromDto(agreement, creatorDto);
        });

        log.info("[GET] Retrieved {} Agreement Versions", agreementPage.getTotalElements());
        return ResponseEntity.ok(agreementResponsePage);
    }

    @Operation(summary = "Get Agreement by Id")
    @GetMapping("/{id}")
    public ResponseEntity<AgreementResponse> getAgreementById(@PathVariable Long id) {
        log.info("[GET] Received request to get agreement by id: {}", id);
        AgreementDto agreementDto = agreementService.getAgreementById(id);
        AgreementResponse agreementResponse = AgreementResponse.fromDto(agreementDto);
        log.info("[GET] Retrieved {} Agreement by Id", agreementResponse.getId());
        return ResponseEntity.ok(agreementResponse);
    }

    @Operation(summary = "Update Agreement Version By Type")
    @PutMapping
    public ResponseEntity<AgreementResponse> updateAgreementVersionByType(@RequestParam AgreementType agreementType,
                                             @RequestBody @Valid AgreementRequest agreementRequest) {
        log.info("[PUT] Received request to update Agreement Version By Type");
        AgreementDto updatedAgreement = agreementService.updateAgreementByType(agreementType, agreementRequest.getContent());
        AgreementResponse agreementResponse = AgreementResponse.fromDto(updatedAgreement);
        log.info("[PUT] Updated Agreement Version to {} for Type {}", updatedAgreement.getVersion(), agreementType);
        return ResponseEntity.ok(agreementResponse);
    }

    private Map<Long, UserDto> getCreatedUsers(Page<AgreementDto> agreementPage) {
        List<Long> createdByUserIds = agreementPage.getContent().stream()
                .map(AgreementDto::getCreatedBy)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        List<UserDto> users = userService.getUsersByUserIds(createdByUserIds);

        log.info("[GET] Fetched details for {} unique users.", users.size());
        return users.stream().collect(Collectors.toMap(UserDto::getId, user -> user));
    }
}
