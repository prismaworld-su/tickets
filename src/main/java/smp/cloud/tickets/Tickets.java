package smp.cloud.tickets;

import com.google.inject.Inject;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import org.slf4j.Logger;
import smp.cloud.tickets.config.ConfigLoader;
import smp.cloud.tickets.config.TicketsConfig;
import smp.cloud.tickets.config.WebhookConfig;
import smp.cloud.tickets.webhook.handler.TicketsWebhookHandler;
import smp.cloud.tickets.webhook.server.WebhookServer;

import java.io.IOException;
import java.nio.file.Path;

@Plugin(id = "tickets", name = "tickets", version = "1.0-SNAPSHOT", description = "Minecraft plugin for adding the ability to create tickets and solve all your players' problems. The Minecraft plugin is designed for the Velocity proxy system.", url = "https://github.com/cloudy-developers", authors = {"losexds"})
public class Tickets {

    @Inject
    private Logger logger;

    @Inject
    @DataDirectory
    private Path dataDirectory;

    private WebhookServer webhookServer;

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        TicketsConfig config = loadConfig();
        startWebhookServer(config.webhook());
    }

    @Subscribe
    public void onProxyShutdown(ProxyShutdownEvent event) {
        if (webhookServer != null) {
            webhookServer.stop();
        }
    }

    private TicketsConfig loadConfig() {
        try {
            return new ConfigLoader(dataDirectory, logger).load();
        } catch (IOException e) {
            logger.error("Failed to load configuration, using defaults", e);
            return TicketsConfig.defaults();
        }
    }

    private void startWebhookServer(WebhookConfig config) {
        if (!config.enabled()) {
            logger.info("Webhook server is disabled in configuration");
            return;
        }
        try {
            webhookServer = WebhookServer.builder()
                    .logger(logger)
                    .config(config.toServerConfig())
                    .register(new TicketsWebhookHandler())
                    .build();
            webhookServer.start();
        } catch (IOException | IllegalArgumentException e) {
            logger.error("Failed to start the webhook server: {}", e.getMessage(), e);
            webhookServer = null;
        }
    }
}
