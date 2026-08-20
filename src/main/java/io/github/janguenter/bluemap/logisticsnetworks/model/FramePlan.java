/*
 * SPDX-License-Identifier: MIT
 */
package io.github.janguenter.bluemap.logisticsnetworks.model;

import java.util.List;

/** Independently authored unit-cube frame with eight joints and twelve edges. */
public final class FramePlan {

    public static final float THICKNESS = 3F / 16F;
    public static final float OUTSET = 1F / 64F;

    private static final float MIN = -OUTSET;
    private static final float MAX = 1F + OUTSET;
    private static final float LOW = THICKNESS;
    private static final float HIGH = 1F - THICKNESS;
    private static final List<Part> PARTS = List.of(
            new Part(MIN, MIN, MIN, LOW, LOW, LOW),
            new Part(HIGH, MIN, MIN, MAX, LOW, LOW),
            new Part(MIN, HIGH, MIN, LOW, MAX, LOW),
            new Part(HIGH, HIGH, MIN, MAX, MAX, LOW),
            new Part(MIN, MIN, HIGH, LOW, LOW, MAX),
            new Part(HIGH, MIN, HIGH, MAX, LOW, MAX),
            new Part(MIN, HIGH, HIGH, LOW, MAX, MAX),
            new Part(HIGH, HIGH, HIGH, MAX, MAX, MAX),
            new Part(LOW, MIN, MIN, HIGH, LOW, LOW),
            new Part(LOW, MIN, HIGH, HIGH, LOW, MAX),
            new Part(LOW, HIGH, MIN, HIGH, MAX, LOW),
            new Part(LOW, HIGH, HIGH, HIGH, MAX, MAX),
            new Part(MIN, LOW, MIN, LOW, HIGH, LOW),
            new Part(HIGH, LOW, MIN, MAX, HIGH, LOW),
            new Part(MIN, LOW, HIGH, LOW, HIGH, MAX),
            new Part(HIGH, LOW, HIGH, MAX, HIGH, MAX),
            new Part(MIN, MIN, LOW, LOW, LOW, HIGH),
            new Part(HIGH, MIN, LOW, MAX, LOW, HIGH),
            new Part(MIN, HIGH, LOW, LOW, MAX, HIGH),
            new Part(HIGH, HIGH, LOW, MAX, MAX, HIGH)
    );

    private FramePlan() {
    }

    public static List<Part> parts() {
        return PARTS;
    }

    public static int quadCount(ConnectionMask mask) {
        int count = 0;
        for (Part part : PARTS) {
            count += 6;
            for (NodeDirection direction : NodeDirection.values()) {
                if (mask.contains(direction) && part.touches(direction)) {
                    count--;
                }
            }
        }
        return count;
    }

    /** One non-overlapping corner joint or edge bar. */
    public record Part(
            float x0,
            float y0,
            float z0,
            float x1,
            float y1,
            float z1
    ) {
        public boolean touches(NodeDirection direction) {
            return switch (direction) {
                case DOWN -> y0 == MIN;
                case UP -> y1 == MAX;
                case NORTH -> z0 == MIN;
                case SOUTH -> z1 == MAX;
                case WEST -> x0 == MIN;
                case EAST -> x1 == MAX;
            };
        }
    }
}
