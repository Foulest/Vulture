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

import io.github.retrooper.packetevents.event.eventtypes.CancellableNMSPacketEvent;
import io.github.retrooper.packetevents.packettype.PacketType;
import io.github.retrooper.packetevents.packetwrappers.NMSPacket;
import io.github.retrooper.packetevents.packetwrappers.play.in.flying.WrappedPacketInFlying;
import io.github.retrooper.packetevents.utils.vector.Vector3d;
import net.foulest.vulture.action.ActionType;
import net.foulest.vulture.check.Check;
import net.foulest.vulture.check.CheckInfo;
import net.foulest.vulture.check.CheckType;
import net.foulest.vulture.data.PlayerData;

@CheckInfo(name = "Flight (C)", type = CheckType.FLIGHT,
        description = "Checks for ignoring gravity completely.")
public class FlightC extends Check {

    private static final double ON_GROUND_VELOCITY = -0.0784000015258789;
    private double lastY;
    private double lastVelocity;
    private int ticksInAir;

    public FlightC(PlayerData playerData) throws ClassNotFoundException {
        super(playerData);
    }

    @Override
    public void handle(CancellableNMSPacketEvent event, byte packetId,
                       NMSPacket nmsPacket, Object packet, long timestamp) {
        if (PacketType.Play.Client.Util.isInstanceOfFlying(packetId)) {
            WrappedPacketInFlying flying = new WrappedPacketInFlying(nmsPacket);
            Vector3d flyingPosition = flying.getPosition();
            double deltaY = flyingPosition.getY() - lastY;
            double velocity = player.getVelocity().getY();

            if (playerData.isNearbyBoat(0.6, 0.6, 0.6)
                    || playerData.isNearSlimeBlock()
                    || playerData.isFlying()
                    || playerData.isNearStairs()
                    || playerData.isNearSlab()
                    || playerData.getTicksSince(ActionType.STOP_FLYING) <= 4
                    || playerData.isInLiquid()) {
                lastY = flyingPosition.getY();
                lastVelocity = velocity;
                return;
            }

            if (velocity != ON_GROUND_VELOCITY && !playerData.isOnGround()
                    && velocity != lastVelocity && velocity != 0.0 && deltaY == 0.0
                    && !playerData.isNearbyBoat(0.6, 0.6, 0.6)) {
                ++ticksInAir;

                if (ticksInAir >= 4) {
                    flag(true, "ticks=" + ticksInAir
                            + " velocity=" + velocity
                            + " lastVelocity=" + lastVelocity
                            + " deltaY=" + deltaY
                            + " Y=" + flyingPosition.getY()
                            + " onGround=" + playerData.isOnGround()
                            + " nearGround=" + playerData.isNearGround()
                            + " againstBlock=" + playerData.isAgainstBlock()
                            + " underBlock=" + playerData.isUnderBlock()
                    );
                }
            } else {
                ticksInAir = 0;
            }

            lastY = flyingPosition.getY();
            lastVelocity = velocity;
        }
    }
}
