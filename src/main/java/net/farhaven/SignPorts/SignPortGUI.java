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

import com.griefdefender.api.GriefDefender;
import com.griefdefender.api.claim.Claim;
import com.griefdefender.api.claim.ClaimManager;
import com.griefdefender.lib.flowpowered.math.vector.Vector3i;

import java.util.ArrayList;
import java.util.List;

public class SignPortGUI implements Listener {
    private final SignPorts plugin;
    private static final int PAGE_SIZE = 28; // 4 rows of 7 items

    public SignPortGUI(SignPorts plugin) {
        this.plugin = plugin;
    }

    public void openSignPortMenu(Player player, int page) {
        List<SignPortSetup> signPorts = new ArrayList<>(plugin.getSignPortMenu().getSignPorts().values());
        int totalPages = (int) Math.ceil((double) signPorts.size() / PAGE_SIZE);
        page = Math.max(1, Math.min(page, totalPages));

        Inventory menu = Bukkit.createInventory(null, 54, PlaceholderAPI.setPlaceholders(player, plugin.getConfig().getString("menu-name", "SignPorts Menu")));

        // Add navigation and functional items
        menu.setItem(0, createGuiItem(Material.REDSTONE, ChatColor.RED + "Close Menu"));
        menu.setItem(8, createGuiItem(Material.BEACON, ChatColor.AQUA + "Edit Your SignPort"));
        menu.setItem(45, createGuiItem(Material.ARROW, ChatColor.YELLOW + "Previous Page"));
        menu.setItem(53, createGuiItem(Material.ARROW, ChatColor.YELLOW + "Next Page"));

        // Fill empty slots with black glass panes
        for (int i = 0; i < 54; i++) {
            if (menu.getItem(i) == null) {
                menu.setItem(i, createGuiItem(Material.BLACK_STAINED_GLASS_PANE, " "));
            }
        }

        // Populate the inventory with SignPort items
        int startIndex = (page - 1) * PAGE_SIZE;
        for (int i = 0; i < PAGE_SIZE && startIndex + i < signPorts.size(); i++) {
            SignPortSetup setup = signPorts.get(startIndex + i);
            ItemStack item = setup.getGuiItem().clone();
            item.setAmount(1); // Ensure only one item is displayed
            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                meta.setDisplayName(ChatColor.GREEN + setup.getName());
                List<String> lore = new ArrayList<>();
                lore.add(ChatColor.BOLD + "Owner: " + setup.getOwnerName());
                lore.add("Claim: " + getClaimName(setup.getSignLocation()));
                lore.add(ChatColor.ITALIC + setup.getDescription());
                meta.setLore(lore);
                item.setItemMeta(meta);
            }
            menu.setItem(18 + i + (i / 7) * 2, item);
        }

        // Update navigation item lore with page info
        updateNavigationItem(menu, 45, page > 1, page - 1);
        updateNavigationItem(menu, 53, page < totalPages, page + 1);

        player.openInventory(menu);
    }

    private ItemStack createGuiItem(Material material, String name) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            item.setItemMeta(meta);
        }
        return item;
    }

    private void updateNavigationItem(Inventory inventory, int slot, boolean enabled, int targetPage) {
        ItemStack item = inventory.getItem(slot);
        if (item != null) {
            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                List<String> lore = new ArrayList<>();
                if (enabled) {
                    lore.add(ChatColor.GRAY + "Click to go to page " + targetPage);
                } else {
                    lore.add(ChatColor.GRAY + "No more pages");
                }
                meta.setLore(lore);
                item.setItemMeta(meta);
            }
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        plugin.getLogger().info("Inventory click event triggered");
        if (!event.getView().getTitle().equals(plugin.getConfig().getString("menu-name", "SignPorts Menu"))) {
            plugin.getLogger().info("Not a SignPorts menu");
            return;
        }
        event.setCancelled(true);
        plugin.getLogger().info("SignPorts menu click detected");
        Player player = (Player) event.getWhoClicked();
        ItemStack clickedItem = event.getCurrentItem();

        if (clickedItem == null || clickedItem.getType() == Material.AIR) {
            plugin.getLogger().info("Clicked on empty slot");
            return;
        }

        int slot = event.getRawSlot();
        plugin.getLogger().info("Clicked on slot: " + slot);

        if (slot == 0) { // Close menu
            plugin.getLogger().info("Closing menu");
            player.closeInventory();
        } else if (slot == 8) { // Edit SignPort
            plugin.getLogger().info("Edit SignPort clicked");
            handleEditSignPort(player);
        } else if (slot == 45 || slot == 53) { // Previous or Next page
            plugin.getLogger().info("Page navigation clicked");
            handlePageNavigation(player, clickedItem);
        } else if (slot >= 18 && slot <= 44) {
            plugin.getLogger().info("SignPort item clicked");
            handleSignPortClick(player, clickedItem);
        } else {
            plugin.getLogger().info("Clicked on non-functional slot: " + slot);
        }
    }

    private void handlePageNavigation(Player player, ItemStack clickedItem) {
        ItemMeta meta = clickedItem.getItemMeta();
        if (meta != null && meta.hasLore()) {
            List<String> lore = meta.getLore();
            if (lore != null && !lore.isEmpty()) {
                String firstLore = lore.getFirst();
                if (firstLore.contains("Click to go to page")) {
                    int currentPage = Integer.parseInt(firstLore.split(" ")[5]);
                    openSignPortMenu(player, currentPage);
                }
            }
        }
    }

    private void handleEditSignPort(Player player) {
        // Implement edit functionality
        player.closeInventory();
        player.sendMessage(ChatColor.YELLOW + "To edit a SignPort, use the following commands:");
        player.sendMessage(ChatColor.YELLOW + "/signport setname <name> - Change the name of your SignPort");
        player.sendMessage(ChatColor.YELLOW + "/signport setdesc <description> - Change the description of your SignPort");
        player.sendMessage(ChatColor.YELLOW + "/signport setitem - Change the item representing your SignPort");
    }

    private void handleSignPortClick(Player player, ItemStack clickedItem) {
        plugin.getLogger().info("Handling SignPort click for player " + player.getName());

        if (clickedItem == null || clickedItem.getType() == Material.BLACK_STAINED_GLASS_PANE) {
            plugin.getLogger().info("Clicked item is null or glass pane, ignoring");
            return;
        }

        ItemMeta meta = clickedItem.getItemMeta();
        if (meta == null) {
            plugin.getLogger().info("Clicked item has no metadata, ignoring");
            return;
        }

        String signPortName = ChatColor.stripColor(meta.getDisplayName());
        plugin.getLogger().info("SignPort name from clicked item: '" + signPortName + "'");

        if (signPortName.isEmpty()) {
            plugin.getLogger().info("SignPort name is empty, ignoring");
            return;
        }

        plugin.getLogger().info("Player " + player.getName() + " clicked on SignPort: " + signPortName);
        SignPortSetup setup = plugin.getSignPortMenu().getSignPortByName(signPortName);
        if (setup != null) {
            Location destination = setup.getSignLocation();
            plugin.getLogger().info("Destination location: " + destination);
            if (plugin.isSafeLocation(destination)) {
                plugin.getLogger().info("Location is safe, attempting teleport");
                player.teleport(destination);
                player.sendMessage(ChatColor.GREEN + "You've been teleported to " + setup.getName());
                plugin.getLogger().info("Player " + player.getName() + " teleported to " + signPortName);
            } else {
                player.sendMessage(ChatColor.RED + "The destination is not safe. Teleportation cancelled.");
                plugin.getLogger().info("Teleportation cancelled for " + player.getName() + " to " + signPortName + ". Unsafe location.");
            }
        } else {
            player.sendMessage(ChatColor.RED + "That SignPort no longer exists.");
            plugin.getLogger().info("SignPort not found: " + signPortName);
        }
    }

    private String getClaimName(Location location) {
        if (location == null || location.getWorld() == null) {
            return "Unknown";
        }

        ClaimManager claimManager = GriefDefender.getCore().getClaimManager(location.getWorld().getUID());
        if (claimManager == null) {
            return "Wilderness";
        }

        Vector3i vector3i = Vector3i.from(location.getBlockX(), location.getBlockY(), location.getBlockZ());
        Claim claim = claimManager.getClaimAt(vector3i);

        if (claim != null) {
            String ownerName = claim.getOwnerName();
            return ownerName != null ? ownerName + "'s Claim" : "Claim-" + claim.getUniqueId().toString().substring(0, 8);
        }
        return "Wilderness";
    }
}