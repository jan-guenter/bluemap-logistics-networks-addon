/*
 * SPDX-License-Identifier: MIT
 */
package io.github.janguenter.bluemap.logisticsnetworks.adapter.bluemap523;

import de.bluecolored.bluemap.core.map.hires.block.BlockRendererType;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.ResourcePack;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.blockstate.Variant;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Set;

/** Records default-renderer variants before late resource extensions wrap them. */
final class OriginalRendererCatalog {

    private final Set<Variant> defaultVariants;

    private OriginalRendererCatalog(Set<Variant> defaultVariants) {
        this.defaultVariants = Collections.unmodifiableSet(defaultVariants);
    }

    static OriginalRendererCatalog capture(ResourcePack resourcePack) {
        Set<Variant> captured = Collections.newSetFromMap(new IdentityHashMap<>());
        resourcePack.getBlockStates().values().forEach(state -> state.forEach(variant -> {
            if (variant != null && variant.getRenderer() == BlockRendererType.DEFAULT) {
                captured.add(variant);
            }
        }));
        return new OriginalRendererCatalog(captured);
    }

    boolean wasDefault(Variant variant) {
        return defaultVariants.contains(variant);
    }

    int size() {
        return defaultVariants.size();
    }
}
