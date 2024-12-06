package dev.airfrom.teamclaim.data;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.stream.Collectors;

public class DataManager {
	
    private JavaPlugin plugin;
    private FileConfiguration teamsConfig;
    private FileConfiguration claimedLandsConfig;
    private FileConfiguration regionsConfig;
    private File teamsFile;
    private File claimedLandsFile;
    private File regionsFile;
    
    private List<Team> teams = new ArrayList<>();
    private List<PlayerData> players = new ArrayList<>();
    private List<ClaimedLand> claimedLands = new ArrayList<>();
    private List<Region> regions = new ArrayList<>();
    
    public DataManager(JavaPlugin plugin) {
        this.plugin = plugin;
        loadConfigurations();
    }

    private void loadConfigurations() {
	    File playersDir = new File(plugin.getDataFolder(), "players");
	    if(!playersDir.exists()) {
	        playersDir.mkdirs();
	    }
	    
    	teamsFile = new File(plugin.getDataFolder().getAbsolutePath() + File.separator + "teams.yml");
    	teamsConfig = YamlConfiguration.loadConfiguration(teamsFile);
    	if(!teamsConfig.contains("teams")) teamsConfig.createSection("teams");
    	
    	claimedLandsFile = new File(plugin.getDataFolder().getAbsolutePath() + File.separator + "claimed_lands.yml");
    	claimedLandsConfig = YamlConfiguration.loadConfiguration(claimedLandsFile);
    	if(!claimedLandsConfig.contains("claimed_lands")) claimedLandsConfig.createSection("claimed_lands");
    	
    	regionsFile = new File(plugin.getDataFolder().getAbsolutePath() + File.separator + "regions.yml");
    	regionsConfig = YamlConfiguration.loadConfiguration(regionsFile);
    	if(!regionsConfig.contains("regions")) regionsConfig.createSection("regions");
    	
        loadPlayers(playersDir);
        loadTeams();
        loadClaimedLands();
        loadRegions();
    }

    private void loadTeams() {
        for(String key : teamsConfig.getConfigurationSection("teams").getKeys(false)) {
            String name = teamsConfig.getString("teams." + key + ".name");
            UUID leader = UUID.fromString(teamsConfig.getString("teams." + key + ".leader"));
            List<String> uuids = teamsConfig.getStringList("teams." + key + ".members");
            List<UUID> members = new ArrayList<UUID>();
            for(String m : uuids) {
            	members.add(UUID.fromString(m));
            }
            int claimBlocks = teamsConfig.getInt("teams." + key + ".claim_blocks");
            long createdDate = teamsConfig.getLong("teams." + key + ".created_date");
            boolean friendlyFire = teamsConfig.getBoolean("teams." + key + ".friendly_fire");
            ItemStack banner = null;
            if(teamsConfig.contains("teams." + key + ".banner") && !teamsConfig.getString("teams." + key + ".banner").equals("null")) {
            	Map<String, Object> bannerData = (Map<String, Object>) teamsConfig.getConfigurationSection("teams." + key + ".banner").getValues(false);
            	banner = ItemStack.deserialize(bannerData);
            }
            Map<String, List<Permission>> permissions = new HashMap<>();
            ConfigurationSection permissionsSection = teamsConfig.getConfigurationSection("teams." + key + ".permissions");
            if(permissionsSection != null) {
                Map<String, Object> entriesRaw = permissionsSection.getValues(false);
                for (String s : entriesRaw.keySet()) {
                    List<?> permissionsRaw = (List<?>) entriesRaw.get(s);
                    List<Permission> perms = new ArrayList<>();
                    for(Object v : permissionsRaw) {
                        if(v instanceof String) {
                            perms.add(Permission.getPermission((String) v));
                        }
                    }
                    permissions.put(s, perms);
                }
            }
            
            List<Invite> invites = new ArrayList<>();
            if(teamsConfig.contains("teams." + key + ".invites") && !teamsConfig.getString("teams." + key + ".invites").equals("null")) {
				for(String inviteKey : teamsConfig.getConfigurationSection("teams." + key + ".invites").getKeys(false)) {
				    UUID inviter = UUID.fromString(teamsConfig.getString("teams." + key + ".invites." + inviteKey + ".inviter"));
				    UUID invitee = UUID.fromString(teamsConfig.getString("teams." + key + ".invites." + inviteKey + ".invitee"));
				    int timeBeforeExpiry = teamsConfig.getInt("teams." + key + ".invites." + inviteKey + ".timeBeforeExpiry");
				    long timestamp = teamsConfig.getLong("teams." + key + ".invites." + inviteKey + ".timestamp");
				
				    invites.add(new Invite(inviter, invitee, timeBeforeExpiry, timestamp));
				}
            }

            teams.add(new Team(UUID.fromString(key), name, leader, members, claimBlocks, createdDate, banner, permissions, invites, friendlyFire));
        }
    }
    
    private void loadPlayers(File playersDir) {
        for(File playerFile : playersDir.listFiles()) {
            if(!playerFile.getName().endsWith(".yml")) continue;
            
            FileConfiguration playerConfig = YamlConfiguration.loadConfiguration(playerFile);
            String key = playerFile.getName().replace(".yml", "");
            
            List<String> team = playerConfig.getStringList("team");
            List<UUID> teamUUIDs = new ArrayList<>();
            for(String t : team) {
                teamUUIDs.add(UUID.fromString(t));
            }
            
            int claimBlocks = playerConfig.getInt("claim_blocks");
            long play_time = playerConfig.getLong("play_time");
            
            ItemStack skull = null;
            
            if(playerConfig.contains("skull") && !playerConfig.getString("skull").equals("null")) {
                Map<String, Object> skullData = (Map<String, Object>) playerConfig.getConfigurationSection("skull").getValues(false);
                skull = ItemStack.deserialize(skullData);
            }
            
            players.add(new PlayerData(UUID.fromString(key), teamUUIDs, claimBlocks, play_time, skull));
        }
    }


    private void loadClaimedLands() {
        for(String key : claimedLandsConfig.getConfigurationSection("claimed_lands").getKeys(false)) {
            UUID land_uuid = UUID.fromString(claimedLandsConfig.getString("claimed_lands." + key + ".uuid"));
            UUID region_uuid = UUID.fromString(claimedLandsConfig.getString("claimed_lands." + key + ".region_uuid"));
            String worldName = claimedLandsConfig.getString("claimed_lands." + key + ".world_name");
            String[] coords = key.split("_");
            int x = Integer.parseInt(coords[0]);
            int z = Integer.parseInt(coords[1]);

            claimedLands.add(new ClaimedLand(land_uuid, region_uuid, worldName, x, z));
        }
    }
    
    private void loadRegions() {
        for(String key : regionsConfig.getConfigurationSection("regions").getKeys(false)) {
            UUID uuid = UUID.fromString(regionsConfig.getString("regions." + key + ".uuid"));
            UUID teamUUID = UUID.fromString(regionsConfig.getString("regions." + key + ".team_uuid"));
            String name = null;
            if(!regionsConfig.getString("regions." + key + ".name").equals("null")) name = regionsConfig.getString("regions." + key + ".name");
            Location spawn = null;
            if(!regionsConfig.getString("regions." + key + ".spawn").equals("null")) spawn = parseStringToLoc(regionsConfig.getString("regions." + key + ".spawn"));
            ItemStack icon = null;
            if(!regionsConfig.getString("regions." + key + ".icon").equals("null")) {
            	Map<String, Object> iconData = (Map<String, Object>) regionsConfig.getConfigurationSection("regions." + key + ".icon").getValues(false);
                icon = ItemStack.deserialize(iconData);
            }

            List<UUID> lands = new ArrayList<>();
            if(regionsConfig.contains("regions." + key + ".lands")) {
                List<String> landUUIDStrings = regionsConfig.getStringList("regions." + key + ".lands");
                for(String landUUIDString : landUUIDStrings) {
                    lands.add(UUID.fromString(landUUIDString));
                }
            }
            
            Map<Property, Boolean> properties = new HashMap<>();
            if(regionsConfig.contains("regions." + key + ".properties")) {
                List<String> propertiesRaw = regionsConfig.getStringList("regions." + key + ".properties");
                for(String prop : propertiesRaw) {
                	boolean bo = Boolean.valueOf(prop.split(":")[1]);
                	properties.put(Property.getProperty(prop.split(":")[0]), bo);
                }
            }

            Map<String, List<Permission>> perms = new HashMap<>();
            if(regionsConfig.contains("regions." + key + ".perms")) {
                for(String permKey : regionsConfig.getConfigurationSection("regions." + key + ".perms").getKeys(false)) {
                    List<String> permissionList = regionsConfig.getStringList("regions." + key + ".perms." + permKey);
                    List<Permission> permissions = new ArrayList<>();
                    for(String s : permissionList) {
                    	permissions.add(Permission.getPermission(s));
                    }
                    perms.put(permKey, permissions);
                }
            }

            List<String> titles = regionsConfig.getStringList("regions." + key + ".titles");
            
            for(Property pr : Property.values()) {
            	if(!properties.containsKey(pr)) {
            		properties.put(pr, false);
            	}
            }

            Region region = new Region(uuid, teamUUID, lands, name, spawn, perms, properties, titles, icon);
            regions.add(region);
        }
    }

    public void saveTeams() throws IOException {
        Map<String, Object> validEntries = new HashMap<>();
        for(Team team : teams) {
            String key = team.getUUID().toString();
            List<String> uuids = new ArrayList<String>();
            for(UUID u : team.getMembers()) {
            	uuids.add(u.toString());
            }
            validEntries.put("teams." + key + ".name", team.getName());
            validEntries.put("teams." + key + ".leader", team.getLeader().toString());
            validEntries.put("teams." + key + ".members", uuids);
            validEntries.put("teams." + key + ".claim_blocks", team.getClaimBlocks());
            validEntries.put("teams." + key + ".created_date", team.getCreatedDate());
            validEntries.put("teams." + key + ".friendly_fire", team.isFriendlyFire());
            if(team.getBanner() != null) validEntries.put("teams." + key + ".banner", team.getBanner().serialize());
            if(team.getBanner() == null) validEntries.put("teams." + key + ".banner", "null");
            
            if (team.getPerms() == null || team.getPerms().isEmpty()) {
                validEntries.put("teams." + key + ".permissions", "null");
            } else {
                for (String s : team.getPerms().keySet()) {
                	List<Permission> perms = team.getPerms().get(s);
                	List<String> perms_string = perms.stream().map(Permission::getName).collect(Collectors.toList());
                    validEntries.put("teams." + key + ".permissions." + s, perms_string);
                }
            }
            
            if(team.getInvites() == null || team.getInvites().isEmpty()) {
                validEntries.put("teams." + key + ".invites", "null");
            }
            if(team.getInvites() != null) {
                int i = 1;
                for(Invite invite : team.getInvites()) {
                    String invitePath = "teams." + key + ".invites." + i;
                    validEntries.put(invitePath + ".inviter", invite.getInviter().toString());
                    validEntries.put(invitePath + ".invitee", invite.getInvitee().toString());
                    validEntries.put(invitePath + ".timeBeforeExpiry", invite.getTimeBeforeExpiry());
                    validEntries.put(invitePath + ".timestamp", invite.getTimestamp());
                    i++;
                }
            }
        }

        for(String key : teamsConfig.getConfigurationSection("teams").getKeys(false)) {
            if(!validEntries.containsKey("teams." + key + ".name")) {
                teamsConfig.set("teams." + key, null);
            }
        }

        for(Map.Entry<String, Object> entry : validEntries.entrySet()) {
            teamsConfig.set(entry.getKey(), entry.getValue());
        }
        
        teamsConfig.save(teamsFile);
    }
    
    public void savePlayers() throws IOException {
        File playersDir = new File(plugin.getDataFolder(), "players");
        
        for(PlayerData player : players) {
            String key = player.getUUID().toString();
            File playerFile = new File(playersDir, key + ".yml");
            FileConfiguration playerConfig = YamlConfiguration.loadConfiguration(playerFile);
            
            playerConfig.set("team", player.getTeamUUIDs() != null ? player.getTeamUUIDs().stream().map(UUID::toString).collect(Collectors.toList()) : null);
            playerConfig.set("claim_blocks", player.getClaimBlocks());
            playerConfig.set("play_time", player.getPlayTime());
            
            if(player.getSkull() != null) {
                playerConfig.set("skull", player.getSkull().serialize());
            } else {
                playerConfig.set("skull", null);
            }
            
            playerConfig.save(playerFile);
        }
    }


    public void saveClaimedLands() throws IOException {
        Map<String, Object> validEntries = new HashMap<>();
        for(ClaimedLand land : claimedLands) {
            String key = land.getX() + "_" + land.getZ();
            validEntries.put("claimed_lands." + key + ".uuid", land.getUUID().toString());
            validEntries.put("claimed_lands." + key + ".region_uuid", land.getRegionUUID().toString());
            validEntries.put("claimed_lands." + key + ".world_name", land.getWorldName());
        }

        for(String key : claimedLandsConfig.getConfigurationSection("claimed_lands").getKeys(false)) {
            if(!validEntries.containsKey("claimed_lands." + key + ".team_uuid")) {
                claimedLandsConfig.set("claimed_lands." + key, null);
            }
        }

        for(Map.Entry<String, Object> entry : validEntries.entrySet()) {
            claimedLandsConfig.set(entry.getKey(), entry.getValue());
        }
        
        claimedLandsConfig.save(claimedLandsFile);
    }
    
    public void saveRegions() throws IOException {
        Map<String, Object> validEntries = new HashMap<>();

        for(Region region : regions) {
            String key = region.getUUID().toString();
            validEntries.put("regions." + key + ".uuid", region.getUUID().toString());
            validEntries.put("regions." + key + ".team_uuid", region.getTeamUUID().toString());
            if(region.getName() == null) validEntries.put("regions." + key + ".name", "null"); 
            if(region.getName() != null) validEntries.put("regions." + key + ".name", region.getName()); 
            if(region.getSpawn() == null) validEntries.put("regions." + key + ".spawn", "null");
            if(region.getSpawn() != null) validEntries.put("regions." + key + ".spawn", unparseLocToString(region.getSpawn()));
            if(region.getIcon() == null) validEntries.put("regions." + key + ".icon", "null");
            if(region.getIcon() != null) validEntries.put("regions." + key + ".icon", region.getIcon().serialize());

            List<String> landUUIDStrings = new ArrayList<>();
            for(UUID landUUID : region.getLands()) {
                landUUIDStrings.add(landUUID.toString());
            }
            validEntries.put("regions." + key + ".lands", landUUIDStrings);

            for(Map.Entry<String, List<Permission>> entry : region.getPerms().entrySet()) {
                String permKey = "regions." + key + ".perms." + entry.getKey();
                List<String> permissionsAsString = new ArrayList<>();
                for(Permission permission : entry.getValue()) {
                    permissionsAsString.add(permission.getName());
                }
                validEntries.put(permKey, permissionsAsString);
            }

            validEntries.put("regions." + key + ".titles", region.getTitles());
            
            if(region.getProperties() == null || region.getProperties().isEmpty()) {
                validEntries.put("regions." + key + ".properties", "null");
            } else {
            	List<String> properties = new ArrayList<>();
                for(Entry<Property, Boolean> s : region.getProperties().entrySet()) {
                    properties.add(s.getKey().getName()+":"+s.getValue().toString());
                }
                validEntries.put("regions." + key + ".properties", properties);
            }
        }

        if(regionsConfig.contains("regions")) {
            for(String key : regionsConfig.getConfigurationSection("regions").getKeys(false)) {
                if(!validEntries.containsKey("regions." + key + ".uuid")) {
                	regionsConfig.set("regions." + key, null);
                }
            }
        }

        for(Map.Entry<String, Object> entry : validEntries.entrySet()) {
            regionsConfig.set(entry.getKey(), entry.getValue());
        }

        regionsConfig.save(regionsFile);
    }



    public List<Team> getTeams() {
        return teams;
    }

    public List<PlayerData> getPlayers() {
        return players;
    }

    public List<ClaimedLand> getClaimedLands() {
        return claimedLands;
    }
    
    public List<Region> getRegions() {
        return regions;
    }
    
    public Location parseStringToLoc(String string) {
		String[] parsedLoc = string.split(",");
		double x = truncateDecimal(Double.valueOf(parsedLoc[0]));
		double y = truncateDecimal(Double.valueOf(parsedLoc[1]));
		double z = truncateDecimal(Double.valueOf(parsedLoc[2]));
		float yaw = (float) truncateDecimal(Double.valueOf(parsedLoc[3]));
		float pitch = (float) truncateDecimal(Double.valueOf(parsedLoc[4]));
		World world = Bukkit.getWorld(parsedLoc[5]);
		
		WorldCreator load = new WorldCreator(parsedLoc[5]);
		world = load.createWorld();
		if(world != null){
			Location g = new Location(world, x, y, z, yaw, pitch);
			return g;
		}
		return null;
	}
	
	public String unparseLocToString(Location loc) {
		return truncateDecimal(loc.getX())+","+truncateDecimal(loc.getY())+","+truncateDecimal(loc.getZ())+","+truncateDecimal(loc.getYaw())+","+truncateDecimal(loc.getPitch())+","+loc.getWorld().getName();
	}
	
	public double truncateDecimal(double x) {
		DecimalFormat numberFormat = new DecimalFormat("#.0");
		return Double.valueOf(numberFormat.format(x));
    }
}


