package com.eternalcode.combat.region;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Remembers the region a player was tagged inside, for regions that let them stay. The player may move around in
 * it, but once they step out the region is forgotten, so they cannot go back in until the fight is over.
 */
public class RegionStayService {

    private final Map<UUID, Region> stays = new ConcurrentHashMap<>();

    public void allow(UUID player, Region region) {
        this.stays.put(player, region);
    }

    public boolean allows(UUID player, Region region) {
        return region.equals(this.stays.get(player));
    }

    public void forget(UUID player) {
        this.stays.remove(player);
    }

    /** Forgets the stay if the player is no longer in the region they were allowed to stay in. */
    public void leave(UUID player, java.util.Optional<Region> now) {
        Region stay = this.stays.get(player);
        if (stay != null && !now.map(stay::equals).orElse(false)) {
            this.stays.remove(player);
        }
    }
}
