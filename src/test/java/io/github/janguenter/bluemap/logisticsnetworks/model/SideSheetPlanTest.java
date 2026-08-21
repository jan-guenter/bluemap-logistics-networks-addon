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

class SideSheetPlanTest {

    private static final List<NodeDirection> CARDINALS = List.of(
            NodeDirection.NORTH,
            NodeDirection.EAST,
            NodeDirection.SOUTH,
            NodeDirection.WEST
    );

    @Test
    void allSixteenMasksExposeExactlyTheUnconnectedCardinalSides() {
        for (int bits = 0; bits < 16; bits++) {
            ConnectionMask mask = horizontalMask(bits);
            List<FramePlan.SideSheet> sheets = FramePlan.sideSheets(mask);
            assertEquals(4 - Integer.bitCount(bits), sheets.size());
            for (NodeDirection direction : CARDINALS) {
                assertEquals(!mask.contains(direction), sheets.stream()
                        .anyMatch(sheet -> sheet.direction() == direction));
            }
        }
    }

    @Test
    void exposedSheetsUseInsetPlanesAndExtendOnlyTowardPerpendicularNeighbors() {
        for (int bits = 0; bits < 16; bits++) {
            ConnectionMask mask = horizontalMask(bits);
            for (FramePlan.SideSheet sheet : FramePlan.sideSheets(mask)) {
                assertEquals(FramePlan.SIDE_Y_MIN, sheet.y0());
                assertEquals(FramePlan.SIDE_Y_MAX, sheet.y1());
                assertTrue(sheet.area() > 0F);
                switch (sheet.direction()) {
                    case NORTH -> {
                        assertEquals(FramePlan.THICKNESS + FramePlan.OUTSET, sheet.z0());
                        assertEquals(sheet.z0(), sheet.z1());
                        assertXSpan(mask, sheet);
                    }
                    case EAST -> {
                        assertEquals(1F - FramePlan.THICKNESS - FramePlan.OUTSET, sheet.x0());
                        assertEquals(sheet.x0(), sheet.x1());
                        assertZSpan(mask, sheet);
                    }
                    case SOUTH -> {
                        assertEquals(1F - FramePlan.THICKNESS - FramePlan.OUTSET, sheet.z0());
                        assertEquals(sheet.z0(), sheet.z1());
                        assertXSpan(mask, sheet);
                    }
                    case WEST -> {
                        assertEquals(FramePlan.THICKNESS + FramePlan.OUTSET, sheet.x0());
                        assertEquals(sheet.x0(), sheet.x1());
                        assertZSpan(mask, sheet);
                    }
                    case DOWN, UP -> throw new AssertionError("vertical side sheet");
                }
            }
        }
    }

    @Test
    void sidePlansRotateWithTheirConnectionMasks() {
        for (int bits = 0; bits < 16; bits++) {
            ConnectionMask mask = horizontalMask(bits);
            Set<FramePlan.SideSheet> rotated = new HashSet<>();
            for (FramePlan.SideSheet sheet : FramePlan.sideSheets(mask)) {
                rotated.add(rotate(sheet));
            }
            assertEquals(new HashSet<>(FramePlan.sideSheets(rotate(mask))), rotated);
        }
    }

    @Test
    void isolatedStraightElbowAndTwoByTwoHaveOnlyMeetingExteriorRectangles() {
        assertLayout(Set.of(new Cell(0, 0)));
        assertLayout(Set.of(new Cell(0, 0), new Cell(1, 0)));
        assertLayout(Set.of(new Cell(0, 0), new Cell(1, 0), new Cell(0, 1)));
        assertLayout(Set.of(
                new Cell(0, 0), new Cell(1, 0),
                new Cell(0, 1), new Cell(1, 1)
        ));
    }

    private static void assertLayout(Set<Cell> cells) {
        List<PlaneFace> panes = new ArrayList<>();
        List<PlaneFace> frameFaces = new ArrayList<>();
        int expectedExteriorSides = 0;
        for (Cell cell : cells) {
            ConnectionMask mask = maskFor(cell, cells);
            expectedExteriorSides += 4 - cardinalConnectionCount(mask);
            for (FramePlan.SideSheet sheet : FramePlan.sideSheets(mask)) {
                panes.add(face(cell, sheet));
                assertFalse(mask.contains(sheet.direction()));
            }
            for (NodeDirection direction : CARDINALS) {
                assertEquals(!mask.contains(direction), side(mask, direction) != null);
            }
            for (FramePlan.Part part : FramePlan.parts(mask)) {
                frameFaces.add(new PlaneFace(Axis.X, cell.x() + part.x0(),
                        cell.z() + part.z0(), cell.z() + part.z1(), part.y0(), part.y1()));
                frameFaces.add(new PlaneFace(Axis.X, cell.x() + part.x1(),
                        cell.z() + part.z0(), cell.z() + part.z1(), part.y0(), part.y1()));
                frameFaces.add(new PlaneFace(Axis.Z, cell.z() + part.z0(),
                        cell.x() + part.x0(), cell.x() + part.x1(), part.y0(), part.y1()));
                frameFaces.add(new PlaneFace(Axis.Z, cell.z() + part.z1(),
                        cell.x() + part.x0(), cell.x() + part.x1(), part.y0(), part.y1()));
            }
        }
        assertEquals(expectedExteriorSides, panes.size());
        assertNoPositiveAreaCoplanarOverlap(panes, panes, true);
        assertNoPositiveAreaCoplanarOverlap(panes, frameFaces, false);
        assertMeetingRuns(cells);
    }

    private static void assertMeetingRuns(Set<Cell> cells) {
        for (Cell cell : cells) {
            Cell east = new Cell(cell.x() + 1, cell.z());
            if (cells.contains(east)) {
                assertMeeting(cell, east, cells, NodeDirection.NORTH);
                assertMeeting(cell, east, cells, NodeDirection.SOUTH);
            }
            Cell south = new Cell(cell.x(), cell.z() + 1);
            if (cells.contains(south)) {
                assertMeeting(cell, south, cells, NodeDirection.WEST);
                assertMeeting(cell, south, cells, NodeDirection.EAST);
            }
        }
    }

    private static void assertMeeting(
            Cell first,
            Cell second,
            Set<Cell> cells,
            NodeDirection exteriorDirection
    ) {
        FramePlan.SideSheet firstSheet = side(maskFor(first, cells), exteriorDirection);
        FramePlan.SideSheet secondSheet = side(maskFor(second, cells), exteriorDirection);
        if (firstSheet == null || secondSheet == null) {
            return;
        }
        PlaneFace firstFace = face(first, firstSheet);
        PlaneFace secondFace = face(second, secondSheet);
        assertEquals(firstFace.plane(), secondFace.plane());
        assertEquals(firstFace.high(), secondFace.low());
        assertFalse(firstFace.overlapsInArea(secondFace));
    }

    private static void assertNoPositiveAreaCoplanarOverlap(
            List<PlaneFace> leftFaces,
            List<PlaneFace> rightFaces,
            boolean sameList
    ) {
        for (int left = 0; left < leftFaces.size(); left++) {
            int start = sameList ? left + 1 : 0;
            for (int right = start; right < rightFaces.size(); right++) {
                assertFalse(leftFaces.get(left).overlapsInArea(rightFaces.get(right)));
            }
        }
    }

    private static PlaneFace face(Cell cell, FramePlan.SideSheet sheet) {
        return switch (sheet.direction()) {
            case NORTH, SOUTH -> new PlaneFace(
                    Axis.Z, cell.z() + sheet.z0(),
                    cell.x() + sheet.x0(), cell.x() + sheet.x1(), sheet.y0(), sheet.y1());
            case EAST, WEST -> new PlaneFace(
                    Axis.X, cell.x() + sheet.x0(),
                    cell.z() + sheet.z0(), cell.z() + sheet.z1(), sheet.y0(), sheet.y1());
            case DOWN, UP -> throw new AssertionError("vertical side sheet");
        };
    }

    private static FramePlan.SideSheet side(ConnectionMask mask, NodeDirection direction) {
        return FramePlan.sideSheets(mask).stream()
                .filter(sheet -> sheet.direction() == direction)
                .findFirst()
                .orElse(null);
    }

    private static void assertXSpan(ConnectionMask mask, FramePlan.SideSheet sheet) {
        assertEquals(mask.contains(NodeDirection.WEST) ? 0F : FramePlan.THICKNESS, sheet.x0());
        assertEquals(mask.contains(NodeDirection.EAST) ? 1F : 1F - FramePlan.THICKNESS, sheet.x1());
    }

    private static void assertZSpan(ConnectionMask mask, FramePlan.SideSheet sheet) {
        assertEquals(mask.contains(NodeDirection.NORTH) ? 0F : FramePlan.THICKNESS, sheet.z0());
        assertEquals(mask.contains(NodeDirection.SOUTH)
                ? 1F : 1F - FramePlan.THICKNESS, sheet.z1());
    }

    private static int cardinalConnectionCount(ConnectionMask mask) {
        return (int) CARDINALS.stream().filter(mask::contains).count();
    }

    private static ConnectionMask maskFor(Cell cell, Set<Cell> cells) {
        ConnectionMask result = ConnectionMask.empty();
        for (NodeDirection direction : CARDINALS) {
            if (cells.contains(new Cell(
                    cell.x() + direction.stepX(),
                    cell.z() + direction.stepZ()))) {
                result = result.with(direction);
            }
        }
        return result;
    }

    private static ConnectionMask horizontalMask(int bits) {
        ConnectionMask result = ConnectionMask.empty();
        for (int index = 0; index < CARDINALS.size(); index++) {
            if ((bits & 1 << index) != 0) {
                result = result.with(CARDINALS.get(index));
            }
        }
        return result;
    }

    private static ConnectionMask rotate(ConnectionMask mask) {
        ConnectionMask result = ConnectionMask.empty();
        for (NodeDirection direction : CARDINALS) {
            if (mask.contains(direction)) {
                result = result.with(rotate(direction));
            }
        }
        return result;
    }

    private static FramePlan.SideSheet rotate(FramePlan.SideSheet sheet) {
        float firstX = 1F - sheet.z0();
        float secondX = 1F - sheet.z1();
        return new FramePlan.SideSheet(
                rotate(sheet.direction()),
                Math.min(firstX, secondX), sheet.y0(), Math.min(sheet.x0(), sheet.x1()),
                Math.max(firstX, secondX), sheet.y1(), Math.max(sheet.x0(), sheet.x1())
        );
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

    private enum Axis {
        X,
        Z
    }

    private record Cell(int x, int z) {
    }

    private record PlaneFace(
            Axis axis,
            float plane,
            float low,
            float high,
            float y0,
            float y1
    ) {
        boolean overlapsInArea(PlaneFace other) {
            return axis == other.axis && plane == other.plane
                    && Math.min(high, other.high) > Math.max(low, other.low)
                    && Math.min(y1, other.y1) > Math.max(y0, other.y0);
        }
    }
}
