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
import net.foulest.vulture.util.BlockUtil;
import net.foulest.vulture.util.MovementUtil;
import org.bukkit.GameMode;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;

@CheckInfo(name = "Speed (C)", type = CheckType.SPEED)
public class SpeedC extends Check {

    private double lastDeltaXZ;
    private double friction = 0.91;
    private double buffer;

    public SpeedC(PlayerData playerData) throws ClassNotFoundException {
        super(playerData);
    }

    @Override
    public void handle(@NotNull MovementEvent event, long timestamp) {
        WrappedPacketInFlying from = event.getFrom();

        double deltaY = event.getDeltaY();
        double deltaXZ = event.getDeltaXZ();

        // Sets friction to 0.91 if the player is not on the ground.
        if (!from.isOnGround()) {
            friction = 0.91;
        }

        // Checks the player for exemptions.
        if (player.getAllowFlight()
                || playerData.isOnClimbable()
                || player.isInsideVehicle()
                || playerData.getTicksSince(ActionType.STEER_VEHICLE) <= 2
                || player.getGameMode() == GameMode.CREATIVE
                || player.getGameMode() == GameMode.SPECTATOR
                || event.isTeleport(playerData)) {
            lastDeltaXZ = deltaXZ * friction;
            return;
        }

        boolean sprinting = playerData.isSprinting();
        boolean onSlab = playerData.isOnSlab();
        boolean onIce = playerData.isOnIce();
        boolean underBlock = playerData.isUnderBlock();
        boolean nearLiquid = playerData.isNearLiquid();

        float speedLevel = MovementUtil.getPotionEffectLevel(player, PotionEffectType.SPEED);
        float jumpLevel = MovementUtil.getPotionEffectLevel(player, PotionEffectType.JUMP);
        float walkSpeed = player.getWalkSpeed();

        double jumpHeight = 0.42 + (jumpLevel * 0.2);
        double velocityHorizontal = playerData.getVelocityXZ().getLast();
        double blockFriction = BlockUtil.getBlockFriction(player);

        double movementSpeed;

        if (from.isOnGround()) {
            movementSpeed = sprinting ? (0.0699999988079071 + 0.030000001192092896) * 1.3 : 0.1;
            movementSpeed *= 0.16277136 / (friction * friction * friction);

            if (deltaY > 0.0000001 && deltaY < jumpHeight && sprinting) {
                movementSpeed += 0.2;
            }

            if (onSlab) {
                movementSpeed += 0.2;
            }

            if (onIce) {
                movementSpeed += 0.11;
            }

        } else {
            movementSpeed = 0.026;

            if (underBlock) {
                movementSpeed = movementSpeed * 2;
            }

            if (onSlab) {
                movementSpeed += 0.2;
            }

            if (deltaY > 0.4199 && sprinting) {
                movementSpeed += 0.4199;
            }
        }

        movementSpeed += (speedLevel * 0.2) * movementSpeed;
        movementSpeed += velocityHorizontal;
        movementSpeed += (walkSpeed - 0.2) * 12;

        if (nearLiquid) {
            movementSpeed += 0.2;
            movementSpeed *= 8;
        }

        double diff = deltaXZ - lastDeltaXZ;
        double speedup = diff - movementSpeed;

        if (speedup > 0.1 && deltaXZ > 0.25) {
            ++buffer;

            if (buffer >= 3) {
                flag(true, "speedup=" + speedup
                        + " buffer=" + buffer
                        + " deltaXZ=" + deltaXZ
                        + " diff=" + diff
                        + " movementSpeed=" + movementSpeed);
            }
        } else {
            buffer = Math.max(buffer - 0.25, 0);
        }

        lastDeltaXZ = deltaXZ * friction;
        friction = blockFriction * 0.91;
    }
}
