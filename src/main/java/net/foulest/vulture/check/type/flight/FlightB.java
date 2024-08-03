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
package net.foulest.vulture.check.type.flight;

import io.github.retrooper.packetevents.utils.player.ClientVersion;
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

@CheckInfo(name = "Flight (B)", type = CheckType.FLIGHT,
        description = "Checks for invalid y-axis movement when in water.")
public class FlightB extends Check {

    private double lastDeltaY;
    private double lastVelocity;

    private int ticksInWater;
    private int ticksAboveCombined;

    public FlightB(PlayerData playerData) throws ClassNotFoundException {
        super(playerData);
    }

    @Override
    public void handle(@NotNull MovementEvent event, long timestamp) {
        double deltaY = event.getDeltaY();
        double velocity = player.getVelocity().getY();

        // Checks the player for exemptions.
        if (player.getGameMode() == GameMode.CREATIVE
                || player.getGameMode() == GameMode.SPECTATOR
                || playerData.getTicksSince(ActionType.LOGIN) < 40
                || playerData.getTicksSince(ActionType.STEER_VEHICLE) < 10
                || !playerData.isInLiquid()
                || playerData.isOnClimbable()
                || BlockUtil.isPlayerInUnloadedChunk(player)
                || BlockUtil.isLocationInUnloadedChunk(event.getToLocation())
                || BlockUtil.isLocationInUnloadedChunk(event.getFromLocation())
                || event.isTeleport(playerData)) {
            ticksInWater = 0;
            lastDeltaY = deltaY;
            lastVelocity = velocity;
            return;
        }

        ++ticksInWater;

        // Checks for invalid y-axis movement when in water.
        checkForInvalidY(deltaY, velocity);

        // Checks for invalid combined movement when in water.
        checkForCombined(deltaY, velocity);

        lastDeltaY = deltaY;
        lastVelocity = velocity;
    }

    /**
     * Checks for invalid y-axis movement when in water.
     *
     * @param deltaY   The change in y-axis.
     * @param velocity The player's velocity.
     */
    public void checkForInvalidY(double deltaY, double velocity) {
        int jumpBoostLevel = MovementUtil.getPotionEffectLevel(player, PotionEffectType.JUMP);
        double maxDeltaY = 0.461 + jumpBoostLevel * 0.1;

        // TODO: This can false flag when players get hit in water.

        if (deltaY > maxDeltaY) {
            flag(true, "deltaY=" + deltaY
                    + " velocity=" + velocity
                    + " maxDeltaY=" + maxDeltaY
                    + " velTicks=" + playerData.getTicksSince(ActionType.VELOCITY_GIVEN)
            );
        }
    }

    /**
     * Checks for invalid combined movement when in water.
     *
     * @param deltaY   The change in y-axis.
     * @param velocity The player's velocity.
     */
    public void checkForCombined(double deltaY, double velocity) {
        if (playerData.getVersion().isNewerThan(ClientVersion.v_1_8)) {
            return;
        }

        boolean nearGround = playerData.isNearGround();

        double deltaYDiff = Math.abs(deltaY - lastDeltaY);
        double velocityDiff = Math.abs(velocity - lastVelocity);
        double combinedDiff = Math.abs(deltaYDiff - velocityDiff);
        double altDiff = Math.abs(deltaY - velocity);

        double combinedThreshold = 0.15;
        int ticksAboveThreshold = (nearGround ? 4 : 2);

        if (combinedDiff >= combinedThreshold) {
            if (++ticksAboveCombined >= ticksAboveThreshold && altDiff > 0.001) {
                flag(true, "deltaY=" + deltaY
                        + " velocity=" + velocity + " |"
                        + " deltaYDiff=" + deltaYDiff
                        + " velocityDiff=" + velocityDiff + " |"
                        + " combinedDiff=" + combinedDiff
                        + " altDiff=" + altDiff + " |"
                        + " ticksInWater=" + ticksInWater
                        + " ticksAboveCombined=" + ticksAboveCombined
                        + " nearGround=" + nearGround
                );
            }
        } else {
            ticksAboveCombined = 0;
        }
    }
}
