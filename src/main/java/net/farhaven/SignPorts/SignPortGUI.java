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

import java.util.ArrayList;
import java.util.List;

public class SignPortGUI implements Listener
{
    private final SignPorts plugin;

    public SignPortGUI (SignPorts plugin)
    {
        this.plugin = plugin;
    }

    public void openSignPortMenu (Player player)
    {
        Inventory signPortMenu =
                Bukkit.createInventory (null, 54,
                        PlaceholderAPI.setPlaceholders (player,
                                "Select a SignPort for %player_name%"));

        // Populate the inventory with SignPort items
        for (String signPortName:plugin.getConfig ().getConfigurationSection ("signports").
                getKeys (false))
        {
            ItemStack item = new ItemStack (Material.OAK_SIGN);
            ItemMeta meta = item.getItemMeta ();
            meta.setDisplayName (ChatColor.GREEN + signPortName);

            Location loc =
                    plugin.getConfig ().getLocation ("signports." + signPortName);
            List < String > lore = new ArrayList <> ();
            lore.add (ChatColor.GRAY + "World: " + loc.getWorld ().getName ());
            lore.add (ChatColor.GRAY + "X: " + loc.getBlockX ());
            lore.add (ChatColor.GRAY + "Y: " + loc.getBlockY ());
            lore.add (ChatColor.GRAY + "Z: " + loc.getBlockZ ());
            meta.setLore (lore);

            item.setItemMeta (meta);
            signPortMenu.addItem (item);
        }

        // Add a custom item
        ItemStack customItem = new ItemStack (Material.DIAMOND);
        ItemMeta customMeta = customItem.getItemMeta ();
        if (customMeta != null)
        {
            customMeta.setDisplayName (PlaceholderAPI.
                    setPlaceholders (player,
                            "%player_name%'s Diamond"));
            customItem.setItemMeta (customMeta);
        }
        signPortMenu.setItem (53, customItem);

        // Apply PlaceholderAPI to all items in the inventory
        for (ItemStack item:signPortMenu.getContents ())
        {
            if (item != null && item.hasItemMeta ())
            {
                ItemMeta meta = item.getItemMeta ();
                String displayName = meta.getDisplayName ();
                if (displayName != null && !displayName.isEmpty ())
                {
                    displayName =
                            PlaceholderAPI.setPlaceholders (player, displayName);
                    meta.setDisplayName (displayName);
                }
                if (meta.hasLore ())
                {
                    List < String > lore = meta.getLore ();
                    List < String > newLore = new ArrayList <> ();
                    for (String loreLine:lore)
                    {
                        newLore.add (PlaceholderAPI.
                                setPlaceholders (player, loreLine));
                    }
                    meta.setLore (newLore);
                }
                item.setItemMeta (meta);
            }
        }

        player.openInventory (signPortMenu);
    }

    @EventHandler public void onInventoryClick (InventoryClickEvent event)
    {
        if (event.getView ().getTitle ().contains ("Select a SignPort"))
        {
            event.setCancelled (true);
            Player player = (Player) event.getWhoClicked ();
            ItemStack clickedItem = event.getCurrentItem ();

            if (clickedItem != null)
            {
                if (clickedItem.getType () == Material.OAK_SIGN)
                {
                    String signPortName =
                            ChatColor.stripColor (clickedItem.getItemMeta ().
                                    getDisplayName ());

                    if (!player.hasPermission ("signports.use"))
                    {
                        player.sendMessage (ChatColor.RED +
                                "You don't have permission to use SignPorts.");
                        player.closeInventory ();
                        return;
                    }

                    Location signPortLocation =
                            plugin.getConfig ().getLocation ("signports." +
                                    signPortName);
                    if (signPortLocation != null)
                    {
                        player.teleport (signPortLocation);
                        String message =
                                plugin.getConfig ().
                                        getString ("messages.teleport-success",
                                                "You've been teleported to %signport%!");
                        message =
                                PlaceholderAPI.setPlaceholders (player,
                                        message.
                                                replace ("%signport%",
                                                        signPortName));
                        player.sendMessage (ChatColor.GREEN + message);
                        player.closeInventory ();
                    }
                    else
                    {
                        player.sendMessage (ChatColor.RED + "SignPort " +
                                signPortName + " does not exist.");
                        player.closeInventory ();
                    }
                }
                else if (clickedItem.getType () == Material.DIAMOND)
                {
                    // Handle custom item click
                    player.sendMessage (ChatColor.AQUA +
                            "You clicked on your special diamond!");
                    player.closeInventory ();
                }
            }
        }
    }
}
