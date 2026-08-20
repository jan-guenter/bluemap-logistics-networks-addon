/*
 * SPDX-License-Identifier: MIT
 */
package io.github.janguenter.bluemap.logisticsnetworks.profile;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LogisticsNetworks1101ProfileTest {

    @Test
    void acceptsOnlyTheExactRuntimeIdentity() {
        assertTrue(LogisticsNetworks1101Profile.acceptsArtifact(
                988_995L,
                "d94395da601ce93d8d7c9ffc434a018f6f46488303c654f6d6d5747961f56187"
        ));
        assertFalse(LogisticsNetworks1101Profile.acceptsArtifact(
                988_994L,
                LogisticsNetworks1101Profile.JAR_SHA256
        ));
        assertFalse(LogisticsNetworks1101Profile.acceptsArtifact(
                LogisticsNetworks1101Profile.JAR_SIZE,
                "0000000000000000000000000000000000000000000000000000000000000000"
        ));
    }
}
