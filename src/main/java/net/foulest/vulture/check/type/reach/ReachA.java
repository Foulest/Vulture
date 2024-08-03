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
package net.foulest.vulture.check.type.reach;

import io.github.retrooper.packetevents.event.eventtypes.CancellableNMSPacketEvent;
import io.github.retrooper.packetevents.packettype.PacketType;
import io.github.retrooper.packetevents.packetwrappers.NMSPacket;
import io.github.retrooper.packetevents.packetwrappers.play.in.useentity.WrappedPacketInUseEntity;
import net.foulest.vulture.check.Check;
import net.foulest.vulture.check.CheckInfo;
import net.foulest.vulture.check.CheckType;
import net.foulest.vulture.data.PlayerData;
import net.foulest.vulture.tracking.EntityTrackerEntry;
import net.foulest.vulture.util.ConstantUtil;
import net.foulest.vulture.util.MathUtil;
import net.foulest.vulture.util.MessageUtil;
import net.foulest.vulture.util.data.Area;
import net.foulest.vulture.util.raytrace.BoundingBox;
import org.bukkit.entity.EntityType;
import org.joml.Vector3d;

import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

@CheckInfo(name = "Reach (A)", type = CheckType.REACH,
        description = "Detects players with invalid reach.")
public class ReachA extends Check {

    public static double cancelDistance;

    public ReachA(PlayerData playerData) throws ClassNotFoundException {
        super(playerData);
    }

    @Override
    public void handle(CancellableNMSPacketEvent event, byte packetId,
                       NMSPacket nmsPacket, Object packet, long timestamp) {
        if (packetId == PacketType.Play.Client.USE_ENTITY) {
            WrappedPacketInUseEntity useEntity = new WrappedPacketInUseEntity(nmsPacket);
            EntityType entityType = useEntity.getEntity().getType();
            int entityId = useEntity.getEntity().getEntityId();
            EntityTrackerEntry entry = playerData.getEntityTracker().getEntry(entityId).orElse(null);

            // If the entity is not being tracked, we cannot perform a reach check.
            if (entry == null) {
                return;
            }

            // Get position area of entity we have been tracking.
            Area position = entry.getPosition();

            // Get the bounding box of the entity (for width and height).
            BoundingBox boundingBox = new BoundingBox(useEntity.getEntity());

            // Expand position area into bounding box
            double width = (boundingBox.max.getX() - boundingBox.min.getX());
            double height = (boundingBox.max.getY() - boundingBox.min.getY());

            // Create a bounding box for the entity
            Area box = new Area(position)
                    .expand(width / 2.0, 0.0, width / 2.0)
                    .addCoord(0.0, height, 0.0);

            // Get the intercept result
            // The hitbox is actually 0.1 blocks bigger than the bounding box
            float offset = ConstantUtil.COLLISION_BORDER_SIZE;
            box.expand(offset, offset, offset);

            // Compensate for fast math errors in the look vector calculations (Can remove if support not needed)
            double error = ConstantUtil.FAST_MATH_ERROR;
            box.expand(error, error, error);

            // Expand the box by the root of the minimum move amount in each axis if the player was not moving the last tick.
            // This is because they could have moved this amount on the client making a difference between a hit or miss.
            if (!playerData.isAccuratePosition()) {
                double minMove = ConstantUtil.MIN_MOVE_UPDATE_ROOT;
                box.expand(minMove, minMove, minMove);
            }

            // Mouse input is done before any sneaking updates
            float eyeHeight = (playerData.isWasSneaking() ? 1.54F : 1.62F);

            // Previous position since movement is done after attacking in the client tick
            Vector3d eye = playerData.getLastLoc().getPos().add(0, eyeHeight, 0, new Vector3d());

            // First check if the eye position is inside
            if (box.isInside(eye.x, eye.y, eye.z)) {
                return;
            }

            // Originally Minecraft uses the old yaw value for mouse intercepts, but some clients and mods fix this
            float lastYaw = playerData.getLastLoc().getRot().x;
            float yaw = playerData.getLoc().getRot().x;
            float pitch = playerData.getLoc().getRot().y;

            // Calculate the view vectors
            Vector3d lastView = MathUtil.getLookVector(lastYaw, pitch).mul(ConstantUtil.RAY_LENGTH);
            Vector3d view = MathUtil.getLookVector(yaw, pitch).mul(ConstantUtil.RAY_LENGTH);

            // Calculate the eye view vectors
            Vector3d lastEyeView = eye.add(lastView, new Vector3d());
            Vector3d eyeView = eye.add(view, new Vector3d());

            // Calculate intercepts with Minecraft ray logic
            Vector3d lastIntercept = MathUtil.calculateIntercept(box, eye, lastEyeView);
            Vector3d intercept = MathUtil.calculateIntercept(box, eye, eyeView);

            // Get minimum value of intercepts
            Optional<Double> result = Stream.of(lastIntercept, intercept)
                    .filter(Objects::nonNull)
                    .map(eye::distance)
                    .min(Double::compare);

            // If the intercept result is not present, cancel the event.
            if (!result.isPresent()) {
                event.setCancelled(true);
                MessageUtil.debug("Cancelled hit for " + player.getName()
                        + " (Range: Invalid) (Entity: " + entityType.name() + ")");
                return;
            }

            // Get the range from the result.
            double range = result.get();

            // Format the range to 2 decimal places.
            range = Math.round(range * 100.0) / 100.0;

            // Flags the player if the range is greater than the max distance.
            if (range > cancelDistance) {
                event.setCancelled(true);
                MessageUtil.debug("Cancelled hit for " + player.getName()
                        + " (Range: " + range + ") (Entity: " + entityType.name() + ")");
            }
        }
    }
}
