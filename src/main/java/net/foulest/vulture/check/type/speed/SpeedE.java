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
import io.github.retrooper.packetevents.utils.player.ClientVersion;
import net.foulest.vulture.action.ActionType;
import net.foulest.vulture.check.Check;
import net.foulest.vulture.check.CheckInfo;
import net.foulest.vulture.check.CheckType;
import net.foulest.vulture.data.PlayerData;
import net.foulest.vulture.event.MovementEvent;
import net.foulest.vulture.util.MovementUtil;
import org.bukkit.GameMode;
import org.bukkit.potion.PotionEffectType;

@CheckInfo(name = "Speed (E)", type = CheckType.SPEED,
        description = "Prevents players from using NoSlowdown.")
public class SpeedE extends Check {

    private double bufferStandard;
    private double bufferRapid;

    public SpeedE(PlayerData playerData) throws ClassNotFoundException {
        super(playerData);
    }

    @Override
    public void handle(MovementEvent event, long timestamp) {
        // Checks the player for exemptions.
        if (player.isFlying()
                || player.getAllowFlight()
                || player.getGameMode() == GameMode.CREATIVE
                || player.getGameMode() == GameMode.SPECTATOR
                || event.isTeleport(playerData)) {
            return;
        }

        WrappedPacketInFlying to = event.getTo();

        int ticksSinceBlocking = playerData.getTicksSince(ActionType.BLOCKING);
        int ticksSinceRelease = playerData.getTicksSince(ActionType.RELEASE_USE_ITEM);
        int ticksBlocking = (ticksSinceBlocking < ticksSinceRelease ? ticksSinceBlocking : 0);

        boolean onIce = playerData.isOnIce();
        boolean blocking = playerData.isBlocking();
        boolean rapidlyBlocking = ticksSinceBlocking <= 2 && ticksSinceRelease <= 2;

        float speedLevel = MovementUtil.getPotionEffectLevel(player, PotionEffectType.SPEED);
        float walkSpeed = player.getWalkSpeed();
        float flySpeed = player.getFlySpeed();

        int groundTicks = playerData.getGroundTicks();

        double deltaXZ = event.getDeltaXZ();
        double velocityHorizontal = playerData.getVelocityXZ().getLast();
        double maxSpeed = to.isOnGround() ? 0.21 : 0.32400000005960464;

        maxSpeed += (playerData.getVersion().isOlderThanOrEquals(ClientVersion.v_1_8) ? 0.0 : 0.2);
        maxSpeed += (walkSpeed - 0.2) * 0.02;
        maxSpeed += (flySpeed - 0.1) * 0.01;
        maxSpeed += velocityHorizontal;
        maxSpeed *= onIce ? 6.8 : 1.0;
        maxSpeed += groundTicks < 5 ? speedLevel * 0.07 : speedLevel * 0.0573;

        // Detects standard no-slowdown.
        if (blocking) {
            if (deltaXZ > maxSpeed) {
                ++bufferStandard;

                if (bufferStandard >= 3) {
                    flag(true, "Standard"
                            + " (deltaXZ=" + deltaXZ
                            + " maxSpeed=" + maxSpeed
                            + " timeBlocking=" + ticksBlocking
                            + " buffer=" + bufferStandard + ")");
                }
            } else {
                bufferStandard = Math.max(bufferStandard - 0.25, 0);
            }
        } else {
            bufferStandard = Math.max(bufferStandard - 0.25, 0);
        }

        // Detects rapidly blocking no-slowdown.
        if (rapidlyBlocking) {
            if (deltaXZ > maxSpeed) {
                ++bufferRapid;

                if (bufferRapid >= 5) {
                    flag(true, "Rapidly blocking"
                            + " (deltaXZ=" + deltaXZ
                            + " maxSpeed=" + maxSpeed
                            + " timeBlocking=" + ticksBlocking
                            + " buffer=" + bufferRapid + ")");
                }
            } else {
                bufferRapid = Math.max(bufferRapid - 0.25, 0);
            }
        } else {
            bufferRapid = Math.max(bufferRapid - 0.25, 0);
        }
    }
}
