/*
 * SPDX-License-Identifier: MIT
 */
package io.github.janguenter.bluemap.logisticsnetworks.model;

import java.util.List;

/** Independently authored one-block-tall cage and translucent sheet plan. */
public final class FramePlan {

    public static final float THICKNESS = 1F / 8F;
    public static final float OUTSET = 1F / 256F;
    public static final float Y_MIN = 1F / 2F;
    public static final float HEIGHT = 1F;
    /** Project-authored depth-order gap below the top-rail underside. */
    public static final float SHEET_Y = Y_MIN + HEIGHT - THICKNESS - OUTSET;
    /** Project-authored vertical-sheet span, inset from its horizontal neighbors. */
    public static final float SIDE_Y_MIN = Y_MIN + THICKNESS + OUTSET;
    public static final float SIDE_Y_MAX = SHEET_Y - OUTSET;

    private static final float HORIZONTAL_MIN = -OUTSET;
    private static final float HORIZONTAL_MAX = 1F + OUTSET;
    private static final float HORIZONTAL_LOW = THICKNESS;
    private static final float HORIZONTAL_HIGH = 1F - THICKNESS;
    private static final float VERTICAL_MIN = Y_MIN - OUTSET;
    private static final float VERTICAL_MAX = Y_MIN + HEIGHT + OUTSET;
    private static final float VERTICAL_LOW = Y_MIN + THICKNESS;
    private static final float VERTICAL_HIGH = Y_MIN + HEIGHT - THICKNESS;
    private static final List<Part> PARTS = List.of(
            part(Role.LOWER_CORNER, sides(NodeDirection.NORTH, NodeDirection.WEST),
                    HORIZONTAL_MIN, VERTICAL_MIN, HORIZONTAL_MIN,
                    HORIZONTAL_LOW, VERTICAL_LOW, HORIZONTAL_LOW),
            part(Role.LOWER_CORNER, sides(NodeDirection.NORTH, NodeDirection.EAST),
                    HORIZONTAL_HIGH, VERTICAL_MIN, HORIZONTAL_MIN,
                    HORIZONTAL_MAX, VERTICAL_LOW, HORIZONTAL_LOW),
            part(Role.UPPER_CORNER, sides(NodeDirection.NORTH, NodeDirection.WEST),
                    HORIZONTAL_MIN, VERTICAL_HIGH, HORIZONTAL_MIN,
                    HORIZONTAL_LOW, VERTICAL_MAX, HORIZONTAL_LOW),
            part(Role.UPPER_CORNER, sides(NodeDirection.NORTH, NodeDirection.EAST),
                    HORIZONTAL_HIGH, VERTICAL_HIGH, HORIZONTAL_MIN,
                    HORIZONTAL_MAX, VERTICAL_MAX, HORIZONTAL_LOW),
            part(Role.LOWER_CORNER, sides(NodeDirection.SOUTH, NodeDirection.WEST),
                    HORIZONTAL_MIN, VERTICAL_MIN, HORIZONTAL_HIGH,
                    HORIZONTAL_LOW, VERTICAL_LOW, HORIZONTAL_MAX),
            part(Role.LOWER_CORNER, sides(NodeDirection.SOUTH, NodeDirection.EAST),
                    HORIZONTAL_HIGH, VERTICAL_MIN, HORIZONTAL_HIGH,
                    HORIZONTAL_MAX, VERTICAL_LOW, HORIZONTAL_MAX),
            part(Role.UPPER_CORNER, sides(NodeDirection.SOUTH, NodeDirection.WEST),
                    HORIZONTAL_MIN, VERTICAL_HIGH, HORIZONTAL_HIGH,
                    HORIZONTAL_LOW, VERTICAL_MAX, HORIZONTAL_MAX),
            part(Role.UPPER_CORNER, sides(NodeDirection.SOUTH, NodeDirection.EAST),
                    HORIZONTAL_HIGH, VERTICAL_HIGH, HORIZONTAL_HIGH,
                    HORIZONTAL_MAX, VERTICAL_MAX, HORIZONTAL_MAX),
            part(Role.LOWER_EDGE, sides(NodeDirection.NORTH),
                    HORIZONTAL_LOW, VERTICAL_MIN, HORIZONTAL_MIN,
                    HORIZONTAL_HIGH, VERTICAL_LOW, HORIZONTAL_LOW),
            part(Role.LOWER_EDGE, sides(NodeDirection.SOUTH),
                    HORIZONTAL_LOW, VERTICAL_MIN, HORIZONTAL_HIGH,
                    HORIZONTAL_HIGH, VERTICAL_LOW, HORIZONTAL_MAX),
            part(Role.UPPER_EDGE, sides(NodeDirection.NORTH),
                    HORIZONTAL_LOW, VERTICAL_HIGH, HORIZONTAL_MIN,
                    HORIZONTAL_HIGH, VERTICAL_MAX, HORIZONTAL_LOW),
            part(Role.UPPER_EDGE, sides(NodeDirection.SOUTH),
                    HORIZONTAL_LOW, VERTICAL_HIGH, HORIZONTAL_HIGH,
                    HORIZONTAL_HIGH, VERTICAL_MAX, HORIZONTAL_MAX),
            part(Role.VERTICAL_EDGE, sides(NodeDirection.NORTH, NodeDirection.WEST),
                    HORIZONTAL_MIN, VERTICAL_LOW, HORIZONTAL_MIN,
                    HORIZONTAL_LOW, VERTICAL_HIGH, HORIZONTAL_LOW),
            part(Role.VERTICAL_EDGE, sides(NodeDirection.NORTH, NodeDirection.EAST),
                    HORIZONTAL_HIGH, VERTICAL_LOW, HORIZONTAL_MIN,
                    HORIZONTAL_MAX, VERTICAL_HIGH, HORIZONTAL_LOW),
            part(Role.VERTICAL_EDGE, sides(NodeDirection.SOUTH, NodeDirection.WEST),
                    HORIZONTAL_MIN, VERTICAL_LOW, HORIZONTAL_HIGH,
                    HORIZONTAL_LOW, VERTICAL_HIGH, HORIZONTAL_MAX),
            part(Role.VERTICAL_EDGE, sides(NodeDirection.SOUTH, NodeDirection.EAST),
                    HORIZONTAL_HIGH, VERTICAL_LOW, HORIZONTAL_HIGH,
                    HORIZONTAL_MAX, VERTICAL_HIGH, HORIZONTAL_MAX),
            part(Role.LOWER_EDGE, sides(NodeDirection.WEST),
                    HORIZONTAL_MIN, VERTICAL_MIN, HORIZONTAL_LOW,
                    HORIZONTAL_LOW, VERTICAL_LOW, HORIZONTAL_HIGH),
            part(Role.LOWER_EDGE, sides(NodeDirection.EAST),
                    HORIZONTAL_HIGH, VERTICAL_MIN, HORIZONTAL_LOW,
                    HORIZONTAL_MAX, VERTICAL_LOW, HORIZONTAL_HIGH),
            part(Role.UPPER_EDGE, sides(NodeDirection.WEST),
                    HORIZONTAL_MIN, VERTICAL_HIGH, HORIZONTAL_LOW,
                    HORIZONTAL_LOW, VERTICAL_MAX, HORIZONTAL_HIGH),
            part(Role.UPPER_EDGE, sides(NodeDirection.EAST),
                    HORIZONTAL_HIGH, VERTICAL_HIGH, HORIZONTAL_LOW,
                    HORIZONTAL_MAX, VERTICAL_MAX, HORIZONTAL_HIGH)
    );

    private FramePlan() {
    }

    public static List<Part> parts() {
        return PARTS;
    }

    /** Parts retained after applying only screenshot-evidenced horizontal merging. */
    public static List<Part> parts(ConnectionMask mask) {
        return PARTS.stream()
                .filter(part -> !part.omittedBy(mask))
                .toList();
    }

    /** One upward sheet whose connected sides stop exactly on the local cell boundary. */
    public static TopSheet topSheet(ConnectionMask mask) {
        return new TopSheet(
                mask.contains(NodeDirection.WEST) ? 0F : HORIZONTAL_LOW,
                SHEET_Y,
                mask.contains(NodeDirection.NORTH) ? 0F : HORIZONTAL_LOW,
                mask.contains(NodeDirection.EAST) ? 1F : HORIZONTAL_HIGH,
                mask.contains(NodeDirection.SOUTH) ? 1F : HORIZONTAL_HIGH
        );
    }

    /** One outward-facing sheet in each exposed cardinal inner-side aperture. */
    public static List<SideSheet> sideSheets(ConnectionMask mask) {
        return java.util.stream.Stream.of(
                        sideSheet(NodeDirection.NORTH, mask),
                        sideSheet(NodeDirection.EAST, mask),
                        sideSheet(NodeDirection.SOUTH, mask),
                        sideSheet(NodeDirection.WEST, mask)
                )
                .filter(java.util.Objects::nonNull)
                .toList();
    }

    private static SideSheet sideSheet(NodeDirection direction, ConnectionMask mask) {
        if (mask.contains(direction)) {
            return null;
        }
        float lowX = mask.contains(NodeDirection.WEST) ? 0F : HORIZONTAL_LOW;
        float highX = mask.contains(NodeDirection.EAST) ? 1F : HORIZONTAL_HIGH;
        float lowZ = mask.contains(NodeDirection.NORTH) ? 0F : HORIZONTAL_LOW;
        float highZ = mask.contains(NodeDirection.SOUTH) ? 1F : HORIZONTAL_HIGH;
        return switch (direction) {
            case NORTH -> new SideSheet(direction,
                    lowX, SIDE_Y_MIN, HORIZONTAL_LOW + OUTSET,
                    highX, SIDE_Y_MAX, HORIZONTAL_LOW + OUTSET);
            case EAST -> new SideSheet(direction,
                    HORIZONTAL_HIGH - OUTSET, SIDE_Y_MIN, lowZ,
                    HORIZONTAL_HIGH - OUTSET, SIDE_Y_MAX, highZ);
            case SOUTH -> new SideSheet(direction,
                    lowX, SIDE_Y_MIN, HORIZONTAL_HIGH - OUTSET,
                    highX, SIDE_Y_MAX, HORIZONTAL_HIGH - OUTSET);
            case WEST -> new SideSheet(direction,
                    HORIZONTAL_LOW + OUTSET, SIDE_Y_MIN, lowZ,
                    HORIZONTAL_LOW + OUTSET, SIDE_Y_MAX, highZ);
            case DOWN, UP -> throw new IllegalArgumentException("side sheet must be cardinal");
        };
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

    /** Vertical caps remain closed even though the corrected envelopes touch. */
    public static boolean seamFacesMayBeOmitted(NodeDirection direction) {
        return switch (direction) {
            case DOWN, UP -> false;
            case NORTH, SOUTH, WEST, EAST -> envelopesContact(direction);
        };
    }

    private static float translatedNeighborGap(float minimum, float maximum) {
        return 1F + minimum - maximum;
    }

    /** Number of independently emitted frame faces, excluding the top sheet. */
    public static int frameQuadCount(ConnectionMask mask) {
        int count = 0;
        for (Part part : parts(mask)) {
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

    /** Frame faces plus one top sheet and one side sheet per exposed cardinal side. */
    public static int quadCount(ConnectionMask mask) {
        return frameQuadCount(mask) + 1 + sideSheets(mask).size();
    }

    private static Part part(
            Role role,
            int horizontalSides,
            float x0,
            float y0,
            float z0,
            float x1,
            float y1,
            float z1
    ) {
        return new Part(x0, y0, z0, x1, y1, z1, role, horizontalSides);
    }

    private static int sides(NodeDirection... directions) {
        int result = 0;
        for (NodeDirection direction : directions) {
            result |= direction.bit();
        }
        return result;
    }

    public enum Role {
        LOWER_CORNER,
        UPPER_CORNER,
        LOWER_EDGE,
        UPPER_EDGE,
        VERTICAL_EDGE
    }

    /** One non-overlapping corner joint or edge bar. */
    public record Part(
            float x0,
            float y0,
            float z0,
            float x1,
            float y1,
            float z1,
            Role role,
            int horizontalSides
    ) {
        public Part {
            if (!(x0 < x1) || !(y0 < y1) || !(z0 < z1) || role == null) {
                throw new IllegalArgumentException("invalid frame part");
            }
        }

        public boolean incidentTo(NodeDirection direction) {
            return (horizontalSides & direction.bit()) != 0;
        }

        public boolean omittedBy(ConnectionMask mask) {
            int connectedIncidentSides = mask.bits() & horizontalSides;
            return switch (role) {
                case UPPER_EDGE -> connectedIncidentSides != 0;
                case UPPER_CORNER -> connectedIncidentSides == horizontalSides;
                case VERTICAL_EDGE -> connectedIncidentSides != 0;
                case LOWER_CORNER, LOWER_EDGE -> false;
            };
        }

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

    /** Local top-sheet rectangle; adjacent rectangles meet but never overlap in area. */
    public record TopSheet(float x0, float y, float z0, float x1, float z1) {
        public TopSheet {
            if (!(x0 < x1) || !(z0 < z1) || !Float.isFinite(y)) {
                throw new IllegalArgumentException("invalid top sheet");
            }
        }

        public float area() {
            return (x1 - x0) * (z1 - z0);
        }
    }

    /** Local vertical sheet rectangle with an outward cardinal normal. */
    public record SideSheet(
            NodeDirection direction,
            float x0,
            float y0,
            float z0,
            float x1,
            float y1,
            float z1
    ) {
        public SideSheet {
            if (direction == null || direction == NodeDirection.DOWN
                    || direction == NodeDirection.UP || !(y0 < y1)
                    || !Float.isFinite(x0) || !Float.isFinite(z0)
                    || !Float.isFinite(x1) || !Float.isFinite(z1)
                    || !((x0 == x1 && z0 < z1) ^ (z0 == z1 && x0 < x1))) {
                throw new IllegalArgumentException("invalid side sheet");
            }
        }

        public float area() {
            return ((x1 - x0) + (z1 - z0)) * (y1 - y0);
        }
    }
}
