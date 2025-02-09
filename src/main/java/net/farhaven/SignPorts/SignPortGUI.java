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
import java.util.Random;

public class SignPortGUI implements Listener {
    private final SignPorts plugin;
    private static final int PAGE_SIZE = 28; // 4 rows of 7 items

    public SignPortGUI(SignPorts plugin) {
        this.plugin = plugin;
    }

    public void openSignPortMenu(Player player, int page) {
        // Convert our signports map to a list.
        List<SignPortSetup> signPorts = new ArrayList<>(plugin.getSignPortMenu().getSignPorts().values());
        int totalPages = (int) Math.ceil((double) signPorts.size() / PAGE_SIZE);
        page = Math.max(1, Math.min(page, totalPages > 0 ? totalPages : 1));

        String menuTitle = getMenuTitle(player);
        Inventory menu = Bukkit.createInventory(null, 54, menuTitle);

        // Set navigation and functional items.
        menu.setItem(0, createGuiItem(Material.REDSTONE, ChatColor.RED + "Close Menu"));
        menu.setItem(8, createGuiItem(Material.BEACON, ChatColor.AQUA + "Edit Your SignPort"));
        menu.setItem(45, createGuiItem(Material.ARROW, ChatColor.YELLOW + "Previous Page"));
        menu.setItem(53, createGuiItem(Material.ARROW, ChatColor.YELLOW + "Next Page"));

        // Fill empty slots with black glass panes.
        for (int i = 0; i < 54; i++) {
            if (menu.getItem(i) == null) {
                menu.setItem(i, createGuiItem(Material.BLACK_STAINED_GLASS_PANE, " "));
            }
        }

        // Populate the inventory with SignPort items.
        int startIndex = (page - 1) * PAGE_SIZE;
        for (int i = 0; i < PAGE_SIZE && startIndex + i < signPorts.size(); i++) {
            SignPortSetup setup = signPorts.get(startIndex + i);
            ItemStack item = setup.getGuiItem().clone();
            item.setAmount(1); // Ensure only one item is displayed.
            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                // Set a random color for display name.
                meta.setDisplayName(getRandomColor() + setup.getName());

                List<String> lore = new ArrayList<>();
                lore.add(ChatColor.ITALIC + "" + ChatColor.GOLD + "Owner: " + setup.getOwnerName());
                lore.add(ChatColor.ITALIC + "" + ChatColor.AQUA + setup.getDescription());
                // Add extra line indicating the lock status.
                if (setup.isLocked()) {
                    lore.add(ChatColor.RED + "Locked");
                } else {
                    lore.add(ChatColor.GREEN + "Unlocked");
                }
                meta.setLore(lore);
                item.setItemMeta(meta);
            }
            // Calculate slot (4 rows of 7 items starting from slot 18 with 2 gap rows).
            int slot = 18 + i + (i / 7) * 2;
            menu.setItem(slot, item);
        }

        // Update navigation items with page info.
        updateNavigationItem(menu, 45, page > 1, page - 1);
        updateNavigationItem(menu, 53, page < totalPages, page + 1);

        player.openInventory(menu);
    }

    private String getMenuTitle(Player player) {
        String rawTitle = plugin.getConfig().getString("menu-name", "SignPorts Menu");
        return PlaceholderAPI.setPlaceholders(player, rawTitle);
    }

    private ChatColor getRandomColor() {
        ChatColor[] colors = {ChatColor.RED, ChatColor.GREEN, ChatColor.BLUE, ChatColor.YELLOW, ChatColor.LIGHT_PURPLE, ChatColor.AQUA};
        return colors[new Random().nextInt(colors.length)];
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
        // Ensure the clicker is a player.
        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }
        Player player = (Player) event.getWhoClicked();
        String menuTitle = getMenuTitle(player);

        // Check if the clicked inventory is the SignPorts menu.
        if (!event.getView().getTitle().equals(menuTitle)) {
            return;
        }

        event.setCancelled(true);
        ItemStack clickedItem = event.getCurrentItem();

        if (clickedItem == null || clickedItem.getType() == Material.AIR) {
            return;
        }

        int slot = event.getRawSlot();

        if (slot == 0) { // Close menu.
            player.closeInventory();
        } else if (slot == 8) { // Edit SignPort.
            handleEditSignPort(player);
        } else if (slot == 45 || slot == 53) { // Previous or Next page.
            handlePageNavigation(player, clickedItem);
        } else if (slot >= 18 && slot <= 44) {
            handleSignPortClick(player, clickedItem);
        }
    }

    private void handlePageNavigation(Player player, ItemStack clickedItem) {
        ItemMeta meta = clickedItem.getItemMeta();
        if (meta != null && meta.hasLore()) {
            List<String> lore = meta.getLore();
            if (lore != null && !lore.isEmpty()) {
                String firstLore = lore.get(0);
                if (firstLore.contains("Click to go to page")) {
                    try {
                        // Expecting format: "Click to go to page X"
                        String[] parts = firstLore.split(" ");
                        if (parts.length >= 6) {
                            int targetPage = Integer.parseInt(parts[5]);
                            openSignPortMenu(player, targetPage);
                        }
                    } catch (NumberFormatException ignored) {
                        player.sendMessage(ChatColor.RED + "Invalid page number in menu navigation.");
                    }
                }
            }
        }
    }

    private void handleEditSignPort(Player player) {
        // Implement edit functionality.
        player.closeInventory();
        player.sendMessage(ChatColor.YELLOW + "To edit your SignPort, use the following commands:");
        player.sendMessage(ChatColor.YELLOW + "/signport setname <name> - Change the name of your SignPort");
        player.sendMessage(ChatColor.YELLOW + "/signport setdesc <description> - Change the description of your SignPort");
        player.sendMessage(ChatColor.YELLOW + "/signport setitem - Change the item representing your SignPort");
    }

    private void handleSignPortClick(Player player, ItemStack clickedItem) {
        plugin.getLogger().info("Handling SignPort click for player " + player.getName());
        if (clickedItem.getType() == Material.BLACK_STAINED_GLASS_PANE) {
            plugin.getLogger().info("Clicked item is a glass pane, ignoring.");
            return;
        }

        ItemMeta meta = clickedItem.getItemMeta();
        if (meta == null) {
            plugin.getLogger().info("Clicked item has no metadata, ignoring.");
            return;
        }

        String signPortName = ChatColor.stripColor(meta.getDisplayName());
        plugin.getLogger().info("SignPort name from clicked item: '" + signPortName + "'");

        if (signPortName.isEmpty()) {
            plugin.getLogger().info("SignPort name is empty, ignoring.");
            return;
        }

        plugin.getLogger().info("Player " + player.getName() + " clicked on SignPort: " + signPortName);
        plugin.getSignPortMenu().handleSignPortClick(player, signPortName);
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