package net.farhaven.signports;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class SignPortGUI implements Listener {
    private final SignPorts plugin;

    public SignPortGUI(SignPorts plugin) {
        this.plugin = plugin;
    }

    public void openSignPortMenu(Player player) {
        Inventory signPortMenu = Bukkit.createInventory(null, InventoryType.CHEST, "Select a SignPort");

        // Populate the inventory with SignPort items
        if (plugin.getConfig().isConfigurationSection("signports")) {
            for (String signPortName : plugin.getConfig().getConfigurationSection("signports").getKeys(false)) {
                Material icon = Material.valueOf(plugin.getConfig().getString("signport-icon", "OAK_SIGN"));
                ItemStack item = new ItemStack(icon);
                ItemMeta meta = item.getItemMeta();
                meta.setDisplayName(ChatColor.GREEN + signPortName);
                item.setItemMeta(meta);
                signPortMenu.addItem(item);
            }