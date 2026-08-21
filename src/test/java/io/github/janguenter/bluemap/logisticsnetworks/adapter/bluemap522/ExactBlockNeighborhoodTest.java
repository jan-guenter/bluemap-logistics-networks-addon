/*
 * SPDX-License-Identifier: MIT
 */
package io.github.janguenter.bluemap.logisticsnetworks.adapter.bluemap522;

import de.bluecolored.bluemap.core.map.hires.RenderSettings;
import de.bluecolored.bluemap.core.map.mask.Mask;
import de.bluecolored.bluemap.core.resources.pack.PackVersion;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.ResourcePack;
import de.bluecolored.bluemap.core.util.Key;
import de.bluecolored.bluemap.core.world.BlockEntity;
import de.bluecolored.bluemap.core.world.BlockState;
import de.bluecolored.bluemap.core.world.DimensionType;
import de.bluecolored.bluemap.core.world.LightData;
import de.bluecolored.bluemap.core.world.biome.Biome;
import de.bluecolored.bluemap.core.world.block.BlockAccess;
import de.bluecolored.bluemap.core.world.block.BlockNeighborhood;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ExactBlockNeighborhoodTest {

    @Test
    void refreshesSparsePositionsThatShareBlueMapsModuloIndex() {
        BlockNeighborhood block = new ExactBlockNeighborhood(
                new PositionBlockAccess(0, 0, 0),
                new ResourcePack(new PackVersion(34, 0)),
                new TestRenderSettings(),
                DimensionType.OVERWORLD
        );

        assertPosition(block, 0, 64, 0);
        assertPosition(block, 8, 64, 0);
        assertPosition(block, -8, -64, 8);

        BlockAccess east = block.getNeighborBlock(1, 0, 0);
        assertEquals(-7, east.getX());
        assertEquals(-64, east.getY());
        assertEquals(8, east.getZ());
        assertEquals(Key.parse("test:position_-7_-64_8"), east.getBlockState().getId());
    }

    @Test
    void mirrorsBlueMapsCaveLightFilter() {
        TestRenderSettings skylightSettings = new TestRenderSettings(10, false);
        BlockNeighborhood dark = neighborhood(0, 0, skylightSettings);
        dark.set(0, 0, 0);
        assertFalse(LogisticsNodeRenderPass.visibleInCaveSettings(
                dark,
                skylightSettings
        ));

        BlockNeighborhood skylit = neighborhood(1, 0, skylightSettings);
        skylit.set(0, 0, 0);
        assertTrue(LogisticsNodeRenderPass.visibleInCaveSettings(
                skylit,
                skylightSettings
        ));

        TestRenderSettings blocklightSettings = new TestRenderSettings(10, true);
        BlockNeighborhood blocklit = neighborhood(0, 1, blocklightSettings);
        blocklit.set(0, 0, 0);
        assertTrue(LogisticsNodeRenderPass.visibleInCaveSettings(
                blocklit,
                blocklightSettings
        ));
    }

    @Test
    void requiresExactAirInTheBlockAboveTheHost() {
        BlockNeighborhood clear = headroomNeighborhood(Set.of(64));
        clear.set(0, 64, 0);
        assertTrue(FullCubeHostPreflight.hasClearHeadroom(clear));

        BlockNeighborhood blocked = headroomNeighborhood(Set.of(64, 65));
        blocked.set(0, 64, 0);
        assertFalse(FullCubeHostPreflight.hasClearHeadroom(blocked));
    }

    @Test
    void verticalFullCubePairSuppressesTheLowerOverlayButAllowsTheClearUpperOne() {
        BlockNeighborhood pair = headroomNeighborhood(Set.of(64, 65));
        pair.set(0, 64, 0);
        assertFalse(FullCubeHostPreflight.hasClearHeadroom(pair));
        pair.set(0, 65, 0);
        assertTrue(FullCubeHostPreflight.hasClearHeadroom(pair));
    }

    private static BlockNeighborhood neighborhood(
            int skylight,
            int blocklight,
            RenderSettings settings
    ) {
        return new ExactBlockNeighborhood(
                new PositionBlockAccess(0, 0, 0, skylight, blocklight),
                new ResourcePack(new PackVersion(34, 0)),
                settings,
                DimensionType.OVERWORLD
        );
    }

    private static void assertPosition(BlockNeighborhood block, int x, int y, int z) {
        block.set(x, y, z);
        assertEquals(x, block.getX());
        assertEquals(y, block.getY());
        assertEquals(z, block.getZ());
        assertEquals(
                Key.parse("test:position_" + x + "_" + y + "_" + z),
                block.getBlockState().getId()
        );
    }

    private static BlockNeighborhood headroomNeighborhood(Set<Integer> solidY) {
        return new ExactBlockNeighborhood(
                new YStateBlockAccess(solidY),
                new ResourcePack(new PackVersion(34, 0)),
                new TestRenderSettings(),
                DimensionType.OVERWORLD
        );
    }

    private static final class PositionBlockAccess implements BlockAccess {

        private int x;
        private int y;
        private int z;
        private final int skylight;
        private final int blocklight;

        private PositionBlockAccess(int x, int y, int z) {
            this(x, y, z, 15, 0);
        }

        private PositionBlockAccess(
                int x,
                int y,
                int z,
                int skylight,
                int blocklight
        ) {
            this.skylight = skylight;
            this.blocklight = blocklight;
            set(x, y, z);
        }

        @Override
        public void set(int newX, int newY, int newZ) {
            x = newX;
            y = newY;
            z = newZ;
        }

        @Override
        public BlockAccess copy() {
            return new PositionBlockAccess(x, y, z, skylight, blocklight);
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
            return new BlockState(Key.parse("test:position_" + x + "_" + y + "_" + z));
        }

        @Override
        public LightData getLightData() {
            return new LightData(skylight, blocklight);
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

    private static final class YStateBlockAccess implements BlockAccess {

        private final Set<Integer> solidY;
        private int x;
        private int y;
        private int z;

        private YStateBlockAccess(Set<Integer> solidY) {
            this.solidY = Set.copyOf(solidY);
        }

        @Override
        public void set(int newX, int newY, int newZ) {
            x = newX;
            y = newY;
            z = newZ;
        }

        @Override
        public BlockAccess copy() {
            YStateBlockAccess copy = new YStateBlockAccess(solidY);
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
            return solidY.contains(y)
                    ? new BlockState(Key.parse("test:full_cube"))
                    : BlockState.AIR;
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

        private final int removeCavesBelowY;
        private final boolean useBlocklight;

        private TestRenderSettings() {
            this(Integer.MIN_VALUE, false);
        }

        private TestRenderSettings(int removeCavesBelowY, boolean useBlocklight) {
            this.removeCavesBelowY = removeCavesBelowY;
            this.useBlocklight = useBlocklight;
        }

        @Override
        public int getRemoveCavesBelowY() {
            return removeCavesBelowY;
        }

        @Override
        public int getCaveDetectionOceanFloor() {
            return 0;
        }

        @Override
        public boolean isCaveDetectionUsesBlockLight() {
            return useBlocklight;
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
