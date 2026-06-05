package ai.leadplus.api.v1.admin.tenantactivity;

import ai.leadplus.application.tenantactivity.TenantActivityReport;
import ai.leadplus.application.tenantactivity.TenantReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v1/admin/analytics")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Tenant Report", description = "Tenant Report APIs for admin")
public class AdminReportController {

    private final TenantReportService tenantReportService;

    @GetMapping("/tenant-activity")
    @Operation(summary = "Historical activity for all tenants")
    public ResponseEntity<List<TenantActivityReport>> getAllTenantActivity(
            @RequestParam(defaultValue = "30daysAgo") String startDate,
            @RequestParam(defaultValue = "today") String endDate) {
        log.info("[GET] Received request to get All Tenant activities");
        List<TenantActivityReport> response = tenantReportService.getTenantActivityReport(startDate, endDate, null);
        log.info("[GET] Retrieved {} Tenant activities", response.size());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/tenant-activity/{tenantId}")
    @Operation(summary = "Historical activity for a specific tenant")
    public ResponseEntity<List<TenantActivityReport>> getTenantActivity(
            @PathVariable Long tenantId,
            @RequestParam(defaultValue = "30daysAgo") String startDate,
            @RequestParam(defaultValue = "today") String endDate) {
        log.info("[GET] Received request to get Tenant activities By Tenant Id: {}", tenantId);
        List<TenantActivityReport> response = tenantReportService.getTenantActivityReport(startDate, endDate, String.valueOf(tenantId));
        log.info("[GET] Retrieved {} Tenant activities", response.size());
        return ResponseEntity.ok(response);
    }
}
