package net.farhaven.SignPorts;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

public class SignPortSetup {
    private String name;
    private Location signLocation;
    private String ownerName;
    private UUID ownerUUID;
    private Location destinationLocation;
    private boolean isPublic;
    private double cost;
    private ItemStack guiItem;
    private String description;

    // Constructor with all parameters
    public SignPortSetup(String name, Location signLocation, String ownerName, UUID ownerUUID, Location destinationLocation, boolean isPublic, double cost) {
        this.name = name;
        this.signLocation = signLocation;
        this.ownerName = ownerName;
        this.ownerUUID = ownerUUID;
        this.destinationLocation = destinationLocation;
        this.isPublic = isPublic;
        this.cost = cost;
    }

    // Constructor with only Location (for initial setup)
    public SignPortSetup(Location signLocation) {
        this.signLocation = signLocation;
        this.name = "";
        this.ownerName = "";
        this.ownerUUID = null;
        this.destinationLocation = null;
        this.isPublic = false;
        this.cost = 0.0;
    }

    // Getters and setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Location getSignLocation() { return signLocation; }
    public void setSignLocation(Location signLocation) { this.signLocation = signLocation; }

    public String getOwnerName() { return ownerName; }
    public void setOwnerName(String ownerName) { this.ownerName = ownerName; }

    public UUID getOwnerUUID() { return ownerUUID; }
    public void setOwnerUUID(UUID ownerUUID) { this.ownerUUID = ownerUUID; }

    public Location getDestinationLocation() { return destinationLocation; }
    public void setDestinationLocation(Location destinationLocation) { this.destinationLocation = destinationLocation; }

    public boolean isPublic() { return isPublic; }
    public void setPublic(boolean isPublic) { this.isPublic = isPublic; }

    public double getCost() { return cost; }
    public void setCost(double cost) { this.cost = cost; }

    public void setGuiItem(ItemStack item) { this.guiItem = item; }
    public ItemStack getGuiItem() { return this.guiItem; }

    public void setDescription(String description) { this.description = description; }
    public String getDescription() { return this.description; }

    // Utility methods
    public World getWorld() {
        return signLocation.getWorld();
    }

    public boolean isInSameWorld(Location otherLocation) {
        return this.getWorld().equals(otherLocation.getWorld());
    }

    public double getDistanceToDestination(Location fromLocation) {
        if (!isInSameWorld(fromLocation)) {
            return Double.MAX_VALUE;
        }
        return fromLocation.distance(destinationLocation);
    }

    // Configuration methods
    public static SignPortSetup fromConfig(ConfigurationSection config) {
        String name = config.getString("name");
        Location signLocation = parseLocation(config.getConfigurationSection("signLocation"));
        String ownerName = config.getString("ownerName");
        UUID ownerUUID = UUID.fromString(config.getString("ownerUUID"));
        Location destinationLocation = parseLocation(config.getConfigurationSection("destinationLocation"));
        boolean isPublic = config.getBoolean("isPublic");
        double cost = config.getDouble("cost");

        SignPortSetup setup = new SignPortSetup(name, signLocation, ownerName, ownerUUID, destinationLocation, isPublic, cost);
        setup.setDescription(config.getString("description"));
        // You might need to implement ItemStack deserialization
        // setup.setGuiItem(deserializeItemStack(config.getString("guiItem")));
        return setup;
    }

    public void saveToConfig(ConfigurationSection config) {
        config.set("name", name);
        config.set("ownerName", ownerName);
        config.set("ownerUUID", ownerUUID != null ? ownerUUID.toString() : null);
        config.set("isPublic", isPublic);
        config.set("cost", cost);
        config.set("description", description);
        // You might need to implement ItemStack serialization
        // config.set("guiItem", serializeItemStack(guiItem));

        saveLocation(config.createSection("signLocation"), signLocation);
        if (destinationLocation != null) {
            saveLocation(config.createSection("destinationLocation"), destinationLocation);
        }
    }

    private static Location parseLocation(ConfigurationSection locationConfig) {
        if (locationConfig == null) return null;
        World world = Bukkit.getWorld(locationConfig.getString("world"));
        double x = locationConfig.getDouble("x");
        double y = locationConfig.getDouble("y");
        double z = locationConfig.getDouble("z");
        float yaw = (float) locationConfig.getDouble("yaw");
        float pitch = (float) locationConfig.getDouble("pitch");

        return new Location(world, x, y, z, yaw, pitch);
    }

    private void saveLocation(ConfigurationSection config, Location location) {
        if (location == null || location.getWorld() == null) return;
        config.set("world", location.getWorld().getName());
        config.set("x", location.getX());
        config.set("y", location.getY());
        config.set("z", location.getZ());
        config.set("yaw", location.getYaw());
        config.set("pitch", location.getPitch());
    }

    @Override
    public String toString() {
        return "SignPortSetup{" +
                "name='" + name + '\'' +
                ", signLocation=" + signLocation +
                ", ownerName='" + ownerName + '\'' +
                ", ownerUUID=" + ownerUUID +
                ", destinationLocation=" + destinationLocation +
                ", isPublic=" + isPublic +
                ", cost=" + cost +
                ", guiItem=" + guiItem +
                ", description='" + description + '\'' +
                '}';
    }

    // You might need to implement these methods for ItemStack serialization/deserialization
    // private static ItemStack deserializeItemStack(String serialized) { ... }
    // private String serializeItemStack(ItemStack item) { ... }
}