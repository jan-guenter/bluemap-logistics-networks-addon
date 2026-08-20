/*
 * SPDX-License-Identifier: MIT
 */
package io.github.janguenter.bluemap.logisticsnetworks.adapter.bluemap522;

import io.github.janguenter.bluemap.logisticsnetworks.profile.LogisticsNetworks1101Profile;
import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.nio.file.Path;
import java.util.zip.ZipFile;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

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
}
