package net.farhaven.SignPorts;

import com.griefdefender.api.GriefDefender;
import com.griefdefender.api.User;
import com.griefdefender.api.claim.Claim;
import com.griefdefender.api.claim.ClaimManager;
import com.griefdefender.lib.flowpowered.math.vector.Vector3i;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import java.util.Objects;

public class GriefDefenderHookImpl implements GriefDefenderHook {
    @Override
    public boolean canPlayerBreakBlock(Player player, Block block) {
        Claim claim = Objects.requireNonNull(GriefDefender.getCore().getClaimManager(block.getWorld().getUID()))
                .getClaimAt(block.getLocation().getBlockX(), block.getLocation().getBlockY(), block.getLocation().getBlockZ());

        User user = GriefDefender.getCore().getUser(player.getUniqueId());
        return claim == null || claim.canBreak(block.getType(), block.getLocation(), user);
    }

    @Override
    public boolean canPlayerMakeClaim(Player player, Location location) {
        Claim claim = GriefDefender.getCore().getClaimAt(location);
        if (claim == null) {
            return false;
        }
        return claim.getOwnerUniqueId().equals(player.getUniqueId());
    }

    @Override
    public String getClaimName(Location location) {
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
