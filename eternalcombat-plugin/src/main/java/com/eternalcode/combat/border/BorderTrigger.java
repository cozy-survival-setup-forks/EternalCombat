package com.eternalcode.combat.border;

import com.eternalcode.combat.region.Region;

record BorderTrigger(BorderPoint min, BorderPoint max, BorderPoint triggerMin, BorderPoint triggerMax, Region region) {

    BorderTrigger(int minX, int minY, int minZ, int maxX, int maxY, int maxZ, int distance, Region region) {
        this(
            new BorderPoint(minX, minY, minZ),
            new BorderPoint(maxX, maxY, maxZ),
            new BorderPoint(minX - distance, minY - distance, minZ - distance),
            new BorderPoint(maxX + distance, maxY + distance, maxZ + distance),
            region
        );
    }

    public boolean isTriggered(int x, int y, int z) {
        return x >= triggerMin.x() && x <= triggerMax.x()
            && y >= triggerMin.y() && y <= triggerMax.y()
            && z >= triggerMin.z() && z <= triggerMax.z();
    }

}
