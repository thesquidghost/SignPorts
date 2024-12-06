package net.farhaven.SignPorts;

import com.google.gson.annotations.Expose;
import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

public class SignPortSetup {
    @Expose
    private String name;
    @Expose
    private Location signLocation;
    @Expose
    private String ownerName;
    @Expose
    private UUID ownerUUID;
    @Expose
    private String description;
    @Expose
    private ItemStack guiItem;
    @Expose
    private boolean locked;

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

    public boolean isLocked() { return locked; }
    public void setLocked(boolean locked) { this.locked = locked; }
}