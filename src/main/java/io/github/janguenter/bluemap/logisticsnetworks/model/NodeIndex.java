/*
 * SPDX-License-Identifier: MIT
 */
package io.github.janguenter.bluemap.logisticsnetworks.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Deterministic position index that atomically suppresses duplicate positions. */
public final class NodeIndex {

    private final Map<BlockPosition, NodeSnapshot> nodes = new HashMap<>();
    private final Set<BlockPosition> duplicates = new HashSet<>();

    public void add(NodeSnapshot snapshot) {
        BlockPosition position = snapshot.attachedPos();
        if (duplicates.contains(position)) {
            return;
        }
        NodeSnapshot previous = nodes.putIfAbsent(position, snapshot);
        if (previous != null) {
            nodes.remove(position);
            duplicates.add(position);
        }
    }

    public List<NodeSnapshot> visibleNodes() {
        List<NodeSnapshot> result = new ArrayList<>();
        for (NodeSnapshot snapshot : nodes.values()) {
            if (snapshot.renderVisible()) {
                result.add(snapshot);
            }
        }
        result.sort((left, right) -> left.attachedPos().compareTo(right.attachedPos()));
        return List.copyOf(result);
    }

    public ConnectionMask connectionsFor(NodeSnapshot current) {
        ConnectionMask mask = ConnectionMask.empty();
        for (NodeDirection direction : NodeDirection.values()) {
            NodeSnapshot neighbor = nodes.get(current.attachedPos().offset(direction));
            if (neighbor != null && neighbor.renderVisible() && neighbor.valid()) {
                mask = mask.with(direction);
            }
        }
        return mask;
    }

    public boolean isDuplicate(BlockPosition position) {
        return duplicates.contains(position);
    }
}
