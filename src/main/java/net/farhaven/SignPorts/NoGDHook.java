package net.farhaven.SignPorts;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

public class NoGDHook implements GriefDefenderHook{
    @Override
    public boolean canPlayerBreakBlock(Player player, Block block) {
        return true;
    }

    @Override
    public boolean canPlayerMakeClaim(Player player, Location location) {
        return true;
    }

    @Override
    public String getClaimName(Location location) {
        return "None";
    }
}
