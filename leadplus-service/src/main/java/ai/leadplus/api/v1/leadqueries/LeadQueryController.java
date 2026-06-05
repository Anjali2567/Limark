package ai.leadplus.api.v1.leadqueries;

import ai.leadplus.application.leadqueries.LeadQueryDto;
import ai.leadplus.application.leadqueries.LeadQueryService;
import ai.leadplus.domain.leadqueries.LeadQueryType;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/v1/leads/queries")
@RequiredArgsConstructor
@Tag(name = "Lead Queries", description = "APIs for managing lead queries")
public class LeadQueryController {

    private final LeadQueryService leadQueryService;

    @Operation(summary = "Get Lead Queries by Types")
    @GetMapping()
    public ResponseEntity<Page<LeadQueryResponse>> getLeadQueriesByType(
            @RequestParam(required = false) String query,
            @RequestParam List<LeadQueryType> types,
            Pageable pageable
    ) {
        log.info("[GET] Received request to get Lead Queries with Query: {}", query);
        Page<LeadQueryDto> leadQueryDtoPage = leadQueryService.searchLeadQueries(query, types, pageable);
        Page<LeadQueryResponse> responsePage = leadQueryDtoPage
                .map(LeadQueryResponse::fromDto);
        log.info("[GET] Retrieved {} Lead Queries matching Query: {}", responsePage.getNumberOfElements(), query);
        return ResponseEntity.ok(responsePage);
    }
}
