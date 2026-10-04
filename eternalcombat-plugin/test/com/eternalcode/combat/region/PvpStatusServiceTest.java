package com.eternalcode.combat.region;

import static com.eternalcode.combat.region.PvpStatusService.Status;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class PvpStatusServiceTest {

    @Test
    void combatWinsOverRegion() {
        assertEquals(Status.IN_COMBAT, PvpStatusService.resolve(true, true));
        assertEquals(Status.IN_COMBAT, PvpStatusService.resolve(true, false));
    }

    @Test
    void blockedRegionDisablesPvp() {
        assertEquals(Status.DISABLED, PvpStatusService.resolve(false, true));
    }

    @Test
    void otherwiseAllowed() {
        assertEquals(Status.ALLOWED, PvpStatusService.resolve(false, false));
    }
}
