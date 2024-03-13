package com.walkercase.skilling.api.action;

import com.google.gson.*;
import com.walkercase.skilling.Skilling;
import com.walkercase.skilling.api.action.skill.Skill;
import com.walkercase.skilling.api.action.skill.SkillManager;
import net.minecraft.resources.ResourceLocation;

import java.lang.reflect.Type;
import java.util.ArrayList;

/**
 * Contains xp data and requirements for a given ActionItem.
 */
public class ActionData {
    public final ResourceLocation action;
    public XpData[] xpData;
    public Requirement[] requirements;

    public static class Serializer implements JsonSerializer<ActionData> {
        @Override
        public JsonElement serialize(ActionData src, Type typeOfSrc, JsonSerializationContext context) {
            JsonObject newObject = new JsonObject();
            newObject.add("action", new JsonPrimitive(src.action.toString()));
            if(src.xpData != null && src.xpData.length > 0){
                JsonObject xpData = new JsonObject();
                for (XpData xpDatum : src.xpData) {
                    if(xpDatum.skill == null){
                        Skilling.LOGGER.error("Null skill in action xp data of " + src.action.toString() + "...skipping entry!");
                        continue;
                    }
                    xpData.add(xpDatum.skill.getUnlocalizedName().toString(), new JsonPrimitive(xpDatum.xp));
                }
                newObject.add("xp", xpData);
            }
            if(src.requirements != null && src.requirements.length > 0){
                JsonObject requirements = new JsonObject();
                for (Requirement requirement : src.requirements) {
                    if(requirement.skill == null){
                        Skilling.LOGGER.error("Null skill in action requirement data of " + src.action + "...skipping entry!");
                        continue;
                    }
                    requirements.add(requirement.skill.getUnlocalizedName().toString(), new JsonPrimitive(requirement.minLevel));
                }
                newObject.add("requirements", requirements);
            }

            return newObject;
        }
    }

    public static class Deserializer implements JsonDeserializer<ActionData> {
        @Override
        public ActionData deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            JsonObject jsonObject = json.getAsJsonObject();
            String action = jsonObject.get("action").getAsString();
            ArrayList<XpData> xpList = new ArrayList<>();
            ArrayList<Requirement> requirementsList = new ArrayList<>();
            if(jsonObject.has("xp")){
                JsonObject xpObj = jsonObject.getAsJsonObject("xp");
                for (String s : xpObj.keySet()) {
                    Skill skill = SkillManager.getSkill(new ResourceLocation(s));
                    double d = xpObj.get(s).getAsDouble();
                    XpData data = new XpData(skill, d);
                    xpList.add(data);
                }
            }
            if(jsonObject.has("requirements")){
                JsonObject requirementObj = jsonObject.getAsJsonObject("requirements");
                for (String s : requirementObj.keySet()) {
                    Skill skill = SkillManager.getSkill(new ResourceLocation(s));
                    int minLevel = requirementObj.get(s).getAsInt();
                    requirementsList.add(new Requirement(skill, new ResourceLocation(action), minLevel));
                }
            }
            return new ActionData(new ResourceLocation(action), xpList.toArray(new XpData[0]), requirementsList.toArray(new Requirement[0]));
        }
    }

    /**
     * Create a new ActionItems ActionData with the given xpData and requirements.
     * @param action
     * @param xpData
     * @param requirements
     */
    public ActionData(ResourceLocation action, XpData[] xpData, Requirement[] requirements) {
        this.action = action;
        this.xpData = xpData;
        this.requirements = requirements;
    }

    /**
     * Immutable xp data.
     * @param skill
     * @param xp
     */
    public record XpData(Skill skill, double xp){}

    /**
     * Immutable requirement data.
     */
    public record Requirement(Skill skill, ResourceLocation action, Integer minLevel){}
}

