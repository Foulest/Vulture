package io.github.retrooper.packetevents.utils.versionlookup.viaversion;

import org.bukkit.entity.Player;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class ViaVersionAccessorImplLegacy implements ViaVersionAccessor {

    private static Class<?> viaClass;
    private static Method apiAccessor;
    private static Method getPlayerVersionMethod;

    @Override
    public int getProtocolVersion(Player player) {
        if (viaClass == null) {
            try {
                viaClass = Class.forName("us.myles.ViaVersion.api.Via");
                Class<?> viaAPIClass = Class.forName("us.myles.ViaVersion.api.ViaAPI");
                apiAccessor = viaClass.getMethod("getAPI");
                getPlayerVersionMethod = viaAPIClass.getMethod("getPlayerVersion", Object.class);
            } catch (ClassNotFoundException | NoSuchMethodException ex) {
                ex.printStackTrace();
            }
        }

        try {
            Object viaAPI = apiAccessor.invoke(null);
            return (int) getPlayerVersionMethod.invoke(viaAPI, player);
        } catch (IllegalAccessException | InvocationTargetException ex) {
            ex.printStackTrace();
        }
        return -1;
    }
}
