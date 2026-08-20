/*
 * SPDX-License-Identifier: MIT
 */
package io.github.janguenter.bluemap.logisticsnetworks.adapter.bluemap522;

import com.flowpowered.math.vector.Vector3d;
import io.github.janguenter.bluemap.logisticsnetworks.model.BlockPosition;
import org.junit.jupiter.api.Test;

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
}
