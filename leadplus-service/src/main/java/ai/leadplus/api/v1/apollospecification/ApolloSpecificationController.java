package ai.leadplus.api.v1.apollospecification;

import ai.leadplus.application.apollospecification.ApolloSpecificationDto;
import ai.leadplus.application.apollospecification.ApolloSpecificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/v1/apollo-specification")
@RequiredArgsConstructor
@Tag(name = "Apollo Specification", description = "APIs for managing Apollo Specifications")
public class ApolloSpecificationController {

    private final ApolloSpecificationService apolloSpecificationService;

    @Operation(summary = "Get Latest Apollo Specification")
    @GetMapping
    public ResponseEntity<ApolloSpecificationResponse> getLatestApolloSpecification() {
        log.info("[GET] Received request to fetch latest Apollo Specification");
        ApolloSpecificationDto dto = apolloSpecificationService.getLatestSpecification();
        ApolloSpecificationResponse response = ApolloSpecificationResponse.fromDto(dto);
        log.info("[GET] Successfully fetched latest Apollo Specification with ID: {}", response.getId());
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Update Apollo Specification")
    @PutMapping
    public ResponseEntity<ApolloSpecificationResponse> updateApolloSpecification(@RequestBody ApolloSpecificationRequest request) {
        log.info("[PUT] Received request to update Apollo Specification");
        ApolloSpecificationDto dto = apolloSpecificationService.updateSpecification(request.toDto());
        ApolloSpecificationResponse response = ApolloSpecificationResponse.fromDto(dto);
        log.info("[PUT] Successfully updated Apollo Specification with ID: {}", response.getId());
        return ResponseEntity.ok(response);
    }
}
