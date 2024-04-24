package dev.thomazz.pledge.pinger.data;

import dev.thomazz.pledge.pinger.ClientPingerImpl;
import lombok.Getter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

@Getter
public class PingData {

    private final Queue<Ping> expectingIds = new ConcurrentLinkedQueue<>();
    private final Player player;
    private final ClientPingerImpl pinger;

    private boolean validated = false;
    private int id;

    public PingData(Player player, @NotNull ClientPingerImpl pinger) {
        this.player = player;
        this.pinger = pinger;
        id = pinger.startId();
    }

    public int pullId() {
        int startId = pinger.startId();
        int endId = pinger.endId();

        boolean direction = endId - startId > 0;
        int oldId = id;
        int newId = oldId + (direction ? 1 : -1);

        if (direction ? newId > endId : newId < endId) {
            newId = startId;
        }

        id = newId;
        return oldId;
    }

    public void offer(@NotNull Ping ping) {
        expectingIds.add(ping);
    }

    public Optional<Ping> confirm(int id) {
        Ping ping = expectingIds.peek();

        if (ping != null && ping.getId() == id) {
            // Make sure to notify validation with the first correct ping received
            if (!validated) {
                pinger.getPingListeners().forEach(listener -> listener.onValidation(player, id));
                validated = true;
            }
            return Optional.ofNullable(expectingIds.poll());
        }
        return Optional.empty();
    }
}
