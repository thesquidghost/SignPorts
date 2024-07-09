package net.farhaven.SignPorts;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

import java.util.HashMap;
import java.util.Map;

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
        signPorts.put(setup.getName(), setup);
    }

    public Map<String, SignPortSetup> getSignPorts() {
        return signPorts;
    }

    public void removeSignPort(String name) {
        signPorts.remove(name);
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
        for (SignPortSetup setup : signPorts.values()) {
            if (setup.getName().equalsIgnoreCase(name)) {
                plugin.getLogger().info("SignPort found: '" + setup.getName() + "'");
                return setup;
            }
        }
        plugin.getLogger().info("SignPort not found: '" + name + "'");
        return null;
    }
}