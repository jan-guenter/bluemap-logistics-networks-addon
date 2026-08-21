/*
 * SPDX-License-Identifier: MIT
 */
package io.github.janguenter.bluemap.logisticsnetworks.adapter.bluemap522;

import de.bluecolored.bluemap.core.resources.pack.resourcepack.ResourcePack;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.texture.Texture;
import de.bluecolored.bluemap.core.util.Key;

import java.awt.image.BufferedImage;
import java.io.IOException;

/** Generates and validates the project-owned translucent sheet texture in memory. */
final class SyntheticSheetTexture {

    static final Key KEY = Key.parse("bluemap_logistics_networks:generated/top_sheet");
    static final int WIDTH = 1;
    static final int HEIGHT = 1;
    static final int RED = 232;
    static final int GREEN = 236;
    static final int BLUE = 236;
    static final int ALPHA = 48;
    static final int ARGB = ALPHA << 24 | RED << 16 | GREEN << 8 | BLUE;

    private SyntheticSheetTexture() {
    }

    static InstallResult install(ResourcePack resourcePack) {
        return install(resourcePack, Texture::from);
    }

    static InstallResult install(ResourcePack resourcePack, TextureFactory factory) {
        if (resourcePack == null || factory == null) {
            return InstallResult.FAILED;
        }
        if (resourcePack.getTextures().containsKey(KEY)) {
            return InstallResult.COLLISION;
        }

        BufferedImage image = image();
        Texture texture;
        try {
            texture = factory.create(KEY, image);
            if (!isExact(texture)) {
                return InstallResult.FAILED;
            }
        } catch (IOException | RuntimeException exception) {
            return InstallResult.FAILED;
        }

        resourcePack.getTextures().put(KEY, texture);
        return InstallResult.INSTALLED;
    }

    static BufferedImage image() {
        BufferedImage image = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_ARGB);
        image.setRGB(0, 0, ARGB);
        return image;
    }

    static boolean isExact(Texture texture) {
        if (texture == null || !KEY.equals(texture.getKey()) || !texture.isHalfTransparent()) {
            return false;
        }
        try {
            BufferedImage image = texture.getTextureImage();
            return image != null
                    && image.getWidth() == WIDTH
                    && image.getHeight() == HEIGHT
                    && image.getRGB(0, 0) == ARGB;
        } catch (IOException | RuntimeException exception) {
            return false;
        }
    }

    enum InstallResult {
        INSTALLED,
        COLLISION,
        FAILED
    }

    @FunctionalInterface
    interface TextureFactory {
        Texture create(Key key, BufferedImage image) throws IOException;
    }
}
