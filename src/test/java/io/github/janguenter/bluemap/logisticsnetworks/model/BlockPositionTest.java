/*
 * SPDX-License-Identifier: MIT
 */
package io.github.janguenter.bluemap.logisticsnetworks.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BlockPositionTest {

    @Test
    void roundTripsSignedPackedCoordinates() {
        for (BlockPosition position : new BlockPosition[]{
                new BlockPosition(0, 0, 0),
                new BlockPosition(1234, -64, -5678),
                new BlockPosition(33_554_431, 2_047, -33_554_432),
                new BlockPosition(-33_554_432, -2_048, 33_554_431)
        }) {
            assertEquals(position, BlockPosition.fromPacked(position.packed()));
        }
    }

    @Test
    void offsetsOnlyTheRequestedAxis() {
        BlockPosition origin = new BlockPosition(4, 5, 6);
        assertEquals(new BlockPosition(3, 5, 6), origin.offset(NodeDirection.WEST));
        assertEquals(new BlockPosition(4, 6, 6), origin.offset(NodeDirection.UP));
        assertEquals(new BlockPosition(4, 5, 7), origin.offset(NodeDirection.SOUTH));
    }
}
