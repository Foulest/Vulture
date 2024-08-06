/*
 * Vulture - an advanced anti-cheat plugin designed for Minecraft 1.8.9 servers.
 * Copyright (C) 2024 Foulest (https://github.com/Foulest)
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
package net.foulest.vulture.check.type.speed;

import io.github.retrooper.packetevents.packetwrappers.play.in.flying.WrappedPacketInFlying;
import net.foulest.vulture.action.ActionType;
import net.foulest.vulture.check.Check;
import net.foulest.vulture.check.CheckInfo;
import net.foulest.vulture.check.CheckType;
import net.foulest.vulture.data.PlayerData;
import net.foulest.vulture.event.MovementEvent;
import net.foulest.vulture.util.MovementUtil;
import org.bukkit.GameMode;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;

@CheckInfo(name = "Speed (A)", type = CheckType.SPEED)
public class SpeedA extends Check {

    private double buffer;
    private double terrainBuffer;

    public SpeedA(PlayerData playerData) throws ClassNotFoundException {
        super(playerData);
    }

    @Override
    public void handle(@NotNull MovementEvent event, long timestamp) {
        WrappedPacketInFlying to = event.getTo();

        // Checks the player for exemptions.
        if (player.isFlying()
                || player.getAllowFlight()
                || player.getGameMode() == GameMode.CREATIVE
                || player.getGameMode() == GameMode.SPECTATOR
                || playerData.getTicksSince(ActionType.STEER_VEHICLE) < 10
                || player.isInsideVehicle()
                || event.isTeleport(playerData)) {
            return;
        }

        boolean inWeb = playerData.isInWeb();
        boolean onSoulSand = playerData.isOnSoulSand();
        boolean onStairs = playerData.isOnStairs();
        boolean onSlab = playerData.isOnSlab();
        boolean nearLiquid = playerData.isNearLiquid();
        boolean nearSlimeBlock = playerData.isNearSlimeBlock();
        boolean nearLilyPad = playerData.isNearLilyPad();

        double deltaXZ = event.getDeltaXZ();
        double maxSpeed = to.isOnGround() && !nearLilyPad ? 0.3125 : 0.35855;

        // Ignores if the player's delta XZ is less than their horizontal velocity taken.
        // This is to prevent false positives with velocity.
        // The air ticks scale with the player's velocity.
        if ((Math.abs(playerData.getVelocityXZ().getLast() - deltaXZ) < 0.01
                || playerData.getVelocityXZ().getLast() > deltaXZ)
                && (playerData.getTotalTicks() - playerData.getVelocityXZ().getFirst())
                <= (int) (playerData.getVelocityXZ().getLast() * 10)) {
            return;
        }

        int groundTicks = playerData.getGroundTicks();
        int groundTicksStrict = playerData.getGroundTicksStrict();

        long timeSinceOnIce = playerData.getTicksSince(ActionType.ON_ICE);
        long timeSinceUnderBlock = playerData.getTicksSince(ActionType.UNDER_BLOCK);

        float speedLevel = MovementUtil.getPotionEffectLevel(player, PotionEffectType.SPEED);
        float slownessLevel = MovementUtil.getPotionEffectLevel(player, PotionEffectType.SLOW);
        float depthStriderLevel = MovementUtil.getDepthStriderLevel(player);

        float walkSpeed = player.getWalkSpeed();
        float flySpeed = player.getFlySpeed();

        maxSpeed += groundTicks < 5 ? speedLevel * 0.07 : speedLevel * 0.0573;
        maxSpeed -= groundTicks < 5 ? slownessLevel * 0.07 : slownessLevel * 0.0573;

        maxSpeed += timeSinceUnderBlock < 100 ? 0.26 : 0.0;
        maxSpeed += nearLiquid ? depthStriderLevel * 0.45 : 0.0;
        maxSpeed += (walkSpeed - 0.2) * 2.5;
        maxSpeed += (flySpeed - 0.1) * 2.5;

        maxSpeed *= (onStairs || onSlab) ? 1.5 : 1.0;
        maxSpeed *= inWeb ? 0.11 : 1.0;
        maxSpeed *= timeSinceOnIce < 100 ? 4.4 : 1.0; // TODO: This is a bit high
        maxSpeed *= onSoulSand && groundTicksStrict > 2 ? 0.6 : 1.0;
        maxSpeed *= nearSlimeBlock ? 1.25 : 1.0;

        double difference = deltaXZ - maxSpeed;

        if (deltaXZ > maxSpeed) {
            if (inWeb || onSoulSand) {
                ++terrainBuffer;

                if (terrainBuffer > 2) {
                    flag(true, "(Terrain)"
                            + " deltaXZ=" + deltaXZ
                            + " maxSpeed=" + maxSpeed
                            + " buffer=" + buffer);
                }
            } else {
                buffer += 0.1 + difference;

                // TODO: This false flags with velocity.

                if (buffer > 1) {
                    flag(true, "deltaXZ=" + deltaXZ
                            + " maxSpeed=" + maxSpeed
                            + " difference=" + difference
                            + " buffer=" + buffer
                            + " velXZ=" + playerData.getVelocityXZ().getLast()
                            + " velXZT=" + (playerData.getTotalTicks() - playerData.getVelocityXZ().getFirst())
                            + " lastVelXZ=" + playerData.getLastVelocityXZ().getLast()
                            + " lastVelXZT=" + (playerData.getTotalTicks() - playerData.getLastVelocityXZ().getFirst())
                    );
                }
            }
        } else {
            buffer = Math.max(buffer - 0.25, 0);
            terrainBuffer = Math.max(terrainBuffer - 0.25, 0);
        }
    }
}
