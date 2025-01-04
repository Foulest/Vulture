/*
 * This file is part of packetevents - https://github.com/retrooper/packetevents
 * Copyright (C) 2022 retrooper and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */
package net.foulest.packetevents.packetwrappers.api.helper;

import net.foulest.packetevents.PacketEvents;
import net.foulest.packetevents.packetwrappers.NMSPacket;
import net.foulest.packetevents.packetwrappers.WrappedPacket;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class WrappedPacketEntityAbstraction extends WrappedPacket {

    private final int entityIDFieldIndex;
    protected @Nullable Entity entity;
    protected int entityID = -1;

    protected WrappedPacketEntityAbstraction(NMSPacket packet, int entityIDFieldIndex) {
        super(packet);
        this.entityIDFieldIndex = entityIDFieldIndex;
    }

    protected WrappedPacketEntityAbstraction(NMSPacket packet) {
        super(packet);
        entityIDFieldIndex = 0;
    }

    protected WrappedPacketEntityAbstraction(int entityIDFieldIndex) {
        this.entityIDFieldIndex = entityIDFieldIndex;
    }

    protected WrappedPacketEntityAbstraction() {
        entityIDFieldIndex = 0;
    }

    public int getEntityId() {
        if (entityID != -1 || nmsPacket == null) {
            return entityID;
        }

        entityID = readInt(entityIDFieldIndex);
        return entityID;
    }

    public void setEntityId(int entityID) {
        if (nmsPacket != null) {
            this.entityID = entityID;
            writeInt(entityIDFieldIndex, this.entityID);
        } else {
            this.entityID = entityID;
        }

        entity = null;
    }

    public Entity getEntity(@Nullable World world) {
        if (entity != null) {
            return entity;
        }
        return PacketEvents.getInstance().getServerUtils().getEntityById(world, getEntityId());
    }

    public Entity getEntity() {
        if (entity != null) {
            return entity;
        }
        return PacketEvents.getInstance().getServerUtils().getEntityById(getEntityId());
    }

    protected void setEntity(@NotNull Entity entity) {
        setEntityId(entity.getEntityId());
        this.entity = entity;
    }
}
