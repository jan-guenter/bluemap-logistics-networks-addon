/*
 * SPDX-License-Identifier: MIT
 */
package io.github.janguenter.bluemap.logisticsnetworks.adapter.bluemap522;

import de.bluecolored.bluemap.core.resources.pack.PackVersion;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.ResourcePack;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.texture.Texture;
import de.bluecolored.bluemap.core.util.Key;
import org.junit.jupiter.api.Test;

import java.awt.image.BufferedImage;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SyntheticSheetTextureTest {

    @Test
    void generatesTheExactProjectOwnedKeyColorAlphaAndSize() throws IOException {
        BufferedImage image = SyntheticSheetTexture.image();
        assertEquals(1, SyntheticSheetTexture.WIDTH);
        assertEquals(1, SyntheticSheetTexture.HEIGHT);
        assertEquals(SyntheticSheetTexture.WIDTH, image.getWidth());
        assertEquals(SyntheticSheetTexture.HEIGHT, image.getHeight());
        assertEquals(SyntheticSheetTexture.ARGB, image.getRGB(0, 0));
        Object pixel = image.getRaster().getDataElements(0, 0, null);
        assertEquals(SyntheticSheetTexture.ALPHA, image.getColorModel().getAlpha(pixel));
        assertEquals(SyntheticSheetTexture.RED, image.getColorModel().getRed(pixel));
        assertEquals(SyntheticSheetTexture.GREEN, image.getColorModel().getGreen(pixel));
        assertEquals(SyntheticSheetTexture.BLUE, image.getColorModel().getBlue(pixel));

        Texture texture = Texture.from(SyntheticSheetTexture.KEY, image);
        assertEquals(Key.parse("bluemap_logistics_networks:generated/top_sheet"), texture.getKey());
        assertTrue(texture.isHalfTransparent());
        assertTrue(SyntheticSheetTexture.isExact(texture));
    }

    @Test
    void installsOnlyAfterExactGenerationAndVerification() {
        ResourcePack pack = emptyPack();
        assertEquals(
                SyntheticSheetTexture.InstallResult.INSTALLED,
                SyntheticSheetTexture.install(pack)
        );
        assertTrue(SyntheticSheetTexture.isExact(
                pack.getTextures().get(SyntheticSheetTexture.KEY)
        ));
        assertEquals(1, pack.getTextures().keySet().size());
    }

    @Test
    void rejectsAReservedKeyCollisionWithoutReplacingIt() {
        ResourcePack pack = emptyPack();
        Texture collision = Texture.missing(SyntheticSheetTexture.KEY);
        pack.getTextures().put(SyntheticSheetTexture.KEY, collision);

        assertEquals(
                SyntheticSheetTexture.InstallResult.COLLISION,
                SyntheticSheetTexture.install(pack)
        );
        assertSame(collision, pack.getTextures().get(SyntheticSheetTexture.KEY));
        assertFalse(SyntheticSheetTexture.isExact(collision));
    }

    @Test
    void generationFailureAndWrongOutputAreAtomic() {
        ResourcePack failedPack = emptyPack();
        assertEquals(
                SyntheticSheetTexture.InstallResult.FAILED,
                SyntheticSheetTexture.install(failedPack, (key, image) -> {
                    throw new IOException("synthetic failure");
                })
        );
        assertNull(failedPack.getTextures().get(SyntheticSheetTexture.KEY));

        ResourcePack wrongPack = emptyPack();
        assertEquals(
                SyntheticSheetTexture.InstallResult.FAILED,
                SyntheticSheetTexture.install(wrongPack, (key, image) ->
                        Texture.missing(Key.parse("test:wrong")))
        );
        assertNull(wrongPack.getTextures().get(SyntheticSheetTexture.KEY));
    }

    @Test
    void noGeneratedPngIsBundled() {
        assertNull(getClass().getResource(
                "/assets/bluemap_logistics_networks/textures/generated/top_sheet.png"
        ));
    }

    private static ResourcePack emptyPack() {
        return new ResourcePack(new PackVersion(34, 0));
    }
}
