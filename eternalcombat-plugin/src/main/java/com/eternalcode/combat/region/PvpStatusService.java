package com.eternalcode.combat.region;

import com.eternalcode.combat.fight.FightManager;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.Server;
import org.bukkit.entity.Player;

/**
 * Tells whether fights are blocked at a player's location (a protected region or claim). The region check reads world
 * state, so it is only done on the main thread; other threads (PlaceholderAPI may call async) get the value stored by
 * the last main thread check, which {@link #refresh()} keeps current for every online player.
 */
public class PvpStatusService {

    public enum Status {
        ALLOWED,
        DISABLED,
        IN_COMBAT
    }

    private final Map<UUID, Boolean> blocked = new ConcurrentHashMap<>();
    private final Server server;
    private final RegionProvider regionProvider;
    private final FightManager fightManager;

    public PvpStatusService(Server server, RegionProvider regionProvider, FightManager fightManager) {
        this.server = server;
        this.regionProvider = regionProvider;
        this.fightManager = fightManager;
    }

    /** A player in combat is reported as such, even inside a region that blocks fights. */
    public static Status resolve(boolean inCombat, boolean blocked) {
        if (inCombat) {
            return Status.IN_COMBAT;
        }

        return blocked ? Status.DISABLED : Status.ALLOWED;
    }

    public Status getStatus(Player player) {
        return resolve(this.fightManager.isInCombat(player.getUniqueId()), this.isBlocked(player));
    }

    /** Whether fights are not blocked here. Being in combat does not change this. */
    public boolean isPvpAllowed(Player player) {
        return !this.isBlocked(player);
    }

    private boolean isBlocked(Player player) {
        if (this.server.isPrimaryThread()) {
            return this.check(player);
        }

        return this.blocked.getOrDefault(player.getUniqueId(), false);
    }

    private boolean check(Player player) {
        boolean value = this.regionProvider.getRegion(player.getLocation()).isPresent();
        this.blocked.put(player.getUniqueId(), value);
        return value;
    }

    /** Must run on the main thread. */
    public void refresh() {
        for (Player player : this.server.getOnlinePlayers()) {
            try {
                this.check(player);
            }
            catch (RuntimeException exception) {
                this.blocked.remove(player.getUniqueId());
            }
        }

        this.blocked.keySet().removeIf(id -> this.server.getPlayer(id) == null);
    }
}
