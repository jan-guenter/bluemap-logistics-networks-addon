/*
 * SPDX-License-Identifier: MIT
 */
package io.github.janguenter.bluemap.logisticsnetworks.adapter.bluemap523;

import de.bluecolored.bluemap.core.map.hires.block.BlockRendererType;
import de.bluecolored.bluemap.core.resources.ResourcePath;
import de.bluecolored.bluemap.core.resources.pack.PackVersion;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.ResourcePack;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.blockstate.BlockState;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.blockstate.Variant;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.blockstate.VariantSet;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.blockstate.Variants;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.model.Model;
import de.bluecolored.bluemap.core.util.Key;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OriginalRendererCatalogTest {

    private static final ResourcePath<Model> MODEL = new ResourcePath<>("test:block/host");

    @Test
    void preservesDefaultIdentityAcrossLateWrapperMutation() {
        ResourcePack pack = new ResourcePack(new PackVersion(34, 0));
        Variant defaultVariant = new Variant(MODEL);
        Variant customVariant = new Variant(MODEL);
        customVariant.setRenderer(renderer("test:custom"));
        pack.getBlockStates().put(Key.parse("test:default"), state(defaultVariant));
        pack.getBlockStates().put(Key.parse("test:custom"), state(customVariant));

        OriginalRendererCatalog catalog = OriginalRendererCatalog.capture(pack);
        BlockRendererType lateWrapper = renderer("bluemap_camol:overlay");
        defaultVariant.setRenderer(lateWrapper);
        customVariant.setRenderer(lateWrapper);

        assertTrue(catalog.wasDefault(defaultVariant));
        assertFalse(catalog.wasDefault(customVariant));
    }

    @Test
    void absentCatalogFailsClosedInProductionPreflight() {
        ResourcePack pack = new ResourcePack(new PackVersion(34, 0));
        Variant variant = new Variant(MODEL);

        assertFalse(new FullCubeHostPreflight(pack, null)
                .supportsVariantSet(new VariantSet(variant)));
    }

    private static BlockState state(Variant variant) {
        return new BlockState(new Variants(new VariantSet[0], new VariantSet(variant)));
    }

    private static BlockRendererType renderer(String key) {
        return new BlockRendererType.Impl(Key.parse(key), (pack, gallery, settings) -> null);
    }
}
