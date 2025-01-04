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
package net.foulest.packetevents.packettype;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Packet Type IDs.
 * This is the Packet ID system, it is recommended to use this over packet comparisons by packet name.
 * This is also faster than comparing packet names.
 *
 * @author retrooper
 * @since 1.6.8
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class PacketType {

    /**
     * If a Packet Type could not be resolved, it will be set to the current value of this constant.
     * This value may change over the versions, so it is important to use the variable and not hard code its value.
     */
    public static final byte INVALID = -128;
    @Getter
    private static final Map<Class<?>, Byte> packetIDMap = new IdentityHashMap<>();

    private static void insertPacketID(Class<?> cls, byte packetID) {
        if (cls != null) {
            packetIDMap.put(cls, packetID);
        }
    }

    public static void load() {
        Status.Client.load();
        Status.Server.load();

        Handshaking.Client.load();

        Login.Client.load();
        Login.Server.load();

        Play.Client.load();
        Play.Server.load();
    }

    /**
     * Status Packet IDs.
     *
     * @author retrooper
     * @see <a href="https://wiki.vg/Protocol#Status">https://wiki.vg/Protocol#Status</a>
     * @since 1.7
     */
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    static final class Status {

        /**
         * Server-bound (client-sided) Status Packet IDs.
         *
         * @author retrooper
         * @see <a href="https://wiki.vg/Protocol#Serverbound_2">https://wiki.vg/Protocol#Serverbound_2</a>
         * @since 1.8
         */
        @NoArgsConstructor(access = AccessLevel.PRIVATE)
        static final class Client {

            static final byte START = -127;
            static final byte PING = -126;

            private static void load() {
                insertPacketID(PacketTypeClasses.Status.Client.START, START);
                insertPacketID(PacketTypeClasses.Status.Client.PING, PING);
            }
        }

        /**
         * Client-bound (server-sided) Status Packet IDs.
         *
         * @author retrooper
         * @see <a href="https://wiki.vg/Protocol#Clientbound_2">https://wiki.vg/Protocol#Clientbound_2</a>
         * @since 1.8
         */
        @NoArgsConstructor(access = AccessLevel.PRIVATE)
        static final class Server {

            static final byte PONG = -125;
            static final byte SERVER_INFO = -124;

            private static void load() {
                insertPacketID(PacketTypeClasses.Status.Server.PONG, PONG);
                insertPacketID(PacketTypeClasses.Status.Server.SERVER_INFO, SERVER_INFO);
            }
        }
    }

    /**
     * Handshaking Packet IDs.
     *
     * @author retrooper
     * @see <a href="https://wiki.vg/Protocol#Handshaking">https://wiki.vg/Protocol#Handshaking</a>
     * @since 1.8
     */
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static final class Handshaking {

        /**
         * Server-bound (client-sided) Handshaking Packet IDs.
         *
         * @author retrooper
         * @since 1.8
         */
        @NoArgsConstructor(access = AccessLevel.PRIVATE)
        public static final class Client {

            public static final byte SET_PROTOCOL = -123;

            private static void load() {
                insertPacketID(PacketTypeClasses.Handshaking.Client.SET_PROTOCOL, SET_PROTOCOL);
            }
        }
    }

    /**
     * Login Packet IDs.
     *
     * @author retrooper
     * @see <a href="https://wiki.vg/Protocol#Login">https://wiki.vg/Protocol#Login</a>
     * @since 1.7
     */
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static final class Login {

        /**
         * Server-bound (client-sided) Login Packet IDs.
         *
         * @author retrooper
         * @see <a href="https://wiki.vg/Protocol#Serverbound_3">https://wiki.vg/Protocol#Serverbound_3</a>
         * @since 1.8
         */
        @NoArgsConstructor(access = AccessLevel.PRIVATE)
        public static final class Client {

            public static final byte START = -121;
            public static final byte ENCRYPTION_BEGIN = -120;

            private static void load() {
                insertPacketID(PacketTypeClasses.Login.Client.START, START);
                insertPacketID(PacketTypeClasses.Login.Client.ENCRYPTION_BEGIN, ENCRYPTION_BEGIN);
            }
        }

        /**
         * Client-bound (server-sided) Login Packet IDs.
         *
         * @author retrooper
         * @see <a href="https://wiki.vg/Protocol#Clientbound_3">https://wiki.vg/Protocol#Clientbound_3</a>
         * @since 1.8
         */
        @NoArgsConstructor(access = AccessLevel.PRIVATE)
        static final class Server {

            static final byte DISCONNECT = -118;
            static final byte ENCRYPTION_BEGIN = -117;
            static final byte SUCCESS = -116;
            static final byte SET_COMPRESSION = -115;

            private static void load() {
                insertPacketID(PacketTypeClasses.Login.Server.DISCONNECT, DISCONNECT);
                insertPacketID(PacketTypeClasses.Login.Server.ENCRYPTION_BEGIN, ENCRYPTION_BEGIN);
                insertPacketID(PacketTypeClasses.Login.Server.SUCCESS, SUCCESS);
                insertPacketID(PacketTypeClasses.Login.Server.SET_COMPRESSION, SET_COMPRESSION);
            }
        }
    }

    /**
     * Play Packet IDs.
     *
     * @author retrooper
     * @see <a href="https://wiki.vg/Protocol#Play">https://wiki.vg/Protocol#Play</a>
     * @since 1.8
     */
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static final class Play {

        /**
         * Server-bound (client-sided) Play Packet IDs.
         *
         * @author retrooper
         * @see <a href="https://wiki.vg/Protocol#Serverbound_4">https://wiki.vg/Protocol#Serverbound_4</a>
         * @since 1.8
         */
        @NoArgsConstructor(access = AccessLevel.PRIVATE)
        public static final class Client {

            static final byte TELEPORT_ACCEPT = -114;
            static final byte TILE_NBT_QUERY = -113;
            public static final byte DIFFICULTY_CHANGE = -112;
            public static final byte CHAT = -111;
            public static final byte CLIENT_COMMAND = -110;
            public static final byte SETTINGS = -109;
            public static final byte TAB_COMPLETE = -108;
            public static final byte TRANSACTION = -107;
            public static final byte ENCHANT_ITEM = -106;
            public static final byte WINDOW_CLICK = -105;
            public static final byte CLOSE_WINDOW = -104;
            public static final byte CUSTOM_PAYLOAD = -103;
            static final byte B_EDIT = -102;
            static final byte ENTITY_NBT_QUERY = -101;
            public static final byte USE_ENTITY = -100;
            static final byte JIGSAW_GENERATE = -99;
            public static final byte KEEP_ALIVE = -98;
            static final byte DIFFICULTY_LOCK = -97;
            public static final byte POSITION = -96;
            public static final byte POSITION_LOOK = -95;
            public static final byte LOOK = -94;
            public static final byte FLYING = -93;
            static final byte VEHICLE_MOVE = -92;
            static final byte BOAT_MOVE = -91;
            static final byte PICK_ITEM = -90;
            static final byte AUTO_RECIPE = -89;
            public static final byte ABILITIES = -88;
            public static final byte BLOCK_DIG = -87;
            public static final byte ENTITY_ACTION = -86;
            public static final byte STEER_VEHICLE = -85;
            static final byte RECIPE_DISPLAYED = -84;
            static final byte ITEM_NAME = -83;
            public static final byte RESOURCE_PACK_STATUS = -82;
            static final byte ADVANCEMENTS = -81;
            static final byte TR_SEL = -80;
            static final byte BEACON = -79;
            public static final byte HELD_ITEM_SLOT = -78;
            static final byte SET_COMMAND_BLOCK = -77;
            static final byte SET_COMMAND_MINECART = -76;
            public static final byte SET_CREATIVE_SLOT = -75;
            static final byte SET_JIGSAW = -74;
            static final byte STRUCT = -73;
            public static final byte UPDATE_SIGN = -72;
            public static final byte ARM_ANIMATION = -71;
            public static final byte SPECTATE = -70;
            static final byte USE_ITEM = -69;
            public static final byte BLOCK_PLACE = -68;

            private static void load() {
                insertPacketID(PacketTypeClasses.Play.Client.TELEPORT_ACCEPT, TELEPORT_ACCEPT);
                insertPacketID(PacketTypeClasses.Play.Client.TILE_NBT_QUERY, TILE_NBT_QUERY);
                insertPacketID(PacketTypeClasses.Play.Client.DIFFICULTY_CHANGE, DIFFICULTY_CHANGE);
                insertPacketID(PacketTypeClasses.Play.Client.CHAT, CHAT);
                insertPacketID(PacketTypeClasses.Play.Client.CLIENT_COMMAND, CLIENT_COMMAND);
                insertPacketID(PacketTypeClasses.Play.Client.SETTINGS, SETTINGS);
                insertPacketID(PacketTypeClasses.Play.Client.TAB_COMPLETE, TAB_COMPLETE);
                insertPacketID(PacketTypeClasses.Play.Client.TRANSACTION, TRANSACTION);
                insertPacketID(PacketTypeClasses.Play.Client.ENCHANT_ITEM, ENCHANT_ITEM);
                insertPacketID(PacketTypeClasses.Play.Client.WINDOW_CLICK, WINDOW_CLICK);
                insertPacketID(PacketTypeClasses.Play.Client.CLOSE_WINDOW, CLOSE_WINDOW);
                insertPacketID(PacketTypeClasses.Play.Client.CUSTOM_PAYLOAD, CUSTOM_PAYLOAD);
                insertPacketID(PacketTypeClasses.Play.Client.B_EDIT, B_EDIT);
                insertPacketID(PacketTypeClasses.Play.Client.ENTITY_NBT_QUERY, ENTITY_NBT_QUERY);
                insertPacketID(PacketTypeClasses.Play.Client.USE_ENTITY, USE_ENTITY);
                insertPacketID(PacketTypeClasses.Play.Client.JIGSAW_GENERATE, JIGSAW_GENERATE);
                insertPacketID(PacketTypeClasses.Play.Client.KEEP_ALIVE, KEEP_ALIVE);
                insertPacketID(PacketTypeClasses.Play.Client.DIFFICULTY_LOCK, DIFFICULTY_LOCK);
                insertPacketID(PacketTypeClasses.Play.Client.POSITION, POSITION);
                insertPacketID(PacketTypeClasses.Play.Client.POSITION_LOOK, POSITION_LOOK);
                insertPacketID(PacketTypeClasses.Play.Client.LOOK, LOOK);
                insertPacketID(PacketTypeClasses.Play.Client.GROUND, FLYING);
                insertPacketID(PacketTypeClasses.Play.Client.VEHICLE_MOVE, VEHICLE_MOVE);
                insertPacketID(PacketTypeClasses.Play.Client.BOAT_MOVE, BOAT_MOVE);
                insertPacketID(PacketTypeClasses.Play.Client.PICK_ITEM, PICK_ITEM);
                insertPacketID(PacketTypeClasses.Play.Client.AUTO_RECIPE, AUTO_RECIPE);
                insertPacketID(PacketTypeClasses.Play.Client.ABILITIES, ABILITIES);
                insertPacketID(PacketTypeClasses.Play.Client.BLOCK_DIG, BLOCK_DIG);
                insertPacketID(PacketTypeClasses.Play.Client.ENTITY_ACTION, ENTITY_ACTION);
                insertPacketID(PacketTypeClasses.Play.Client.STEER_VEHICLE, STEER_VEHICLE);
                insertPacketID(PacketTypeClasses.Play.Client.RECIPE_DISPLAYED, RECIPE_DISPLAYED);
                insertPacketID(PacketTypeClasses.Play.Client.ITEM_NAME, ITEM_NAME);
                insertPacketID(PacketTypeClasses.Play.Client.RESOURCE_PACK_STATUS, RESOURCE_PACK_STATUS);
                insertPacketID(PacketTypeClasses.Play.Client.ADVANCEMENTS, ADVANCEMENTS);
                insertPacketID(PacketTypeClasses.Play.Client.TR_SEL, TR_SEL);
                insertPacketID(PacketTypeClasses.Play.Client.BEACON, BEACON);
                insertPacketID(PacketTypeClasses.Play.Client.HELD_ITEM_SLOT, HELD_ITEM_SLOT);
                insertPacketID(PacketTypeClasses.Play.Client.SET_COMMAND_BLOCK, SET_COMMAND_BLOCK);
                insertPacketID(PacketTypeClasses.Play.Client.SET_COMMAND_MINECART, SET_COMMAND_MINECART);
                insertPacketID(PacketTypeClasses.Play.Client.SET_CREATIVE_SLOT, SET_CREATIVE_SLOT);
                insertPacketID(PacketTypeClasses.Play.Client.SET_JIGSAW, SET_JIGSAW);
                insertPacketID(PacketTypeClasses.Play.Client.STRUCT, STRUCT);
                insertPacketID(PacketTypeClasses.Play.Client.UPDATE_SIGN, UPDATE_SIGN);
                insertPacketID(PacketTypeClasses.Play.Client.ARM_ANIMATION, ARM_ANIMATION);
                insertPacketID(PacketTypeClasses.Play.Client.SPECTATE, SPECTATE);
                insertPacketID(PacketTypeClasses.Play.Client.USE_ITEM, USE_ITEM);
                insertPacketID(PacketTypeClasses.Play.Client.BLOCK_PLACE, BLOCK_PLACE);
            }

            /**
             * Server-bound Play Packet Type utility.
             * Save a few lines of code by using this.
             *
             * @author retrooper
             * @since 1.8
             */
            @NoArgsConstructor(access = AccessLevel.PRIVATE)
            public static final class Util {

                /**
                 * Is the play packet a PacketPlayInFlying, PacketPlayInPosition, PacketPlayInPositionLook
                 * or a PacketPlayInLook packet?
                 *
                 * @param packetID Play Packet ID.
                 * @return Is the Packet ID an instance of the PacketPlayInFlying packet?
                 */
                public static boolean isInstanceOfFlying(byte packetID) {
                    return packetID == FLYING
                            || packetID == POSITION
                            || packetID == POSITION_LOOK
                            || packetID == LOOK;
                }
            }
        }

        /**
         * Client-bound (server-sided) Play Packet IDs.
         *
         * @author retrooper
         * @see <a href="https://wiki.vg/Protocol#Clientbound_4">https://wiki.vg/Protocol#Clientbound_4</a>
         * @since 1.8
         */
        @NoArgsConstructor(access = AccessLevel.PRIVATE)
        public static final class Server {

            static final byte SPAWN_ENTITY = -67;
            static final byte SPAWN_ENTITY_EXPERIENCE_ORB = -66;
            static final byte SPAWN_ENTITY_WEATHER = -65;
            public static final byte SPAWN_ENTITY_LIVING = -64;
            static final byte SPAWN_ENTITY_PAINTING = -63;
            static final byte SPAWN_ENTITY_SPAWN = -62;
            public static final byte ANIMATION = -61;
            static final byte STATISTIC = -60;
            static final byte BLOCK_BREAK = -59;
            static final byte BLOCK_BREAK_ANIMATION = -58;
            static final byte TILE_ENTITY_DATA = -57;
            static final byte BLOCK_ACTION = -56;
            public static final byte BLOCK_CHANGE = -55;
            static final byte BOSS = -54;
            static final byte SERVER_DIFFICULTY = -53;
            static final byte CHAT = -52;
            static final byte MULTI_BLOCK_CHANGE = -51;
            static final byte TAB_COMPLETE = -50;
            static final byte COMMANDS = -49;
            public static final byte TRANSACTION = -48;
            public static final byte CLOSE_WINDOW = -47;
            static final byte WINDOW_ITEMS = -46;
            static final byte WINDOW_DATA = -45;
            static final byte SET_SLOT = -44;
            static final byte SET_COOLDOWN = -43;
            static final byte CUSTOM_PAYLOAD = -42;
            static final byte CUSTOM_SOUND_EFFECT = -41;
            static final byte KICK_DISCONNECT = -40;
            static final byte ENTITY_STATUS = -39;
            static final byte EXPLOSION = -38;
            static final byte UNLOAD_CHUNK = -37;
            static final byte GAME_STATE_CHANGE = -36;
            static final byte OPEN_WINDOW_HORSE = -35;
            public static final byte KEEP_ALIVE = -34;
            static final byte MAP_CHUNK = -33;
            static final byte WORLD_EVENT = -32;
            static final byte WORLD_PARTICLES = -31;
            static final byte LIGHT_UPDATE = -30;
            static final byte LOGIN = -29;
            static final byte MAP = -28;
            static final byte OPEN_WINDOW_MERCHANT = -27;
            public static final byte REL_ENTITY_MOVE = -26;
            public static final byte REL_ENTITY_MOVE_LOOK = -25;
            static final byte ENTITY_LOOK = -24;
            static final byte ENTITY = -23;
            static final byte VEHICLE_MOVE = -22;
            static final byte OPEN_BOOK = -21;
            public static final byte OPEN_WINDOW = -20;
            public static final byte OPEN_SIGN_EDITOR = -19;
            static final byte AUTO_RECIPE = -18;
            public static final byte ABILITIES = -17;
            static final byte COMBAT_EVENT = -16;
            static final byte PLAYER_INFO = -15;
            static final byte LOOK_AT = -14;
            public static final byte POSITION = -13;
            static final byte RECIPES = -12;
            public static final byte ENTITY_DESTROY = -11;
            static final byte REMOVE_ENTITY_EFFECT = -10;
            public static final byte RESOURCE_PACK_SEND = -9;
            public static final byte RESPAWN = -8;
            static final byte ENTITY_HEAD_ROTATION = -7;
            static final byte SELECT_ADVANCEMENT_TAB = -6;
            static final byte WORLD_BORDER = -5;
            public static final byte CAMERA = -4;
            static final byte HELD_ITEM_SLOT = -3;
            static final byte VIEW_CENTRE = -2;
            static final byte VIEW_DISTANCE = -1;
            static final byte SCOREBOARD_DISPLAY_OBJECTIVE = 0;
            static final byte ENTITY_METADATA = 1;
            public static final byte ATTACH_ENTITY = 2;
            public static final byte ENTITY_VELOCITY = 3;
            static final byte ENTITY_EQUIPMENT = 4;
            static final byte EXPERIENCE = 5;
            static final byte UPDATE_HEALTH = 6;
            static final byte SCOREBOARD_OBJECTIVE = 7;
            static final byte MOUNT = 8;
            static final byte SCOREBOARD_TEAM = 9;
            static final byte SCOREBOARD_SCORE = 10;
            static final byte SPAWN_POSITION = 11;
            static final byte UPDATE_TIME = 12;
            static final byte TITLE = 13;
            static final byte ENTITY_SOUND = 14;
            static final byte NAMED_SOUND_EFFECT = 15;
            static final byte STOP_SOUND = 16;
            static final byte PLAYER_LIST_HEADER_FOOTER = 17;
            static final byte NBT_QUERY = 18;
            static final byte COLLECT = 19;
            public static final byte ENTITY_TELEPORT = 20;
            static final byte ADVANCEMENTS = 21;
            static final byte UPDATE_ATTRIBUTES = 22;
            static final byte ENTITY_EFFECT = 23;
            static final byte RECIPE_UPDATE = 24;
            static final byte TAGS = 25;
            static final byte MAP_CHUNK_BULK = 26;
            public static final byte NAMED_ENTITY_SPAWN = 27;

            private static void load() {
                insertPacketID(PacketTypeClasses.Play.Server.SPAWN_ENTITY, SPAWN_ENTITY);
                insertPacketID(PacketTypeClasses.Play.Server.SPAWN_ENTITY_EXPERIENCE_ORB, SPAWN_ENTITY_EXPERIENCE_ORB);
                insertPacketID(PacketTypeClasses.Play.Server.SPAWN_ENTITY_WEATHER, SPAWN_ENTITY_WEATHER);
                insertPacketID(PacketTypeClasses.Play.Server.SPAWN_ENTITY_LIVING, SPAWN_ENTITY_LIVING);
                insertPacketID(PacketTypeClasses.Play.Server.SPAWN_ENTITY_PAINTING, SPAWN_ENTITY_PAINTING);
                insertPacketID(PacketTypeClasses.Play.Server.SPAWN_ENTITY_SPAWN, SPAWN_ENTITY_SPAWN);
                insertPacketID(PacketTypeClasses.Play.Server.ANIMATION, ANIMATION);
                insertPacketID(PacketTypeClasses.Play.Server.STATISTIC, STATISTIC);
                insertPacketID(PacketTypeClasses.Play.Server.BLOCK_BREAK, BLOCK_BREAK);
                insertPacketID(PacketTypeClasses.Play.Server.BLOCK_BREAK_ANIMATION, BLOCK_BREAK_ANIMATION);
                insertPacketID(PacketTypeClasses.Play.Server.TILE_ENTITY_DATA, TILE_ENTITY_DATA);
                insertPacketID(PacketTypeClasses.Play.Server.BLOCK_ACTION, BLOCK_ACTION);
                insertPacketID(PacketTypeClasses.Play.Server.BLOCK_CHANGE, BLOCK_CHANGE);
                insertPacketID(PacketTypeClasses.Play.Server.BOSS, BOSS);
                insertPacketID(PacketTypeClasses.Play.Server.SERVER_DIFFICULTY, SERVER_DIFFICULTY);
                insertPacketID(PacketTypeClasses.Play.Server.CHAT, CHAT);
                insertPacketID(PacketTypeClasses.Play.Server.MULTI_BLOCK_CHANGE, MULTI_BLOCK_CHANGE);
                insertPacketID(PacketTypeClasses.Play.Server.TAB_COMPLETE, TAB_COMPLETE);
                insertPacketID(PacketTypeClasses.Play.Server.COMMANDS, COMMANDS);
                insertPacketID(PacketTypeClasses.Play.Server.TRANSACTION, TRANSACTION);
                insertPacketID(PacketTypeClasses.Play.Server.CLOSE_WINDOW, CLOSE_WINDOW);
                insertPacketID(PacketTypeClasses.Play.Server.WINDOW_ITEMS, WINDOW_ITEMS);
                insertPacketID(PacketTypeClasses.Play.Server.WINDOW_DATA, WINDOW_DATA);
                insertPacketID(PacketTypeClasses.Play.Server.SET_SLOT, SET_SLOT);
                insertPacketID(PacketTypeClasses.Play.Server.SET_COOLDOWN, SET_COOLDOWN);
                insertPacketID(PacketTypeClasses.Play.Server.CUSTOM_PAYLOAD, CUSTOM_PAYLOAD);
                insertPacketID(PacketTypeClasses.Play.Server.CUSTOM_SOUND_EFFECT, CUSTOM_SOUND_EFFECT);
                insertPacketID(PacketTypeClasses.Play.Server.KICK_DISCONNECT, KICK_DISCONNECT);
                insertPacketID(PacketTypeClasses.Play.Server.ENTITY_STATUS, ENTITY_STATUS);
                insertPacketID(PacketTypeClasses.Play.Server.EXPLOSION, EXPLOSION);
                insertPacketID(PacketTypeClasses.Play.Server.UNLOAD_CHUNK, UNLOAD_CHUNK);
                insertPacketID(PacketTypeClasses.Play.Server.GAME_STATE_CHANGE, GAME_STATE_CHANGE);
                insertPacketID(PacketTypeClasses.Play.Server.OPEN_WINDOW_HORSE, OPEN_WINDOW_HORSE);
                insertPacketID(PacketTypeClasses.Play.Server.KEEP_ALIVE, KEEP_ALIVE);
                insertPacketID(PacketTypeClasses.Play.Server.MAP_CHUNK, MAP_CHUNK);
                insertPacketID(PacketTypeClasses.Play.Server.WORLD_EVENT, WORLD_EVENT);
                insertPacketID(PacketTypeClasses.Play.Server.WORLD_PARTICLES, WORLD_PARTICLES);
                insertPacketID(PacketTypeClasses.Play.Server.LIGHT_UPDATE, LIGHT_UPDATE);
                insertPacketID(PacketTypeClasses.Play.Server.LOGIN, LOGIN);
                insertPacketID(PacketTypeClasses.Play.Server.MAP, MAP);
                insertPacketID(PacketTypeClasses.Play.Server.OPEN_WINDOW_MERCHANT, OPEN_WINDOW_MERCHANT);
                insertPacketID(PacketTypeClasses.Play.Server.REL_ENTITY_MOVE, REL_ENTITY_MOVE);
                insertPacketID(PacketTypeClasses.Play.Server.REL_ENTITY_MOVE_LOOK, REL_ENTITY_MOVE_LOOK);
                insertPacketID(PacketTypeClasses.Play.Server.ENTITY_LOOK, ENTITY_LOOK);
                insertPacketID(PacketTypeClasses.Play.Server.ENTITY, ENTITY);
                insertPacketID(PacketTypeClasses.Play.Server.VEHICLE_MOVE, VEHICLE_MOVE);
                insertPacketID(PacketTypeClasses.Play.Server.OPEN_BOOK, OPEN_BOOK);
                insertPacketID(PacketTypeClasses.Play.Server.OPEN_WINDOW, OPEN_WINDOW);
                insertPacketID(PacketTypeClasses.Play.Server.OPEN_SIGN_EDITOR, OPEN_SIGN_EDITOR);
                insertPacketID(PacketTypeClasses.Play.Server.AUTO_RECIPE, AUTO_RECIPE);
                insertPacketID(PacketTypeClasses.Play.Server.ABILITIES, ABILITIES);
                insertPacketID(PacketTypeClasses.Play.Server.COMBAT_EVENT, COMBAT_EVENT);
                insertPacketID(PacketTypeClasses.Play.Server.PLAYER_INFO, PLAYER_INFO);
                insertPacketID(PacketTypeClasses.Play.Server.LOOK_AT, LOOK_AT);
                insertPacketID(PacketTypeClasses.Play.Server.POSITION, POSITION);
                insertPacketID(PacketTypeClasses.Play.Server.RECIPES, RECIPES);
                insertPacketID(PacketTypeClasses.Play.Server.ENTITY_DESTROY, ENTITY_DESTROY);
                insertPacketID(PacketTypeClasses.Play.Server.REMOVE_ENTITY_EFFECT, REMOVE_ENTITY_EFFECT);
                insertPacketID(PacketTypeClasses.Play.Server.RESOURCE_PACK_SEND, RESOURCE_PACK_SEND);
                insertPacketID(PacketTypeClasses.Play.Server.RESPAWN, RESPAWN);
                insertPacketID(PacketTypeClasses.Play.Server.ENTITY_HEAD_ROTATION, ENTITY_HEAD_ROTATION);
                insertPacketID(PacketTypeClasses.Play.Server.SELECT_ADVANCEMENT_TAB, SELECT_ADVANCEMENT_TAB);
                insertPacketID(PacketTypeClasses.Play.Server.WORLD_BORDER, WORLD_BORDER);
                insertPacketID(PacketTypeClasses.Play.Server.CAMERA, CAMERA);
                insertPacketID(PacketTypeClasses.Play.Server.HELD_ITEM_SLOT, HELD_ITEM_SLOT);
                insertPacketID(PacketTypeClasses.Play.Server.VIEW_CENTRE, VIEW_CENTRE);
                insertPacketID(PacketTypeClasses.Play.Server.VIEW_DISTANCE, VIEW_DISTANCE);
                insertPacketID(PacketTypeClasses.Play.Server.SCOREBOARD_DISPLAY_OBJECTIVE, SCOREBOARD_DISPLAY_OBJECTIVE);
                insertPacketID(PacketTypeClasses.Play.Server.ENTITY_METADATA, ENTITY_METADATA);
                insertPacketID(PacketTypeClasses.Play.Server.ATTACH_ENTITY, ATTACH_ENTITY);
                insertPacketID(PacketTypeClasses.Play.Server.ENTITY_VELOCITY, ENTITY_VELOCITY);
                insertPacketID(PacketTypeClasses.Play.Server.ENTITY_EQUIPMENT, ENTITY_EQUIPMENT);
                insertPacketID(PacketTypeClasses.Play.Server.EXPERIENCE, EXPERIENCE);
                insertPacketID(PacketTypeClasses.Play.Server.UPDATE_HEALTH, UPDATE_HEALTH);
                insertPacketID(PacketTypeClasses.Play.Server.SCOREBOARD_OBJECTIVE, SCOREBOARD_OBJECTIVE);
                insertPacketID(PacketTypeClasses.Play.Server.MOUNT, MOUNT);
                insertPacketID(PacketTypeClasses.Play.Server.SCOREBOARD_TEAM, SCOREBOARD_TEAM);
                insertPacketID(PacketTypeClasses.Play.Server.SCOREBOARD_SCORE, SCOREBOARD_SCORE);
                insertPacketID(PacketTypeClasses.Play.Server.SPAWN_POSITION, SPAWN_POSITION);
                insertPacketID(PacketTypeClasses.Play.Server.UPDATE_TIME, UPDATE_TIME);
                insertPacketID(PacketTypeClasses.Play.Server.TITLE, TITLE);
                insertPacketID(PacketTypeClasses.Play.Server.ENTITY_SOUND, ENTITY_SOUND);
                insertPacketID(PacketTypeClasses.Play.Server.NAMED_SOUND_EFFECT, NAMED_SOUND_EFFECT);
                insertPacketID(PacketTypeClasses.Play.Server.STOP_SOUND, STOP_SOUND);
                insertPacketID(PacketTypeClasses.Play.Server.PLAYER_LIST_HEADER_FOOTER, PLAYER_LIST_HEADER_FOOTER);
                insertPacketID(PacketTypeClasses.Play.Server.NBT_QUERY, NBT_QUERY);
                insertPacketID(PacketTypeClasses.Play.Server.COLLECT, COLLECT);
                insertPacketID(PacketTypeClasses.Play.Server.ENTITY_TELEPORT, ENTITY_TELEPORT);
                insertPacketID(PacketTypeClasses.Play.Server.ADVANCEMENTS, ADVANCEMENTS);
                insertPacketID(PacketTypeClasses.Play.Server.UPDATE_ATTRIBUTES, UPDATE_ATTRIBUTES);
                insertPacketID(PacketTypeClasses.Play.Server.ENTITY_EFFECT, ENTITY_EFFECT);
                insertPacketID(PacketTypeClasses.Play.Server.RECIPE_UPDATE, RECIPE_UPDATE);
                insertPacketID(PacketTypeClasses.Play.Server.TAGS, TAGS);
                insertPacketID(PacketTypeClasses.Play.Server.MAP_CHUNK_BULK, MAP_CHUNK_BULK);
                insertPacketID(PacketTypeClasses.Play.Server.NAMED_ENTITY_SPAWN, NAMED_ENTITY_SPAWN);
            }

            /**
             * Client-bound Play Packet Type utility.
             * Save a few lines of code by using this.
             *
             * @author retrooper
             * @since 1.8
             */
            @NoArgsConstructor(access = AccessLevel.PRIVATE)
            static final class Util {

                /**
                 * Is the play packet a PacketPlayOutEntity, PacketPlayOutRelEntityMove, PacketPlayOutRelEntityMoveLook
                 * or a PacketPlayOutEntityLook packet?
                 *
                 * @param packetID Play Packet ID.
                 * @return Is the Packet ID an instance of the PacketPlayOutEntity packet?
                 */
                public static boolean isInstanceOfEntity(byte packetID) {
                    return packetID == ENTITY
                            || packetID == REL_ENTITY_MOVE
                            || packetID == REL_ENTITY_MOVE_LOOK
                            || packetID == ENTITY_LOOK;
                }
            }
        }
    }

    /**
     * Get the packet class from the given packet id.
     *
     * @param packetId The packet id.
     * @return The packet class.
     */
    public static Class<?> getPacketFromId(Byte packetId) {
        return packetIDMap.entrySet().stream()
                .filter(entry -> Objects.equals(entry.getValue(), packetId))
                .map(Map.Entry::getKey)
                .findFirst()
                .orElse(null);
    }
}
