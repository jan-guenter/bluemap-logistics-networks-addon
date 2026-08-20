/*
 * SPDX-License-Identifier: MIT
 */
package io.github.janguenter.bluemap.logisticsnetworks.model;

/** Minecraft's stable packed block-position value, decoded without game classes. */
public record BlockPosition(int x, int y, int z) implements Comparable<BlockPosition> {

    private static final long X_MASK = 0x3ff_ffffL;
    private static final long Y_MASK = 0xfffL;
    private static final long Z_MASK = 0x3ff_ffffL;

    public static BlockPosition fromPacked(long packed) {
        int x = (int) (packed >> 38);
        int y = (int) (packed << 52 >> 52);
        int z = (int) (packed << 26 >> 38);
        return new BlockPosition(x, y, z);
    }

    public long packed() {
        return ((long) x & X_MASK) << 38
                | ((long) z & Z_MASK) << 12
                | (long) y & Y_MASK;
    }

    public BlockPosition offset(NodeDirection direction) {
        return new BlockPosition(
                x + direction.stepX(),
                y + direction.stepY(),
                z + direction.stepZ()
        );
    }

    @Override
    public int compareTo(BlockPosition other) {
        int byX = Integer.compare(x, other.x);
        if (byX != 0) {
            return byX;
        }
        int byY = Integer.compare(y, other.y);
        return byY != 0 ? byY : Integer.compare(z, other.z);
    }
}
