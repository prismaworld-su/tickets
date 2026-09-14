package smp.cloud.paper.gui;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.plugin.Plugin;
import smp.cloud.common.messaging.AcceptTicketPayload;
import smp.cloud.common.messaging.TicketChannel;
import smp.cloud.common.messaging.TicketProtocol;

import java.util.UUID;

public final class TicketsGuiListener implements Listener {

    private final Plugin plugin;

    public TicketsGuiListener(Plugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        InventoryHolder holder = event.getInventory().getHolder();
        if (!(holder instanceof TicketsGui gui)) {
            return;
        }
        event.setCancelled(true);
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }
        if (!player.getUniqueId().equals(gui.viewerId())) {
            return;
        }
        UUID ticketId = gui.ticketAtSlot(event.getRawSlot());
        if (ticketId == null) {
            return;
        }
        AcceptTicketPayload payload = new AcceptTicketPayload(ticketId, player.getUniqueId(), player.getName());
        player.sendPluginMessage(plugin, TicketChannel.ID, TicketProtocol.encode(payload));
        player.closeInventory();
    }
}
