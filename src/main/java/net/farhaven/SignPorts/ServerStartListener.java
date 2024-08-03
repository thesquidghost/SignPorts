package net.farhaven.SignPorts;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.ServerLoadEvent;

public class ServerStartListener implements Listener {
    private final SignPorts plugin;

    public ServerStartListener(SignPorts plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onServerLoad(ServerLoadEvent event) {
        plugin.loadSignPorts();
    }
}