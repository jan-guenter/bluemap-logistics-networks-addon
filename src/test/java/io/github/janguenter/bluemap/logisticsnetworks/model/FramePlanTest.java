/*
 * SPDX-License-Identifier: MIT
 */
package io.github.janguenter.bluemap.logisticsnetworks.model;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FramePlanTest {

    private static final List<NodeDirection> HORIZONTAL = List.of(
            NodeDirection.NORTH,
            NodeDirection.EAST,
            NodeDirection.SOUTH,
            NodeDirection.WEST
    );

    @Test
    void standaloneCageHasTwentyFramePartsAndOneTopSheet() {
        ConnectionMask isolated = ConnectionMask.empty();
        assertEquals(20, FramePlan.parts(isolated).size());
        assertEquals(120, FramePlan.frameQuadCount(isolated));
        assertEquals(121, FramePlan.quadCount(isolated));
        assertEquals(1, FramePlan.quadCount(isolated) - FramePlan.frameQuadCount(isolated));
        assertEquals(4, roleCount(isolated, FramePlan.Role.UPPER_EDGE));
        assertEquals(4, roleCount(isolated, FramePlan.Role.UPPER_CORNER));
        assertEquals(
                new FramePlan.TopSheet(
                        FramePlan.THICKNESS,
                        FramePlan.SHEET_Y,
                        FramePlan.THICKNESS,
                        1F - FramePlan.THICKNESS,
                        1F - FramePlan.THICKNESS
                ),
                FramePlan.topSheet(isolated)
        );
    }

    @Test
    void allSixteenHorizontalMasksHaveExactRailsCornersAndCoverage() {
        for (int horizontalBits = 0; horizontalBits < 16; horizontalBits++) {
            ConnectionMask mask = horizontalMask(horizontalBits);
            int connectedSides = Integer.bitCount(horizontalBits);
            int expectedCorners = 0;
            for (FramePlan.Part part : FramePlan.parts()) {
                if (part.role() == FramePlan.Role.UPPER_CORNER
                        && (part.horizontalSides() & mask.bits()) != part.horizontalSides()) {
                    expectedCorners++;
                }
            }

            assertEquals(4 - connectedSides, roleCount(mask, FramePlan.Role.UPPER_EDGE));
            assertEquals(expectedCorners, roleCount(mask, FramePlan.Role.UPPER_CORNER));
            assertEquals(12, nonUpperPartCount(mask));
            assertEquals(1, FramePlan.quadCount(mask) - FramePlan.frameQuadCount(mask));
            assertSheetBounds(mask);
            assertNoTopFootprintOverlap(mask);
        }
    }

    @Test
    void everyHorizontalMaskIsRotationallySymmetric() {
        for (int horizontalBits = 0; horizontalBits < 16; horizontalBits++) {
            ConnectionMask mask = horizontalMask(horizontalBits);
            ConnectionMask rotatedMask = rotate(mask);

            Set<PartShape> rotatedParts = new HashSet<>();
            for (FramePlan.Part part : FramePlan.parts(mask)) {
                rotatedParts.add(rotate(part));
            }
            Set<PartShape> plannedParts = new HashSet<>();
            for (FramePlan.Part part : FramePlan.parts(rotatedMask)) {
                plannedParts.add(new PartShape(
                        part.x0(), part.y0(), part.z0(),
                        part.x1(), part.y1(), part.z1(),
                        part.role(), part.horizontalSides()
                ));
            }
            assertEquals(plannedParts, rotatedParts);
            assertEquals(rotate(FramePlan.topSheet(mask)), FramePlan.topSheet(rotatedMask));
        }
    }

    @Test
    void isolatedStraightLAndTwoByTwoHaveOnlyTheirOuterTopPerimeters() {
        assertLayout(Set.of(new Cell(0, 0)), 4, 4);
        assertLayout(Set.of(new Cell(0, 0), new Cell(1, 0)), 6, 8);
        assertLayout(Set.of(new Cell(0, 0), new Cell(1, 0), new Cell(0, 1)), 8, 11);

        Set<Cell> square = Set.of(
                new Cell(0, 0), new Cell(1, 0),
                new Cell(0, 1), new Cell(1, 1)
        );
        assertLayout(square, 8, 12);
        assertFalse(hasUpperCorner(maskFor(new Cell(0, 0), square),
                NodeDirection.EAST, NodeDirection.SOUTH));
        assertFalse(hasUpperCorner(maskFor(new Cell(1, 0), square),
                NodeDirection.WEST, NodeDirection.SOUTH));
        assertFalse(hasUpperCorner(maskFor(new Cell(0, 1), square),
                NodeDirection.EAST, NodeDirection.NORTH));
        assertFalse(hasUpperCorner(maskFor(new Cell(1, 1), square),
                NodeDirection.WEST, NodeDirection.NORTH));
    }

    @Test
    void adjacentSheetRectanglesMeetExactlyAtTheCellBoundaryWithoutAreaOverlap() {
        Set<Cell> straight = Set.of(new Cell(0, 0), new Cell(1, 0));
        FramePlan.TopSheet west = FramePlan.topSheet(maskFor(new Cell(0, 0), straight));
        FramePlan.TopSheet east = FramePlan.topSheet(maskFor(new Cell(1, 0), straight));
        Rectangle westWorld = new Rectangle(west.x0(), west.z0(), west.x1(), west.z1());
        Rectangle eastWorld = new Rectangle(
                1F + east.x0(), east.z0(), 1F + east.x1(), east.z1()
        );

        assertEquals(1F, westWorld.x1());
        assertEquals(1F, eastWorld.x0());
        assertEquals(westWorld.z0(), eastWorld.z0());
        assertEquals(westWorld.z1(), eastWorld.z1());
        assertFalse(westWorld.overlapsInArea(eastWorld));
    }

    @Test
    void verticalConnectionsRetainClosedCapsAndDoNotAlterTheTopSurface() {
        ConnectionMask vertical = ConnectionMask.empty()
                .with(NodeDirection.DOWN)
                .with(NodeDirection.UP);
        assertEquals(-2F * FramePlan.OUTSET,
                FramePlan.neighborEnvelopeGap(NodeDirection.DOWN));
        assertEquals(-2F * FramePlan.OUTSET,
                FramePlan.neighborEnvelopeGap(NodeDirection.UP));
        assertTrue(FramePlan.envelopesContact(NodeDirection.DOWN));
        assertTrue(FramePlan.envelopesContact(NodeDirection.UP));
        assertFalse(FramePlan.seamFacesMayBeOmitted(NodeDirection.DOWN));
        assertFalse(FramePlan.seamFacesMayBeOmitted(NodeDirection.UP));
        assertEquals(FramePlan.parts(ConnectionMask.empty()), FramePlan.parts(vertical));
        assertEquals(FramePlan.topSheet(ConnectionMask.empty()), FramePlan.topSheet(vertical));
        assertEquals(FramePlan.quadCount(ConnectionMask.empty()), FramePlan.quadCount(vertical));
    }

    @Test
    void usesTheRevisedProvisionalProjectAuthoredDimensions() {
        assertEquals(1F / 8F, FramePlan.THICKNESS);
        assertEquals(1F / 256F, FramePlan.OUTSET);
        assertEquals(1F / 2F, FramePlan.Y_MIN);
        assertEquals(1F, FramePlan.HEIGHT);
        assertEquals(FramePlan.Y_MIN + FramePlan.HEIGHT - FramePlan.THICKNESS,
                FramePlan.SHEET_Y);
        assertEquals(-FramePlan.OUTSET, minimum(FramePlan.Part::x0));
        assertEquals(1F + FramePlan.OUTSET, maximum(FramePlan.Part::x1));
        assertEquals(FramePlan.Y_MIN - FramePlan.OUTSET, minimum(FramePlan.Part::y0));
        assertEquals(FramePlan.Y_MIN + FramePlan.HEIGHT + FramePlan.OUTSET,
                maximum(FramePlan.Part::y1));
        assertEquals(-FramePlan.OUTSET, minimum(FramePlan.Part::z0));
        assertEquals(1F + FramePlan.OUTSET, maximum(FramePlan.Part::z1));
    }

    private static void assertSheetBounds(ConnectionMask mask) {
        FramePlan.TopSheet sheet = FramePlan.topSheet(mask);
        assertEquals(mask.contains(NodeDirection.WEST) ? 0F : FramePlan.THICKNESS, sheet.x0());
        assertEquals(mask.contains(NodeDirection.EAST) ? 1F : 1F - FramePlan.THICKNESS, sheet.x1());
        assertEquals(mask.contains(NodeDirection.NORTH) ? 0F : FramePlan.THICKNESS, sheet.z0());
        assertEquals(mask.contains(NodeDirection.SOUTH) ? 1F : 1F - FramePlan.THICKNESS, sheet.z1());
        assertEquals(FramePlan.SHEET_Y, sheet.y());
        assertTrue(sheet.area() > 0F);
    }

    private static void assertNoTopFootprintOverlap(ConnectionMask mask) {
        List<Rectangle> rectangles = new ArrayList<>();
        FramePlan.TopSheet sheet = FramePlan.topSheet(mask);
        rectangles.add(new Rectangle(sheet.x0(), sheet.z0(), sheet.x1(), sheet.z1()));
        for (FramePlan.Part part : FramePlan.parts(mask)) {
            if (part.role() == FramePlan.Role.UPPER_EDGE
                    || part.role() == FramePlan.Role.UPPER_CORNER) {
                rectangles.add(new Rectangle(part.x0(), part.z0(), part.x1(), part.z1()));
            }
        }
        for (int left = 0; left < rectangles.size(); left++) {
            for (int right = left + 1; right < rectangles.size(); right++) {
                assertFalse(rectangles.get(left).overlapsInArea(rectangles.get(right)));
            }
        }
    }

    private static void assertLayout(
            Set<Cell> cells,
            int expectedUpperEdges,
            int expectedUpperCorners
    ) {
        int upperEdges = 0;
        int upperCorners = 0;
        List<Rectangle> sheets = new ArrayList<>();
        for (Cell cell : cells) {
            ConnectionMask mask = maskFor(cell, cells);
            upperEdges += roleCount(mask, FramePlan.Role.UPPER_EDGE);
            upperCorners += roleCount(mask, FramePlan.Role.UPPER_CORNER);
            for (NodeDirection direction : HORIZONTAL) {
                long rails = FramePlan.parts(mask).stream()
                        .filter(part -> part.role() == FramePlan.Role.UPPER_EDGE)
                        .filter(part -> part.incidentTo(direction))
                        .count();
                assertEquals(mask.contains(direction) ? 0 : 1, rails);
            }
            FramePlan.TopSheet sheet = FramePlan.topSheet(mask);
            sheets.add(new Rectangle(
                    cell.x() + sheet.x0(),
                    cell.z() + sheet.z0(),
                    cell.x() + sheet.x1(),
                    cell.z() + sheet.z1()
            ));
        }
        assertEquals(expectedUpperEdges, upperEdges);
        assertEquals(expectedUpperCorners, upperCorners);
        for (int left = 0; left < sheets.size(); left++) {
            for (int right = left + 1; right < sheets.size(); right++) {
                assertFalse(sheets.get(left).overlapsInArea(sheets.get(right)));
            }
        }
    }

    private static ConnectionMask maskFor(Cell cell, Set<Cell> cells) {
        ConnectionMask result = ConnectionMask.empty();
        for (NodeDirection direction : HORIZONTAL) {
            Cell neighbor = new Cell(
                    cell.x() + direction.stepX(),
                    cell.z() + direction.stepZ()
            );
            if (cells.contains(neighbor)) {
                result = result.with(direction);
            }
        }
        return result;
    }

    private static boolean hasUpperCorner(
            ConnectionMask mask,
            NodeDirection first,
            NodeDirection second
    ) {
        return FramePlan.parts(mask).stream()
                .anyMatch(part -> part.role() == FramePlan.Role.UPPER_CORNER
                        && part.incidentTo(first) && part.incidentTo(second));
    }

    private static int roleCount(ConnectionMask mask, FramePlan.Role role) {
        return (int) FramePlan.parts(mask).stream()
                .filter(part -> part.role() == role)
                .count();
    }

    private static int nonUpperPartCount(ConnectionMask mask) {
        return (int) FramePlan.parts(mask).stream()
                .filter(part -> part.role() != FramePlan.Role.UPPER_EDGE)
                .filter(part -> part.role() != FramePlan.Role.UPPER_CORNER)
                .count();
    }

    private static ConnectionMask horizontalMask(int bits) {
        ConnectionMask result = ConnectionMask.empty();
        for (int index = 0; index < HORIZONTAL.size(); index++) {
            if ((bits & 1 << index) != 0) {
                result = result.with(HORIZONTAL.get(index));
            }
        }
        return result;
    }

    private static ConnectionMask rotate(ConnectionMask mask) {
        ConnectionMask result = ConnectionMask.empty();
        for (NodeDirection direction : HORIZONTAL) {
            if (mask.contains(direction)) {
                result = result.with(rotate(direction));
            }
        }
        if (mask.contains(NodeDirection.DOWN)) {
            result = result.with(NodeDirection.DOWN);
        }
        if (mask.contains(NodeDirection.UP)) {
            result = result.with(NodeDirection.UP);
        }
        return result;
    }

    private static NodeDirection rotate(NodeDirection direction) {
        return switch (direction) {
            case NORTH -> NodeDirection.EAST;
            case EAST -> NodeDirection.SOUTH;
            case SOUTH -> NodeDirection.WEST;
            case WEST -> NodeDirection.NORTH;
            case DOWN, UP -> direction;
        };
    }

    private static PartShape rotate(FramePlan.Part part) {
        int rotatedSides = 0;
        for (NodeDirection direction : HORIZONTAL) {
            if (part.incidentTo(direction)) {
                rotatedSides |= rotate(direction).bit();
            }
        }
        return new PartShape(
                1F - part.z1(), part.y0(), part.x0(),
                1F - part.z0(), part.y1(), part.x1(),
                part.role(), rotatedSides
        );
    }

    private static FramePlan.TopSheet rotate(FramePlan.TopSheet sheet) {
        return new FramePlan.TopSheet(
                1F - sheet.z1(), sheet.y(), sheet.x0(),
                1F - sheet.z0(), sheet.x1()
        );
    }

    private static float minimum(Coordinate coordinate) {
        return FramePlan.parts().stream().map(coordinate::value)
                .min(Float::compare).orElseThrow();
    }

    private static float maximum(Coordinate coordinate) {
        return FramePlan.parts().stream().map(coordinate::value)
                .max(Float::compare).orElseThrow();
    }

    @FunctionalInterface
    private interface Coordinate {
        float value(FramePlan.Part part);
    }

    private record Cell(int x, int z) {
    }

    private record Rectangle(float x0, float z0, float x1, float z1) {
        boolean overlapsInArea(Rectangle other) {
            return Math.min(x1, other.x1) > Math.max(x0, other.x0)
                    && Math.min(z1, other.z1) > Math.max(z0, other.z0);
        }
    }

    private record PartShape(
            float x0,
            float y0,
            float z0,
            float x1,
            float y1,
            float z1,
            FramePlan.Role role,
            int horizontalSides
    ) {
    }
}
