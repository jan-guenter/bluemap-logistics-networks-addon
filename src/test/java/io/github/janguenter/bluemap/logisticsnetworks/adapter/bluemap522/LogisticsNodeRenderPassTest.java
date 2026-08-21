/*
 * SPDX-License-Identifier: MIT
 */
package io.github.janguenter.bluemap.logisticsnetworks.adapter.bluemap522;

import com.flowpowered.math.vector.Vector3d;
import de.bluecolored.bluemap.core.map.hires.ArrayTileModel;
import de.bluecolored.bluemap.core.map.hires.TileModelView;
import de.bluecolored.bluemap.core.world.mca.entity.MCAEntity;
import io.github.janguenter.bluemap.logisticsnetworks.model.BlockPosition;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LogisticsNodeRenderPassTest {

    @Test
    void admitsCenteredAndPlacementHeightEntityPositions() {
        BlockPosition attached = new BlockPosition(10, 70, -3);
        assertTrue(LogisticsNodeRenderPass.nearAttachedHost(
                new Vector3d(10.5D, 70.5D, -2.5D),
                attached
        ));
        assertTrue(LogisticsNodeRenderPass.nearAttachedHost(
                new Vector3d(10.5D, 70D, -2.5D),
                attached
        ));
    }

    @Test
    void rejectsRemoteAndNonFiniteEntityPositions() {
        BlockPosition attached = new BlockPosition(10, 70, -3);
        assertFalse(LogisticsNodeRenderPass.nearAttachedHost(
                new Vector3d(50D, 70D, -2.5D),
                attached
        ));
        assertFalse(LogisticsNodeRenderPass.nearAttachedHost(
                new Vector3d(11.5D, 70.5D, -2.5D),
                attached
        ));
        assertFalse(LogisticsNodeRenderPass.nearAttachedHost(
                new Vector3d(Double.NaN, 70D, -2.5D),
                attached
        ));
    }

    @Test
    void matchingEntityCollectionStopsAtomicallyAtTheCapacitySentinel() {
        List<LogisticsNodeEntityData> result = new ArrayList<>();
        int[] count = {0};
        LogisticsNodeRenderPass.collectOne(new MCAEntity(), result, count);
        assertEquals(0, count[0]);

        for (int index = 0; index <= LogisticsNodeRenderPass.MAX_NODE_ENTITIES; index++) {
            LogisticsNodeRenderPass.collectOne(
                    new LogisticsNodeEntityData(),
                    result,
                    count
            );
        }
        LogisticsNodeRenderPass.collectOne(new LogisticsNodeEntityData(), result, count);

        assertEquals(LogisticsNodeRenderPass.MAX_NODE_ENTITIES + 1, count[0]);
        assertEquals(LogisticsNodeRenderPass.MAX_NODE_ENTITIES, result.size());
        assertTrue(count[0] > LogisticsNodeRenderPass.MAX_NODE_ENTITIES);
    }

    @Test
    void passResetDropsOnlyPartialCustomGeometry() {
        ArrayTileModel model = new ArrayTileModel(16);
        model.add(7);
        TileModelView view = new TileModelView(model);
        view.add(9);

        LogisticsNodeRenderPass.resetPass(view, 7);

        assertEquals(7, model.size());
        assertEquals(7, view.getStart());
        assertEquals(0, view.getSize());
    }
}
