package ai.leadplus.api.v1.leadcompanies;

import ai.leadplus.application.apollo.ApolloLeadCompanyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/v1/companies")
@RequiredArgsConstructor
@Tag(name = "Company (Apollo)", description = "Company APIs related to Apollo")
@ConditionalOnProperty(name = "apollo.enabled", havingValue = "true")
public class ApolloLeadCompanyController {

    private final ApolloLeadCompanyService leadCompanyService;

    @Operation(summary = "Sync Company Data from Apollo")
    @PostMapping("/{idOrDomain}/sync/apollo")
    public ResponseEntity<Void> syncCompanyDataFromApollo(@PathVariable String idOrDomain) {
        log.info("[POST] Received request to sync Company data from Apollo for ID/Domain: {}", idOrDomain);
        leadCompanyService.executePeopleSearchApollo(idOrDomain);
        log.info("[POST] Synced Company data from Apollo for ID/Domain: {}", idOrDomain);
        return ResponseEntity.accepted().build();
    }

    @Operation(summary = "Sync Company Organization Data from Apollo")
    @PostMapping("/{idOrDomain}/sync/apollo/organization")
    public ResponseEntity<Void> syncCompanyOrganizationDataFromApollo(@PathVariable String idOrDomain) {
        log.info("[POST] Received request to sync Company Organization data from Apollo for ID/Domain: {}", idOrDomain);
        leadCompanyService.enrichCompanyWithApolloOrganizationData(idOrDomain);
        log.info("[POST] Synced Company Organization data from Apollo for ID/Domain: {}", idOrDomain);
        return ResponseEntity.accepted().build();
    }
}
