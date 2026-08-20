/*
 * SPDX-License-Identifier: MIT
 */
package io.github.janguenter.bluemap.logisticsnetworks.adapter.bluemap522;

import de.bluecolored.bluemap.core.map.TextureGallery;
import de.bluecolored.bluemap.core.map.hires.TileModel;
import de.bluecolored.bluemap.core.map.hires.TileModelView;
import de.bluecolored.bluemap.core.util.Direction;
import de.bluecolored.bluemap.core.util.Key;
import de.bluecolored.bluemap.core.world.LightData;
import de.bluecolored.bluemap.core.world.block.BlockNeighborhood;
import io.github.janguenter.bluemap.logisticsnetworks.model.ConnectionMask;
import io.github.janguenter.bluemap.logisticsnetworks.model.FramePlan;
import io.github.janguenter.bluemap.logisticsnetworks.model.NodeDirection;

/** Emits the project-owned frame using an operator-provided texture key. */
final class FrameEmitter {

    private final TextureGallery textures;
    private final Key texture;

    FrameEmitter(TextureGallery textures, Key texture) {
        this.textures = textures;
        this.texture = texture;
    }

    void emit(
            BlockNeighborhood block,
            TileModelView target,
            ConnectionMask connections
    ) {
        for (FramePlan.Part part : FramePlan.parts()) {
            box(block, target, part, connections);
        }
    }

    private void box(
            BlockNeighborhood block,
            TileModelView target,
            FramePlan.Part part,
            ConnectionMask connections
    ) {
        float x0 = part.x0();
        float y0 = part.y0();
        float z0 = part.z0();
        float x1 = part.x1();
        float y1 = part.y1();
        float z1 = part.z1();

        if (!omitted(NodeDirection.DOWN, part, connections)) {
            quad(block, target, Direction.DOWN,
                    x0, y0, z0, x1, y0, z0, x1, y0, z1, x0, y0, z1);
        }
        if (!omitted(NodeDirection.UP, part, connections)) {
            quad(block, target, Direction.UP,
                    x0, y1, z1, x1, y1, z1, x1, y1, z0, x0, y1, z0);
        }
        if (!omitted(NodeDirection.NORTH, part, connections)) {
            quad(block, target, Direction.NORTH,
                    x1, y0, z0, x0, y0, z0, x0, y1, z0, x1, y1, z0);
        }
        if (!omitted(NodeDirection.SOUTH, part, connections)) {
            quad(block, target, Direction.SOUTH,
                    x0, y0, z1, x1, y0, z1, x1, y1, z1, x0, y1, z1);
        }
        if (!omitted(NodeDirection.WEST, part, connections)) {
            quad(block, target, Direction.WEST,
                    x0, y0, z0, x0, y0, z1, x0, y1, z1, x0, y1, z0);
        }
        if (!omitted(NodeDirection.EAST, part, connections)) {
            quad(block, target, Direction.EAST,
                    x1, y0, z1, x1, y0, z0, x1, y1, z0, x1, y1, z1);
        }
    }

    private static boolean omitted(
            NodeDirection direction,
            FramePlan.Part part,
            ConnectionMask connections
    ) {
        return connections.contains(direction)
                && part.touches(direction);
    }

    @SuppressWarnings("checkstyle:ParameterNumber")
    private void quad(
            BlockNeighborhood block,
            TileModelView target,
            Direction direction,
            float ax,
            float ay,
            float az,
            float bx,
            float by,
            float bz,
            float cx,
            float cy,
            float cz,
            float dx,
            float dy,
            float dz
    ) {
        int start = target.add(2);
        TileModel model = target.getTileModel();
        model.setPositions(start, ax, ay, az, bx, by, bz, cx, cy, cz);
        model.setPositions(start + 1, ax, ay, az, cx, cy, cz, dx, dy, dz);
        model.setUvs(start, 0F, 1F, 1F, 1F, 1F, 0F);
        model.setUvs(start + 1, 0F, 1F, 1F, 0F, 0F, 0F);
        int material = textures.get(texture);
        model.setMaterialIndex(start, material);
        model.setMaterialIndex(start + 1, material);
        model.setColor(start, 1F, 1F, 1F);
        model.setColor(start + 1, 1F, 1F, 1F);
        model.setAOs(start, 1F, 1F, 1F);
        model.setAOs(start + 1, 1F, 1F, 1F);

        LightSample light = sampleLight(block, direction);
        model.setSunlight(start, light.sunlight());
        model.setSunlight(start + 1, light.sunlight());
        model.setBlocklight(start, light.blocklight());
        model.setBlocklight(start + 1, light.blocklight());
    }

    private static LightSample sampleLight(BlockNeighborhood block, Direction direction) {
        LightData own = block.getLightData();
        LightData faced = block.getNeighborBlock(
                direction.toVector().getX(),
                direction.toVector().getY(),
                direction.toVector().getZ()
        ).getLightData();
        return new LightSample(
                Math.max(own.getSkyLight(), faced.getSkyLight()),
                Math.max(own.getBlockLight(), faced.getBlockLight())
        );
    }

    private record LightSample(int sunlight, int blocklight) {
    }
}
