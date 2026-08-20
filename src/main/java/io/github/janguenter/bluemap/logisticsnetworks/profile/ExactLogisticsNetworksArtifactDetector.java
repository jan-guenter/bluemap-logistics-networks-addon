/*
 * SPDX-License-Identifier: MIT
 */
package io.github.janguenter.bluemap.logisticsnetworks.profile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashSet;
import java.util.HexFormat;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/** Bounded exact-byte detector for the operator-installed runtime artifact. */
public final class ExactLogisticsNetworksArtifactDetector {

    private static final int MAX_ROOTS = 4_096;
    private static final int MAX_DESCRIPTOR_BYTES = 1024 * 1024;
    private static final int BUFFER_SIZE = 64 * 1024;
    private static final String MOD_DESCRIPTOR = "META-INF/neoforge.mods.toml";
    private static final Pattern SHA256 = Pattern.compile("[0-9a-f]{64}");

    private ExactLogisticsNetworksArtifactDetector() {
    }

    /** Returns true only when one distinct declared artifact has the exact bytes. */
    public static boolean matches(Iterable<Path> roots, String sha256, long size) {
        Objects.requireNonNull(roots, "roots");
        Objects.requireNonNull(sha256, "sha256");
        if (size <= 0 || !SHA256.matcher(sha256).matches()
                || !LogisticsNetworks1101Profile.JAR_SHA256.equals(sha256)) {
            throw new IllegalArgumentException("invalid exact artifact identity");
        }

        Set<Path> inspected = new HashSet<>();
        Path candidate = null;
        int rootCount = 0;
        for (Path root : roots) {
            if (Thread.currentThread().isInterrupted()) {
                return false;
            }
            if (++rootCount > MAX_ROOTS) {
                return false;
            }
            if (root == null || !Files.isRegularFile(root)) {
                continue;
            }
            Path fileName = root.getFileName();
            if (fileName == null
                    || !fileName.toString().toLowerCase(Locale.ROOT).endsWith(".jar")) {
                continue;
            }
            try {
                Path real = root.toRealPath();
                if (!inspected.add(real) || !declaresTarget(real)) {
                    continue;
                }
                if (candidate != null) {
                    return false;
                }
                candidate = real;
            } catch (IOException exception) {
                return false;
            }
        }

        try {
            return candidate != null
                    && Files.size(candidate) == size
                    && sha256.equals(digest(candidate));
        } catch (IOException exception) {
            return false;
        }
    }

    private static boolean declaresTarget(Path jar) throws IOException {
        Pattern declaration = Pattern.compile(
                "^(?:modId|\\\"modId\\\"|'modId')\\s*=\\s*"
                        + "(?:\\\"logisticsnetworks\\\"|'logisticsnetworks')$"
        );
        try (ZipFile zip = new ZipFile(jar.toFile())) {
            ZipEntry descriptor = zip.getEntry(MOD_DESCRIPTOR);
            if (descriptor == null || descriptor.isDirectory()
                    || descriptor.getSize() > MAX_DESCRIPTOR_BYTES) {
                return false;
            }
            byte[] content;
            try (InputStream input = zip.getInputStream(descriptor)) {
                content = input.readNBytes(MAX_DESCRIPTOR_BYTES + 1);
            }
            if (content.length > MAX_DESCRIPTOR_BYTES) {
                return false;
            }
            boolean inModsTable = false;
            for (String line : new String(content, StandardCharsets.UTF_8).split("\\R", -1)) {
                int comment = line.indexOf('#');
                String statement = (comment < 0 ? line : line.substring(0, comment)).trim();
                if (statement.startsWith("[")) {
                    inModsTable = statement.equals("[[mods]]")
                            || statement.equals("[[\"mods\"]]")
                            || statement.equals("[['mods']]");
                } else if (inModsTable && declaration.matcher(statement).matches()) {
                    return true;
                }
            }
            return false;
        }
    }

    private static String digest(Path path) throws IOException {
        MessageDigest digest;
        try {
            digest = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 unavailable", exception);
        }
        byte[] buffer = new byte[BUFFER_SIZE];
        try (InputStream input = Files.newInputStream(path)) {
            int read;
            while ((read = input.read(buffer)) != -1) {
                digest.update(buffer, 0, read);
            }
        }
        return HexFormat.of().formatHex(digest.digest());
    }
}
