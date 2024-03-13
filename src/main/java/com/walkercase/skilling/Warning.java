package com.walkercase.skilling;

import com.walkercase.skilling.api.action.ActionData;
import com.walkercase.skilling.api.action.skill.SkillManager;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

public class Warning {

    public static final Warning GENERIC = new Warning(new ResourceLocation(Skilling.MODID, "generic"));
    public static final Warning GENERIC_REQUIREMENT = new Warning(new ResourceLocation(Skilling.MODID, "generic.requirement"));
    public static final Warning DUPLICATE_OPEN_MENU = new Warning(new ResourceLocation(Skilling.MODID, "generic.duplicate.openmenu"));

    public final ResourceLocation name;

    public Warning(ResourceLocation name) {
        this.name = name;
    }

    public String getKey() {
        return "warn." + name.toString().replace(":", ".");
    }

    /**
     * Sends the specified warning to the player.
     * @param player
     * @param warning
     * @param args
     */
    public static void warnGeneric(Player player, Warning warning, Object... args) {
        warnGeneric(5000, player, warning, args);
    }

    /**
     * Sends the specified warning to the player.
     * @param player
     * @param warning
     * @param timeout
     * @param args
     */
    public static void warnGeneric(long timeout, Player player, Warning warning, Object... args) {
        if (!SkillManager.getSkillsModNBT(player).contains(warning.getKey()))
            SkillManager.getSkillsModNBT(player).put(warning.getKey(), new CompoundTag());
        CompoundTag tag = SkillManager.getSkillsModNBT(player);
        if (!player.level.isClientSide) {
            if (tag.getLong(warning.getKey()) < System.currentTimeMillis()) {
                tag.putLong(warning.getKey(), System.currentTimeMillis() + timeout);
                player.sendSystemMessage(Component.translatable(warning.getKey(), args));
            }
        }
    }

    /**
     * Warns the player about not having the proper skill requirements.
     * @param player
     * @param warning
     * @param requirementData
     */
    public static void warnRequirement(Player player, Warning warning, ActionData requirementData) {
        warnRequirement(5000, player, warning, requirementData);
    }

    /**
     * Warns the player about not having the proper skill requirements.
     * @param timeout
     * @param player
     * @param warning
     * @param requirementData
     */
    public static void warnRequirement(long timeout, Player player, Warning warning, ActionData requirementData) {
        if (!SkillManager.getSkillsModNBT(player).contains(warning.getKey()))
            SkillManager.getSkillsModNBT(player).put(warning.getKey(), new CompoundTag());
        CompoundTag tag = SkillManager.getSkillsModNBT(player);
        if (!player.level.isClientSide) {
            if (tag.getLong(warning.getKey()) < System.currentTimeMillis()) {
                tag.putLong(warning.getKey(), System.currentTimeMillis() + timeout);
                player.sendSystemMessage(Component.translatable(warning.getKey()));
                if(requirementData != null){
                    for(ActionData.Requirement r : requirementData.requirements){
                        ChatFormatting format = SkillManager.playerPassesRequirement(player, r) ? ChatFormatting.GREEN : ChatFormatting.GRAY;
                        player.sendSystemMessage(
                                Component.translatable(r.skill().name.toString())
                                .append(" ")
                                .append(Component.literal(r.minLevel() + "")).withStyle(format));
                    }
                }
            }
        }
    }

}
