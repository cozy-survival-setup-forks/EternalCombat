package com.eternalcode.combat.fight.knockback;

import com.eternalcode.combat.fight.FightManager;
import com.eternalcode.combat.fight.event.FightTagEvent;
import com.eternalcode.combat.fight.event.FightUntagEvent;
import com.eternalcode.combat.notification.NoticeService;
import com.eternalcode.combat.region.Region;
import com.eternalcode.combat.region.RegionProvider;
import com.eternalcode.combat.region.RegionStayService;
import java.time.Duration;
import java.util.Optional;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.vehicle.VehicleMoveEvent;

public class KnockbackRegionController implements Listener {

    private final NoticeService noticeService;
    private final RegionProvider regionProvider;
    private final FightManager fightManager;
    private final KnockbackService knockbackService;
    private final Server server;
    private final RegionStayService stayService;

    public KnockbackRegionController(NoticeService noticeService, RegionProvider regionProvider, FightManager fightManager, KnockbackService knockbackService, Server server, RegionStayService stayService) {
        this.noticeService = noticeService;
        this.regionProvider = regionProvider;
        this.fightManager = fightManager;
        this.knockbackService = knockbackService;
        this.server = server;
        this.stayService = stayService;
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        if (!this.fightManager.isInCombat(player.getUniqueId())) {
            return;
        }

        Location locationTo = event.getTo();
        int xTo = locationTo.getBlockX();
        int yTo = locationTo.getBlockY();
        int zTo = locationTo.getBlockZ();

        Location locationFrom = event.getFrom();
        int xFrom = locationFrom.getBlockX();
        int yFrom = locationFrom.getBlockY();
        int zFrom = locationFrom.getBlockZ();

        if (xTo != xFrom || yTo != yFrom || zTo != zFrom) {
            Optional<Region> regionOptional = this.regionProvider.getRegion(locationTo);
            this.stayService.leave(player.getUniqueId(), regionOptional);
            if (regionOptional.isEmpty()) {
                return;
            }

            Region region = regionOptional.get();
            if (this.stayService.allows(player.getUniqueId(), region)) {
                return;
            }

            if (region.contains(locationFrom)) {
                this.knockbackService.knockback(region, player);
                this.knockbackService.forceKnockbackLater(player, region);
            } else {
                event.setCancelled(true);
                this.knockbackService.knockbackLater(region, player, Duration.ofMillis(50));
            }

            this.noticeService.create()
                .player(player.getUniqueId())
                .notice(config -> config.messagesSettings.cantEnterOnRegion)
                .send();
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    void onPlayerTeleport(PlayerTeleportEvent event) {
        Player player = event.getPlayer();
        if (!this.fightManager.isInCombat(player.getUniqueId())) {
            return;
        }

        Location targetLocation = event.getTo();

        if (this.regionProvider.isInRegion(targetLocation)) {
            event.setCancelled(true);
            this.noticeService.create()
                .player(player.getUniqueId())
                .notice(config -> config.messagesSettings.cantEnterOnRegion)
                .send();
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    void onVehicleMove(VehicleMoveEvent event) {
        Location locationTo = event.getTo();
        Location locationFrom = event.getFrom();

        if (locationTo.getBlockX() == locationFrom.getBlockX()
            && locationTo.getBlockY() == locationFrom.getBlockY()
            && locationTo.getBlockZ() == locationFrom.getBlockZ()) {
            return;
        }

        for (Entity passenger : event.getVehicle().getPassengers()) {
            if (!(passenger instanceof Player player)) {
                continue;
            }

            if (!this.fightManager.isInCombat(player.getUniqueId())) {
                continue;
            }

            Optional<Region> regionOptional = this.regionProvider.getRegion(locationTo);
            this.stayService.leave(player.getUniqueId(), regionOptional);
            if (regionOptional.isEmpty()) {
                return;
            }

            Region region = regionOptional.get();
            if (this.stayService.allows(player.getUniqueId(), region)) {
                continue;
            }

            if (region.contains(locationFrom)) {
                this.knockbackService.knockback(region, player);
                this.knockbackService.forceKnockbackLater(player, region);
            } else {
                this.knockbackService.knockbackLater(region, player, Duration.ofMillis(50));
            }

            this.noticeService.create()
                .player(player.getUniqueId())
                .notice(config -> config.messagesSettings.cantEnterOnRegion)
                .send();
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    void onTag(FightTagEvent event) {
        Player player = this.server.getPlayer(event.getPlayer());
        if (player == null) {
            throw new IllegalStateException("Player cannot be null!");
        }

        Optional<Region> regionOptional = this.regionProvider.getRegion(player.getLocation());
        if (regionOptional.isEmpty()) {
            return;
        }

        Region region = regionOptional.get();
        if (region.keepsTaggedPlayersInside()) {
            this.stayService.allow(player.getUniqueId(), region);
            return;
        }

        this.knockbackService.knockback(region, player);
        this.knockbackService.forceKnockbackLater(player, region);

        this.noticeService.create()
            .player(player.getUniqueId())
            .notice(config -> config.messagesSettings.cantEnterOnRegion)
            .send();
    }

    @EventHandler
    void onUntag(FightUntagEvent event) {
        this.stayService.forget(event.getPlayer());
    }

    @EventHandler
    void onQuit(PlayerQuitEvent event) {
        this.stayService.forget(event.getPlayer().getUniqueId());
    }
}
