package ai.leadplus.api.v1.leadcontacts;

import ai.leadplus.application.apollo.ApolloLeadContactService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/v1")
@RequiredArgsConstructor
@ConditionalOnProperty(name = "apollo.enabled", havingValue = "true")
@Tag(name = "Contact (Apollo)", description = "Contact APIs related to Apollo")
public class ApolloLeadContactController {

    private final ApolloLeadContactService leadContactService;

    @Operation(summary = "Sync Contact Data from Apollo")
    @PostMapping("/contacts/{id}/sync/apollo")
    public ResponseEntity<Void> syncContactDataFromApollo(@PathVariable Long id) {
        log.info("[POST] Sync Contact id={} from Apollo", id);
        leadContactService.enrichContactWithApolloData(id);
        log.info("[POST] Synced Contact id={} from Apollo", id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Sync Contact Data from Apollo")
    @PostMapping("/contacts/bulk-sync/apollo")
    public ResponseEntity<Void> syncContactDataFromApollo(@RequestParam List<Long> ids) {
        log.info("[POST] Bulk Sync Contacts ids={} from Apollo", ids);
        leadContactService.enrichContactWithApolloData(ids);
        log.info("[POST] Bulk Synced Contacts ids={} from Apollo", ids);
        return ResponseEntity.noContent().build();
    }
}
