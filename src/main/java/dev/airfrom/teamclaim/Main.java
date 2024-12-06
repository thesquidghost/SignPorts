package dev.airfrom.teamclaim;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.profile.PlayerProfile;
import org.bukkit.profile.PlayerTextures;

import dev.airfrom.teamclaim.Main;
import dev.airfrom.teamclaim.commands.Commands;
import dev.airfrom.teamclaim.commands.TeamCmd;
import dev.airfrom.teamclaim.data.ClaimedLand;
import dev.airfrom.teamclaim.data.DataManager;
import dev.airfrom.teamclaim.data.GUI;
import dev.airfrom.teamclaim.data.Invite;
import dev.airfrom.teamclaim.data.Permission;
import dev.airfrom.teamclaim.data.PlayerData;
import dev.airfrom.teamclaim.data.Region;
import dev.airfrom.teamclaim.data.Team;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;

public class Main extends JavaPlugin {
	
	public final String ANSI_RESET = "\u001B[0m";
	public final String ANSI_BLACK = "\u001B[30m";
	public final String ANSI_RED = "\u001B[31m";
	public final String ANSI_GREEN = "\u001B[32m";
	public final String ANSI_YELLOW = "\u001B[33m";
	public final String ANSI_WHITE_BACKGROUND = "\u001B[47m";

	public String console_pre = "["+getName()+"] ";
		
	public String prefix;
	public String help_prefix;
	public String err_prefix;
	public String prefix_color;
	public String err_color;
	
	public String welcome_default_title;
	public String welcome_default_subtitle;
	public String exit_default_title;
	public String exit_default_subtitle;

	public ItemStack claim_block;
	public int claim_block_rate;
	public int invite_expiry_time;
	public ItemStack right_arrow;
	public ItemStack left_arrow;
	public ItemStack global_skull;
	
	public Map<UUID, Long> timeTracking = new HashMap<>();
	public Map<String, ClaimedLand> claimedLandCache = new HashMap<>();
	public Map<UUID, Integer> inClaimCheck = new HashMap<>();	
	public Map<UUID, Long> lastParticleShowTime = new HashMap<>();
	public Map<UUID, String> settingParameter = new HashMap<>();
	public Map<UUID, List<String>> settingClaimArea = new HashMap<>();
	
	public String claimBlockKey = "claim_block";
	public String claimCheckToolKey = "claim_check_tool";
	public String GUIItem = "GUI_item";
	
	public NamespacedKey GUIItemkey = new NamespacedKey(this, GUIItem);
	
    public DataManager dataManager;
	
    private static final Class<?>[] PLUGIN_CLASSES = {
			Commands.class,
			Events.class,
			ClaimedLand.class,
			DataManager.class,
			GUI.class,
			Permission.class,
			PlayerData.class,
			Region.class,
			Team.class
	    };
	
	@Override
    public void onLoad() {
        ClassLoader classLoader = getClassLoader();
        for (Class<?> pluginClass : PLUGIN_CLASSES) {
            try {
                classLoader.loadClass(pluginClass.getName());
            } catch (ClassNotFoundException e) {
                getLogger().warning("Failed to load plugin class " + pluginClass.getName());
                e.printStackTrace();
            }
        }
    }
    
	@Override
	public void onEnable() {
		//DO NOT DELETE NOR EDIT THIS BLOCK
		on();
		//DO NOT DELETE NOR EDIT THIS BLOCK
		saveDefaultConfig();
		registerEvents();
		registerCommands(); 
		
		initConfigVar();
		
		dataManager = new DataManager(this);
		
		for(Player pl : Bukkit.getOnlinePlayers()) {
			PlayerData pData = dataManager.getPlayers().stream().filter(tl -> tl.getUUID().equals(pl.getUniqueId())).findFirst().orElse(null);
			if(pData == null) {
				ItemStack skull = new ItemStack(Material.PLAYER_HEAD);
				SkullMeta i = (SkullMeta) skull.getItemMeta();
				i.setOwningPlayer(pl);
				skull.setItemMeta(i);
				dataManager.getPlayers().add(new PlayerData(pl.getUniqueId(), null, 0, 0, skull));
			}
			if(!timeTracking.containsKey(pl.getUniqueId())) {
				timeTracking.put(pl.getUniqueId(), System.currentTimeMillis());
			}
		}
        
		runTask();
        super.onEnable();
	}
	
	public void registerEvents() {
		getServer().getPluginManager().registerEvents(new Events(this), this);
	}
	
	public void registerCommands() {
		getCommand("team").setExecutor(new TeamCmd(this));
		getCommand("t").setExecutor(new TeamCmd(this));
		getCommand("teamh").setExecutor(new TeamCmd(this));
		getCommand("th").setExecutor(new TeamCmd(this));
		getCommand("land").setExecutor(new Commands(this));
		getCommand("claimblock").setExecutor(new Commands(this));
		getCommand("invite").setExecutor(new Commands(this));
	}

	@Override
	public void onDisable() {
		saveData();
			
		//DO NOT DELETE NOR EDIT THIS BLOCK
		off();
		//DO NOT DELETE NOR EDIT THIS BLOCK
		super.onDisable();
	}
	
	private void on() {
		//DO NOT REMOVE NOR EDIT THIS BLOCK
		Bukkit.getLogger().info(console_pre+"----------------------------------------------");
		Bukkit.getLogger().info(console_pre+ANSI_YELLOW+getDescription().getName()+" v"+getDescription().getVersion()+" "+ANSI_GREEN+"ENABLED"+ANSI_RESET+" - "+Bukkit.getBukkitVersion());
		Bukkit.getLogger().info(console_pre+ANSI_WHITE_BACKGROUND+ANSI_BLACK+"Made by MC-WISE.com"+ANSI_RESET);
		Bukkit.getLogger().info(console_pre+"----------------------------------------------");
		//
	}
	
	private void off() {
		//DO NOT REMOVE NOR EDIT THIS BLOCK
		Bukkit.getLogger().info(console_pre+"----------------------------------------------");
		Bukkit.getLogger().info(console_pre+ANSI_YELLOW+getDescription().getName()+" v"+getDescription().getVersion()+" "+ANSI_RED+"DISABLED"+ANSI_RESET+" - "+Bukkit.getBukkitVersion());
		Bukkit.getLogger().info(console_pre+ANSI_WHITE_BACKGROUND+ANSI_BLACK+"Made by MC-WISE.com"+ANSI_RESET);
		Bukkit.getLogger().info(console_pre+"----------------------------------------------");
		//
	}
	
	private void saveData() {
		try {
            dataManager.saveTeams();
            dataManager.savePlayers();
            dataManager.saveClaimedLands();
            dataManager.saveRegions();
        } catch (IOException e) {
            Bukkit.getLogger().severe(console_pre+"Could not save data: " + e.getMessage());
        }
	}
	
	private void runTask() {
	    int rewardIntervalMinutes = claim_block_rate;
	    getServer().getScheduler().runTaskTimer(this, () -> {
            long currentTimeMillis = System.currentTimeMillis();

            for(Player pl : getServer().getOnlinePlayers()) {
                UUID playerUUID = pl.getUniqueId();
                long joinTime = timeTracking.get(playerUUID);

                long timeElapsedSeconds = (currentTimeMillis - joinTime) / 1000;
                if(timeElapsedSeconds >= 60) {
                    long playTimeMillis = currentTimeMillis - joinTime;
                    long playTimeMinutes = playTimeMillis / (1000 * 60);

					PlayerData pData = dataManager.getPlayers().stream().filter(tl -> tl.getUUID().equals(playerUUID)).findFirst().orElse(null);
					if(pData != null) {
						long newPlayTime = pData.getPlayTime()+playTimeMinutes;
						pData.setPlayTime(newPlayTime);
						if(Math.floor(newPlayTime) % rewardIntervalMinutes == 0) {
					    	giveClaimBlocks(pl, pData);
					    }
					}
                    timeTracking.put(playerUUID, currentTimeMillis);
                }
            }
            
            if(!inClaimCheck.isEmpty()) {
	            Iterator<UUID> iterator = inClaimCheck.keySet().iterator();
	            while(iterator.hasNext()) {
	                UUID key = iterator.next();
	        		int ti = inClaimCheck.get(key);
	        		if(ti > 0) inClaimCheck.replace(key, ti-1);
	        		if(ti == 0) {
	        			OfflinePlayer o = Bukkit.getOfflinePlayer(key);
	        			if(o != null && o.isOnline()) {
	        				((Player) o).sendMessage(prefix+"§cClaim checking has automatically been turned off!");
	        			}
	        			iterator.remove();
	        		}
	            }
            }
            
            for(Team t : dataManager.getTeams()) {
            	Map<Invite, Boolean> invites = new HashMap<>();
            	for(Invite i : t.getInvites()) {
            		if(t.getMembers().contains(i.getInvitee()) || t.getLeader().equals(i.getInvitee())) {
            			invites.put(i, false);
            		}
            		if((System.currentTimeMillis() - i.getTimestamp()) >= i.getTimeBeforeExpiry() * 60 * 1000) {
            			invites.put(i, true);
            		}
            	}
            	for(Entry<Invite, Boolean> i : invites.entrySet()) {
            		t.getInvites().remove(i.getKey());
            		if(i.getValue()) {
	        			OfflinePlayer o1 = Bukkit.getOfflinePlayer(i.getKey().getInviter());
						OfflinePlayer o2 = Bukkit.getOfflinePlayer(i.getKey().getInvitee());
						if(o1.isOnline()) ((Player) o1).sendMessage(prefix+"Your invite to §f"+o2.getName()+prefix_color+" to join §f"+t.getName()+prefix_color+" has expired!");
						if(o2.isOnline()) ((Player) o2).sendMessage(prefix+"The invite from §f"+o1.getName()+prefix_color+" to join §f"+t.getName()+prefix_color+" has expired!");
            		}
            	}
            }
        }, 0L, 20L);
	    
	    getServer().getScheduler().runTaskTimer(this, () -> {
	    	if(!inClaimCheck.isEmpty()) {
	            for(Entry<UUID, Integer> u : inClaimCheck.entrySet()) {
	            	OfflinePlayer pl = Bukkit.getOfflinePlayer(u.getKey());
	            	if(pl != null && pl.isOnline()) {
	            		Location l = pl.getLocation();
	            		int x = l.getBlockX();
	            		int z = l.getBlockZ();
	            		World w = l.getWorld();
	            		ClaimedLand c = dataManager.getClaimedLands().stream().filter(cl -> cl.getX() == x && cl.getZ() == z && Bukkit.getWorld(cl.getWorldName()) == w).findFirst().orElse(null);
	        			if(c != null) {
	        				Region r = dataManager.getRegions().stream().filter(cl -> cl.getUUID().equals(c.getRegionUUID())).findFirst().orElse(null);
	        				Team t = dataManager.getTeams().stream().filter(m -> m.getUUID().equals(r.getTeamUUID())).findFirst().orElse(null);
	        				TextComponent textcom = new TextComponent("§cClaimed land: "+t.getName());
			        		((Player) pl).spigot().sendMessage(ChatMessageType.ACTION_BAR, new BaseComponent[]{textcom});
	        			} else {
	        				TextComponent textcom = new TextComponent("§2Wilderness");
			        		((Player) pl).spigot().sendMessage(ChatMessageType.ACTION_BAR, new BaseComponent[]{textcom});
	        			}
	            	}
	            }
	    	}
        }, 0L, 2L);
	    
	    getServer().getScheduler().runTaskTimer(this, () -> {
            saveData();
        }, 0, 20*60);
	}
	
	public void giveClaimBlocks(Player p, PlayerData pData) {
		pData.setClaimBlocks(pData.getClaimBlocks()+claim_block.getAmount());
		TextComponent textcom = new TextComponent("+"+claim_block.getAmount()+" "+claim_block.getItemMeta().getDisplayName());
		p.spigot().sendMessage(ChatMessageType.ACTION_BAR, new BaseComponent[]{textcom});
	}
	
	private void initConfigVar() {
		//chat prefix
		prefix = getConfig().getString("prefix");
		help_prefix = getConfig().getString("help_prefix");
		err_prefix = getConfig().getString("err_prefix");
		
		welcome_default_title = getConfig().getString("welcome_default_title");
		welcome_default_subtitle = getConfig().getString("welcome_default_subtitle");
		exit_default_title = getConfig().getString("exit_default_title");
		exit_default_subtitle = getConfig().getString("exit_default_subtitle");
		
		String claim_block_name = getConfig().getString("claim_block_name");
		List<String> claim_block_lore_uncolored = getConfig().getStringList("claim_block_lore");
		List<String> claim_block_lore = new ArrayList<String>();
		
		if(prefix == null) prefix = "";
		if(help_prefix == null) help_prefix = "";
		if(err_prefix == null) err_prefix = "";
		if(welcome_default_title == null) welcome_default_title = "";
		if(welcome_default_subtitle == null) welcome_default_subtitle = "";
		if(exit_default_title == null) exit_default_title = "";
		if(exit_default_subtitle == null) exit_default_subtitle = "";
		if(claim_block_name == null) claim_block_name = "Claim Block";
		if(claim_block_lore_uncolored == null) claim_block_lore.add("");
				
		prefix = prefix.replaceAll("&", "§");
		help_prefix = help_prefix.replaceAll("&", "§");
		err_prefix = err_prefix.replaceAll("&", "§");
		
		prefix_color = "§"+getColorCharacter(prefix);
		err_color = "§"+getColorCharacter(err_prefix);
		
		claim_block_name = claim_block_name.replaceAll("&", "§");
		for(String s : claim_block_lore_uncolored) {
			claim_block_lore.add(s.replaceAll("&", "§"));
		}
		
		boolean isEnchanted = false;
		
		String cb_material = getConfig().getString("claim_block");
		
		if(cb_material != null && (cb_material.endsWith(":enchanted") || cb_material.endsWith(":ENCHANTED"))) {
			isEnchanted = true;
			cb_material = cb_material.replace(":enchanted", "");
			cb_material = cb_material.replace(":ENCHANTED", "");
		}
		Material claim_block_material = null;
		try {
			claim_block_material = Material.valueOf(cb_material);
        } catch (IllegalArgumentException e) {
            Bukkit.getLogger().info(console_pre+"Material not found: " + cb_material);
        }
		int claim_block_amount = getConfig().getInt("claim_block_amount");
		
		if(claim_block_material != null && claim_block_amount > 0 && claim_block_name != null) {
			claim_block = new ItemStack(claim_block_material, claim_block_amount);
			ItemMeta im = claim_block.getItemMeta();
			im.setDisplayName(claim_block_name);
			im.setLore(claim_block_lore);
			if(isEnchanted) im.addEnchant(Enchantment.POWER, 1, false);
			im.addItemFlags(ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_ATTRIBUTES);
			claim_block.setItemMeta(im);
		} else {
			claim_block = new ItemStack(Material.STONE, 1);
			ItemMeta im = claim_block.getItemMeta();
			im.setDisplayName("Claim Block");
			im.addEnchant(Enchantment.POWER, 1, false);
			im.addItemFlags(ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_ATTRIBUTES);
			claim_block.setItemMeta(im);
		}
		
		claim_block_rate = getConfig().getInt("claim_block_rate");
		invite_expiry_time = getConfig().getInt("invite_expiry_time");
		
		String right_arrow_link = getConfig().getString("right_arrow");
		String left_arrow_link = getConfig().getString("left_arrow");
		String global_link = getConfig().getString("global_skull");
		
		try {
			right_arrow = getCustomSkull(new URL(right_arrow_link), "§3§lNext page", "team_right_arrow");
			left_arrow = getCustomSkull(new URL(left_arrow_link), "§3§lPrevious page", "team_left_arrow");
			global_skull = getCustomSkull(new URL(global_link), "§3Global - Others", "");
		} catch (IOException e) {
			Bukkit.getLogger().severe(console_pre+"Could not load skins from config: " + e.getMessage());
		}
	}
	
	public static Character getColorCharacter(String message) {
        int index = message.lastIndexOf('§');
        if (index != -1 && index + 1 < message.length()) {
            return message.charAt(index + 1);
        }
        return null;
	 }
	
	public boolean isClaimBlock(ItemStack i) {
		if(i.hasItemMeta()) {
			NamespacedKey key = new NamespacedKey(this, claimBlockKey);
			return i.getItemMeta().getPersistentDataContainer().has(key, PersistentDataType.BOOLEAN);
		}
		return false;
	}
	
	public boolean isClaimTool(ItemStack i) {
		if(i.hasItemMeta()) {
			NamespacedKey key = new NamespacedKey(this, claimCheckToolKey);
			return i.getItemMeta().getPersistentDataContainer().has(key, PersistentDataType.BOOLEAN);
		}
		return false;
	}
	
	public boolean isGUIItem(ItemStack i) {
		if(i.hasItemMeta()) {
			return i.getItemMeta().getPersistentDataContainer().has(GUIItemkey, PersistentDataType.STRING);
		}
		return false;
	}
	
	public boolean isGUIItemTag(ItemStack i, String s) {
		if(i.hasItemMeta()) {
			if(i.getItemMeta().getPersistentDataContainer().has(GUIItemkey, PersistentDataType.STRING)) {
				return i.getItemMeta().getPersistentDataContainer().get(GUIItemkey, PersistentDataType.STRING).equals(s);
			}
		}
		return false;
	}
	
	public boolean containsSpecialCharacter(String input) {
        String specialCharacters = "[^a-zA-Z0-9]";
        return input != null && input.matches(".*" + specialCharacters + ".*");
    }
    
	public  ItemStack getCustomSkull(URL url, String name, String tag) throws IOException {
        PlayerProfile profile = Bukkit.createPlayerProfile(UUID.randomUUID(), null);
        PlayerTextures textures = profile.getTextures();
        textures.setSkin(url);
        profile.setTextures(textures);

        ItemStack skull = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta skullMeta = (SkullMeta) skull.getItemMeta();
        skullMeta.setDisplayName(name);
        skullMeta.getPersistentDataContainer().set(GUIItemkey, PersistentDataType.STRING, tag);
        assert skullMeta != null;

        skullMeta.setOwnerProfile(profile);
        skull.setItemMeta(skullMeta);

        return skull;
    }
	
	public void removeInvite(Team t, Map<UUID, UUID> invited, Player p) {
		Invite i = null;
		for(Invite inv : t.getInvites()) {
			if(inv.getInviter().equals(invited.get(p.getUniqueId())) && inv.getInvitee().equals(p.getUniqueId())) {
				i = inv;
			}
		}
		t.getInvites().remove(i);
		return;
	}
	
}
