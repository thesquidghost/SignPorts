package dev.airfrom.teamclaim.commands;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.Particle;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import dev.airfrom.teamclaim.Main;
import dev.airfrom.teamclaim.data.GUI;
import dev.airfrom.teamclaim.data.Invite;
import dev.airfrom.teamclaim.data.PlayerData;
import dev.airfrom.teamclaim.data.Team;

public class Commands implements CommandExecutor, TabCompleter{
	
	private Main main;
	private GUI GUI;
	
	public Commands(Main main) {
		this.main = main;
		this.GUI = new GUI(main);
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String msg, String[] args) {
		if(!(sender instanceof Player)) return false;
		Player p = (Player) sender;
		if(cmd.getName().equalsIgnoreCase("land")){
			if(args.length == 0){
				GUI.openLandGUI(p);
				return true;
			}else {
				p.sendMessage(main.err_prefix+"Try /land");
				return true;
			}
		} else if(cmd.getName().equalsIgnoreCase("invite")){
			if(args.length == 2 && (args[0].equalsIgnoreCase("accept") || args[0].equalsIgnoreCase("deny"))) {
				String nn = args[1];
				Team t = main.dataManager.getTeams().stream().filter(tl -> tl.getName().equalsIgnoreCase(nn)).findFirst().orElse(null);
				if(t == null) {
					p.sendMessage(main.err_prefix+"Could not find a team named §l"+nn+"§7!");
					return true;
				}
				
				Map<UUID, UUID> invited = new HashMap<>();
				for(Invite i : t.getInvites()) {
					invited.put(i.getInvitee(), i.getInviter());
				}
				
				if(!invited.containsKey(p.getUniqueId())) {
					p.sendMessage(main.err_prefix+"Could not find that invite!");
					return true;
				}
				
				if(args[0].equalsIgnoreCase("accept")) {
					OfflinePlayer op = Bukkit.getOfflinePlayer(invited.get(p.getUniqueId()));
					p.sendMessage(main.prefix+"You accepted "+op.getName()+"'s invite to join "+t.getName()+"!");
					if(op.isOnline()) {
						((Player) op).sendMessage(main.prefix+p.getName()+" accepted your invite to join "+t.getName()+"!");
					}
					
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
					
					PlayerData pData = main.dataManager.getPlayers().stream().filter(tl -> tl.getUUID().equals(p.getUniqueId())).findFirst().orElse(null);
					List<UUID> teamUUIDs = pData.getTeamUUIDs();
					teamUUIDs.add(t.getUUID());
					pData.setTeamList(teamUUIDs);
					
					p.sendMessage(main.prefix+"§aYou joined §l"+t.getName()+"§r§a!");
					return true;
				} else if(args[0].equalsIgnoreCase("deny")) {
					main.removeInvite(t, invited, p);
					OfflinePlayer op = Bukkit.getOfflinePlayer(invited.get(p.getUniqueId()));
					p.sendMessage(main.prefix+"You denied "+op.getName()+"'s invite to join "+t.getName()+"!");
					if(op.isOnline()) {
						((Player) op).sendMessage(main.prefix+p.getName()+" denied your invite to join "+t.getName()+"!");
					}
					return true;
				}
			}else {
				p.sendMessage(main.err_prefix+"Try /invite accept/deny <team>");
				return true;
			}
		} else if(cmd.getName().equalsIgnoreCase("claimblock")){			
			if(args.length == 0) {
				PlayerData pData = main.dataManager.getPlayers().stream().filter(tl -> tl.getUUID().equals(p.getUniqueId())).findFirst().orElse(null);
				p.sendMessage(main.prefix+"You currently have "+pData.getClaimBlocks()+" Claim Blocks!");
				p.sendMessage(main.prefix_color+"Deposit or withdraw claim blocks from your personal pool by performing: /claimblock deposit/withdraw <amount>");
			} else {
				p.sendMessage(main.err_prefix+"Try /claimblock");
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

	    if(cmd.getName().equalsIgnoreCase("invite")) {
	    	if(args.length == 1) {
	    		suggestions.addAll(Arrays.asList("accept","deny"));
	    	} else if(args.length == 2) {
	    		List<String> teams = new ArrayList<>();
	    		for(Team t : main.dataManager.getTeams()) {
	    			for(Invite i : t.getInvites()) {
	    				if(i.getInvitee().equals(p.getUniqueId())) {
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
	
	public void showStaticParticleWall(Player player, Particle particle, int distance, int particleCount, long durationInSeconds) {
	    Location playerLocation = player.getLocation();
	    Vector direction = playerLocation.getDirection().normalize();
	    
	    Location wallLocation = playerLocation.add(direction.multiply(distance));
	    
	    double wallHeight = 4.0;
	    double wallWidth = 4.0;
	    double step = 0.5;

	    double halfWidth = wallWidth / 2;
	    double minY = wallLocation.getY();
	    double maxY = minY + wallHeight;
	    
	    new BukkitRunnable() {
	        long endTime = System.currentTimeMillis() + (durationInSeconds * 1000);

	        @Override
	        public void run() {
	            if(System.currentTimeMillis() >= endTime) {
	                this.cancel();
	                return;
	            }

	            for(double y = minY; y <= maxY; y += step) {
	                for(double offset = -halfWidth; offset <= halfWidth; offset += step) {
	                    Location particleLocation = wallLocation.clone();
	                    if(direction.getZ() == 0) {
	                        particleLocation.add(0, y - wallLocation.getY(), offset);
	                    } else { 
	                        particleLocation.add(offset, y - wallLocation.getY(), 0);
	                    }

	                    player.spawnParticle(particle, particleLocation, particleCount, 0, 0, 0, 0);
	                }
	            }
	        }
	    }.runTaskTimer(main, 0, 10);
	}
	 
}
