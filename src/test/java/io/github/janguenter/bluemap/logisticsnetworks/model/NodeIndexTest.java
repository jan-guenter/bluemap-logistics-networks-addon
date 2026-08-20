/*
 * SPDX-License-Identifier: MIT
 */
package io.github.janguenter.bluemap.logisticsnetworks.model;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NodeIndexTest {

    private static final BlockPosition ORIGIN = new BlockPosition(0, 64, 0);

    @Test
    void hiddenNodesEmitNothingAndCannotConnect() {
        NodeIndex index = new NodeIndex();
        NodeSnapshot visible = new NodeSnapshot(ORIGIN, true, true);
        index.add(visible);
        index.add(new NodeSnapshot(ORIGIN.offset(NodeDirection.EAST), true, false));

        assertEquals(List.of(visible), index.visibleNodes());
        assertFalse(index.connectionsFor(visible).contains(NodeDirection.EAST));
    }

    @Test
    void visibleInvalidNodeRendersButIsNotEligibleAsNeighbor() {
        NodeIndex index = new NodeIndex();
        NodeSnapshot invalid = new NodeSnapshot(ORIGIN, false, true);
        NodeSnapshot valid = new NodeSnapshot(ORIGIN.offset(NodeDirection.EAST), true, true);
        index.add(invalid);
        index.add(valid);

        assertEquals(List.of(invalid, valid), index.visibleNodes());
        assertTrue(index.connectionsFor(invalid).contains(NodeDirection.EAST));
        assertFalse(index.connectionsFor(valid).contains(NodeDirection.WEST));
    }

    @Test
    void duplicateAttachedPositionSuppressesEveryClaimant() {
        NodeIndex index = new NodeIndex();
        index.add(new NodeSnapshot(ORIGIN, true, true));
        index.add(new NodeSnapshot(ORIGIN, false, true));
        index.add(new NodeSnapshot(ORIGIN, true, true));

        assertTrue(index.visibleNodes().isEmpty());
        assertTrue(index.isDuplicate(ORIGIN));
    }
}
