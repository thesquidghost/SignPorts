package net.farhaven.SignPorts;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Collections;

public class SignPortLockGUI implements Listener {
    // Using a fixed prefix for the GUI title.
    private final String guiTitlePrefix = ChatColor.DARK_BLUE + "Lock Options for ";
    private final SignPorts plugin;

    public SignPortLockGUI(SignPorts plugin) {
        this.plugin = plugin;
    }

    public void openLockOptions(Player player, SignPortSetup setup) {
        // Use the SignPort's name in lowercase in the title to ensure consistent lookup.
        Inventory lockGUI = Bukkit.createInventory(null, 9, guiTitlePrefix + setup.getName().toLowerCase());

        ItemStack lockItem = createGuiItem(Material.IRON_DOOR, ChatColor.GREEN + "Lock SignPort", "Click to lock your SignPort.");
        ItemStack unlockItem = createGuiItem(Material.BARRIER, ChatColor.RED + "Unlock SignPort", "Click to unlock your SignPort.");

        lockGUI.setItem(3, lockItem);
        lockGUI.setItem(5, unlockItem);

        player.openInventory(lockGUI);
    }

    private ItemStack createGuiItem(Material material, String name, String lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        meta.setLore(Collections.singletonList(lore));
        item.setItemMeta(meta);
        return item;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player))
            return;
        Player player = (Player) event.getWhoClicked();
        String title = event.getView().getTitle();
        if (!title.startsWith(guiTitlePrefix))
            return;

        event.setCancelled(true);
        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || clicked.getType() == Material.AIR)
            return;

        // Extract the SignPort name from the title.
        // Title format: "Lock Options for <name>" where <name> is stored in lower-case.
        String signPortName = title.substring(guiTitlePrefix.length()).trim();
        // Look up using lower-case to match how they are stored in the in‑memory map.
        SignPortSetup setup = plugin.getSignPortMenu().getSignPortByName(signPortName);
        if (setup == null) {
            player.sendMessage(ChatColor.RED + "SignPort not found.");
            player.closeInventory();
            return;
        }
        if (!setup.getOwnerUUID().equals(player.getUniqueId())) {
            player.sendMessage(ChatColor.RED + "Only the owner can modify lock status.");
            player.closeInventory();
            return;
        }
        // Toggle the lock state based on the clicked item.
        if (clicked.getType() == Material.IRON_DOOR) {
            setup.setLocked(true);
            plugin.getSignPortStorage().saveSignPorts();
            // Re-add the SignPort so that it is updated in the in‑memory map.
            plugin.getSignPortMenu().addSignPort(setup);
            player.sendMessage(ChatColor.GREEN + "Your SignPort has been locked.");
        } else if (clicked.getType() == Material.BARRIER) {
            setup.setLocked(false);
            plugin.getSignPortStorage().saveSignPorts();
            plugin.getSignPortMenu().addSignPort(setup);
            player.sendMessage(ChatColor.GREEN + "Your SignPort has been unlocked.");
        }
        player.closeInventory();
    }
}