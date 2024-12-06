package dev.airfrom.teamclaim.data;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bukkit.inventory.ItemStack;

public class PlayerData {
    private UUID uuid;
    private List<UUID> teamUUID;
    private int claimBlocks;
    private long playTime;
    private ItemStack skull;

    public PlayerData(UUID uuid, List<UUID> teamUUID, int claimBlocks, long playTime, ItemStack skull) {
        this.uuid = uuid;
        if(teamUUID == null) {
        	List<UUID> uuids = new ArrayList<>();
        	teamUUID = uuids;
        }
        this.teamUUID = teamUUID;
        this.claimBlocks = claimBlocks;
        this.playTime = playTime;
        this.skull = skull;
    }
    
    public void setUUID(UUID uuid) {
        this.uuid = uuid;
    }

    public void setTeamList(List<UUID> uuid) {
    	this.teamUUID = uuid;
    }

    public void setClaimBlocks(int cb) {
        this.claimBlocks = cb;
    }

    public void setPlayTime(long pl) {
        this.playTime = pl;
    }
    
    public void setSkull(ItemStack skull) {
        this.skull = skull;
    }

    public UUID getUUID() {
        return uuid;
    }

    public List<UUID> getTeamUUIDs() {
        return teamUUID;
    }

    public int getClaimBlocks() {
        return claimBlocks;
    }

    public long getPlayTime() {
        return playTime;
    }
    
    public ItemStack getSkull() {
        return skull;
    }
    
}


