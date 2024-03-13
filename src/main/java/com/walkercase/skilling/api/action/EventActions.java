package com.walkercase.skilling.api.action;

import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.ApiStatus;

import java.util.ArrayList;
import java.util.HashMap;

import static com.walkercase.skilling.api.action.EventActions.EventKeys.*;

/**
 * The link between usable forge events and the ActionAPI.
 */
public class EventActions {

    private static final HashMap<ResourceLocation, ArrayList<ActionHolder>> EVENT_ACTION_LIST = new HashMap<>();

    /**
     * Contains a list of arbitrary keys used to bind forge events to the ActionAPI action system.
     */
    public static class EventKeys{
        public static final ResourceLocation BLOCK_BREAK_EVENT = new ResourceLocation("minecraft", "block_break_event");
        public static final ResourceLocation BLOCK_PLACE_EVENT = new ResourceLocation("minecraft", "block_place_event");
        public static final ResourceLocation RIGHT_CLICK_BLOCK = new ResourceLocation("minecraft", "right_click_block_event");
        public static final ResourceLocation ITEM_USE = new ResourceLocation("minecraft", "item_use_event");
        public static final ResourceLocation INTERACT_ENTITY = new ResourceLocation("minecraft", "interact_entity_event");
        public static final ResourceLocation MAP_UPDATE = new ResourceLocation("minecraft", "map_update");
        public static final ResourceLocation SMELTING = new ResourceLocation("minecraft", "smelting");
        public static final ResourceLocation SMITHING_CHANGED = new ResourceLocation("minecraft", "smithing_changed");
        public static final ResourceLocation CRAFTING_GRID_CHANGED = new ResourceLocation("minecraft", "crafting_grid_changed");

        public static final ResourceLocation SINGLE_PERSON_USE_BLOCK = new ResourceLocation("skilling", "single_person_use_block_event");
        public static final ResourceLocation FIRST_TIME_BIOME_ENTER = new ResourceLocation("skilling", "first_time_biome_enter");

    }

    /**
     * Called to bind the default actions.
     * This should not be called by modders!
     */
    @ApiStatus.Internal
    public static void bindDefaultActions(){
        EventActions.addActionEventListener(BLOCK_BREAK_EVENT, ActionAPI.ActionKeys.SKILLING_CHOPPING);
        EventActions.addActionEventListener(BLOCK_BREAK_EVENT, ActionAPI.ActionKeys.MINECRAFT_MINING);
        EventActions.addActionEventListener(BLOCK_BREAK_EVENT, ActionAPI.ActionKeys.MINECRAFT_BREAK_BLOCK);
        EventActions.addActionEventListener(RIGHT_CLICK_BLOCK, ActionAPI.ActionKeys.MINECRAFT_INTERACT_BLOCK);
        EventActions.addActionEventListener(BLOCK_PLACE_EVENT, ActionAPI.ActionKeys.MINECRAFT_PLACE_BLOCK);

        EventActions.addActionEventListener(INTERACT_ENTITY, ActionAPI.ActionKeys.MINECRAFT_INTERACT_ENTITY);
        EventActions.addActionEventListener(ITEM_USE, ActionAPI.ActionKeys.MINECRAFT_INTERACT_ITEM);
        EventActions.addActionEventListener(FIRST_TIME_BIOME_ENTER, ActionAPI.ActionKeys.MINECRAFT_FIRST_TIME_BIOME_ENTER);
        EventActions.addActionEventListener(MAP_UPDATE, ActionAPI.ActionKeys.MINECRAFT_MAP_UPDATE);
        EventActions.addActionEventListener(SINGLE_PERSON_USE_BLOCK, ActionAPI.ActionKeys.MINECRAFT_SINGLE_USE_BLOCK);

        EventActions.addActionEventListener(CRAFTING_GRID_CHANGED, ActionAPI.ActionKeys.MINECRAFT_CRAFTING);

        EventActions.addActionEventListener(SMELTING, ActionAPI.ActionKeys.SKILLING_COOKING);
        EventActions.addActionEventListener(SMELTING, ActionAPI.ActionKeys.MINECRAFT_SMOKING);
        EventActions.addActionEventListener(SMELTING, ActionAPI.ActionKeys.MINECRAFT_SMELTING);
        EventActions.addActionEventListener(SMELTING, ActionAPI.ActionKeys.MINECRAFT_BLASTING);

        EventActions.addActionEventListener(SMITHING_CHANGED, ActionAPI.ActionKeys.MINECRAFT_SMITHING);
        EventActions.addActionEventListener(SMITHING_CHANGED, ActionAPI.ActionKeys.MINECRAFT_CRAFTING);
        EventActions.addActionEventListener(SMITHING_CHANGED, ActionAPI.ActionKeys.MINECRAFT_FARMING);
    }

    /**
     * The given ActionHolder will listen for events fired on the given key.
     * @param key
     * @param actionKeyName
     */
    public static void addActionEventListener(ResourceLocation key, ResourceLocation actionKeyName) {
        addActionEventListener(key, new EventActions.ActionHolder(actionKeyName));
    }

    /**
     * The given ActionHolder will listen for events fired on the given key.
     * @param key
     * @param holder
     */
    private static synchronized void addActionEventListener(ResourceLocation key, ActionHolder holder) {
        if(!EVENT_ACTION_LIST.containsKey(key))
            EVENT_ACTION_LIST.put(key, new ArrayList<>());
        EVENT_ACTION_LIST.get(key).add(holder);
    }

    /**
     * Returns a list of ActionHolders registered to the given class.
     *
     * @param key
     * @return
     */
    public static synchronized ArrayList<ActionHolder> getActionHolders(ResourceLocation key){
        if(!EVENT_ACTION_LIST.containsKey(key))
            return new ArrayList<>();
        return deepCopy(EVENT_ACTION_LIST.get(key));
    }

    /**
     * Returns a deep copy of the ActionHolder list as an array.
     *
     * @param list
     * @param <T>
     * @return
     */
    private static <T extends ActionHolder> ArrayList<T> deepCopy(ArrayList<T> list) {
        ArrayList<T> temp = new ArrayList<>();
        list.forEach(x -> temp.add((T) x.clone()));
        return temp;
    }

    /**
     * Ties skills and actions together.
     */
    public static class ActionHolder{
        public final ResourceLocation actionKey;
        public boolean passed = false;
        public ActionData actionData;

        public ActionHolder(ResourceLocation actionKey){
            this.actionKey = actionKey;
        }

        public ActionHolder clone(){
           return new ActionHolder(actionKey);
        }
    }
}
