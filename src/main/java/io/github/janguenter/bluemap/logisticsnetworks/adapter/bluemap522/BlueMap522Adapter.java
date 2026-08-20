/*
 * SPDX-License-Identifier: MIT
 */
package io.github.janguenter.bluemap.logisticsnetworks.adapter.bluemap522;

import de.bluecolored.bluemap.core.map.hires.RenderPassType;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.ResourcePack;
import de.bluecolored.bluemap.core.util.Keyed;
import de.bluecolored.bluemap.core.util.Registry;
import de.bluecolored.bluemap.core.world.mca.entity.EntityType;
import io.github.janguenter.bluemap.logisticsnetworks.activation.LogisticsNetworksRuntime;
import io.github.janguenter.bluemap.logisticsnetworks.profile.LogisticsNetworks1101Profile;

/** BlueMap 5.22 registration boundary. */
public final class BlueMap522Adapter {

    private static final LogisticsNetworksRuntime RUNTIME =
            LogisticsNetworksRuntime.INSTANCE;
    private static final EntityType ENTITY_TYPE = new EntityType.Impl(
            LogisticsNetworks1101Profile.ENTITY_KEY,
            LogisticsNodeEntityData.class
    );
    private static final ResourcePack.Extension<LogisticsNetworksResourceExtension> EXTENSION =
            new LogisticsNetworksResourceExtensionType(RUNTIME);
    private static final RenderPassType RENDER_PASS = new RenderPassType.Impl(
            de.bluecolored.bluemap.core.util.Key.parse(
                    "bluemap_logistics_networks:logistics_nodes"
            ),
            (pack, gallery, settings) -> new LogisticsNodeRenderPass(
                    pack,
                    gallery,
                    settings,
                    RUNTIME
            )
    );

    private BlueMap522Adapter() {
    }

    /** Registers the entity projection before BlueNBT reads world entities. */
    public static synchronized boolean install() {
        if (!canRegister(EntityType.REGISTRY, ENTITY_TYPE)
                || !canRegister(ResourcePack.Extension.REGISTRY, EXTENSION)
                || !canRegister(RenderPassType.REGISTRY, RENDER_PASS)) {
            RUNTIME.fail("registry-collision");
            return false;
        }
        if (!register(EntityType.REGISTRY, ENTITY_TYPE)
                || !register(ResourcePack.Extension.REGISTRY, EXTENSION)
                || !register(RenderPassType.REGISTRY, RENDER_PASS)) {
            RUNTIME.fail("registry-registration-failed");
            return false;
        }
        return true;
    }

    private static <T extends Keyed> boolean canRegister(Registry<T> registry, T candidate) {
        T existing = registry.get(candidate.getKey());
        return existing == null || existing == candidate;
    }

    private static <T extends Keyed> boolean register(Registry<T> registry, T candidate) {
        T existing = registry.get(candidate.getKey());
        if (existing == null) {
            registry.register(candidate);
            existing = registry.get(candidate.getKey());
        }
        return existing == candidate;
    }
}
