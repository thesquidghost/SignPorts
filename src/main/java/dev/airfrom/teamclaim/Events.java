package dev.airfrom.teamclaim;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.OfflinePlayer;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Animals;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.block.BlockIgniteEvent.IgniteCause;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.BlockSpreadEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import dev.airfrom.teamclaim.data.ClaimedLand;
import dev.airfrom.teamclaim.data.GUI;
import dev.airfrom.teamclaim.data.Invite;
import dev.airfrom.teamclaim.data.Permission;
import dev.airfrom.teamclaim.data.PlayerData;
import dev.airfrom.teamclaim.data.Property;
import dev.airfrom.teamclaim.data.Region;
import dev.airfrom.teamclaim.data.Team;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;

public class Events implements Listener{
	
	Map<Player, String> isIn = new HashMap<>();
	Map<Block, String> tnts = new HashMap<>();
	
	private final long COOLDOWN_TIME = 600;
    private Map<Player, Long> lastErrorMessage = new HashMap<>();
	
	private Main main;
	private GUI GUI;
	
	public Events(Main main) {
		this.main = main;
		this.GUI = new GUI(main);
	}

	@EventHandler
	public void OnJoin(PlayerJoinEvent e) {
		Player p = e.getPlayer();
		PlayerData pData = main.dataManager.getPlayers().stream().filter(tl -> tl.getUUID().equals(p.getUniqueId())).findFirst().orElse(null);
		ItemStack skull = new ItemStack(Material.PLAYER_HEAD);
		SkullMeta i = (SkullMeta) skull.getItemMeta();
		i.setOwningPlayer(p);
		skull.setItemMeta(i);
		if(pData == null) {
			main.dataManager.getPlayers().add(new PlayerData(p.getUniqueId(), null, 0, 0, skull));
		} else {
			pData.setSkull(skull);
		}
		
		if(!main.timeTracking.containsKey(p.getUniqueId())) {
			main.timeTracking.put(p.getUniqueId(), System.currentTimeMillis());
		}
	}
	
	@EventHandler
    public void onQuit(PlayerQuitEvent e) {
		Player p = e.getPlayer();
		if(main.timeTracking.containsKey(p.getUniqueId())) {
			main.timeTracking.remove(p.getUniqueId());
		}
	}	
	
	@EventHandler
	public void onGUIDrag(InventoryDragEvent e){
		if(e.getInventory() != null && e.getInventory().getType() != InventoryType.PLAYER) {
			for(int slot : e.getRawSlots()) {
		        if(slot < e.getView().getTopInventory().getSize()) {		            
		            ItemStack draggedItem = e.getOldCursor();
		            if(draggedItem != null && main.isClaimTool(draggedItem)) {
		                e.setCancelled(true);
		                return;
		            }
		        }
		    }
		}
	}
	
	@SuppressWarnings("deprecation")
	@EventHandler
	public void onGUIClick(InventoryClickEvent e){
		Player p = (Player) e.getWhoClicked();
		ItemStack item = e.getCurrentItem();
		if(e.getClickedInventory() != null && e.getClickedInventory().getType() != InventoryType.PLAYER) {
			ItemStack cursorItem = e.getCursor();
	        if(cursorItem != null && main.isClaimTool(cursorItem)) {
	            e.setCancelled(true);
	            return;
	        }
		}
		if(e.getView().getTitle().equalsIgnoreCase("Land Property")){
			if(e.getClickedInventory() == null || !e.getClickedInventory().equals(e.getView().getTopInventory())) return;
			if(item == null || !main.isGUIItem(item)) return;
			e.setCancelled(true);
			Location l = p.getLocation();
			int x = l.getBlockX();
			int z = l.getBlockZ();
			World w = l.getWorld();
			if(main.isGUIItemTag(item, "close")) p.closeInventory();
			if(main.isGUIItemTag(item, "unclaim_land")){
				ClaimedLand c = main.dataManager.getClaimedLands().stream().filter(cl -> cl.getX() == x && cl.getZ() == z && Bukkit.getWorld(cl.getWorldName()) == w).findFirst().orElse(null);
				if(c != null) {
					Region r = main.dataManager.getRegions().stream().filter(cl -> cl.getUUID().equals(c.getRegionUUID())).findFirst().orElse(null);
					Team team = main.dataManager.getTeams().stream().filter(t -> t.getUUID().equals(r.getTeamUUID())).findFirst().orElse(null);
					if(r == null || team == null || (Bukkit.getOfflinePlayer(team.getLeader()) != p && !team.playerHas(p.getUniqueId(), Permission.TEAM_MANAGE_CLAIMS))) {
						p.closeInventory();
						p.sendMessage(main.err_prefix+"Could not unclaim the land!");
						e.setCancelled(true);
						return;
					}
					if(r.getLands().size() <= 1) {
						p.closeInventory();
						p.sendMessage(main.err_prefix+"Could not unclaim the land!");
						p.sendMessage(main.err_color+"To delete a claim, please access the claim management menu.");
						e.setCancelled(true);
						return;
					}
					
					main.dataManager.getClaimedLands().remove(c);
					if(r.getLands().contains(c.getUUID())) r.getLands().remove(c.getUUID());
					p.playSound(p.getLocation(), Sound.ENTITY_VILLAGER_TRADE, 1, 2);
					p.closeInventory();
					p.sendMessage(main.prefix+"You unclaimed this land!");
					e.setCancelled(true);
					return;
				}
			}
		} else if(e.getView().getTitle().startsWith("Teams")) {
			if(e.getClickedInventory() == null || !e.getClickedInventory().equals(e.getView().getTopInventory())) return;
			e.setCancelled(true);
			if(item == null || !main.isGUIItem(item)) return;
			if(e.getClick() == ClickType.LEFT || e.getClick() == ClickType.RIGHT) {
				PlayerData pData = main.dataManager.getPlayers().stream().filter(tl -> tl.getUUID().equals(p.getUniqueId())).findFirst().orElse(null);
				
				if(pData == null || pData.getTeamUUIDs().isEmpty()){
					p.closeInventory();
					p.sendMessage(main.err_prefix+"Please try again.");
					return;
				}
				
				String tag = item.getItemMeta().getPersistentDataContainer().get(main.GUIItemkey, PersistentDataType.STRING);
				
				if(tag.split("_").length == 2) {
					UUID uuid = UUID.fromString(tag.split("_")[1]);
					Team t = main.dataManager.getTeams().stream().filter(tl -> tl.getUUID().equals(uuid)).findFirst().orElse(null);
					if(!t.getMembers().contains(p.getUniqueId()) && !t.getLeader().equals(p.getUniqueId())) {
						p.closeInventory();
						p.sendMessage(main.err_prefix+"Please try again.");
						return;
					}
					GUI.openTeamGUI(p, t);
					return;
				} else if(main.isGUIItemTag(item, "team_right_arrow")) {
					String pages = e.getView().getTitle().split(" ")[1];
					int newPage = Integer.valueOf(pages.split("/")[0])+1;
					int maxPage = Integer.valueOf(pages.split("/")[1]);
					if(newPage > maxPage) newPage = maxPage;
					
					GUI.openTeamsGUI(p, pData, newPage);
					return;
				} else if(main.isGUIItemTag(item, "team_left_arrow")) {
					String pages = e.getView().getTitle().split(" ")[1];
					int newPage = Integer.valueOf(pages.split("/")[0])-1;
					if(newPage < 1) newPage = 1;
					
					GUI.openTeamsGUI(p, pData, newPage);
					return;
				} else if(main.isGUIItemTag(item, "close")) {
					p.closeInventory();
					return;
				}
			}
		} else if(e.getView().getTitle().startsWith("Team") && !e.getView().getTitle().contains("Management")) {
			if(e.getClickedInventory() == null || !e.getClickedInventory().equals(e.getView().getTopInventory())) return;
			e.setCancelled(true);
			if(item == null || !main.isGUIItem(item)) return;
			if(e.getClick() == ClickType.LEFT || e.getClick() == ClickType.RIGHT) {
				PlayerData pData = main.dataManager.getPlayers().stream().filter(tl -> tl.getUUID().equals(p.getUniqueId())).findFirst().orElse(null);
				String teamName = e.getView().getTitle().split(" ")[1];
				Team t = main.dataManager.getTeams().stream().filter(tl -> tl.getName().equals(teamName)).findFirst().orElse(null);
				
				if(t == null || (!t.getMembers().contains(pData.getUUID()) && !t.getLeader().equals(pData.getUUID()))){
					p.closeInventory();
					p.sendMessage(main.err_prefix+"Please try again.");
					return;
				}
				
				if(main.isGUIItemTag(item, "claim_management_info")) {
					GUI.openClaimsGUI(p, pData, t);
					return;
				} else if(main.isGUIItemTag(item, "team_management_info")) {
					if(t.getLeader().equals(pData.getUUID()) || t.playerHas(p.getUniqueId(), Permission.TEAM_MANAGE_INVITES)) {
						GUI.openTeamManagementGUI(p, t, 1);
						return;
					} else {
						sendTeamInfo(p, t);
						return;
					}
				} else if(main.isGUIItemTag(item, "team_claim_blocks")) {
					GUI.openClaimBlockGUI(p, pData, t);
					return;
				}
			}
		} else if(e.getView().getTitle().startsWith("Team") && e.getView().getTitle().contains("Management")) {
			if(e.getClickedInventory() == null || !e.getClickedInventory().equals(e.getView().getTopInventory())) return;
			e.setCancelled(true);
			if(item == null || !main.isGUIItem(item)) return;
			if(e.getClick() == ClickType.LEFT || e.getClick() == ClickType.RIGHT || e.getClick() == ClickType.SHIFT_LEFT) {
				PlayerData pData = main.dataManager.getPlayers().stream().filter(tl -> tl.getUUID().equals(p.getUniqueId())).findFirst().orElse(null);
				String teamName = e.getView().getTitle().split(" ")[2];
				Team t = main.dataManager.getTeams().stream().filter(tl -> tl.getName().equals(teamName)).findFirst().orElse(null);
				
				if(t == null || (!t.getMembers().contains(pData.getUUID()) && !t.getLeader().equals(pData.getUUID()))){
					p.closeInventory();
					p.sendMessage(main.err_prefix+"Please try again.");
					return;
				}
				
				if(item.getItemMeta().getPersistentDataContainer().get(main.GUIItemkey, PersistentDataType.STRING).contains("member_")) {
					if(t.getLeader().equals(pData.getUUID())) {
						if(item.getItemMeta().getPersistentDataContainer().get(main.GUIItemkey, PersistentDataType.STRING).equals("member_global")) {
							GUI.openManagementGUI(p, "ALL", t);
							return;
						}
						String uuid = item.getItemMeta().getPersistentDataContainer().get(main.GUIItemkey, PersistentDataType.STRING).split("_")[1];
						GUI.openManagementGUI(p, uuid, t);
						return;
					}
				} else if(main.isGUIItemTag(item, "team_management_info")) {
					if(t.getLeader().equals(pData.getUUID())) {
						if(e.getClick() == ClickType.SHIFT_LEFT) {
							if(main.settingParameter.containsKey(p.getUniqueId())) {
								if(main.settingParameter.get(p.getUniqueId()).startsWith("team_rename")) {
									Bukkit.getScheduler().runTaskLater(main, () -> {p.closeInventory();}, 1L);
									p.sendMessage(main.prefix+"Enter your team's new name in the chat :");
									return;
								} else {
									Bukkit.getScheduler().runTaskLater(main, () -> {p.closeInventory();}, 1L);
									p.sendMessage(main.err_prefix+"You are already entering a new value in the chat!");
									return;
								}
							} else {
								main.settingParameter.put(p.getUniqueId(), "team_rename_"+t.getUUID().toString());
								Bukkit.getScheduler().runTaskLater(main, () -> {p.closeInventory();}, 1L);
								p.sendMessage(main.prefix+"Enter your team's new name in the chat :");
								p.sendMessage(main.prefix_color+"(Type \"cancel\" to cancel!)");
								return;
							}
						} else if(e.getClick() == ClickType.LEFT && e.getCursor() != null  && e.getCursor().getType() != Material.AIR && main.isGUIItemTag(item, "team_management_info")) {
							ItemStack i = new ItemStack(e.getCursor().getType());
							i.setItemMeta(e.getCursor().getItemMeta());
							t.setBanner(i);
							
							ItemStack in = i.clone();
							ItemMeta im = in.getItemMeta();
							ChatColor ch = null;
							ch = GUI.getChatColor(i.getType());
							im.setDisplayName("§r"+ch+t.getName());
							im.setLore(Arrays.asList("§7› Shift click to rename", "§7› Set new Team Banner"));
							im.addItemFlags(ItemFlag.HIDE_DYE, ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ADDITIONAL_TOOLTIP);
							in.setItemMeta(im);
							
							e.getCursor().setAmount(e.getCursor().getAmount()-1);
							p.getOpenInventory().setItem(e.getSlot(), in);
							p.updateInventory();
							p.sendMessage(main.prefix+"§aYou successfully set a new team banner!");
							return;
						}
					} else if(t.playerHas(pData.getUUID(), Permission.TEAM_MANAGE_INVITES)) {
						if(e.getClick() == ClickType.LEFT) {
							sendTeamInfo(p, t);
							return;
						}
					}
				} 
				if(e.getClick() == ClickType.RIGHT || e.getClick() == ClickType.LEFT) {
					if(main.isGUIItemTag(item, "team_right_arrow")) {
						if(t.getLeader().equals(pData.getUUID())) {
							String pages = e.getView().getTitle().split(" ")[3];
							int newPage = Integer.valueOf(pages.split("/")[0])+1;
							int maxPage = Integer.valueOf(pages.split("/")[1]);
							if(newPage > maxPage) newPage = maxPage;
							
							GUI.openTeamManagementGUI(p, t, newPage);
							return;
						}
					} else if(main.isGUIItemTag(item, "team_left_arrow")) {
						if(t.getLeader().equals(pData.getUUID())) {
							String pages = e.getView().getTitle().split(" ")[3];
							int newPage = Integer.valueOf(pages.split("/")[0])-1;
							if(newPage < 1) newPage = 1;
							
							GUI.openTeamManagementGUI(p, t, newPage);
							return;
						}
					} else if(main.isGUIItemTag(item, "toggle_team_pvp")) {
						if(t.getLeader().equals(pData.getUUID())) {
							boolean friendly_fire = !t.isFriendlyFire();
							t.setFriendlyFire(friendly_fire);
							
							String status = "";
							if(friendly_fire) status = "§aENABLED";
							if(!friendly_fire) status = "§cDISABLED";
							
							ItemStack it = e.getCurrentItem().clone();
							ItemMeta im = it.getItemMeta();
							List<String> lore = im.getLore();
							lore.remove(lore.size()-1);
							lore.add("§7Friendly-fire is "+status);
							im.setLore(lore);
							it.setItemMeta(im);
							
							e.getClickedInventory().setItem(e.getSlot(), it);
							p.updateInventory();
							return;
						}
					} else if(main.isGUIItemTag(item, "invites_management")) {
						GUI.openInvitesGUI(p, pData, t);
						return;
					} else if(main.isGUIItemTag(item, "close")) {
						GUI.openTeamGUI(p, t);
						return;
					}
				}
			}
		} else if(e.getView().getTitle().startsWith("Management")) {
			if(e.getClickedInventory() == null || !e.getClickedInventory().equals(e.getView().getTopInventory())) return;
			e.setCancelled(true);
			if(item == null || !main.isGUIItem(item)) return;
			if(e.getClick() == ClickType.LEFT || e.getClick() == ClickType.RIGHT) {
				PlayerData pData = main.dataManager.getPlayers().stream().filter(tl -> tl.getUUID().equals(p.getUniqueId())).findFirst().orElse(null);
				String targetRaw = e.getView().getTitle().split(" ")[1];
				boolean isGlobal = targetRaw.equals("Global");
				OfflinePlayer target = null;
				if(!isGlobal) {target = Bukkit.getOfflinePlayer(targetRaw);}
				String teamName = e.getView().getTitle().split(" ")[2];
				Team t = main.dataManager.getTeams().stream().filter(tl -> tl.getName().equals(teamName)).findFirst().orElse(null);
				
				if(t == null || (!t.getMembers().contains(pData.getUUID()) && !t.getLeader().equals(pData.getUUID()))){
					p.closeInventory();
					p.sendMessage(main.err_prefix+"Please try again.");
					return;
				}
				
				if(target != null && t.getMembers().contains(target.getUniqueId())) {
					if(main.isGUIItemTag(item, "team_promote_player")) {
						if(t.getLeader().equals(pData.getUUID())) {
							if(target == p) {
								p.sendMessage(main.err_prefix+"You cannot promote yourself!");
								return;
							}
							t.getMembers().remove(target.getUniqueId());
							t.getMembers().add(p.getUniqueId());
							t.setLeader(target.getUniqueId());
							if(target.isOnline()) ((Player) target).sendMessage(main.prefix+"§aYou have been promoted to §cLeader§a!");
							p.closeInventory();
							p.sendMessage(main.prefix+"§aYou have successfully promoted §e"+target.getName()+"§a to §cLeader§a!");
							return;
						}
					} else if(main.isGUIItemTag(item, "team_kick_player")) {
						if(t.getLeader().equals(pData.getUUID())) {
							if(target == p) {
								p.sendMessage(main.err_prefix+"You cannot kick yourself! Try \"/teamh disband\" to disband your team.");
								return;
							}
							UUID tUUID = target.getUniqueId();
							PlayerData tData = main.dataManager.getPlayers().stream().filter(tl -> tl.getUUID().equals(tUUID)).findFirst().orElse(null);
							List<UUID> teamUUIDs = tData.getTeamUUIDs();
							teamUUIDs.remove(t.getUUID());
							tData.setTeamList(teamUUIDs);
							t.getMembers().remove(target.getUniqueId());
							if(t.getPerms().containsKey(target.getUniqueId().toString())) {
								t.getPerms().remove(target.getUniqueId().toString());
							}
							
							for(UUID pls : t.getMembers()) {
								OfflinePlayer pl = Bukkit.getOfflinePlayer(pls);
								if(pl != null && pl.isOnline()) {
									((Player) pl).sendMessage(main.prefix+"§6"+target.getName()+"§c has been kicked out of the team "+t.getName()+"!");
								}
							}
							if(target.isOnline()) ((Player) target).sendMessage(main.prefix+"§cYou have been kicked out of "+t.getName()+"!");
							p.sendMessage(main.prefix+"§cYou have successfully kicked §6"+target.getName()+"§c out of the team!");
							GUI.openTeamManagementGUI(p, t, 1);
							return;
						}
					}
				}
				
				String tag = item.getItemMeta().getPersistentDataContainer().get(main.GUIItemkey, PersistentDataType.STRING);
				
				if(tag.contains("team_permission")) {
					if(t.getLeader().equals(pData.getUUID())) {
						Permission perm = Permission.getPermission(tag.split("_")[2].replaceAll("-", "_"));
						if(!isGlobal) {
							e.getClickedInventory().setItem(e.getSlot(), toggleTeamPermission(p, t, target.getUniqueId().toString(), perm, e.getCurrentItem()));
							p.updateInventory();
						} else {
							e.getClickedInventory().setItem(e.getSlot(), toggleTeamPermission(p, t, "ALL", perm, e.getCurrentItem()));
							p.updateInventory();
						}
						return;
					}
				} else if(main.isGUIItemTag(item, "close")) {
					GUI.openTeamManagementGUI(p, t, 1);
					return;
				}
			}
		} else if(e.getView().getTitle().startsWith("Invites Management")) {
			if(e.getClickedInventory() == null || !e.getClickedInventory().equals(e.getView().getTopInventory())) return;
			e.setCancelled(true);
			if(item == null || !main.isGUIItem(item)) return;
			if(e.getClick() == ClickType.LEFT || e.getClick() == ClickType.RIGHT || e.getClick() == ClickType.SHIFT_LEFT) {
				PlayerData pData = main.dataManager.getPlayers().stream().filter(tl -> tl.getUUID().equals(p.getUniqueId())).findFirst().orElse(null);
				String teamName = e.getView().getTitle().split(" ")[2];
				Team t = main.dataManager.getTeams().stream().filter(tl -> tl.getName().equals(teamName)).findFirst().orElse(null);
				
				if(t == null || (!t.getMembers().contains(pData.getUUID()) && !t.getLeader().equals(pData.getUUID()))){
					p.closeInventory();
					p.sendMessage(main.err_prefix+"Please try again.");
					return;
				}
				
				if(main.isGUIItemTag(item, "invite")) {
					if(e.getClick() == ClickType.SHIFT_LEFT) {
						if(!t.getLeader().equals(pData.getUUID()) && !t.playerHas(pData.getUUID(), Permission.TEAM_MANAGE_INVITES)) {
							Bukkit.getScheduler().runTaskLater(main, () -> {p.closeInventory();}, 1L);
							p.sendMessage(main.err_prefix+"You do not have the permission!");
							return;
						}
						if(e.getSlot() >= t.getInvites().size()) {
							GUI.openInvitesGUI(p, pData, t);
							return;
						}
						Invite i = t.getInvites().get(e.getSlot());
						OfflinePlayer o = Bukkit.getOfflinePlayer(i.getInvitee());
						t.getInvites().remove(i);
						p.sendMessage(main.prefix+"You withdrew the invite for "+o.getName()+" to join "+t.getName()+"!");
						if(o.isOnline()) {
							((Player) o).sendMessage(main.prefix+"Your invite to join "+t.getName()+" has been withdrawn!");
						}
						GUI.openInvitesGUI(p, pData, t);
						return;
					}
				} 
				if(e.getClick() == ClickType.RIGHT || e.getClick() == ClickType.LEFT) {
					if(main.isGUIItemTag(item, "create_invite")) {
						if(!t.getLeader().equals(pData.getUUID()) && !t.playerHas(pData.getUUID(), Permission.TEAM_CREATE_INVITES)) {
							Bukkit.getScheduler().runTaskLater(main, () -> {p.closeInventory();}, 1L);
							p.sendMessage(main.err_prefix+"You do not have the permission!");
							return;
						}
						if(main.settingParameter.containsKey(p.getUniqueId())) {
							if(main.settingParameter.get(p.getUniqueId()).startsWith("team_invite")) {
								p.closeInventory();
								p.sendMessage(main.prefix+"Enter the player you would like to invite to "+t.getName()+" in the chat :");
								return;
							} else {
								p.closeInventory();
								p.sendMessage(main.err_prefix+"You are already entering a new value in the chat!");
								return;
							}
						} else {
							main.settingParameter.put(p.getUniqueId(), "team_invite_"+t.getUUID().toString());
							p.closeInventory();
							p.sendMessage(main.prefix+"Enter the name of the player you wish to invite to "+t.getName()+" in the chat :");
							p.sendMessage(main.prefix_color+"(Type \"cancel\" to cancel!)");
							return;
						}
					} else if(main.isGUIItemTag(item, "close")) {
						GUI.openTeamManagementGUI(p, t, 1);
						return;
					}
				}
			}
		} else if(e.getView().getTitle().startsWith("Claims")) {
			if(e.getClickedInventory() == null || !e.getClickedInventory().equals(e.getView().getTopInventory())) return;
			e.setCancelled(true);
			if(item == null || !main.isGUIItem(item)) return;
			if(e.getClick() == ClickType.LEFT || e.getClick() == ClickType.RIGHT || e.getClick() == ClickType.SHIFT_LEFT) {
				PlayerData pData = main.dataManager.getPlayers().stream().filter(tl -> tl.getUUID().equals(p.getUniqueId())).findFirst().orElse(null);
				String teamName = e.getView().getTitle().split(" ")[1];
				Team t = main.dataManager.getTeams().stream().filter(tl -> tl.getName().equals(teamName)).findFirst().orElse(null);
				
				if(t == null || (!t.getMembers().contains(pData.getUUID()) && !t.getLeader().equals(pData.getUUID()))){
					p.closeInventory();
					p.sendMessage(main.err_prefix+"Please try again.");
					return;
				}
									
				List<Integer> slots = new ArrayList<>();
				for(int i = 11;i<33;i++) {
					if(i >= 11 && i <= 15 || i >= 20 && i <= 24 || i >= 29 && i <= 33) {
						slots.add(i);
					}
				}
				
				if(slots.contains(e.getSlot()) && main.isGUIItem(item) && item.getItemMeta().getPersistentDataContainer().get(main.GUIItemkey, PersistentDataType.STRING).contains("region_")) {
					UUID regionUUID = UUID.fromString(item.getItemMeta().getPersistentDataContainer().get(main.GUIItemkey, PersistentDataType.STRING).split("_")[1]);
					Region r = main.dataManager.getRegions().stream().filter(tl -> tl.getUUID().equals(regionUUID)).findFirst().orElse(null);
					if(r != null) {
						if(!r.getTeamUUID().equals(t.getUUID())) {
							p.closeInventory();
							p.sendMessage(main.err_prefix+"Please try again.");
							return;
						}
						if(t.getLeader().equals(pData.getUUID()) || t.playerHas(pData.getUUID(), Permission.TEAM_MANAGE_CLAIMS)) {
							if(e.getClick() == ClickType.LEFT && r.getSpawn() != null) {
								p.closeInventory();
								p.teleport(r.getSpawn());
								return;
							} else if(e.getClick() == ClickType.RIGHT) {
								GUI.openClaimManagementGUI(p, t, r);
								return;
							} else if(e.getClick() == ClickType.SHIFT_LEFT && (t.getLeader().equals(pData.getUUID()) || t.playerHas(pData.getUUID(), Permission.TEAM_MANAGE_CLAIM_SPAWNS))) {
								Location l = p.getLocation();
								int x = l.getBlockX();
								int z = l.getBlockZ();
								World w = l.getWorld();
								ClaimedLand c = main.dataManager.getClaimedLands().stream().filter(cl -> cl.getX() == x && cl.getZ() == z && Bukkit.getWorld(cl.getWorldName()) == w).findFirst().orElse(null);
								if(c == null) {
									Bukkit.getScheduler().runTaskLater(main, () -> {p.closeInventory();}, 1L);
									p.sendMessage(main.err_prefix+"The spawn point must be set in the claim!");
									return;
								}	
								Region tr = main.dataManager.getRegions().stream().filter(cl -> cl.getUUID().equals(c.getRegionUUID())).findFirst().orElse(null);
								Team team = main.dataManager.getTeams().stream().filter(tl -> tl.getUUID().equals(tr.getTeamUUID())).findFirst().orElse(null);
								if(team == null || tr != r || team != t) {
									Bukkit.getScheduler().runTaskLater(main, () -> {p.closeInventory();}, 1L);
									p.sendMessage(main.err_prefix+"The spawn point must be set in the claim!");
									return;
								}
								r.setSpawn(p.getLocation());
								String name = r.getUUID().toString();
								if(r.getName() != null) name = r.getName();
								Bukkit.getScheduler().runTaskLater(main, () -> {p.closeInventory();}, 1L);
								p.sendMessage(main.prefix+"§aSuccessfully set new spawn for §l"+name+"§r§a!");
								return;
							}
						} else {
							if(e.getClick() == ClickType.LEFT && r.getSpawn() != null) {
								p.closeInventory();
								p.teleport(r.getSpawn());
								return;
							}
						}
					}
				}
				if(e.getClick() == ClickType.RIGHT || e.getClick() == ClickType.LEFT) {
					if(main.isGUIItemTag(item, "create_claim")) {
						if(t.getLeader().equals(pData.getUUID()) || t.playerHas(pData.getUUID(), Permission.TEAM_MANAGE_CLAIMS)) {
							List<Region> regions = new ArrayList<Region>();
							for(Region r : main.dataManager.getRegions()) {
								if(r.getTeamUUID().equals(t.getUUID())) {
									regions.add(r);
								}
							}
							
							if(regions.size() >= 15) {
								p.closeInventory();
								p.sendMessage(main.err_prefix+"Your team has reached the maximum amount of claims!");
								return;
							}
							if(t.getClaimBlocks() < 1) {
								p.closeInventory();
								p.sendMessage(main.err_prefix+"Your team does not have enough claim blocks!");
								p.sendMessage(main.err_color+"Try depositing some in the team claim block pool.");
								return;
							}
							
							Location l = p.getLocation();
							int x = l.getBlockX();
							int z = l.getBlockZ();
							World w = l.getWorld();
							ClaimedLand c = main.dataManager.getClaimedLands().stream().filter(cl -> cl.getX() == x && cl.getZ() == z && Bukkit.getWorld(cl.getWorldName()) == w).findFirst().orElse(null);
							if(c != null) {
								Region r = main.dataManager.getRegions().stream().filter(cl -> cl.getUUID().equals(c.getRegionUUID())).findFirst().orElse(null);
								Team team = main.dataManager.getTeams().stream().filter(tl -> tl.getUUID().equals(r.getTeamUUID())).findFirst().orElse(null);
								if(team == null) {
									p.closeInventory();
									p.sendMessage(main.err_prefix+"You cannot claim that land!");
									return;
								}
								p.closeInventory();
								p.sendMessage(main.err_prefix+"This is §6"+team.getName()+main.err_color+"'s land!");
								return;
							}
							
							if(main.settingParameter.containsKey(p.getUniqueId())) {
								if(main.settingParameter.get(p.getUniqueId()).startsWith("create_claim")) {
									p.closeInventory();
									p.sendMessage(main.prefix+"Enter your claim's name in the chat :");
									return;
								} else {
									p.closeInventory();
									p.sendMessage(main.err_prefix+"You are already entering a value in the chat!");
									return;
								}
							} else {
								main.settingParameter.put(p.getUniqueId(), "create_claim_"+t.getUUID().toString()+"_"+String.valueOf(x)+":"+String.valueOf(z)+"_"+w.getName());
								p.closeInventory();
								p.sendMessage(main.prefix+"Enter your claim's name in the chat :");
								p.sendMessage(main.prefix_color+"(Type \"cancel\" to cancel!)");
								return;
							}
						} else {
							p.closeInventory();
							p.sendMessage(main.err_prefix+"You do not have the permission!");
							return;
						}
					} else if(main.isGUIItemTag(item, "check_claim")) {
						if(t.getLeader().equals(pData.getUUID()) || t.playerHas(pData.getUUID(), Permission.TEAM_MANAGE_CLAIMS)) {
							ItemStack claimTool = new ItemStack(Material.GOLDEN_SHOVEL);
							ItemMeta ct = claimTool.getItemMeta();
							ct.setDisplayName("§eClaim Tool");
							ct.addEnchant(Enchantment.POWER, 1, false);
							ct.addItemFlags(ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_ATTRIBUTES);
															
							NamespacedKey key = new NamespacedKey(main, main.claimCheckToolKey);
						    ct.getPersistentDataContainer().set(key, PersistentDataType.BOOLEAN, true);
						    
							claimTool.setItemMeta(ct);
							if(p.getInventory().firstEmpty() == -1) p.getWorld().dropItemNaturally(p.getLocation(), claimTool);
							if(p.getInventory().firstEmpty() != -1) p.getInventory().addItem(claimTool);
							p.closeInventory();
							p.sendMessage(main.prefix+"You received the claim tool!");
							return;
						} else {
							p.closeInventory();
							p.sendMessage(main.err_prefix+"You do not have the permission!");
							return;
						}
					} else if(main.isGUIItemTag(item, "check_claim_return")) {
						if(t.getLeader().equals(pData.getUUID()) || t.playerHas(pData.getUUID(), Permission.TEAM_MANAGE_CLAIMS)) {
							List<Integer> shovel_slots = new ArrayList<>();
							for(int i = 0; i<36; i++) {
								ItemStack stack = p.getInventory().getItem(i);
								if(stack != null) {
									if(main.isClaimTool(stack)) {
										shovel_slots.add(i);
									}
								}
							}
							
							if(shovel_slots.isEmpty()) {
								p.closeInventory();
								p.sendMessage(main.prefix+"There is no claim tool to return!");
								return;
							}
							
							for(int i : shovel_slots) {
								p.getInventory().setItem(i, new ItemStack(Material.AIR));
							}
							p.sendMessage(main.prefix+"You returned the claim tool!");
							GUI.openClaimsGUI(p, pData, t);
							return;
						} else {
							p.closeInventory();
							p.sendMessage(main.err_prefix+"You do not have the permission!");
							return;
						}
					} else if(main.isGUIItemTag(item, "close")) {
						GUI.openTeamGUI(p, t);
						return;
					}
				}
			}
		} else if(e.getView().getTitle().contains("Claim Settings")) {
			if(e.getClickedInventory() == null || !e.getClickedInventory().equals(e.getView().getTopInventory())) return;
			e.setCancelled(true);
			if(item == null || !main.isGUIItem(item)) return;
			if(e.getClick() == ClickType.LEFT || e.getClick() == ClickType.RIGHT || e.getClick() == ClickType.SHIFT_LEFT) {
				PlayerData pData = main.dataManager.getPlayers().stream().filter(tl -> tl.getUUID().equals(p.getUniqueId())).findFirst().orElse(null);
				UUID regionUUID = UUID.fromString(e.getClickedInventory().getItem(13).getItemMeta().getPersistentDataContainer().get(main.GUIItemkey, PersistentDataType.STRING).split("_")[1]);
				Region r = main.dataManager.getRegions().stream().filter(tl -> tl.getUUID().equals(regionUUID)).findFirst().orElse(null);
				String teamName = e.getView().getTitle().split(" ")[2];
				Team t = main.dataManager.getTeams().stream().filter(tl -> tl.getName().equals(teamName)).findFirst().orElse(null);
				
				if(r == null || t == null || (!t.getMembers().contains(pData.getUUID()) && !t.getLeader().equals(pData.getUUID())) || !r.getTeamUUID().equals(t.getUUID())){
					p.closeInventory();
					p.sendMessage(main.err_prefix+"Please try again.");
					return;
				}
				
				if(item.getItemMeta().getPersistentDataContainer().get(main.GUIItemkey, PersistentDataType.STRING).startsWith("region_")) {
					if(t.getLeader().equals(pData.getUUID())) {
						if(e.getClick() == ClickType.SHIFT_LEFT) {
							if(main.settingParameter.containsKey(p.getUniqueId())) {
								if(main.settingParameter.get(p.getUniqueId()).startsWith("claim_rename")) {
									Bukkit.getScheduler().runTaskLater(main, () -> {p.closeInventory();}, 1L);
									p.sendMessage(main.prefix+"Enter your claim's new name in the chat :");
									return;
								} else {
									Bukkit.getScheduler().runTaskLater(main, () -> {p.closeInventory();}, 1L);
									p.sendMessage(main.err_prefix+"You are already entering a new value in the chat!");
									return;
								}
							} else {
								main.settingParameter.put(p.getUniqueId(), "claim_rename_"+r.getUUID().toString());
								Bukkit.getScheduler().runTaskLater(main, () -> {p.closeInventory();}, 1L);
								p.sendMessage(main.prefix+"Enter your claim's new name in the chat :");
								p.sendMessage(main.prefix_color+"(Type \"cancel\" to cancel!)");
								return;
							}
						} else if(e.getClick() == ClickType.LEFT && e.getCursor() != null && e.getCursor().getType() != Material.AIR) {
							ItemStack i = new ItemStack(e.getCursor().getType());
							r.setIcon(i);
							e.getCursor().setAmount(e.getCursor().getAmount()-1);
							GUI.openClaimManagementGUI(p, t, r);
							p.sendMessage(main.prefix+"§aYou successfully set a new claim icon!");
							return;
						}
					}
				} else if(item.getItemMeta().getPersistentDataContainer().get(main.GUIItemkey, PersistentDataType.STRING).startsWith("delete_claim")) {
					if(t.getLeader().equals(pData.getUUID()) || t.playerHas(pData.getUUID(), Permission.TEAM_DELETE_CLAIMS)) {		
						if(r != null && e.getClick() == ClickType.SHIFT_LEFT) {
							List<ClaimedLand> clands = new ArrayList<>();
							for(UUID uuid : r.getLands()) {
								ClaimedLand cc = main.dataManager.getClaimedLands().stream().filter(tl -> tl.getUUID().equals(uuid)).findFirst().orElse(null);
								if(cc != null) clands.add(cc);
							}
							for(ClaimedLand cl : clands) {
								if(cl.getRegionUUID().equals(r.getUUID())) {
									main.dataManager.getClaimedLands().remove(cl);
								}
							}
							
							main.dataManager.getRegions().remove(r);
							
							OfflinePlayer ple = Bukkit.getOfflinePlayer(t.getLeader());
							if(ple != null && ple.isOnline()) {
								((Player) ple).sendMessage(main.prefix+"§cThe claim §4§l"+r.getName()+"§c was deleted by "+p.getName()+" in "+t.getName());
							}
							
							for(UUID pls : t.getMembers()) {
								OfflinePlayer pl = Bukkit.getOfflinePlayer(pls);
								if(pl != null && pl.isOnline()) {
									((Player) pl).sendMessage(main.prefix+"§cThe claim §4"+r.getName()+"§c was deleted by "+p.getName()+" in "+t.getName());
								}
							}
							
							GUI.openClaimsGUI(p, pData, t);
							return;
						}
					} else {
						Bukkit.getScheduler().runTaskLater(main, () -> {p.closeInventory();}, 1L);
						p.sendMessage(main.err_prefix+"You do not have the permission!");
						return;
					}
				}
				if(e.getClick() == ClickType.RIGHT || e.getClick() == ClickType.LEFT) {
					if(main.isGUIItemTag(item, "claim_public_access")) {
						if((t.getLeader().equals(pData.getUUID()) || r.playerHas(p.getUniqueId(), Permission.CLAIM_MANAGE_PUBLIC_ACCESS))) {
							ItemStack doorItem = item.clone();
							List<String> lore = new ArrayList<>();
		
							lore.add(" ");
							if(r.getProperties().get(Property.PUBLIC_ACCESS)) {
								r.setProperty(Property.PUBLIC_ACCESS, false);
								lore.add("§7› Enabled");
								lore.add("§c› Disabled");
								
								kickPlayersOutofRegion(r, 1000);
							} else {
								r.setProperty(Property.PUBLIC_ACCESS, true);
								lore.add("§a› Enabled");
								lore.add("§7› Disabled");
							}
							lore.add("");
							lore.add("§7Click to toggle");
							
							ItemMeta im = doorItem.getItemMeta();
							im.setLore(lore);
							doorItem.setItemMeta(im);
							
							e.getClickedInventory().setItem(e.getSlot(), doorItem);
						}
					} else if(main.isGUIItemTag(item, "claim_titles")) {
						if((t.getLeader().equals(pData.getUUID()) || r.playerHas(p.getUniqueId(), Permission.CLAIM_MANAGE_ENTER_EXIT_MSG))) {
							if(main.settingParameter.containsKey(p.getUniqueId())) {
								if(main.settingParameter.get(p.getUniqueId()).startsWith("claim_set_msg")) {
									Bukkit.getScheduler().runTaskLater(main, () -> {p.closeInventory();}, 1L);
									p.sendMessage(main.prefix+"Enter your claim's new welcome/exit titles and subtitles in the chat :");
									return;
								} else {
									Bukkit.getScheduler().runTaskLater(main, () -> {p.closeInventory();}, 1L);
									p.sendMessage(main.err_prefix+"You are already entering a new value in the chat!");
									return;
								}
							} else {
								main.settingParameter.put(p.getUniqueId(), "claim_set_msg_"+r.getUUID().toString());
								Bukkit.getScheduler().runTaskLater(main, () -> {p.closeInventory();}, 1L);
								p.sendMessage(main.prefix+"Enter your claim's new welcome/exit titles and subtitles in the chat :");
								p.sendMessage(main.prefix_color+"Expected format: Welcome to %region%;Owned by %team%;Leaving %region%;See you soon!");
								p.sendMessage(main.prefix_color+"(Type \"cancel\" to cancel!)");
								return;
							}
						}
					} else if(main.isGUIItemTag(item, "claim_flags")) {
							if(!t.getLeader().equals(pData.getUUID()) && !r.hasPropertyPerm(pData.getUUID())) {
								p.closeInventory();
								p.sendMessage(main.err_prefix+"You do not have the permission!");
								return;
							}
							GUI.openClaimFlagsGUI(p, t, r);
							return;
					} else if(main.isGUIItemTag(item, "claim_hurt_animal")) {
						if(t.getLeader().equals(pData.getUUID()) || r.playerHas(p.getUniqueId(), Permission.CLAIM_MANAGE_HURT_ANIMAL)) {
							ItemStack leatherItem = item.clone();
							List<String> lore = new ArrayList<>();
		
							lore.add(" ");
							if(r.getProperties().get(Property.HURT_ANIMAL)) {
								r.setProperty(Property.HURT_ANIMAL, false);
								lore.add("§7› Enabled");
								lore.add("§c› Disabled");
							} else {
								r.setProperty(Property.HURT_ANIMAL, true);
								lore.add("§a› Enabled");
								lore.add("§7› Disabled");
							}
							lore.add("");
							lore.add("§7Click to toggle");
							
							ItemMeta im = leatherItem.getItemMeta();
							im.setLore(lore);
							leatherItem.setItemMeta(im);
							
							e.getClickedInventory().setItem(e.getSlot(), leatherItem);
						}
					} else if(main.isGUIItemTag(item, "claim_hurt_monster")) {
						if((t.getLeader().equals(pData.getUUID()) || r.playerHas(p.getUniqueId(), Permission.CLAIM_MANAGE_HURT_MONSTER))) {
							ItemStack leatherItem = item.clone();
							List<String> lore = new ArrayList<>();
							
							lore.add(" ");
							if(r.getProperties().get(Property.HURT_MONSTER)) {
								r.setProperty(Property.HURT_MONSTER, false);
								lore.add("§7› Enabled");
								lore.add("§c› Disabled");
							} else {
								r.setProperty(Property.HURT_MONSTER, true);
								lore.add("§a› Enabled");
								lore.add("§7› Disabled");
							}
							lore.add("");
							lore.add("§7Click to toggle");
							
							ItemMeta im = leatherItem.getItemMeta();
							im.setLore(lore);
							leatherItem.setItemMeta(im);
							
							e.getClickedInventory().setItem(e.getSlot(), leatherItem);
						}
					} else if(main.isGUIItemTag(item, "claim_permissions")) {
						if(!t.getLeader().equals(pData.getUUID())) {
							p.closeInventory();
							p.sendMessage(main.err_prefix+"You do not have the permission!");
							return;
						}
						GUI.openClaimPermissionGUI(p, t, r, 1, null);
						return;
					} else if(main.isGUIItemTag(item, "close")) {
						GUI.openClaimsGUI(p, pData, t);
						return;
					}
				}
			}
		} else if(e.getView().getTitle().startsWith("Claim") && e.getView().getTitle().contains("Flags")) {
			if(e.getClickedInventory() == null || !e.getClickedInventory().equals(e.getView().getTopInventory())) return;
			e.setCancelled(true);
			if(item == null || !main.isGUIItem(item)) return;
			if(e.getClick() == ClickType.LEFT || e.getClick() == ClickType.RIGHT) {
				PlayerData pData = main.dataManager.getPlayers().stream().filter(tl -> tl.getUUID().equals(p.getUniqueId())).findFirst().orElse(null);
				String dataTag = e.getClickedInventory().getItem(10).getItemMeta().getPersistentDataContainer().get(main.GUIItemkey, PersistentDataType.STRING);
				UUID regionUUID = UUID.fromString(dataTag.split("_")[1]);
				Region r = main.dataManager.getRegions().stream().filter(tl -> tl.getUUID().equals(regionUUID)).findFirst().orElse(null);
				UUID teamUUID = UUID.fromString(dataTag.split("_")[3]);
				Team t = main.dataManager.getTeams().stream().filter(tl -> tl.getUUID().equals(teamUUID)).findFirst().orElse(null);
								
				if(r == null || t == null || (!t.getMembers().contains(pData.getUUID()) && !t.getLeader().equals(pData.getUUID())) || !r.getTeamUUID().equals(t.getUUID())){
					p.closeInventory();
					p.sendMessage(main.err_prefix+"Please try again.");
					return;
				}
				
				String tag = item.getItemMeta().getPersistentDataContainer().get(main.GUIItemkey, PersistentDataType.STRING);
				
				if(tag.startsWith("region_") && tag.contains("_property_")) {
					Property property = Property.getProperty(tag.split("_")[5].replaceAll("-", "_"));
					Permission permission = Permission.getPermission(tag.split("_")[7].replaceAll("-", "_"));
					if(t.getLeader().equals(pData.getUUID()) || r.playerHas(pData.getUUID(), permission)) {
						ItemStack propItem = item.clone();
						List<String> lore = new ArrayList<>();
	
						boolean isProperty = r.getProperties().get(property);
						r.setProperty(property, !isProperty);
						lore.add(" ");
						if(!isProperty) {
							lore.add("§7State › §aEnabled");
						} else {
							lore.add("§7State › §cDisabled");
						}
						lore.add(" ");
						lore.add("§7Click to toggle");
						
						ItemMeta im = propItem.getItemMeta();
						im.setLore(lore);
						propItem.setItemMeta(im);
						e.getClickedInventory().setItem(e.getSlot(), propItem);
					}
				} else if(main.isGUIItemTag(item, "close")) {
					GUI.openClaimManagementGUI(p, t, r);
					return;
				}
			}
		} else if(e.getView().getTitle().startsWith("Claim") && e.getView().getTitle().contains("Management")) {
			if(e.getClickedInventory() == null || !e.getClickedInventory().equals(e.getView().getTopInventory())) return;
			e.setCancelled(true);
			if(item == null || !main.isGUIItem(item)) return;
			if(e.getClick() == ClickType.LEFT || e.getClick() == ClickType.RIGHT) {
				PlayerData pData = main.dataManager.getPlayers().stream().filter(tl -> tl.getUUID().equals(p.getUniqueId())).findFirst().orElse(null);
				String dataTag = e.getClickedInventory().getItem(4).getItemMeta().getPersistentDataContainer().get(main.GUIItemkey, PersistentDataType.STRING);
				UUID regionUUID = UUID.fromString(dataTag.split("_")[1]);
				Region r = main.dataManager.getRegions().stream().filter(tl -> tl.getUUID().equals(regionUUID)).findFirst().orElse(null);
				UUID teamUUID = UUID.fromString(dataTag.split("_")[3]);
				Team t = main.dataManager.getTeams().stream().filter(tl -> tl.getUUID().equals(teamUUID)).findFirst().orElse(null);
				
				if(r == null || t == null || (!t.getMembers().contains(pData.getUUID()) && !t.getLeader().equals(pData.getUUID()) || !r.getTeamUUID().equals(t.getUUID()))){
					p.closeInventory();
					p.sendMessage(main.err_prefix+"Please try again.");
					return;
				}
				
				String tag = item.getItemMeta().getPersistentDataContainer().get(main.GUIItemkey, PersistentDataType.STRING);
				
				if(tag.contains("member_")) {
					if(!t.getLeader().equals(pData.getUUID())) {
						p.closeInventory();
						p.sendMessage(main.err_prefix+"You do not have the permission!");
						return;
					}
					if(tag.equals("member_global")) {
						GUI.openClaimPermissionGUI(p, t, r, -1, "ALL");
						return;
					}
					if(tag.equals("member_members")) {
						GUI.openClaimPermissionGUI(p, t, r, -1, "MEMBERS");
						return;
					}
					String uuid = tag.split("_")[1];
					GUI.openClaimPermissionGUI(p, t, r, -1, uuid);
					return;
				} else if(main.isGUIItemTag(item, "team_right_arrow")) {
					if(!t.getLeader().equals(pData.getUUID())) {
						p.closeInventory();
						p.sendMessage(main.err_prefix+"You do not have the permission!");
						return;
					}
					String pages = e.getView().getTitle().split(" ")[3];
					int newPage = Integer.valueOf(pages.split("/")[0])+1;
					int maxPage = Integer.valueOf(pages.split("/")[1]);
					if(newPage > maxPage) newPage = maxPage;
					
					GUI.openClaimPermissionGUI(p, t, r, newPage, null);
					return;
				} else if(main.isGUIItemTag(item, "team_left_arrow")) {
					if(!t.getLeader().equals(pData.getUUID())) {
						p.closeInventory();
						p.sendMessage(main.err_prefix+"You do not have the permission!");
						return;
					}
					String pages = e.getView().getTitle().split(" ")[3];
					int newPage = Integer.valueOf(pages.split("/")[0])-1;
					if(newPage < 1) newPage = 1;
					
					GUI.openClaimPermissionGUI(p, t, r, newPage, null);
					return;
				} else if(main.isGUIItemTag(item, "close")) {
					GUI.openClaimManagementGUI(p, t, r);
					return;
				}
			}
		} else if(e.getView().getTitle().startsWith("Claim") && e.getView().getTitle().contains("Permissions")) {
			if(e.getClickedInventory() == null || !e.getClickedInventory().equals(e.getView().getTopInventory())) return;
			e.setCancelled(true);
			if(item == null || !main.isGUIItem(item)) return;
			if(e.getClick() == ClickType.LEFT || e.getClick() == ClickType.RIGHT) {
				PlayerData pData = main.dataManager.getPlayers().stream().filter(tl -> tl.getUUID().equals(p.getUniqueId())).findFirst().orElse(null);
				String dataTag = e.getClickedInventory().getItem(4).getItemMeta().getPersistentDataContainer().get(main.GUIItemkey, PersistentDataType.STRING);
				String targetRaw = dataTag.split("_")[1];
				boolean isGlobal = targetRaw.equals("Global");
				boolean isMembers = targetRaw.equals("Members");
				OfflinePlayer target = null;
				if(!isGlobal && !isMembers) {target = Bukkit.getOfflinePlayer(UUID.fromString(targetRaw));}
				UUID regionUUID = UUID.fromString(dataTag.split("_")[3]);
				UUID teamUUID = UUID.fromString(dataTag.split("_")[5]);
				Region r = main.dataManager.getRegions().stream().filter(tl -> tl.getUUID().equals(regionUUID)).findFirst().orElse(null);
				Team t = main.dataManager.getTeams().stream().filter(tl -> tl.getUUID().equals(teamUUID)).findFirst().orElse(null);
				
				if(r == null || t == null || (!t.getMembers().contains(pData.getUUID()) && !t.getLeader().equals(pData.getUUID()) || !r.getTeamUUID().equals(t.getUUID()))){
					p.closeInventory();
					p.sendMessage(main.err_prefix+"Please try again.");
					return;
				}
				
				String tag = item.getItemMeta().getPersistentDataContainer().get(main.GUIItemkey, PersistentDataType.STRING);
				
				if(tag.contains("claim_permission")) {
					if(t.getLeader().equals(pData.getUUID())) {
						Permission perm = Permission.getPermission(tag.split("_")[2].replaceAll("-", "_"));
						if(isGlobal) {
							e.getClickedInventory().setItem(e.getSlot(), toggleRegionPermission(p, r, "ALL", perm, e.getCurrentItem()));
							p.updateInventory();
						} else if(isMembers) {
							e.getClickedInventory().setItem(e.getSlot(), toggleRegionPermission(p, r, "MEMBERS", perm, e.getCurrentItem()));
							p.updateInventory();
						} else {
							e.getClickedInventory().setItem(e.getSlot(), toggleRegionPermission(p, r, target.getUniqueId().toString(), perm, e.getCurrentItem()));
							p.updateInventory();
						}
						return;
					}
				} else if(main.isGUIItemTag(item, "close")) {
					GUI.openClaimPermissionGUI(p, t, r, 1, null);
					return;
				}
			}
		} else if(e.getView().getTitle().contains("Claim Blocks")) {
			if(e.getClickedInventory() == null || !e.getClickedInventory().equals(e.getView().getTopInventory())) return;
			e.setCancelled(true);
			if(item == null || !main.isGUIItem(item)) return;
			if(e.getClick() == ClickType.LEFT || e.getClick() == ClickType.RIGHT) {
				PlayerData pData = main.dataManager.getPlayers().stream().filter(tl -> tl.getUUID().equals(p.getUniqueId())).findFirst().orElse(null);
				String teamName = e.getView().getTitle().split(" ")[0];
				Team t = main.dataManager.getTeams().stream().filter(tl -> tl.getName().equals(teamName)).findFirst().orElse(null);
				
				if(t == null || (!t.getMembers().contains(pData.getUUID()) && !t.getLeader().equals(pData.getUUID()))){
					p.closeInventory();
					p.sendMessage(main.err_prefix+"Please try again.");
					return;
				}
				
				if(main.isGUIItemTag(item, "deposit_claim_block")) {
					if(pData.getClaimBlocks() > 0) {
						GUI.openClaimBlockGUI2(p, pData, t, "deposit");
						return;
					} else {
						p.closeInventory();
						p.sendMessage(main.err_prefix+"You do not have enough claim blocks!");
						return;
					}
				} else if(main.isGUIItemTag(item, "withdraw_claim_block")) {
					if(t.getLeader().equals(pData.getUUID()) || t.playerHas(pData.getUUID(), Permission.TEAM_WITHDRAW_CLAIM_BLOCKS)) {
						if(t.getClaimBlocks() > 0) {
							GUI.openClaimBlockGUI2(p, pData, t, "withdraw");
							return;
						} else {
							p.closeInventory();
							p.sendMessage(main.err_prefix+"Your team does not have any claim blocks!");
							return;
						}
					} else {
						p.closeInventory();
						p.sendMessage(main.err_prefix+"You do not have the permission!");
						return;
					}
				} else if(main.isGUIItemTag(item, "close")) {
					GUI.openTeamGUI(p, t);
					return;
				}
			}
		} else if(e.getView().getTitle().contains("Withdraw") || e.getView().getTitle().contains("Deposit")) {
			if(e.getClickedInventory() == null || !e.getClickedInventory().equals(e.getView().getTopInventory())) return;
			e.setCancelled(true);
			if(item == null || !main.isGUIItem(item)) return;
			if(e.getClick() == ClickType.LEFT || e.getClick() == ClickType.RIGHT) {
				if(e.getClickedInventory().getItem(13) != null) {
					int amount = Integer.valueOf(e.getClickedInventory().getItem(13).getItemMeta().getLore().get(0).split(" ")[1]);
					PlayerData pData = main.dataManager.getPlayers().stream().filter(tl -> tl.getUUID().equals(p.getUniqueId())).findFirst().orElse(null);
					String teamName = e.getView().getTitle().split(" ")[0];
					Team t = main.dataManager.getTeams().stream().filter(tl -> tl.getName().equals(teamName)).findFirst().orElse(null);
					
					if(t == null || (!t.getMembers().contains(pData.getUUID()) && !t.getLeader().equals(pData.getUUID()))){
						p.closeInventory();
						p.sendMessage(main.err_prefix+"Please try again.");
						return;
					}
					
					if(main.isGUIItemTag(item, "cb_withdraw")) {
						if(!t.getLeader().equals(pData.getUUID()) || t.playerHas(pData.getUUID(), Permission.TEAM_WITHDRAW_CLAIM_BLOCKS)) {
							p.closeInventory();
							p.sendMessage(main.err_prefix+"You do not have the permission!");
							return;
						}
						
						if(amount > t.getClaimBlocks()) {
							p.closeInventory();
							p.sendMessage(main.err_prefix+"Your team does not have enough claim blocks!");
							return;
						}
						
						int newcbamount = t.getClaimBlocks()-amount;
						t.setClaimBlocks(newcbamount);
						int playercbamount = pData.getClaimBlocks()+amount;
						pData.setClaimBlocks(playercbamount);
						p.closeInventory();
						p.sendMessage(main.prefix+"§aSuccesfully withdrew "+amount+" claim blocks from "+t.getName()+"'s claim block pool!");
						TextComponent textcom = new TextComponent("+"+amount+" "+main.claim_block.getItemMeta().getDisplayName());
						p.spigot().sendMessage(ChatMessageType.ACTION_BAR, new BaseComponent[]{textcom});
						return;
					} else if(main.isGUIItemTag(item, "cb_deposit")) {
						if(amount > pData.getClaimBlocks()) {
							p.closeInventory();
							p.sendMessage(main.err_prefix+"You do not have enough claim blocks!");
							return;
						}
						
						int playercbamount = pData.getClaimBlocks()-amount;
						pData.setClaimBlocks(playercbamount);
						int newcbamount = t.getClaimBlocks()+amount;
						t.setClaimBlocks(newcbamount);
						p.closeInventory();
						p.sendMessage(main.prefix+"§aSuccesfully deposited "+amount+" claim blocks into "+t.getName()+" claim block pool!");
						TextComponent textcom = new TextComponent("-"+amount+" "+main.claim_block.getItemMeta().getDisplayName());
						p.spigot().sendMessage(ChatMessageType.ACTION_BAR, new BaseComponent[]{textcom});
						return;
					} else if(main.isGUIItemTag(item, "cb_add_1") || main.isGUIItemTag(item, "cb_add_10") || main.isGUIItemTag(item, "cb_remove_1") || main.isGUIItemTag(item, "cb_remove_10")) {
						int addAmount = 0;
						if(main.isGUIItemTag(item, "cb_remove_1") || main.isGUIItemTag(item, "cb_remove_10")) addAmount = 0-Integer.valueOf(e.getCurrentItem().getItemMeta().getDisplayName().split(" ")[1]);
						if(main.isGUIItemTag(item, "cb_add_1") || main.isGUIItemTag(item, "cb_add_10")) addAmount = Integer.valueOf(e.getCurrentItem().getItemMeta().getDisplayName().split(" ")[1]);
						
						int newamount = amount+addAmount;
						if(newamount < 1) {
							newamount = 1;
						}
						if(e.getView().getTitle().contains("Withdraw")) {
							if(newamount > t.getClaimBlocks()) {
								newamount = t.getClaimBlocks();
							}
						}
						if(e.getView().getTitle().contains("Deposit")) {
							if(newamount > pData.getClaimBlocks()) {
								newamount = pData.getClaimBlocks();
							}
						}
						
						ItemStack x = new ItemStack(e.getClickedInventory().getItem(13).getType(), newamount);
						ItemMeta xu = x.getItemMeta();
						List<String> lore = new ArrayList<>();
						for(String s : e.getClickedInventory().getItem(13).getItemMeta().getLore()) {
							lore.add(replaceNumbers(s, newamount));
						}
						if(e.getView().getTitle().contains("Withdraw")) {
							xu.setDisplayName("§c§lWithdraw"); 
							xu.getPersistentDataContainer().set(main.GUIItemkey, PersistentDataType.STRING, "cb_withdraw");
						}
						if(e.getView().getTitle().contains("Deposit")) {
							xu.setDisplayName("§a§lDeposit"); 
							xu.getPersistentDataContainer().set(main.GUIItemkey, PersistentDataType.STRING, "cb_deposit");
						}
						xu.setLore(lore);
						x.setItemMeta(xu);
						e.getClickedInventory().setItem(13, x);
						p.updateInventory();
						return;
					} else if(e.getCurrentItem().getType() == Material.BARRIER) {
						GUI.openClaimBlockGUI(p, pData, t);
						return;
					}
				}
			}
		}
	}
	
	public String replaceNumbers(String str, int newInt) {
        String replacement = Integer.toString(newInt);
        return str.replaceAll("\\b\\d+\\b", replacement);
    }
	
	@EventHandler
	public void onChat(AsyncPlayerChatEvent e){
		Player p = e.getPlayer();	
		if(!main.settingParameter.containsKey(p.getUniqueId())) return;
		String s = e.getMessage();
		String se = main.settingParameter.get(p.getUniqueId());
		
		if(se.startsWith("team_rename")) {
			e.setCancelled(true);
			if(s.equals("cancel")) {
				p.sendMessage(main.prefix+"Renaming team has been cancelled!");
				main.settingParameter.remove(p.getUniqueId());
				return;
			}
			UUID teamUUID = UUID.fromString(se.split("_")[2]);
			Team t = main.dataManager.getTeams().stream().filter(tl -> tl.getUUID().equals(teamUUID)).findFirst().orElse(null);
			if(t == null) {
				p.sendMessage(main.err_prefix+"Could not find the team you were going to rename!");
				main.settingParameter.remove(p.getUniqueId());
				return;
			}
			
			if(!t.getLeader().equals(p.getUniqueId())) {
				p.sendMessage(main.err_prefix+"You are not the leader of "+t.getName()+"!");
				main.settingParameter.remove(p.getUniqueId());
				return;
			}
			
			if(s.length() > 16) {
				p.sendMessage(main.err_prefix+"Your new team name cannot be longer than 16 characters!");
				p.sendMessage(main.prefix+"Enter your team's new name in the chat :");
				return;
			}
			if(main.containsSpecialCharacter(s)) {
				p.sendMessage(main.err_prefix+"Your new team name cannot contain special characters!");
				p.sendMessage(main.prefix+"Enter your team's new name in the chat :");
				return;
			}
			if(s.equals(t.getName())) {
				p.sendMessage(main.err_prefix+"Your new team name must be different from the current one!");
				p.sendMessage(main.prefix+"Enter your team's new name in the chat :");
				return;
			}
			
			boolean isNameUsed = false;
			for(Team team : main.dataManager.getTeams()) {
				if(team.getName().equalsIgnoreCase(s)) {
					isNameUsed = true;
				}
			}
			
			if(isNameUsed) {
				p.sendMessage(main.err_prefix+"That team name is already in use!");
				p.sendMessage(main.prefix+"Enter your team's new name in the chat :");
				return;
			}
			
			t.setName(s);
			main.settingParameter.remove(p.getUniqueId());
			p.sendMessage(main.prefix+"§aSuccessfully set your team's new name to §l"+s+"§r§a!");
			
			boolean notified = false;
			for(UUID member : t.getMembers()) {
				OfflinePlayer pl = Bukkit.getOfflinePlayer(member);
				if(pl != null && pl.isOnline()) {
					((Player) pl).sendMessage(main.prefix+"Your team name has been changed to §r§l"+s+main.prefix_color+" by the leader!");
					notified = true;
				}
			}
			
			if(notified) p.sendMessage("§aOnline members of your team have been notified!");
			return;
		} else if(se.startsWith("team_invite")) {
			e.setCancelled(true);
			if(s.equals("cancel")) {
				p.sendMessage(main.prefix+"Creating invite has been cancelled!");
				main.settingParameter.remove(p.getUniqueId());
				return;
			}
			PlayerData pData = main.dataManager.getPlayers().stream().filter(tl -> tl.getUUID().equals(p.getUniqueId())).findFirst().orElse(null);
			if(pData.getTeamUUIDs() == null || pData.getTeamUUIDs().isEmpty()) {
				p.sendMessage(main.err_prefix+"You must be in a team!");
				main.settingParameter.remove(p.getUniqueId());
				return;
			}
			UUID teamUUID = UUID.fromString(se.split("_")[2]);
			Team t = main.dataManager.getTeams().stream().filter(tl -> tl.getUUID().equals(teamUUID)).findFirst().orElse(null);
			if(t == null) {
				p.sendMessage(main.err_prefix+"Could not find the team you were going to invite the player to!");
				main.settingParameter.remove(p.getUniqueId());
				return;
			}
			if(!t.getMembers().contains(p.getUniqueId()) && !t.getLeader().equals(p.getUniqueId())) {
				p.sendMessage(main.err_prefix+"You are not apart of that team!");
				main.settingParameter.remove(p.getUniqueId());
				return;
			}
			if(!t.getLeader().equals(p.getUniqueId()) && !t.playerHas(p.getUniqueId(), Permission.TEAM_CREATE_INVITES)) {
				p.sendMessage(main.err_prefix+"You do not have the permission!");
				main.settingParameter.remove(p.getUniqueId());
				return;
			}
			@SuppressWarnings("deprecation")
			OfflinePlayer target = Bukkit.getOfflinePlayer(s);
			if(target == p) {
				p.sendMessage(main.err_prefix+"You cannot invite yourself!");
				p.sendMessage(main.prefix+"Enter the name of the player you wish to invite in the chat :");
				return;
			}
			if(target == null || Bukkit.getOfflinePlayer(target.getUniqueId()).getName().equals("null")) {
				p.sendMessage(main.err_prefix+"Could not find any player named §l"+s+"§r"+main.err_color+"!");
				p.sendMessage(main.prefix+"Enter the name of the player you wish to invite in the chat :");
				return;
			}
				
			if(t.getMembers().contains(target.getUniqueId()) || t.getLeader().equals(target.getUniqueId())) {
				p.sendMessage(main.err_prefix+"Could not invite §l"+s+"§r"+main.err_color+" to the team as they are already apart of it!");
				p.sendMessage(main.prefix+"Enter the name of the player you wish to invite in the chat :");
				return;
			}
			
			Invite invite = null;
			
			for(Invite i : t.getInvites()) {
				if(i.getInvitee().equals(target.getUniqueId())) {
					invite = i;
				}
			}
			
			if(invite != null) {
				if(invite.getInviter().equals(p.getUniqueId())) {
					p.sendMessage(main.err_prefix+"You have already invited that player!");
					main.settingParameter.remove(p.getUniqueId());
					return;
				}
				p.sendMessage(main.err_prefix+"That player has already been invited to "+t.getName()+"!");
				main.settingParameter.remove(p.getUniqueId());
				return;
			}
			
			p.sendMessage(main.prefix+"§eYou have invited §f"+target.getName()+"§e to join §f"+t.getName()+"§e!");
			if(target.isOnline()) {
				((Player) target).sendMessage(main.prefix+"§eYou have been invited by §f"+p.getName()+"§e to join §f"+t.getName()+"§e!");
				TextComponent textcom = new TextComponent(main.prefix);
				TextComponent textcom2 = new TextComponent("§eClick ");
				TextComponent textcom3 = new TextComponent("§ahere");
				TextComponent textcom4 = new TextComponent(" §eto accept it or ");
				TextComponent textcom5 = new TextComponent("§chere");
				TextComponent textcom6 = new TextComponent(" §eto deny it!");
				textcom3.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/invite accept "+t.getName()));
				textcom5.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/invite deny "+t.getName()));
				textcom.addExtra(textcom2);
				textcom.addExtra(textcom3);
				textcom.addExtra(textcom4);
				textcom.addExtra(textcom5);
				textcom.addExtra(textcom6);
				((Player) target).spigot().sendMessage(ChatMessageType.CHAT, new BaseComponent[]{textcom});
			}
			invite = new Invite(p.getUniqueId(), target.getUniqueId(), main.invite_expiry_time, System.currentTimeMillis());
			t.getInvites().add(invite);
			main.settingParameter.remove(p.getUniqueId());
			return;
		} else if(se.startsWith("claim_rename")) {
			e.setCancelled(true);
			if(s.equals("cancel")) {
				p.sendMessage(main.prefix+"Renaming claim has been cancelled!");
				main.settingParameter.remove(p.getUniqueId());
				return;
			}
			
			UUID regionUUID = UUID.fromString(se.split("_")[2]);
			Region r = main.dataManager.getRegions().stream().filter(tl -> tl.getUUID().equals(regionUUID)).findFirst().orElse(null);
			
			if(r == null) {
				p.sendMessage(main.err_prefix+"Could not find the claim you were going to rename!");
				main.settingParameter.remove(p.getUniqueId());
				return;
			}
			
			Team t = main.dataManager.getTeams().stream().filter(tl -> tl.getUUID().equals(r.getTeamUUID())).findFirst().orElse(null);
			
			if(t == null) {
				p.sendMessage(main.err_prefix+"Could not find the team you were going to rename the claim for!");
				main.settingParameter.remove(p.getUniqueId());
				return;
			}
			
			if(!t.getLeader().equals(p.getUniqueId())) {
				p.sendMessage(main.err_prefix+"You are not the leader of "+t.getName()+"!");
				main.settingParameter.remove(p.getUniqueId());
				return;
			}
			
			if(s.length() > 16) {
				p.sendMessage(main.err_prefix+"Your new claim name cannot be longer than 16 characters!");
				p.sendMessage(main.prefix+"Enter your claim's new name in the chat :");
				return;
			}
			
			String specialCharacters = "[^a-zA-Z0-9 ]";
			if(s.matches(".*" + specialCharacters + ".*")) {
				p.sendMessage(main.err_prefix+"Your new claim name cannot contain special characters!");
				p.sendMessage(main.prefix+"Enter your claim's new name in the chat :");
				return;
			}
			if(s.equals(r.getName())) {
				p.sendMessage(main.err_prefix+"Your new claim name must be different from the current one!");
				p.sendMessage(main.prefix+"Enter your claim's new name in the chat :");
				return;
			}
			
			r.setName(s);
			main.settingParameter.remove(p.getUniqueId());
			p.sendMessage(main.prefix+"§aSuccessfully set your claim's new name to §l"+s+"§r§a!");
			return;
		} else if(se.startsWith("claim_set_msg")) {
			e.setCancelled(true);
			if(s.equals("cancel")) {
				p.sendMessage(main.prefix+"Setting of new team titles has been cancelled!");
				main.settingParameter.remove(p.getUniqueId());
				return;
			}
			
			UUID regionUUID = UUID.fromString(se.split("_")[3]);
			Region r = main.dataManager.getRegions().stream().filter(tl -> tl.getUUID().equals(regionUUID)).findFirst().orElse(null);
			
			if(r == null) {
				p.sendMessage(main.err_prefix+"Could not find the claim you were going to set values for!");
				main.settingParameter.remove(p.getUniqueId());
				return;
			}
			
			Team t = main.dataManager.getTeams().stream().filter(tl -> tl.getUUID().equals(r.getTeamUUID())).findFirst().orElse(null);
			
			if(t == null) {
				p.sendMessage(main.err_prefix+"Could not find the team you were going to set claim values for!");
				main.settingParameter.remove(p.getUniqueId());
				return;
			}
			
			if(!t.getLeader().equals(p.getUniqueId()) && !r.playerHas(p.getUniqueId(), Permission.CLAIM_MANAGE_ENTER_EXIT_MSG)) {
				p.sendMessage(main.err_prefix+"You do not have the permission!");
				main.settingParameter.remove(p.getUniqueId());
				return;
			}
			
			if(s.chars().filter(c -> c == ';').count() != 3) {
				p.sendMessage(main.err_prefix+"Your claim's titles and subtitles must be in the right format!");
				p.sendMessage(main.err_color+"welcome_title;welcome_subtitle;exit_title;exit_subtitle");
				return;
			}
			
			String[] titles = s.split(";");
			for(String title : titles) {
				String specialCharacters = "[^a-zA-Z0-9&%!. ]";
				if(title.matches(".*" + specialCharacters + ".*")) {
					p.sendMessage(title);
				    p.sendMessage(main.err_prefix + "Some special characters are not allowed!");
				    p.sendMessage(main.prefix + "Enter your claim's new welcome/exit titles and subtitles in the chat :");
				    return;
				}
			}
			List<String> titleList = Arrays.asList(titles);
			r.setTitles(titleList);
			main.settingParameter.remove(p.getUniqueId());
			p.sendMessage(main.prefix+"§aSuccessfully set your claim's new welcome/exit titles and subtitles!");
			return;
		} else if(se.startsWith("set_region_area")) {
			e.setCancelled(true);
			if(s.equals("cancel")) {
				p.sendMessage(main.prefix+"Extending claim has been cancelled!");
				main.settingParameter.remove(p.getUniqueId());
				return;
			}
			
			PlayerData pData = main.dataManager.getPlayers().stream().filter(tl -> tl.getUUID().equals(p.getUniqueId())).findFirst().orElse(null);
			if(pData.getTeamUUIDs().isEmpty()) {
				p.sendMessage(main.err_prefix+"In order to be able to claim lands, you should be in a team or own one!");
				main.settingParameter.remove(p.getUniqueId());
				return;
			}
			
			int x1 = Integer.valueOf(se.split("_")[3].split(":")[0]);
			int z1 = Integer.valueOf(se.split("_")[3].split(":")[1]);
			int x2 = Integer.valueOf(se.split("_")[4].split(":")[0]);
			int z2 = Integer.valueOf(se.split("_")[4].split(":")[1]);
			String w = se.split("_")[5];
			
			String[] possibleRegionNames = se.split("_")[6].split("/");
						
			List<UUID> possibleRegions = new ArrayList<>();
			for(String pr : possibleRegionNames) {
				possibleRegions.add(UUID.fromString(pr));
			}
			
			Region r = null;
			for(Region reg : main.dataManager.getRegions()) {
				for(UUID regionUUID : possibleRegions) {
					if(reg.getUUID().equals(regionUUID)) {
						if(s.equalsIgnoreCase(reg.getName())) {
							r = reg;
						}
					}
				}
			}
			
			if(r == null) {
				p.sendMessage(main.err_prefix+"Could not find the claim you were going to extend!");
				main.settingParameter.remove(p.getUniqueId());
				return;
			}
			
			UUID teamUUID = r.getTeamUUID();
			Team t = main.dataManager.getTeams().stream().filter(tl -> tl.getUUID().equals(teamUUID)).findFirst().orElse(null);
			
			if(t == null) {
				p.sendMessage(main.err_prefix+"Could not find the team you were going to extend a claim for!");
				main.settingParameter.remove(p.getUniqueId());
				return;
			}
			
			if(!(t.getMembers().contains(p.getUniqueId()) && t.playerHas(p.getUniqueId(), Permission.TEAM_MANAGE_CLAIMS)) && !t.getLeader().equals(p.getUniqueId())) {
				p.sendMessage(main.err_prefix+"You do not have the permission to extend claims for"+t.getName());
				main.settingParameter.remove(p.getUniqueId());
				return;
			}
			
			int min_x = Math.min(x1, x2);
			int max_x = Math.max(x1, x2);
			int min_z = Math.min(z1, z2);
			int max_z = Math.max(z1, z2);
			int blockCount = 0;
			for(int o = min_x; o <= max_x; o++) {
			    for(int y = min_z; y <= max_z; y++) {
			    	int x = o;
			    	int z = y;
			    	ClaimedLand c = main.dataManager.getClaimedLands().stream().filter(cl -> cl.getX() == x && cl.getZ() == z && cl.getWorldName().equals(w)).findFirst().orElse(null);
					if(c != null) {
						p.sendMessage(main.err_prefix+"The selected area contains claimed lands. Try again with a different area!");
						main.settingParameter.remove(p.getUniqueId());
						return;
					}
					
					List<Region> d = getRegionsAroundCoords(o, y, w);
					
					if(!d.contains(r)) {
						p.sendMessage(main.err_prefix+"Could not find the claim you were going to extend!");
						main.settingParameter.remove(p.getUniqueId());
						return;
					}
					blockCount++;
			    }
			}
			
			int teamBlocks = t.getClaimBlocks();
			
			if(blockCount > teamBlocks) {
				p.sendMessage(main.err_prefix+"Your team does not have enough claim blocks!");
				p.sendMessage(main.err_color+"Try depositing some in the team claim block pool.");
				main.settingParameter.remove(p.getUniqueId());
				return;
			}
			
			t.setClaimBlocks(teamBlocks-blockCount);
			
			for(int o = min_x; o <= max_x; o++) {
			    for(int y = min_z; y <= max_z; y++) {
					UUID uuid = UUID.randomUUID();
					main.dataManager.getClaimedLands().add(new ClaimedLand(uuid, r.getUUID(), w, o, y));
					r.getLands().add(uuid);
			    }
			}
			
			main.settingParameter.remove(p.getUniqueId());
			main.settingClaimArea.remove(p.getUniqueId());
			p.playSound(p.getLocation(), Sound.ENTITY_VILLAGER_TRADE, 1, 2);
			p.sendMessage(main.prefix+"§aYou claimed a new land for "+t.getName()+"!");
			return;
		} else if(se.startsWith("create_claim")) {
			e.setCancelled(true);
			if(s.equals("cancel")) {
				p.sendMessage(main.prefix+"Creating new claim has been cancelled!");
				main.settingParameter.remove(p.getUniqueId());
				return;
			}
			
			UUID teamUUID = UUID.fromString(se.split("_")[2]);
			Team t = main.dataManager.getTeams().stream().filter(tl -> tl.getUUID().equals(teamUUID)).findFirst().orElse(null);
			
			if(t == null) {
				p.sendMessage(main.err_prefix+"Could not find the team you were going to create a claim for!");
				main.settingParameter.remove(p.getUniqueId());
				return;
			}
			
			if(!t.getLeader().equals(p.getUniqueId()) && !t.playerHas(p.getUniqueId(), Permission.TEAM_MANAGE_CLAIMS)) {
				p.sendMessage(main.err_prefix+"You do not have the permission!");
				main.settingParameter.remove(p.getUniqueId());
				return;
			}
			
			
			List<Region> regions = new ArrayList<Region>();
			for(Region r : main.dataManager.getRegions()) {
				if(r.getTeamUUID().equals(t.getUUID())) {
					regions.add(r);
				}
			}
			
			if(regions.size() >= 15) {
				p.sendMessage(main.err_prefix+"Your team has reached the maximum amount of claims!");
				main.settingParameter.remove(p.getUniqueId());
				return;
			}
			if(t.getClaimBlocks() < 1) {
				p.sendMessage(main.err_prefix+"Your team does not have enough claim blocks!");
				p.sendMessage(main.err_color+"Try depositing some in the team claim block pool.");
				main.settingParameter.remove(p.getUniqueId());
				return;
			}
			
			int x = Integer.valueOf(se.split("_")[3].split(":")[0]);
			int z = Integer.valueOf(se.split("_")[3].split(":")[1]);
			String w = se.split("_")[4];
			ClaimedLand c = main.dataManager.getClaimedLands().stream().filter(cl -> cl.getX() == x && cl.getZ() == z && cl.getWorldName().equals(w)).findFirst().orElse(null);
			if(c != null) {
				Region r = main.dataManager.getRegions().stream().filter(cl -> cl.getUUID().equals(c.getRegionUUID())).findFirst().orElse(null);
				Team team = main.dataManager.getTeams().stream().filter(tl -> tl.getUUID().equals(r.getTeamUUID())).findFirst().orElse(null);
				if(team == null) {
					p.sendMessage(main.err_prefix+"You cannot claim that land!");
					main.settingParameter.remove(p.getUniqueId());
					return;
				}
				p.sendMessage(main.err_prefix+"This is §6"+team.getName()+main.err_color+"'s land!");
				main.settingParameter.remove(p.getUniqueId());
				return;
			}
			
			if(s.length() > 16) {
				p.sendMessage(main.err_prefix+"Your claim name cannot be longer than 16 characters!");
				p.sendMessage(main.prefix+"Enter your claim's name in the chat :");
				return;
			}
			
			String specialCharacters = "[^a-zA-Z0-9 ]";
			if(s.matches(".*" + specialCharacters + ".*")) {
				p.sendMessage(main.err_prefix+"Your claim name cannot contain special characters!");
				p.sendMessage(main.prefix+"Enter your claim's name in the chat :");
				return;
			}
			
			UUID regionUUID = UUID.randomUUID();
			UUID claimUUID = UUID.randomUUID();
			Map<String, List <Permission>> perms = new HashMap<>();
			perms.put("MEMBERS", new ArrayList<>(Arrays.asList(Permission.CLAIM_ACCESS, Permission.CLAIM_INTERACT, Permission.CLAIM_PLACE_BREAK_BLOCK)));
			Region region = new Region(regionUUID, t.getUUID(), new ArrayList<>(Arrays.asList(claimUUID)), s, null, perms, null, null, null);
			main.dataManager.getRegions().add(region);
			main.dataManager.getClaimedLands().add(new ClaimedLand(claimUUID, regionUUID, w, x, z));
			
			p.playSound(p.getLocation(), Sound.ENTITY_VILLAGER_TRADE, 1, 2);
			p.sendMessage(main.prefix+"§aSuccessfully created new claim §l"+region.getName()+"§r§a for §l"+t.getName()+"§r§a!");
			main.settingParameter.remove(p.getUniqueId());
			return;
		}
	}
	
	@EventHandler
    public void onPlayerDeath(PlayerDeathEvent e) {
        List<ItemStack> is = new ArrayList<>();
        for(ItemStack i : e.getDrops()) {
        	if(i != null) {
        		if(main.isClaimTool(i)) is.add(i);
        	}
        }
        
        for(ItemStack i : is) {
        	e.getDrops().remove(i);
        }
    }
	
	@EventHandler
    public void onDrop(PlayerDropItemEvent e) {
        if(main.isClaimTool(e.getItemDrop().getItemStack())) {
        	Player p = e.getPlayer();
        	boolean toDelete = true;
        	for(Team t : main.dataManager.getTeams()) {
        		if(t.getLeader().equals(p.getUniqueId()) || t.playerHas(p.getUniqueId(), Permission.TEAM_MANAGE_CLAIMS)) {
        			toDelete = false;
        		}
        	}
        	if(toDelete) e.getItemDrop().getItemStack().setAmount(0);
        	e.setCancelled(true);
        	return;
        }
    }

	
	@EventHandler
	public void onBucketEmpty(PlayerBucketEmptyEvent e){
		Player p = e.getPlayer();
		int x = e.getBlockClicked().getRelative(e.getBlockFace()).getX();
		int z = e.getBlockClicked().getRelative(e.getBlockFace()).getZ();
		World w = e.getBlockClicked().getWorld();
		if(e.getBucket() == Material.LAVA_BUCKET || e.getBucket() == Material.WATER_BUCKET){
			ClaimedLand c = main.dataManager.getClaimedLands().stream().filter(cl -> cl.getX() == x && cl.getZ() == z && Bukkit.getWorld(cl.getWorldName()) == w).findFirst().orElse(null);
			if(c != null) {
				Region r = main.dataManager.getRegions().stream().filter(cl -> cl.getUUID().equals(c.getRegionUUID())).findFirst().orElse(null);
				OfflinePlayer pl = Bukkit.getOfflinePlayer(main.dataManager.getTeams().stream().filter(t -> t.getUUID().equals(r.getTeamUUID())).findFirst().orElse(null).getLeader());
				if(!pl.getUniqueId().equals(p.getUniqueId())) {
					p.sendMessage(main.err_prefix+"This is §6"+pl.getName()+main.err_color+"'s land!");
					e.setCancelled(true);
					return;
				}
			 }
		 }
	 }
	
	@EventHandler
	public void onBlockFromTo(BlockFromToEvent e) {
		Material id = e.getBlock().getType();
	  	if(id == Material.WATER || id == Material.LAVA) {
		    int x = e.getBlock().getLocation().getBlockX();
		    int z = e.getBlock().getLocation().getBlockZ();
		    int x1 = e.getToBlock().getLocation().getBlockX();
		    int z1 = e.getToBlock().getLocation().getBlockZ();
		    World w = e.getBlock().getWorld();
		    if(x != x1 || z != z1) {
		    	ClaimedLand c = main.dataManager.getClaimedLands().stream().filter(cl -> cl.getX() == x && cl.getZ() == z && Bukkit.getWorld(cl.getWorldName()) == w).findFirst().orElse(null);
		    	ClaimedLand c1 = main.dataManager.getClaimedLands().stream().filter(cl -> cl.getX() == x1 && cl.getZ() == z1 && Bukkit.getWorld(cl.getWorldName()) == w).findFirst().orElse(null);
		    	if(c != null || c1 != null) {
		    		if(c != null && c1 == null) {
		    	    	e.setCancelled(true);
		    	    	return;
		    	    }
		    		if(c == null && c1 != null) {
		    	    	e.setCancelled(true);
		    	    	return;
		    	    }
		    		if(c != null && c1 != null) {
		    			Region r = main.dataManager.getRegions().stream().filter(cl -> cl.getUUID().equals(c.getRegionUUID())).findFirst().orElse(null);
			    		Region r1 = main.dataManager.getRegions().stream().filter(cl -> cl.getUUID().equals(c1.getRegionUUID())).findFirst().orElse(null);
		    			if(!r.getTeamUUID().equals(r1.getTeamUUID())) {
		    				e.setCancelled(true);
			    	    	return;
		    			}
		    	    }
			    }
		    }
	  	}
	}
	
	@EventHandler
	public void onInteract(PlayerInteractEvent e){
		Player p = e.getPlayer();
		if(e.getItem() != null && e.getItem().getType() == Material.GOLDEN_SHOVEL && main.isClaimTool(e.getItem())) {
			e.setCancelled(true);
			if(e.getAction() == Action.LEFT_CLICK_AIR) {
				if(main.settingClaimArea.containsKey(p.getUniqueId())) {
					p.sendMessage(main.prefix+"Click on a block to set pos2!");
					return;
				}
				p.sendMessage(main.prefix+"Click on a block to set pos1!");
				return;
			} else if(e.getAction() == Action.LEFT_CLICK_BLOCK) {
				if(p.isSneaking() && main.settingClaimArea.containsKey(p.getUniqueId())) {
					main.settingClaimArea.remove(p.getUniqueId());
					p.sendMessage(main.prefix+"Unselected pos1!");
					return;
				}
				Location l = e.getClickedBlock().getLocation();
				int x1 = l.getBlockX();
				int z1 = l.getBlockZ();
				String w = l.getWorld().getName();
				if(main.settingClaimArea.containsKey(p.getUniqueId())) {
					String pos1 = main.settingClaimArea.get(p.getUniqueId()).get(0);
					int x2 = Integer.valueOf(pos1.split(":")[0]);
					int z2 = Integer.valueOf(pos1.split(":")[1]);
					String w1 = pos1.split(":")[2];
					
					
					PlayerData pData = main.dataManager.getPlayers().stream().filter(tl -> tl.getUUID().equals(p.getUniqueId())).findFirst().orElse(null);
					if(pData.getTeamUUIDs().isEmpty()) {
						p.sendMessage(main.err_prefix+"In order to be able to claim lands, you should be in a team or own one!");
						return;
					}
					
					if(w.equals(w1)) {
						int min_x = Math.min(x1, x2);
						int max_x = Math.max(x1, x2);
						int min_z = Math.min(z1, z2);
						int max_z = Math.max(z1, z2);
						List<Region> regions = new ArrayList<>();
						int blockCount = 0;
						for(int o = min_x; o <= max_x; o++) {
						    for(int y = min_z; y <= max_z; y++) {
						    	int x = o;
						    	int z = y;
						    	ClaimedLand c = main.dataManager.getClaimedLands().stream().filter(cl -> cl.getX() == x && cl.getZ() == z && cl.getWorldName().equals(w)).findFirst().orElse(null);
								if(c != null) {
									p.sendMessage(main.err_prefix+"The selected area contains claimed lands. Try again with a different area!");
									return;
								}
								
								List<Region> d = getRegionsAroundCoords(o, y, w);
								
								for(Region cl : d) {
									Team t = main.dataManager.getTeams().stream().filter(tl -> tl.getUUID().equals(cl.getTeamUUID())).findFirst().orElse(null);
									if((t.getMembers().contains(p.getUniqueId()) && t.playerHas(p.getUniqueId(), Permission.TEAM_MANAGE_CLAIMS)) || t.getLeader().equals(p.getUniqueId())) {
										if(!regions.contains(cl)) {
											regions.add(cl);
										}
									}
								}
								blockCount++;
						    }
						}
										
						if(regions.isEmpty()) {
							p.sendMessage(main.err_prefix+"You cannot claim that land!");
							return;
						}
						
						if(regions.size() != 1) {
							p.sendMessage(main.err_prefix+"Too many claims you can claim this land for!");
							if(main.settingParameter.containsKey(p.getUniqueId())) {
								if(!main.settingParameter.get(p.getUniqueId()).startsWith("set_region_area")) {
									p.sendMessage(main.err_prefix+"You are already entering a new value in the chat!");
									return;
								}
								p.sendMessage(main.prefix+"Enter the name of the claim you would like to extend in the chat :");
								return;
							} else {
								String regionString = regions.get(0).getUUID().toString();
								String regionNames = regions.get(0).getName();
								for(int i = 1;i<regions.size();i++) {
									regionString += "/"+regions.get(i).getUUID().toString();
									regionNames += " ; "+regions.get(i).getName();
								}
								main.settingParameter.put(p.getUniqueId(), "set_region_area_"+x1+":"+z1+"_"+x2+":"+z2+"_"+w+"_"+regionString);
								p.sendMessage(main.prefix+"Enter the name of the claim you would like to extend in the chat :");
								p.sendMessage(main.prefix_color+regionNames);
								p.sendMessage(main.prefix_color+"(Type \"cancel\" to cancel!)");
								return;
							}
						}
						
						Team t = main.dataManager.getTeams().stream().filter(tl -> tl.getUUID().equals(regions.get(0).getTeamUUID())).findFirst().orElse(null);
						int teamBlocks = t.getClaimBlocks();
						
						if(blockCount > teamBlocks) {
							p.sendMessage(main.err_prefix+"Your team does not have enough claim blocks!");
							p.sendMessage(main.err_color+"Try depositing some in the team claim block pool.");
							return;
						}
						
						t.setClaimBlocks(teamBlocks-blockCount);
						
						for(int o = min_x; o <= max_x; o++) {
						    for(int y = min_z; y <= max_z; y++) {
								UUID uuid = UUID.randomUUID();
								main.dataManager.getClaimedLands().add(new ClaimedLand(uuid, regions.get(0).getUUID(), w, o, y));
								regions.get(0).getLands().add(uuid);
						    }
						}
						
						main.settingClaimArea.remove(p.getUniqueId());
						p.playSound(p.getLocation(), Sound.ENTITY_VILLAGER_TRADE, 1, 2);
						p.closeInventory();
						p.sendMessage(main.prefix+"§aYou claimed a new land for "+t.getName()+"!");
						return;
					} else {
						p.sendMessage(main.err_prefix+"The two positions must be in the same world!");
						return;
					}
				} 
				p.sendMessage(main.prefix+"Successfully set pos1!");
				main.settingClaimArea.put(p.getUniqueId(), new ArrayList<>(Arrays.asList(String.valueOf(x1)+":"+String.valueOf(z1)+":"+w)));
				return;
			} else if(e.getAction() == Action.RIGHT_CLICK_AIR || e.getAction() == Action.RIGHT_CLICK_BLOCK) {
				if(main.inClaimCheck.containsKey(p.getUniqueId())) {
					main.inClaimCheck.remove(p.getUniqueId());
					p.sendMessage(main.prefix+"§cYou turned off claim checking!");
					return;
				} else {
					main.inClaimCheck.put(p.getUniqueId(), 120);
					p.sendMessage(main.prefix+"§aYou turned on claim checking!");
					return;
				}
			}
			return;
		}
		
		if(e.getClickedBlock() != null && 
			    !(e.getAction() == Action.RIGHT_CLICK_BLOCK && 
			      e.getItem() != null && e.getItem().getType().isBlock() && 
			      (!e.getClickedBlock().getType().isInteractable() || e.getClickedBlock().getType().isInteractable() && p.isSneaking())) && 
			    !(e.getAction() == Action.LEFT_CLICK_BLOCK)) {
			Location l = e.getClickedBlock().getLocation();
			int x = l.getBlockX();
			int z = l.getBlockZ();
			World w = l.getWorld();
			ClaimedLand c = main.dataManager.getClaimedLands().stream().filter(cl -> cl.getX() == x && cl.getZ() == z && Bukkit.getWorld(cl.getWorldName()) == w).findFirst().orElse(null);
			if(c != null) {
				Region r = main.dataManager.getRegions().stream().filter(cl -> cl.getUUID().equals(c.getRegionUUID())).findFirst().orElse(null);
				Team t = main.dataManager.getTeams().stream().filter(tl -> tl.getUUID().equals(r.getTeamUUID())).findFirst().orElse(null);
				if(!t.getLeader().equals(p.getUniqueId()) && (!t.getMembers().contains(p.getUniqueId()) && !r.globalHas(Permission.CLAIM_INTERACT) || 
					t.getMembers().contains(p.getUniqueId()) && !r.membersHave(Permission.CLAIM_INTERACT) && !r.playerHas(p.getUniqueId(), Permission.CLAIM_INTERACT))) {
					
					long currentTime = System.currentTimeMillis();
	                if(lastErrorMessage.containsKey(p)) {
	                    long lastMessageTime = lastErrorMessage.get(p);
	                    if(currentTime - lastMessageTime < COOLDOWN_TIME) {
	                    	e.setCancelled(true);
	    					return;
	                    }
	                }
	                
	                lastErrorMessage.put(p, currentTime);
					p.sendMessage(main.err_prefix+"This is §6"+t.getName()+main.err_color+"'s land!");
					e.setCancelled(true);
					return;
				}
			}
		}
	}
	
	@EventHandler
	public void onPlayerEntityInteract(PlayerInteractEntityEvent e) {
		Player p = e.getPlayer();
		if(e.getRightClicked() != null && !(e.getRightClicked() instanceof Player)) {
			Location l = e.getRightClicked().getLocation();
			int x = l.getBlockX();
			int z = l.getBlockZ();
			World w = l.getWorld();
			ClaimedLand c = main.dataManager.getClaimedLands().stream().filter(cl -> cl.getX() == x && cl.getZ() == z && Bukkit.getWorld(cl.getWorldName()) == w).findFirst().orElse(null);
			if(c != null) {
				Region r = main.dataManager.getRegions().stream().filter(cl -> cl.getUUID().equals(c.getRegionUUID())).findFirst().orElse(null);
				Team t = main.dataManager.getTeams().stream().filter(tl -> tl.getUUID().equals(r.getTeamUUID())).findFirst().orElse(null);
				if(!t.getLeader().equals(p.getUniqueId()) && (!t.getMembers().contains(p.getUniqueId()) && !r.globalHas(Permission.CLAIM_INTERACT) || 
					t.getMembers().contains(p.getUniqueId()) && !r.membersHave(Permission.CLAIM_INTERACT) && !r.playerHas(p.getUniqueId(), Permission.CLAIM_INTERACT))) {
					
					long currentTime = System.currentTimeMillis();
	                if(lastErrorMessage.containsKey(p)) {
	                    long lastMessageTime = lastErrorMessage.get(p);
	                    if(currentTime - lastMessageTime < COOLDOWN_TIME) {
	                    	e.setCancelled(true);
	    					return;
	                    }
	                }
	                
	                lastErrorMessage.put(p, currentTime);
					p.sendMessage(main.err_prefix+"This is §6"+t.getName()+main.err_color+"'s land!");
					e.setCancelled(true);
					return;
				}
			}
		}
	}
	
	@EventHandler
	public void onBreak(BlockBreakEvent e){
		Player p = e.getPlayer();
		Location l = e.getBlock().getLocation();
		int x = l.getBlockX();
		int z = l.getBlockZ();
		World w = l.getWorld();
		ClaimedLand c = main.dataManager.getClaimedLands().stream().filter(cl -> cl.getX() == x && cl.getZ() == z && Bukkit.getWorld(cl.getWorldName()) == w).findFirst().orElse(null);
		if(c != null) {
			Region r = main.dataManager.getRegions().stream().filter(cl -> cl.getUUID().equals(c.getRegionUUID())).findFirst().orElse(null);
			Team t = main.dataManager.getTeams().stream().filter(tl -> tl.getUUID().equals(r.getTeamUUID())).findFirst().orElse(null);
			if(!t.getLeader().equals(p.getUniqueId()) && (!t.getMembers().contains(p.getUniqueId()) && !r.globalHas(Permission.CLAIM_PLACE_BREAK_BLOCK) || 
				t.getMembers().contains(p.getUniqueId()) && !r.membersHave(Permission.CLAIM_PLACE_BREAK_BLOCK) && !r.playerHas(p.getUniqueId(), Permission.CLAIM_PLACE_BREAK_BLOCK))) {
				
				long currentTime = System.currentTimeMillis();
                if(lastErrorMessage.containsKey(p)) {
                    long lastMessageTime = lastErrorMessage.get(p);
                    if(currentTime - lastMessageTime < COOLDOWN_TIME) {
                    	e.setCancelled(true);
    					return;
                    }
                }
                
                lastErrorMessage.put(p, currentTime);
				p.sendMessage(main.err_prefix+"This is §6"+t.getName()+main.err_color+"'s land!");
				e.setCancelled(true);
				return;
			}
		}
	}
	
	@EventHandler
	public void onPlace(BlockPlaceEvent e){
		Player p = e.getPlayer();
		Location l = e.getBlock().getLocation();
		int x = l.getBlockX();
		int z = l.getBlockZ();
		World w = l.getWorld();
		ClaimedLand c = main.dataManager.getClaimedLands().stream().filter(cl -> cl.getX() == x && cl.getZ() == z && Bukkit.getWorld(cl.getWorldName()) == w).findFirst().orElse(null);
		if(c != null) {
			Region r = main.dataManager.getRegions().stream().filter(cl -> cl.getUUID().equals(c.getRegionUUID())).findFirst().orElse(null);
			Team t = main.dataManager.getTeams().stream().filter(tl -> tl.getUUID().equals(r.getTeamUUID())).findFirst().orElse(null);
			if(!t.getLeader().equals(p.getUniqueId()) && (!t.getMembers().contains(p.getUniqueId()) && !r.globalHas(Permission.CLAIM_PLACE_BREAK_BLOCK) || 
					t.getMembers().contains(p.getUniqueId()) && !r.membersHave(Permission.CLAIM_PLACE_BREAK_BLOCK) && !r.playerHas(p.getUniqueId(), Permission.CLAIM_PLACE_BREAK_BLOCK))) {
				long currentTime = System.currentTimeMillis();
                if(lastErrorMessage.containsKey(p)) {
                    long lastMessageTime = lastErrorMessage.get(p);
                    if(currentTime - lastMessageTime < COOLDOWN_TIME) {
                    	e.setCancelled(true);
    					return;
                    }
                }
                
                lastErrorMessage.put(p, currentTime);
				p.sendMessage(main.err_prefix+"This is §6"+t.getName()+main.err_color+"'s land!");
				e.setCancelled(true);
				return;
			}
		}
	}
	
	public List<Region> getTeamAround(int x, int z, World w) {
		List<Region> cn = new ArrayList<>();
		ClaimedLand c = main.dataManager.getClaimedLands().stream().filter(cl -> cl.getX() == x+1 && cl.getZ() == z && Bukkit.getWorld(cl.getWorldName()) == w).findFirst().orElse(null);
		if(c != null) {
			Region r = main.dataManager.getRegions().stream().filter(cl -> cl.getUUID().equals(c.getRegionUUID())).findFirst().orElse(null);
			cn.add(r);
		}
		ClaimedLand c1 = main.dataManager.getClaimedLands().stream().filter(cl -> cl.getX() == x && cl.getZ() == z+1 && Bukkit.getWorld(cl.getWorldName()) == w).findFirst().orElse(null);
		if(c1 != null) {
			Region r = main.dataManager.getRegions().stream().filter(cl -> cl.getUUID().equals(c1.getRegionUUID())).findFirst().orElse(null);
			cn.add(r);
		}
		ClaimedLand c2 = main.dataManager.getClaimedLands().stream().filter(cl -> cl.getX() == x && cl.getZ() == z-1 && Bukkit.getWorld(cl.getWorldName()) == w).findFirst().orElse(null);
		if(c2 != null) {
			Region r = main.dataManager.getRegions().stream().filter(cl -> cl.getUUID().equals(c2.getRegionUUID())).findFirst().orElse(null);
			cn.add(r);
		}
		ClaimedLand c3 = main.dataManager.getClaimedLands().stream().filter(cl -> cl.getX() == x-1 && cl.getZ() == z && Bukkit.getWorld(cl.getWorldName()) == w).findFirst().orElse(null);
		if(c3 != null) {
			Region r = main.dataManager.getRegions().stream().filter(cl -> cl.getUUID().equals(c3.getRegionUUID())).findFirst().orElse(null);
			cn.add(r);
		}
		
		return cn;
	}
	
	@EventHandler
	public void onExplode(EntityExplodeEvent e) {
		for(Block g : e.blockList()) {
    		tnts.put(g, "");
    	}
		for(Block h : tnts.keySet()) {
			int x = h.getLocation().getBlockX();
			int z = h.getLocation().getBlockZ();
			World w = h.getLocation().getWorld();
			ClaimedLand c = main.dataManager.getClaimedLands().stream().filter(cl -> cl.getX() == x && cl.getZ() == z && Bukkit.getWorld(cl.getWorldName()) == w).findFirst().orElse(null);
			if(c != null) {
				Region r = main.dataManager.getRegions().stream().filter(cl -> cl.getUUID().equals(c.getRegionUUID())).findFirst().orElse(null);
				if(!r.getProperties().get(Property.EXPLOSIONS) && h.getType() != Material.TNT) {
					e.blockList().remove(h);
				}
			}
		}
		tnts.clear();
		return;
	}
	
	@EventHandler
	public void onEntitySpawn(EntitySpawnEvent e) {
		if(e.getEntity() instanceof Player || (!(e.getEntity() instanceof Animals) && !(e.getEntity() instanceof Monster))) return;
		Location l = e.getLocation();
		int x = l.getBlockX();
		int z = l.getBlockZ();
		World w = l.getWorld();
		ClaimedLand c = main.dataManager.getClaimedLands().stream().filter(cl -> cl.getX() == x && cl.getZ() == z && Bukkit.getWorld(cl.getWorldName()) == w).findFirst().orElse(null);
		if(c != null) {
			Region r = main.dataManager.getRegions().stream().filter(cl -> cl.getUUID().equals(c.getRegionUUID())).findFirst().orElse(null);
			if(!r.getProperties().get(Property.MOB_SPAWNING)) {
				e.setCancelled(true);
			}
		}
	}
	
	@EventHandler
    public void onBlockSpread(BlockSpreadEvent e) {
        if(e.getSource().getType() == Material.FIRE) {
        	Location l = e.getBlock().getLocation();
    		int x = l.getBlockX();
    		int z = l.getBlockZ();
    		World w = l.getWorld();
    		ClaimedLand c = main.dataManager.getClaimedLands().stream().filter(cl -> cl.getX() == x && cl.getZ() == z && Bukkit.getWorld(cl.getWorldName()) == w).findFirst().orElse(null);
    		if(c != null) {
    			Region r = main.dataManager.getRegions().stream().filter(cl -> cl.getUUID().equals(c.getRegionUUID())).findFirst().orElse(null);
    			if(!r.getProperties().get(Property.FIRE_SPREADING)) {
    				e.setCancelled(true);
    			}
    		}
        }
    }
	
	@EventHandler
    public void onBlockBurn(BlockBurnEvent e) {
		Location l = e.getBlock().getLocation();
		int x = l.getBlockX();
		int z = l.getBlockZ();
		World w = l.getWorld();
		ClaimedLand c = main.dataManager.getClaimedLands().stream().filter(cl -> cl.getX() == x && cl.getZ() == z && Bukkit.getWorld(cl.getWorldName()) == w).findFirst().orElse(null);
		if(c != null) {
			Region r = main.dataManager.getRegions().stream().filter(cl -> cl.getUUID().equals(c.getRegionUUID())).findFirst().orElse(null);
			if(!r.getProperties().get(Property.FIRE_SPREADING)) {
				e.setCancelled(true);
			}
		}
	}
	
	@EventHandler
    public void onBlockIgnite(BlockIgniteEvent e) {
		if(e.getCause() == IgniteCause.SPREAD) {
			Location l = e.getBlock().getLocation();
			int x = l.getBlockX();
			int z = l.getBlockZ();
			World w = l.getWorld();
			ClaimedLand c = main.dataManager.getClaimedLands().stream().filter(cl -> cl.getX() == x && cl.getZ() == z && Bukkit.getWorld(cl.getWorldName()) == w).findFirst().orElse(null);
			if(c != null) {
				Region r = main.dataManager.getRegions().stream().filter(cl -> cl.getUUID().equals(c.getRegionUUID())).findFirst().orElse(null);
				if(!r.getProperties().get(Property.FIRE_SPREADING)) {
					e.setCancelled(true);
				}
			}
		}
	}
	
	@EventHandler
	public void onDamage(EntityDamageEvent e) {
		if(!(e.getEntity() instanceof Player)) return;
		if(e.getCause() == DamageCause.FALL) {
			Player p = (Player) e.getEntity();
			Location l = p.getLocation();
			int x = l.getBlockX();
			int z = l.getBlockZ();
			World w = l.getWorld();
			ClaimedLand c = main.dataManager.getClaimedLands().stream().filter(cl -> cl.getX() == x && cl.getZ() == z && Bukkit.getWorld(cl.getWorldName()) == w).findFirst().orElse(null);
			if(c != null) {
				Region r = main.dataManager.getRegions().stream().filter(cl -> cl.getUUID().equals(c.getRegionUUID())).findFirst().orElse(null);
				if(!r.getProperties().get(Property.FALL_DAMAGE)) {
					e.setCancelled(true);
				}
			}
		}
	}
	
	@EventHandler
	public void onAttack(EntityDamageByEntityEvent e) {
		if(!(e.getDamager() instanceof Player)) return;
		Player j = (Player) e.getDamager();
		Entity en = e.getEntity();
		if(en instanceof Animals) {
			Location l = en.getLocation();
			int x = l.getBlockX();
			int z = l.getBlockZ();
			World w = l.getWorld();
			ClaimedLand c = main.dataManager.getClaimedLands().stream().filter(cl -> cl.getX() == x && cl.getZ() == z && Bukkit.getWorld(cl.getWorldName()) == w).findFirst().orElse(null);
			if(c != null) {
				Region r = main.dataManager.getRegions().stream().filter(cl -> cl.getUUID().equals(c.getRegionUUID())).findFirst().orElse(null);
				if(!r.getProperties().get(Property.HURT_ANIMAL)) {
					e.setCancelled(true);
				}
			}
		} else if(en instanceof Monster) {
			Location l = en.getLocation();
			int x = l.getBlockX();
			int z = l.getBlockZ();
			World w = l.getWorld();
			ClaimedLand c = main.dataManager.getClaimedLands().stream().filter(cl -> cl.getX() == x && cl.getZ() == z && Bukkit.getWorld(cl.getWorldName()) == w).findFirst().orElse(null);
			if(c != null) {
				Region r = main.dataManager.getRegions().stream().filter(cl -> cl.getUUID().equals(c.getRegionUUID())).findFirst().orElse(null);
				if(!r.getProperties().get(Property.HURT_MONSTER)) {
					e.setCancelled(true);
				}
			}
		}
		if(!(e.getEntity() instanceof Player)) return;
		Player i = (Player) e.getEntity();
		
		Location l = i.getLocation();
		int x = l.getBlockX();
		int z = l.getBlockZ();
		World w = l.getWorld();
		ClaimedLand c = main.dataManager.getClaimedLands().stream().filter(cl -> cl.getX() == x && cl.getZ() == z && Bukkit.getWorld(cl.getWorldName()) == w).findFirst().orElse(null);
		Region r = null;
		if(c != null) {
			r = main.dataManager.getRegions().stream().filter(cl -> cl.getUUID().equals(c.getRegionUUID())).findFirst().orElse(null);
			if(!r.getProperties().get(Property.GLOBAL_PVP)) {
				e.setCancelled(true);
			}
		}
		
		PlayerData jData = main.dataManager.getPlayers().stream().filter(tl -> tl.getUUID().equals(j.getUniqueId())).findFirst().orElse(null);
		PlayerData iData = main.dataManager.getPlayers().stream().filter(tl -> tl.getUUID().equals(i.getUniqueId())).findFirst().orElse(null);
		
		if(iData == null || jData == null) {
			return;
		}
		
		List<Team> teams = new ArrayList<>();
		for(UUID uuid : jData.getTeamUUIDs()) {
			if(iData.getTeamUUIDs().contains(uuid)) {
				Team t = main.dataManager.getTeams().stream().filter(tl -> tl.getUUID().equals(uuid)).findFirst().orElse(null);
				if(t != null) {
					teams.add(t);
				}
			}
		}
		
		boolean isFriendlyFire = true;
		for(Team t : teams) {
			if(!t.isFriendlyFire()) {
				isFriendlyFire = false;
			}
		}
		
		Team team = null;
		if(r != null && !r.getProperties().get(Property.TEAM_PVP)) {
			Region rt = r;
			team = main.dataManager.getTeams().stream().filter(tl -> tl.getUUID().equals(rt.getTeamUUID())).findFirst().orElse(null);
		}
		
		if(!isFriendlyFire || (team != null && (team.getMembers().contains(j.getUniqueId()) || team.getLeader().equals(j.getUniqueId())) && (team.getMembers().contains(i.getUniqueId()) || team.getLeader().equals(i.getUniqueId())))) {
			e.setCancelled(true);
			return;
		}
	}
	
	public static boolean isAnimal(Entity entity) {
        return entity instanceof Animals;
    }

    public static boolean isMonster(Entity entity) {
        return entity instanceof Monster;
    }
	
	@EventHandler
	public void onMove(PlayerMoveEvent e){
		Player p = e.getPlayer();
		Location l = p.getLocation();
		int x = l.getBlockX();
		int z = l.getBlockZ();
		World w = l.getWorld();
		setPosition(x+":"+z, w, p);
		showBoundaryParticles(p);
		checkRegionAccess(p, e.getFrom(), e.getTo(), w);
		return;
	}
	
	public void checkRegionAccess(Player p, Location fromLoc, Location toLoc, World w) {
		if(fromLoc.distance(toLoc) == 0) {
            return;
        }
		ClaimedLand c = main.dataManager.getClaimedLands().stream().filter(cl -> cl.getX() == fromLoc.getBlockX() && cl.getZ() == fromLoc.getBlockZ() && Bukkit.getWorld(cl.getWorldName()) == w).findFirst().orElse(null);
		Region r = null;
		if(c != null) {
			r = main.dataManager.getRegions().stream().filter(cl -> cl.getUUID().equals(c.getRegionUUID())).findFirst().orElse(null);
		}
		ClaimedLand c1 = main.dataManager.getClaimedLands().stream().filter(cl -> cl.getX() == toLoc.getBlockX() && cl.getZ() == toLoc.getBlockZ() && Bukkit.getWorld(cl.getWorldName()) == w).findFirst().orElse(null);
		Region r1 = null;
		if(c1 != null) {
			r1 = main.dataManager.getRegions().stream().filter(cl -> cl.getUUID().equals(c1.getRegionUUID())).findFirst().orElse(null);
		}
		if(r1 != null && r1 != r) {
			final Region r1_ = r1;
			Team t = main.dataManager.getTeams().stream().filter(tl -> tl.getUUID().equals(r1_.getTeamUUID())).findFirst().orElse(null);
			if(!t.getLeader().equals(p.getUniqueId()) && ((!t.getMembers().contains(p.getUniqueId()) && !r1.getProperties().get(Property.PUBLIC_ACCESS)) || 
					t.getMembers().contains(p.getUniqueId()) && !r1.membersHave(Permission.CLAIM_ACCESS) && !r1.playerHas(p.getUniqueId(), Permission.CLAIM_ACCESS))) {
				Vector movement = toLoc.toVector().subtract(fromLoc.toVector());
		        Vector oppositeMovement = movement.multiply(-2);
		        Location oppositeLocation = fromLoc.clone().add(oppositeMovement);
		        oppositeLocation.setY(fromLoc.getY());
		        p.teleport(oppositeLocation);
			}
		}
	}

	public void setPosition(String loc, World w, Player p){
		if(isIn.containsKey(p)){ 
			if(isIn.get(p) == loc) return;
			String oldLoc = isIn.get(p); 
			if(isIn.containsKey(p)){
				isIn.remove(p);
			}
			isIn.put(p, loc); 
			UUID t1 = null;
			UUID t2 = null;
			int oldloc_x = Integer.valueOf(oldLoc.split(":")[0]);
			int oldloc_z = Integer.valueOf(oldLoc.split(":")[1]);
			ClaimedLand c = main.dataManager.getClaimedLands().stream().filter(cl -> cl.getX() == oldloc_x && cl.getZ() == oldloc_z && Bukkit.getWorld(cl.getWorldName()) == w).findFirst().orElse(null);
			Region r = null;
			if(c != null) {
				r = main.dataManager.getRegions().stream().filter(cl -> cl.getUUID().equals(c.getRegionUUID())).findFirst().orElse(null);
				t1 = r.getTeamUUID();
			}
			int loc_x = Integer.valueOf(loc.split(":")[0]);
			int loc_z = Integer.valueOf(loc.split(":")[1]);
			ClaimedLand c1 = main.dataManager.getClaimedLands().stream().filter(cl -> cl.getX() == loc_x && cl.getZ() == loc_z && Bukkit.getWorld(cl.getWorldName()) == w).findFirst().orElse(null);
			Region r1 = null;
			if(c1 != null) {
				r1 = main.dataManager.getRegions().stream().filter(cl -> cl.getUUID().equals(c1.getRegionUUID())).findFirst().orElse(null);
				t2 = r1.getTeamUUID();
			}
			if(t1 != null && r.equals(r1)) return;
			if(c1 == null){ 
				notify(r, t1,"exit", p);
			}else{
				notify(r1, t2,"enter", p);
			}
			return;
		}else{
			isIn.put(p, loc);
			return;
		}
	}

	public void notify(Region r, UUID team, String type, Player p){
		Team t = main.dataManager.getTeams().stream().filter(tl -> tl.getUUID().equals(team)).findFirst().orElse(null);
		if(t != null) {	
			if(type.equalsIgnoreCase("enter")){
				if(r.getTitles().size() == 4) {
					String wlcm_title = r.getTitles().get(0).replaceAll("&", "§").replaceAll("%claim%", r.getName()).replaceAll("%team%", t.getName());
					String wlcm_sub = r.getTitles().get(1).replaceAll("&", "§").replaceAll("%claim%", r.getName()).replaceAll("%team%", t.getName());
					p.sendTitle(wlcm_title, wlcm_sub, 20, 80, 30);
				} else {
					String wlcm_title = main.welcome_default_title.replaceAll("&", "§").replaceAll("%claim%", r.getName()).replaceAll("%team%", t.getName());
					String wlcm_sub = main.welcome_default_subtitle.replaceAll("&", "§").replaceAll("%claim%", r.getName()).replaceAll("%team%", t.getName());
					p.sendTitle(wlcm_title, wlcm_sub, 20, 80, 30);
				}
			}else{
				if(r.getTitles().size() == 4) {
					String exit_title = r.getTitles().get(2).replaceAll("&", "§").replaceAll("%claim%", r.getName()).replaceAll("%team%", t.getName());
					String exit_sub = r.getTitles().get(3).replaceAll("&", "§").replaceAll("%claim%", r.getName()).replaceAll("%team%", t.getName());
					p.sendTitle(exit_title, exit_sub, 10, 60, 20);
				} else {
					String exit_title = main.exit_default_title.replaceAll("&", "§").replaceAll("%claim%", r.getName()).replaceAll("%team%", t.getName());
					String exit_sub = main.exit_default_subtitle.replaceAll("&", "§").replaceAll("%claim%", r.getName()).replaceAll("%team%", t.getName());
					p.sendTitle(exit_title, exit_sub, 10, 60, 20);
				}
			}
			
		}
	}
	
	private static final double RADIUS = 10.0;

	public void showBoundaryParticles(Player p) {
	    UUID playerUUID = p.getUniqueId();
	    long currentTime = System.currentTimeMillis();

	    if(main.lastParticleShowTime.containsKey(playerUUID) && 
	        currentTime - main.lastParticleShowTime.get(playerUUID) < 1000) {
	        return;
	    }

	    main.lastParticleShowTime.put(playerUUID, currentTime);

	    if(main.inClaimCheck.containsKey(playerUUID)) {
	        Location playerLocation = p.getLocation();
	        int playerY = playerLocation.getBlockY();

	        for(Region region : main.dataManager.getRegions()) {
	            Team t = main.dataManager.getTeams().stream()
	                    .filter(tl -> tl.getUUID().equals(region.getTeamUUID()))
	                    .findFirst().orElse(null);
	            if(!(t.getMembers().contains(p.getUniqueId()) && t.playerHas(p.getUniqueId(), Permission.TEAM_MANAGE_CLAIMS)) && !t.getLeader().equals(p.getUniqueId())) continue;

	            for(UUID landUUID : region.getLands()) {
	                ClaimedLand c = main.dataManager.getClaimedLands().stream()
	                        .filter(cl -> cl.getUUID().equals(landUUID))
	                        .findFirst().orElse(null);
	                if(c == null || !c.getWorldName().equals(playerLocation.getWorld().getName())) continue;

	                Location claimedLocation = new Location(p.getWorld(), c.getX(), playerY, c.getZ());
	                if(playerLocation.distance(claimedLocation) <= RADIUS) {
	                    if(isAtBoundary(c, region)) {
	                    	World w = Bukkit.getWorld(c.getWorldName());
	                    	List<double[]> locations = getWildernessEdgeLocations(w, c.getX(), c.getZ(), region);
	                    	for(double[] l : locations) {
	                    		showFireworkWall(p, w, l[0], ((double) playerY-2), l[1], l[2],((double) playerY+3), l[3]);
	                    		showStaticFireworkParticleWall(p, w, l[0], ((double) playerY-2), l[1], l[2],((double) playerY+3), l[3], 5);
	                    	}
	                    }
	                }
	            }
	        }
	    }
	}

	private void showStaticFireworkParticleWall(Player p, World world, double x1, double y1, double z1, double x2, double y2, double z2, long durationInSeconds) {

	    new BukkitRunnable() {
	        long endTime = System.currentTimeMillis() + (durationInSeconds * 1000);

	        @Override
	        public void run() {
	            if(System.currentTimeMillis() >= endTime || !main.inClaimCheck.containsKey(p.getUniqueId())) {
	                this.cancel();
	                return;
	            }
	            if(p != null && p.isOnline()) {
	            	showFireworkWall(p, world, x1, y1, z1, x2, y2, z2);
	            }
	        }
	    }.runTaskTimer(main, 0, 20);
	}
	
	public void showFireworkWall(Player p, World world, double x1, double y1, double z1, double x2, double y2, double z2) {
        double xMin = Math.min(x1, x2);
        double xMax = Math.max(x1, x2);
        double yMin = Math.min(y1, y2);
        double yMax = Math.max(y1, y2);
        double zMin = Math.min(z1, z2);
        double zMax = Math.max(z1, z2);

        double spacing = 0.5;

        if (x1 == x2) {
            for (double z = zMin; z <= zMax; z += spacing) {
                for (double y = yMin; y <= yMax; y += spacing) {
                    if (((int) ((z - zMin) / spacing) + (int) ((y - yMin) / spacing)) % 2 == 0) {
                        Location loc = new Location(world, x1, y, z);
                        p.spawnParticle(Particle.FIREWORK, loc, 0);
                    }
                }
            }
        } else if (z1 == z2) {
            for (double x = xMin; x <= xMax; x += spacing) {
                for (double y = yMin; y <= yMax; y += spacing) {
                    if (((int) ((x - xMin) / spacing) + (int) ((y - yMin) / spacing)) % 2 == 0) {
                        Location loc = new Location(world, x, y, z1);
                        p.spawnParticle(Particle.FIREWORK, loc, 0);
                    }
                }
            }
        }
    }


    private boolean isAtBoundary(ClaimedLand land, Region region) {
        return !isLandApartofRegion(land.getX() + 1, land.getZ(), land.getWorldName(), region) ||
               !isLandApartofRegion(land.getX() - 1, land.getZ(), land.getWorldName(), region) ||
               !isLandApartofRegion(land.getX(), land.getZ() + 1, land.getWorldName(), region) ||
               !isLandApartofRegion(land.getX(), land.getZ() - 1, land.getWorldName(), region);
    }
    
    private List<Region> getRegionsAroundCoords(int x, int z, String w) {
        Region r1 = getRegionfromCoords(x+1, z, w);
        Region r2 = getRegionfromCoords(x-1, z, w);
        Region r3 = getRegionfromCoords(x, z+1, w);
        Region r4 = getRegionfromCoords(x, z-1, w);
    	
        List<Region> regions = new ArrayList<>();        
        if(r1 != null) regions.add(r1);
        if(r2 != null) regions.add(r2);
        if(r3 != null) regions.add(r3);
        if(r4 != null) regions.add(r4);
        
    	return regions;
    }

    private boolean isLandApartofRegion(int x, int z, String worldName, Region region) {
    	ClaimedLand c = main.dataManager.getClaimedLands().stream().filter(cl -> cl.getX() == x && cl.getZ() == z && cl.getWorldName().equals(worldName)).findFirst().orElse(null);
    	if(c == null || !c.getRegionUUID().equals(region.getUUID())) {
    		return false;
    	}
    	return true;
    }
    
    private Region getRegionfromCoords(int x, int z, String worldName) {
    	ClaimedLand c = main.dataManager.getClaimedLands().stream().filter(cl -> cl.getX() == x && cl.getZ() == z && cl.getWorldName().equals(worldName)).findFirst().orElse(null);
    	if(c != null) {
    		Region r = main.dataManager.getRegions().stream().filter(cl -> cl.getUUID().equals(c.getRegionUUID())).findFirst().orElse(null);
    		if(r != null) {
    			return r;
    		}
    	}
    	return null;
    }
    
    public List<double[]> getWildernessEdgeLocations(World w, int x, int z, Region r) {
        if (w == null) return new ArrayList<>();

        List<double[]> edgeLocations = new ArrayList<>();

        checkAdjacentDirt(r, w, edgeLocations, x, z + 1, x, z);
        checkAdjacentDirt(r, w, edgeLocations, x, z - 1, x, z);
        checkAdjacentDirt(r, w, edgeLocations, x + 1, z, x, z);
        checkAdjacentDirt(r, w, edgeLocations, x - 1, z, x, z);

        return edgeLocations;
    }

    private void checkAdjacentDirt(Region r, World world, List<double[]> edgeLocations, int dirtX, int dirtZ, int edgeX, int edgeZ) {
        ClaimedLand c = main.dataManager.getClaimedLands().stream().filter(cl -> cl.getX() == dirtX && cl.getZ() == dirtZ && Bukkit.getWorld(cl.getWorldName()) == world).findFirst().orElse(null);
        if(c == null || !c.getRegionUUID().equals(r.getUUID())) {
            if(dirtX == edgeX) { // Same X (north/south face)
                if (dirtZ > edgeZ) { // North dirt block
                	edgeLocations.add(new double[]{edgeX+1, edgeZ+1, edgeX, edgeZ+1}); // Back edge (south)
                } else { // South dirt block
                	edgeLocations.add(new double[]{edgeX, edgeZ, edgeX+1, edgeZ}); // Forward edge (north)
                }
            } else if(dirtZ == edgeZ) { // Same Z (east/west face)
                if(dirtX > edgeX) { // East dirt block
                    edgeLocations.add(new double[]{edgeX+1, edgeZ, edgeX+1, edgeZ+1}); // Forward edge (east)
                } else { // West dirt block
                    edgeLocations.add(new double[]{edgeX, edgeZ+1, edgeX, edgeZ}); // Back edge (west)
                }
            }
            
        }
    }
    
    public void sendTeamInfo(Player p, Team t) {
    	p.closeInventory();
		p.sendMessage(main.prefix+"Information for the team §8"+t.getName()+"§7!");
		int memberCount = t.getMembers().size()+1;
		String memberMessage = (memberCount == 1) ? 
		    main.prefix_color+"There is §f" + memberCount + " §7member in your team!" : 
		    main.prefix_color+"There are §f" + memberCount + " §7members in your team!";
		p.sendMessage(memberMessage);
		
		p.sendMessage("§cLeader: §e"+Bukkit.getOfflinePlayer(t.getLeader()).getName());
		List<String> members = new ArrayList<String>();
		for(UUID u : t.getMembers()) {
			members.add(Bukkit.getOfflinePlayer(u).getName());
		}
		String memberslist = members.stream().collect(Collectors.joining(", "));
		p.sendMessage("§6Members: §e"+memberslist);
		
		p.sendMessage(main.prefix_color+"Execute: /team help");
		return;
    }
    
    public ItemStack toggleTeamPermission(Player p, Team t, String s, Permission perm, ItemStack item) {
    	ItemStack signItem = item.clone();
		
		List<String> lore = new ArrayList<>();
		lore.add(" ");
    	if(t.getPerms().containsKey(s) && t.getPerms().get(s).contains(perm)) {
			t.getPerms().get(s).remove(perm);
			lore.add("§7› Enabled");
			lore.add("§c› Disabled");
			
		} else {
			if(!t.getPerms().containsKey(s)) {
				List<Permission> permlist = new ArrayList<>();
				t.getPerms().put(s, permlist);
			}
			t.getPerms().get(s).add(perm);
			lore.add("§a› Enabled");
			lore.add("§7› Disabled");
		}
    	lore.add("");
		lore.add("§7Click to toggle");
		
		ItemMeta im = signItem.getItemMeta();
		im.setLore(lore);
		signItem.setItemMeta(im);
		
		return signItem;
    }
    
    public ItemStack toggleRegionPermission(Player p, Region r, String s, Permission perm, ItemStack item) {
    	ItemStack signItem = item.clone();
    	
		List<String> lore = new ArrayList<>();
		lore.add(" ");
		if(perm == Permission.CLAIM_ACCESS && s.equals("ALL")) {
			if(r.getProperties().get(Property.PUBLIC_ACCESS)) {
				r.setProperty(Property.PUBLIC_ACCESS, false);
				lore.add("§7› Enabled");
				lore.add("§c› Disabled");
				
				kickPlayersOutofRegion(r, 1000);
			} else {
				r.setProperty(Property.PUBLIC_ACCESS, true);
				lore.add("§a› Enabled");
				lore.add("§7› Disabled");
			}
		}else {
	    	if(r.getPerms().containsKey(s) && r.getPerms().get(s).contains(perm)) {
				r.getPerms().get(s).remove(perm);
				lore.add("§7› Enabled");
				lore.add("§c› Disabled");
			} else {
				if(!r.getPerms().containsKey(s)) {
					List<Permission> permlist = new ArrayList<>();
					r.getPerms().put(s, permlist);
				}
				r.getPerms().get(s).add(perm);
				lore.add("§a› Enabled");
				lore.add("§7› Disabled");
			}
		}
    	lore.add("");
		lore.add("§7Click to toggle");
		
		ItemMeta im = signItem.getItemMeta();
		im.setLore(lore);
		signItem.setItemMeta(im);
		
		return signItem;
    }
    
    public void kickPlayersOutofRegion(Region r, int maxDistance) {
    	for(Player pl : Bukkit.getOnlinePlayers()) {
			Location l = pl.getLocation();
			int x = l.getBlockX();
			int z = l.getBlockZ();
			World w = l.getWorld();
			ClaimedLand c = main.dataManager.getClaimedLands().stream().filter(cl -> cl.getX() == x && cl.getZ() == z && Bukkit.getWorld(cl.getWorldName()) == w).findFirst().orElse(null);
			Region r1 = null;
			if(c != null) {
				r1 = main.dataManager.getRegions().stream().filter(cl -> cl.getUUID().equals(c.getRegionUUID())).findFirst().orElse(null);
				if(r1 != null) {
					if(r1.getUUID().equals(r.getUUID())) {
						Team t = main.dataManager.getTeams().stream()
			                    .filter(tl -> tl.getUUID().equals(r.getTeamUUID()))
			                    .findFirst().orElse(null);
						if(!t.getLeader().equals(pl.getUniqueId()) && ((!t.getMembers().contains(pl.getUniqueId()) && !r1.getProperties().get(Property.PUBLIC_ACCESS)) || 
								t.getMembers().contains(pl.getUniqueId()) && !r1.membersHave(Permission.CLAIM_ACCESS) && !r1.playerHas(pl.getUniqueId(), Permission.CLAIM_ACCESS))) {
							Vector direction = pl.getLocation().getDirection().normalize();
							double threshold = 0.7;

							if(direction.getY() >= threshold || direction.getY() <= -threshold) {
							    Random rand = new Random();
							    double randomX = rand.nextDouble() * 2 - 1;
							    double randomZ = rand.nextDouble() * 2 - 1;
							    direction = new Vector(randomX, 0, randomZ).normalize();
							}
							
						    Location startLocation = pl.getLocation();
						    
						    for(int i = 0; i < maxDistance; i++) {
						        Vector step = direction.clone().multiply(i);
						        Location blockLocation = startLocation.clone().add(step);
						        
						        int x1 = blockLocation.getBlockX();
						        int z1 = blockLocation.getBlockZ();
						        World w1 = blockLocation.getWorld();
						        
						        ClaimedLand c1 = main.dataManager.getClaimedLands().stream().filter(cl -> cl.getX() == x1 && cl.getZ() == z1 && Bukkit.getWorld(cl.getWorldName()) == w1).findFirst().orElse(null);
								Region r12 = null;
								if(c1 != null) {
									r12 = main.dataManager.getRegions().stream().filter(cl -> cl.getUUID().equals(c1.getRegionUUID())).findFirst().orElse(null);
									if(r12 == null || r12.getTeamUUID() == null || !r12.getUUID().equals(r.getUUID())) {
										Location loc = w1.getHighestBlockAt(blockLocation).getLocation();
										loc.add(0, 1, 0);
										pl.teleport(loc);
										return;
									}
								} else {
									Location loc = w1.getHighestBlockAt(blockLocation).getLocation();
									loc.add(0.5, 1.2, 0.5);
									pl.teleport(loc);
									return;
								}
						    }
						}
					}
				}
			}	
    	}
	}
	

}
