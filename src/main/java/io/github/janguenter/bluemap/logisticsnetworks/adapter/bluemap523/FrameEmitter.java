/*
 * SPDX-License-Identifier: MIT
 */
package io.github.janguenter.bluemap.logisticsnetworks.adapter.bluemap523;

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

import java.util.List;

/** Emits the project-owned frame and generated translucent sheets. */
final class FrameEmitter {

    private final TextureGallery textures;
    private final Key frameTexture;
    private final Key sheetTexture;

    FrameEmitter(TextureGallery textures, Key frameTexture, Key sheetTexture) {
        this.textures = textures;
        this.frameTexture = frameTexture;
        this.sheetTexture = sheetTexture;
        resolveMaterials();
    }

    void emit(
            BlockNeighborhood block,
            TileModelView target,
            ConnectionMask connections
    ) {
        MaterialIds materials = resolveMaterials();
        for (FramePlan.Part part : FramePlan.parts(connections)) {
            box(block, target, part, connections, materials.frame());
        }
        sheet(block, target, FramePlan.topSheet(connections), materials.sheet());
        for (FramePlan.SideSheet sideSheet : FramePlan.sideSheets(connections)) {
            sideSheet(block, target, sideSheet, materials.sheet());
        }
    }

    private void box(
            BlockNeighborhood block,
            TileModelView target,
            FramePlan.Part part,
            ConnectionMask connections,
            int material
    ) {
        float x0 = part.x0();
        float y0 = part.y0();
        float z0 = part.z0();
        float x1 = part.x1();
        float y1 = part.y1();
        float z1 = part.z1();

        if (!omitted(NodeDirection.DOWN, part, connections)) {
            quad(block, target, Direction.DOWN,
                    x0, y0, z0, x1, y0, z0, x1, y0, z1, x0, y0, z1,
                    material);
        }
        if (!omitted(NodeDirection.UP, part, connections)) {
            quad(block, target, Direction.UP,
                    x0, y1, z1, x1, y1, z1, x1, y1, z0, x0, y1, z0,
                    material);
        }
        if (!omitted(NodeDirection.NORTH, part, connections)) {
            quad(block, target, Direction.NORTH,
                    x1, y0, z0, x0, y0, z0, x0, y1, z0, x1, y1, z0,
                    material);
        }
        if (!omitted(NodeDirection.SOUTH, part, connections)) {
            quad(block, target, Direction.SOUTH,
                    x0, y0, z1, x1, y0, z1, x1, y1, z1, x0, y1, z1,
                    material);
        }
        if (!omitted(NodeDirection.WEST, part, connections)) {
            quad(block, target, Direction.WEST,
                    x0, y0, z0, x0, y0, z1, x0, y1, z1, x0, y1, z0,
                    material);
        }
        if (!omitted(NodeDirection.EAST, part, connections)) {
            quad(block, target, Direction.EAST,
                    x1, y0, z1, x1, y0, z0, x1, y1, z0, x1, y1, z1,
                    material);
        }
    }

    private void sheet(
            BlockNeighborhood block,
            TileModelView target,
            FramePlan.TopSheet sheet,
            int material
    ) {
        SheetQuad geometry = sheetQuad(sheet);
        quad(block, target, Direction.UP,
                geometry.a().x(), geometry.a().y(), geometry.a().z(),
                geometry.b().x(), geometry.b().y(), geometry.b().z(),
                geometry.c().x(), geometry.c().y(), geometry.c().z(),
                geometry.d().x(), geometry.d().y(), geometry.d().z(),
                material);
    }

    private void sideSheet(
            BlockNeighborhood block,
            TileModelView target,
            FramePlan.SideSheet sheet,
            int material
    ) {
        SheetQuad geometry = sideSheetQuad(sheet);
        Direction direction = switch (sheet.direction()) {
            case NORTH -> Direction.NORTH;
            case EAST -> Direction.EAST;
            case SOUTH -> Direction.SOUTH;
            case WEST -> Direction.WEST;
            case DOWN, UP -> throw new IllegalArgumentException("side sheet must be cardinal");
        };
        quad(block, target, direction,
                geometry.a().x(), geometry.a().y(), geometry.a().z(),
                geometry.b().x(), geometry.b().y(), geometry.b().z(),
                geometry.c().x(), geometry.c().y(), geometry.c().z(),
                geometry.d().x(), geometry.d().y(), geometry.d().z(),
                material);
    }

    private MaterialIds resolveMaterials() {
        int frame = textures.get(frameTexture);
        int sheet = textures.get(sheetTexture);
        if (frame <= 0 || sheet <= 0 || frame == sheet) {
            throw new IllegalStateException("frame or sheet texture is unavailable");
        }
        return new MaterialIds(frame, sheet);
    }

    static SheetQuad sheetQuad(FramePlan.TopSheet sheet) {
        return new SheetQuad(
                new Vertex(sheet.x0(), sheet.y(), sheet.z1()),
                new Vertex(sheet.x1(), sheet.y(), sheet.z1()),
                new Vertex(sheet.x1(), sheet.y(), sheet.z0()),
                new Vertex(sheet.x0(), sheet.y(), sheet.z0())
        );
    }

    static SheetQuad sideSheetQuad(FramePlan.SideSheet sheet) {
        return switch (sheet.direction()) {
            case NORTH -> new SheetQuad(
                    new Vertex(sheet.x1(), sheet.y0(), sheet.z0()),
                    new Vertex(sheet.x0(), sheet.y0(), sheet.z0()),
                    new Vertex(sheet.x0(), sheet.y1(), sheet.z0()),
                    new Vertex(sheet.x1(), sheet.y1(), sheet.z0())
            );
            case EAST -> new SheetQuad(
                    new Vertex(sheet.x0(), sheet.y0(), sheet.z1()),
                    new Vertex(sheet.x0(), sheet.y0(), sheet.z0()),
                    new Vertex(sheet.x0(), sheet.y1(), sheet.z0()),
                    new Vertex(sheet.x0(), sheet.y1(), sheet.z1())
            );
            case SOUTH -> new SheetQuad(
                    new Vertex(sheet.x0(), sheet.y0(), sheet.z0()),
                    new Vertex(sheet.x1(), sheet.y0(), sheet.z0()),
                    new Vertex(sheet.x1(), sheet.y1(), sheet.z0()),
                    new Vertex(sheet.x0(), sheet.y1(), sheet.z0())
            );
            case WEST -> new SheetQuad(
                    new Vertex(sheet.x0(), sheet.y0(), sheet.z0()),
                    new Vertex(sheet.x0(), sheet.y0(), sheet.z1()),
                    new Vertex(sheet.x0(), sheet.y1(), sheet.z1()),
                    new Vertex(sheet.x0(), sheet.y1(), sheet.z0())
            );
            case DOWN, UP -> throw new IllegalArgumentException("side sheet must be cardinal");
        };
    }

    static boolean omitted(
            NodeDirection direction,
            FramePlan.Part part,
            ConnectionMask connections
    ) {
        return connections.contains(direction)
                && FramePlan.seamFacesMayBeOmitted(direction)
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
            float dz,
            int material
    ) {
        int start = target.add(2);
        TileModel model = target.getTileModel();
        model.setPositions(start, ax, ay, az, bx, by, bz, cx, cy, cz);
        model.setPositions(start + 1, ax, ay, az, cx, cy, cz, dx, dy, dz);
        model.setUvs(start,
                projectedU(direction, ax, ay, az), projectedV(direction, ax, ay, az),
                projectedU(direction, bx, by, bz), projectedV(direction, bx, by, bz),
                projectedU(direction, cx, cy, cz), projectedV(direction, cx, cy, cz));
        model.setUvs(start + 1,
                projectedU(direction, ax, ay, az), projectedV(direction, ax, ay, az),
                projectedU(direction, cx, cy, cz), projectedV(direction, cx, cy, cz),
                projectedU(direction, dx, dy, dz), projectedV(direction, dx, dy, dz));
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

    static float projectedU(Direction direction, float x, float y, float z) {
        return switch (direction) {
            case DOWN, UP, NORTH, SOUTH -> unitCoordinate(x);
            case WEST, EAST -> unitCoordinate(z);
        };
    }

    static float projectedV(Direction direction, float x, float y, float z) {
        return switch (direction) {
            case DOWN, UP -> unitCoordinate(z);
            case NORTH, SOUTH, WEST, EAST ->
                    1F - unitCoordinate((y - FramePlan.Y_MIN) / FramePlan.HEIGHT);
        };
    }

    private static float unitCoordinate(float coordinate) {
        return Math.max(0F, Math.min(1F, coordinate));
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

    private record MaterialIds(int frame, int sheet) {
    }

    record Vertex(float x, float y, float z) {
    }

    record Triangle(Vertex a, Vertex b, Vertex c) {
    }

    record SheetQuad(Vertex a, Vertex b, Vertex c, Vertex d) {
        List<Triangle> triangles() {
            return List.of(new Triangle(a, b, c), new Triangle(a, c, d));
        }
    }
}
