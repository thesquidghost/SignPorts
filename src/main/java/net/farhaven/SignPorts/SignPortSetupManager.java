package net.farhaven.SignPorts;

import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

public class SignPortSetupManager {
    private final SignPorts plugin;
    private final Map<Player, SignPortSetup> pendingSetups;

    public SignPortSetupManager(SignPorts plugin) {
        this.plugin = plugin;
        this.pendingSetups = new HashMap<>();
    }

    public void startSetup(Player player, SignPortSetup setup) {
        pendingSetups.put(player, setup);
    }

    public SignPortSetup getPendingSetup(Player player) {
        return pendingSetups.get(player);
    }

    public void updatePendingSetup(Player player, SignPortSetup updatedSetup) {
        pendingSetups.put(player, updatedSetup);
    }

    public void completePendingSetup(Player player) {
        SignPortSetup setup = pendingSetups.remove(player);
        if (setup != null) {
            plugin.saveSignPort(setup);
        }
    }
}