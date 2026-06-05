package ai.leadplus.api.v1.leadcompanies;

import ai.leadplus.application.leadcompany.LeadCompanyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/v1/segments")
@RequiredArgsConstructor
@Tag(name = "Segment", description = "Segment APIs")
public class SegmentController {

    private final LeadCompanyService leadCompanyService;

    @Operation(summary = "Retrieve all the segments")
    @GetMapping
    public ResponseEntity<List<String>> getAllSegments() {
        log.info("[GET] Received request to get all segments");
        List<String> uniqueSegments = leadCompanyService.getUniqueSegments();
        log.info("[GET] Retrieved {} unique segments.", uniqueSegments.size());
        return ResponseEntity.ok(uniqueSegments);
    }
}
