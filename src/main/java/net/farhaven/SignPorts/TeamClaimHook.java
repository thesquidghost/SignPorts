package net.farhaven.SignPorts;

import com.sun.tools.javac.Main;
import net.farhaven.SignPorts.GriefDefenderHook;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

public class TeamClaimHook implements GriefDefenderHook {
    private Main teamClaimPlugin;

    public TeamClaimHook(Main teamClaimPlugin) {
        this.teamClaimPlugin = teamClaimPlugin;
    }

    @Override
    public boolean canPlayerBreakBlock(Player player, Block block) {
        Location loc = block.getLocation();
        ClaimedLand claim = teamClaimPlugin.dataManager.getClaimedLands().stream()
                .filter(cl -> cl.getX() == loc.getBlockX() &&
                        cl.getZ() == loc.getBlockZ() &&
                        cl.getWorldName().equals(loc.getWorld().getName()))
                .findFirst()
                .orElse(null);

        if(claim == null) return true; // Not claimed, can break

        Region region = teamClaimPlugin.dataManager.getRegions().stream()
                .filter(r -> r.getUUID().equals(claim.getRegionUUID()))
                .findFirst()
                .orElse(null);

        if(region == null) return true;

        Team team = teamClaimPlugin.dataManager.getTeams().stream()
                .filter(t -> t.getUUID().equals(region.getTeamUUID()))
                .findFirst()
                .orElse(null);

        if(team == null) return true;

        return team.getLeader().equals(player.getUniqueId()) ||
                (team.getMembers().contains(player.getUniqueId()) &&
                        region.playerHas(player.getUniqueId(), Permission.CLAIM_PLACE_BREAK_BLOCK));
    }

    @Override
    public boolean canPlayerMakeClaim(Player player, Location location) {
        ClaimedLand claim = teamClaimPlugin.dataManager.getClaimedLands().stream()
                .filter(cl -> cl.getX() == location.getBlockX() &&
                        cl.getZ() == location.getBlockZ() &&
                        cl.getWorldName().equals(location.getWorld().getName()))
                .findFirst()
                .orElse(null);

        if(claim != null) return false; // Already claimed

        PlayerData playerData = teamClaimPlugin.dataManager.getPlayers().stream()
                .filter(pd -> pd.getUUID().equals(player.getUniqueId()))
                .findFirst()
                .orElse(null);

        return playerData != null && !playerData.getTeamUUIDs().isEmpty();
    }

    @Override
    public String getClaimName(Location location) {
        ClaimedLand claim = teamClaimPlugin.dataManager.getClaimedLands().stream()
                .filter(cl -> cl.getX() == location.getBlockX() &&
                        cl.getZ() == location.getBlockZ() &&
                        cl.getWorldName().equals(location.getWorld().getName()))
                .findFirst()
                .orElse(null);

        if(claim == null) return "Wilderness";

        Region region = teamClaimPlugin.dataManager.getRegions().stream()
                .filter(r -> r.getUUID().equals(claim.getRegionUUID()))
                .findFirst()
                .orElse(null);

        if(region == null) return "Unknown";

        Team team = teamClaimPlugin.dataManager.getTeams().stream()
                .filter(t -> t.getUUID().equals(region.getTeamUUID()))
                .findFirst()
                .orElse(null);

        if(team == null) return "Unknown";

        return team.getName() + "'s Claim";
    }
}