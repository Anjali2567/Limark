package ai.leadplus.api.v1.facts;

import ai.leadplus.application.facts.FactDto;
import ai.leadplus.application.facts.FactService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/v1/facts")
@RequiredArgsConstructor
@Tag(name = "Facts", description = "Facts APIs")
public class FactController {
    
    private final FactService factService;

    @Operation(summary = "Add New Fact")
    @PostMapping
    public ResponseEntity<FactResponse> addFact(@Valid @RequestBody FactRequest factRequest) {
        log.info("[POST] Received request to create new Fact");
        FactDto factDto = factService.addFact(factRequest.toDto());
        FactResponse factResponse = FactResponse.toResponse(factDto);
        log.info("[POST] Created Fact with ID: {}", factResponse.getId());
        return new ResponseEntity<>(factResponse, HttpStatus.CREATED);
    }

    @Operation(summary = "Update Fact by ID")
    @PutMapping("/{id}")
    public ResponseEntity<FactResponse> updateFactById(
            @PathVariable Long id,
            @Valid @RequestBody FactRequest factRequest) {
        log.info("[PUT] Received request to update Fact with ID: {}", id);
        FactDto factDto = factService.updateFact(id, factRequest.toDto());
        FactResponse factResponse = FactResponse.toResponse(factDto);
        log.info("[PUT] Updated Fact with ID: {}", id);
        return ResponseEntity.ok(factResponse);
    }

    @Operation(summary = "Delete Fact by ID")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteFactById(@PathVariable Long id) {
        log.info("[DELETE] Received request to delete Fact with ID: {}", id);
        factService.deleteFact(id);
        log.info("[DELETE] Deleted Fact with ID: {}", id);
        return ResponseEntity.noContent().build();
    }
}