package net.farhaven.SignPorts;

import net.farhaven.SignPorts.SignPorts;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SignPortCommand implements CommandExecutor {
    private final SignPorts plugin;

    public SignPortCommand(SignPorts plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command,
                             String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(ChatColor.RED +
                    "This command can only be used by players.");
            return true;
        }

        if (args.length == 0) {
            player.sendMessage(ChatColor.RED +
                    "Usage: /signport <create|list|remove|teleport|gui> [name]");
            return true;
        }

        String subCommand = args[0].toLowerCase();

        switch (subCommand) {
            case "create":
                return handleCreate(player, args);
            case "list":
                return handleList(player);
            case "remove":
                return handleRemove(player, args);
            case "teleport":
                return handleTeleport(player, args);
            case "gui":
                return handleGUI(player);
            default:
                player.sendMessage(ChatColor.RED +
                        "Unknown subcommand. Use create, list, remove, teleport, or gui.");
                return true;
        }
    }

    private boolean handleCreate(Player player, String[] args) {
        if (!player.hasPermission("signports.create")) {
            player.sendMessage(ChatColor.RED +
                    "You don't have permission to create SignPorts.");
            return true;
        }
        if (args.length < 2) {
            player.sendMessage(ChatColor.RED + "Usage: /signport create <name>");
            return true;
        }
        String signPortName = args[1];
        Location location = player.getLocation();
        plugin.getConfig().set("signports." + signPortName, location);
        plugin.saveConfig();
        String message =
                plugin.getConfig().getString("messages.creation-success",
                        "SignPort %signport% created successfully!");
        player.sendMessage(ChatColor.GREEN +
                message.replace("%signport%", signPortName));
        return true;
    }

    private boolean handleList(Player player) {
        if (!player.hasPermission("signports.list")) {
            player.sendMessage(ChatColor.RED +
                    "You don't have permission to list SignPorts.");
            return true;
        }
        player.sendMessage(ChatColor.GREEN + "Available SignPorts:");
        for (String signPortName : plugin.getConfig().getConfigurationSection("signports").
                getKeys(false)) {
            player.sendMessage(ChatColor.YELLOW + "- " + signPortName);
        }
        return true;
    }

    private boolean handleRemove(Player player, String[] args) {
        if (!player.hasPermission("signports.remove")) {
            player.sendMessage(ChatColor.RED +
                    "You don't have permission to remove SignPorts.");
            return true;
        }
        if (args.length < 2) {
            player.sendMessage(ChatColor.RED + "Usage: /signport remove <name>");
            return true;
        }
        String signPortName = args[1];
        if (plugin.getConfig().get("signports." + signPortName) != null) {
            plugin.getConfig().set("signports." + signPortName, null);
            plugin.saveConfig();
            String message =
                    plugin.getConfig().getString("messages.removal-success",
                            "SignPort %signport% removed successfully!");
            player.sendMessage(ChatColor.GREEN +
                    message.replace("%signport%", signPortName));
        } else {
            player.sendMessage(ChatColor.RED + "SignPort " + signPortName +
                    " does not exist.");
        }
        return true;
    }

    private boolean handleTeleport(Player player, String[] args) {
        if (!player.hasPermission("signports.use")) {
            player.sendMessage(ChatColor.RED +
                    "You don't have permission to use SignPorts.");
            return true;
        }
        if (args.length < 2) {
            player.sendMessage(ChatColor.RED +
                    "Usage: /signport teleport <name>");
            return true;
        }
        String signPortName = args[1];
        Location signPortLocation =
                plugin.getConfig().getLocation("signports." + signPortName);
        if (signPortLocation != null) {
            player.teleport(signPortLocation);
            String message =
                    plugin.getConfig().getString("messages.teleport-success",
                            "You've been teleported to %signport%!");
            player.sendMessage(ChatColor.GREEN +
                    message.replace("%signport%", signPortName));
        } else {
            player.sendMessage(ChatColor.RED + "SignPort " + signPortName +
                    " does not exist.");
        }
        return true;
    }

    private boolean handleGUI(Player player) {
        if (!player.hasPermission("signports.gui")) {
            player.sendMessage(ChatColor.RED +
                    "You don't have permission to use the SignPort GUI.");
            return true;
        }
        new net.farhaven.SignPorts.SignPortGUI(plugin).openSignPortMenu(player);
        return true;
    }
}