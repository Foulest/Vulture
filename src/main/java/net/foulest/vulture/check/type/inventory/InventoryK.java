package net.foulest.vulture.check.type.inventory;

import io.github.retrooper.packetevents.packetwrappers.play.in.flying.WrappedPacketInFlying;
import io.github.retrooper.packetevents.utils.vector.Vector3d;
import lombok.NonNull;
import net.foulest.vulture.action.ActionType;
import net.foulest.vulture.check.Check;
import net.foulest.vulture.check.CheckInfo;
import net.foulest.vulture.check.CheckType;
import net.foulest.vulture.data.PlayerData;
import net.foulest.vulture.event.MovementEvent;
import net.foulest.vulture.util.MovementUtil;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

@CheckInfo(name = "Inventory (K)", type = CheckType.INVENTORY, maxViolations = 25,
        description = "Prevents players from moving with an open inventory.")
public class InventoryK extends Check {

    public InventoryK(@NonNull PlayerData playerData) throws ClassNotFoundException {
        super(playerData);
    }

    @Override
    public void handle(@NonNull MovementEvent event, long timestamp) {
        WrappedPacketInFlying to = event.getTo();
        WrappedPacketInFlying from = event.getFrom();

        Vector3d toPosition = to.getPosition();
        Vector3d fromPosition = from.getPosition();

        double deltaX = toPosition.getX() - fromPosition.getX();
        double deltaZ = toPosition.getZ() - fromPosition.getZ();
        double deltaXZ = Math.sqrt(deltaX * deltaX + deltaZ * deltaZ);

        Vector velocity = player.getVelocity();

        long timeSinceOpen = playerData.getTimeSince(ActionType.INVENTORY_OPEN);

        float speedLevel = MovementUtil.getPotionEffectLevel(player, PotionEffectType.SPEED);
        float slownessLevel = MovementUtil.getPotionEffectLevel(player, PotionEffectType.SPEED);
        // TODO: factor in slowness level

        double maxSpeed = player.getInventory().getType() == InventoryType.PLAYER ? 0.1 : 0.16;
        maxSpeed += playerData.getGroundTicks() < 5 ? speedLevel * 0.07 : speedLevel * 0.0573;
        maxSpeed += Math.abs(playerData.getVelocityHorizontal());
        maxSpeed += Math.abs(velocity.getX());
        maxSpeed += Math.abs(velocity.getZ());

        // Detects moving while inventory is open.
        if (playerData.isInventoryOpen() && deltaXZ > maxSpeed && timeSinceOpen > 500) {
            flag("deltaXZ=" + deltaXZ + " maxSpeed=" + maxSpeed + " timeSinceOpen=" + timeSinceOpen);
        }
    }
}
