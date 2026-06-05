package ai.leadplus.application.datapacks;

import ai.leadplus.application.leaddatapacks.GatedInfo;
import ai.leadplus.application.vendordatapacks.VendorAccess;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DataPackGateTest {

    private DataPackGate gate;

    @BeforeEach
    void setUp() {
        gate = new DataPackGate();
    }

    // --- Segment overlap match ---

    @Test
    void accessible_whenSegmentsOverlap() {
        GatedInfo gatedInfo = new GatedInfo(List.of("Robotics", "Automate26"), true);
        VendorAccess vendorAccess = new VendorAccess(List.of(), List.of("Robotics"), false, 23L);

        assertTrue(gate.isAccessible(List.of("Robotics", "AI"), List.of(), gatedInfo, vendorAccess));
    }

    // --- No segment overlap ---

    @Test
    void inaccessible_whenNoSegmentOverlap() {
        GatedInfo gatedInfo = new GatedInfo(List.of("Robotics", "Automate26"), true);
        VendorAccess vendorAccess = new VendorAccess(List.of(), List.of("Robotics"), false, 23L);

        assertFalse(gate.isAccessible(List.of("Automate26"), List.of(), gatedInfo, vendorAccess));
    }

    // --- Null-segment entity + vendor has null access ---

    @Test
    void accessible_whenNullSegmentsAndVendorHasNullAccess() {
        GatedInfo gatedInfo = new GatedInfo(List.of("Robotics"), true);
        VendorAccess vendorAccess = new VendorAccess(List.of(), List.of(), true, 23L);

        assertTrue(gate.isAccessible(null, null, gatedInfo, vendorAccess));
        assertTrue(gate.isAccessible(Collections.emptyList(), null, gatedInfo, vendorAccess));
    }

    // --- Null-segment entity + vendor does NOT have null access ---

    @Test
    void inaccessible_whenNullSegmentsAndVendorLacksNullAccess() {
        GatedInfo gatedInfo = new GatedInfo(List.of("Robotics"), true);
        VendorAccess vendorAccess = new VendorAccess(List.of(), List.of("Robotics"), false, 23L);

        assertFalse(gate.isAccessible(null, null, gatedInfo, vendorAccess));
        assertFalse(gate.isAccessible(Collections.emptyList(), null, gatedInfo, vendorAccess));
    }

    // --- Tenant match ---

    @Test
    void accessible_whenTenantMatches() {
        GatedInfo gatedInfo = new GatedInfo(List.of("Robotics"), false);
        VendorAccess vendorAccess = new VendorAccess(List.of(), List.of("Robotics"), false, 23L);

        assertTrue(gate.isAccessible(List.of("Robotics"), List.of("23"), gatedInfo, vendorAccess));
    }

    // --- Tenant mismatch ---

    @Test
    void inaccessible_whenTenantMismatch() {
        GatedInfo gatedInfo = new GatedInfo(List.of("Robotics"), false);
        VendorAccess vendorAccess = new VendorAccess(List.of(), List.of("Robotics"), false, 23L);

        assertFalse(gate.isAccessible(List.of("Robotics"), List.of("99"), gatedInfo, vendorAccess));
    }

    @Test
    void inaccessible_whenTenantIdsPresent_butVendorHasNoTenant() {
        GatedInfo gatedInfo = new GatedInfo(List.of("Robotics"), false);
        VendorAccess vendorAccess = new VendorAccess(List.of(), List.of("Robotics"), false, null);

        assertFalse(gate.isAccessible(List.of("Robotics"), List.of("99"), gatedInfo, vendorAccess));
    }

    // --- Public data (null tenantIds) ---

    @Test
    void accessible_whenTenantIdsNullOrEmpty() {
        GatedInfo gatedInfo = new GatedInfo(List.of("Robotics"), false);
        VendorAccess vendorAccess = new VendorAccess(List.of(), List.of("Robotics"), false, 23L);

        assertTrue(gate.isAccessible(List.of("Robotics"), null, gatedInfo, vendorAccess));
        assertTrue(gate.isAccessible(List.of("Robotics"), Collections.emptyList(), gatedInfo, vendorAccess));
    }

    // --- No gating active ---

    @Test
    void accessible_whenNoGatingActive() {
        GatedInfo gatedInfo = new GatedInfo(Collections.emptyList(), false);
        VendorAccess vendorAccess = new VendorAccess(List.of(), List.of(), false, 23L);

        assertTrue(gate.isAccessible(List.of("Anything"), null, gatedInfo, vendorAccess));
        assertTrue(gate.isAccessible(null, null, gatedInfo, vendorAccess));
    }

    @Test
    void accessible_whenGatedInfoIsNull() {
        VendorAccess vendorAccess = new VendorAccess(List.of(), List.of(), false, 23L);

        assertTrue(gate.isAccessible(List.of("Anything"), null, null, vendorAccess));
    }

    // --- Null vendorAccess with gating active (fail closed) ---

    @Test
    void inaccessible_whenVendorAccessNullAndGatingActive() {
        GatedInfo gatedInfo = new GatedInfo(List.of("Robotics"), true);

        assertFalse(gate.isAccessible(List.of("Robotics"), null, gatedInfo, null));
        assertFalse(gate.isAccessible(null, null, gatedInfo, null));
    }

    // --- Entity has segments but none are gated -> accessible ---

    @Test
    void accessible_whenEntitySegmentsNotGated() {
        GatedInfo gatedInfo = new GatedInfo(List.of("Robotics"), false);
        VendorAccess vendorAccess = new VendorAccess(List.of(), List.of(), false, 23L);

        // Entity has "Healthcare" which is not in gatedInfo's named segments
        assertTrue(gate.isAccessible(List.of("Healthcare"), null, gatedInfo, vendorAccess));
    }

    // --- Combined: segments match but tenant blocks ---

    @Test
    void inaccessible_whenSegmentsMatchButTenantBlocks() {
        GatedInfo gatedInfo = new GatedInfo(List.of("Robotics"), false);
        VendorAccess vendorAccess = new VendorAccess(List.of(), List.of("Robotics"), false, 23L);

        assertFalse(gate.isAccessible(List.of("Robotics"), List.of("99"), gatedInfo, vendorAccess));
    }

    // --- Vendor has null access AND named segments ---

    @Test
    void accessible_whenVendorHasBothNullAccessAndNamedSegments() {
        GatedInfo gatedInfo = new GatedInfo(List.of("Robotics"), true);
        VendorAccess vendorAccess = new VendorAccess(List.of(), List.of("Robotics"), true, 23L);

        // Named segment entity
        assertTrue(gate.isAccessible(List.of("Robotics"), null, gatedInfo, vendorAccess));
        // Null segment entity
        assertTrue(gate.isAccessible(null, null, gatedInfo, vendorAccess));
    }

    // --- Vendor access null with tenant-gated entity ---

    @Test
    void inaccessible_whenVendorAccessNullAndEntityHasTenants() {
        assertFalse(gate.isAccessible(List.of("Robotics"), List.of("23"), null, null));
    }
}
