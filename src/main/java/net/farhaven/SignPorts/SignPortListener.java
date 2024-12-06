package net.farhaven.SignPorts;

import me.partlysunny.sunbeam.menu.Menus;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.type.WallHangingSign;
import org.bukkit.block.data.type.WallSign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;

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
            setup.setLocked(false);
            updateSignPortConfig(player, event, signportIdentifier);
            player.sendMessage(ChatColor.YELLOW + "Hold the item you want to use as the GUI icon and type " + ChatColor.AQUA + "/confirm" + ChatColor.YELLOW + " to proceed.");
            plugin.getSignPortSetupManager().startSetup(player, setup);
        }
    }

    private boolean hasPermissionToCreateSignPort(Player player, Location location) {
        // Check if player is in their team's claim
        if (!SignPorts.griefDefenderHook.canPlayerMakeClaim(player, location)) {
            player.sendMessage(ChatColor.RED + "You can only create SignPorts in your team's claim.");
            return false;
        }

        // Check if player has build permissions in this claim
        if (!SignPorts.griefDefenderHook.canPlayerBreakBlock(player, location.getBlock())) {
            player.sendMessage(ChatColor.RED + "You don't have build permissions in this claim.");
            return false;
        }

        return true;
    }

    private boolean isPlayerAllowedToBreak(Player player, Block block) {
        SignPortSetup setup = plugin.getSignPortMenu().getSignPortByLocation(block.getLocation());

        // If it's their SignPort, they can break it
        if (setup != null && setup.getOwnerUUID().equals(player.getUniqueId())) {
            return true;
        }

        // Otherwise, check TeamClaim permissions
        return SignPorts.griefDefenderHook.canPlayerBreakBlock(player, block);
    }

    private void updateSignPortConfig(Player player, SignChangeEvent event, String signportIdentifier) {
        event.setLine(0, ChatColor.BLUE + signportIdentifier);
        event.setLine(1, ChatColor.GREEN + player.getName());
        event.setLine(2, "");
        event.setLine(3, "");
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        Block brokenBlock = event.getBlock();
        Player player = event.getPlayer();
        Block[] attached = {brokenBlock, brokenBlock.getRelative(BlockFace.DOWN), brokenBlock.getRelative(BlockFace.UP),
                brokenBlock.getRelative(BlockFace.NORTH), brokenBlock.getRelative(BlockFace.SOUTH),
                brokenBlock.getRelative(BlockFace.WEST), brokenBlock.getRelative(BlockFace.EAST)};

        for (Block block : attached) {
            if (block.getState() instanceof Sign sign) {
                String signportIdentifier = plugin.getConfig().getString("signport-identifier", "[SignPort]");
                if (sign.getLine(0).equalsIgnoreCase(ChatColor.BLUE + signportIdentifier)) {
                    SignPortSetup setup = plugin.getSignPortMenu().getSignPortByLocation(block.getLocation());
                    if (setup != null) {
                        // Check if the sign is attached to the broken block
                        BlockData blockData = block.getBlockData();
                        if (blockData instanceof WallSign) {
                            Block attachedBlock = block.getRelative(((WallSign) blockData).getFacing().getOppositeFace());
                            if (!attachedBlock.getLocation().equals(brokenBlock.getLocation())) {
                                continue;
                            }
                        }

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
                    // Check if player owns the SignPort or has team permissions
                    if (!setup.getOwnerUUID().equals(player.getUniqueId()) &&
                            !SignPorts.griefDefenderHook.canPlayerBreakBlock(player, block)) {
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
                if (setup != null) {
                    // Check if player owns the SignPort or has team permissions
                    if (!setup.getOwnerUUID().equals(player.getUniqueId()) &&
                            !SignPorts.griefDefenderHook.canPlayerBreakBlock(player, event.getClickedBlock())) {
                        event.setCancelled(true);
                        player.sendMessage(ChatColor.RED + "You don't have permission to interact with this SignPort.");
                    } else if (player.isSneaking()) {
                        event.setCancelled(true);
                        EditSignUI.editing.put(player.getUniqueId(), event.getClickedBlock().getLocation());
                        Menus.open(player, "editsignport");
                    }
                }
            }
        }
    }
}