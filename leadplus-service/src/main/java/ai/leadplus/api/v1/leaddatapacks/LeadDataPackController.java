package ai.leadplus.api.v1.leaddatapacks;

import ai.leadplus.application.leaddatapacks.LeadDataPackDto;
import ai.leadplus.application.leaddatapacks.LeadDataPackService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/lead-data-packs")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Lead Data Packs")
public class LeadDataPackController {

    private final LeadDataPackService leadDataPackService;

    @GetMapping("/{id}")
    @Operation(summary = "Get Lead Data Pack by ID")
    public ResponseEntity<LeadDataPackResponse> getById(@PathVariable String id) {
        log.info("[GET] Received request to get Lead Data Pack with id: {}", id);
        LeadDataPackDto dto = leadDataPackService.getById(id);
        log.info("[GET] Retrieved Lead Data Pack with id: {}", dto.getId());
        return ResponseEntity.ok(LeadDataPackResponse.fromDto(dto));
    }

    @GetMapping
    @Operation(summary = "Get all Lead Data Packs")
    public ResponseEntity<Page<LeadDataPackResponse>> getAllLeadDataPacks(Pageable pageable) {
        log.info("[GET] Received request to get Lead Data Packs with pagination");
        Page<LeadDataPackResponse> response =
                leadDataPackService.getAllLeadDataPacks(pageable)
                        .map(LeadDataPackResponse::fromDto);
        log.info("[GET] Retrieved Lead Data Packs with pagination");
        return ResponseEntity.ok(response);
    }

}


