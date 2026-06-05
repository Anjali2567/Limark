package ai.leadplus.api.v1.unsubscribe;

import ai.leadplus.application.contactoutreachstatuses.ContactOutreachStatusService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/unsubscribe")
@Tag(name = "Unsubscribe", description = "Public unsubscribe APIs")
public class ContactUnsubscribeController {

    private final ContactOutreachStatusService contactOutreachStatusService;

    @Operation(summary = "Unsubscribe contact using unsubscribe token")
    @GetMapping
    public ResponseEntity<Void> unsubscribe(@RequestParam("token") String unsubscribeToken) {
        log.info("[GET] Unsubscribe request received. token={}", unsubscribeToken);
        contactOutreachStatusService.unsubscribeByToken(unsubscribeToken);
        log.info("[GET] Successfully unsubscribed contact");
        return ResponseEntity.ok().build();
    }
}