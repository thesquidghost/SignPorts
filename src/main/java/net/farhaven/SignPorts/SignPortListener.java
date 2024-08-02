package net.farhaven.SignPorts;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import com.griefdefender.api.GriefDefender;
import com.griefdefender.api.claim.Claim;
import com.griefdefender.api.User;

import java.util.Objects;

public class SignPortListener implements Listener {
    private final SignPorts plugin;

    public SignPortListener(SignPorts plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onSignChange(SignChangeEvent event) {
        String signportIdentifier = plugin.getConfig().getString("signport-identifier", "[SignPort]");
        String[] lines = event.getLines();

        if (Objects.requireNonNull(lines[0]).equalsIgnoreCase(signportIdentifier)) {
            Player player = event.getPlayer();
            Location location = event.getBlock().getLocation();

            plugin.getLogger().info("Player " + player.getName() + " attempting to create SignPort at " + location);

            if (!hasPermissionToCreateSignPort(player, location)) {
                event.setCancelled(true);
                return;
            }

            if (plugin.playerHasSignPort(player)) {
                player.sendMessage(ChatColor.RED + "You already have a SignPort. You can only have one at a time.");
                event.setCancelled(true);
                return;
            }

            SignPortSetup setup = new SignPortSetup(location);
            setup.setOwnerUUID(player.getUniqueId());
            setup.setOwnerName(player.getName());
            updateSignPortConfig(player, event, signportIdentifier);
            player.sendMessage(ChatColor.YELLOW + "Hold the item you want to use as the GUI icon and type " + ChatColor.AQUA + "/confirm" + ChatColor.YELLOW + " to proceed.");
            plugin.getSignPortSetupManager().startSetup(player, setup);
        }
    }

    private boolean hasPermissionToCreateSignPort(Player player, Location location) {
        Claim claim = GriefDefender.getCore().getClaimAt(location);

        if (claim == null) {
            player.sendMessage(ChatColor.RED + "You can only create SignPorts within a claim.");
            return false;
        }

        if (!claim.getOwnerUniqueId().equals(player.getUniqueId())) {
            player.sendMessage(ChatColor.RED + "You can only create SignPorts in your own claim.");
            return false;
        }

        return true;
    }

    private boolean isPlayerAllowedToBreak(Player player, Block block) {
        SignPortSetup setup = plugin.getSignPortMenu().getSignPortByLocation(block.getLocation());
        if (setup != null && setup.getOwnerUUID().equals(player.getUniqueId())) {
            return true;
        }

        Claim claim = Objects.requireNonNull(GriefDefender.getCore().getClaimManager(block.getWorld().getUID()))
                .getClaimAt(block.getLocation().getBlockX(), block.getLocation().getBlockY(), block.getLocation().getBlockZ());

        User user = GriefDefender.getCore().getUser(player.getUniqueId());
        return claim == null || claim.canBreak(block.getType(), block.getLocation(), user);
    }

    private void updateSignPortConfig(Player player, SignChangeEvent event, String signportIdentifier) {
        event.setLine(0, ChatColor.BLUE + signportIdentifier);
        event.setLine(1, ChatColor.GREEN + player.getName());
        event.setLine(2, "");
        event.setLine(3, "");
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        Block block = event.getBlock();
        Player player = event.getPlayer();

        if (block.getState() instanceof Sign sign) {
            String signportIdentifier = plugin.getConfig().getString("signport-identifier", "[SignPort]");
            if (sign.getLine(0).equalsIgnoreCase(ChatColor.BLUE + signportIdentifier)) {
                SignPortSetup setup = plugin.getSignPortMenu().getSignPortByLocation(block.getLocation());
                if (setup != null) {
                    if (!isPlayerAllowedToBreak(player, block)) {
                        event.setCancelled(true);
                        player.sendMessage(ChatColor.RED + "You don't have permission to remove this SignPort.");
                        return;
                    }

                    plugin.getSignPortMenu().removeSignPort(setup.getName());
                    player.sendMessage(ChatColor.RED + "Your SignPort has been destroyed.");
                    plugin.getLogger().warning("SignPort owned by " + player.getName() + " has been destroyed at " + block.getLocation());
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onSignEdit(SignChangeEvent event) {
        Block block = event.getBlock();
        if (block.getState() instanceof Sign sign) {
            String signportIdentifier = plugin.getConfig().getString("signport-identifier", "[SignPort]");
            if (sign.getLine(0).equalsIgnoreCase(ChatColor.BLUE + signportIdentifier)) {
                Player player = event.getPlayer();
                SignPortSetup setup = plugin.getSignPortMenu().getSignPortByLocation(block.getLocation());
                if (setup != null) {
                    if (!setup.getOwnerUUID().equals(player.getUniqueId())) {
                        event.setCancelled(true);
                        player.sendMessage(ChatColor.RED + "You don't have permission to edit this SignPort.");
                    } else if (!Objects.requireNonNull(event.getLine(0)).equalsIgnoreCase(signportIdentifier)) {
                        plugin.getSignPortMenu().removeSignPort(setup.getName());
                        player.sendMessage(ChatColor.RED + "Your SignPort has been removed due to editing the sign.");
                        plugin.getLogger().warning("SignPort owned by " + player.getName() + " has been removed due to editing at " + block.getLocation());
                    }
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getClickedBlock() != null && event.getClickedBlock().getState() instanceof Sign sign) {
            String signportIdentifier = plugin.getConfig().getString("signport-identifier", "[SignPort]");
            if (sign.getLine(0).equalsIgnoreCase(ChatColor.BLUE + signportIdentifier)) {
                Player player = event.getPlayer();
                SignPortSetup setup = plugin.getSignPortMenu().getSignPortByLocation(event.getClickedBlock().getLocation());
                if (setup != null && !setup.getOwnerUUID().equals(player.getUniqueId())) {
                    event.setCancelled(true);
                    // Optionally, you can add a message here if you want to inform the player
                    player.sendMessage(ChatColor.RED + "You don't have permission to interact with this SignPort.");
                }
            }
        }
    }
}