package com.walkercase.skilling.api.action;

import com.google.gson.*;
import com.walkercase.skilling.Skilling;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.PotionItem;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.ApiStatus;

import javax.annotation.Nullable;
import java.lang.reflect.Type;
import java.util.ArrayList;

/**
 * Contains information for displaying an ActionItem in the skill book.
 */
public class ActionIcon {
    private ResourceLocation icon;
    private String type = "";
    private String potion = "";
    private String displayName = "";
    private boolean displayNameTranslated = false;
    private EnchantmentInstance[] enchantments;
    private ActionItem actionItem;
    private boolean skillbookVisible;
    private int scale;

    public static class Serializer implements JsonSerializer<ActionIcon> {
        @Override
        public JsonElement serialize(ActionIcon src, Type typeOfSrc, JsonSerializationContext context) {
            JsonObject newObject = new JsonObject();
            if(src.getIcon() != null)
                newObject.add("icon", new JsonPrimitive(src.getIcon().toString()));
            newObject.add("type", new JsonPrimitive(src.type));
            newObject.add("potion", new JsonPrimitive(src.potion));
            newObject.add("skillbookVisible", new JsonPrimitive(src.skillbookVisible));
            newObject.add("scale", new JsonPrimitive(src.scale));

            JsonObject displayNameObj = new JsonObject();
            displayNameObj.add("name", new JsonPrimitive(src.displayName));
            displayNameObj.add("translated", new JsonPrimitive(src.displayNameTranslated));
            newObject.add("displayName", displayNameObj);

            if(src.getEnchantments() != null && src.getEnchantments().length > 0){
                JsonObject arr = new JsonObject();
                for(EnchantmentInstance ei : src.getEnchantments()) {
                    ResourceLocation rl = ForgeRegistries.ENCHANTMENTS.getKey(ei.enchantment);
                    arr.add(rl.toString(), new JsonPrimitive(ei.level));
                }
                newObject.add("enchantments", arr);
            }
            return newObject;
        }
    }

    public static class Deserializer implements JsonDeserializer<ActionIcon> {
        @Override
        public ActionIcon deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            JsonObject iconObject = json.getAsJsonObject();
            boolean skillbookVisible = true;
            String icon = "";
            String type = "";
            int scale = 8;
            String potion = "";
            String displayName = "";
            boolean translated = false;
            ArrayList<EnchantmentInstance> enchantments = new ArrayList<>();

            if (iconObject.has("icon"))
                icon = iconObject.get("icon").getAsString();
            else
                skillbookVisible = false;


            if(iconObject.has("displayName")){
                JsonObject displayData = iconObject.getAsJsonObject("displayName");
                if(displayData.has("name")){
                    displayName = displayData.get("name").getAsString();
                }

                if(displayData.has("translated")){
                    translated = displayData.get("translated").getAsBoolean();
                }
            }
            if(iconObject.has("type"))
                type = iconObject.get("type").getAsString();
            if(iconObject.has("scale"))
                scale = iconObject.get("scale").getAsInt();
            if(iconObject.has("icon"))
                icon = iconObject.get("icon").getAsString();
            if(iconObject.has("skillbookVisible"))
                skillbookVisible = iconObject.get("skillbookVisible").getAsBoolean();
            if(iconObject.has("potion"))
                potion = iconObject.get("potion").getAsString();
            if(iconObject.has("enchantments")){
                JsonObject arr = iconObject.get("enchantments").getAsJsonObject();
                for(String key : arr.keySet()){
                    Enchantment enchantment = ForgeRegistries.ENCHANTMENTS.getValue(new ResourceLocation(key));
                    if(enchantment != null){
                        int level = arr.get(key).getAsInt();
                        enchantments.add(new EnchantmentInstance(enchantment, level));
                    }else{
                        Skilling.LOGGER.error("Failed to read icon enchantments when loading ");
                        Skilling.LOGGER.error(iconObject.toString(), new NullPointerException());
                        skillbookVisible = false;
                    }
                }
            }

            return new ActionIcon(new ResourceLocation(icon), skillbookVisible, scale, type, potion, displayName, translated, enchantments.toArray(new EnchantmentInstance[0]));
        }
    }

    /**
     * Creates a new empty ActionIcon that isn't visible.
     */
    public ActionIcon(){
        this(new ResourceLocation(""), false, 8);
    }

    /**
     * Creates a new ActionIcon with the given ResourceLocation.
     * May not show properly if a valid entry isn't found.
     * @param icon
     */
    public ActionIcon(@Nullable ResourceLocation icon){
        this(icon, true, 8);
    }

    /**
     * Create a new ActionIcon with the given ResourceLocation and set whether it should be visible or not.
     * @param icon
     * @param skillbookVisible
     */
    public ActionIcon(@Nullable ResourceLocation icon, boolean skillbookVisible){
        this(icon, skillbookVisible, 8);
    }

    /**
     * Create a new ActionIcon with the given parameters.
     * @param icon
     * @param skillbookVisible
     * @param scale
     */
    public ActionIcon(@Nullable ResourceLocation icon, boolean skillbookVisible, int scale){
        this(icon, skillbookVisible, scale, "");
    }

    /**
     * Create a new ActionIcon with the given parameters.
     * @param icon
     * @param skillbookVisible
     * @param scale
     * @param type
     */
    public ActionIcon(@Nullable ResourceLocation icon, boolean skillbookVisible, int scale, String type){
        this.icon = icon;
        this.skillbookVisible = skillbookVisible;
        this.scale = scale;
        this.type = type;
    }

    /**
     * Create a new ActionIcon with the given parameters.
     * @param resourceLocation
     * @param skillbookVisible
     * @param scale
     * @param type
     * @param potion
     * @param displayName
     * @param translated
     * @param enchantments
     */
    public ActionIcon(ResourceLocation resourceLocation, boolean skillbookVisible, int scale, String type, String potion, String displayName, boolean translated, EnchantmentInstance[] enchantments) {
        this(resourceLocation, skillbookVisible, scale, type);
        this.potion = potion;
        this.enchantments = enchantments;
        this.displayName = displayName;
        this.displayNameTranslated = translated;
    }

    /**
     * Returns true if the display name provided should be translated.
     * @return
     */
    public boolean isDisplayNameTranslated(){
        return this.displayNameTranslated;
    }

    /**
     * Returns the raw potion string for this icon.
     * @return
     */
    public String getRawPotion(){
        return this.potion;
    }

    /**
     * Returns the raw type string for this icon.
     * @return
     */
    public String getRawType(){
        return this.type;
    }

    /**
     * Returns a list of enchantments for this ActionIcon.
     * @return
     */
    public EnchantmentInstance[] getEnchantments(){
        return this.enchantments;
    }

    /**
     * Returns the ActionItem associated with this icon.
     * @return
     */
    public ActionItem getActionItem() {
        return actionItem;
    }

    /**
     * Called to set the ActionItem associated with this icon.
     * @param actionItem
     */
    @ApiStatus.Internal
    public ActionIcon setActionItem(ActionItem actionItem) {
        this.actionItem = actionItem;
        return this;
    }

    /**
     * Returns true if this ActionIcon should be shown in the skillbook.
     * @return
     */
    public boolean isSkillbookVisible() {
        return skillbookVisible;
    }

    /**
     * Set wether or not this ActionIcon should render in the skillbook.
     * @param skillbookVisible
     */
    public ActionIcon setSkillbookVisible(boolean skillbookVisible) {
        this.skillbookVisible = skillbookVisible;
        return this;
    }

    /**
     * Returns this ActionIcons scale. Currently only used for entity types.
     * @return
     */
    public int getScale() {
        return scale;
    }

    /**
     * Sets this ActionIcons scale. Currently only used for entity types.
     * @param scale
     */
    public ActionIcon setScale(int scale) {
        this.scale = scale;
        return this;
    }

    /**
     * Returns true if the icon type is an entity.
     * @return
     */
    public boolean isEntityIcon(){
        if(type.equals("item") || type.equals("block"))
            return false;
        if(type.equals("entity"))
            return true;
        return  !isIconItem() && ForgeRegistries.ENTITY_TYPES.containsKey(getIcon());
    }

    /**
     * Returns this ActionIcons display name.
     * @return
     */
    public String getDisplayName(){
        return StringUtils.isEmpty(displayName) ? null : displayName;
    }

    /**
     * Set this ActionIcons display name and returns.
     * @param displayName
     * @param displayNameTranslated
     * @return ActionIcon
     */
    public ActionIcon setDisplayName(String displayName, boolean displayNameTranslated){
        this.displayName = displayName;
        this.displayNameTranslated = displayNameTranslated;
        return this;
    }

    /**
     * Returns the EntityType for this ActionIcon.
     * @return
     */
    public @Nullable EntityType<?> getEntityType(){
        return ForgeRegistries.ENTITY_TYPES.getValue(getIcon());
    }

    /**
     * Returns true if this ActionIcon is an item type.
     * @return
     */
    public boolean isIconItem(){
        if(type.equals("entity"))
            return false;
        if(type.equals("item") || type.equals("block"))
            return true;
        return  ForgeRegistries.ITEMS.containsKey(getIcon()) || ForgeRegistries.BLOCKS.containsKey(getIcon());
    }

    /**
     * Returns the ItemStack for this ActionIcon.
     * If it would be null, Items.AIR is returned instead.
     * @return
     */
    public ItemStack getIconAsItem(){
        ItemStack is = new ItemStack(type.equals("block") ? ForgeRegistries.BLOCKS.getValue(getIcon())  : ForgeRegistries.ITEMS.containsKey(getIcon()) ? ForgeRegistries.ITEMS.getValue(getIcon()) :
                ForgeRegistries.BLOCKS.containsKey(getIcon()) ? ForgeRegistries.BLOCKS.getValue(getIcon()) : Items.AIR);

        if(is.getItem() instanceof PotionItem && !StringUtils.isEmpty(potion)){
            CompoundTag tag = is.getOrCreateTag();
            tag.putString("Potion", potion);
        }

        if(enchantments != null){
            for (EnchantmentInstance enchantment : enchantments) {
                is.enchant(enchantment.enchantment, enchantment.level);
            }
        }

        return is;
    }

    /**
     * Called to set this ActionIcons icon location.
     * @param location
     */
    public ActionIcon setIcon(ResourceLocation location){
        this.icon = location;
        return this;
    }

    /**
     * Returns the raw icon for this ActionIcon.
     * @return
     */
    public ResourceLocation getIcon(){
        return this.actionItem == null ? icon : actionItem.item;
    }
}
