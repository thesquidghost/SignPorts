package net.farhaven.SignPorts;

import me.partlysunny.sunbeam.menu.Menu;
import me.partlysunny.sunbeam.menu.MenuProvider;
import me.partlysunny.sunbeam.menu.Menus;
import me.partlysunny.sunbeam.menu.item.ButtonItem;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.ConversationFactory;
import org.bukkit.conversations.Prompt;
import org.bukkit.conversations.StringPrompt;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class EditSignUI implements MenuProvider {

    public static Map<UUID, Location> editing = new HashMap<>();

    private final SignPorts plugin;

    public EditSignUI(SignPorts plugin) {
        this.plugin = plugin;
    }
    private boolean canEditSign(Player player, Location location) {
        return SignPorts.griefDefenderHook.canPlayerBreakBlock(player, location.getBlock());
    }

    @Override
    public Menu apply(Player player) {
        Menu menu = new Menu("Edit SignPort", 3);
        SignPortSetup setup = plugin.getSignPortMenu().getSignPortByLocation(editing.get(player.getUniqueId()));

        if (setup == null || !canEditSign(player, editing.get(player.getUniqueId()))) {
            menu.fillBackground(Material.BARRIER);
            return menu;
        }

        menu.fillBackground(Material.BLACK_STAINED_GLASS_PANE);
        ButtonItem rename = new ButtonItem(event -> {
            new ConversationFactory(plugin)
                    .withFirstPrompt(new StringPrompt() {
                        @Override
                        public @NotNull String getPromptText(@NotNull ConversationContext conversationContext) {
                            return "Type the new name for the SignPort.";
                        }

                        @Override
                        public @Nullable Prompt acceptInput(@NotNull ConversationContext conversationContext, @Nullable String s) {
                            if (s != null) {
                                plugin.updateSignPortName(player, s);
                            }
                            return null;
                        }
                    })
                    .withLocalEcho(false)
                    .buildConversation(player)
                    .begin();
            Menus.close(player);
        }, Material.BOOK, "Rename SignPort");
        ButtonItem redesc = new ButtonItem(event -> {
            new ConversationFactory(plugin)
                    .withFirstPrompt(new StringPrompt() {
                        @Override
                        public @NotNull String getPromptText(@NotNull ConversationContext conversationContext) {
                            return "Type the new description for the SignPort.";
                        }

                        @Override
                        public @Nullable Prompt acceptInput(@NotNull ConversationContext conversationContext, @Nullable String s) {
                            if (s != null) {
                                plugin.updateSignPortDescription(player, s);
                            }
                            return null;
                        }
                    })
                    .withLocalEcho(false)
                    .buildConversation(player)
                    .begin();
            Menus.close(player);
        }, Material.ACACIA_SIGN, "Change SignPort Description");
        ButtonItem locked = new ButtonItem(event -> {
            plugin.updateSignPortLocked(player, !setup.isLocked());
            Menus.close(player);
        }, Material.CHAIN, "Toggle Locked SignPort", ChatColor.GRAY + "Currently: " + (setup.isLocked() ? "Locked" : "Unlocked"));
        ButtonItem delete = new ButtonItem(event -> {
            plugin.getSignPortMenu().removeSignPort(setup.getName());
            Menus.close(player);
        }, Material.BARRIER, "Delete SignPort");
        menu.setItem(10, rename);
        menu.setItem(12, redesc);
        menu.setItem(14, locked);
        menu.setItem(16, delete);
        return menu;
    }
}
