package com.eternalcode.combat.region;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;
import java.util.UUID;
import org.bukkit.Location;
import org.junit.jupiter.api.Test;

class RegionStayServiceTest {

    private record Box(int id) implements Region {
        public Point getCenter() {
            return null;
        }

        public Location getMin() {
            return null;
        }

        public Location getMax() {
            return null;
        }
    }

    private final RegionStayService service = new RegionStayService();
    private final UUID player = UUID.randomUUID();
    private final Region home = new Box(1);
    private final Region other = new Box(2);

    @Test
    void aPlayerMayStayOnlyInTheRegionTheyWereTaggedIn() {
        service.allow(player, home);

        assertTrue(service.allows(player, home));
        assertFalse(service.allows(player, other));
        assertFalse(service.allows(UUID.randomUUID(), home));
    }

    @Test
    void movingAroundInsideKeepsTheStay() {
        service.allow(player, home);
        service.leave(player, Optional.of(home));

        assertTrue(service.allows(player, home));
    }

    @Test
    void oncePlayerStepsOutTheyCannotComeBack() {
        service.allow(player, home);

        service.leave(player, Optional.empty());
        assertFalse(service.allows(player, home));

        service.leave(player, Optional.of(home));
        assertFalse(service.allows(player, home));
    }

    @Test
    void steppingIntoAnotherRegionEndsTheStay() {
        service.allow(player, home);
        service.leave(player, Optional.of(other));

        assertFalse(service.allows(player, home));
    }
}
