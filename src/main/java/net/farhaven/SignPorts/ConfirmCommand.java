package net.farhaven.SignPorts;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class ConfirmCommand implements CommandExecutor {
    private final SignPorts plugin;

    public ConfirmCommand(SignPorts plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("This command can only be used by players.");
            return true;
        }

        Player player = (Player) sender;
        SignPortSetupManager setupManager = plugin.getSignPortSetupManager();

        if (setupManager.hasPendingSetup(player)) {
            SignPortSetup setup = setupManager.getPendingSetup(player);
            if (setup != null) {
                ItemStack itemInHand = player.getInventory().getItemInMainHand();
                if (itemInHand.getType().isAir()) {
                    player.sendMessage(ChatColor.RED + "Please hold an item to use as the GUI icon.");
                    return true;
                }

                setup.setGuiItem(itemInHand.clone());

                // You might want to prompt for or set other properties here
                // For example:
                // setup.setDescription("A new SignPort");
                // setup.setPermission("signports.use." + setup.getName().toLowerCase());

                setupManager.completePendingSetup(player);
                player.sendMessage(ChatColor.GREEN + "SignPort setup completed successfully!");
            } else {
                player.sendMessage(ChatColor.RED + "An error occurred during setup. Please try again.");
            }
        } else {
            player.sendMessage(ChatColor.RED + "You don't have a pending SignPort setup.");
        }

        return true;
    }
}