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
package io.github.retrooper.packetevents.packettype;

import io.github.retrooper.packetevents.utils.reflection.Reflection;
import io.github.retrooper.packetevents.utils.reflection.SubclassUtil;
import io.github.retrooper.packetevents.utils.server.ServerVersion;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class PacketTypeClasses {

    public static void load() {
        // STATUS
        PacketTypeClasses.Status.Client.load();
        PacketTypeClasses.Status.Server.load();

        // HANDSHAKING
        PacketTypeClasses.Handshaking.Client.load();

        // LOGIN
        PacketTypeClasses.Login.Client.load();
        PacketTypeClasses.Login.Server.load();

        // PLAY
        PacketTypeClasses.Play.Client.load();
        PacketTypeClasses.Play.Server.load();
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class Status {

        @NoArgsConstructor(access = AccessLevel.PRIVATE)
        public static class Client {

            public static Class<?> START;
            public static Class<?> PING;

            public static void load() {
                String prefix = ServerVersion.getNMSDirectory() + ".";
                Client.START = Reflection.getClassByNameWithoutException(prefix + "PacketStatusInStart");
                Client.PING = Reflection.getClassByNameWithoutException(prefix + "PacketStatusInPing");
            }
        }

        @NoArgsConstructor(access = AccessLevel.PRIVATE)
        public static class Server {

            public static Class<?> PONG;
            public static Class<?> SERVER_INFO;

            public static void load() {
                String prefix = ServerVersion.getNMSDirectory() + ".";
                Server.PONG = Reflection.getClassByNameWithoutException(prefix + "PacketStatusOutPong");
                Server.SERVER_INFO = Reflection.getClassByNameWithoutException(prefix + "PacketStatusOutServerInfo");
            }
        }
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class Handshaking {

        @NoArgsConstructor(access = AccessLevel.PRIVATE)
        public static class Client {

            public static Class<?> SET_PROTOCOL;

            public static void load() {
                String prefix = ServerVersion.getNMSDirectory() + ".";
                Handshaking.Client.SET_PROTOCOL = Reflection.getClassByNameWithoutException(prefix + "PacketHandshakingInSetProtocol");
            }
        }
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class Login {

        @NoArgsConstructor(access = AccessLevel.PRIVATE)
        public static class Client {

            public static Class<?> START;
            public static Class<?> ENCRYPTION_BEGIN;

            public static void load() {
                String prefix = ServerVersion.getNMSDirectory() + ".";

                Client.START = Reflection.getClassByNameWithoutException(prefix + "PacketLoginInStart");
                Client.ENCRYPTION_BEGIN = Reflection.getClassByNameWithoutException(prefix + "PacketLoginInEncryptionBegin");
            }
        }

        @NoArgsConstructor(access = AccessLevel.PRIVATE)
        public static class Server {

            public static Class<?> DISCONNECT;
            public static Class<?> ENCRYPTION_BEGIN;
            public static Class<?> SUCCESS;
            public static Class<?> SET_COMPRESSION;

            public static void load() {
                String prefix = ServerVersion.getNMSDirectory() + ".";

                Server.DISCONNECT = Reflection.getClassByNameWithoutException(prefix + "PacketLoginOutDisconnect");
                Server.ENCRYPTION_BEGIN = Reflection.getClassByNameWithoutException(prefix + "PacketLoginOutEncryptionBegin");
                Server.SUCCESS = Reflection.getClassByNameWithoutException(prefix + "PacketLoginOutSuccess");
                Server.SET_COMPRESSION = Reflection.getClassByNameWithoutException(prefix + "PacketLoginOutSetCompression");
            }
        }
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class Play {

        @NoArgsConstructor(access = AccessLevel.PRIVATE)
        public static class Client {

            public static Class<?> FLYING;
            public static Class<?> POSITION;
            public static Class<?> POSITION_LOOK;
            public static Class<?> LOOK;
            public static Class<?> GROUND;
            public static Class<?> CLIENT_COMMAND;
            public static Class<?> TRANSACTION;
            public static Class<?> BLOCK_DIG;
            public static Class<?> ENTITY_ACTION;
            public static Class<?> USE_ENTITY;
            public static Class<?> WINDOW_CLICK;
            public static Class<?> STEER_VEHICLE;
            public static Class<?> CUSTOM_PAYLOAD;
            public static Class<?> ARM_ANIMATION;
            public static Class<?> BLOCK_PLACE;
            public static Class<?> USE_ITEM;
            public static Class<?> ABILITIES;
            public static Class<?> HELD_ITEM_SLOT;
            public static Class<?> CLOSE_WINDOW;
            public static Class<?> TAB_COMPLETE;
            public static Class<?> CHAT;
            public static Class<?> SET_CREATIVE_SLOT;
            public static Class<?> KEEP_ALIVE;
            public static Class<?> SETTINGS;
            public static Class<?> ENCHANT_ITEM;
            public static Class<?> TELEPORT_ACCEPT;
            public static Class<?> TILE_NBT_QUERY;
            public static Class<?> DIFFICULTY_CHANGE;
            public static Class<?> B_EDIT;
            public static Class<?> ENTITY_NBT_QUERY;
            public static Class<?> JIGSAW_GENERATE;
            public static Class<?> DIFFICULTY_LOCK;
            public static Class<?> VEHICLE_MOVE;
            public static Class<?> BOAT_MOVE;
            public static Class<?> PICK_ITEM;
            public static Class<?> AUTO_RECIPE;
            public static Class<?> RECIPE_DISPLAYED;
            public static Class<?> ITEM_NAME;
            public static Class<?> RESOURCE_PACK_STATUS;
            public static Class<?> ADVANCEMENTS;
            public static Class<?> TR_SEL;
            public static Class<?> BEACON;
            public static Class<?> SET_COMMAND_BLOCK;
            public static Class<?> SET_COMMAND_MINECART;
            public static Class<?> SET_JIGSAW;
            public static Class<?> STRUCT;
            public static Class<?> UPDATE_SIGN;
            public static Class<?> SPECTATE;

            /**
             * Initiate all server-bound play packet classes.
             */
            public static void load() {
                String prefix = ServerVersion.getNMSDirectory() + ".";
                String commonPrefix = prefix + "PacketPlayIn";

                FLYING = Reflection.getClassByNameWithoutException(commonPrefix + "Flying");

                try {
                    POSITION = Class.forName(commonPrefix + "Position");
                    POSITION_LOOK = Class.forName(commonPrefix + "PositionLook");
                    LOOK = Class.forName(commonPrefix + "Look");
                } catch (ClassNotFoundException ex) {
                    POSITION = SubclassUtil.getSubClass(FLYING, "PacketPlayInPosition");
                    POSITION_LOOK = SubclassUtil.getSubClass(FLYING, "PacketPlayInPositionLook");
                    LOOK = SubclassUtil.getSubClass(FLYING, "PacketPlayInLook");
                }

                GROUND = FLYING;
                TRANSACTION = Reflection.getClassByNameWithoutException(commonPrefix + "Transaction");

                try {
                    SETTINGS = Class.forName(commonPrefix + "Settings");
                    ENCHANT_ITEM = Class.forName(commonPrefix + "EnchantItem");

                    CLIENT_COMMAND = Class.forName(commonPrefix + "ClientCommand");
                    BLOCK_DIG = Class.forName(commonPrefix + "BlockDig");
                    ENTITY_ACTION = Class.forName(commonPrefix + "EntityAction");
                    USE_ENTITY = Class.forName(commonPrefix + "UseEntity");
                    WINDOW_CLICK = Class.forName(commonPrefix + "WindowClick");
                    STEER_VEHICLE = Class.forName(commonPrefix + "SteerVehicle");
                    CUSTOM_PAYLOAD = Class.forName(commonPrefix + "CustomPayload");
                    ARM_ANIMATION = Class.forName(commonPrefix + "ArmAnimation");
                    ABILITIES = Class.forName(commonPrefix + "Abilities");
                    HELD_ITEM_SLOT = Class.forName(commonPrefix + "HeldItemSlot");
                    CLOSE_WINDOW = Class.forName(commonPrefix + "CloseWindow");
                    TAB_COMPLETE = Class.forName(commonPrefix + "TabComplete");
                    CHAT = Class.forName(commonPrefix + "Chat");
                    SET_CREATIVE_SLOT = Class.forName(commonPrefix + "SetCreativeSlot");
                    KEEP_ALIVE = Class.forName(commonPrefix + "KeepAlive");
                    UPDATE_SIGN = Reflection.getClassByNameWithoutException(commonPrefix + "UpdateSign");

                    TELEPORT_ACCEPT = Reflection.getClassByNameWithoutException(commonPrefix + "TeleportAccept");
                    TILE_NBT_QUERY = Reflection.getClassByNameWithoutException(commonPrefix + "TileNBTQuery");
                    DIFFICULTY_CHANGE = Reflection.getClassByNameWithoutException(commonPrefix + "DifficultyChange");
                    B_EDIT = Reflection.getClassByNameWithoutException(commonPrefix + "BEdit");
                    ENTITY_NBT_QUERY = Reflection.getClassByNameWithoutException(commonPrefix + "EntityNBTQuery");
                    JIGSAW_GENERATE = Reflection.getClassByNameWithoutException(commonPrefix + "JigsawGenerate");
                    DIFFICULTY_LOCK = Reflection.getClassByNameWithoutException(commonPrefix + "DifficultyLock");
                    VEHICLE_MOVE = Reflection.getClassByNameWithoutException(commonPrefix + "VehicleMove");
                    BOAT_MOVE = Reflection.getClassByNameWithoutException(commonPrefix + "BoatMove");
                    PICK_ITEM = Reflection.getClassByNameWithoutException(commonPrefix + "PickItem");
                    AUTO_RECIPE = Reflection.getClassByNameWithoutException(commonPrefix + "AutoRecipe");
                    RECIPE_DISPLAYED = Reflection.getClassByNameWithoutException(commonPrefix + "RecipeDisplayed");
                    ITEM_NAME = Reflection.getClassByNameWithoutException(commonPrefix + "ItemName");

                    // 1.8+
                    RESOURCE_PACK_STATUS = Reflection.getClassByNameWithoutException(commonPrefix + "ResourcePackStatus");

                    ADVANCEMENTS = Reflection.getClassByNameWithoutException(commonPrefix + "Advancements");
                    TR_SEL = Reflection.getClassByNameWithoutException(commonPrefix + "TrSel");
                    BEACON = Reflection.getClassByNameWithoutException(commonPrefix + "Beacon");
                    SET_COMMAND_BLOCK = Reflection.getClassByNameWithoutException(commonPrefix + "SetCommandBlock");
                    SET_COMMAND_MINECART = Reflection.getClassByNameWithoutException(commonPrefix + "SetCommandMinecart");
                    SET_JIGSAW = Reflection.getClassByNameWithoutException(commonPrefix + "SetJigsaw");
                    STRUCT = Reflection.getClassByNameWithoutException(commonPrefix + "Struct");
                    SPECTATE = Reflection.getClassByNameWithoutException(commonPrefix + "Spectate");
                } catch (Exception ex) {
                    ex.printStackTrace();
                }

                try {
                    BLOCK_PLACE = Class.forName(commonPrefix + "BlockPlace");
                    USE_ITEM = Reflection.getClassByNameWithoutException(commonPrefix + "UseItem");
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }

        @NoArgsConstructor(access = AccessLevel.PRIVATE)
        public static class Server {

            public static Class<?> SPAWN_ENTITY;
            public static Class<?> SPAWN_ENTITY_EXPERIENCE_ORB;
            public static Class<?> SPAWN_ENTITY_WEATHER;
            public static Class<?> SPAWN_ENTITY_LIVING;
            public static Class<?> SPAWN_ENTITY_PAINTING;
            public static Class<?> SPAWN_ENTITY_SPAWN;
            public static Class<?> ANIMATION;
            public static Class<?> STATISTIC;
            public static Class<?> BLOCK_BREAK;
            public static Class<?> BLOCK_BREAK_ANIMATION;
            public static Class<?> TILE_ENTITY_DATA;
            public static Class<?> BLOCK_ACTION;
            public static Class<?> BLOCK_CHANGE;
            public static Class<?> BOSS;
            public static Class<?> SERVER_DIFFICULTY;
            public static Class<?> CHAT;
            public static Class<?> MULTI_BLOCK_CHANGE;
            public static Class<?> TAB_COMPLETE;
            public static Class<?> COMMANDS;
            public static Class<?> TRANSACTION;
            public static Class<?> CLOSE_WINDOW;
            public static Class<?> WINDOW_ITEMS;
            public static Class<?> WINDOW_DATA;
            public static Class<?> SET_SLOT;
            public static Class<?> SET_COOLDOWN;
            public static Class<?> CUSTOM_PAYLOAD;
            public static Class<?> CUSTOM_SOUND_EFFECT;
            public static Class<?> KICK_DISCONNECT;
            public static Class<?> ENTITY_STATUS;
            public static Class<?> EXPLOSION;
            public static Class<?> UNLOAD_CHUNK;
            public static Class<?> GAME_STATE_CHANGE;
            public static Class<?> OPEN_WINDOW_HORSE;
            public static Class<?> KEEP_ALIVE;
            public static Class<?> MAP_CHUNK;
            public static Class<?> WORLD_EVENT;
            public static Class<?> WORLD_PARTICLES;
            public static Class<?> LIGHT_UPDATE;
            public static Class<?> LOGIN;
            public static Class<?> MAP;
            public static Class<?> OPEN_WINDOW_MERCHANT;
            public static Class<?> REL_ENTITY_MOVE;
            public static Class<?> REL_ENTITY_MOVE_LOOK;
            public static Class<?> ENTITY_LOOK;
            public static Class<?> ENTITY;
            public static Class<?> VEHICLE_MOVE;
            public static Class<?> OPEN_BOOK;
            public static Class<?> OPEN_WINDOW;
            public static Class<?> OPEN_SIGN_EDITOR;
            public static Class<?> AUTO_RECIPE;
            public static Class<?> ABILITIES;
            public static Class<?> COMBAT_EVENT;
            public static Class<?> PLAYER_INFO;
            public static Class<?> LOOK_AT;
            public static Class<?> POSITION;
            public static Class<?> RECIPES;
            public static Class<?> ENTITY_DESTROY;
            public static Class<?> REMOVE_ENTITY_EFFECT;
            public static Class<?> RESOURCE_PACK_SEND;
            public static Class<?> RESPAWN;
            public static Class<?> ENTITY_HEAD_ROTATION;
            public static Class<?> SELECT_ADVANCEMENT_TAB;
            public static Class<?> WORLD_BORDER;
            public static Class<?> CAMERA;
            public static Class<?> HELD_ITEM_SLOT;
            public static Class<?> VIEW_CENTRE;
            public static Class<?> VIEW_DISTANCE;
            public static Class<?> SCOREBOARD_DISPLAY_OBJECTIVE;
            public static Class<?> ENTITY_METADATA;
            public static Class<?> ATTACH_ENTITY;
            public static Class<?> ENTITY_VELOCITY;
            public static Class<?> ENTITY_EQUIPMENT;
            public static Class<?> EXPERIENCE;
            public static Class<?> UPDATE_HEALTH;
            public static Class<?> SCOREBOARD_OBJECTIVE;
            public static Class<?> MOUNT;
            public static Class<?> SCOREBOARD_TEAM;
            public static Class<?> SCOREBOARD_SCORE;
            public static Class<?> SPAWN_POSITION;
            public static Class<?> UPDATE_TIME;
            public static Class<?> TITLE;
            public static Class<?> ENTITY_SOUND;
            public static Class<?> NAMED_SOUND_EFFECT;
            public static Class<?> STOP_SOUND;
            public static Class<?> PLAYER_LIST_HEADER_FOOTER;
            public static Class<?> NBT_QUERY;
            public static Class<?> COLLECT;
            public static Class<?> ENTITY_TELEPORT;
            public static Class<?> ADVANCEMENTS;
            public static Class<?> UPDATE_ATTRIBUTES;
            public static Class<?> ENTITY_EFFECT;
            public static Class<?> RECIPE_UPDATE;
            public static Class<?> TAGS;
            public static Class<?> MAP_CHUNK_BULK;
            public static Class<?> NAMED_ENTITY_SPAWN;

            /**
             * Initiate all client-bound packet classes.
             */
            public static void load() {
                String prefix = ServerVersion.getNMSDirectory() + ".";
                String commonPrefix = prefix + "PacketPlayOut";

                SPAWN_ENTITY = Reflection.getClassByNameWithoutException(commonPrefix + "SpawnEntity");
                SPAWN_ENTITY_EXPERIENCE_ORB = Reflection.getClassByNameWithoutException(commonPrefix + "SpawnEntityExperienceOrb");
                SPAWN_ENTITY_WEATHER = Reflection.getClassByNameWithoutException(commonPrefix + "SpawnEntityWeather");
                SPAWN_ENTITY_LIVING = Reflection.getClassByNameWithoutException(commonPrefix + "SpawnEntityLiving");
                SPAWN_ENTITY_PAINTING = Reflection.getClassByNameWithoutException(commonPrefix + "SpawnEntityPainting");
                SPAWN_ENTITY_SPAWN = Reflection.getClassByNameWithoutException(commonPrefix + "SpawnEntitySpawn");
                ANIMATION = Reflection.getClassByNameWithoutException(commonPrefix + "Animation");
                STATISTIC = Reflection.getClassByNameWithoutException(commonPrefix + "Statistic");
                BLOCK_BREAK = Reflection.getClassByNameWithoutException(commonPrefix + "BlockBreak");
                BLOCK_BREAK_ANIMATION = Reflection.getClassByNameWithoutException(commonPrefix + "BlockBreakAnimation");
                TILE_ENTITY_DATA = Reflection.getClassByNameWithoutException(commonPrefix + "TileEntityData");
                BLOCK_ACTION = Reflection.getClassByNameWithoutException(commonPrefix + "BlockAction");
                BLOCK_CHANGE = Reflection.getClassByNameWithoutException(commonPrefix + "BlockChange");
                BOSS = Reflection.getClassByNameWithoutException(commonPrefix + "Boss");
                SERVER_DIFFICULTY = Reflection.getClassByNameWithoutException(commonPrefix + "ServerDifficulty");
                CHAT = Reflection.getClassByNameWithoutException(commonPrefix + "Chat");
                MULTI_BLOCK_CHANGE = Reflection.getClassByNameWithoutException(commonPrefix + "MultiBlockChange");
                TAB_COMPLETE = Reflection.getClassByNameWithoutException(commonPrefix + "TabComplete");
                COMMANDS = Reflection.getClassByNameWithoutException(commonPrefix + "Commands");
                TRANSACTION = Reflection.getClassByNameWithoutException(commonPrefix + "Transaction");
                CLOSE_WINDOW = Reflection.getClassByNameWithoutException(commonPrefix + "CloseWindow");
                WINDOW_ITEMS = Reflection.getClassByNameWithoutException(commonPrefix + "WindowItems");
                WINDOW_DATA = Reflection.getClassByNameWithoutException(commonPrefix + "WindowData");
                SET_SLOT = Reflection.getClassByNameWithoutException(commonPrefix + "SetSlot");
                SET_COOLDOWN = Reflection.getClassByNameWithoutException(commonPrefix + "SetCooldown");
                CUSTOM_PAYLOAD = Reflection.getClassByNameWithoutException(commonPrefix + "CustomPayload");
                CUSTOM_SOUND_EFFECT = Reflection.getClassByNameWithoutException(commonPrefix + "CustomSoundEffect");
                KICK_DISCONNECT = Reflection.getClassByNameWithoutException(commonPrefix + "KickDisconnect");
                ENTITY_STATUS = Reflection.getClassByNameWithoutException(commonPrefix + "EntityStatus");
                EXPLOSION = Reflection.getClassByNameWithoutException(commonPrefix + "Explosion");
                UNLOAD_CHUNK = Reflection.getClassByNameWithoutException(commonPrefix + "UnloadChunk");
                GAME_STATE_CHANGE = Reflection.getClassByNameWithoutException(commonPrefix + "GameStateChange");
                OPEN_WINDOW_HORSE = Reflection.getClassByNameWithoutException(commonPrefix + "OpenWindowHorse");
                KEEP_ALIVE = Reflection.getClassByNameWithoutException(commonPrefix + "KeepAlive");
                MAP_CHUNK = Reflection.getClassByNameWithoutException(commonPrefix + "MapChunk");
                WORLD_EVENT = Reflection.getClassByNameWithoutException(commonPrefix + "WorldEvent");
                WORLD_PARTICLES = Reflection.getClassByNameWithoutException(commonPrefix + "WorldParticles");
                LIGHT_UPDATE = Reflection.getClassByNameWithoutException(commonPrefix + "LightUpdate");
                LOGIN = Reflection.getClassByNameWithoutException(commonPrefix + "Login");
                MAP = Reflection.getClassByNameWithoutException(commonPrefix + "Map");
                OPEN_WINDOW_MERCHANT = Reflection.getClassByNameWithoutException(commonPrefix + "OpenWindowMerchant");
                ENTITY = Reflection.getClassByNameWithoutException(commonPrefix + "Entity");
                REL_ENTITY_MOVE = SubclassUtil.getSubClass(ENTITY, "PacketPlayOutRelEntityMove");
                REL_ENTITY_MOVE_LOOK = SubclassUtil.getSubClass(ENTITY, "PacketPlayOutRelEntityMoveLook");
                ENTITY_LOOK = SubclassUtil.getSubClass(ENTITY, "PacketPlayOutEntityLook");

                if (REL_ENTITY_MOVE == null) {
                    // is not a subclass and should be accessed normally
                    REL_ENTITY_MOVE = Reflection.getClassByNameWithoutException(commonPrefix + "RelEntityMove");
                    REL_ENTITY_MOVE_LOOK = Reflection.getClassByNameWithoutException(commonPrefix + "RelEntityMoveLook");
                    ENTITY_LOOK = Reflection.getClassByNameWithoutException(commonPrefix + "RelEntityLook");
                }

                VEHICLE_MOVE = Reflection.getClassByNameWithoutException(commonPrefix + "VehicleMove");
                OPEN_BOOK = Reflection.getClassByNameWithoutException(commonPrefix + "OpenBook");
                OPEN_WINDOW = Reflection.getClassByNameWithoutException(commonPrefix + "OpenWindow");
                OPEN_SIGN_EDITOR = Reflection.getClassByNameWithoutException(commonPrefix + "OpenSignEditor");
                AUTO_RECIPE = Reflection.getClassByNameWithoutException(commonPrefix + "AutoRecipe");
                ABILITIES = Reflection.getClassByNameWithoutException(commonPrefix + "Abilities");
                COMBAT_EVENT = Reflection.getClassByNameWithoutException(commonPrefix + "CombatEvent");
                PLAYER_INFO = Reflection.getClassByNameWithoutException(commonPrefix + "PlayerInfo");
                LOOK_AT = Reflection.getClassByNameWithoutException(commonPrefix + "LookAt");
                POSITION = Reflection.getClassByNameWithoutException(commonPrefix + "Position");
                RECIPES = Reflection.getClassByNameWithoutException(commonPrefix + "Recipes");
                ENTITY_DESTROY = Reflection.getClassByNameWithoutException(commonPrefix + "EntityDestroy");
                REMOVE_ENTITY_EFFECT = Reflection.getClassByNameWithoutException(commonPrefix + "RemoveEntityEffect");
                RESOURCE_PACK_SEND = Reflection.getClassByNameWithoutException(commonPrefix + "ResourcePackSend");
                RESPAWN = Reflection.getClassByNameWithoutException(commonPrefix + "Respawn");
                ENTITY_HEAD_ROTATION = Reflection.getClassByNameWithoutException(commonPrefix + "EntityHeadRotation");
                SELECT_ADVANCEMENT_TAB = Reflection.getClassByNameWithoutException(commonPrefix + "SelectAdvancementTab");
                WORLD_BORDER = Reflection.getClassByNameWithoutException(commonPrefix + "WorldBorder");
                CAMERA = Reflection.getClassByNameWithoutException(commonPrefix + "Camera");
                HELD_ITEM_SLOT = Reflection.getClassByNameWithoutException(commonPrefix + "HeldItemSlot");
                VIEW_CENTRE = Reflection.getClassByNameWithoutException(commonPrefix + "ViewCentre");
                VIEW_DISTANCE = Reflection.getClassByNameWithoutException(commonPrefix + "ViewDistance");
                SCOREBOARD_DISPLAY_OBJECTIVE = Reflection.getClassByNameWithoutException(commonPrefix + "ScoreboardDisplayObjective");
                ENTITY_METADATA = Reflection.getClassByNameWithoutException(commonPrefix + "EntityMetadata");
                ATTACH_ENTITY = Reflection.getClassByNameWithoutException(commonPrefix + "AttachEntity");
                ENTITY_VELOCITY = Reflection.getClassByNameWithoutException(commonPrefix + "EntityVelocity");
                ENTITY_EQUIPMENT = Reflection.getClassByNameWithoutException(commonPrefix + "EntityEquipment");
                EXPERIENCE = Reflection.getClassByNameWithoutException(commonPrefix + "Experience");
                UPDATE_HEALTH = Reflection.getClassByNameWithoutException(commonPrefix + "UpdateHealth");
                SCOREBOARD_OBJECTIVE = Reflection.getClassByNameWithoutException(commonPrefix + "ScoreboardObjective");
                MOUNT = Reflection.getClassByNameWithoutException(commonPrefix + "Mount");
                SCOREBOARD_TEAM = Reflection.getClassByNameWithoutException(commonPrefix + "ScoreboardTeam");
                SCOREBOARD_SCORE = Reflection.getClassByNameWithoutException(commonPrefix + "ScoreboardScore");
                SPAWN_POSITION = Reflection.getClassByNameWithoutException(commonPrefix + "SpawnPosition");
                UPDATE_TIME = Reflection.getClassByNameWithoutException(commonPrefix + "UpdateTime");
                TITLE = Reflection.getClassByNameWithoutException(commonPrefix + "Title");
                ENTITY_SOUND = Reflection.getClassByNameWithoutException(commonPrefix + "EntitySound");
                NAMED_SOUND_EFFECT = Reflection.getClassByNameWithoutException(commonPrefix + "NamedSoundEffect");
                STOP_SOUND = Reflection.getClassByNameWithoutException(commonPrefix + "StopSound");
                PLAYER_LIST_HEADER_FOOTER = Reflection.getClassByNameWithoutException(commonPrefix + "PlayerListHeaderFooter");
                NBT_QUERY = Reflection.getClassByNameWithoutException(commonPrefix + "NBTQuery");
                COLLECT = Reflection.getClassByNameWithoutException(commonPrefix + "Collect");
                ENTITY_TELEPORT = Reflection.getClassByNameWithoutException(commonPrefix + "EntityTeleport");
                ADVANCEMENTS = Reflection.getClassByNameWithoutException(commonPrefix + "Advancements");
                UPDATE_ATTRIBUTES = Reflection.getClassByNameWithoutException(commonPrefix + "UpdateAttributes");
                ENTITY_EFFECT = Reflection.getClassByNameWithoutException(commonPrefix + "EntityEffect");
                RECIPE_UPDATE = Reflection.getClassByNameWithoutException(commonPrefix + "RecipeUpdate");
                TAGS = Reflection.getClassByNameWithoutException(commonPrefix + "Tags");
                MAP_CHUNK_BULK = Reflection.getClassByNameWithoutException(commonPrefix + "MapChunkBulk");
                NAMED_ENTITY_SPAWN = Reflection.getClassByNameWithoutException(commonPrefix + "NamedEntitySpawn");
            }
        }
    }
}
