package net.farhaven.SignPorts;

import com.google.gson.*;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

import java.lang.reflect.Type;
import java.util.Objects;

public class LocationAdapter implements JsonSerializer<Location>, JsonDeserializer<Location> {

    @Override
    public JsonElement serialize(Location src, Type typeOfSrc, JsonSerializationContext context) {
        JsonObject obj = new JsonObject();
        // Ensure that world isn't null (it shouldn't be)
        obj.addProperty("world", Objects.requireNonNull(src.getWorld(), "World cannot be null").getName());
        obj.addProperty("x", src.getX());
        obj.addProperty("y", src.getY());
        obj.addProperty("z", src.getZ());
        obj.addProperty("yaw", src.getYaw());
        obj.addProperty("pitch", src.getPitch());
        return obj;
    }

    @Override
    public Location deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        JsonObject obj = json.getAsJsonObject();
        String worldName = obj.get("world").getAsString();
        World world = Bukkit.getWorld(worldName);
        if (world == null) {
            throw new JsonParseException("World '" + worldName + "' could not be found");
        }
        double x = obj.get("x").getAsDouble();
        double y = obj.get("y").getAsDouble();
        double z = obj.get("z").getAsDouble();
        float yaw = obj.get("yaw").getAsFloat();
        float pitch = obj.get("pitch").getAsFloat();
        return new Location(world, x, y, z, yaw, pitch);
    }
}