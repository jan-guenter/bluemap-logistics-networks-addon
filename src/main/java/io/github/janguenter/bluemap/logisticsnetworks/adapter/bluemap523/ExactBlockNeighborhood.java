/*
 * SPDX-License-Identifier: MIT
 */
package io.github.janguenter.bluemap.logisticsnetworks.adapter.bluemap523;

import de.bluecolored.bluemap.core.map.hires.RenderSettings;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.ResourcePack;
import de.bluecolored.bluemap.core.world.DimensionType;
import de.bluecolored.bluemap.core.world.block.BlockAccess;
import de.bluecolored.bluemap.core.world.block.BlockNeighborhood;

/** Prevents sparse positions from aliasing BlueMap's modulo-indexed current block. */
final class ExactBlockNeighborhood extends BlockNeighborhood {

    ExactBlockNeighborhood(
            BlockAccess blockAccess,
            ResourcePack resourcePack,
            RenderSettings renderSettings,
            DimensionType dimensionType
    ) {
        super(blockAccess, resourcePack, renderSettings, dimensionType);
    }

    @Override
    public void set(int x, int y, int z) {
        int primeX = x == Integer.MAX_VALUE ? x - 1 : x + 1;
        super.set(primeX, y, z);
        super.set(x, y, z);
    }
}
