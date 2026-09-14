package smp.cloud.tickets.paper;

import org.bukkit.plugin.java.JavaPlugin;
import smp.cloud.tickets.common.messaging.TicketChannel;
import smp.cloud.tickets.paper.gui.TicketsGuiListener;
import smp.cloud.tickets.paper.messaging.TicketsPluginMessageListener;

public final class TicketsPaperPlugin extends JavaPlugin {

    @Override
    public void onEnable() {
        getServer().getMessenger().registerIncomingPluginChannel(this, TicketChannel.ID,
                new TicketsPluginMessageListener(this));
        getServer().getMessenger().registerOutgoingPluginChannel(this, TicketChannel.ID);
        getServer().getPluginManager().registerEvents(new TicketsGuiListener(this), this);
        getLogger().info("Tickets Paper companion enabled");
    }

    @Override
    public void onDisable() {
        getServer().getMessenger().unregisterIncomingPluginChannel(this);
        getServer().getMessenger().unregisterOutgoingPluginChannel(this);
    }
}
