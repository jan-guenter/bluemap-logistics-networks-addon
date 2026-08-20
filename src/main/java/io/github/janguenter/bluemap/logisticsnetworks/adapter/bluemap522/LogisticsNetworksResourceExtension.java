/*
 * SPDX-License-Identifier: MIT
 */
package io.github.janguenter.bluemap.logisticsnetworks.adapter.bluemap522;

import de.bluecolored.bluemap.core.resources.pack.resourcepack.ResourcePack;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.ResourcePackExtension;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.texture.Texture;
import de.bluecolored.bluemap.core.util.Key;
import io.github.janguenter.bluemap.logisticsnetworks.activation.LogisticsNetworksRuntime;
import io.github.janguenter.bluemap.logisticsnetworks.profile.ExactLogisticsNetworksArtifactDetector;
import io.github.janguenter.bluemap.logisticsnetworks.profile.LogisticsNetworks1101Profile;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.Set;

/** Exact artifact activation and operator-texture admission. */
final class LogisticsNetworksResourceExtension implements ResourcePackExtension {

    private final ResourcePack resourcePack;
    private final LogisticsNetworksRuntime runtime;

    LogisticsNetworksResourceExtension(
            ResourcePack resourcePack,
            LogisticsNetworksRuntime runtime
    ) {
        this.resourcePack = resourcePack;
        this.runtime = runtime;
    }

    @Override
    public void loadResources(Iterable<Path> roots) {
        if (!ExactLogisticsNetworksArtifactDetector.matches(
                roots,
                LogisticsNetworks1101Profile.JAR_SHA256,
                LogisticsNetworks1101Profile.JAR_SIZE
        )) {
            runtime.inactive("exact-artifact-missing-or-duplicate");
            return;
        }
        if (resourcePack.getEntityStates().get(LogisticsNetworks1101Profile.ENTITY_KEY) != null) {
            runtime.inactive("unexpected-entity-state-route");
            return;
        }
        runtime.activate();
    }

    @Override
    public Set<Key> collectUsedTextureKeys() {
        return runtime.isActive()
                ? Set.of(LogisticsNetworks1101Profile.NODE_TEXTURE)
                : Set.of();
    }

    @Override
    public void bake() {
        if (!runtime.isActive()) {
            return;
        }
        Texture texture = resourcePack.getTextures().get(LogisticsNetworks1101Profile.NODE_TEXTURE);
        if (texture == null) {
            runtime.inactive("node-texture-missing");
            return;
        }
        try {
            BufferedImage image = texture.getTextureImage();
            if (image == null
                    || image.getWidth() != LogisticsNetworks1101Profile.NODE_TEXTURE_WIDTH
                    || image.getHeight() != LogisticsNetworks1101Profile.NODE_TEXTURE_HEIGHT
                    || !LogisticsNetworks1101Profile.NODE_TEXTURE_RGBA_SHA256.equals(
                            rgbaSha256(image)
                    )) {
                runtime.inactive("node-texture-fingerprint-mismatch");
            }
        } catch (IOException | RuntimeException exception) {
            runtime.inactive("node-texture-unreadable");
        }
    }

    static String rgbaSha256(BufferedImage image) {
        MessageDigest digest;
        try {
            digest = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 unavailable", exception);
        }
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                int argb = image.getRGB(x, y);
                digest.update((byte) (argb >>> 16));
                digest.update((byte) (argb >>> 8));
                digest.update((byte) argb);
                digest.update((byte) (argb >>> 24));
            }
        }
        return HexFormat.of().formatHex(digest.digest());
    }
}
