package net.farhaven.SignPorts;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

public interface GriefDefenderHook {

    boolean canPlayerBreakBlock(Player player, Block block);

    boolean canPlayerMakeClaim(Player player, Location location);

    String getClaimName(Location location);

}
