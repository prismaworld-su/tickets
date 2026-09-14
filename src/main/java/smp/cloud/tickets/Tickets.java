package smp.cloud.tickets;

import com.google.inject.Inject;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.plugin.Plugin;
import org.slf4j.Logger;

@Plugin(id = "tickets", name = "tickets", version = "1.0-SNAPSHOT", description = "Minecraft plugin for adding the ability to create tickets and solve all your players' problems. The Minecraft plugin is designed for the Velocity proxy system.", url = "https://github.com/cloudy-developers", authors = {"losexds"})
public class Tickets {

    @Inject
    private Logger logger;

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
    }
}
