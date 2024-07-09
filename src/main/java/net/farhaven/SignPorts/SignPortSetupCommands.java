package net.farhaven.SignPorts;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class SignPortSetupCommands implements CommandExecutor {

    private final SignPorts plugin;

    public SignPortSetupCommands(SignPorts plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Only players can use this command.");
            return true;
        }

        Player player = (Player) sender;
        SignPortSetup setup = plugin.getSignPortSetupManager().getPendingSetup(player);

        if (setup == null) {
            player.sendMessage(ChatColor.RED + "You are not currently setting up a SignPort.");
            return true;
        }

        if (label.equalsIgnoreCase("confirm")) {
            ItemStack itemInHand = player.getInventory().getItemInMainHand();
            if (itemInHand.getType() == Material.AIR) {
                player.sendMessage(ChatColor.RED + "You must hold an item in your hand.");
                return true;
            }

            setup.setGuiItem(itemInHand);
            player.sendMessage(ChatColor.YELLOW + "Item set. Now type /setname <name> to set the name for the SignPort.");
            plugin.getSignPortSetupManager().updatePendingSetup(player, setup);
            return true;
        }

        if (label.equalsIgnoreCase("setname")) {
            if (args.length == 0) {
                player.sendMessage(ChatColor.RED + "You must provide a name.");
                return true;
            }

            String name = String.join(" ", args);
            setup.setName(name);
            player.sendMessage(ChatColor.YELLOW + "Name set. Now type /setdesc <description> to set the description for the SignPort.");
            plugin.getSignPortSetupManager().updatePendingSetup(player, setup);
            return true;
        }

        if (label.equalsIgnoreCase("setdesc")) {
            if (args.length == 0) {
                player.sendMessage(ChatColor.RED + "You must provide a description.");
                return true;
            }

            String description = String.join(" ", args);
            setup.setDescription(description);
            player.sendMessage(ChatColor.GREEN + "SignPort setup complete.");
            plugin.getSignPortSetupManager().completePendingSetup(player);

            return true;
        }

        return false;
    }
}