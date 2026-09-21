package com.eternalcode.combat.region.huskclaims;

import com.eternalcode.combat.region.Region;
import com.eternalcode.combat.region.RegionProvider;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import net.william278.huskclaims.api.BukkitHuskClaimsAPI;
import net.william278.huskclaims.claim.Claim;
import org.bukkit.Location;
import org.bukkit.World;

/** Claims as regions. A child claim counts as part of its parent, so walking between them is not entering. */
public class HuskClaimsRegionProvider implements RegionProvider {

    private final BukkitHuskClaimsAPI api = BukkitHuskClaimsAPI.getInstance();

    @Override
    public Optional<Region> getRegion(Location location) {
        if (location.getWorld() == null) {
            return Optional.empty();
        }

        return this.api.getClaimAt(this.api.getPosition(location))
            .map(claim -> claim.getParent().orElse(claim))
            .map(claim -> toRegion(location.getWorld(), claim));
    }

    @Override
    public Collection<Region> getRegions(World world) {
        return this.api.getClaimWorld(world.getName())
            .map(claimWorld -> claimWorld.getClaims().stream()
                .filter(claim -> !claim.isChildClaim())
                .map(claim -> toRegion(world, claim))
                .toList())
            .orElse(List.of());
    }

    private static Region toRegion(World world, Claim claim) {
        var near = claim.getRegion().getNearCorner();
        var far = claim.getRegion().getFarCorner();
        return new HuskClaimsRegion(
            world,
            Math.min(near.getBlockX(), far.getBlockX()),
            Math.min(near.getBlockZ(), far.getBlockZ()),
            Math.max(near.getBlockX(), far.getBlockX()),
            Math.max(near.getBlockZ(), far.getBlockZ())
        );
    }
}
