package net.farhaven.SignPorts;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.scheduler.BukkitRunnable;

public class TeleportTask extends BukkitRunnable {
    private final SignPorts plugin;
    private final Player player;
    private final Location initialLocation;
    private final Location destination;
    private final String signPortName;
    private int countdown;

    public TeleportTask(SignPorts plugin, Player player, Location destination, String signPortName) {
        this.plugin = plugin;
        this.player = player;
        this.initialLocation = player.getLocation();
        this.destination = destination;
        this.signPortName = signPortName;
        this.countdown = plugin.getTeleportDelay();
    }

    @Override
    public void run() {
        if (countdown > 0) {
            Location currentLocation = player.getLocation();
            if (currentLocation.getX() != initialLocation.getX() ||
                    currentLocation.getY() != initialLocation.getY() ||
                    currentLocation.getZ() != initialLocation.getZ()) {
                cancel();
                player.sendMessage(ChatColor.RED + "You moved! Teleportation to " + signPortName + " cancelled.");
                return;
            }
            player.sendMessage(ChatColor.YELLOW + "Teleporting to " + signPortName + " in " + countdown + " seconds. Don't move!");

            // Spawn portal particles around the player
            Location particleLocation = player.getLocation().add(0, 1, 0);
            player.getWorld().spawnParticle(Particle.PORTAL, particleLocation, 50, 0.5, 1, 0.5, 0.1);

            // Play a sound effect
            player.playSound(player.getLocation(), Sound.BLOCK_PORTAL_AMBIENT, 0.5f, 1.0f);

            countdown--;
        } else {
            cancel();
            Location currentLocation = player.getLocation();
            if (currentLocation.getX() == initialLocation.getX() &&
                    currentLocation.getY() == initialLocation.getY() &&
                    currentLocation.getZ() == initialLocation.getZ()) {

                // Trigger the teleport event with the NETHER_PORTAL cause
                PlayerTeleportEvent teleportEvent = new PlayerTeleportEvent(player, player.getLocation(), destination, PlayerTeleportEvent.TeleportCause.NETHER_PORTAL);
                plugin.getServer().getPluginManager().callEvent(teleportEvent);

                if (!teleportEvent.isCancelled()) {
                    player.teleport(destination, PlayerTeleportEvent.TeleportCause.PLUGIN);
                    player.sendMessage(ChatColor.GREEN + "You've been teleported to " + signPortName + "!");
                    plugin.getLogger().info("Player " + player.getName() + " teleported to " + signPortName);

                    // Play a sound effect at the destination
                    player.playSound(destination, Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 1.0f);
                }
            } else {
                player.sendMessage(ChatColor.RED + "You moved! Teleportation to " + signPortName + " cancelled.");
            }
        }
    }
}