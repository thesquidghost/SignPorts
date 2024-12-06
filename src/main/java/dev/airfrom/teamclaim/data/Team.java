package dev.airfrom.teamclaim.data;

import java.util.*;

import org.bukkit.inventory.ItemStack;

public class Team {
    private UUID uuid;
    private String name;
    private UUID leader;
    private List<UUID> members;
    private int claimBlocks;
    private long createdDate;
    private ItemStack banner;
    private Map<String, List<Permission>> perms;
    private boolean friendly_fire;

    private List<Invite> teamInvites;

    public Team(UUID uuid, String name, UUID leader, List<UUID> members, int claimBlocks, long createdDate, ItemStack banner, Map<String, List<Permission>> perms, List<Invite> teamInvites, boolean friendly_fire) {
        this.uuid = uuid;
        this.name = name;
        this.leader = leader;
        this.members = members;
        this.claimBlocks = claimBlocks;
        this.createdDate = createdDate;
        this.banner = banner;
        if(perms == null) {
        	Map<String, List<Permission>> uuids = new HashMap<>();
        	perms = uuids;
        }
        this.perms = perms;
        this.teamInvites = teamInvites;
        this.friendly_fire = friendly_fire;
    }

    public UUID getUUID() { return uuid; }
    public String getName() { return name; }
    public UUID getLeader() { return leader; }
    public List<UUID> getMembers() { return members; }
    public int getClaimBlocks() { return claimBlocks; }
    public long getCreatedDate() { return createdDate; }
    public ItemStack getBanner() { return banner; }
    public Map<String, List<Permission>> getPerms() {return perms;}
    public List<Invite> getInvites(){return teamInvites;}
    public boolean isFriendlyFire(){return friendly_fire;}

    public void setName(String name) {
    	this.name = name;
    }

    public void setLeader(UUID leader) {
    	this.leader = leader;
    }

    public void setMembers(List<UUID> members) {
    	this.members = members;
    }

    public void setClaimBlocks(int claimBlocks) {
    	this.claimBlocks = claimBlocks;
    }

    public void setCreatedDate(long createdDate) {
    	this.createdDate = createdDate;
    }
    
    public void setBanner(ItemStack banner) {
    	this.banner = banner;
    }
    
    public void setPerms(Map<String, List<Permission>> perms) {
        this.perms = perms;
    }
  
    public void setInvites(List<Invite> invites){
    	this.teamInvites = invites;
    }
    
    public void setFriendlyFire(boolean friendly_fire){
    	this.friendly_fire = friendly_fire;
    }
    
    public boolean playerHas(UUID uuid, Permission perm) {
    	if(perms.containsKey(uuid.toString())) {
    		return perms.get(uuid.toString()).contains(perm);
    	} 
    	return false;
    }
    
    public boolean globalHas(Permission perm) {
		if(perms.containsKey("ALL")) {
			return perms.get("ALL").contains(perm);
		}
    	return false;
    }
    
}


