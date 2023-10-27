package net.foulest.vulture.check.type.speed;

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

@CheckInfo(name = "Speed (F)", type = CheckType.SPEED,
        description = "Prevents players from moving with an open inventory.")
public class SpeedF extends Check {

    private double buffer;

    public SpeedF(@NonNull PlayerData playerData) throws ClassNotFoundException {
        super(playerData);
    }

    @Override
    public void handle(@NonNull MovementEvent event, long timestamp) {
        Vector velocity = player.getVelocity();

        float speedLevel = MovementUtil.getPotionEffectLevel(player, PotionEffectType.SPEED);
        float slownessLevel = MovementUtil.getPotionEffectLevel(player, PotionEffectType.SPEED);

        long timeSinceOpen = playerData.getTimeSince(ActionType.INVENTORY_OPEN);

        double deltaXZ = event.getDeltaXZ();
        double maxSpeed = player.getInventory().getType() == InventoryType.PLAYER ? 0.1 : 0.16;

        maxSpeed += playerData.getGroundTicks() < 5 ? speedLevel * 0.07 : speedLevel * 0.0573;
        maxSpeed -= playerData.getGroundTicks() < 5 ? slownessLevel * 0.07 : slownessLevel * 0.0573;
        maxSpeed += Math.abs(playerData.getVelocityHorizontal());
        maxSpeed += Math.abs(velocity.getX());
        maxSpeed += Math.abs(velocity.getZ());

        // Detects moving while inventory is open.
        if (playerData.isInventoryOpen() && deltaXZ > maxSpeed && timeSinceOpen > 500) {
            if (++buffer >= 5) {
                flag(true, "deltaXZ=" + deltaXZ
                        + " maxSpeed=" + maxSpeed
                        + " timeSinceOpen=" + timeSinceOpen);
            }
        } else {
            buffer = Math.max(buffer - 0.25, 0);
        }
    }
}
