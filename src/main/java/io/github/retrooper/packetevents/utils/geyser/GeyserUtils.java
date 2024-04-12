package io.github.retrooper.packetevents.utils.geyser;


import io.github.retrooper.packetevents.utils.reflection.Reflection;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.UUID;

public class GeyserUtils {

    private static Class<?> GEYSER_CLASS;
    private static Class<?> GEYSER_API_CLASS;
    private static Method GEYSER_API_METHOD;
    private static Method CONNECTION_BY_UUID_METHOD;

    public static boolean isGeyserPlayer(UUID uuid) {
        if (GEYSER_CLASS == null) {
            GEYSER_CLASS = Reflection.getClassByNameWithoutException("org.geysermc.api.Geyser");
        }

        if (GEYSER_CLASS == null) {
            return false;
        }

        if (GEYSER_API_CLASS == null) {
            GEYSER_API_CLASS = Reflection.getClassByNameWithoutException("org.geysermc.api.GeyserApiBase");
        }

        if (GEYSER_API_METHOD == null) {
            GEYSER_API_METHOD = Reflection.getMethod(GEYSER_CLASS, "api", null, new Class<?>[]{});
        }

        if (CONNECTION_BY_UUID_METHOD == null) {
            CONNECTION_BY_UUID_METHOD = Reflection.getMethod(GEYSER_API_CLASS, "connectionByUuid", 0);
        }

        Object apiInstance = null;

        try {
            apiInstance = GEYSER_API_METHOD.invoke(null);
        } catch (IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }

        Object connection = null;

        try {
            if (apiInstance != null) {
                connection = CONNECTION_BY_UUID_METHOD.invoke(apiInstance, uuid);
            }
        } catch (IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
        return connection != null;
    }
}
