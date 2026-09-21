package com.eternalcode.combat.region.huskclaims;

import com.eternalcode.combat.region.Point;
import com.eternalcode.combat.region.Region;
import org.bukkit.Location;
import org.bukkit.World;

/** A HuskClaims claim. The corners are block coordinates and both are inside the claim. */
public record HuskClaimsRegion(World world, int minX, int minZ, int maxX, int maxZ) implements Region {

    @Override
    public Point getCenter() {
        return new Point(this.world, (this.minX + this.maxX + 1) / 2.0, (this.minZ + this.maxZ + 1) / 2.0);
    }

    @Override
    public Location getMin() {
        return new Location(this.world, this.minX, this.world.getMinHeight(), this.minZ);
    }

    @Override
    public Location getMax() {
        return new Location(this.world, this.maxX, this.world.getMaxHeight() - 1, this.maxZ);
    }

    @Override
    public boolean contains(double x, double y, double z) {
        int blockX = (int) Math.floor(x);
        int blockZ = (int) Math.floor(z);
        return blockX >= this.minX && blockX <= this.maxX && blockZ >= this.minZ && blockZ <= this.maxZ;
    }

    @Override
    public boolean keepsTaggedPlayersInside() {
        return true;
    }
}
