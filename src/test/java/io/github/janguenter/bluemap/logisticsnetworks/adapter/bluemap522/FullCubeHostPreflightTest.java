/*
 * SPDX-License-Identifier: MIT
 */
package io.github.janguenter.bluemap.logisticsnetworks.adapter.bluemap522;

import com.flowpowered.math.vector.Vector3f;
import de.bluecolored.bluemap.core.resources.ResourcePath;
import de.bluecolored.bluemap.core.resources.pack.PackVersion;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.ResourcePack;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.blockstate.BlockStateCondition;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.blockstate.Variant;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.blockstate.VariantSet;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.blockstate.Variants;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.model.Element;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.model.Face;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.model.Model;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.model.Rotation;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.model.TextureVariable;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.texture.Texture;
import de.bluecolored.bluemap.core.util.Direction;
import de.bluecolored.bluemap.core.util.Key;
import de.bluecolored.bluemap.core.util.math.Axis;
import org.junit.jupiter.api.Test;

import java.util.EnumMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FullCubeHostPreflightTest {

    private static final Key TEXTURE_KEY = Key.parse("test:block/host");
    private static final ResourcePath<Model> MODEL_KEY =
            new ResourcePath<>("test:block/host");

    @Test
    void acceptsExactlyOneFullCubeWithSixResolvableFaces() {
        ResourcePack pack = packWithTexture();
        FullCubeHostPreflight preflight = new FullCubeHostPreflight(pack);

        assertTrue(preflight.supportsModel(model(fullCubeFaces(), Rotation.ZERO)));
    }

    @Test
    void rejectsMissingFaceRotatedElementAndMissingTexture() {
        ResourcePack pack = packWithTexture();
        FullCubeHostPreflight preflight = new FullCubeHostPreflight(pack);

        EnumMap<Direction, Face> missingFace = fullCubeFaces();
        missingFace.remove(Direction.UP);
        assertFalse(preflight.supportsModel(model(missingFace, Rotation.ZERO)));
        assertFalse(preflight.supportsModel(model(
                fullCubeFaces(),
                new Rotation(new Vector3f(8F, 8F, 8F), Axis.Y, 45F, false)
        )));

        ResourcePack emptyPack = new ResourcePack(new PackVersion(34, 0));
        assertFalse(new FullCubeHostPreflight(emptyPack)
                .supportsModel(model(fullCubeFaces(), Rotation.ZERO)));
    }

    @Test
    void admitsRightAngleDefaultRendererVariantOnly() {
        Variant identity = new Variant(MODEL_KEY);
        Variant rightAngle = new Variant(MODEL_KEY, 0F, 90F, 0F);
        Variant diagonal = new Variant(MODEL_KEY, 0F, 45F, 0F);

        assertTrue(FullCubeHostPreflight.supportsVariant(identity));
        assertTrue(FullCubeHostPreflight.supportsVariant(rightAngle));
        assertFalse(FullCubeHostPreflight.supportsVariant(diagonal));
        assertFalse(FullCubeHostPreflight.supportsVariant(
                new Variant(ResourcePack.MISSING_BLOCK_MODEL)
        ));
    }

    @Test
    void rejectsOverlappingOrWeightedStateSelection() {
        Variant first = new Variant(MODEL_KEY);
        Variant second = new Variant(MODEL_KEY);
        de.bluecolored.bluemap.core.world.BlockState world =
                new de.bluecolored.bluemap.core.world.BlockState(
                        Key.parse("test:host"),
                        Map.of("facing", "north")
                );
        VariantSet matching = new VariantSet(
                BlockStateCondition.property("facing", "north"),
                first
        );
        VariantSet overlap = new VariantSet(
                BlockStateCondition.property("facing", "north"),
                second
        );
        assertNull(FullCubeHostPreflight.selectSingleVariant(
                new Variants(new VariantSet[]{matching, overlap}, null),
                world
        ));

        VariantSet weighted = new VariantSet(first, second);
        assertNull(FullCubeHostPreflight.selectSingleVariant(
                new Variants(new VariantSet[0], weighted),
                world
        ));
        assertSame(first, FullCubeHostPreflight.selectSingleVariant(
                new Variants(new VariantSet[]{matching}, null),
                world
        ));
    }

    private static ResourcePack packWithTexture() {
        ResourcePack pack = new ResourcePack(new PackVersion(34, 0));
        pack.getTextures().put(TEXTURE_KEY, Texture.missing(TEXTURE_KEY));
        return pack;
    }

    private static Model model(EnumMap<Direction, Face> faces, Rotation rotation) {
        Element element = new Element(
                Vector3f.ZERO,
                new Vector3f(16F, 16F, 16F),
                rotation,
                faces
        );
        return new Model(Map.of(), element);
    }

    private static EnumMap<Direction, Face> fullCubeFaces() {
        EnumMap<Direction, Face> faces = new EnumMap<>(Direction.class);
        for (Direction direction : Direction.values()) {
            faces.put(direction, new Face(new TextureVariable(
                    new ResourcePath<Texture>(TEXTURE_KEY)
            )));
        }
        return faces;
    }
}
