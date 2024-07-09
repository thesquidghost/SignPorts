package net.farhaven.SignPorts;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.command.*;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SignPorts extends JavaPlugin {
    private SignPortMenu signPortMenu;
    private SignPortSetupManager signPortSetupManager;
    private final Map<UUID, Boolean> playerHasSignPort = new HashMap<>();

    @Override
    public void onEnable() {
        if (checkPluginAvailability("GriefDefender", "GriefDefender not found! Disabling SignPorts.")) return;
        if (checkPluginAvailability("PlaceholderAPI", "Could not find PlaceholderAPI! This plugin is required."))
            return;

        saveDefaultConfig();
        getLogger().info("SignPorts is working hard!");

        initializeManagers();
        registerEventsAndCommands();
        loadSignPorts();
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

    private void loadSignPorts() {
        Bukkit.getScheduler().runTaskAsynchronously(this, () -> {
            ConfigurationSection signports = getConfig().getConfigurationSection("signports");
            if (signports != null) {
                for (String key : signports.getKeys(false)) {
                    ConfigurationSection signportSection = signports.getConfigurationSection(key);
                    if (signportSection != null) {
                        SignPortSetup setup = SignPortSetup.fromConfig(signportSection);

                        // Run UI updates on the main thread
                        Bukkit.getScheduler().runTask(this, () -> {
                            signPortMenu.addSignPort(setup);
                            setPlayerHasReachedSignPortLimit(setup.getOwnerUUID(), true);
                        });
                    }
                }
            }
        });
    }

    @Override
    public void onDisable() {
        getLogger().info("SignPorts is hardly (not) working!");
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

    public SignPortSetupManager getSignPortSetupManager() {
        return signPortSetupManager;
    }

    public void saveSignPort(SignPortSetup setup) {
        Bukkit.getScheduler().runTaskAsynchronously(this, () -> {
            String name = setup.getName();
            ConfigurationSection signportSection = getConfig().createSection("signports." + name);
            setup.saveToConfig(signportSection);
            saveConfig();

            // Run any UI updates on the main thread
            Bukkit.getScheduler().runTask(this, () -> {
                signPortMenu.addSignPort(setup);
                setPlayerHasReachedSignPortLimit(setup.getOwnerUUID(), true);
            });
        });
    }

    public boolean playerHasReachedSignPortLimit(Player player) {
        return playerHasSignPort.getOrDefault(player.getUniqueId(), false);
    }

    public void setPlayerHasReachedSignPortLimit(UUID playerUUID, boolean hasReachedLimit) {
        playerHasSignPort.put(playerUUID, hasReachedLimit);
    }

    public boolean isSafeLocation(Location location) {
        World world = location.getWorld();
        if (world == null) return false;

        int x = location.getBlockX();
        int y = location.getBlockY();
        int z = location.getBlockZ();

        Block feet = world.getBlockAt(x, y, z);
        Block head = world.getBlockAt(x, y + 1, z);
        Block ground = world.getBlockAt(x, y - 1, z);

        return !feet.getType().isSolid() && !head.getType().isSolid() && ground.getType().isSolid();
    }
}