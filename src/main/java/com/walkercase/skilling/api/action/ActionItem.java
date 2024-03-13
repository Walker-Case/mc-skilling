package com.walkercase.skilling.api.action;

import com.google.gson.*;
import com.walkercase.skilling.Skilling;
import net.minecraft.resources.ResourceLocation;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

/**
 * ActionItem is used to house data for actions, requirements, xp, and the skillbook icon for this item.
 */
public class ActionItem{
    public final ResourceLocation item;
    public ActionData[] actions;
    public final ActionIcon icon;

    public static class Serializer implements JsonSerializer<ActionItem> {
        @Override
        public JsonElement serialize(ActionItem src, Type typeOfSrc, JsonSerializationContext context) {
            Gson gson = Skilling.getGson();
            JsonObject newObject = new JsonObject();
            newObject.add("item", gson.toJsonTree(src.item.toString()));
            newObject.add("icon", gson.toJsonTree(src.icon, ActionIcon.class));
            JsonArray actions = new JsonArray();
            for (ActionData action : src.actions) {
                actions.add(gson.toJsonTree(action, ActionData.class));
            }
            newObject.add("actions", actions);

            return newObject;
        }
    }

    public static class Deserializer implements JsonDeserializer<ActionItem> {
        @Override
        public ActionItem deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            JsonObject itemObject = json.getAsJsonObject();
            ArrayList<ActionData> actionList = new ArrayList<>();
            Gson gson = Skilling.getGson();

            String name = itemObject.get("item").getAsString();

            AtomicReference<ActionIcon> actionIcon=new AtomicReference<>(null);
            if(itemObject.has("icon")){
                JsonObject iconData = itemObject.getAsJsonObject("icon");
                actionIcon.set(gson.fromJson(iconData, ActionIcon.class));
            }

            if(itemObject.has("actions")){
                JsonArray actions = itemObject.getAsJsonArray("actions");
                actions.forEach(obj->{
                    JsonObject actionObj = obj.getAsJsonObject();
                    ActionData ad = gson.fromJson(actionObj, ActionData.class);

                    actionList.add(ad);
                });
            }

            Skilling.LOGGER.debug("Successfully loaded item action: " + name);

            if(actionIcon.get() == null)
                actionIcon.set(new ActionIcon(new ResourceLocation(name), true, 8));
            return new ActionItem(new ResourceLocation(name), actionList.toArray(new ActionData[0]), actionIcon.get());
        }
    }

    /**
     * Create a new ActionItem with the given parameters.
     * @param item
     * @param actions
     * @param icon
     */
    public ActionItem(ResourceLocation item, ActionData[] actions, ActionIcon icon) {
        this.item = item;
        this.actions = actions;
        this.icon = icon;
    }

    /**
     * Called to add ActionData to this ActionItem.
     * @param action
     */
    public void addAction(ActionData action) {
        List<ActionData> list = Arrays.asList(actions);
        list.add(action);
        actions = list.toArray(new ActionData[0]);
    }
}