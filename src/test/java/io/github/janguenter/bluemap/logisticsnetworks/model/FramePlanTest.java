/*
 * SPDX-License-Identifier: MIT
 */
package io.github.janguenter.bluemap.logisticsnetworks.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FramePlanTest {

    @Test
    void standaloneFrameHasEightJointsTwelveEdgesAndOneHundredTwentyQuads() {
        assertEquals(20, FramePlan.parts().size());
        assertEquals(120, FramePlan.quadCount(ConnectionMask.empty()));
    }

    @Test
    void eachAxialConnectionRemovesEightMatchingFaces() {
        ConnectionMask mask = ConnectionMask.empty();
        for (NodeDirection direction : NodeDirection.values()) {
            mask = mask.with(direction);
        }
        assertEquals(72, FramePlan.quadCount(mask));
    }

    @Test
    void oneConnectionLeavesOneHundredTwelveQuads() {
        assertEquals(
                112,
                FramePlan.quadCount(ConnectionMask.empty().with(NodeDirection.NORTH))
        );
    }

    @Test
    void everyPartProtrudesBeyondTheStockUnitCube() {
        for (FramePlan.Part part : FramePlan.parts()) {
            assertTrue(part.x0() < 0F || part.y0() < 0F || part.z0() < 0F
                    || part.x1() > 1F || part.y1() > 1F || part.z1() > 1F);
        }
        assertEquals(-FramePlan.OUTSET, minimumCoordinate());
        assertEquals(1F + FramePlan.OUTSET, maximumCoordinate());
    }

    private static float minimumCoordinate() {
        return FramePlan.parts().stream()
                .map(part -> Math.min(part.x0(), Math.min(part.y0(), part.z0())))
                .min(Float::compare)
                .orElseThrow();
    }

    private static float maximumCoordinate() {
        return FramePlan.parts().stream()
                .map(part -> Math.max(part.x1(), Math.max(part.y1(), part.z1())))
                .max(Float::compare)
                .orElseThrow();
    }
}
