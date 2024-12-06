package dev.airfrom.teamclaim.data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BannerMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.persistence.PersistentDataType;

import dev.airfrom.teamclaim.Main;

public class GUI {
	
	private Main main;
	
	public GUI(Main main) {
		this.main = main;
	}
	
	public void openLandGUI(Player p) {
		Inventory inv = Bukkit.createInventory(null, 3*9,"Land Property");
		
		for(int i = 0;i<27;i++) {
			if(i != 13 && i != 18) {
				inv.setItem(i, getGlassPane());
			}
		}
		
		ArrayList<String> lore = new ArrayList<String>();
		
		Location l = p.getLocation();
		int f = l.getBlockX();
		int g = l.getBlockZ();
		World w = l.getWorld();
		ClaimedLand c = main.dataManager.getClaimedLands().stream().filter(cl -> cl.getX() == f && cl.getZ() == g && Bukkit.getWorld(cl.getWorldName()) == w).findFirst().orElse(null);
		
		if(c != null) {
			Region r = main.dataManager.getRegions().stream().filter(cl -> cl.getUUID().equals(c.getRegionUUID())).findFirst().orElse(null);
			Team t = main.dataManager.getTeams().stream().filter(tl -> tl.getUUID().equals(r.getTeamUUID())).findFirst().orElse(null);
			ItemStack banner = new ItemStack(Material.WHITE_BANNER, 1);
			if(t.getBanner() != null) banner = t.getBanner().clone();
			BannerMeta bannerMeta = (BannerMeta) banner.getItemMeta();
			lore.clear();
			lore.add(" ");
			if(t.getLeader().equals(p.getUniqueId()) || t.playerHas(p.getUniqueId(), Permission.TEAM_MANAGE_CLAIMS)) {
			    bannerMeta.setDisplayName("§cUnclaim your land");
			    lore.add("§7Click to unclaim this land!");
			    bannerMeta.getPersistentDataContainer().set(main.GUIItemkey, PersistentDataType.STRING, "unclaim_land");
			} else {
			    bannerMeta.setDisplayName("§cClaimed Land");
			    lore.add("§cOwner: " + t.getName());
			    bannerMeta.getPersistentDataContainer().set(main.GUIItemkey, PersistentDataType.STRING, "claimed_land");
			}
			bannerMeta.setLore(lore);
			bannerMeta.addItemFlags(ItemFlag.HIDE_DYE, ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ADDITIONAL_TOOLTIP);
			banner.setItemMeta(bannerMeta);
			inv.setItem(13, banner);
		} else {
			inv.setItem(13, getGUIItem(Material.SHORT_GRASS, "§2Wilderness", 1, "wilderness_land"));
		}
		inv.setItem(18, getGUIItem(Material.BARRIER, "§cClose", 1, "close"));
		p.openInventory(inv);
	}
	
	public void openTeamGUI(Player p, Team t) {
		Inventory inv = Bukkit.createInventory(null, 3*9, "Team "+t.getName());
		
		for(int i = 0;i<27;i++) {
			if(i != 11 && i != 13 && i != 15) {
				inv.setItem(i, getGlassPane());
			}
		}
		
		if(t.getLeader().equals(p.getUniqueId()) || t.playerHas(p.getUniqueId(), Permission.TEAM_MANAGE_CLAIMS)) inv.setItem(11, getGUIItem(Material.GRASS_BLOCK, "§3§lClaim Management", Arrays.asList("§7› Manage Claims", " ", "§7Click to manage"), false, "claim_management_info"));
		if(!t.getLeader().equals(p.getUniqueId()) && !t.playerHas(p.getUniqueId(), Permission.TEAM_MANAGE_CLAIMS)) inv.setItem(11, getGUIItem(Material.GRASS_BLOCK, "§3§lClaim Management", Arrays.asList("§7› View Claims", " ", "§7Click to view"), false, "claim_management_info"));
		
		ItemStack i = new ItemStack(Material.WHITE_BANNER);
		if(t.getBanner() != null) {i = t.getBanner().clone();}
		ItemMeta im = i.getItemMeta();
		im.setDisplayName("§3§lTeam Management");
		im.addItemFlags(ItemFlag.HIDE_DYE, ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ADDITIONAL_TOOLTIP);
		
		if(t.getLeader().equals(p.getUniqueId())) {
			im.setLore(Arrays.asList("§7› Manage Members", "§7› Manage Permissions", "§7› Manage Invites", " ", "§7Click to manage"));
		} else if(t.playerHas(p.getUniqueId(), Permission.TEAM_MANAGE_INVITES)) {
			im.setLore(Arrays.asList("§7› Get Team information", "§7› Manage Invites", " ", "§7Click to view"));
		} else {
			im.setLore(Arrays.asList("§7› Get Team information", " ", "§7Click to view"));
		}
		
	    im.getPersistentDataContainer().set(main.GUIItemkey, PersistentDataType.STRING, "team_management_info");
		i.setItemMeta(im);
		inv.setItem(13, i);
		
		if(t.getLeader().equals(p.getUniqueId()) || t.playerHas(p.getUniqueId(), Permission.TEAM_WITHDRAW_CLAIM_BLOCKS)) inv.setItem(15, getGUIItem(main.claim_block.getType(), "§3§lTeam Claim Blocks", Arrays.asList("§7› Deposit Claim Blocks", "§7› Withdraw Claim Blocks"), false, "team_claim_blocks"));
		if(!t.getLeader().equals(p.getUniqueId()) && !t.playerHas(p.getUniqueId(), Permission.TEAM_WITHDRAW_CLAIM_BLOCKS)) inv.setItem(15, getGUIItem(main.claim_block.getType(), "§3§lTeam Claim Blocks", Arrays.asList("§7› Deposit Claim Blocks"), false, "team_claim_blocks"));
		
		p.openInventory(inv);
	}
	
	public void openTeamsGUI(Player p, PlayerData pData, int page) {		
		HashMap<Integer, ItemStack> slots = new HashMap<>();
		int c = (page-1)*18;
		for(int i = 0;i<27;i++) {
			if(i <= 17) {
				if(c < pData.getTeamUUIDs().size()) {
					UUID uuid = pData.getTeamUUIDs().get(c);
					Team t = main.dataManager.getTeams().stream().filter(tl -> tl.getUUID().equals(uuid)).findFirst().orElse(null);
					if(t != null) {
						ItemStack banner = new ItemStack(Material.WHITE_BANNER);
						if(t.getBanner() != null) {banner = t.getBanner().clone();}
						ItemMeta im = banner.getItemMeta();
						ChatColor ch = null;
						ch = getChatColor(banner.getType());
						im.setDisplayName("§r"+ch+"§l"+t.getName());
						im.addItemFlags(ItemFlag.HIDE_DYE, ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ADDITIONAL_TOOLTIP);
						im.setLore(Arrays.asList("§7› Click to view"));
					    im.getPersistentDataContainer().set(main.GUIItemkey, PersistentDataType.STRING, "team_"+uuid.toString());
						banner.setItemMeta(im);
						
						slots.put(i, banner);
						c++;
					}
				}
			} else {
				slots.put(i, getGlassPane());
			}
		}
		double mc = (pData.getTeamUUIDs().size()+1) / 20f;
		int maxPage = (int) Math.ceil(mc);
		if(maxPage == 0) maxPage = 1;
		Inventory inv = Bukkit.createInventory(null, 3*9, "Teams "+page+"/"+maxPage);
		
		for(Entry<Integer, ItemStack> slot : slots.entrySet()) {
			inv.setItem(slot.getKey(), slot.getValue());
		}
		
		if(maxPage > 1 && page != maxPage) {
			inv.setItem(26, main.right_arrow);
		}
		
		if(page != 1) {
			inv.setItem(25, main.left_arrow);
		}
		
		inv.setItem(18, getGUIItem(Material.BARRIER, "§cClose", Arrays.asList("§7Click to close"), false, "close"));
		
		p.openInventory(inv);
	}
	
	public void openClaimBlockGUI(Player p, PlayerData pData, Team t) {
		int totalCB = t.getClaimBlocks()+pData.getClaimBlocks();
		Inventory inv = Bukkit.createInventory(null, 3*9, t.getName()+" Claim Blocks ("+t.getClaimBlocks()+"/"+totalCB+")");
		Set<Integer> slot = new HashSet<>(Arrays.asList(13));
		if(t.getLeader().equals(pData.getUUID()) || t.playerHas(pData.getUUID(), Permission.TEAM_WITHDRAW_CLAIM_BLOCKS)) slot = new HashSet<>(Arrays.asList(12, 14));
		
		for(int i = 0;i<27;i++) {
			inv.setItem(i, getGlassPane());
		}
		
		if(slot.size() == 1) {
			inv.setItem(13, getGUIItem(Material.EMERALD, "§a§lDeposit", Arrays.asList("§7Click to deposit Claim Blocks"), false, "deposit_claim_block"));
		} else {
			inv.setItem(12, getGUIItem(Material.REDSTONE, "§c§lWithdraw", Arrays.asList("§7Click to withdraw Claim Blocks"), false, "withdraw_claim_block"));
			inv.setItem(14, getGUIItem(Material.EMERALD, "§a§lDeposit", Arrays.asList("§7Click to deposit Claim Blocks"), false, "deposit_claim_block"));
		}
		
		inv.setItem(18, getGUIItem(Material.BARRIER, "§cBack to team menu", Arrays.asList("§7Click to go back to team menu"), false, "close"));
		
		p.openInventory(inv);
	}
	
	public void openClaimBlockGUI2(Player p, PlayerData pData, Team t, String type) {
		int totalCB = t.getClaimBlocks()+pData.getClaimBlocks();
		Inventory inv = Bukkit.createInventory(null, 3*9, t.getName()+" "+type.substring(0, 1).toUpperCase()+type.substring(1).toLowerCase()+" ("+t.getClaimBlocks()+"/"+totalCB+")");
		
		for(int i = 0;i<27;i++) {
			inv.setItem(i, getGlassPane());
		}
		
		inv.setItem(11, getGUIItem(Material.RED_STAINED_GLASS_PANE, "§cRemove 10", 10, "cb_remove_10"));
		inv.setItem(12, getGUIItem(Material.RED_STAINED_GLASS_PANE, "§cRemove 1", 1, "cb_remove_1"));
		
		if(type.equals("withdraw")) inv.setItem(13, getGUIItem(main.claim_block.getType(), "§c§lWithdraw", Arrays.asList("§6Amount: 1", "§7Click to withdraw 1 Claim Block"), false, "cb_withdraw"));
		if(type.equals("deposit")) inv.setItem(13, getGUIItem(main.claim_block.getType(), "§a§lDeposit", Arrays.asList("§6Amount: 1", "§7Click to deposit 1 Claim Block"), false, "cb_deposit"));
		
		inv.setItem(14, getGUIItem(Material.GREEN_STAINED_GLASS_PANE, "§aAdd 1", 1, "cb_add_1"));
		inv.setItem(15, getGUIItem(Material.GREEN_STAINED_GLASS_PANE, "§aAdd 10", 10, "cb_add_10"));			
		
		inv.setItem(18, getGUIItem(Material.BARRIER, "§cBack to team claim block menu", Arrays.asList("§7Click to go back to team claim block menu"), false, "close"));
		
		p.openInventory(inv);
	}
	
	public void openTeamManagementGUI(Player p, Team t, int page) {
		if(t.getLeader().equals(p.getUniqueId())) {
			HashMap<Integer, ItemStack> slots = new HashMap<>();
			int c = (page-1)*20;
			for(int i = 0;i<54;i++) {
				if(i >= 11 && i <= 15 || i >= 20 && i <= 24 ||  i >= 29 && i <= 33 || i >= 38 && i <= 42) {
					if(c < t.getMembers().size()) {
						UUID uuid = t.getMembers().get(c);
						PlayerData plData = main.dataManager.getPlayers().stream().filter(tl -> tl.getUUID().equals(uuid)).findFirst().orElse(null);
						ItemStack skull = null;
						if(plData.getSkull() == null) skull = new ItemStack(Material.PLAYER_HEAD);
						if(plData.getSkull() != null) skull = plData.getSkull().clone();
						SkullMeta m = (SkullMeta) skull.getItemMeta();
						m.setDisplayName("§3"+Bukkit.getOfflinePlayer(plData.getUUID()).getName());
						m.setLore(Arrays.asList("§7› Click to manage"));
						m.getPersistentDataContainer().set(main.GUIItemkey, PersistentDataType.STRING, "member_"+uuid.toString());
						skull.setItemMeta(m);
						slots.put(i, skull);
						c++;
					} else if(c == t.getMembers().size()) {
						ItemStack skull = main.global_skull.clone();
						SkullMeta m = (SkullMeta) skull.getItemMeta();
						m.setLore(Arrays.asList("§7› Click to manage"));
						m.getPersistentDataContainer().set(main.GUIItemkey, PersistentDataType.STRING, "member_global");
						skull.setItemMeta(m);
						slots.put(i, skull);
						c++;
					}
				} else {
					slots.put(i, getGlassPane());
				}
			}
			double mc = (t.getMembers().size()+1) / 20f;
			int maxPage = (int) Math.ceil(mc);
			if(maxPage == 0) maxPage = 1;
			Inventory inv = Bukkit.createInventory(null, 6*9, "Team Management "+t.getName()+" "+page+"/"+maxPage);
			
			for(Entry<Integer, ItemStack> slot : slots.entrySet()) {
				inv.setItem(slot.getKey(), slot.getValue());
			}
			
			if(maxPage > 1 && page != maxPage) {
				inv.setItem(51, main.right_arrow);
			}
			
			if(page != 1) {
				inv.setItem(47, main.left_arrow);
			}
			
			if(t.getBanner() == null) {
				List<String> lore = new ArrayList<>();
				lore = Arrays.asList("§7› Shift click to rename", "§7› Set new Team Banner");
				inv.setItem(4, getGUIItem(Material.WHITE_BANNER, "§r§f"+t.getName(), lore, false, "team_management_info"));
			}
			if(t.getBanner() != null) {
				ItemStack i = t.getBanner().clone();
				ItemMeta im = i.getItemMeta();
				ChatColor ch = null;
				ch = getChatColor(i.getType());
				im.setDisplayName("§r"+ch+t.getName());
				im.setLore(Arrays.asList("§7› Shift click to rename", "§7› Set new Team Banner"));
				im.addItemFlags(ItemFlag.HIDE_DYE, ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ADDITIONAL_TOOLTIP);
				im.getPersistentDataContainer().set(main.GUIItemkey, PersistentDataType.STRING, "team_management_info");
				i.setItemMeta(im);
				inv.setItem(4, i);
			}
			
			String status = "";
			if(t.isFriendlyFire()) status = "§aENABLED";
			if(!t.isFriendlyFire()) status = "§cDISABLED";
			inv.setItem(44, getGUIItem(Material.DIAMOND_SWORD, "§3§lToggle Team PVP", Arrays.asList("§7Click to toggle damaging members", "§7of your own team.", "§7Friendly-fire is "+status), false, "toggle_team_pvp"));
			inv.setItem(53, getGUIItem(Material.PAPER, "§3§lManage Invites", Arrays.asList("§7Click to manage team invites"), false, "invites_management"));
			inv.setItem(45, getGUIItem(Material.BARRIER, "§cBack to team menu", Arrays.asList("§7Click to go back to team menu"), false, "close"));
			
			p.openInventory(inv);
		} else if(t.playerHas(p.getUniqueId(), Permission.TEAM_MANAGE_INVITES)) {
			Inventory inv = Bukkit.createInventory(null, 3*9, "Team Management "+t.getName());
			for(int i = 0;i<27;i++) {
				inv.setItem(i, getGlassPane());
			}
			if(t.getBanner() == null) {
				List<String> lore = new ArrayList<>();
				lore = Arrays.asList("§7› Get Team information", " ", "§7Click to view");
				inv.setItem(12, getGUIItem(Material.WHITE_BANNER, "§r§f"+t.getName(), lore, false, "team_management_info"));
			}
			if(t.getBanner() != null) {
				ItemStack i = t.getBanner().clone();
				ItemMeta im = i.getItemMeta();
				ChatColor ch = null;
				ch = getChatColor(i.getType());
				im.setDisplayName("§r"+ch+t.getName());
				im.setLore(Arrays.asList("§7› Get Team information", " ", "§7Click to view"));
				im.addItemFlags(ItemFlag.HIDE_DYE, ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ADDITIONAL_TOOLTIP);
				im.getPersistentDataContainer().set(main.GUIItemkey, PersistentDataType.STRING, "team_management_info");
				i.setItemMeta(im);
				inv.setItem(12, i);
			}
			inv.setItem(14, getGUIItem(Material.PAPER, "§3§lManage Invites", Arrays.asList("§7Click to manage team invites"), false, "invites_management"));
			inv.setItem(18, getGUIItem(Material.BARRIER, "§cBack to team menu", Arrays.asList("§7Click to go back to team menu"), false, "close"));
			
			p.openInventory(inv);
		}
	}
	
	public void openManagementGUI(Player p, String target, Team t) {
		if(!t.getLeader().equals(p.getUniqueId())) return;
		String name = "Global";
		if(!target.equals("ALL")) name = Bukkit.getOfflinePlayer(UUID.fromString(target)).getName();
		Inventory inv = Bukkit.createInventory(null, 5*9, "Management "+name+" "+t.getName());
		
		for(int i = 0;i<45;i++) {
			inv.setItem(i, getGlassPane());
		}

		ItemStack skull = null;
		if(target.equals("ALL")) {
			skull = main.global_skull.clone();
			SkullMeta m = (SkullMeta) skull.getItemMeta();
			m.setDisplayName("§3Global - Others");
			skull.setItemMeta(m);
		} else {
			PlayerData plData = main.dataManager.getPlayers().stream().filter(tl -> tl.getUUID().equals(UUID.fromString(target))).findFirst().orElse(null);
			if(plData.getSkull() == null) {skull = new ItemStack(Material.PLAYER_HEAD);}
			if(plData.getSkull() != null) {skull = plData.getSkull().clone();}
			SkullMeta m = (SkullMeta) skull.getItemMeta();
			m.setDisplayName("§3"+name);
			skull.setItemMeta(m);
			inv.setItem(12, getGUIItem(Material.REDSTONE_BLOCK, "§c§lKick Player", Arrays.asList("§7Kicks the player", "§7out of the team."), false, "team_kick_player"));
			inv.setItem(14, getGUIItem(Material.EMERALD_BLOCK, "§a§lPromote Player", Arrays.asList("§7Promotes the player", "§7to leader, you will", "§7step down as a member."), false, "team_promote_player"));
		}
		
		int e = 0;
		Permission[] perms = new Permission[Permission.values().length];
		for(Permission pe : Permission.values()) {
			if(pe.isGlobalPermission() == target.equals("ALL") && pe.isTeamPermission()) {
				perms[e] = pe;
				e++;
			}
		}
		
		int o = 0;
		for(int i = 20;i<=33;i++) {
			if(i >= 20 && i <= 24 || i >= 29 && i <= 33) {
				if(o < perms.length && perms[o] != null) {
					Permission perm = perms[o];
					String perm_name = "§3"+perm.toString().replace("_", " "); 
					List<String> lore = new ArrayList<>();
					lore.add(" ");
					if(t.getPerms().containsKey(target) && t.getPerms().get(target).contains(perm)) {
						lore.add("§a› Enabled");
						lore.add("§7› Disabled");
					} else {
						lore.add("§7› Enabled");
						lore.add("§c› Disabled");
					}
					lore.add("");
					lore.add("§7Click to toggle");
					ItemStack item = getGUIItem(Material.OAK_SIGN, perm_name, lore, false, "team_permission_"+perm.getName().replaceAll("_", "-"));
					inv.setItem(i, item);
					o++;
				} else {
					inv.setItem(i, getGlassPane());
				}
			}
		}
		
		inv.setItem(13, skull);
		
		inv.setItem(36, getGUIItem(Material.BARRIER, "§cBack to team menu", Arrays.asList("§7Click to go back to team menu"), false, "close"));
		
		p.openInventory(inv);
	}
	
	public void openInvitesGUI(Player p, PlayerData pData, Team t) {
		Inventory inv = Bukkit.createInventory(null, 3*9, "Invites Management "+t.getName());
		
		for(int i = 19;i<26;i++) {
			inv.setItem(i, getGlassPane());
		}
		
		setInvitesItems(t, inv);
		
		inv.setItem(26, getGUIItem(Material.PAPER, "§3§lCreate an Invite", Arrays.asList("§7Click to create an invite"), false, "create_invite"));
		inv.setItem(18, getGUIItem(Material.BARRIER, "§cBack to team management", Arrays.asList("§7Click to go back to team management"), false, "close"));
		p.openInventory(inv);
		Bukkit.getScheduler().runTaskLater(main, () -> {p.updateInventory();}, 20L);
	}
	
	public void openClaimsGUI(Player p, PlayerData pData, Team t) {
		Inventory inv = Bukkit.createInventory(null, 5*9, "Claims "+t.getName());
		
		List<Region> regions = new ArrayList<>();
		
		for(Region r : main.dataManager.getRegions()) {
			if(r.getTeamUUID().equals(t.getUUID())) {
				regions.add(r);
			}
		}
		
		int c = 0;
		
		for(int i = 0;i<45;i++) {
			if(i >= 11 && i <= 15 || i >= 20 && i <= 24 || i >= 29 && i <= 33) {
				if(c < regions.size()) {
					Region r = regions.get(c);
					String name = r.getUUID().toString();
					if(r.getName() != null) name = "§3§l"+r.getName();
					List<String> lore = null;
					if(t.getLeader().equals(pData.getUUID()) || t.playerHas(pData.getUUID(), Permission.TEAM_MANAGE_CLAIMS)) {
						lore = new ArrayList<>(Arrays.asList("§7Usage › §f"+r.getLands().size()+" blocks", " "));
						if(t.getLeader().equals(pData.getUUID()) || t.playerHas(pData.getUUID(), Permission.TEAM_MANAGE_CLAIM_SPAWNS)) {
							lore.add("§7Shift Click to Set Teleport");
						}
						lore.add("§7Right Click to Manage");
						if(r.getSpawn() != null) {
							lore.add(2, "§7Left Click to Teleport");
						}
					}
					if(!t.getLeader().equals(pData.getUUID()) && !t.playerHas(pData.getUUID(), Permission.TEAM_MANAGE_CLAIMS)) {
						lore = new ArrayList<>(Arrays.asList("§7Size › §f"+r.getLands().size()+" blocks", " "));
						if(r.getSpawn() != null) {
							lore.add("§7Left Click to Teleport");
						} else {
							lore.add("§8Claim spawn has yet to be set!");
						}
					}
					Material icon = Material.SHORT_GRASS;
					if(r.getIcon() != null) icon = r.getIcon().getType(); 
					ItemStack claimIcon = getGUIItem(icon, name, lore, false, "region_"+r.getUUID().toString());
					inv.setItem(i, claimIcon);
					c++;
				}
			} else {
				inv.setItem(i, getGlassPane());
			}
		}
		
		if(t.getLeader().equals(pData.getUUID()) || t.playerHas(pData.getUUID(), Permission.TEAM_MANAGE_CLAIMS)) {
			inv.setItem(39, getGUIItem(Material.NAME_TAG, "§3§lCreate Claim", Arrays.asList("§7Click to create claim", " ", "§7Your location will be", "§7the first claimed land", "§7of the new claim!"), false, "create_claim"));
			
			boolean hasClaimTool = false;
			for(int i = 0; i<36; i++) {
				ItemStack stack = p.getInventory().getItem(i);
				if(stack != null) {
					if(main.isClaimTool(stack)) {
						hasClaimTool = true;
					}
				}
			}
			if(!hasClaimTool) inv.setItem(41, getGUIItem(Material.GOLDEN_SHOVEL, "§b§lCheck For Claims", Arrays.asList("§7Click to receive the", "§7claim check tool", " ", "§7You will be able to see", "§7your claims boundaries!"), true, "check_claim"));
			if(hasClaimTool) inv.setItem(41, getGUIItem(Material.WOODEN_SHOVEL, "§b§lReturn Claim Check Tool", Arrays.asList("§7Click to return the", "§7claim check tool"), true, "check_claim_return"));
		}
		
		inv.setItem(36, getGUIItem(Material.BARRIER, "§cBack to team menu", Arrays.asList("§7Click to go back to team menu"), false, "close"));
		p.openInventory(inv);
	}
	
	public void openClaimManagementGUI(Player p, Team t, Region r) {
		Inventory inv = Bukkit.createInventory(null, 5*9, "Claim Settings "+t.getName());
		
		for(int i = 0;i<45;i++) {
			inv.setItem(i, getGlassPane());
		}
		
		Material icon = Material.SHORT_GRASS;
		if(r.getIcon() != null) icon = r.getIcon().clone().getType();
		ItemStack claimIcon = getGUIItem(icon, "§3§l"+r.getName(), Arrays.asList("§7Usage › §f"+r.getLands().size()+" blocks"), false, "region_"+r.getUUID().toString());
		inv.setItem(13, claimIcon);
		
		List<String> lore = new ArrayList<>();
		lore.add(" ");
		if(r.getProperties().get(Property.PUBLIC_ACCESS)) {
			lore.add("§a› Enabled");
			lore.add("§7› Disabled");
		} else {
			lore.add("§7› Enabled");
			lore.add("§c› Disabled");
		}
		
		if(t.getLeader().equals(p.getUniqueId()) || r.playerHas(p.getUniqueId(), Permission.CLAIM_MANAGE_PUBLIC_ACCESS)) {
			lore.add("");
			lore.add("§7Click to toggle");
		}
		
		inv.setItem(21, getGUIItem(Material.IRON_DOOR, "§3§lPublic Access", lore, false, "claim_public_access"));
		
		if(t.getLeader().equals(p.getUniqueId()) || r.hasPropertyPerm(p.getUniqueId())) {
			inv.setItem(22, getGUIItem(Material.LILY_PAD, "§3§lClaim Flags", Arrays.asList("§7Click to manage claim flags"), false, "claim_flags"));
		}
		
		List<String> lore1 = new ArrayList<>();
		lore1.add("§7Welcome Message:");
		if(r.getTitles().size() >= 2) {
			lore1.add("§7§o"+r.getTitles().get(0));
			lore1.add("§7§o"+r.getTitles().get(1));
		} else {
			lore1.add("§7§oNone");
		}
		lore1.add("§7Exit Message");
		if(r.getTitles().size() >= 2) {
			lore1.add("§7§o"+r.getTitles().get(2));
			lore1.add("§7§o"+r.getTitles().get(3));
		} else {
			lore1.add("§7§oNone");
		}
		if(t.getLeader().equals(p.getUniqueId()) || r.playerHas(p.getUniqueId(), Permission.CLAIM_MANAGE_ENTER_EXIT_MSG)) {
			lore1.add(" ");
			lore1.add("§7Click to set welcome/exit");
		}
		
		inv.setItem(23, getGUIItem(Material.PAPER, "§3§lWelcome/Exit Message", lore1, false, "claim_titles"));
				
		if(t.getLeader().equals(p.getUniqueId())) {
			List<String> lore2 = new ArrayList<>();
			lore2.add(" ");
			if(r.getProperties().get(Property.HURT_ANIMAL)) {
				lore2.add("§a› Enabled");
				lore2.add("§7› Disabled");
			} else {
				lore2.add("§7› Enabled");
				lore2.add("§c› Disabled");
			}
			lore2.add("");
			lore2.add("§7Click to toggle");
			
			List<String> lore3 = new ArrayList<>();
			lore3.add(" ");
			if(r.getProperties().get(Property.HURT_MONSTER)) {
				lore3.add("§a› Enabled");
				lore3.add("§7› Disabled");
			} else {
				lore3.add("§7› Enabled");
				lore3.add("§c› Disabled");
			}
			lore3.add("");
			lore3.add("§7Click to toggle");
			
			inv.setItem(30, getGUIItem(Material.LEATHER, "§3§lHurt Animal Exceptions", lore2, false, "claim_hurt_animal"));
			inv.setItem(31, getGUIItem(Material.OAK_SIGN, "§3§lPermissions", Arrays.asList("§7Click to manage claim", "§7specific perms"), false, "claim_permissions"));
			inv.setItem(32, getGUIItem(Material.ZOMBIE_HEAD, "§4§lHurt Monster Exceptions", lore3, false, "claim_hurt_monster"));
		}
		
		if(t.getLeader().equals(p.getUniqueId()) || t.playerHas(p.getUniqueId(), Permission.TEAM_DELETE_CLAIMS)) {
			inv.setItem(44, getGUIItem(Material.BARRIER, "§4§lDelete this claim", Arrays.asList("§7Shift Click to delete this claim"), false, "delete_claim"));
		}
		
		inv.setItem(36, getGUIItem(Material.BARRIER, "§cBack to claims menu", Arrays.asList("§7Click to go back to claims menu"), false, "close"));
		p.openInventory(inv);
	}
	
	public void openClaimFlagsGUI(Player p, Team t, Region r) {
		Inventory inv = Bukkit.createInventory(null, 5*9, "Claim Flags "+r.getName());
		
		Set<Integer> slots = new HashSet<>(Arrays.asList(10, 12, 14, 16, 20, 22, 24, 30, 32));
		
		Map<Property, Boolean> map = new HashMap<>(r.getProperties());
		
		for(Property pr : map.keySet()) {
			Permission permission = null;
			for(Permission perm : Permission.values()) {
				if(perm.getLinkedProperty() == pr) {
					permission = perm;
				}
			}
			if(permission != null) {
				if(t.getLeader().equals(p.getUniqueId()) || r.playerHas(p.getUniqueId(), permission)) {
					map.put(pr, true);
				} else {
					map.put(pr, false);
				}
			}
		}
		
		List<Property> properties = new ArrayList<>(map.keySet());
		properties.sort(Comparator.comparing(Property::getName));
		Collections.swap(properties, 3, 7);
		
		int c = 0;
		for(int i = 0;i<45;i++) {
			if(slots.contains(i) && c < properties.size()) {
				Property property = properties.get(c);
				List<String> lore = new ArrayList<>();
				lore.add(" ");
				if(r.getProperties().get(property)) {
					lore.add("§7State › §aEnabled");
				} else {
					lore.add("§7State › §cDisabled");
				}
				if(map.get(property)) {
					lore.add(" ");
					lore.add("§7Click to toggle");
				}
				Permission permission = null;
				for(Permission perm : Permission.values()) {
					if(perm.getLinkedProperty() != null && perm.getLinkedProperty() == property) {
						permission = perm;
					}
				}
				
				String propertyName = property.getName().replaceAll("_", "-");
				String permissionName = permission.getName().replaceAll("_", "-");
				
				inv.setItem(i, getGUIItem(property.getIcon(), "§3§l"+property.getName().replaceAll("_", " ").toUpperCase(), lore, false, "region_"+r.getUUID().toString()+"_team_"+t.getUUID().toString()+"_property_"+propertyName+"_permission_"+permissionName));
				c++;
			} else {
				inv.setItem(i, getGlassPane());
			}
		}
		
		inv.setItem(36, getGUIItem(Material.BARRIER, "§cBack to claim menu", Arrays.asList("§7Click to go back to claim menu"), false, "close"));
		p.openInventory(inv);
	}
	
	public void openClaimPermissionGUI(Player p, Team t, Region r, int page, String target) {
		if(target == null) { 
			HashMap<Integer, ItemStack> slots = new HashMap<>();
			int c = (page-1)*15;
			for(int i = 0;i<45;i++) {
				if(i >= 11 && i <= 15 || i >= 20 && i <= 24 ||  i >= 29 && i <= 33) {
					if(c < t.getMembers().size()) {
						UUID uuid = t.getMembers().get(c);
						PlayerData plData = main.dataManager.getPlayers().stream().filter(tl -> tl.getUUID().equals(uuid)).findFirst().orElse(null);
						ItemStack skull = null;
						if(plData.getSkull() == null) skull = new ItemStack(Material.PLAYER_HEAD);
						if(plData.getSkull() != null) skull = plData.getSkull().clone();
						SkullMeta m = (SkullMeta) skull.getItemMeta();
						m.setDisplayName("§3"+Bukkit.getOfflinePlayer(plData.getUUID()).getName());
						m.setLore(Arrays.asList("§7› Click to manage"));
						m.getPersistentDataContainer().set(main.GUIItemkey, PersistentDataType.STRING, "member_"+uuid.toString());
						skull.setItemMeta(m);
						slots.put(i, skull);
						c++;
					} else if(c == t.getMembers().size()) {
						ItemStack banner = new ItemStack(Material.WHITE_BANNER);
						if(t.getBanner() != null) banner = t.getBanner().clone();
						ItemMeta m = banner.getItemMeta();
						m.setDisplayName("§3All Team Members");
						m.setLore(Arrays.asList("§7› Click to manage"));
						m.getPersistentDataContainer().set(main.GUIItemkey, PersistentDataType.STRING, "member_members");
						m.addItemFlags(ItemFlag.HIDE_DYE, ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ADDITIONAL_TOOLTIP);
						banner.setItemMeta(m);
						slots.put(i, banner);
						c++;
					} else if(c == t.getMembers().size()+1) {
						ItemStack skull = main.global_skull.clone();
						SkullMeta m = (SkullMeta) skull.getItemMeta();
						m.setLore(Arrays.asList("§7› Click to manage"));
						m.getPersistentDataContainer().set(main.GUIItemkey, PersistentDataType.STRING, "member_global");
						skull.setItemMeta(m);
						slots.put(i, skull);
						c++;
					}
				} else {
					slots.put(i, getGlassPane());
				}
			}
			double mc = (t.getMembers().size()+2) / 15f;
			int maxPage = (int) Math.ceil(mc);
			if(maxPage == 0) maxPage = 1;
			Inventory inv = Bukkit.createInventory(null, 5*9, "Claim Management "+r.getName()+" "+page+"/"+maxPage);
			
			for(Entry<Integer, ItemStack> slot : slots.entrySet()) {
				inv.setItem(slot.getKey(), slot.getValue());
			}
			
			if(maxPage > 1 && page != maxPage) {
				inv.setItem(42, main.right_arrow);
			}
			
			if(page != 1) {
				inv.setItem(38, main.left_arrow);
			}
			
			Material icon = Material.SHORT_GRASS;
			if(r.getIcon() != null) icon = r.getIcon().clone().getType();
			ItemStack claimIcon = getGUIItem(icon, "§3§l"+r.getName(), new ArrayList<>(), false, "region_"+r.getUUID().toString()+"_team_"+t.getUUID().toString());
			inv.setItem(4, claimIcon);
			inv.setItem(36, getGUIItem(Material.BARRIER, "§cBack to claim menu", Arrays.asList("§7Click to go back to claim menu"), false, "close"));
			
			p.openInventory(inv);
		} else {
			String name = "";
			if(!target.equals("ALL") && !target.equals("MEMBERS")) name = target;
			if(target.equals("ALL")) name = "Global";
			if(target.equals("MEMBERS")) name = "Members";
			
			Inventory inv = Bukkit.createInventory(null, 5*9, "Claim Permissions");

			ItemStack skull = null;
			if(target.equals("ALL")) {
				skull = main.global_skull.clone();
				SkullMeta m = (SkullMeta) skull.getItemMeta();
				m.setDisplayName("§3Global - Others");
				m.setLore(Arrays.asList("§7Claim: "+r.getName()));
				m.getPersistentDataContainer().set(main.GUIItemkey, PersistentDataType.STRING, "member_"+name+"_region_"+r.getUUID().toString()+"_team_"+t.getUUID().toString());
				skull.setItemMeta(m);
			} else if(target.equals("MEMBERS")) {
				skull = new ItemStack(Material.WHITE_BANNER, 1);
				if(t.getBanner() != null) skull = t.getBanner().clone();
				ItemMeta m = skull.getItemMeta();
				m.setDisplayName("§3All Team Members");
				m.setLore(Arrays.asList("§7Claim: "+r.getName()));
				m.getPersistentDataContainer().set(main.GUIItemkey, PersistentDataType.STRING, "member_"+name+"_region_"+r.getUUID().toString()+"_team_"+t.getUUID().toString());
				skull.setItemMeta(m);
			} else {
				PlayerData plData = main.dataManager.getPlayers().stream().filter(tl -> tl.getUUID().equals(UUID.fromString(target))).findFirst().orElse(null);
				if(plData.getSkull() == null) {skull = new ItemStack(Material.PLAYER_HEAD);}
				if(plData.getSkull() != null) {skull = plData.getSkull().clone();}
				SkullMeta m = (SkullMeta) skull.getItemMeta();
				m.setDisplayName("§3"+Bukkit.getOfflinePlayer(UUID.fromString(name)).getName());
				m.setLore(Arrays.asList("§7Claim: "+r.getName()));
				m.getPersistentDataContainer().set(main.GUIItemkey, PersistentDataType.STRING, "member_"+name+"_region_"+r.getUUID().toString()+"_team_"+t.getUUID().toString());
				skull.setItemMeta(m);
			}
			
			int e = 0;
			Permission[] globalPerms = new Permission[Permission.values().length];
			int a = 0;
			Permission[] managementPerms = new Permission[Permission.values().length];
			for(Permission pe : Permission.values()) {
				if(!pe.isTeamPermission()) {
					if(pe.isGlobalPermission()) {
						globalPerms[e] = pe;
						e++;
					} else if(!target.equals("ALL")){
						managementPerms[a] = pe;
						a++;
					}
				}
			}
			
			e = 0;
			a = 0;
			for(int i = 0;i<45;i++) {
				if((i >= 10 && i <= 12 || i >= 19 && i <= 21 || i >= 1 && i <= 3 || i >= 28 && i <= 30) && !target.equals("ALL")) {
					if(e < globalPerms.length && globalPerms[e] != null) {
						String perm = "§3"+globalPerms[e].toString().replace("_", " "); 
						List<String> lore = new ArrayList<>();
						lore.add(" ");
						
						if(r.getPerms().containsKey(target) && r.getPerms().get(target).contains(globalPerms[e])) {
							lore.add("§a› Enabled");
							lore.add("§7› Disabled");
						} else {
							lore.add("§7› Enabled");
							lore.add("§c› Disabled");
						}
						lore.add("");
						lore.add("§7Click to toggle");
						ItemStack item = getGUIItem(Material.BIRCH_SIGN, perm, lore, false, "claim_permission_"+globalPerms[e].getName().replaceAll("_", "-"));
						inv.setItem(i, item);
						e++;
					} else {
						inv.setItem(i, getGlassPane());
					}
				} else if((i >= 12 && i <= 14 || i >= 21 && i <= 23 || i >= 30 && i <= 32) && target.equals("ALL")) {
					if(e < globalPerms.length && globalPerms[e] != null) {
						String perm = "§3"+globalPerms[e].toString().replace("_", " "); 
						List<String> lore = new ArrayList<>();
						lore.add(" ");
						if(globalPerms[e] == Permission.CLAIM_ACCESS) {
							if(r.getProperties().get(Property.PUBLIC_ACCESS)) {
								lore.add("§a› Enabled");
								lore.add("§7› Disabled");
							} else {
								lore.add("§7› Enabled");
								lore.add("§c› Disabled");
							}
						}else {
							if(r.getPerms().containsKey(target) && r.getPerms().get(target).contains(globalPerms[e])) {
								lore.add("§a› Enabled");
								lore.add("§7› Disabled");
							} else {
								lore.add("§7› Enabled");
								lore.add("§c› Disabled");
							}
						}
						lore.add("");
						lore.add("§7Click to toggle");
						ItemStack item = getGUIItem(Material.BIRCH_SIGN, perm, lore, false, "claim_permission_"+globalPerms[e].getName().replaceAll("_", "-"));
						inv.setItem(i, item);
						e++;
					} else {
						inv.setItem(i, getGlassPane());
					}
				} else if(i >= 5 && i <= 7 || i >= 14 && i <= 16 || i >= 23 && i <= 25 || i >= 32 && i <= 34) {
					if(a < managementPerms.length && managementPerms[a] != null) {
						String perm = "§3"+managementPerms[a].toString().replace("_", " "); 
						List<String> lore = new ArrayList<>();
						lore.add(" ");
						
						if(r.getPerms().containsKey(target) && r.getPerms().get(target).contains(managementPerms[a])) {
							lore.add("§a› Enabled");
							lore.add("§7› Disabled");
						} else {
							lore.add("§7› Enabled");
							lore.add("§c› Disabled");
						}
						lore.add("");
						lore.add("§7Click to toggle");
						ItemStack item = getGUIItem(Material.WARPED_SIGN, perm, lore, false, "claim_permission_"+managementPerms[a].getName().replaceAll("_", "-"));
						inv.setItem(i, item);
						a++;
					} else {
						inv.setItem(i, getGlassPane());
					}
				} else {
					inv.setItem(i, getGlassPane());
				}
			}
			
			inv.setItem(4, skull);
			inv.setItem(36, getGUIItem(Material.BARRIER, "§cBack to claim menu", Arrays.asList("§7Click to go back to claim menu"), false, "close"));
			p.openInventory(inv);
		}
	}
	
	public void setInvitesItems(Team t, Inventory inv) {
		List<ItemStack> invites = new ArrayList<>();
		for(Invite invite : t.getInvites()) {
			String i = Bukkit.getOfflinePlayer(invite.getInvitee()).getName();
			String o = Bukkit.getOfflinePlayer(invite.getInviter()).getName();

			long timeBeforeExpiryMillis = TimeUnit.MINUTES.toMillis(invite.getTimeBeforeExpiry());
		    long remainingTimeMillis = (invite.getTimestamp() + timeBeforeExpiryMillis) - System.currentTimeMillis();
		    String expiryMessage = getFormattedTimeRemaining(remainingTimeMillis);
			invites.add(getGUIItem(Material.PAPER, "§3Invite to "+i, Arrays.asList("§7From "+o, " ", "§7Expires in "+expiryMessage, "§7Shift Click to withdraw"), false, "invite"));
		}
		
		for(int i= 0;i<18;i++) {
			if(i < invites.size()) {
				inv.setItem(i, invites.get(i));
			}
		}
	}
	
	private String getFormattedTimeRemaining(long millis) {
	    if (millis <= 0) return "Expired";

	    long years = TimeUnit.MILLISECONDS.toDays(millis) / 365;
	    millis -= TimeUnit.DAYS.toMillis(years * 365);
	    long months = TimeUnit.MILLISECONDS.toDays(millis) / 30;
	    millis -= TimeUnit.DAYS.toMillis(months * 30);
	    long days = TimeUnit.MILLISECONDS.toDays(millis);
	    millis -= TimeUnit.DAYS.toMillis(days);
	    long hours = TimeUnit.MILLISECONDS.toHours(millis);
	    millis -= TimeUnit.HOURS.toMillis(hours);
	    long minutes = TimeUnit.MILLISECONDS.toMinutes(millis);
	    millis -= TimeUnit.MINUTES.toMillis(minutes);
	    long seconds = TimeUnit.MILLISECONDS.toSeconds(millis);

	    StringBuilder timeString = new StringBuilder();
	    if (years > 0) timeString.append(years).append(" year").append(years > 1 ? "s " : " ");
	    if (months > 0) timeString.append(months).append(" month").append(months > 1 ? "s " : " ");
	    if (days > 0) timeString.append(days).append("d ");
	    if (hours > 0) timeString.append(hours).append("h ");
	    if (minutes > 0) timeString.append(minutes).append("min ");
	    if (seconds > 0 || timeString.length() == 0) timeString.append(seconds).append("s");

	    return timeString.toString().trim();
	}
	
	private ItemStack getGlassPane() {
		ItemStack chain = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
		ItemMeta im = chain.getItemMeta();
		im.setDisplayName(" ");
		chain.setItemMeta(im);
		return chain;
	}
	
	private ItemStack getGUIItem(Material material, String name, List<String> lore, boolean isEnchanted, String tag) {
		ItemStack item = new ItemStack(material);
		ItemMeta im = item.getItemMeta();
		im.setDisplayName(name);
		im.setLore(lore);
		if(isEnchanted) {
			im.addEnchant(Enchantment.POWER, 1, false);
			im.addItemFlags(ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_ATTRIBUTES);
		}else {
			im.addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ADDITIONAL_TOOLTIP);
		}
	    im.getPersistentDataContainer().set(main.GUIItemkey, PersistentDataType.STRING, tag);
		item.setItemMeta(im);
		return item;
	}
	
	private ItemStack getGUIItem(Material material, String name, int i, String tag) {
		ItemStack item = new ItemStack(material, i);
		ItemMeta im = item.getItemMeta();
		im.setDisplayName(name);
	    im.getPersistentDataContainer().set(main.GUIItemkey, PersistentDataType.STRING, tag);
		item.setItemMeta(im);
		return item;
	}
	
	public ChatColor getChatColor(Material bannerMaterial) {
        switch (bannerMaterial) {
            case WHITE_BANNER:
                return ChatColor.WHITE;
            case ORANGE_BANNER:
                return ChatColor.GOLD;
            case MAGENTA_BANNER:
                return ChatColor.LIGHT_PURPLE;
            case LIGHT_BLUE_BANNER:
                return ChatColor.AQUA;
            case YELLOW_BANNER:
                return ChatColor.YELLOW;
            case LIME_BANNER:
                return ChatColor.GREEN;
            case PINK_BANNER:
                return ChatColor.LIGHT_PURPLE;
            case GRAY_BANNER:
                return ChatColor.DARK_GRAY;
            case LIGHT_GRAY_BANNER:
                return ChatColor.GRAY;
            case CYAN_BANNER:
                return ChatColor.DARK_AQUA;
            case PURPLE_BANNER:
                return ChatColor.DARK_PURPLE;
            case BLUE_BANNER:
                return ChatColor.BLUE;
            case BROWN_BANNER:
                return ChatColor.GOLD;
            case GREEN_BANNER:
                return ChatColor.DARK_GREEN;
            case RED_BANNER:
                return ChatColor.RED;
            case BLACK_BANNER:
                return ChatColor.BLACK;
            default:
                return ChatColor.RESET;
        }
    }

}
