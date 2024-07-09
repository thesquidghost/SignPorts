package net.farhaven.SignPorts;

import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;
import java.util.stream.Collectors;

public class SignPortMenu implements Listener {

    private final SignPorts plugin;
    private final List<Material> signMaterials;
    private final Map<String, SignPortSetup> signPorts;
    private Inventory menu;
    private int currentPage = 0;
    private static final int ROWS = 6;
    private static final int SLOTS_PER_PAGE = (ROWS - 4) * 9;

    public SignPortMenu(SignPorts plugin) {
        this.plugin = plugin;
        this.signMaterials = findSignMaterials();
        this.signPorts = new HashMap<>();
        this.menu = Bukkit.createInventory(null, ROWS * 9, plugin.getConfig().getString("menu-name", "SignPorts Menu"));
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    public void addSignPort(SignPortSetup setup) {
        signPorts.put(setup.getName(), setup);
        updateMenu();
    }

    public Map<String, SignPortSetup> getSignPorts() {
        return signPorts;
    }

    public void openSignPortMenu(Player player) {
        updateMenu();
        player.openInventory(menu);
    }

    private List<Material> findSignMaterials() {
        return Arrays.stream(Material.values())
                .filter(material -> material.name().endsWith("_SIGN"))
                .collect(Collectors.toList());
    }

    private void updateMenu() {
        // ... (implementation remains the same)
    }

    private void addNavigationButtons() {
        // ... (implementation remains the same)
    }

    private ItemStack createGuiItem(Material material, String name) {
        ItemStack item = new ItemStack(material, 1);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            item.setItemMeta(meta);
        }
        return item;  // Add this return statement
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        // ... (implement inventory click handling)
    }

    public void removeSignPort(String name) {
        signPorts.remove(name);
        updateMenu();
    }

    public SignPortSetup getSignPortByLocation(Location location) {
        for (SignPortSetup setup : signPorts.values()) {
            if (setup.getSignLocation().equals(location)) {  // Change getLocation() to getSignLocation()
                return setup;
            }
        }
        return null;
    }
}