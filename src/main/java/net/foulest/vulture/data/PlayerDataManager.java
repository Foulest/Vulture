package net.foulest.vulture.data;

import net.foulest.vulture.check.Check;
import net.foulest.vulture.check.CheckManager;
import org.bukkit.entity.Player;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerDataManager {

    private static final Map<UUID, PlayerData> playerDataMap = new HashMap<>();

    public static PlayerData getPlayerData(Player player) {
        if (playerDataMap.containsKey(player.getUniqueId())) {
            return playerDataMap.get(player.getUniqueId());
        } else {
            addPlayerData(player);
        }
        return playerDataMap.get(player.getUniqueId());
    }

    public static void addPlayerData(Player player) {
        if (!playerDataMap.containsKey(player.getUniqueId())) {
            PlayerData data = new PlayerData(player.getUniqueId(), player);

            for (Class<? extends Check> checkClass : CheckManager.CHECK_CLASSES) {
                try {
                    Constructor<? extends Check> constructor = checkClass.getConstructor(PlayerData.class);
                    Check checkInstance = constructor.newInstance(data);
                    data.getChecks().add(checkInstance);
                } catch (NoSuchMethodException | IllegalAccessException | InstantiationException |
                         InvocationTargetException ex) {
                    ex.printStackTrace();
                }
            }

            playerDataMap.put(player.getUniqueId(), data);
        }
    }

    public static void removePlayerData(Player player) {
        playerDataMap.remove(player.getUniqueId());
    }
}
