/*
 * SPDX-License-Identifier: MIT
 */
package io.github.janguenter.bluemap.logisticsnetworks.adapter.bluemap522;

import com.flowpowered.math.vector.Vector3f;
import de.bluecolored.bluemap.core.map.hires.block.BlockRendererType;
import de.bluecolored.bluemap.core.resources.ResourcePath;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.ResourcePack;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.blockstate.Variant;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.blockstate.VariantSet;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.blockstate.Variants;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.model.Element;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.model.Face;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.model.Model;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.model.Rotation;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.texture.Texture;
import de.bluecolored.bluemap.core.util.Direction;
import de.bluecolored.bluemap.core.world.BlockState;
import de.bluecolored.bluemap.core.world.block.BlockNeighborhood;

/** Conservative resource-only proof for the prototype's admitted host shape. */
final class FullCubeHostPreflight {

    static final int MAX_VARIANTS = 64;
    private static final Vector3f FULL_MIN = Vector3f.ZERO;
    private static final Vector3f FULL_MAX = new Vector3f(16F, 16F, 16F);

    private final ResourcePack resourcePack;

    FullCubeHostPreflight(ResourcePack resourcePack) {
        this.resourcePack = resourcePack;
    }

    boolean supports(BlockNeighborhood block) {
        BlockState worldState = block.getBlockState();
        if (worldState == null || worldState.isAir() || worldState.isWater()) {
            return false;
        }
        de.bluecolored.bluemap.core.resources.pack.resourcepack.blockstate.BlockState state =
                resourcePack.getBlockStates().get(worldState.getId());
        if (state == null || state.getMultipart() != null || state.getVariants() == null) {
            return false;
        }

        VariantSet selected = selectUniqueVariantSet(state.getVariants(), worldState);
        if (!supportsVariantSet(selected)) {
            return false;
        }
        return true;
    }

    static VariantSet selectUniqueVariantSet(Variants variants, BlockState worldState) {
        if (variants == null || variants.getVariants() == null || worldState == null) {
            return null;
        }
        VariantSet selected = null;
        for (VariantSet candidate : variants.getVariants()) {
            if (candidate == null || candidate.getCondition() == null) {
                return null;
            }
            if (!candidate.getCondition().matches(worldState)) {
                continue;
            }
            if (selected != null) {
                return null;
            }
            selected = candidate;
        }
        if (selected == null) {
            selected = variants.getDefaultVariant();
        }
        return selected;
    }

    boolean supportsVariantSet(VariantSet selected) {
        if (selected == null || selected.getVariants() == null
                || selected.getVariants().length == 0
                || selected.getVariants().length > MAX_VARIANTS) {
            return false;
        }
        double totalWeight = 0D;
        for (Variant variant : selected.getVariants()) {
            if (!supportsVariant(variant)) {
                return false;
            }
            totalWeight += variant.getWeight();
            if (!Double.isFinite(totalWeight)) {
                return false;
            }
            Model model = variant.getModel().getResource(resourcePack.getModels()::get);
            if (!supportsModel(model)) {
                return false;
            }
        }
        return true;
    }

    static boolean supportsVariant(Variant variant) {
        return variant != null
                && variant.getRenderer() == BlockRendererType.DEFAULT
                && variant.getModel() != null
                && !ResourcePack.MISSING_BLOCK_MODEL.equals(variant.getModel())
                && Double.isFinite(variant.getWeight())
                && variant.getWeight() > 0D
                && rightAngle(variant.getX())
                && rightAngle(variant.getY())
                && rightAngle(variant.getZ());
    }

    boolean supportsModel(Model model) {
        if (model == null || model.getTextures() == null
                || model.getElements() == null || model.getElements().length != 1) {
            return false;
        }
        Element element = model.getElements()[0];
        if (element == null
                || !FULL_MIN.equals(element.getFrom())
                || !FULL_MAX.equals(element.getTo())
                || !zeroRotation(element.getRotation())
                || element.getFaces() == null
                || element.getFaces().size() != Direction.values().length) {
            return false;
        }
        for (Direction direction : Direction.values()) {
            Face face = element.getFaces().get(direction);
            if (face == null || face.getTexture() == null) {
                return false;
            }
            ResourcePath<Texture> texture = face.getTexture()
                    .getTexturePath(model.getTextures()::get);
            if (texture == null
                    || ResourcePack.MISSING_TEXTURE.equals(texture)
                    || resourcePack.getTextures().get(texture) == null) {
                return false;
            }
        }
        return true;
    }

    private static boolean rightAngle(float angle) {
        return Float.isFinite(angle) && angle % 90F == 0F;
    }

    private static boolean zeroRotation(Rotation rotation) {
        return rotation != null
                && Float.compare(rotation.getX(), 0F) == 0
                && Float.compare(rotation.getY(), 0F) == 0
                && Float.compare(rotation.getZ(), 0F) == 0
                && Float.compare(rotation.getAngle(), 0F) == 0
                && !rotation.isRescale();
    }
}
