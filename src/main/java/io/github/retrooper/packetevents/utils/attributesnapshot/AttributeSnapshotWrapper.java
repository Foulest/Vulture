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
package io.github.retrooper.packetevents.utils.attributesnapshot;

import io.github.retrooper.packetevents.packettype.PacketTypeClasses;
import io.github.retrooper.packetevents.packetwrappers.NMSPacket;
import io.github.retrooper.packetevents.packetwrappers.WrappedPacket;
import io.github.retrooper.packetevents.utils.nms.NMSUtils;
import io.github.retrooper.packetevents.utils.reflection.Reflection;
import io.github.retrooper.packetevents.utils.reflection.SubclassUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

public class AttributeSnapshotWrapper extends WrappedPacket {

    private static byte constructorMode;
    private static Constructor<?> attributeSnapshotConstructor;
    private static Class<?> attributeSnapshotClass;
    private static Class<?> attributeBaseClass;
    private static Field iRegistryAttributeBaseField;
    private static Method getiRegistryByMinecraftKeyMethod;
    private static boolean stringKeyPresent;

    public AttributeSnapshotWrapper(NMSPacket packet) {
        super(packet);
    }

    public AttributeSnapshotWrapper(String key, double value, List<AttributeModifierWrapper> modifiers) {
        super(Objects.requireNonNull(create(key, value, modifiers)).packet);
    }

    public static @Nullable AttributeSnapshotWrapper create(String key, double value,
                                                            Collection<AttributeModifierWrapper> modifiers) {
        Object nmsAttributeSnapshot = null;

        if (attributeSnapshotClass == null) {
            attributeSnapshotClass = NMSUtils.getNMSClassWithoutException("AttributeSnapshot");

            if (attributeSnapshotClass == null) {
                attributeSnapshotClass = SubclassUtil.getSubClass(PacketTypeClasses.Play.Server.UPDATE_ATTRIBUTES, "AttributeSnapshot");
            }
        }

        if (attributeSnapshotConstructor == null) {
            try {
                attributeSnapshotConstructor = attributeSnapshotClass.getConstructor(PacketTypeClasses.Play.Server.UPDATE_ATTRIBUTES, String.class, double.class, Collection.class);
                constructorMode = 0;
            } catch (NoSuchMethodException e) {
                try {
                    attributeSnapshotConstructor = attributeSnapshotClass.getConstructor(String.class, double.class, Collection.class);
                    constructorMode = 1;
                } catch (NoSuchMethodException e2) {
                    constructorMode = 2;

                    if (attributeBaseClass == null) {
                        attributeBaseClass = NMSUtils.getNMSClassWithoutException("AttributeBase");
                    }

                    try {
                        attributeSnapshotConstructor = attributeSnapshotClass.getConstructor(attributeBaseClass, double.class, Collection.class);
                    } catch (NoSuchMethodException e3) {
                        e3.printStackTrace();
                    }

                    Class<?> iRegistryClass = NMSUtils.getNMSClassWithoutException("IRegistry");

                    if (iRegistryClass != null) {
                        try {
                            iRegistryAttributeBaseField = iRegistryClass.getField("ATTRIBUTE");
                            getiRegistryByMinecraftKeyMethod = iRegistryClass.getDeclaredMethod("get", NMSUtils.minecraftKeyClass);
                        } catch (NoSuchFieldException | NoSuchMethodException ex) {
                            ex.printStackTrace();
                        }
                    }
                }
            }
        }

        // Check if attributeSnapshotConstructor is still null after initialization attempts
        if (attributeSnapshotConstructor == null) {
            throw new IllegalStateException("Failed to initialize attributeSnapshotConstructor");
        }

        List<Object> nmsModifiers = new ArrayList<>(modifiers.size());

        for (AttributeModifierWrapper modifier : modifiers) {
            nmsModifiers.add(modifier.getNMSPacket().getRawNMSPacket());
        }

        try {
            switch (constructorMode) {
                case 0:
                    nmsAttributeSnapshot = attributeSnapshotConstructor.newInstance(null, key, value, nmsModifiers);
                    break;

                case 1:
                    nmsAttributeSnapshot = attributeSnapshotConstructor.newInstance(key, value, nmsModifiers);
                    break;

                case 2:
                    Object minecraftKey = NMSUtils.generateMinecraftKeyNew(key);
                    Object attributeObj = iRegistryAttributeBaseField.get(null);
                    Object nmsAttributeBase = getiRegistryByMinecraftKeyMethod.invoke(attributeObj, minecraftKey);
                    nmsAttributeSnapshot = attributeSnapshotConstructor.newInstance(nmsAttributeBase, value, nmsModifiers);
                    break;

                default:
                    return null;
            }
        } catch (IllegalAccessException | InstantiationException | InvocationTargetException ex) {
            ex.printStackTrace();
        }
        return new AttributeSnapshotWrapper(new NMSPacket(nmsAttributeSnapshot));
    }

    @Override
    protected void load() {
        stringKeyPresent = Reflection.getField(packet.getRawNMSPacket().getClass(), String.class, 0) != null;

        if (attributeBaseClass == null) {
            attributeBaseClass = NMSUtils.getNMSClassWithoutException("AttributeBase");

            if (attributeBaseClass == null) {
                attributeBaseClass = NMSUtils.getNMClassWithoutException("world.entity.ai.attributes.AttributeBase");
            }
        }

        if (attributeSnapshotClass == null) {
            attributeSnapshotClass = NMSUtils.getNMSClassWithoutException("AttributeSnapshot");

            if (attributeSnapshotClass == null) {
                attributeSnapshotClass = SubclassUtil.getSubClass(PacketTypeClasses.Play.Server.UPDATE_ATTRIBUTES, "AttributeSnapshot");
            }
        }

        if (attributeSnapshotConstructor == null) {
            try {
                attributeSnapshotConstructor = attributeSnapshotClass.getConstructor(attributeBaseClass, double.class, Collection.class);
            } catch (NoSuchMethodException ex) {
                ex.printStackTrace();
            }
        }

        Class<?> iRegistryClass = NMSUtils.getNMSClassWithoutException("IRegistry");

        if (iRegistryClass == null) {
            iRegistryClass = NMSUtils.getNMClassWithoutException("core.IRegistry");
        }

        if (iRegistryClass != null) {
            try {
                iRegistryAttributeBaseField = iRegistryClass.getField("ATTRIBUTE");
                getiRegistryByMinecraftKeyMethod = iRegistryClass.getDeclaredMethod("get", NMSUtils.minecraftKeyClass);
            } catch (NoSuchFieldException | NoSuchMethodException ex) {
                ex.printStackTrace();
            }
        }
    }

    public String getKey() {
        if (stringKeyPresent) {
            return readString(0);
        } else {
            Object attributeBase = readObject(0, attributeBaseClass);
            WrappedPacket attributeBaseWrapper = new WrappedPacket(new NMSPacket(attributeBase), attributeBaseClass);
            return attributeBaseWrapper.readString(0);
        }
    }

    public void setKey(String identifier) {
        if (stringKeyPresent) {
            writeString(0, identifier);
        } else {
            Object minecraftKey = NMSUtils.generateMinecraftKeyNew(identifier);
            Object attributeObj = null;

            try {
                attributeObj = iRegistryAttributeBaseField.get(null);
            } catch (IllegalAccessException ex) {
                ex.printStackTrace();
            }

            Object nmsAttributeBase = null;

            try {
                nmsAttributeBase = getiRegistryByMinecraftKeyMethod.invoke(attributeObj, minecraftKey);
            } catch (IllegalAccessException | InvocationTargetException ex) {
                ex.printStackTrace();
            }

            write(attributeBaseClass, 0, nmsAttributeBase);
        }
    }

    public double getValue() {
        return readDouble(0);
    }

    public void setValue(double value) {
        writeDouble(0, value);
    }

    public Collection<AttributeModifierWrapper> getModifiers() {
        Collection<?> collection = readObject(0, Collection.class);
        Collection<AttributeModifierWrapper> modifierWrappers = new ArrayList<>(collection.size());

        for (Object obj : collection) {
            modifierWrappers.add(new AttributeModifierWrapper(new NMSPacket(obj)));
        }
        return modifierWrappers;
    }

    public void setModifiers(@NotNull Collection<AttributeModifierWrapper> attributeModifiers) {
        Collection<Object> collection = new ArrayList<>(attributeModifiers.size());

        for (AttributeModifierWrapper modifierWrapper : attributeModifiers) {
            collection.add(modifierWrapper.getNMSPacket().getRawNMSPacket());
        }

        writeObject(0, collection);
    }

    public NMSPacket getNMSPacket() {
        return packet;
    }
}
