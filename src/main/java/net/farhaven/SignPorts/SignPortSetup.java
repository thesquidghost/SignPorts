package net.farhaven.SignPorts;

import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;

import java.util.Objects;
import java.util.UUID;

public class SignPortSetup {
    private String name;
    private final Location signLocation;
    private String ownerName;
    private UUID ownerUUID;
    private String description;
    private ItemStack guiItem;

    public SignPortSetup(Location signLocation) {
        this.signLocation = signLocation;
    }

    // Getters and setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Location getSignLocation() { return signLocation; }

    public String getOwnerName() { return ownerName; }
    public void setOwnerName(String ownerName) { this.ownerName = ownerName; }

    public UUID getOwnerUUID() { return ownerUUID; }
    public void setOwnerUUID(UUID ownerUUID) { this.ownerUUID = ownerUUID; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public ItemStack getGuiItem() { return guiItem; }
    public void setGuiItem(ItemStack guiItem) { this.guiItem = guiItem; }

    public void saveToConfig(ConfigurationSection config) {
        config.set("name", name);
        config.set("ownerName", ownerName);
        config.set("ownerUUID", ownerUUID.toString());
        config.set("description", description);
        config.set("signLocation", signLocation);
        config.set("guiItem", guiItem);
    }

    public static SignPortSetup fromConfig(ConfigurationSection config) {
        SignPortSetup setup = new SignPortSetup((Location) config.get("signLocation"));
        setup.setName(config.getString("name"));
        setup.setOwnerName(config.getString("ownerName"));
        setup.setOwnerUUID(UUID.fromString(Objects.requireNonNull(config.getString("ownerUUID"))));
        setup.setDescription(config.getString("description"));
        setup.setGuiItem((ItemStack) config.get("guiItem"));
        return setup;
    }
}