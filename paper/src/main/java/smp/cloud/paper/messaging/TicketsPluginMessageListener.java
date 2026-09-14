package smp.cloud.paper.messaging;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.messaging.PluginMessageListener;
import smp.cloud.common.messaging.OpenGuiPayload;
import smp.cloud.common.messaging.TicketChannel;
import smp.cloud.common.messaging.TicketPayload;
import smp.cloud.common.messaging.TicketProtocol;
import smp.cloud.paper.gui.TicketsGui;

import java.io.IOException;

public final class TicketsPluginMessageListener implements PluginMessageListener {

    private final Plugin plugin;

    public TicketsPluginMessageListener(Plugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void onPluginMessageReceived(String channel, Player player, byte[] message) {
        if (!TicketChannel.ID.equals(channel)) {
            return;
        }
        try {
            TicketPayload payload = TicketProtocol.decode(message);
            if (payload instanceof OpenGuiPayload open) {
                openGuiFor(open);
            }
        } catch (IOException e) {
            plugin.getLogger().warning("Failed to decode ticket plugin message: " + e.getMessage());
        }
    }

    private void openGuiFor(OpenGuiPayload payload) {
        Player viewer = Bukkit.getPlayer(payload.viewerId());
        if (viewer == null) {
            return;
        }
        TicketsGui gui = new TicketsGui(payload.viewerId(), payload.tickets());
        Bukkit.getScheduler().runTask(plugin, () -> viewer.openInventory(gui.getInventory()));
    }
}
