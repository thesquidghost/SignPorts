package net.farhaven.SignPorts;

import com.google.gson.*;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.Type;

public class ItemStackAdapter implements JsonSerializer<ItemStack>, JsonDeserializer<ItemStack> {

    @Override
    public JsonElement serialize(ItemStack src, Type typeOfSrc, JsonSerializationContext context) {
        JsonObject obj = new JsonObject();
        obj.addProperty("type", src.getType().toString());
        obj.addProperty("amount", src.getAmount());
        // Add more properties if needed
        return obj;
    }

    @Override
    public ItemStack deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        JsonObject obj = json.getAsJsonObject();
        // Restore more properties if needed
        return new ItemStack(org.bukkit.Material.valueOf(obj.get("type").getAsString()), obj.get("amount").getAsInt());
    }
}