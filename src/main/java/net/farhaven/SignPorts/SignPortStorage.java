package net.farhaven.SignPorts;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;

public class SignPortStorage {
    private final SignPorts plugin;
    private final File storageFile;
    private final Gson gson;
    private final Type signPortMapType;

    private Map<UUID, SignPortSetup> signPorts;

    public SignPortStorage(SignPorts plugin) {
        this.plugin = plugin;
        this.storageFile = new File(plugin.getDataFolder(), "signports.json");
        this.gson = new GsonBuilder()
                .registerTypeAdapter(Location.class, new LocationAdapter())
                .registerTypeAdapter(ItemStack.class, new ItemStackAdapter())
                .setExclusionStrategies(new ExclusionStrategyImpl())
                .setPrettyPrinting() // Optional: Makes the JSON more readable
                .create();
        this.signPortMapType = new TypeToken<Map<UUID, SignPortSetup>>() {}.getType();
        this.signPorts = new HashMap<>();
        loadSignPorts();
    }

    public void loadSignPorts() {
        plugin.getLogger().info("Loading sign ports...");
        if (!storageFile.exists()) {
            try {
                if (!storageFile.getParentFile().mkdirs() && !storageFile.getParentFile().exists()) {
                    plugin.getLogger().warning("Failed to create parent directories for signports.json");
                }
                if (!storageFile.createNewFile()) {
                    plugin.getLogger().warning("Failed to create signports.json file");
                }
                plugin.getLogger().info("Created new signports.json file.");
            } catch (IOException e) {
                plugin.getLogger().log(Level.SEVERE, "Could not create signports.json file", e);
            }
        }
        try (FileReader reader = new FileReader(storageFile)) {
            signPorts = gson.fromJson(reader, signPortMapType);
            if (signPorts == null) {
                signPorts = new HashMap<>();
                plugin.getLogger().info("Initialized empty signPorts map.");
            }
            plugin.getLogger().info("Sign ports loaded successfully. Total: " + signPorts.size());
            for (Map.Entry<UUID, SignPortSetup> entry : signPorts.entrySet()) {
                plugin.getLogger().info("Loaded SignPort: " + entry.getValue().getName() + " by " + entry.getValue().getOwnerName());
            }
        } catch (JsonSyntaxException | IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Could not load signports from " + storageFile + ". The file may be corrupted or improperly formatted.", e);
            signPorts = new HashMap<>();
        }
    }

    public void saveSignPorts() {
        plugin.getLogger().info("Saving sign ports...");
        try (FileWriter writer = new FileWriter(storageFile)) {
            gson.toJson(signPorts, signPortMapType, writer);
            plugin.getLogger().info("Sign ports saved successfully.");
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Could not save signports to " + storageFile, e);
        }
    }

    public Map<String, SignPortSetup> getSignPorts() {
        Map<String, SignPortSetup> stringKeyedSignPorts = new HashMap<>();
        for (Map.Entry<UUID, SignPortSetup> entry : signPorts.entrySet()) {
            stringKeyedSignPorts.put(entry.getKey().toString(), entry.getValue());
        }
        return stringKeyedSignPorts;
    }

    public void addSignPort(SignPortSetup setup) {
        signPorts.put(setup.getOwnerUUID(), setup);
        saveSignPorts();
    }

    public SignPortSetup getSignPort(UUID ownerUUID) {
        return signPorts.get(ownerUUID);
    }
}