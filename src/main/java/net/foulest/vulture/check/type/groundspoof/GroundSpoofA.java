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
package net.foulest.vulture.check.type.groundspoof;

import io.github.retrooper.packetevents.packetwrappers.play.in.flying.WrappedPacketInFlying;
import io.github.retrooper.packetevents.utils.vector.Vector3d;
import net.foulest.vulture.action.ActionType;
import net.foulest.vulture.check.Check;
import net.foulest.vulture.check.CheckInfo;
import net.foulest.vulture.check.CheckType;
import net.foulest.vulture.data.PlayerData;
import net.foulest.vulture.event.MovementEvent;
import net.foulest.vulture.util.MovementUtil;

@CheckInfo(name = "GroundSpoof (A)", type = CheckType.GROUNDSPOOF,
        description = "Detects clients spoofing their ground status.")
public class GroundSpoofA extends Check {

    private int offGroundTicks;
    private int onGroundTicks;

    public GroundSpoofA(PlayerData playerData) throws ClassNotFoundException {
        super(playerData);
    }

    @Override
    public void handle(MovementEvent event, long timestamp) {
        // Checks the player for exemptions.
        if (playerData.getTicksSince(ActionType.LOGIN) <= 40
                || playerData.getTicksSince(ActionType.TELEPORT) <= 2
                || playerData.isNearbyBoat(0.6, 0.6, 0.6)) {
            return;
        }

        WrappedPacketInFlying to = event.getTo();
        Vector3d toPosition = to.getPosition();

        double deltaY = event.getDeltaY();
        double velocity = player.getVelocity().getY();

        boolean isYLevel = MovementUtil.isYLevel(toPosition.getY());
        boolean underBlock = playerData.isUnderBlock();
        boolean insideBlock = playerData.isInsideBlock();
        boolean onGround = playerData.isOnGround();
        boolean nearGround = playerData.isNearGround();

        int underBlockTicks = playerData.getUnderBlockTicks();

        if (!to.isOnGround() && onGround && isYLevel && velocity != 0.0
                && !playerData.isNearClimbable() && !playerData.isNearSlimeBlock()) {
            if (++offGroundTicks >= 2) {
                flag(true, "Sending Off Ground"
                        + " (Y=" + toPosition.getY()
                        + " deltaY=" + deltaY
                        + " velocity=" + velocity
                        + " underBlockTicks=" + underBlockTicks
                        + " insideBlock=" + insideBlock
                        + " teleport=" + playerData.getTicksSince(ActionType.TELEPORT)
                );
            }
        } else {
            offGroundTicks = 0;
        }

        if (to.isOnGround()) {
            ++onGroundTicks;

            if (!onGround && !isYLevel) {
                // Fixes a false flag when landing. (hopefully)
                if (onGroundTicks == 1 && nearGround) {
                    return;
                }

                flag(true, "Sending On Ground"
                        + " (Y=" + toPosition.getY()
                        + " deltaY=" + deltaY
                        + " velocity=" + velocity
                        + " underBlock=" + underBlock
                        + " underBlockTicks=" + underBlockTicks
                        + " nearGround=" + nearGround
                        + " onGroundTicks=" + onGroundTicks
                );
            }
        } else {
            onGroundTicks = 0;
        }
    }
}
