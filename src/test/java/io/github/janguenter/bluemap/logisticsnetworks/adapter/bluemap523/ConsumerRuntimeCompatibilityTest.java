/*
 * SPDX-License-Identifier: MIT
 */
package io.github.janguenter.bluemap.logisticsnetworks.adapter.bluemap523;

import io.github.janguenter.bluemap.addon.adapter.api.bluemap523.BlueMapRuntimeCompatibility;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ConsumerRuntimeCompatibilityTest {

    private static final String VERSION =
            "5.22-feature.backport-5.23-stateless-java-web-server-46";
    private static final String COMMIT =
            "7e07f4e74ec1e92a6ead9aa1e66054af3e133aac";

    @Test
    void admitsOnlyTheExactAuditedFeatureBackport() {
        assertTrue(BlueMapRuntimeCompatibility.matches(VERSION, COMMIT));
        assertFalse(BlueMapRuntimeCompatibility.matches(VERSION, "unknown"));
        assertFalse(BlueMapRuntimeCompatibility.matches("5.23", COMMIT));
    }
}
