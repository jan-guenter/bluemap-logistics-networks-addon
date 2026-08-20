/*
 * SPDX-License-Identifier: MIT
 */
package io.github.janguenter.bluemap.logisticsnetworks.model;

import java.util.Objects;

/** Only stable fields admitted into the static node renderer. */
public record NodeSnapshot(BlockPosition attachedPos, boolean valid, boolean renderVisible) {

    public NodeSnapshot {
        Objects.requireNonNull(attachedPos, "attachedPos");
    }
}
