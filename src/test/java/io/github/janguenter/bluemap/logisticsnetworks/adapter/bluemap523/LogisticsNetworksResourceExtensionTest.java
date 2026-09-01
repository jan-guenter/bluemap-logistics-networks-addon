/*
 * SPDX-License-Identifier: MIT
 */
package io.github.janguenter.bluemap.logisticsnetworks.adapter.bluemap523;

import de.bluecolored.bluemap.core.resources.pack.PackVersion;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.ResourcePack;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.texture.Texture;
import io.github.janguenter.bluemap.logisticsnetworks.activation.LogisticsNetworksRuntime;
import io.github.janguenter.bluemap.logisticsnetworks.profile.LogisticsNetworks1101Profile;
import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.nio.file.Path;
import java.util.zip.ZipFile;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LogisticsNetworksResourceExtensionTest {

    @Test
    void exactOperatorTextureHasPinnedDecodedPixels() throws Exception {
        String property = System.getProperty("logisticsNetworksJar");
        assertNotNull(property, "exact artifact system property is required by the gate");
        try (ZipFile zip = new ZipFile(Path.of(property).toFile())) {
            BufferedImage image = ImageIO.read(zip.getInputStream(zip.getEntry(
                    "assets/logisticsnetworks/textures/entity/node.png"
            )));
            assertNotNull(image);
            assertEquals(64, image.getWidth());
            assertEquals(64, image.getHeight());
            assertEquals(
                    LogisticsNetworks1101Profile.NODE_TEXTURE_RGBA_SHA256,
                    LogisticsNetworksResourceExtension.rgbaSha256(image)
            );
        }
    }

    @Test
    void postBakeSheetTextureIsExactAndLifecycleIsIdempotent() {
        ResourcePack pack = new ResourcePack(new PackVersion(34, 0));
        LogisticsNetworksResourceExtension extension =
                new LogisticsNetworksResourceExtension(
                        pack,
                        LogisticsNetworksRuntime.INSTANCE
                );

        assertEquals(
                SyntheticSheetTexture.InstallResult.INSTALLED,
                extension.bakeSheetTexture()
        );
        Texture installed = pack.getTextures().get(SyntheticSheetTexture.KEY);
        assertTrue(SyntheticSheetTexture.isExact(installed));
        assertEquals(
                SyntheticSheetTexture.InstallResult.INSTALLED,
                extension.bakeSheetTexture()
        );
        assertSame(installed, pack.getTextures().get(SyntheticSheetTexture.KEY));
    }

    @Test
    void postBakeSheetTextureRejectsPreexistingKeyCollision() {
        ResourcePack pack = new ResourcePack(new PackVersion(34, 0));
        Texture collision = Texture.missing(SyntheticSheetTexture.KEY);
        pack.getTextures().put(SyntheticSheetTexture.KEY, collision);
        LogisticsNetworksResourceExtension extension =
                new LogisticsNetworksResourceExtension(
                        pack,
                        LogisticsNetworksRuntime.INSTANCE
                );

        assertEquals(
                SyntheticSheetTexture.InstallResult.COLLISION,
                extension.bakeSheetTexture()
        );
        assertSame(collision, pack.getTextures().get(SyntheticSheetTexture.KEY));
    }
}
