/*
 * SPDX-License-Identifier: MIT
 */
package io.github.janguenter.bluemap.logisticsnetworks.adapter.bluemap523;

import de.bluecolored.bluemap.core.map.hires.RenderPassType;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.ResourcePack;
import de.bluecolored.bluemap.core.util.Key;
import de.bluecolored.bluemap.core.world.mca.entity.EntityType;
import io.github.janguenter.bluemap.addon.adapter.api.bluemap523.RegistryGuard;
import io.github.janguenter.bluemap.addon.adapter.api.bluemap523.ResourceExtensionType;
import io.github.janguenter.bluemap.logisticsnetworks.activation.LogisticsNetworksRuntime;
import io.github.janguenter.bluemap.logisticsnetworks.profile.LogisticsNetworks1101Profile;

/** BlueMap 5.23 feature-backport registration boundary. */
public final class BlueMap523Adapter {

    private static final LogisticsNetworksRuntime RUNTIME =
            LogisticsNetworksRuntime.INSTANCE;
    private static final EntityType ENTITY_TYPE = new EntityType.Impl(
            LogisticsNetworks1101Profile.ENTITY_KEY,
            LogisticsNodeEntityData.class
    );
    private static final ResourcePack.Extension<LogisticsNetworksResourceExtension> EXTENSION =
            new ResourceExtensionType<>(
                    Key.parse("bluemap_logistics_networks:exact_profile"),
                    pack -> new LogisticsNetworksResourceExtension(pack, RUNTIME)
            );
    private static final RenderPassType RENDER_PASS = new RenderPassType.Impl(
            Key.parse("bluemap_logistics_networks:logistics_nodes"),
            (pack, gallery, settings) -> {
                LogisticsNetworksResourceExtension extension = pack.getExtension(EXTENSION);
                return new LogisticsNodeRenderPass(
                        pack,
                        gallery,
                        settings,
                        RUNTIME,
                        extension == null ? null : extension.originalRenderers()
                );
            }
    );

    private BlueMap523Adapter() {
    }

    /** Registers the entity projection before BlueNBT reads world entities. */
    public static synchronized boolean install() {
        if (!RegistryGuard.canRegister(EntityType.REGISTRY, ENTITY_TYPE)
                || !RegistryGuard.canRegister(ResourcePack.Extension.REGISTRY, EXTENSION)
                || !RegistryGuard.canRegister(RenderPassType.REGISTRY, RENDER_PASS)) {
            RUNTIME.fail("registry-collision");
            return false;
        }
        if (!RegistryGuard.register(EntityType.REGISTRY, ENTITY_TYPE)
                || !RegistryGuard.register(ResourcePack.Extension.REGISTRY, EXTENSION)
                || !RegistryGuard.register(RenderPassType.REGISTRY, RENDER_PASS)) {
            RUNTIME.fail("registry-registration-failed");
            return false;
        }
        return true;
    }

}
