package smp.cloud.paper;

import org.bukkit.plugin.java.JavaPlugin;
import smp.cloud.common.messaging.TicketChannel;
import smp.cloud.paper.gui.TicketsGuiListener;
import smp.cloud.paper.messaging.TicketsPluginMessageListener;

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
