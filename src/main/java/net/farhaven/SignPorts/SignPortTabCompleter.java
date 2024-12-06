package net.farhaven.SignPorts;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class SignPortTabCompleter implements TabCompleter
{
    private final SignPorts plugin;

    public SignPortTabCompleter (SignPorts plugin)
    {
        this.plugin = plugin;
    }

    @Override
    public List < String > onTabComplete (@NotNull CommandSender sender,
                                          Command command, @NotNull String alias,
                                          String[]args)
    {
        if (command.getName ().equalsIgnoreCase ("signport"))
        {
            if (args.length == 1)
            {
                List < String > subcommands =
                        Arrays.asList ("create", "list", "remove", "teleport", "gui");
                return subcommands.stream ().filter (sc->sc.
                                startsWith (args[0].
                                        toLowerCase ())).
                        collect (Collectors.toList ());
            }
            else if (args.length == 2)
            {
                ConfigurationSection signportsSection =
                        plugin.getConfig ().getConfigurationSection ("signports");
                if (signportsSection != null)
                {
                    return new ArrayList <> (signportsSection.getKeys (false));
                }
            }
        }
        return new ArrayList <> ();
    }
}
