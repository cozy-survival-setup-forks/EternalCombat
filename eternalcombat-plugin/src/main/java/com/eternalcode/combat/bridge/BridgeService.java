package com.eternalcode.combat.bridge;

import com.eternalcode.combat.bridge.placeholder.FightTagPlaceholder;
import com.eternalcode.combat.config.implementation.PluginConfig;
import com.eternalcode.combat.fight.FightManager;
import com.eternalcode.combat.region.CompositeRegionProvider;
import com.eternalcode.combat.region.PvpStatusService;
import com.eternalcode.combat.region.bukkit.DefaultRegionProvider;
import com.eternalcode.combat.region.RegionProvider;
import com.eternalcode.combat.region.huskclaims.HuskClaimsRegionProvider;
import com.eternalcode.combat.region.worldguard.WorldGuardRegionProvider;
import com.eternalcode.commons.bukkit.scheduler.MinecraftScheduler;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import org.bukkit.Server;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;

public class BridgeService {

    private final PluginConfig config;
    private final PluginManager pluginManager;
    private final Logger logger;
    private final Plugin plugin;
    private final FightManager fightManager;

    private RegionProvider regionProvider;

    public BridgeService(
        PluginConfig config,
        PluginManager pluginManager,
        Logger logger,
        Plugin plugin,
        FightManager fightManager
    ) {
        this.config = config;
        this.pluginManager = pluginManager;
        this.logger = logger;
        this.plugin = plugin;
        this.fightManager = fightManager;
    }

    public void init(Server server, MinecraftScheduler scheduler) {
        List<RegionProvider> providers = new ArrayList<>();


        if (this.config.regions.protectHuskClaims) {
            this.initialize(
                "HuskClaims",
                () -> providers.add(new HuskClaimsRegionProvider()),
                () -> this.logger.warning("HuskClaims not found; skipping HuskClaimsRegionProvider.")
            );
        }

        this.initialize(
            "WorldGuard",
            () -> providers.add( new WorldGuardRegionProvider(this.config)),
            () -> this.logger.warning("WorldGuard not found; skipping WorldGuardRegionProvider.")
        );

        if (providers.isEmpty()) {
            providers.add(new DefaultRegionProvider(this.config.regions.restrictedRegionRadius));
            this.logger.warning("No region plugin found; using DefaultRegionProvider.");
        }

        this.regionProvider = new CompositeRegionProvider(providers);
        this.logger.info("Using composite region provider with: " +
            providers.stream()
                .map(p -> p.getClass().getSimpleName())
                .collect(Collectors.joining(", "))
        );

        initialize(
            "PlaceholderAPI",
            () -> {
                PvpStatusService pvpStatusService = new PvpStatusService(server, this.regionProvider, this.fightManager);
                scheduler.timer(pvpStatusService::refresh, Duration.ofSeconds(1), Duration.ofSeconds(1));
                new FightTagPlaceholder(this.config, this.fightManager, pvpStatusService, server, this.plugin).register();
            },
            () -> this.logger.warning("PlaceholderAPI not found; skipping placeholders.")
        );
    }

    private void initialize(String pluginName, BridgeInitializer initializer, Runnable onFailure) {
        if (this.pluginManager.isPluginEnabled(pluginName)) {
            initializer.initialize();
            this.logger.info("Initialized " + pluginName + " bridge.");
        }
        else {
            onFailure.run();
        }
    }

    public RegionProvider getRegionProvider() {
        return this.regionProvider;
    }
}
