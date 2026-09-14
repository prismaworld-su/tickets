package smp.cloud.tickets;

import com.google.inject.Inject;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.plugin.Plugin;
import org.slf4j.Logger;
import smp.cloud.tickets.webhook.handler.TicketsWebhookHandler;
import smp.cloud.tickets.webhook.server.WebhookServer;
import smp.cloud.tickets.webhook.server.WebhookServerConfig;

import java.io.IOException;

@Plugin(id = "tickets", name = "tickets", version = "1.0-SNAPSHOT", description = "Minecraft plugin for adding the ability to create tickets and solve all your players' problems. The Minecraft plugin is designed for the Velocity proxy system.", url = "https://github.com/cloudy-developers", authors = {"losexds"})
public class Tickets {

    @Inject
    private Logger logger;

    private WebhookServer webhookServer;

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        webhookServer = WebhookServer.builder()
                .logger(logger)
                .config(WebhookServerConfig.defaults())
                .register(new TicketsWebhookHandler())
                .build();
        try {
            webhookServer.start();
        } catch (IOException e) {
            logger.error("Failed to start the webhook server", e);
        }
    }

    @Subscribe
    public void onProxyShutdown(ProxyShutdownEvent event) {
        if (webhookServer != null) {
            webhookServer.stop();
        }
    }
}
