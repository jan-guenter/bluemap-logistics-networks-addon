/*
 * SPDX-License-Identifier: MIT
 */
package io.github.janguenter.bluemap.logisticsnetworks.adapter.bluemap522;

import de.bluecolored.bluemap.core.resources.pack.resourcepack.ResourcePack;
import de.bluecolored.bluemap.core.util.Key;
import io.github.janguenter.bluemap.logisticsnetworks.activation.LogisticsNetworksRuntime;

/** Resource extension factory installed before resource-pack construction. */
final class LogisticsNetworksResourceExtensionType
        implements ResourcePack.Extension<LogisticsNetworksResourceExtension> {

    static final Key KEY = Key.parse("bluemap_logistics_networks:exact_profile");

    private final LogisticsNetworksRuntime runtime;

    LogisticsNetworksResourceExtensionType(LogisticsNetworksRuntime runtime) {
        this.runtime = runtime;
    }

    @Override
    public Key getKey() {
        return KEY;
    }

    @Override
    public LogisticsNetworksResourceExtension create(ResourcePack pack) {
        return new LogisticsNetworksResourceExtension(pack, runtime);
    }
}
