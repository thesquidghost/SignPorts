package net.farhaven.SignPorts;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

import java.util.HashMap;
import java.util.Map;

public class SignPortMenu implements Listener {
    private final Map<String, SignPortSetup> signPorts;
    private final SignPortGUI gui;

    public SignPortMenu(SignPorts plugin) {
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
        return signPorts.get(name);
    }
}