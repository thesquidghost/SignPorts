package dev.airfrom.teamclaim.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;

public class Region {
	
    private UUID uuid;
    private UUID teamUUID;
    private List<UUID> lands;
    private String name;
    private Location spawn;
    private Map<Property, Boolean> properties;
    private Map<String, List<Permission>> perms;
    private List<String> titles;
    private ItemStack icon;

    public Region(UUID uuid, UUID teamUUID, List<UUID> lands, String name, Location spawn, Map<String, List<Permission>> perms, Map<Property, Boolean> properties, List<String> titles, ItemStack icon) {
        this.uuid = uuid;
        this.teamUUID = teamUUID;
        if(lands == null) {
        	lands = new ArrayList<>();
        }
        this.lands = lands;
        if(perms == null) {
        	perms = new HashMap<>();
        }
        this.spawn = spawn;
        this.perms = perms;
        if(titles == null) {
        	titles = new ArrayList<>();
        }
        this.titles = titles;
        this.icon = icon;
        if(properties == null) {
        	properties = new HashMap<>();
        	properties.put(Property.PUBLIC_ACCESS, true);
        	for(Property pr : Property.values()) {
        		if(pr != Property.PUBLIC_ACCESS) {
        			properties.put(pr, false);
        		}
        	}
        }
        this.properties = properties;
        this.name = name;
    }

    public UUID getUUID() { return uuid; }
    public void setUUID(UUID uuid) { this.uuid = uuid; }

    public UUID getTeamUUID() { return teamUUID; }
    public void setTeamUUID(UUID teamUUID) { this.teamUUID = teamUUID; }

    public List<UUID> getLands() { return lands; }
    public void setLands(List<UUID> lands) { this.lands = lands; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public Location getSpawn() { return spawn; }
    public void setSpawn(Location spawn) { this.spawn = spawn; }

    public Map<String, List<Permission>> getPerms() { return perms; }
    public void setPerms(Map<String, List<Permission>> perms) { this.perms = perms; }
    
    public Map<Property, Boolean> getProperties() { return properties; }
    
    public void setProperty(Property property, boolean bool) { 
    	properties.replace(property, bool);
    }

    public List<String> getTitles() { return titles; }
    public void setTitles(List<String> titles) { this.titles = titles; }
    
    public ItemStack getIcon() { return icon; }
    public void setIcon(ItemStack icon) { this.icon = icon; }
    
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
    
    public boolean membersHave(Permission perm) {
		if(perms.containsKey("MEMBERS")) {
			return perms.get("MEMBERS").contains(perm);
		}
    	return false;
    }
    
    public boolean hasPropertyPerm(UUID uuid) {
    	if(!perms.containsKey(uuid.toString())) {
    		return false;
    	}
    	boolean has = false;
    	for(Permission perm : Permission.values()) {
    		if(perm.getLinkedProperty() != null) {
    			has = perms.get(uuid.toString()).contains(perm);
    		}
    	}
    	return has;
    }
    
}

