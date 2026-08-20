/*
 * SPDX-License-Identifier: MIT
 */
package io.github.janguenter.bluemap.logisticsnetworks.adapter.bluemap522;

import io.github.janguenter.bluemap.logisticsnetworks.model.BlockPosition;
import io.github.janguenter.bluemap.logisticsnetworks.model.NodeSnapshot;

import java.util.Optional;

/** Strict decoder: all three exact custom fields must be present and typed. */
final class NodeSnapshotDecoder {

    Optional<NodeSnapshot> decode(LogisticsNodeEntityData data) {
        if (data == null || data.attachedPos() == null
                || data.valid() == null || data.renderVisible() == null) {
            return Optional.empty();
        }
        return Optional.of(new NodeSnapshot(
                BlockPosition.fromPacked(data.attachedPos()),
                data.valid(),
                data.renderVisible()
        ));
    }
}
