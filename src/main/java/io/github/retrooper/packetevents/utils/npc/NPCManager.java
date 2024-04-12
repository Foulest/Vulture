package io.github.retrooper.packetevents.utils.npc;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class NPCManager {

    private final Map<Integer, NPC> npcMap = new ConcurrentHashMap<>();

    @Nullable
    public NPC getNPCById(int entityID) {
        return npcMap.get(entityID);
    }

    public Collection<NPC> getNPCList() {
        return npcMap.values();
    }

    public void registerNPC(NPC npc) {
        npcMap.put(npc.getEntityId(), npc);
    }

    public void unregisterNPC(@NotNull NPC npc) {
        npcMap.remove(npc.getEntityId());
    }
}
