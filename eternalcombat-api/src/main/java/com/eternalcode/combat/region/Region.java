package com.eternalcode.combat.region;

import org.bukkit.Location;

public interface Region {

    Point getCenter();

    Location getMin();

    Location getMax();

    default boolean contains(Location location) {
        return this.contains(location.getX(), location.getY(), location.getZ());
    }

    default boolean contains(double x, double y, double z) {
        Location min = this.getMin();
        Location max = this.getMax();
        return x >= min.getX() && x < max.getX()
            && y >= min.getY() && y < max.getY()
            && z >= min.getZ() && z < max.getZ();
    }

    /**
     * If true, a player tagged inside this region may stay and move around in it. Once they leave they cannot come
     * back in until the fight ends. If false, they are pushed out.
     */
    default boolean keepsTaggedPlayersInside() {
        return false;
    }
}
