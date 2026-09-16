package smp.cloud.paper.listener;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.Plugin;
import smp.cloud.common.messaging.StaffStatusPayload;
import smp.cloud.common.messaging.TicketChannel;
import smp.cloud.common.messaging.TicketProtocol;

import java.util.Objects;

public final class StaffStatusPublisher implements Listener {

    private static final long DELAY_TICKS = 20L;

    private final Plugin plugin;
    private final String permission;

    public StaffStatusPublisher(Plugin plugin, String permission) {
        this.plugin = Objects.requireNonNull(plugin, "plugin");
        this.permission = Objects.requireNonNull(permission, "permission");
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        Bukkit.getScheduler().runTaskLater(plugin, () -> publish(player), DELAY_TICKS);
    }

    private void publish(Player player) {
        if (!player.isOnline()) {
            return;
        }
        boolean staff = player.hasPermission(permission) || player.isOp();
        StaffStatusPayload payload = new StaffStatusPayload(player.getUniqueId(), staff);
        player.sendPluginMessage(plugin, TicketChannel.ID, TicketProtocol.encode(payload));
    }
}
