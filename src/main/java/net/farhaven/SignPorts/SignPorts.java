package net.farhaven.SignPorts;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.TabCompleter;
import org.bukkit.command.CommandExecutor;

import java.util.Objects;

public class SignPorts extends JavaPlugin {
    private SignPortMenu signPortMenu;
    private SignPortSetupManager signPortSetupManager;

    @Override
    public void onEnable() {
        if (!checkPluginAvailability("GriefDefender", "GriefDefender not found! Disabling SignPorts.")) return;
        if (!checkPluginAvailability("PlaceholderAPI", "Could not find PlaceholderAPI! This plugin is required."))
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
            return false;
        }
        return true;
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
        registerCommandWithTabCompleter("signport", new SignPortTabCompleter(this));
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

    private void registerCommandWithTabCompleter(String commandName, TabCompleter tabCompleter) {
        PluginCommand command = getCommand(commandName);
        if (command == null) {
            getLogger().warning("Failed to register tab completer for command: " + commandName + " (command not found in plugin.yml)");
            return;
        }
        command.setTabCompleter(tabCompleter);
    }

    private void loadSignPorts() {
        ConfigurationSection signports = getConfig().getConfigurationSection("signports");
        if (signports != null) {
            for (String key : signports.getKeys(false)) {
                ConfigurationSection signportSection = signports.getConfigurationSection(key);
                if (signportSection != null) {
                    SignPortSetup setup = SignPortSetup.fromConfig(signportSection);
                    if (setup != null) {
                        signPortMenu.addSignPort(setup);
                    }
                }
            }
        }
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
        String name = setup.getName();
        ConfigurationSection signportSection = getConfig().createSection("signports." + name);
        setup.saveToConfig(signportSection);
        saveConfig();
        signPortMenu.addSignPort(setup);
    }

    public boolean hasSignPort(Player player) {
        for (SignPortSetup setup : signPortMenu.getSignPorts().values()) {
            if (setup.getOwnerUUID().equals(player.getUniqueId())) {
                return true;
            }
        }
        return false;
    }
}