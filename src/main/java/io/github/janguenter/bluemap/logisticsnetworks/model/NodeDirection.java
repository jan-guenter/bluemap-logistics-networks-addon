/*
 * SPDX-License-Identifier: MIT
 */
package io.github.janguenter.bluemap.logisticsnetworks.model;

/** The six axial adjacency directions used by the static frame. */
public enum NodeDirection {
    DOWN(0, -1, 0, 1),
    UP(0, 1, 0, 1 << 1),
    NORTH(0, 0, -1, 1 << 2),
    SOUTH(0, 0, 1, 1 << 3),
    WEST(-1, 0, 0, 1 << 4),
    EAST(1, 0, 0, 1 << 5);

    private final int stepX;
    private final int stepY;
    private final int stepZ;
    private final int bit;

    NodeDirection(int stepX, int stepY, int stepZ, int bit) {
        this.stepX = stepX;
        this.stepY = stepY;
        this.stepZ = stepZ;
        this.bit = bit;
    }

    public int stepX() {
        return stepX;
    }

    public int stepY() {
        return stepY;
    }

    public int stepZ() {
        return stepZ;
    }

    public int bit() {
        return bit;
    }
}
