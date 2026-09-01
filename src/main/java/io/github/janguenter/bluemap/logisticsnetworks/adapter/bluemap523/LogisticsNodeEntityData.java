/*
 * SPDX-License-Identifier: MIT
 */
package io.github.janguenter.bluemap.logisticsnetworks.adapter.bluemap523;

import de.bluecolored.bluemap.core.world.mca.entity.MCAEntity;
import de.bluecolored.bluenbt.NBTName;

/** BlueNBT projection of the three exact stable custom fields used by the pass. */
public final class LogisticsNodeEntityData extends MCAEntity {

    @NBTName("AttachedPos")
    private Long attachedPos;

    @NBTName("Valid")
    private Boolean valid;

    @NBTName("RenderVisible")
    private Boolean renderVisible;

    public LogisticsNodeEntityData() {
    }

    Long attachedPos() {
        return attachedPos;
    }

    Boolean valid() {
        return valid;
    }

    Boolean renderVisible() {
        return renderVisible;
    }
}
