/*
 * SPDX-License-Identifier: MIT
 */
package io.github.janguenter.bluemap.logisticsnetworks.model;

import java.util.List;

/** Independently authored shifted frame with eight joints and twelve edges. */
public final class FramePlan {

    public static final float THICKNESS = 1F / 8F;
    public static final float OUTSET = 1F / 256F;
    public static final float Y_MIN = 1F / 2F;
    public static final float HEIGHT = 1F / 2F;

    private static final float HORIZONTAL_MIN = -OUTSET;
    private static final float HORIZONTAL_MAX = 1F + OUTSET;
    private static final float HORIZONTAL_LOW = THICKNESS;
    private static final float HORIZONTAL_HIGH = 1F - THICKNESS;
    private static final float VERTICAL_MIN = Y_MIN - OUTSET;
    private static final float VERTICAL_MAX = Y_MIN + HEIGHT + OUTSET;
    private static final float VERTICAL_LOW = Y_MIN + THICKNESS;
    private static final float VERTICAL_HIGH = Y_MIN + HEIGHT - THICKNESS;
    private static final List<Part> PARTS = List.of(
            new Part(HORIZONTAL_MIN, VERTICAL_MIN, HORIZONTAL_MIN,
                    HORIZONTAL_LOW, VERTICAL_LOW, HORIZONTAL_LOW),
            new Part(HORIZONTAL_HIGH, VERTICAL_MIN, HORIZONTAL_MIN,
                    HORIZONTAL_MAX, VERTICAL_LOW, HORIZONTAL_LOW),
            new Part(HORIZONTAL_MIN, VERTICAL_HIGH, HORIZONTAL_MIN,
                    HORIZONTAL_LOW, VERTICAL_MAX, HORIZONTAL_LOW),
            new Part(HORIZONTAL_HIGH, VERTICAL_HIGH, HORIZONTAL_MIN,
                    HORIZONTAL_MAX, VERTICAL_MAX, HORIZONTAL_LOW),
            new Part(HORIZONTAL_MIN, VERTICAL_MIN, HORIZONTAL_HIGH,
                    HORIZONTAL_LOW, VERTICAL_LOW, HORIZONTAL_MAX),
            new Part(HORIZONTAL_HIGH, VERTICAL_MIN, HORIZONTAL_HIGH,
                    HORIZONTAL_MAX, VERTICAL_LOW, HORIZONTAL_MAX),
            new Part(HORIZONTAL_MIN, VERTICAL_HIGH, HORIZONTAL_HIGH,
                    HORIZONTAL_LOW, VERTICAL_MAX, HORIZONTAL_MAX),
            new Part(HORIZONTAL_HIGH, VERTICAL_HIGH, HORIZONTAL_HIGH,
                    HORIZONTAL_MAX, VERTICAL_MAX, HORIZONTAL_MAX),
            new Part(HORIZONTAL_LOW, VERTICAL_MIN, HORIZONTAL_MIN,
                    HORIZONTAL_HIGH, VERTICAL_LOW, HORIZONTAL_LOW),
            new Part(HORIZONTAL_LOW, VERTICAL_MIN, HORIZONTAL_HIGH,
                    HORIZONTAL_HIGH, VERTICAL_LOW, HORIZONTAL_MAX),
            new Part(HORIZONTAL_LOW, VERTICAL_HIGH, HORIZONTAL_MIN,
                    HORIZONTAL_HIGH, VERTICAL_MAX, HORIZONTAL_LOW),
            new Part(HORIZONTAL_LOW, VERTICAL_HIGH, HORIZONTAL_HIGH,
                    HORIZONTAL_HIGH, VERTICAL_MAX, HORIZONTAL_MAX),
            new Part(HORIZONTAL_MIN, VERTICAL_LOW, HORIZONTAL_MIN,
                    HORIZONTAL_LOW, VERTICAL_HIGH, HORIZONTAL_LOW),
            new Part(HORIZONTAL_HIGH, VERTICAL_LOW, HORIZONTAL_MIN,
                    HORIZONTAL_MAX, VERTICAL_HIGH, HORIZONTAL_LOW),
            new Part(HORIZONTAL_MIN, VERTICAL_LOW, HORIZONTAL_HIGH,
                    HORIZONTAL_LOW, VERTICAL_HIGH, HORIZONTAL_MAX),
            new Part(HORIZONTAL_HIGH, VERTICAL_LOW, HORIZONTAL_HIGH,
                    HORIZONTAL_MAX, VERTICAL_HIGH, HORIZONTAL_MAX),
            new Part(HORIZONTAL_MIN, VERTICAL_MIN, HORIZONTAL_LOW,
                    HORIZONTAL_LOW, VERTICAL_LOW, HORIZONTAL_HIGH),
            new Part(HORIZONTAL_HIGH, VERTICAL_MIN, HORIZONTAL_LOW,
                    HORIZONTAL_MAX, VERTICAL_LOW, HORIZONTAL_HIGH),
            new Part(HORIZONTAL_MIN, VERTICAL_HIGH, HORIZONTAL_LOW,
                    HORIZONTAL_LOW, VERTICAL_MAX, HORIZONTAL_HIGH),
            new Part(HORIZONTAL_HIGH, VERTICAL_HIGH, HORIZONTAL_LOW,
                    HORIZONTAL_MAX, VERTICAL_MAX, HORIZONTAL_HIGH)
    );

    private FramePlan() {
    }

    public static List<Part> parts() {
        return PARTS;
    }

    /** Positive when adjacent translated envelopes are separated. */
    public static float neighborEnvelopeGap(NodeDirection direction) {
        return switch (direction) {
            case DOWN, UP -> translatedNeighborGap(VERTICAL_MIN, VERTICAL_MAX);
            case NORTH, SOUTH, WEST, EAST ->
                    translatedNeighborGap(HORIZONTAL_MIN, HORIZONTAL_MAX);
        };
    }

    public static boolean envelopesContact(NodeDirection direction) {
        return neighborEnvelopeGap(direction) <= 0F;
    }

    /** Only evidenced horizontal contacts may open a frame seam. */
    public static boolean seamFacesMayBeOmitted(NodeDirection direction) {
        return switch (direction) {
            case DOWN, UP -> false;
            case NORTH, SOUTH, WEST, EAST -> envelopesContact(direction);
        };
    }

    private static float translatedNeighborGap(float minimum, float maximum) {
        return 1F + minimum - maximum;
    }

    public static int quadCount(ConnectionMask mask) {
        int count = 0;
        for (Part part : PARTS) {
            count += 6;
            for (NodeDirection direction : NodeDirection.values()) {
                if (mask.contains(direction)
                        && seamFacesMayBeOmitted(direction)
                        && part.touches(direction)) {
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
                case DOWN -> y0 == VERTICAL_MIN;
                case UP -> y1 == VERTICAL_MAX;
                case NORTH -> z0 == HORIZONTAL_MIN;
                case SOUTH -> z1 == HORIZONTAL_MAX;
                case WEST -> x0 == HORIZONTAL_MIN;
                case EAST -> x1 == HORIZONTAL_MAX;
            };
        }
    }
}
