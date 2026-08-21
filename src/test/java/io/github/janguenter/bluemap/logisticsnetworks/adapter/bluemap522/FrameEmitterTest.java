/*
 * SPDX-License-Identifier: MIT
 */
package io.github.janguenter.bluemap.logisticsnetworks.adapter.bluemap522;

import de.bluecolored.bluemap.core.map.TextureGallery;
import de.bluecolored.bluemap.core.map.hires.ArrayTileModel;
import de.bluecolored.bluemap.core.map.hires.RenderSettings;
import de.bluecolored.bluemap.core.map.hires.TileModelView;
import de.bluecolored.bluemap.core.map.mask.Mask;
import de.bluecolored.bluemap.core.resources.pack.PackVersion;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.ResourcePack;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.texture.Texture;
import de.bluecolored.bluemap.core.util.Direction;
import de.bluecolored.bluemap.core.util.Key;
import de.bluecolored.bluemap.core.world.BlockEntity;
import de.bluecolored.bluemap.core.world.BlockState;
import de.bluecolored.bluemap.core.world.DimensionType;
import de.bluecolored.bluemap.core.world.LightData;
import de.bluecolored.bluemap.core.world.biome.Biome;
import de.bluecolored.bluemap.core.world.block.BlockAccess;
import de.bluecolored.bluemap.core.world.block.BlockNeighborhood;
import io.github.janguenter.bluemap.logisticsnetworks.model.ConnectionMask;
import io.github.janguenter.bluemap.logisticsnetworks.model.FramePlan;
import io.github.janguenter.bluemap.logisticsnetworks.model.NodeDirection;
import io.github.janguenter.bluemap.logisticsnetworks.profile.LogisticsNetworks1101Profile;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FrameEmitterTest {

    private static final Key FRAME_KEY = LogisticsNetworks1101Profile.NODE_TEXTURE;
    private static final Key SHEET_KEY = SyntheticSheetTexture.KEY;

    @Test
    void usesFrameWidePlanarTextureCoordinates() {
        assertEquals(0F, FrameEmitter.projectedU(
                Direction.NORTH, -FramePlan.OUTSET, 0F, 0F
        ));
        assertEquals(FramePlan.THICKNESS, FrameEmitter.projectedU(
                Direction.NORTH, FramePlan.THICKNESS, 0F, 0F
        ));
        assertEquals(1F - FramePlan.THICKNESS, FrameEmitter.projectedU(
                Direction.NORTH, 1F - FramePlan.THICKNESS, 0F, 0F
        ));
        assertEquals(1F, FrameEmitter.projectedU(
                Direction.NORTH, 1F + FramePlan.OUTSET, 0F, 0F
        ));
    }

    @Test
    void normalizesTheFullCageVerticalSpan() {
        assertEquals(1F, FrameEmitter.projectedV(
                Direction.SOUTH, 0F, FramePlan.Y_MIN - FramePlan.OUTSET, 0F
        ));
        assertEquals(1F - FramePlan.THICKNESS / FramePlan.HEIGHT,
                FrameEmitter.projectedV(
                        Direction.SOUTH, 0F, FramePlan.Y_MIN + FramePlan.THICKNESS, 0F
                ));
        assertEquals(FramePlan.THICKNESS / FramePlan.HEIGHT,
                FrameEmitter.projectedV(
                        Direction.SOUTH, 0F,
                        FramePlan.Y_MIN + FramePlan.HEIGHT - FramePlan.THICKNESS, 0F
                ));
        assertEquals(0F, FrameEmitter.projectedV(
                Direction.SOUTH, 0F,
                FramePlan.Y_MIN + FramePlan.HEIGHT + FramePlan.OUTSET, 0F
        ));
    }

    @Test
    void projectsTopAndSideFacesOnTheirOwnPlanes() {
        assertEquals(0.75F, FrameEmitter.projectedV(
                Direction.UP, 0F, 0F, 0.75F
        ));
        assertEquals(0.25F, FrameEmitter.projectedU(
                Direction.WEST, 0F, 0F, 0.25F
        ));
    }

    @Test
    void omitsOnlyCapsWhoseNeighborEnvelopeContactsTheFrame() {
        FramePlan.Part upperCorner = FramePlan.parts().stream()
                .filter(part -> part.touches(NodeDirection.UP))
                .findFirst()
                .orElseThrow();
        FramePlan.Part northCorner = FramePlan.parts().stream()
                .filter(part -> part.touches(NodeDirection.NORTH))
                .findFirst()
                .orElseThrow();

        assertFalse(FrameEmitter.omitted(
                NodeDirection.UP,
                upperCorner,
                ConnectionMask.empty().with(NodeDirection.UP)
        ));
        assertTrue(FrameEmitter.omitted(
                NodeDirection.NORTH,
                northCorner,
                ConnectionMask.empty().with(NodeDirection.NORTH)
        ));
    }

    @Test
    void everyHorizontalMaskEmitsOneNonDuplicatedUpwardSheetQuad() {
        List<NodeDirection> directions = List.of(
                NodeDirection.NORTH,
                NodeDirection.EAST,
                NodeDirection.SOUTH,
                NodeDirection.WEST
        );
        for (int bits = 0; bits < 16; bits++) {
            ConnectionMask mask = ConnectionMask.empty();
            for (int index = 0; index < directions.size(); index++) {
                if ((bits & 1 << index) != 0) {
                    mask = mask.with(directions.get(index));
                }
            }
            FramePlan.TopSheet sheet = FramePlan.topSheet(mask);
            FrameEmitter.SheetQuad quad = FrameEmitter.sheetQuad(sheet);
            List<FrameEmitter.Triangle> triangles = quad.triangles();
            assertEquals(2, triangles.size());

            Set<FrameEmitter.Vertex> first = vertices(triangles.get(0));
            Set<FrameEmitter.Vertex> second = vertices(triangles.get(1));
            assertNotEquals(first, second);
            Set<FrameEmitter.Vertex> shared = new HashSet<>(first);
            shared.retainAll(second);
            assertEquals(2, shared.size());
            Set<FrameEmitter.Vertex> all = new HashSet<>(first);
            all.addAll(second);
            assertEquals(4, all.size());
            assertEquals(sheet.area() / 2F, area(triangles.get(0)));
            assertEquals(sheet.area() / 2F, area(triangles.get(1)));
        }
    }

    @Test
    void everySideSheetIsTwoNonDuplicatedOutwardWoundTriangles() {
        for (int bits = 0; bits < 16; bits++) {
            ConnectionMask mask = horizontalMask(bits);
            for (FramePlan.SideSheet sheet : FramePlan.sideSheets(mask)) {
                FrameEmitter.SheetQuad quad = FrameEmitter.sideSheetQuad(sheet);
                assertEquals(2, quad.triangles().size());
                Set<FrameEmitter.Vertex> first = vertices(quad.triangles().get(0));
                Set<FrameEmitter.Vertex> second = vertices(quad.triangles().get(1));
                assertNotEquals(first, second);
                Set<FrameEmitter.Vertex> all = new HashSet<>(first);
                all.addAll(second);
                assertEquals(4, all.size());
                assertEquals(sheet.area() / 2F, area3d(quad.triangles().get(0)));
                assertEquals(sheet.area() / 2F, area3d(quad.triangles().get(1)));
                assertTrue(outwardDot(quad.triangles().get(0), sheet.direction()) > 0F);
                assertTrue(outwardDot(quad.triangles().get(1), sheet.direction()) > 0F);
            }
        }
    }

    @Test
    void everyMaskUsesTheGeneratedMaterialForTopAndExposedSideSheets() {
        TextureGallery gallery = new TextureGallery();
        put(gallery, ResourcePack.MISSING_TEXTURE);
        put(gallery, FRAME_KEY);
        put(gallery, SHEET_KEY);
        int frameMaterial = gallery.get(FRAME_KEY);
        int sheetMaterial = gallery.get(SHEET_KEY);
        FrameEmitter emitter = new FrameEmitter(gallery, FRAME_KEY, SHEET_KEY);

        for (int bits = 0; bits < 16; bits++) {
            ConnectionMask mask = horizontalMask(bits);
            RecordingTileModel model = emit(emitter, mask);
            int frameTriangles = FramePlan.frameQuadCount(mask) * 2;
            int expectedSheetTriangles = (1 + FramePlan.sideSheets(mask).size()) * 2;
            assertEquals(frameTriangles + expectedSheetTriangles, model.size());
            assertTrue(model.materials().subList(0, frameTriangles).stream()
                    .allMatch(material -> material == frameMaterial));
            assertTrue(model.materials().subList(frameTriangles, model.size()).stream()
                    .allMatch(material -> material == sheetMaterial));
        }
    }

    @Test
    void resolvesCurrentMaterialIdsAfterGalleryResetAndReordering() {
        TextureGallery gallery = new TextureGallery();
        put(gallery, ResourcePack.MISSING_TEXTURE);
        put(gallery, FRAME_KEY);
        put(gallery, SHEET_KEY);
        int firstFrame = gallery.get(FRAME_KEY);
        int firstSheet = gallery.get(SHEET_KEY);
        FrameEmitter emitter = new FrameEmitter(gallery, FRAME_KEY, SHEET_KEY);

        RecordingTileModel first = emit(emitter);
        assertMaterials(first, firstFrame, firstSheet);

        gallery.clear();
        put(gallery, ResourcePack.MISSING_TEXTURE);
        put(gallery, Key.parse("test:reset_extra_a"));
        put(gallery, Key.parse("test:reset_extra_b"));
        put(gallery, SHEET_KEY);
        put(gallery, FRAME_KEY);
        int currentFrame = gallery.get(FRAME_KEY);
        int currentSheet = gallery.get(SHEET_KEY);
        assertNotEquals(firstFrame, currentFrame);
        assertNotEquals(firstSheet, currentSheet);

        RecordingTileModel afterReset = emit(emitter);
        assertMaterials(afterReset, currentFrame, currentSheet);
    }

    @Test
    void missingMaterialsAfterGalleryResetFailBeforeModelMutation() {
        TextureGallery gallery = new TextureGallery();
        put(gallery, ResourcePack.MISSING_TEXTURE);
        put(gallery, FRAME_KEY);
        put(gallery, SHEET_KEY);
        FrameEmitter emitter = new FrameEmitter(gallery, FRAME_KEY, SHEET_KEY);
        gallery.clear();
        put(gallery, ResourcePack.MISSING_TEXTURE);

        assertAtomicMaterialFailure(emitter);
    }

    @Test
    void equalMaterialIdsAfterGalleryResetFailBeforeModelMutation() {
        ResettableEqualGallery gallery = new ResettableEqualGallery();
        FrameEmitter emitter = new FrameEmitter(gallery, FRAME_KEY, SHEET_KEY);
        gallery.resetToEqualIds();

        assertAtomicMaterialFailure(emitter);
    }

    private static Set<FrameEmitter.Vertex> vertices(FrameEmitter.Triangle triangle) {
        return Set.of(triangle.a(), triangle.b(), triangle.c());
    }

    private static float area(FrameEmitter.Triangle triangle) {
        FrameEmitter.Vertex a = triangle.a();
        FrameEmitter.Vertex b = triangle.b();
        FrameEmitter.Vertex c = triangle.c();
        return Math.abs(
                a.x() * (b.z() - c.z())
                        + b.x() * (c.z() - a.z())
                        + c.x() * (a.z() - b.z())
        ) / 2F;
    }

    private static float area3d(FrameEmitter.Triangle triangle) {
        float[] normal = normal(triangle);
        return (float) Math.sqrt(normal[0] * normal[0]
                + normal[1] * normal[1] + normal[2] * normal[2]) / 2F;
    }

    private static float outwardDot(
            FrameEmitter.Triangle triangle,
            NodeDirection direction
    ) {
        float[] normal = normal(triangle);
        return normal[0] * direction.stepX()
                + normal[1] * direction.stepY()
                + normal[2] * direction.stepZ();
    }

    private static float[] normal(FrameEmitter.Triangle triangle) {
        FrameEmitter.Vertex a = triangle.a();
        FrameEmitter.Vertex b = triangle.b();
        FrameEmitter.Vertex c = triangle.c();
        float abX = b.x() - a.x();
        float abY = b.y() - a.y();
        float abZ = b.z() - a.z();
        float acX = c.x() - a.x();
        float acY = c.y() - a.y();
        float acZ = c.z() - a.z();
        return new float[]{
            abY * acZ - abZ * acY,
            abZ * acX - abX * acZ,
            abX * acY - abY * acX
        };
    }

    private static ConnectionMask horizontalMask(int bits) {
        List<NodeDirection> directions = List.of(
                NodeDirection.NORTH,
                NodeDirection.EAST,
                NodeDirection.SOUTH,
                NodeDirection.WEST
        );
        ConnectionMask result = ConnectionMask.empty();
        for (int index = 0; index < directions.size(); index++) {
            if ((bits & 1 << index) != 0) {
                result = result.with(directions.get(index));
            }
        }
        return result;
    }

    private static RecordingTileModel emit(FrameEmitter emitter) {
        return emit(emitter, ConnectionMask.empty());
    }

    private static RecordingTileModel emit(
            FrameEmitter emitter,
            ConnectionMask mask
    ) {
        RecordingTileModel model = new RecordingTileModel(512);
        emitter.emit(
                neighborhood(),
                new TileModelView(model),
                mask
        );
        return model;
    }

    private static void assertMaterials(
            RecordingTileModel model,
            int frameMaterial,
            int sheetMaterial
    ) {
        int frameTriangles = FramePlan.frameQuadCount(ConnectionMask.empty()) * 2;
        int expectedTriangles = FramePlan.quadCount(ConnectionMask.empty()) * 2;
        assertEquals(expectedTriangles, model.size());
        assertEquals(expectedTriangles, model.materials().size());
        assertTrue(model.materials().subList(0, frameTriangles).stream()
                .allMatch(material -> material == frameMaterial));
        assertTrue(model.materials().subList(frameTriangles, expectedTriangles).stream()
                .allMatch(material -> material == sheetMaterial));
    }

    private static void assertAtomicMaterialFailure(FrameEmitter emitter) {
        RecordingTileModel model = new RecordingTileModel(16);
        model.add(7);
        TileModelView view = new TileModelView(model);

        assertThrows(IllegalStateException.class, () ->
                emitter.emit(neighborhood(), view, ConnectionMask.empty()));

        assertEquals(7, model.size());
        assertEquals(7, view.getStart());
        assertEquals(0, view.getSize());
        assertTrue(model.materials().isEmpty());
    }

    private static BlockNeighborhood neighborhood() {
        BlockNeighborhood block = new BlockNeighborhood(
                new ConstantBlockAccess(),
                new ResourcePack(new PackVersion(34, 0)),
                new TestRenderSettings(),
                DimensionType.OVERWORLD
        );
        block.set(0, 64, 0);
        return block;
    }

    private static void put(TextureGallery gallery, Key key) {
        gallery.put(key, Texture.missing(key));
    }

    private static final class RecordingTileModel extends ArrayTileModel {

        private final List<Integer> materials = new ArrayList<>();

        private RecordingTileModel(int initialCapacity) {
            super(initialCapacity);
        }

        @Override
        public RecordingTileModel setMaterialIndex(int face, int material) {
            super.setMaterialIndex(face, material);
            materials.add(material);
            return this;
        }

        private List<Integer> materials() {
            return List.copyOf(materials);
        }
    }

    private static final class ResettableEqualGallery extends TextureGallery {

        private boolean equalIds;

        @Override
        public int get(Key key) {
            if (FRAME_KEY.equals(key)) {
                return 1;
            }
            if (SHEET_KEY.equals(key)) {
                return equalIds ? 1 : 2;
            }
            return 0;
        }

        private void resetToEqualIds() {
            equalIds = true;
        }
    }

    private static final class ConstantBlockAccess implements BlockAccess {

        private int x;
        private int y;
        private int z;

        @Override
        public void set(int newX, int newY, int newZ) {
            x = newX;
            y = newY;
            z = newZ;
        }

        @Override
        public BlockAccess copy() {
            ConstantBlockAccess copy = new ConstantBlockAccess();
            copy.set(x, y, z);
            return copy;
        }

        @Override
        public int getX() {
            return x;
        }

        @Override
        public int getY() {
            return y;
        }

        @Override
        public int getZ() {
            return z;
        }

        @Override
        public BlockState getBlockState() {
            return BlockState.AIR;
        }

        @Override
        public LightData getLightData() {
            return new LightData(15, 0);
        }

        @Override
        public Biome getBiome() {
            return Biome.DEFAULT;
        }

        @Override
        public BlockEntity getBlockEntity() {
            return null;
        }

        @Override
        public boolean hasOceanFloorY() {
            return false;
        }

        @Override
        public int getOceanFloorY() {
            return 0;
        }
    }

    private static final class TestRenderSettings implements RenderSettings {

        @Override
        public int getRemoveCavesBelowY() {
            return Integer.MIN_VALUE;
        }

        @Override
        public int getCaveDetectionOceanFloor() {
            return 0;
        }

        @Override
        public boolean isCaveDetectionUsesBlockLight() {
            return false;
        }

        @Override
        public float getAmbientLight() {
            return 0F;
        }

        @Override
        public Mask getRenderMask() {
            return Mask.ALL;
        }

        @Override
        public boolean isSaveHiresLayer() {
            return true;
        }

        @Override
        public boolean isRenderTopOnly() {
            return false;
        }
    }
}
