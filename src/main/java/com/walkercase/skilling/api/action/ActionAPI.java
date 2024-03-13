package com.walkercase.skilling.api.action;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.walkercase.skilling.Skilling;
import com.walkercase.skilling.api.action.skill.Skill;
import com.walkercase.skilling.api.action.skill.SkillManager;
import com.walkercase.skilling.event.ActionLoadedEvent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.MinecraftForge;
import oshi.util.tuples.Pair;

import java.util.ArrayList;
import java.util.Optional;

/**
 * Allows for adding new Actions to the Skilling API.
 */
public class ActionAPI {
    private static final ArrayList<ActionItem> ACTIONS = new ArrayList<>();

    /**
     * Located the provided action and checks player requirements if it exists.
     * Returns a Pair consisting of a boolean representing if the player passed the requirements and
     * the Actions ActionData if it did pass otherwise B is null.
     * In cases
     * @param player
     * @param actionItem
     * @param actionKey
     * @return
     */
    public static Pair<Boolean, ActionData> findActionCheckRequirements(Player player, ResourceLocation actionItem, ResourceLocation actionKey){
        Optional<ActionItem> opt = getActionItem(actionItem);
        if(opt.isPresent()){
            ActionItem ai = opt.get();
            ActionData action = getAction(ai, actionKey);
            if(action != null){
                return new Pair<>(playerPassesRequirements(player, action), action);
            }
        }
        return new Pair<>(false, null);
    }

    /**
     * Contains a list of statically defined action keys.
     * These may not exist in data.
     */
    public static class ActionKeys{

        public static final ResourceLocation MINECRAFT_CRAFTING = new ResourceLocation("minecraft", "crafting");
        public static final ResourceLocation MINECRAFT_MINING = new ResourceLocation("minecraft", "mining");
        public static final ResourceLocation MINECRAFT_EQUIP = new ResourceLocation("minecraft", "equip");
        public static final ResourceLocation MINECRAFT_FISHING = new ResourceLocation("minecraft", "fishing");
        public static final ResourceLocation MINECRAFT_SMELTING = new ResourceLocation("minecraft", "smelting");
        public static final ResourceLocation MINECRAFT_KILLED = new ResourceLocation("minecraft", "killed");
        public static final ResourceLocation MINECRAFT_SMOKING = new ResourceLocation("minecraft", "smoking");
        public static final ResourceLocation MINECRAFT_BLASTING = new ResourceLocation("minecraft", "blasting");
        public static final ResourceLocation MINECRAFT_SMITHING = new ResourceLocation("minecraft", "smithing");
        public static final ResourceLocation MINECRAFT_FARMING = new ResourceLocation("minecraft", "farming");
        public static final ResourceLocation MINECRAFT_INTERACT_BLOCK = new ResourceLocation("minecraft", "interact_block");
        public static final ResourceLocation MINECRAFT_PLACE_BLOCK = new ResourceLocation("minecraft", "place_block");
        public static final ResourceLocation MINECRAFT_BREAK_BLOCK = new ResourceLocation("minecraft", "break_block");
        public static final ResourceLocation MINECRAFT_FIRST_TIME_BIOME_ENTER = new ResourceLocation("minecraft", "first_time_biome_enter");
        public static final ResourceLocation MINECRAFT_MAP_UPDATE = new ResourceLocation("minecraft", "map_update");
        public static final ResourceLocation MINECRAFT_INTERACT_ENTITY = new ResourceLocation("minecraft", "interact_entity");
        public static final ResourceLocation MINECRAFT_INTERACT_ITEM = new ResourceLocation("minecraft", "interact_item");
        public static final ResourceLocation MINECRAFT_BREWING = new ResourceLocation("minecraft", "brewing");
        public static final ResourceLocation MINECRAFT_ENCHANTING = new ResourceLocation("minecraft", "enchanting");
        public static final ResourceLocation MINECRAFT_SINGLE_USE_BLOCK = new ResourceLocation("minecraft", "single_player_use_block");

        public static final ResourceLocation SKILLING_CHOPPING = new ResourceLocation(Skilling.MODID, "chopping");
        public static final ResourceLocation SKILLING_COOKING = new ResourceLocation(Skilling.MODID, "cooking");
        public static final ResourceLocation SKILLING_CONSTRUCTION = new ResourceLocation(Skilling.MODID, "construction");
    }

    /**
     * Returns the action item from the provided resource location.
     * @param rl
     * @return
     */
    public static Optional<ActionItem> getActionItem(ResourceLocation rl){
        return ACTIONS.stream().filter(ah->ah.item.toString().equals(rl.toString())).findFirst();
    }

    /**
     * Returns an array of all action items.
     * @return
     */
    public static ActionItem[] getActionItems(){
        return ACTIONS.toArray(new ActionItem[0]);
    }

    /**
     * Loads actions from the provided JsonObject.
     * @param
     */
    public static void loadActionsFromJson(JsonObject itemObject, ResourceLocation name){
        Gson gson = Skilling.getGson();

        ActionItem item = gson.fromJson(itemObject, ActionItem.class);
        ActionLoadedEvent event = new ActionLoadedEvent(item);
        MinecraftForge.EVENT_BUS.post(event);
        if(!event.isCanceled())
            ACTIONS.add(item);
    }

    /**
     * Reads the action items requirements into memory.
     * @param name
     * @param actionKey
     * @param object
     * @return
     */
    public static ArrayList<ActionData.Requirement> readRequirements(String name, ResourceLocation actionKey, JsonObject object){
        ArrayList<ActionData.Requirement> requirements = new ArrayList<>();
        object.keySet().forEach(reqSkill->{
                ResourceLocation skillRl = new ResourceLocation(reqSkill);
                Skill skill = SkillManager.getSkill(skillRl);
                if(skill != null){
                    requirements.add(new ActionData.Requirement(skill, actionKey, object.get(reqSkill).getAsInt()));
                }else{
                    Skilling.LOGGER.error("Failed to find skill when loading requirement group: " + skillRl + " in item " + name);
                }
            });
        return requirements;
    }

    /**
     * Returns the ActionData with the given name from the ActionItem provided.
     * @param holder
     * @param location
     * @return
     */
    public static ActionData getAction(ActionItem holder, ResourceLocation location){
        for (ActionData action1 : holder.actions) {
            if(action1.action.toString().equals(location.toString())){
                return action1;
            }
        }
        return null;
    }

    /**
     * Returns true if the player passes all requirements as specified by the provided ActionData.
     * @param player
     * @param action
     * @return
     */
    public static boolean playerPassesRequirements(Player player, ActionData action){
        for (ActionData.Requirement requirement : action.requirements) {
            if(!SkillManager.playerPassesRequirement(player, requirement))
                return false;

        }

        return true;
    }

    /**
     * Returns a list of ActionItems that has an action by the provided name.
     * @param rl
     * @return
     */
    public static ActionItem[] getItemsForAction(ResourceLocation rl){
        ArrayList<ActionItem> acList = new ArrayList<>();
        for (ActionItem action : ACTIONS) {
            boolean addAction = false;
            for (ActionData actionData : action.actions) {
                if(actionData.action.equals(rl))
                    addAction = true;
            }
            if(addAction)
                acList.add(action);
        }
        return acList.toArray(new ActionItem[0]);
    }

}
