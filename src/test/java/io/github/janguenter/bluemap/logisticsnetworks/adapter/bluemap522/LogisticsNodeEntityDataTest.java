/*
 * SPDX-License-Identifier: MIT
 */
package io.github.janguenter.bluemap.logisticsnetworks.adapter.bluemap522;

import de.bluecolored.bluenbt.BlueNBT;
import de.bluecolored.bluenbt.NBTWriter;
import de.bluecolored.bluemap.core.world.mca.MCAUtil;
import io.github.janguenter.bluemap.logisticsnetworks.model.BlockPosition;
import io.github.janguenter.bluemap.logisticsnetworks.model.NodeSnapshot;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LogisticsNodeEntityDataTest {

    @Test
    void readsOnlyTheThreeExactCustomFields() throws IOException {
        BlockPosition position = new BlockPosition(-21, 77, 42);
        LogisticsNodeEntityData data = read(writer -> {
            writer.name("AttachedPos").value(position.packed());
            writer.name("Valid").value((byte) 0);
            writer.name("RenderVisible").value((byte) 1);
            writer.name("NetworkName").value("intentionally ignored");
        });

        NodeSnapshot snapshot = new NodeSnapshotDecoder().decode(data).orElseThrow();
        assertEquals(position, snapshot.attachedPos());
        assertEquals(false, snapshot.valid());
        assertEquals(true, snapshot.renderVisible());
    }

    @Test
    void missingRequiredFieldFailsClosed() throws IOException {
        LogisticsNodeEntityData data = read(writer -> {
            writer.name("AttachedPos").value(new BlockPosition(1, 2, 3).packed());
            writer.name("Valid").value((byte) 1);
        });

        assertTrue(new NodeSnapshotDecoder().decode(data).isEmpty());
    }

    private static LogisticsNodeEntityData read(WriterAction action) throws IOException {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (NBTWriter writer = new NBTWriter(bytes)) {
            writer.beginCompound();
            action.write(writer);
            writer.endCompound();
        }
        return MCAUtil.addCommonNbtSettings(new BlueNBT()).read(
                new ByteArrayInputStream(bytes.toByteArray()),
                LogisticsNodeEntityData.class
        );
    }

    @FunctionalInterface
    private interface WriterAction {
        void write(NBTWriter writer) throws IOException;
    }
}
