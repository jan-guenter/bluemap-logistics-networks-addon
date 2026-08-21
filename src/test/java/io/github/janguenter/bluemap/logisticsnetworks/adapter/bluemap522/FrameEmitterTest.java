/*
 * SPDX-License-Identifier: MIT
 */
package io.github.janguenter.bluemap.logisticsnetworks.adapter.bluemap522;

import de.bluecolored.bluemap.core.util.Direction;
import io.github.janguenter.bluemap.logisticsnetworks.model.ConnectionMask;
import io.github.janguenter.bluemap.logisticsnetworks.model.FramePlan;
import io.github.janguenter.bluemap.logisticsnetworks.model.NodeDirection;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FrameEmitterTest {

    @Test
    void usesFrameWidePlanarTextureCoordinates() {
        assertEquals(0F, FrameEmitter.projectedU(
                Direction.NORTH, -FramePlan.OUTSET, 0F, 0F
        ));
        assertEquals(FramePlan.THICKNESS, FrameEmitter.projectedU(
                Direction.NORTH, FramePlan.THICKNESS, 0F, 0F
        ));
        assertEquals(1F - FramePlan.THICKNESS, FrameEmitter.projectedU(
                Direction.NORTH, 1F - FramePlan.THICKNESS, 0F, 0F
        ));
        assertEquals(1F, FrameEmitter.projectedU(
                Direction.NORTH, 1F + FramePlan.OUTSET, 0F, 0F
        ));
    }

    @Test
    void normalizesTheUpperHalfVerticalSpan() {
        assertEquals(1F, FrameEmitter.projectedV(
                Direction.SOUTH, 0F, FramePlan.Y_MIN - FramePlan.OUTSET, 0F
        ));
        assertEquals(1F - FramePlan.THICKNESS / FramePlan.HEIGHT,
                FrameEmitter.projectedV(
                        Direction.SOUTH, 0F, FramePlan.Y_MIN + FramePlan.THICKNESS, 0F
                ));
        assertEquals(FramePlan.THICKNESS / FramePlan.HEIGHT,
                FrameEmitter.projectedV(
                        Direction.SOUTH, 0F,
                        FramePlan.Y_MIN + FramePlan.HEIGHT - FramePlan.THICKNESS, 0F
                ));
        assertEquals(0F, FrameEmitter.projectedV(
                Direction.SOUTH, 0F,
                FramePlan.Y_MIN + FramePlan.HEIGHT + FramePlan.OUTSET, 0F
        ));
    }

    @Test
    void projectsTopAndSideFacesOnTheirOwnPlanes() {
        assertEquals(0.75F, FrameEmitter.projectedV(
                Direction.UP, 0F, 0F, 0.75F
        ));
        assertEquals(0.25F, FrameEmitter.projectedU(
                Direction.WEST, 0F, 0F, 0.25F
        ));
    }

    @Test
    void omitsOnlyCapsWhoseNeighborEnvelopeContactsTheFrame() {
        FramePlan.Part upperCorner = FramePlan.parts().stream()
                .filter(part -> part.touches(NodeDirection.UP))
                .findFirst()
                .orElseThrow();
        FramePlan.Part northCorner = FramePlan.parts().stream()
                .filter(part -> part.touches(NodeDirection.NORTH))
                .findFirst()
                .orElseThrow();

        assertFalse(FrameEmitter.omitted(
                NodeDirection.UP,
                upperCorner,
                ConnectionMask.empty().with(NodeDirection.UP)
        ));
        assertTrue(FrameEmitter.omitted(
                NodeDirection.NORTH,
                northCorner,
                ConnectionMask.empty().with(NodeDirection.NORTH)
        ));
    }
}
