/*
 * SPDX-License-Identifier: MIT
 */
package io.github.janguenter.bluemap.logisticsnetworks.profile;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ExactLogisticsNetworksArtifactDetectorTest {

    @TempDir
    Path temporary;

    @Test
    void acceptsExactArtifactAndRejectsDistinctDuplicate() throws IOException {
        String property = System.getProperty("logisticsNetworksJar");
        assertNotNull(property, "exact artifact system property is required by the gate");
        Path exact = Path.of(property);

        assertTrue(ExactLogisticsNetworksArtifactDetector.matches(
                List.of(exact),
                LogisticsNetworks1101Profile.JAR_SHA256,
                LogisticsNetworks1101Profile.JAR_SIZE
        ));

        Path duplicate = temporary.resolve("duplicate.jar");
        Files.copy(exact, duplicate);
        assertFalse(ExactLogisticsNetworksArtifactDetector.matches(
                List.of(exact, duplicate),
                LogisticsNetworks1101Profile.JAR_SHA256,
                LogisticsNetworks1101Profile.JAR_SIZE
        ));
    }

    @Test
    void rejectsMissingAndWrongByteArtifacts() throws IOException {
        Path wrong = temporary.resolve("wrong.jar");
        Files.write(wrong, new byte[]{1, 2, 3});
        assertFalse(ExactLogisticsNetworksArtifactDetector.matches(
                List.of(wrong),
                LogisticsNetworks1101Profile.JAR_SHA256,
                LogisticsNetworks1101Profile.JAR_SIZE
        ));
        assertFalse(ExactLogisticsNetworksArtifactDetector.matches(
                List.of(temporary.resolve("missing.jar")),
                LogisticsNetworks1101Profile.JAR_SHA256,
                LogisticsNetworks1101Profile.JAR_SIZE
        ));
    }
}
