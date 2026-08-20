/*
 * SPDX-License-Identifier: MIT
 */
package io.github.janguenter.bluemap.logisticsnetworks.model;

/** Immutable bounded six-bit adjacency mask. */
public record ConnectionMask(int bits) {

    private static final int ALL_BITS = (1 << NodeDirection.values().length) - 1;

    public ConnectionMask {
        if ((bits & ~ALL_BITS) != 0) {
            throw new IllegalArgumentException("connection mask has unknown bits");
        }
    }

    public boolean contains(NodeDirection direction) {
        return (bits & direction.bit()) != 0;
    }

    public ConnectionMask with(NodeDirection direction) {
        return new ConnectionMask(bits | direction.bit());
    }

    public static ConnectionMask empty() {
        return new ConnectionMask(0);
    }
}
