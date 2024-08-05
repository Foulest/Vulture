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
package io.github.retrooper.packetevents.packetwrappers.login.out.custompayload;

import io.github.retrooper.packetevents.PacketEvents;
import io.github.retrooper.packetevents.packetwrappers.NMSPacket;
import io.github.retrooper.packetevents.packetwrappers.WrappedPacket;
import io.github.retrooper.packetevents.packetwrappers.api.SendableWrapper;
import io.github.retrooper.packetevents.packetwrappers.api.WrapperPacketReader;
import io.github.retrooper.packetevents.utils.nms.NMSUtils;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

@ToString
@AllArgsConstructor
public class WrappedPacketLoginOutCustomPayload extends WrappedPacket implements SendableWrapper {

    @Getter
    @Setter
    private static Constructor<?> constructor;
    @Getter
    @Setter
    private static Constructor<?> packetDataSerializerConstructor;

    private int messageID;
    private String channelName;
    private byte[] data;

    public WrappedPacketLoginOutCustomPayload(NMSPacket packet) {
        super(packet);
    }

    private int getMessageId() {
        if (nmsPacket != null) {
            return readInt(0);
        }
        return messageID;
    }

    public void setMessageId(int messageID) {
        if (nmsPacket != null) {
            writeInt(0, messageID);
        } else {
            this.messageID = messageID;
        }
    }

    public String getChannelName() {
        if (nmsPacket != null) {
            return readMinecraftKey(0);
        } else {
            return channelName;
        }
    }

    public void setChannelName(String channelName) {
        if (nmsPacket != null) {
            writeMinecraftKey(0, channelName);
        } else {
            this.channelName = channelName;
        }
    }

    public byte[] getData() {
        if (nmsPacket != null) {
            Object dataSerializer = readObject(0, NMSUtils.packetDataSerializerClass);
            WrapperPacketReader byteBufWrapper = new WrappedPacket(new NMSPacket(dataSerializer));
            Object byteBuf = byteBufWrapper.readObject(0, NMSUtils.byteBufClass);
            return PacketEvents.getInstance().getByteBufUtil().getBytes(byteBuf);
        } else {
            return data;
        }
    }

    public void setData(byte[] data) {
        if (nmsPacket != null) {
            PacketEvents.getInstance().getByteBufUtil().setBytes(getBuffer(), data);
        } else {
            this.data = data;
        }
    }

    private Object getBuffer() {
        Object dataSerializer = readObject(0, NMSUtils.packetDataSerializerClass);
        WrapperPacketReader byteBufWrapper = new WrappedPacket(new NMSPacket(dataSerializer));
        return byteBufWrapper.readObject(0, NMSUtils.byteBufClass);
    }

    @Override
    public Object asNMSPacket() throws InvocationTargetException, InstantiationException, IllegalAccessException {
        Object byteBufObject = PacketEvents.getInstance().getByteBufUtil().newByteBuf(data);
        Object minecraftKey = NMSUtils.generateMinecraftKeyNew(channelName);
        Object dataSerializer = packetDataSerializerConstructor.newInstance(byteBufObject);
        return constructor.newInstance(getMessageId(), minecraftKey, dataSerializer);
    }
}
