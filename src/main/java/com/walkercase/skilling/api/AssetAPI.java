package com.walkercase.skilling.api;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import net.minecraft.resources.ResourceLocation;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

/**
 * Contains various methods to assist with asset usage.
 */
public class AssetAPI {

    /**
     * Read a loot table from data/{modid}/loot_tables/{location}.json
     *
     * @param location
     * @return
     */
    public static synchronized JsonElement readLootTable(ResourceLocation location) {
        return readData(new ResourceLocation(location.getNamespace(), "loot_tables/" + location.getPath()));
    }

    /**
     * Read a forge item from data/{modid}/forge_items/{location}.json
     *
     * @param location
     * @return
     */
    public static JsonElement readForgeItem(ResourceLocation location) {
        return readData(new ResourceLocation(location.getNamespace(), "forge_items/" + location.getPath()));
    }

    /**
     * Read a forge item from data/{modid}/loot_modifiers/{location}.json
     *
     * @param location
     * @return
     */
    public static JsonElement readLootModifier(ResourceLocation location) {
        return readData(new ResourceLocation(location.getNamespace(), "loot_modifiers/" + location.getPath()));
    }

    /**
     * Read from assets/models/block/{location}.json
     *
     * @param location
     * @return
     */
    public static JsonElement readBlockModel(ResourceLocation location) {
        return readModel(new ResourceLocation(location.getNamespace(), "block/" + location.getPath()));
    }

    /**
     * Read from assets/models/entity/{location}.json
     *
     * @param location
     * @return
     */
    public static JsonElement readEntityModel(ResourceLocation location) {
        return readModel(new ResourceLocation(location.getNamespace(), "entity/" + location.getPath()));
    }

    /**
     * Read from assets/models/item/{location}.json
     *
     * @param location
     * @return
     */
    public static JsonElement readItemModel(ResourceLocation location) {
        return readModel(new ResourceLocation(location.getNamespace(), "item/" + location.getPath()));
    }

    /**
     * Read from assets/models/{location}.json
     *
     * @param location
     * @return
     */
    public static JsonElement readModel(ResourceLocation location) {
        return readAsset(new ResourceLocation(location.getNamespace(), "models/" + location.getPath()));
    }

    /**
     * Read a JsonElement from data/{modid}/{location}.json.
     *
     * @param location
     * @return
     */
    public static JsonElement readData(ResourceLocation location) {
        return read("data/" + location.getNamespace() + "/" + location.getPath() + ".json");
    }

    /**
     * Reads a lang file from the disk.
     *
     * @param modid
     * @param lang
     * @return
     */
    public static JsonObject readLang(String modid, String lang) {
        return AssetAPI.readAsset(new ResourceLocation(modid, "lang/" + lang)).getAsJsonObject();
    }

    /**
     * Read a JsonElement from assets/{modid}/{location}.json.
     *
     * @param location
     * @return
     */
    public static JsonElement readAsset(ResourceLocation location) {
        return read("assets/" + location.getNamespace() + "/" + location.getPath() + ".json");
    }

    /**
     * Read a JsonElement from the provided location.
     *
     * @param location
     * @return
     */
    public static JsonElement read(String location) throws JsonSyntaxException {
        StringBuilder sb = new StringBuilder();
        InputStream is = AssetAPI.class.getClassLoader()
                .getResourceAsStream(location);

        if (is == null) {
            return null;
        }

        InputStreamReader isr = new InputStreamReader(is, StandardCharsets.UTF_8);
        new BufferedReader(isr).lines().forEach(sb::append);
        return JsonParser.parseString(sb.toString());
    }

}
