package io.github.retrooper.packetevents.packetwrappers.play.out.playerinfo;

import io.github.retrooper.packetevents.packettype.PacketTypeClasses;
import io.github.retrooper.packetevents.packetwrappers.NMSPacket;
import io.github.retrooper.packetevents.packetwrappers.WrappedPacket;
import io.github.retrooper.packetevents.packetwrappers.api.SendableWrapper;
import io.github.retrooper.packetevents.utils.enums.EnumUtil;
import io.github.retrooper.packetevents.utils.gameprofile.GameProfileUtil;
import io.github.retrooper.packetevents.utils.gameprofile.WrappedGameProfile;
import io.github.retrooper.packetevents.utils.nms.NMSUtils;
import io.github.retrooper.packetevents.utils.reflection.SubclassUtil;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.bukkit.GameMode;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

@ToString
public class WrappedPacketOutPlayerInfo extends WrappedPacket implements SendableWrapper {

    private static Class<? extends Enum<?>> enumPlayerInfoActionClass;
    private static Constructor<?> packetConstructor;
    private static Constructor<?> playerInfoDataConstructor;
    private static byte constructorMode;

    private PlayerInfoAction action;
    private PlayerInfo[] playerInfoArray = new PlayerInfo[0];

    private WrappedPacketOutPlayerInfo(NMSPacket packet) {
        super(packet);
    }

    public WrappedPacketOutPlayerInfo(PlayerInfoAction action, PlayerInfo... playerInfoArray) {
        this.action = action;
        this.playerInfoArray = playerInfoArray;
    }

    @Override
    protected void load() {
        enumPlayerInfoActionClass = SubclassUtil.getEnumSubClass(PacketTypeClasses.Play.Server.PLAYER_INFO, "EnumPlayerInfoAction");

        try {
            packetConstructor = PacketTypeClasses.Play.Server.PLAYER_INFO.getConstructor();
        } catch (NoSuchMethodException ex) {
            ex.printStackTrace();
        }

        Class<?> playerInfoDataClass = SubclassUtil.getSubClass(PacketTypeClasses.Play.Server.PLAYER_INFO, "PlayerInfoData");

        if (playerInfoDataClass != null) {
            try {
                playerInfoDataConstructor = playerInfoDataClass.getConstructor(NMSUtils.gameProfileClass, int.class, NMSUtils.enumGameModeClass, NMSUtils.iChatBaseComponentClass);
            } catch (NoSuchMethodException e) {
                try {
                    playerInfoDataConstructor = playerInfoDataClass.getConstructor(PacketTypeClasses.Play.Server.PLAYER_INFO, NMSUtils.gameProfileClass, int.class, NMSUtils.enumGameModeClass, NMSUtils.iChatBaseComponentClass);
                    constructorMode = 1;
                } catch (NoSuchMethodException e2) {
                    e.printStackTrace();
                    e2.printStackTrace();
                }
            }
        }
    }

    private PlayerInfoAction getAction() {
        if (nmsPacket != null) {
            int index;
            Enum<?> enumConst = readEnumConstant(0, enumPlayerInfoActionClass);
            index = enumConst.ordinal();
            return PlayerInfoAction.values()[index];
        } else {
            return action;
        }
    }

    private void setAction(PlayerInfoAction action) {
        if (nmsPacket != null) {
            Enum<?> enumConst = EnumUtil.valueByIndex(enumPlayerInfoActionClass.asSubclass(Enum.class), action.ordinal());
            writeEnumConstant(0, enumConst);
        } else {
            this.action = action;
        }
    }

    private PlayerInfo[] getPlayerInfo() {
        if (nmsPacket != null) {
            PlayerInfo[] playerInfos = new PlayerInfo[1];
            List<Object> nmsPlayerInfoDataList = readList();

            for (int i = 0; i < nmsPlayerInfoDataList.size(); i++) {
                Object nmsPlayerInfoData = nmsPlayerInfoDataList.get(i);
                WrappedPacket nmsPlayerInfoDataWrapper = new WrappedPacket(new NMSPacket(nmsPlayerInfoData));
                String username = nmsPlayerInfoDataWrapper.readIChatBaseComponent(0);
                Object mojangGameProfile = nmsPlayerInfoDataWrapper.readObject(0, NMSUtils.gameProfileClass);
                WrappedGameProfile gameProfile = GameProfileUtil.getWrappedGameProfile(mojangGameProfile);
                GameMode gameMode = nmsPlayerInfoDataWrapper.readGameMode(0);
                int ping = nmsPlayerInfoDataWrapper.readInt(0);
                playerInfos[i] = new PlayerInfo(username, gameProfile, gameMode, ping);
            }
            return playerInfos;
        } else {
            return playerInfoArray;
        }
    }

    private void setPlayerInfo(PlayerInfo... playerInfoArray) {
        if (nmsPacket != null) {
            List<Object> nmsPlayerInfoList = new ArrayList<>();

            for (PlayerInfo playerInfo : playerInfoArray) {
                Object usernameIChatBaseComponent = NMSUtils.generateIChatBaseComponent(NMSUtils.fromStringToJSON(playerInfo.username));
                Object mojangGameProfile = GameProfileUtil.getGameProfile(playerInfo.gameProfile.getId(), playerInfo.gameProfile.getName());
                Enum<?> nmsGameModeEnumConstant = EnumUtil.valueByIndex(NMSUtils.enumGameModeClass.asSubclass(Enum.class), playerInfo.gameMode.ordinal());
                int ping = playerInfo.ping;

                try {
                    if (constructorMode == 0) {
                        nmsPlayerInfoList.add(playerInfoDataConstructor.newInstance(mojangGameProfile, ping, nmsGameModeEnumConstant, usernameIChatBaseComponent));
                    } else if (constructorMode == 1) {
                        nmsPlayerInfoList.add(playerInfoDataConstructor.newInstance(null, mojangGameProfile, ping, nmsGameModeEnumConstant, usernameIChatBaseComponent));
                    }
                } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
                    e.printStackTrace();
                }
            }

            writeList(nmsPlayerInfoList);
        } else {
            this.playerInfoArray = playerInfoArray;
        }
    }

    @Override
    public Object asNMSPacket() throws InvocationTargetException, InstantiationException, IllegalAccessException {
        Object packetInstance = packetConstructor.newInstance();
        WrappedPacketOutPlayerInfo playerInfoWrapper = new WrappedPacketOutPlayerInfo(new NMSPacket(packetInstance));
        PlayerInfo[] playerInfos = getPlayerInfo();

        if (playerInfos.length != 0) {
            playerInfoWrapper.setPlayerInfo(playerInfos);
        }

        playerInfoWrapper.setAction(getAction());
        return packetInstance;
    }

    public enum PlayerInfoAction {
        ADD_PLAYER,
        UPDATE_GAME_MODE,
        UPDATE_LATENCY,
        UPDATE_DISPLAY_NAME,
        REMOVE_PLAYER
    }

    @Getter
    @Setter
    @ToString
    @AllArgsConstructor
    public static class PlayerInfo {

        private String username;
        private WrappedGameProfile gameProfile;
        private GameMode gameMode;
        private int ping;
    }
}
