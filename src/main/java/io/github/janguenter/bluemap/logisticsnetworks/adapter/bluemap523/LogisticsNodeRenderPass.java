/*
 * SPDX-License-Identifier: MIT
 */
package io.github.janguenter.bluemap.logisticsnetworks.adapter.bluemap523;

import com.flowpowered.math.vector.Vector3d;
import com.flowpowered.math.vector.Vector3i;
import de.bluecolored.bluemap.core.map.TextureGallery;
import de.bluecolored.bluemap.core.map.TileMetaConsumer;
import de.bluecolored.bluemap.core.map.hires.MaxCapacityReachedException;
import de.bluecolored.bluemap.core.map.hires.RenderPass;
import de.bluecolored.bluemap.core.map.hires.RenderSettings;
import de.bluecolored.bluemap.core.map.hires.TileModelView;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.ResourcePack;
import de.bluecolored.bluemap.core.world.Entity;
import de.bluecolored.bluemap.core.world.World;
import de.bluecolored.bluemap.core.world.block.Block;
import de.bluecolored.bluemap.core.world.block.BlockNeighborhood;
import io.github.janguenter.bluemap.logisticsnetworks.activation.LogisticsNetworksRuntime;
import io.github.janguenter.bluemap.logisticsnetworks.model.BlockPosition;
import io.github.janguenter.bluemap.logisticsnetworks.model.NodeIndex;
import io.github.janguenter.bluemap.logisticsnetworks.model.NodeSnapshot;
import io.github.janguenter.bluemap.logisticsnetworks.profile.LogisticsNetworks1101Profile;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** Bounded tile pass for static LogisticsNetworks node frames. */
final class LogisticsNodeRenderPass implements RenderPass {

    static final int MAX_NODE_ENTITIES = 1_024;
    static final int QUERY_MARGIN = 1;
    static final double POSITION_EPSILON = 0.000_001D;

    private final ResourcePack resourcePack;
    private final RenderSettings renderSettings;
    private final LogisticsNetworksRuntime runtime;
    private final FrameEmitter emitter;
    private final FullCubeHostPreflight hostPreflight;
    private final NodeSnapshotDecoder decoder = new NodeSnapshotDecoder();

    LogisticsNodeRenderPass(
            ResourcePack resourcePack,
            TextureGallery textureGallery,
            RenderSettings renderSettings,
            LogisticsNetworksRuntime runtime,
            OriginalRendererCatalog originalRenderers
    ) {
        this.resourcePack = resourcePack;
        this.renderSettings = renderSettings;
        this.runtime = runtime;
        this.emitter = createEmitter(resourcePack, textureGallery, runtime);
        this.hostPreflight = new FullCubeHostPreflight(
                resourcePack,
                originalRenderers
        );
    }

    @Override
    public void render(
            World world,
            Vector3i modelMin,
            Vector3i modelMax,
            Vector3i modelAnchor,
            TileModelView tileModel,
            TileMetaConsumer tileMetaConsumer
    ) {
        if (!runtime.isActive() || emitter == null) {
            return;
        }
        int passStart = tileModel.getTileModel().size();
        try {
            List<LogisticsNodeEntityData> entities = collect(world, modelMin, modelMax);
            if (entities == null) {
                return;
            }

            NodeIndex index = new NodeIndex();
            BlockNeighborhood block = new ExactBlockNeighborhood(
                    new Block(world, 0, 0, 0),
                    resourcePack,
                    renderSettings,
                    world.getDimensionType()
            );
            for (LogisticsNodeEntityData entity : entities) {
                Optional<NodeSnapshot> decoded = decoder.decode(entity);
                if (decoded.isEmpty()) {
                    continue;
                }
                NodeSnapshot snapshot = decoded.get();
                if (!isExactEntity(entity) || !nearAttachedHost(entity.getPos(), snapshot.attachedPos())) {
                    continue;
                }
                BlockPosition attached = snapshot.attachedPos();
                block.set(attached.x(), attached.y(), attached.z());
                if (!hostPreflight.supports(block)
                        || !FullCubeHostPreflight.hasClearHeadroom(block)) {
                    continue;
                }
                index.add(snapshot);
            }

            for (NodeSnapshot snapshot : index.visibleNodes()) {
                BlockPosition attached = snapshot.attachedPos();
                if (!insideTile(attached, modelMin, modelMax)) {
                    continue;
                }
                block.set(attached.x(), attached.y(), attached.z());
                if (!visibleInCaveSettings(block, renderSettings)) {
                    continue;
                }
                tileModel.initialize();
                emitter.emit(block, tileModel, index.connectionsFor(snapshot));
                tileModel.translate(
                        attached.x() - modelAnchor.getX(),
                        attached.y() - modelAnchor.getY(),
                        attached.z() - modelAnchor.getZ()
                );
            }
        } catch (MaxCapacityReachedException exception) {
            resetPass(tileModel, passStart);
        } catch (RuntimeException exception) {
            resetPass(tileModel, passStart);
            runtime.fail("render-pass-failed");
        }
    }

    static List<LogisticsNodeEntityData> collect(
            World world,
            Vector3i modelMin,
            Vector3i modelMax
    ) {
        List<LogisticsNodeEntityData> result = new ArrayList<>();
        int[] count = {0};
        world.iterateEntities(
                subtract(modelMin.getX(), QUERY_MARGIN),
                subtract(modelMin.getZ(), QUERY_MARGIN),
                add(modelMax.getX(), QUERY_MARGIN),
                add(modelMax.getZ(), QUERY_MARGIN),
                entity -> collectOne(entity, result, count)
        );
        return count[0] > MAX_NODE_ENTITIES ? null : List.copyOf(result);
    }

    static void collectOne(
            Entity entity,
            List<LogisticsNodeEntityData> result,
            int[] count
    ) {
        if (!(entity instanceof LogisticsNodeEntityData node)) {
            return;
        }
        if (count[0] <= MAX_NODE_ENTITIES) {
            count[0]++;
        }
        if (count[0] <= MAX_NODE_ENTITIES) {
            result.add(node);
        }
    }

    static boolean nearAttachedHost(Vector3d entityPos, BlockPosition attached) {
        if (entityPos == null
                || !Double.isFinite(entityPos.getX())
                || !Double.isFinite(entityPos.getY())
                || !Double.isFinite(entityPos.getZ())) {
            return false;
        }
        boolean centeredY = close(entityPos.getY(), attached.y() + 0.5D);
        boolean placementY = close(entityPos.getY(), attached.y());
        return close(entityPos.getX(), attached.x() + 0.5D)
                && (centeredY || placementY)
                && close(entityPos.getZ(), attached.z() + 0.5D);
    }

    private static boolean close(double left, double right) {
        return Math.abs(left - right) <= POSITION_EPSILON;
    }

    static boolean visibleInCaveSettings(
            BlockNeighborhood block,
            RenderSettings renderSettings
    ) {
        if (!block.isRemoveIfCave()) {
            return true;
        }
        de.bluecolored.bluemap.core.world.LightData light = block.getLightData();
        int caveLight = renderSettings.isCaveDetectionUsesBlockLight()
                ? Math.max(light.getBlockLight(), light.getSkyLight())
                : light.getSkyLight();
        return caveLight != 0;
    }

    private static boolean isExactEntity(LogisticsNodeEntityData entity) {
        return LogisticsNetworks1101Profile.ENTITY_KEY.equals(entity.getId());
    }

    private static boolean insideTile(
            BlockPosition position,
            Vector3i modelMin,
            Vector3i modelMax
    ) {
        return position.x() >= modelMin.getX() && position.x() <= modelMax.getX()
                && position.z() >= modelMin.getZ() && position.z() <= modelMax.getZ();
    }

    private static int add(int value, int delta) {
        return value > Integer.MAX_VALUE - delta ? Integer.MAX_VALUE : value + delta;
    }

    private static int subtract(int value, int delta) {
        return value < Integer.MIN_VALUE + delta ? Integer.MIN_VALUE : value - delta;
    }

    static void resetPass(TileModelView tileModel, int passStart) {
        tileModel.getTileModel().reset(passStart);
        tileModel.initialize(passStart);
    }

    private static FrameEmitter createEmitter(
            ResourcePack resourcePack,
            TextureGallery textureGallery,
            LogisticsNetworksRuntime runtime
    ) {
        if (!runtime.isActive()) {
            return null;
        }
        if (!SyntheticSheetTexture.isExact(
                resourcePack.getTextures().get(SyntheticSheetTexture.KEY)
        )) {
            runtime.fail("synthetic-sheet-texture-lifecycle-mismatch");
            return null;
        }
        try {
            return new FrameEmitter(
                    textureGallery,
                    LogisticsNetworks1101Profile.NODE_TEXTURE,
                    SyntheticSheetTexture.KEY
            );
        } catch (RuntimeException exception) {
            runtime.fail("render-texture-gallery-mismatch");
            return null;
        }
    }
}
