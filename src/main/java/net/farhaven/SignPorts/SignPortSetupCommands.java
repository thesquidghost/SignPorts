package net.farhaven.SignPorts;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class SignPortSetupCommands implements CommandExecutor {

    private final SignPorts plugin;

    public SignPortSetupCommands(SignPorts plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(ChatColor.RED + "Only players can use this command.");
            return true;
        }

        SignPortSetup setup = plugin.getSignPortSetupManager().getPendingSetup(player);

        if (setup == null) {
            player.sendMessage(ChatColor.RED + "You are not currently setting up a SignPort.");
            return true;
        }

        return switch (command.getName().toLowerCase()) {
            case "confirm" -> handleConfirm(player);
            case "setname" -> handleSetName(player, args);
            case "setdesc" -> handleSetDesc(player, args);
            default -> false;
        };
    }

    private boolean handleConfirm(Player player) {
        ItemStack itemInHand = player.getInventory().getItemInMainHand().clone();
        if (itemInHand.getType() == Material.AIR) {
            player.sendMessage(ChatColor.RED + "You must hold an item in your hand.");
            return false;
        }

        itemInHand.setAmount(1);
        SignPortSetup setup = plugin.getSignPortSetupManager().getPendingSetup(player);
        setup.setGuiItem(itemInHand);
        player.sendMessage(ChatColor.YELLOW + "Item set. Now type /setname <name> to set the name for the SignPort.");
        plugin.getSignPortSetupManager().updatePendingSetup(player, setup);
        return true;
    }

    private boolean handleSetName(Player player, String[] args) {
        if (args.length == 0) {
            player.sendMessage(ChatColor.RED + "Usage: /setname <name>");
            return false;
        }

        String name = String.join(" ", args);
        if (plugin.getSignPortMenu().getSignPortByName(name) != null) {
            player.sendMessage(ChatColor.RED + "A SignPort with that name already exists. Please choose a different name.");
            return false;
        }

        SignPortSetup setup = plugin.getSignPortSetupManager().getPendingSetup(player);
        setup.setName(name);
        player.sendMessage(ChatColor.YELLOW + "Name set to '" + name + "'. Now type /setdesc <description> to set the description for the SignPort.");
        plugin.getSignPortSetupManager().updatePendingSetup(player, setup);
        return true;
    }

    private boolean handleSetDesc(Player player, String[] args) {
        if (args.length == 0) {
            player.sendMessage(ChatColor.RED + "Usage: /setdesc <description>");
            return false;
        }

        String description = String.join(" ", args);
        SignPortSetup setup = plugin.getSignPortSetupManager().getPendingSetup(player);
        setup.setDescription(description);
        player.sendMessage(ChatColor.GREEN + "SignPort setup complete. Your SignPort '" + setup.getName() + "' is now active!");
        plugin.getSignPortSetupManager().completePendingSetup(player);
        plugin.saveSignPort(setup);

        return true;
    }
}