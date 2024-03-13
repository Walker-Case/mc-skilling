package com.walkercase.skilling.client.gui.component;

import com.google.common.collect.Lists;
import com.walkercase.skilling.api.action.ActionData;
import com.walkercase.skilling.api.action.ActionIcon;
import com.walkercase.skilling.api.action.ActionItem;
import com.walkercase.skilling.api.action.skill.Skill;
import com.walkercase.skilling.api.action.skill.SkillManager;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectUtil;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.PotionItem;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import net.minecraftforge.registries.ForgeRegistries;
import oshi.util.tuples.Pair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SkillPageWidget {


    public final HashMap<ResourceLocation, ArrayList<Pair<Skill, Integer>>> requirements = new HashMap();
    private ActionIcon actionIcon;
    private ActionItem actionItem;

    public SkillPageWidget(ActionItem actionItem){
        this.actionItem = actionItem;
        this.actionIcon = actionItem.icon;
    }

    public SkillPageWidget(ActionItem actionItem, ResourceLocation icon) {
        this.actionIcon = new ActionIcon(icon);
        this.actionItem = actionItem;
    }

    public ActionItem getActionItem(){
        return this.actionItem;
    }

    public ActionIcon getActionIcon(){
        return this.actionIcon;
    }

    public void addRequirement(ResourceLocation action, Pair<Skill, Integer> reqs){
        if(!requirements.containsKey(action))
            requirements.put(action, new ArrayList<>());
        requirements.get(action).add(reqs);
    }

    public List<Component> getTooltipLines(){
        List<Component> lines = new ArrayList<>();

        if(this.actionIcon.isIconItem())
            lines.add(Component.empty().append(getTooltipFromItem(this.actionIcon.getIconAsItem()).get(0)).withStyle(this.actionIcon.getIconAsItem().getRarity().getStyleModifier()));
        else if(this.actionIcon.isEntityIcon())
            lines.add(Component.translatable("entity." + actionIcon.getIcon().getNamespace() + "." + actionIcon.getIcon().getPath()).withStyle(ChatFormatting.AQUA, ChatFormatting.UNDERLINE));
        else
            lines.add(Component.translatable(actionIcon.getIcon().toString()).withStyle(ChatFormatting.AQUA, ChatFormatting.UNDERLINE));

        if(this.actionIcon.getEnchantments() != null){
            for (EnchantmentInstance enchantment : this.actionIcon.getEnchantments()) {
                ResourceLocation key = ForgeRegistries.ENCHANTMENTS.getKey(enchantment.enchantment);
                lines.add(Component.translatable("enchantment."  + key.getNamespace() + "." + key.getPath()).append(Component.literal(" " + enchantment.level)).withStyle(ChatFormatting.AQUA));
            }
        }

        if(this.actionIcon.getIconAsItem().getItem() instanceof PotionItem){
            List<com.mojang.datafixers.util.Pair<Attribute, AttributeModifier>> list = Lists.newArrayList();
            PotionUtils.getMobEffects(this.actionIcon.getIconAsItem()).forEach(mobeffectinstance->{
                MutableComponent mutablecomponent = Component.translatable(mobeffectinstance.getDescriptionId());
                MobEffect mobeffect = mobeffectinstance.getEffect();
                Map<Attribute, AttributeModifier> map = mobeffect.getAttributeModifiers();
                if (!map.isEmpty()) {
                    for(Map.Entry<Attribute, AttributeModifier> entry : map.entrySet()) {
                        AttributeModifier attributemodifier = entry.getValue();
                        AttributeModifier attributemodifier1 = new AttributeModifier(attributemodifier.getName(), mobeffect.getAttributeModifierValue(mobeffectinstance.getAmplifier(), attributemodifier), attributemodifier.getOperation());
                        list.add(new com.mojang.datafixers.util.Pair<>(entry.getKey(), attributemodifier1));
                    }
                }

                if (mobeffectinstance.getAmplifier() > 0) {
                    mutablecomponent = Component.translatable("potion.withAmplifier", mutablecomponent, Component.translatable("potion.potency." + mobeffectinstance.getAmplifier()));
                }

                if (!mobeffectinstance.endsWithin(20)) {
                    mutablecomponent = Component.translatable("potion.withDuration", mutablecomponent, MobEffectUtil.formatDuration(mobeffectinstance, 1.0f));
                }

                lines.add(mutablecomponent.withStyle(mobeffect.getCategory().getTooltipFormatting()));
            });
        }


        requirements.forEach((action, list)->{
            lines.add(Component.translatable(action.toString()).withStyle(ChatFormatting.GRAY, ChatFormatting.UNDERLINE));

            for(int i=0;i<list.size();i++){
                ChatFormatting color = SkillManager.getLevel(Minecraft.getInstance().player,
                        list.get(i).getA()) < list.get(i).getB() ? ChatFormatting.GRAY : ChatFormatting.GREEN;

                lines.add(Component.translatable(list.get(i).getA().getUnlocalizedName().toString()).withStyle(color)
                        .append(Component.literal(" " + list.get(i).getB()).withStyle(color)));
            }
        });


        return lines;
    }

    private List<Component> getTooltipFromItem(ItemStack p_96556_) {
        return p_96556_.getTooltipLines(Minecraft.getInstance().player, Minecraft.getInstance().options.advancedItemTooltips ? TooltipFlag.Default.ADVANCED : TooltipFlag.Default.NORMAL);
    }

    public void addRequirement(ActionData.Requirement requirement) {
        addRequirement(requirement.action(), new Pair<>(requirement.skill(), requirement.minLevel()));
    }
}
