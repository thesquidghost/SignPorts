package net.farhaven.SignPorts;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

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
            player.sendMessage(ChatColor.RED + "Usage: /signport <create|list|remove|teleport|gui> [name]");
            return true;
        }

        String subCommand = args[0].toLowerCase();

        return switch (subCommand) {
            case "create" -> handleCreate(player, args);
            case "list" -> handleList(player);
            case "remove" -> handleRemove(player, args);
            case "teleport" -> handleTeleport(player, args);
            case "gui" -> handleGUI(player);
            default -> {
                player.sendMessage(ChatColor.RED + "Unknown subcommand. Use create, list, remove, teleport, or gui.");
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

        if (plugin.playerHasReachedSignPortLimit(player)) {
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
            return false;  // Return false when there are no SignPorts
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

        String name = args[1];
        SignPortSetup setup = plugin.getSignPortMenu().getSignPortByName(name);
        if (setup == null) {
            player.sendMessage(ChatColor.RED + "No SignPort found with that name.");
            return false;
        }

        Location destination = setup.getSignLocation();
        if (!plugin.isSafeLocation(destination)) {
            player.sendMessage(ChatColor.RED + "The destination is not safe. Teleportation cancelled.");
            return false;
        }

        player.teleport(destination);
        player.sendMessage(ChatColor.GREEN + "You've been teleported to " + setup.getName() + ".");
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
}