/*
 * SPDX-License-Identifier: MIT
 */
package io.github.janguenter.bluemap.logisticsnetworks.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FramePlanTest {

    @Test
    void standaloneFrameHasEightJointsTwelveEdgesAndOneHundredTwentyQuads() {
        assertEquals(20, FramePlan.parts().size());
        assertEquals(120, FramePlan.quadCount(ConnectionMask.empty()));
    }

    @Test
    void allConnectionsRemoveOnlyTheThirtyTwoTouchingHorizontalFaces() {
        ConnectionMask mask = ConnectionMask.empty();
        for (NodeDirection direction : NodeDirection.values()) {
            mask = mask.with(direction);
        }
        assertEquals(88, FramePlan.quadCount(mask));
    }

    @Test
    void oneHorizontalConnectionLeavesOneHundredTwelveQuads() {
        assertEquals(
                112,
                FramePlan.quadCount(ConnectionMask.empty().with(NodeDirection.NORTH))
        );
    }

    @Test
    void onlyTouchingNeighborEnvelopesMayRemoveSeamFaces() {
        for (NodeDirection direction : NodeDirection.values()) {
            ConnectionMask oneConnection = ConnectionMask.empty().with(direction);
            if (direction == NodeDirection.DOWN || direction == NodeDirection.UP) {
                assertEquals(63F / 128F, FramePlan.neighborEnvelopeGap(direction));
                assertFalse(FramePlan.envelopesContact(direction));
                assertFalse(FramePlan.seamFacesMayBeOmitted(direction));
                assertEquals(120, FramePlan.quadCount(oneConnection));
            } else {
                assertEquals(-2F * FramePlan.OUTSET,
                        FramePlan.neighborEnvelopeGap(direction));
                assertTrue(FramePlan.envelopesContact(direction));
                assertTrue(FramePlan.seamFacesMayBeOmitted(direction));
                assertEquals(112, FramePlan.quadCount(oneConnection));
            }
        }
    }

    @Test
    void usesProvisionalProjectAuthoredDimensions() {
        assertEquals(1F / 8F, FramePlan.THICKNESS);
        assertEquals(1F / 256F, FramePlan.OUTSET);
        assertEquals(1F / 2F, FramePlan.Y_MIN);
        assertEquals(1F / 2F, FramePlan.HEIGHT);
        assertEquals(-FramePlan.OUTSET, minimumX());
        assertEquals(1F + FramePlan.OUTSET, maximumX());
        assertEquals(FramePlan.Y_MIN - FramePlan.OUTSET, minimumY());
        assertEquals(FramePlan.Y_MIN + FramePlan.HEIGHT + FramePlan.OUTSET, maximumY());
        assertEquals(-FramePlan.OUTSET, minimumZ());
        assertEquals(1F + FramePlan.OUTSET, maximumZ());
    }

    private static float minimumX() {
        return FramePlan.parts().stream()
                .map(FramePlan.Part::x0)
                .min(Float::compare)
                .orElseThrow();
    }

    private static float maximumX() {
        return FramePlan.parts().stream()
                .map(FramePlan.Part::x1)
                .max(Float::compare)
                .orElseThrow();
    }

    private static float minimumY() {
        return FramePlan.parts().stream()
                .map(FramePlan.Part::y0)
                .min(Float::compare)
                .orElseThrow();
    }

    private static float maximumY() {
        return FramePlan.parts().stream()
                .map(FramePlan.Part::y1)
                .max(Float::compare)
                .orElseThrow();
    }

    private static float minimumZ() {
        return FramePlan.parts().stream()
                .map(FramePlan.Part::z0)
                .min(Float::compare)
                .orElseThrow();
    }

    private static float maximumZ() {
        return FramePlan.parts().stream()
                .map(FramePlan.Part::z1)
                .max(Float::compare)
                .orElseThrow();
    }
}
