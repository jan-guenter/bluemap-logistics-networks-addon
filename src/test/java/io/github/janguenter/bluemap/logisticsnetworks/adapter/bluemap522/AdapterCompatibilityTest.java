/*
 * SPDX-License-Identifier: MIT
 */
package io.github.janguenter.bluemap.logisticsnetworks.adapter.bluemap522;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AdapterCompatibilityTest {

    @Test
    void admitsOnlyTheTwoAuditedBlueMapIdentities() {
        assertTrue(AdapterCompatibility.supported(
                AdapterCompatibility.UPSTREAM_VERSION,
                AdapterCompatibility.UPSTREAM_COMMIT
        ));
        assertTrue(AdapterCompatibility.supported(
                AdapterCompatibility.BACKPORT_VERSION,
                AdapterCompatibility.BACKPORT_COMMIT
        ));
        assertFalse(AdapterCompatibility.supported(
                AdapterCompatibility.BACKPORT_VERSION,
                "0000000000000000000000000000000000000000"
        ));
        assertFalse(AdapterCompatibility.supported("5.23", AdapterCompatibility.UPSTREAM_COMMIT));
    }
}
