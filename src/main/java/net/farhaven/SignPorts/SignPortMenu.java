package net.farhaven.SignPorts;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SignPortMenu implements Listener {
    private final SignPorts plugin;
    private final Map<String, SignPortSetup> signPorts;
    private final SignPortGUI gui;

    public SignPortMenu(SignPorts plugin) {
        this.plugin = plugin;
        this.signPorts = new HashMap<>();
        this.gui = new SignPortGUI(plugin);
    }

    public void openSignPortMenu(Player player) {
        gui.openSignPortMenu(player, 1);
    }

    public void addSignPort(SignPortSetup setup) {
        signPorts.put(setup.getName().toLowerCase(), setup);
    }

    public Map<String, SignPortSetup> getSignPorts() {
        return new HashMap<>(signPorts); // Return a copy to prevent external modification
    }

    public void removeSignPort(String name) {
        signPorts.remove(name.toLowerCase());
    }

    public SignPortSetup getSignPortByLocation(Location location) {
        for (SignPortSetup setup : signPorts.values()) {
            if (setup.getSignLocation().equals(location)) {
                return setup;
            }
        }
        return null;
    }

    public SignPortSetup getSignPortByName(String name) {
        plugin.getLogger().info("Searching for SignPort: '" + name + "'");
        SignPortSetup setup = signPorts.get(name.toLowerCase());
        if (setup != null) {
            plugin.getLogger().info("SignPort found: '" + setup.getName() + "'");
        } else {
            plugin.getLogger().info("SignPort not found: '" + name + "'");
        }
        return setup;
    }

    public SignPortSetup getSignPortByOwner(UUID ownerUUID) {
        for (SignPortSetup setup : signPorts.values()) {
            if (setup.getOwnerUUID().equals(ownerUUID)) {
                return setup;
            }
        }
        return null;
    }

    public void reloadSignPorts() {
        signPorts.clear();
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            ConfigurationSection signportsSection = plugin.getConfig().getConfigurationSection("signports");
            if (signportsSection != null) {
                for (String key : signportsSection.getKeys(false)) {
                    ConfigurationSection signportSection = signportsSection.getConfigurationSection(key);
                    if (signportSection != null) {
                        SignPortSetup setup = SignPortSetup.fromConfig(signportSection);
                        Bukkit.getScheduler().runTask(plugin, () -> addSignPort(setup));
                    }
                }
            }
            plugin.getLogger().info("SignPorts reloaded successfully.");
        });
    }

}