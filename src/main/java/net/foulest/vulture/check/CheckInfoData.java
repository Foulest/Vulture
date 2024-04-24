package net.foulest.vulture.check;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CheckInfoData {

    private String name;
    private CheckType type;
    private String description = "No description provided.";
    private boolean enabled = true;
    private boolean punishable = true;
    private String banCommand = "vulture kick %player% %check%";
    private int maxViolations = 10;
    private boolean experimental = false;
    private boolean acceptsServerPackets = false;

    public CheckInfoData(@NotNull CheckInfo checkInfo) {
        name = checkInfo.name();
        type = checkInfo.type();
        description = checkInfo.description();
        enabled = checkInfo.enabled();
        punishable = checkInfo.punishable();
        banCommand = checkInfo.banCommand();
        maxViolations = checkInfo.maxViolations();
        experimental = checkInfo.experimental();
        acceptsServerPackets = checkInfo.acceptsServerPackets();
    }
}
