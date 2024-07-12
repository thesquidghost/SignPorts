package net.farhaven.SignPorts;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Arrays;

public class SignPortCommand implements CommandExecutor {
    private final SignPorts plugin;

    public SignPortCommand(SignPorts plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(ChatColor.RED + "This command can only be used by players.");
            return true;
        }

        if (args.length == 0) {
            player.sendMessage(ChatColor.RED + "Usage: /signport <create|list|remove|teleport|gui|setname|setdesc|setitem|reload> [arguments]");
            return true;
        }

        String subCommand = args[0].toLowerCase();

        return switch (subCommand) {
            case "create" -> handleCreate(player, args);
            case "list" -> handleList(player);
            case "remove" -> handleRemove(player, args);
            case "teleport" -> handleTeleport(player, args);
            case "gui" -> handleGUI(player);
            case "setname" -> handleSetName(player, args);
            case "setdesc" -> handleSetDescription(player, args);
            case "setitem" -> handleSetItem(player);
            case "reload" -> handleReload(player);
            default -> {
                player.sendMessage(ChatColor.RED + "Unknown subcommand. Use create, list, remove, teleport, gui, setname, setdesc, setitem, or reload.");
                yield true;
            }
        };
    }

    private boolean handleCreate(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(ChatColor.RED + "Usage: /signport create <name>");
            return false;
        }

        String name = args[1];
        if (plugin.getSignPortMenu().getSignPortByName(name) != null) {
            player.sendMessage(ChatColor.RED + "A SignPort with that name already exists.");
            return false;
        }

        if (plugin.playerHasSignPort(player)) {
            player.sendMessage(ChatColor.RED + "You have reached the maximum number of SignPorts you can create.");
            return false;
        }

        Location location = player.getLocation();
        SignPortSetup setup = new SignPortSetup(location);
        setup.setName(name);
        setup.setOwnerUUID(player.getUniqueId());
        setup.setOwnerName(player.getName());
        plugin.getSignPortSetupManager().startSetup(player, setup);
        player.sendMessage(ChatColor.GREEN + "SignPort creation started. Use /confirm to set the item, then /setname and /setdesc to complete the setup.");
        return true;
    }

    private boolean handleList(Player player) {
        Map<String, SignPortSetup> signPorts = plugin.getSignPortMenu().getSignPorts();
        if (signPorts.isEmpty()) {
            player.sendMessage(ChatColor.YELLOW + "There are no SignPorts available.");
            return false;
        }

        player.sendMessage(ChatColor.GREEN + "Available SignPorts:");
        for (SignPortSetup setup : signPorts.values()) {
            player.sendMessage(ChatColor.YELLOW + "- " + setup.getName() + " (Owner: " + setup.getOwnerName() + ")");
        }
        return true;
    }

    private boolean handleRemove(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(ChatColor.RED + "Usage: /signport remove <name>");
            return false;
        }

        String name = args[1];
        SignPortSetup setup = plugin.getSignPortMenu().getSignPortByName(name);
        if (setup == null) {
            player.sendMessage(ChatColor.RED + "No SignPort found with that name.");
            return false;
        }

        if (!setup.getOwnerUUID().equals(player.getUniqueId()) && !player.hasPermission("signports.admin")) {
            player.sendMessage(ChatColor.RED + "You don't have permission to remove this SignPort.");
            return false;
        }

        plugin.getSignPortMenu().removeSignPort(name);
        player.sendMessage(ChatColor.GREEN + "SignPort '" + name + "' has been removed.");
        return true;
    }

    private boolean handleTeleport(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(ChatColor.RED + "Usage: /signport teleport <name>");
            return false;
        }

        String name = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
        plugin.getLogger().info("Attempting to teleport to: '" + name + "'");

        SignPortSetup setup = plugin.getSignPortMenu().getSignPortByName(name);
        if (setup == null) {
            plugin.getLogger().info("SignPort not found: '" + name + "'");
            player.sendMessage(ChatColor.RED + "No SignPort found with that name.");
            return false;
        }

        Location destination = setup.getSignLocation();
        plugin.getLogger().info("Destination location: " + destination);

        if (!plugin.isSafeLocation(destination)) {
            plugin.getLogger().info("Destination is not safe: " + destination);
            player.sendMessage(ChatColor.RED + "The destination is not safe. Teleportation cancelled.");
            return false;
        }

        if (!plugin.checkCooldown(player)) {
            plugin.getLogger().info("Teleportation cancelled for " + player.getName() + " to " + name + ". Cooldown active.");
            return false;
        }

        plugin.getLogger().info("Initiating teleport countdown for " + player.getName() + " to " + name);
        player.sendMessage(ChatColor.YELLOW + "Preparing to teleport to " + setup.getName() + ". Don't move!");
        new TeleportTask(plugin, player, destination, setup.getName()).runTaskTimer(plugin, 0L, 20L);
        return true;
    }

    private boolean handleGUI(Player player) {
        if (!player.hasPermission("signports.gui")) {
            player.sendMessage(ChatColor.RED + "You don't have permission to use the SignPort GUI.");
            return false;
        }
        plugin.getSignPortMenu().openSignPortMenu(player);
        return true;
    }

    private boolean handleSetName(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(ChatColor.RED + "Usage: /signport setname <new name>");
            return false;
        }
        String newName = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
        plugin.updateSignPortName(player, newName);
        return true;
    }

    private boolean handleSetDescription(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(ChatColor.RED + "Usage: /signport setdesc <new description>");
            return false;
        }
        String newDesc = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
        plugin.updateSignPortDescription(player, newDesc);
        return true;
    }

    private boolean handleSetItem(Player player) {
        ItemStack itemInHand = player.getInventory().getItemInMainHand();
        if (itemInHand.getType() == Material.AIR) {
            player.sendMessage(ChatColor.RED + "You must be holding an item to set as the SignPort icon.");
            return false;
        }
        plugin.updateSignPortItem(player, itemInHand);
        return true;
    }

    private boolean handleReload(Player player) {
        if (!player.hasPermission("signports.admin")) {
            player.sendMessage(ChatColor.RED + "You don't have permission to use this command.");
            return false;
        }
        plugin.reloadPluginConfig();
        player.sendMessage(ChatColor.GREEN + "SignPorts config reloaded and cooldowns reset.");
        return true;
    }
}