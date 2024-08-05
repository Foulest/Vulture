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
package io.github.retrooper.packetevents.packetwrappers.login.out.setcompression;

import io.github.retrooper.packetevents.packettype.PacketTypeClasses;
import io.github.retrooper.packetevents.packetwrappers.NMSPacket;
import io.github.retrooper.packetevents.packetwrappers.WrappedPacket;
import io.github.retrooper.packetevents.packetwrappers.api.SendableWrapper;
import lombok.AllArgsConstructor;
import lombok.ToString;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

@ToString
@AllArgsConstructor
public class WrappedPacketLoginOutSetCompression extends WrappedPacket implements SendableWrapper {

    private static Constructor<?> constructor;
    private int threshold;

    public WrappedPacketLoginOutSetCompression(NMSPacket packet) {
        super(packet);
    }

    @Override
    protected void load() {
        try {
            if (PacketTypeClasses.Login.Server.SET_COMPRESSION != null) {
                constructor = PacketTypeClasses.Login.Server.SET_COMPRESSION.getConstructor(int.class);
            }
        } catch (NoSuchMethodException ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Maximum size of a packet before it can be compressed.
     *
     * @return threshold Packet compression threshold
     */
    private int getThreshold() {
        if (nmsPacket != null) {
            return readInt(0);
        }
        return threshold;
    }

    public void setThreshold(int threshold) {
        if (nmsPacket != null) {
            writeInt(0, threshold);
        } else {
            this.threshold = threshold;
        }
    }

    @Override
    public Object asNMSPacket() throws InvocationTargetException, InstantiationException, IllegalAccessException {
        return constructor.newInstance(getThreshold());
    }
}
