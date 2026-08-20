/*
 * SPDX-License-Identifier: MIT
 */
package io.github.janguenter.bluemap.logisticsnetworks.activation;

import java.util.Locale;

/** Shared fail-closed activation state for the exact profile. */
public final class LogisticsNetworksRuntime {

    public static final LogisticsNetworksRuntime INSTANCE = new LogisticsNetworksRuntime();

    private volatile State state = State.INACTIVE;
    private volatile String detail = "not-installed";

    private LogisticsNetworksRuntime() {
    }

    public boolean isActive() {
        return state == State.ACTIVE;
    }

    public State state() {
        return state;
    }

    public String detail() {
        return detail;
    }

    public synchronized void activate() {
        if (state != State.FAILED) {
            state = State.ACTIVE;
            detail = "exact-profile";
        }
    }

    public synchronized void inactive(String reason) {
        if (state != State.FAILED) {
            state = State.INACTIVE;
            detail = normalize(reason);
        }
    }

    public synchronized void fail(String reason) {
        state = State.FAILED;
        detail = normalize(reason);
    }

    private static String normalize(String value) {
        if (value == null) {
            throw new IllegalArgumentException("activation detail is null");
        }
        String normalized = value.trim().toLowerCase(Locale.ROOT).replace(' ', '-');
        if (!normalized.matches("[a-z0-9][a-z0-9._:-]*")) {
            throw new IllegalArgumentException("activation detail is not a wire value");
        }
        return normalized;
    }

    public enum State {
        INACTIVE,
        ACTIVE,
        FAILED
    }
}
