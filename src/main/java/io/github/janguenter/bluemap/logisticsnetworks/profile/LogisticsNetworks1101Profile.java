/*
 * SPDX-License-Identifier: MIT
 */
package io.github.janguenter.bluemap.logisticsnetworks.profile;

import de.bluecolored.bluemap.core.util.Key;

/** Exact All the Mons 1.2.0 profile for LogisticsNetworks 1.10.1. */
public final class LogisticsNetworks1101Profile {

    public static final String PROFILE_ID = "logisticsnetworks-1.10.1";
    public static final String MOD_ID = "logisticsnetworks";
    public static final String VERSION = "1.10.1";
    public static final String ARTIFACT = "logisticsnetworks-1.21.1-1.10.1.jar";
    public static final long JAR_SIZE = 988_995L;
    public static final String JAR_SHA1 = "57f685c27f64042be1d18e42bbecae5dc12477d3";
    public static final String JAR_SHA256 =
            "d94395da601ce93d8d7c9ffc434a018f6f46488303c654f6d6d5747961f56187";
    public static final int CURSEFORGE_PROJECT_ID = 1_448_257;
    public static final int CURSEFORGE_FILE_ID = 8_381_956;
    public static final String SOURCE_COMMIT =
            "d8554a27f6666caca0fddcc2ecab95944eb3ff77";
    public static final String SOURCE_TREE =
            "7a95bcfc47aa4aded3715e0f0e37f248385b4761";

    public static final String ENTITY_ID = "logisticsnetworks:logistics_node";
    public static final Key ENTITY_KEY = Key.parse(ENTITY_ID);
    public static final Key NODE_TEXTURE = Key.parse("logisticsnetworks:entity/node");
    public static final int NODE_TEXTURE_WIDTH = 64;
    public static final int NODE_TEXTURE_HEIGHT = 64;
    public static final long NODE_TEXTURE_SIZE = 4_291L;
    public static final String NODE_TEXTURE_SHA256 =
            "03194c53acc840f953e44838e1086a350f27036a0fcb44165bb5a1786ae7885e";
    public static final String NODE_TEXTURE_RGBA_SHA256 =
            "421cb4f00218f915d8d3626997b8aa6e3aa821e20fb6ec50d903eae615187e15";

    private LogisticsNetworks1101Profile() {
    }

    public static boolean acceptsArtifact(long size, String sha256) {
        return size == JAR_SIZE && JAR_SHA256.equals(sha256);
    }
}
