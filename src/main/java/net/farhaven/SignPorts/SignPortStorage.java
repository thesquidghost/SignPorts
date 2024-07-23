package net.farhaven.SignPorts;

import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.logging.Level;

public class SignPortStorage {
    private final SignPorts plugin;
    private final File storageFile;
    private FileConfiguration storageConfig;
    private final Map<UUID, SignPortSetup> signPorts = new HashMap<>();

    public SignPortStorage(SignPorts plugin) {
        this.plugin = plugin;
        this.storageFile = new File(plugin.getDataFolder(), "signports.yml");
        this.loadSignPorts();
    }

    public void loadSignPorts() {
        if (!storageFile.exists()) {
            try {
                if (!storageFile.getParentFile().mkdirs() && !storageFile.getParentFile().exists()) {
                    plugin.getLogger().warning("Failed to create parent directories for signports.yml");
                }
                if (!storageFile.createNewFile()) {
                    plugin.getLogger().warning("Failed to create signports.yml file");
                }
            } catch (IOException e) {
                plugin.getLogger().log(Level.SEVERE, "Could not create signports.yml file", e);
            }
        }
        storageConfig = YamlConfiguration.loadConfiguration(storageFile);

        signPorts.clear();
        for (String key : storageConfig.getKeys(false)) {
            SignPortSetup setup = SignPortSetup.fromConfig(Objects.requireNonNull(storageConfig.getConfigurationSection(key)));
            signPorts.put(setup.getOwnerUUID(), setup);
        }
    }

    public void getSignPorts() {
	return new HashMap<>(signPorts);
    }

    public void saveSignPorts() {
        for (SignPortSetup setup : signPorts.values()) {
            setup.saveToConfig(storageConfig.createSection(setup.getOwnerUUID().toString()));
        }
        try {
            storageConfig.save(storageFile);
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Could not save signports to " + storageFile, e);
        }
    }

    @SuppressWarnings("unused")
    public void addSignPort(SignPortSetup setup) {
        signPorts.put(setup.getOwnerUUID(), setup);
        saveSignPorts();
    }

    @SuppressWarnings("unused")
    public void removeSignPort(UUID ownerUUID) {
        signPorts.remove(ownerUUID);
        storageConfig.set(ownerUUID.toString(), null);
        saveSignPorts();
    }

    public SignPortSetup getSignPort(UUID ownerUUID) {
        return signPorts.get(ownerUUID);
    }

    @SuppressWarnings("unused")
    public SignPortSetup getSignPortByLocation(Location location) {
        for (SignPortSetup setup : signPorts.values()) {
            if (setup.getSignLocation().equals(location)) {
                return setup;
            }
        }
        return null;
    }

    @SuppressWarnings("unused")
    public boolean hasSignPort(UUID ownerUUID) {
        return signPorts.containsKey(ownerUUID);
    }
}
