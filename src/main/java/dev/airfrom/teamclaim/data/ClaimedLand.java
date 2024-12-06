package dev.airfrom.teamclaim.data;

import java.util.UUID;

public class ClaimedLand {
    private UUID uuid;
    private UUID region_uuid;
    private String worldName;
    private int x;
    private int z;

    public ClaimedLand(UUID uuid, UUID region_uuid, String worldName, int x, int z) {
        this.uuid = uuid;
        this.region_uuid = region_uuid;
        this.worldName = worldName;
        this.x = x;
        this.z = z;
    }

    public UUID getRegionUUID() { return region_uuid; }
    public UUID getUUID() { return uuid; }
    public String getWorldName() { return worldName; }
    public int getX() { return x; }
    public int getZ() { return z; }
    
    public void setRegionUUID(UUID region_uuid) { this.region_uuid = region_uuid; }
    public void setUUID(UUID uuid) { this.uuid = uuid; }
    public void setWorldName(String worldName) { this.worldName = worldName; }
    public void setX(int x) { this.x = x; }
    public void setZ(int z) { this.z = z; }

}

