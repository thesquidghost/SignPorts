package dev.airfrom.teamclaim.commands;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import dev.airfrom.teamclaim.Main;
import dev.airfrom.teamclaim.data.ClaimedLand;
import dev.airfrom.teamclaim.data.GUI;
import dev.airfrom.teamclaim.data.Invite;
import dev.airfrom.teamclaim.data.Permission;
import dev.airfrom.teamclaim.data.PlayerData;
import dev.airfrom.teamclaim.data.Region;
import dev.airfrom.teamclaim.data.Team;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;

public class TeamCmd implements CommandExecutor, TabCompleter{
	
	private Main main;
	private GUI GUI;
	
	public TeamCmd(Main main) {
		this.main = main;
		this.GUI = new GUI(main);
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String msg, String[] args) {
		if(!(sender instanceof Player)) return false;
		Player p = (Player) sender;
		if(cmd.getName().equalsIgnoreCase("team") || cmd.getName().equalsIgnoreCase("t")){
			if(args.length == 0) {
				PlayerData pData = main.dataManager.getPlayers().stream().filter(tl -> tl.getUUID().equals(p.getUniqueId())).findFirst().orElse(null);
				if(pData.getTeamUUIDs() == null || pData.getTeamUUIDs().isEmpty()) {
					p.sendMessage(main.err_prefix+"You must be in a team!");
					p.sendMessage(main.err_color+"In order to join a team, execute: /teamh join <team>");
					return true;
				} else {
					GUI.openTeamsGUI(p, pData, 1);
					return true;
				}
			} else if(args.length == 1) {
				PlayerData pData = main.dataManager.getPlayers().stream().filter(tl -> tl.getUUID().equals(p.getUniqueId())).findFirst().orElse(null);
				if(pData.getTeamUUIDs() == null || pData.getTeamUUIDs().isEmpty()) {
					p.sendMessage(main.err_prefix+"You must be in a team!");
					p.sendMessage(main.err_color+"In order to join a team, execute: /teamh join <team>");
					return true;
				}
				String nn = args[0];
				Team t = main.dataManager.getTeams().stream().filter(tl -> tl.getName().equalsIgnoreCase(nn)).findFirst().orElse(null);
				if(t == null) {
					p.sendMessage(main.err_prefix+"Could not find that team!");
					return true;
				}
				if(!t.getMembers().contains(p.getUniqueId()) && !t.getLeader().equals(p.getUniqueId())) {
					p.sendMessage(main.err_prefix+"You are not apart of that team!");
					return true;
				}
				GUI.openTeamGUI(p, t);
				return true;
			} else {
				p.sendMessage(main.err_prefix+"Try /"+cmd.getName().toLowerCase()+" <team>");
				return true;
			}
		} else if(cmd.getName().equalsIgnoreCase("teamh") || cmd.getName().equalsIgnoreCase("th")){
			if(args.length == 1) {
				if(args[0].equalsIgnoreCase("help")) {
					p.sendMessage("§e-------------|| "+main.help_prefix+" ||-------------\n" + 
							"§1§l-----------------------------------\n" + 
							"§elist:§7§l lists every team\n" +
							"§ecreate <name>:§7§l creates a team\n" + 
							"§edisband:§7§l disbands your team\n" + 
							"§ejoin <team>:§7§l makes you join a team\n" + 
							"§eleave <team>:§7§l makes you leave a team\n" + 
							"§ekick <player>:§7§l kicks a player out of your team\n" + 
							"§epromote <player>:§7§l promotes a member to leader\n" + 
							"§7Plugin made by MC-WISE"+
							"§1§l-----------------------------------");
					return true;
				} else if(args[0].equalsIgnoreCase("disband")) {
					PlayerData pData = main.dataManager.getPlayers().stream().filter(tl -> tl.getUUID().equals(p.getUniqueId())).findFirst().orElse(null);
					if(pData.getTeamUUIDs() == null || pData.getTeamUUIDs().isEmpty()) {
						p.sendMessage(main.err_prefix+"You must be in a team!");
						return true;
					}
					Team t = main.dataManager.getTeams().stream().filter(tl -> tl.getLeader().equals(pData.getUUID())).findFirst().orElse(null);
					if(t == null) {
						p.sendMessage(main.err_prefix+"You are not the leader of any team you are in!");
						return true;
					}
					
					for(UUID member : t.getMembers()) {
						OfflinePlayer pl = Bukkit.getOfflinePlayer(member);
						if(pl != null && pl.isOnline()) {
							((Player) pl).sendMessage(main.prefix+"§cYour team has been disbanded by the leader!");
						}
						PlayerData da = main.dataManager.getPlayers().stream().filter(tl -> tl.getUUID().equals(member)).findFirst().orElse(null);
						
						List<UUID> teamUUIDs = da.getTeamUUIDs();
						teamUUIDs.remove(t.getUUID());
						da.setTeamList(teamUUIDs);
					}
					main.dataManager.getTeams().remove(t);
					
					List<Region> tli = new ArrayList<>();
					
					for(Region cl : main.dataManager.getRegions()) {
						if(cl.getTeamUUID().equals(t.getUUID())) {
							tli.add(cl);
						}
					}
					if(!tli.isEmpty()) {
						for(Region c : tli) {
							for(UUID cl : c.getLands()) {
								ClaimedLand g = main.dataManager.getClaimedLands().stream().filter(tl -> tl.getUUID().equals(cl)).findFirst().orElse(null);
								main.dataManager.getClaimedLands().remove(g);
							}
							main.dataManager.getRegions().remove(c);
						}
					}
					
					List<UUID> teamUUIDs = pData.getTeamUUIDs();
					teamUUIDs.remove(t.getUUID());
					pData.setTeamList(teamUUIDs);
		
					p.sendMessage(main.prefix+"You disbanded your team "+t.getName()+"!");
					return true;
				} else if(args[0].equalsIgnoreCase("list")) {
					int nbTeam = main.dataManager.getTeams().size();
					int pteams = 0;
					for(PlayerData pl : main.dataManager.getPlayers()) {
						if(pl.getTeamUUIDs() != null && !pl.getTeamUUIDs().isEmpty()) {
							pteams++;
						}
					}
					if(nbTeam != 0) {
						p.sendMessage(main.prefix+"§eThere are §6"+nbTeam+" Teams§e and §6"+pteams+" players§e in teams.");
						p.sendMessage("§1§l----------------------");
						for(Team t : main.dataManager.getTeams()) {
							if(t.getMembers().size()+1 > 1) {
								int totalPlayers = t.getMembers().size()+1;
								p.sendMessage("§c"+t.getName()+":§6 "+totalPlayers+" members");
							} else {
								p.sendMessage("§c"+t.getName()+":§6 1 member");
							}
						}
						p.sendMessage("§1§l----------------------");
						return true;
					} else {
						p.sendMessage(main.err_prefix+"There is no team registered!");
						p.sendMessage(main.err_color+"Create yours by executing: /teamh create <name>");
						return true;
					}
				} else {
					p.sendMessage(main.err_prefix+"Try /"+cmd.getName().toLowerCase()+" help");
					return true;
				}
			}else if(args.length == 2) {
				String nn = args[1];
				if(args[0].equalsIgnoreCase("join")) {
					Team t = main.dataManager.getTeams().stream().filter(tl -> tl.getName().equalsIgnoreCase(nn)).findFirst().orElse(null);
					if(t == null) {
						p.sendMessage(main.err_prefix+"Could not find a team named §l"+nn+"§7!");
						return true;
					}
					
					PlayerData pData = main.dataManager.getPlayers().stream().filter(tl -> tl.getUUID().equals(p.getUniqueId())).findFirst().orElse(null);
					if(pData.getTeamUUIDs().contains(t.getUUID())) {
						p.sendMessage(main.err_prefix+"You already are apart of "+t.getName()+"!");
						return true;
					}
					
					Map<UUID, UUID> invited = new HashMap<>();
					for(Invite i : t.getInvites()) {
						invited.put(i.getInvitee(), i.getInviter());
					}
					
					if(!t.globalHas(Permission.TEAM_PUBLIC_ACCESS) && !invited.containsKey(p.getUniqueId())) {
						p.sendMessage(main.err_prefix+"That team does not have public access on! You must be invited to join it.");
						return true;
					}
					
					if(invited.containsKey(p.getUniqueId())) {
						main.removeInvite(t, invited, p);
						
						OfflinePlayer ple = Bukkit.getOfflinePlayer(t.getLeader());
						if(ple != null && ple.isOnline()) {
							((Player) ple).sendMessage(main.prefix+"§6"+p.getName()+"§a joined "+t.getName()+"!");
						}
						
						for(UUID pls : t.getMembers()) {
							OfflinePlayer pl = Bukkit.getOfflinePlayer(pls);
							if(pl != null && pl.isOnline()) {
								((Player) pl).sendMessage(main.prefix+"§6"+p.getName()+"§a joined "+t.getName()+"!");
							}
						}
						
						t.getMembers().add(p.getUniqueId());
						
						List<UUID> teamUUIDs = pData.getTeamUUIDs();
						teamUUIDs.add(t.getUUID());
						pData.setTeamList(teamUUIDs);
						
						p.sendMessage(main.prefix+"§aYou joined §l"+t.getName()+"§r§a!");
						return true;
					} else if(t.globalHas(Permission.TEAM_PUBLIC_ACCESS)) {
						OfflinePlayer ple = Bukkit.getOfflinePlayer(t.getLeader());
						if(ple != null && ple.isOnline()) {
							((Player) ple).sendMessage(main.prefix+"§6"+p.getName()+"§a joined "+t.getName()+"!");
						}
						
						for(UUID pls : t.getMembers()) {
							OfflinePlayer pl = Bukkit.getOfflinePlayer(pls);
							if(pl != null && pl.isOnline()) {
								((Player) pl).sendMessage(main.prefix+"§6"+p.getName()+"§a joined "+t.getName()+"!");
							}
						}
						
						t.getMembers().add(p.getUniqueId());
						
						List<UUID> teamUUIDs = pData.getTeamUUIDs();
						teamUUIDs.add(t.getUUID());
						pData.setTeamList(teamUUIDs);
						
						p.sendMessage(main.prefix+"§aYou joined §l"+t.getName()+"§r§a!");
						return true;
					}
				} else if(args[0].equalsIgnoreCase("create")) {
					PlayerData pData = main.dataManager.getPlayers().stream().filter(tl -> tl.getUUID().equals(p.getUniqueId())).findFirst().orElse(null);
					Team t = main.dataManager.getTeams().stream().filter(tl -> tl.getLeader().equals(pData.getUUID())).findFirst().orElse(null);
					if(t != null) {
						p.sendMessage(main.err_prefix+"You already are a leader of a team!");
						return true;
					}
					if(nn.equalsIgnoreCase("help") || nn.equalsIgnoreCase("disband") || nn.equalsIgnoreCase("list") || nn.equalsIgnoreCase("management") || nn.equalsIgnoreCase("team")) {
						p.sendMessage(main.err_prefix+"That team name is not valid!");
						return true;
					}
					
					if(nn.length() > 16) {
						p.sendMessage(main.err_prefix+"Your team name cannot be longer than 16 characters!");
						return true;
					}
					
					if(nn.length() < 3) {
						p.sendMessage(main.err_prefix+"Your team name cannot be shorter than 3 characters!");
						return true;
					}
					
					if(main.containsSpecialCharacter(nn)) {
						p.sendMessage(main.err_prefix+"Your team name cannot contain special characters!");
						return true;
					}
					
					Team teamc = main.dataManager.getTeams().stream().filter(tl -> tl.getName().equalsIgnoreCase(nn)).findFirst().orElse(null);
					if(teamc != null) {
						p.sendMessage(main.err_prefix+"That team name is already taken!");
						return true;
					}
					UUID uuid = UUID.randomUUID();
					List<UUID> members = new ArrayList<>();
					List<Invite> invites = new ArrayList<>();
					Map<String, List<Permission>> perms = new HashMap<>();
					main.dataManager.getTeams().add(new Team(uuid, nn, p.getUniqueId(), members, 0, System.currentTimeMillis(), null, perms, invites, false));
					
					List<UUID> teamUUIDs = pData.getTeamUUIDs();
					teamUUIDs.add(uuid);
					pData.setTeamList(teamUUIDs);
					
					p.sendMessage(main.prefix+"§aYou successfully created §l"+nn+"§r§a team!");
					return true;
				} else if(args[0].equalsIgnoreCase("leave")) {
					PlayerData pData = main.dataManager.getPlayers().stream().filter(tl -> tl.getUUID().equals(p.getUniqueId())).findFirst().orElse(null);
					if(pData.getTeamUUIDs() == null || pData.getTeamUUIDs().isEmpty()) {
						p.sendMessage(main.err_prefix+"There is no team to leave!");
						return true;
					}
					Team targetTeam = null;
					for(Team t : main.dataManager.getTeams()) {
						if(t.getName().equalsIgnoreCase(nn)) {
							targetTeam = t;
						}
					}
					if(targetTeam == null) {
						p.sendMessage(main.err_prefix+"Could not find a team named §l"+nn+"§r"+main.err_color+"!");
						return true;
					}
					if(!targetTeam.getMembers().contains(pData.getUUID())){
						p.sendMessage(main.err_prefix+"You are not apart of that team!");
						return true;
					}
					if(targetTeam.getLeader().equals(p.getUniqueId())) {
						p.sendMessage(main.err_prefix+"You cannot leave your team as you are the leader!");
						p.sendMessage(main.err_color+"You should either promote another member to leader or disband the team.");
						return true;
					}
					
					List<UUID> teamUUIDs = pData.getTeamUUIDs();
					teamUUIDs.remove(targetTeam.getUUID());
					pData.setTeamList(teamUUIDs);
					
					targetTeam.getMembers().remove(p.getUniqueId());
					if(targetTeam.getPerms().containsKey(p.getUniqueId().toString())) {
						targetTeam.getPerms().remove(p.getUniqueId().toString());
					}
					
					for(Region rg : main.dataManager.getRegions()) {
						if(rg.getTeamUUID().equals(targetTeam.getUUID())) {
							if(rg.getPerms().containsKey(p.getUniqueId().toString())) {
								rg.getPerms().remove(p.getUniqueId().toString());
							}
						}
					}
					
					OfflinePlayer ple = Bukkit.getOfflinePlayer(targetTeam.getLeader());
					if(ple != null && ple.isOnline()) {
						((Player) ple).sendMessage(main.prefix+"§6"+p.getName()+"§c left "+targetTeam.getName()+"!");
					}
					for(UUID pls : targetTeam.getMembers()) {
						OfflinePlayer pl = Bukkit.getOfflinePlayer(pls);
						if(pl != null && pl.isOnline()) {
							((Player) pl).sendMessage(main.prefix+"§6"+p.getName()+"§c left "+targetTeam.getName()+"!");
						}
					}
					p.sendMessage(main.prefix+"§cYou left "+targetTeam.getName()+"!");
					return true;
				} else if(args[0].equalsIgnoreCase("setname")) {
					PlayerData pData = main.dataManager.getPlayers().stream().filter(tl -> tl.getUUID().equals(p.getUniqueId())).findFirst().orElse(null);
					if(pData.getTeamUUIDs() == null || pData.getTeamUUIDs().isEmpty()) {
						p.sendMessage(main.err_prefix+"You must be in a team!");
						return true;
					}
					Team t = main.dataManager.getTeams().stream().filter(tl -> tl.getLeader().equals(pData.getUUID())).findFirst().orElse(null);
					if(t == null) {
						p.sendMessage(main.err_prefix+"You are not the leader of any team you are in!");
						return true;
					}
					if(nn.length() > 16) {
						p.sendMessage(main.err_prefix+"Your new team name cannot be longer than 16 characters!");
						return true;
					}
					if(nn.length() < 3) {
						p.sendMessage(main.err_prefix+"Your new team name cannot be shorter than 3 characters!");
						return true;
					}
					if(main.containsSpecialCharacter(nn)) {
						p.sendMessage(main.err_prefix+"Your new team name cannot contain special characters!");
						return true;
					}
					if(nn.equals(t.getName())) {
						p.sendMessage(main.err_prefix+"Your new team name must be different from the current one!");
						return true;
					}
					
					t.setName(nn);
					
					for(UUID member : t.getMembers()) {
						OfflinePlayer pl = Bukkit.getOfflinePlayer(member);
						if(pl != null && pl.isOnline()) {
							((Player) pl).sendMessage(main.prefix+"Your team name has been changed to §r§l"+nn+main.prefix_color+" by the leader!");
						}
					}
					
					p.sendMessage(main.prefix+"§aYou successfully changed your team name to §r§l"+nn+"§a!");
					return true;		
				}
				if(args[0].equalsIgnoreCase("promote")) {
					PlayerData pData = main.dataManager.getPlayers().stream().filter(tl -> tl.getUUID().equals(p.getUniqueId())).findFirst().orElse(null);
					if(pData.getTeamUUIDs() == null || pData.getTeamUUIDs().isEmpty()) {
						p.sendMessage(main.err_prefix+"You must be in a team!");
						return true;
					}
					Team t = main.dataManager.getTeams().stream().filter(tl -> tl.getLeader().equals(pData.getUUID())).findFirst().orElse(null);
					if(t == null) {
						p.sendMessage(main.err_prefix+"You are not the leader of any team you are in!");
						return true;
					}
					@SuppressWarnings("deprecation")
					OfflinePlayer target = Bukkit.getOfflinePlayer(args[1]);
					if(target == p) {
						p.sendMessage(main.err_prefix+"You cannot promote yourself!");
						return true;
					}
					if(target != null && t.getMembers().contains(target.getUniqueId())) {
						t.getMembers().remove(target.getUniqueId());
						t.getMembers().add(p.getUniqueId());
						t.setLeader(target.getUniqueId());
						if(target.isOnline()) ((Player) target).sendMessage(main.prefix+"§aYou have been promoted to §cLeader§a!");
						p.sendMessage(main.prefix+"§aYou have successfully promoted §e"+target.getName()+"§a to §cLeader§a!");
						return true;
					} else {
						p.sendMessage(main.err_prefix+"Could not find any team member named §l"+args[1]+"§r"+main.err_color+"!");
						return true;
					}
				} else if(args[0].equalsIgnoreCase("kick")) {
					PlayerData pData = main.dataManager.getPlayers().stream().filter(tl -> tl.getUUID().equals(p.getUniqueId())).findFirst().orElse(null);
					if(pData.getTeamUUIDs() == null || pData.getTeamUUIDs().isEmpty()) {
						p.sendMessage(main.err_prefix+"You must be in a team!");
						return true;
					}
					Team t = main.dataManager.getTeams().stream().filter(tl -> tl.getLeader().equals(pData.getUUID())).findFirst().orElse(null);
					if(t == null) {
						p.sendMessage(main.err_prefix+"You are not the leader of any team you are in!");
						return true;
					}
					@SuppressWarnings("deprecation")
					OfflinePlayer target = Bukkit.getOfflinePlayer(args[1]);
					if(target == p) {
						p.sendMessage(main.err_prefix+"You cannot kick yourself! Try \"/teamh disband\" to disband your team.");
						return true;
					}
					if(target != null && t.getMembers().contains(target.getUniqueId())) {
						PlayerData tData = main.dataManager.getPlayers().stream().filter(tl -> tl.getUUID().equals(target.getUniqueId())).findFirst().orElse(null);
						List<UUID> teamUUIDs = tData.getTeamUUIDs();
						teamUUIDs.remove(t.getUUID());
						tData.setTeamList(teamUUIDs);
						t.getMembers().remove(target.getUniqueId());
						if(t.getPerms().containsKey(target.getUniqueId().toString())) {
							t.getPerms().remove(target.getUniqueId().toString());
						}
						for(Region rg : main.dataManager.getRegions()) {
							if(rg.getTeamUUID().equals(t.getUUID())) {
								if(rg.getPerms().containsKey(p.getUniqueId().toString())) {
									rg.getPerms().remove(p.getUniqueId().toString());
								}
							}
						}
						
						for(UUID pls : t.getMembers()) {
							OfflinePlayer pl = Bukkit.getOfflinePlayer(pls);
							if(pl != null && pl.isOnline()) {
								((Player) pl).sendMessage(main.prefix+"§6"+target.getName()+"§c has been kicked out of "+t.getName()+"!");
							}
						}
						if(target.isOnline()) ((Player) target).sendMessage(main.prefix+"§cYou have been kicked out of "+t.getName()+"!");
						p.sendMessage(main.prefix+"§cYou have successfully kicked §6"+target.getName()+"§c out of the team!");
						return true;
					} else {
						p.sendMessage(main.err_prefix+"Could not find any team member named §l"+args[1]+"§r"+main.err_color+"!");
						return true;
					}
				} else{
					p.sendMessage(main.err_prefix+"Try /"+cmd.getName().toLowerCase()+" help");
					return true;
				}
			} else if(args.length == 3 && args[0].equalsIgnoreCase("invite")) {
			PlayerData pData = main.dataManager.getPlayers().stream().filter(tl -> tl.getUUID().equals(p.getUniqueId())).findFirst().orElse(null);
			if(pData.getTeamUUIDs() == null || pData.getTeamUUIDs().isEmpty()) {
				p.sendMessage(main.err_prefix+"You must be in a team!");
				return true;
			}
			String teamName = args[2];
			Team t = main.dataManager.getTeams().stream().filter(tl -> tl.getName().equalsIgnoreCase(teamName)).findFirst().orElse(null);
			if(t == null) {
				p.sendMessage(main.err_prefix+"Could not find that team!");
				return true;
			}
			if(!t.getMembers().contains(p.getUniqueId()) && !t.getLeader().equals(p.getUniqueId())) {
				p.sendMessage(main.err_prefix+"You are not apart of that team!");
				return true;
			}
			if(!t.getLeader().equals(p.getUniqueId()) && !t.playerHas(p.getUniqueId(), Permission.TEAM_CREATE_INVITES)) {
				p.sendMessage(main.err_prefix+"You do not have the permission!");
				return true;
			}
			@SuppressWarnings("deprecation")
			OfflinePlayer target = Bukkit.getOfflinePlayer(args[1]);
			if(target == p) {
				p.sendMessage(main.err_prefix+"You cannot invite yourself!");
				return true;
			}
			if(target == null) {
				p.sendMessage(main.err_prefix+"Could not find any player named §l"+args[1]+"§r"+main.err_color+"!");
				return true;
			}
				
			if(t.getMembers().contains(target.getUniqueId()) || t.getLeader().equals(target.getUniqueId())) {
				p.sendMessage(main.err_prefix+"Could not invite §l"+args[1]+"§r"+main.err_color+" to "+t.getName()+" as they are already apart of it!");
				return true;
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
					return true;
				}
				p.sendMessage(main.err_prefix+"That player has already been invited to "+t.getName()+"!");
				return true;
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
			return true;
		} else {
			p.sendMessage(main.err_prefix+"Try /"+cmd.getName().toLowerCase()+" help");
				return true;
			}
		}
		return false;
	}
	
	
	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
	    if(!(sender instanceof Player)) return null;
	    Player p = (Player) sender;

	    List<String> suggestions = new ArrayList<>();

	    if(cmd.getName().equalsIgnoreCase("team") || cmd.getName().equalsIgnoreCase("t")) {
	        if(args.length == 1) {
	            List<String> su = new ArrayList<>();

	            for(Team t : main.dataManager.getTeams()) {
	                if(t.getLeader().equals(p.getUniqueId())) {
	                    su.add(0, t.getName()); 
	                } else if(t.getMembers().contains(p.getUniqueId())) {
	                    su.add(t.getName());
	                }
	            }

	            suggestions.addAll(su);
	        }
	    } else if(cmd.getName().equalsIgnoreCase("teamh") || cmd.getName().equalsIgnoreCase("th")) {
	    	if(args.length == 1) {
	            List<String> su = new ArrayList<>();
	            boolean isLeader = false;
	            boolean isInTeam = false;
	            
	            boolean canInvite = false;
	            boolean canLeave = false;

	            for(Team t : main.dataManager.getTeams()) {
	                if(t.getLeader().equals(p.getUniqueId())) {
	                    isLeader = true;
	                    isInTeam = true;
	                    canInvite = true;
	                } else if(t.getMembers().contains(p.getUniqueId())) {
	                    isInTeam = true;
	                    canLeave = true;
	                    if(t.playerHas(p.getUniqueId(), Permission.TEAM_CREATE_INVITES)) {
	                    	canInvite = true;
	                    }
	                }
	            }

	            if(isLeader) {
	                su.addAll(Arrays.asList("disband", "promote", "kick"));
	            }
	            if(!isInTeam) {
	                su.add("create");
	            }
	            if(canLeave) {
	                su.add("leave");
	            }
	            if(canInvite) {
	            	su.add("invite");
	            }

	            su.addAll(Arrays.asList("list", "help", "join"));
	            suggestions.addAll(su);
	        } else if(args.length == 2) {
				boolean isLeader = false;
				boolean isMember = false;
				boolean canInvite = false;

	            for(Team t : main.dataManager.getTeams()) {
	                if(t.getLeader().equals(p.getUniqueId())) {
	                    isLeader = true;
	                    canInvite = true;
	                } else if(t.getMembers().contains(p.getUniqueId())) {
	                    isMember = true;
	                    if(t.playerHas(p.getUniqueId(), Permission.TEAM_CREATE_INVITES)) {
	                    	canInvite = true;
	                    }
	                }
	            }
	            List<String> su = new ArrayList<>();
	            
	            if(isLeader) {
		            if(args[0].equalsIgnoreCase("kick") || args[0].equalsIgnoreCase("promote")) {
		                for(Team t : main.dataManager.getTeams()) {
		                    if(t.getLeader().equals(p.getUniqueId())) {
		                        for(UUID uuid : t.getMembers()) {
		                            if(!uuid.equals(p.getUniqueId())) {
		                                su.add(Bukkit.getOfflinePlayer(uuid).getName());
		                            }
		                        }
		                    }
		                }
		            }
	            } 
	            if(isMember) {
	            	if(args[0].equalsIgnoreCase("leave")) {
		                for(Team t : main.dataManager.getTeams()) {
		                    if(t.getMembers().contains(p.getUniqueId()) && !t.getLeader().equals(p.getUniqueId())) {
		                        su.add(t.getName());
		                    }
		                }
		            }
	            }
	            if(canInvite) {
	            	if(args[0].equalsIgnoreCase("invite")) {
	            		for(Player pl : Bukkit.getOnlinePlayers()) {
	            			if(pl != p) {
	            				su.add(pl.getName());
	            			}
	            		}
	            	}
	            }
	            suggestions.addAll(su);
	        } else if(args.length == 3 && args[0].equalsIgnoreCase("invite")) {
	        	List<String> teams = new ArrayList<>();
	        	
	            for(Team t : main.dataManager.getTeams()) {
	                if(t.getLeader().equals(p.getUniqueId())) {
	                    teams.add(t.getName());
	                } else if(t.getMembers().contains(p.getUniqueId())) {
	                    if(t.playerHas(p.getUniqueId(), Permission.TEAM_CREATE_INVITES)) {
	                    	teams.add(t.getName());
	                    }
	                }
	            }
	            if(!teams.isEmpty()) {
	            	suggestions.addAll(teams);
	            }
	        }
	    }

	    return suggestions.stream()
	            .filter(s -> s.toLowerCase().startsWith(args[args.length - 1].toLowerCase()))
	            .collect(Collectors.toList());
	}
	
	
}

