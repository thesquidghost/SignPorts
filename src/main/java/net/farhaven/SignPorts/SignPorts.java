package net.farhaven.SignPorts;

import me.partlysunny.sunbeam.Sunbeam;
import me.partlysunny.sunbeam.menu.Menus;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;

public class SignPorts extends JavaPlugin {
    private SignPortMenu signPortMenu;
    private SignPortSetupManager signPortSetupManager;
    private SignPortStorage signPortStorage;
    private final Map<UUID, Long> teleportCooldowns = new HashMap<>();

    public static GriefDefenderHook griefDefenderHook;

    @Override
    public void onEnable() {
        getLogger().info("SignPorts is enabling...");

        if (checkPluginAvailability("PlaceholderAPI", "Could not find PlaceholderAPI! This plugin is required.")) return;

        // Initialize TeamClaim hook
        Plugin teamClaimPlugin = getServer().getPluginManager().getPlugin("TeamClaim");
        if (teamClaimPlugin != null && teamClaimPlugin instanceof dev.airfrom.teamclaim.Main) {
            getLogger().info("TeamClaim found! Hooking into TeamClaim.");
            griefDefenderHook = new TeamClaimHook((dev.airfrom.teamclaim.Main) teamClaimPlugin);
        } else {
            getLogger().warning("TeamClaim not found! SignPorts will not be able to check for claims.");
            griefDefenderHook = new NoGDHook();
        }

        Sunbeam.init(this);
        Menus.registerMenu("editsignport", new EditSignUI(this));

        saveDefaultConfig();
        getLogger().info("Configuration loaded.");

        try {
            initializeManagers();
            getLogger().info("Managers initialized.");
            registerEventsAndCommands();
            getLogger().info("Events and commands registered.");

            // Register the server start listener
            getServer().getPluginManager().registerEvents(new ServerStartListener(this), this);
            getLogger().info("Server start listener registered.");
        } catch (Exception e) {
            getLogger().log(Level.SEVERE, "An error occurred during plugin initialization. Disabling SignPorts.", e);
            getServer().getPluginManager().disablePlugin(this);
        }
    }

    public void reloadPluginConfig() {
        reloadConfig();
        teleportCooldowns.clear();
        signPortMenu.reloadSignPorts();
        signPortStorage.loadSignPorts();
        getLogger().info("SignPorts configuration reloaded.");
    }

    @SuppressWarnings("unused")
    public int getTeleportDelay() {
        return getConfig().getInt("teleport-delay", 5); // Default to 5 seconds if not set
    }

    private boolean checkPluginAvailability(String pluginName, String warningMessage) {
        if (getServer().getPluginManager().getPlugin(pluginName) == null) {
            getLogger().severe(warningMessage);
            getServer().getPluginManager().disablePlugin(this);
            return true;
        }
        return false;
    }

    private void initializeManagers() {
        this.signPortStorage = new SignPortStorage(this);
        this.signPortMenu = new SignPortMenu(this);
        this.signPortSetupManager = new SignPortSetupManager(this);
    }

    private void registerEventsAndCommands() {
        SignPortListener signListener = new SignPortListener(this);
        SignPortGUI signPortGUI = new SignPortGUI(this);
        SignPortSetupCommands setupCommands = new SignPortSetupCommands(this);

        getServer().getPluginManager().registerEvents(signListener, this);
        getServer().getPluginManager().registerEvents(signPortGUI, this);
        getServer().getPluginManager().registerEvents(signPortMenu, this);

        registerCommand("signport", new SignPortCommand(this));
        registerCommandWithTabCompleter(new SignPortTabCompleter(this));
        registerCommand("signportmenu", this);
        registerCommand("confirm", setupCommands);
        registerCommand("setname", setupCommands);
        registerCommand("setdesc", setupCommands);
    }

    private void registerCommand(String commandName, CommandExecutor executor) {
        PluginCommand command = getCommand(commandName);
        if (command == null) {
            getLogger().warning("Failed to register command: " + commandName + " (not found in plugin.yml)");
            return;
        }
        command.setExecutor(executor);
    }

    private void registerCommandWithTabCompleter(TabCompleter tabCompleter) {
        PluginCommand command = getCommand("signport");
        if (command == null) {
            getLogger().warning("Failed to register tab completer for command: signport (command not found in plugin.yml)");
            return;
        }
        command.setTabCompleter(tabCompleter);
    }

    public void loadSignPorts() {
        signPortStorage.loadSignPorts();
        signPortMenu.setSignPorts(signPortStorage.getSignPorts());
        getLogger().info("SignPorts loaded successfully after delay. Total: " + signPortMenu.getSignPorts().size());
    }

    @Override
    public void onDisable() {
        try {
            if (signPortStorage != null) {
                signPortStorage.saveSignPorts();
                getLogger().info("SignPorts data saved successfully.");
            } else {
                getLogger().warning("SignPortStorage was null during shutdown. SignPorts may not have been saved.");
            }
        } catch (Exception e) {
            getLogger().log(Level.SEVERE, "An error occurred while saving SignPorts", e);
        }
        getLogger().info("SignPorts is shutting down. Goodbye!");
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, Command command, @NotNull String label, String[] args) {
        if (command.getName().equalsIgnoreCase("signportmenu")) {
            if (sender instanceof Player player) {
                signPortMenu.openSignPortMenu(player);
                return true;
            } else {
                sender.sendMessage("This command can only be used by players.");
                return false;
            }
        }
        return false;
    }

    public SignPortMenu getSignPortMenu() {
        return signPortMenu;
    }

    public SignPortStorage getSignPortStorage() {
        return signPortStorage;
    }

    public SignPortSetupManager getSignPortSetupManager() {
        return signPortSetupManager;
    }

    public boolean playerHasSignPort(Player player) {
        return getSignPortMenu().getSignPortByOwner(player.getUniqueId()) != null;
    }

    public void saveSignPort(SignPortSetup setup) {
        getSignPortMenu().addSignPort(setup);
        getSignPortStorage().addSignPort(setup);
        getLogger().info("SignPort saved and added to menu: '" + setup.getName() + "'");
    }

    public boolean isSafeLocation(Location location) {
        World world = location.getWorld();
        if (world == null) {
            getLogger().info("World is null for location: " + location);
            return false;
        }

        int x = location.getBlockX();
        int y = location.getBlockY();
        int z = location.getBlockZ();

        Block feet = world.getBlockAt(x, y, z);
        Block head = world.getBlockAt(x, y + 1, z);
        Block ground = world.getBlockAt(x, y - 1, z);

        getLogger().info("Checking safety for location: " + location);
        getLogger().info("Feet block: " + feet.getType());
        getLogger().info("Head block: " + head.getType());
        getLogger().info("Ground block: " + ground.getType());

        boolean isSafe = (feet.getType().isAir() || feet.getType().name().contains("SIGN")) && head.getType().isAir();
        getLogger().info("Is there space for the player? " + isSafe);

        isSafe = isSafe && (ground.getType().isSolid() || ground.getType().toString().contains("SLAB")
                || ground.getType().toString().contains("STAIRS") || ground.getType().toString().contains("CARPET")
                || ground.getType().toString().contains("BED") || ground.getType().name().contains("LEAVES"));
        getLogger().info("Is there something to stand on? " + isSafe);

        if (!isSafe) {
            getLogger().info("Checking one block lower");
            feet = world.getBlockAt(x, y - 1, z);
            head = world.getBlockAt(x, y, z);
            ground = world.getBlockAt(x, y - 2, z);

            getLogger().info("Feet block (lower): " + feet.getType());
            getLogger().info("Head block (lower): " + head.getType());
            getLogger().info("Ground block (lower): " + ground.getType());

            isSafe = (feet.getType().isAir() || feet.getType().name().contains("SIGN")) && head.getType().isAir();
            getLogger().info("Is there space for the player (lower)? " + isSafe);

            isSafe = isSafe && (ground.getType().isSolid() || ground.getType().toString().contains("SLAB")
                    || ground.getType().toString().contains("STAIRS") || ground.getType().toString().contains("CARPET")
                    || ground.getType().toString().contains("BED") || ground.getType().name().contains("LEAVES"));
            getLogger().info("Is there something to stand on (lower)? " + isSafe);
        }

        getLogger().info("Final safety result: " + isSafe);
        return isSafe;
    }

    public boolean checkCooldown(Player player) {
        int cooldownSeconds = getConfig().getInt("teleport-cooldown", 30);
        if (teleportCooldowns.containsKey(player.getUniqueId())) {
            long secondsLeft = ((teleportCooldowns.get(player.getUniqueId()) / 1000) + cooldownSeconds) - (System.currentTimeMillis() / 1000);
            if (secondsLeft > 0) {
                player.sendMessage(ChatColor.RED + "You must wait " + secondsLeft + " seconds before teleporting again.");
                return false;
            }
        }
        teleportCooldowns.put(player.getUniqueId(), System.currentTimeMillis());
        return true;
    }

    public void updateSignPortName(Player player, String newName) {
        SignPortSetup setup = signPortStorage.getSignPort(player.getUniqueId());
        if (setup != null) {
            if (signPortMenu.getSignPortByName(newName) != null) {
                player.sendMessage(ChatColor.RED + "A SignPort with that name already exists.");
                return;
            }
            String oldName = setup.getName();
            setup.setName(newName);
            signPortMenu.removeSignPort(oldName);
            signPortMenu.addSignPort(setup);
            signPortStorage.addSignPort(setup);
            player.sendMessage(ChatColor.GREEN + "SignPort name updated to: " + newName);
        } else {
            player.sendMessage(ChatColor.RED + "You don't have a SignPort to edit.");
        }
    }

    public void updateSignPortLocked(Player player, boolean locked) {
        SignPortSetup setup = signPortStorage.getSignPort(player.getUniqueId());
        if (setup != null) {
            setup.setLocked(locked);
            signPortStorage.saveSignPorts();
            player.sendMessage(ChatColor.GREEN + "SignPort locked status updated.");
        } else {
            player.sendMessage(ChatColor.RED + "You don't have a SignPort to edit.");
        }
    }

    public void updateSignPortDescription(Player player, String newDescription) {
        SignPortSetup setup = signPortStorage.getSignPort(player.getUniqueId());
        if (setup != null) {
            setup.setDescription(newDescription);
            signPortStorage.saveSignPorts();
            player.sendMessage(ChatColor.GREEN + "SignPort description updated.");
        } else {
            player.sendMessage(ChatColor.RED + "You don't have a SignPort to edit.");
        }
    }

    public void updateSignPortItem(Player player, ItemStack newItem) {
        SignPortSetup setup = signPortStorage.getSignPort(player.getUniqueId());
        if (setup != null) {
            setup.setGuiItem(newItem);
            signPortStorage.saveSignPorts();
            player.sendMessage(ChatColor.GREEN + "SignPort item updated to: " + newItem.getType());
        } else {
            player.sendMessage(ChatColor.RED + "You don't have a SignPort to edit.");
        }
    }
}