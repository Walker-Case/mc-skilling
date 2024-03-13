package com.walkercase.skilling.client.skill;

import com.walkercase.skilling.Skilling;
import com.walkercase.skilling.api.action.ActionAPI;
import com.walkercase.skilling.api.action.ActionData;
import com.walkercase.skilling.api.action.ActionIcon;
import com.walkercase.skilling.api.action.ActionItem;
import com.walkercase.skilling.api.action.skill.Skill;
import com.walkercase.skilling.api.action.skill.SkillManager;
import com.walkercase.skilling.client.event.SkillPageRequirements;
import com.walkercase.skilling.client.gui.component.SkillPageWidget;
import com.walkercase.skilling.client.gui.overlay.OverlayListener;
import com.walkercase.skilling.event.ActionLoadedEvent;
import com.walkercase.skilling.event.PlayerLevelSetEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

@OnlyIn(Dist.CLIENT)
@Mod.EventBusSubscriber(modid = Skilling.MODID, value = Dist.CLIENT)
public class SkillClientEvents {

    @SubscribeEvent
    public static void playerLevelSet(PlayerLevelSetEvent event){
        if(event.player.level.isClientSide){
            updateSkillBookVisibility(event.player);
        }
    }

    @SubscribeEvent
    public static void actionLoaded(ActionLoadedEvent e){
        for (ActionData action : e.item.actions) {
            if(action.action.equals(ActionAPI.ActionKeys.MINECRAFT_FIRST_TIME_BIOME_ENTER)){
                e.item.icon.setSkillbookVisible(false);
            }
        }
    }

    /**
     * Called to update the client players visibility.
     * @param player
     */
    public static void updateSkillBookVisibility(Player player){
        if(player.level.isClientSide){
            for (ActionItem actionItem : ActionAPI.getItemsForAction(ActionAPI.ActionKeys.MINECRAFT_FIRST_TIME_BIOME_ENTER)) {
                actionItem.icon.setSkillbookVisible(SkillManager.playerHasVisitedBiome(player, actionItem.item));
            }
        }
    }

    @SubscribeEvent
    public static void skillPageSpecialEntries(SkillPageRequirements event){
        ResourceLocation xpBonusRl = new ResourceLocation(Skilling.MODID, "xpbonus");

        if(event.skill == SkillManager.SkillKeys.CONSTRUCTION_SKILL.get()){
            for(int i = 5; i< SkillManager.SkillKeys.CONSTRUCTION_SKILL.get().getMaxLevel(); i+=5){
                SkillPageWidget widget = new SkillPageWidget(new ActionItem(ForgeRegistries.BLOCKS.getKey(Blocks.ANVIL), null,
                        new ActionIcon(ForgeRegistries.BLOCKS.getKey(Blocks.ANVIL)).setDisplayName(xpBonusRl.toString(), true)));
                widget.addRequirement(new ActionData.Requirement(SkillManager.SkillKeys.CONSTRUCTION_SKILL.get(), xpBonusRl, i));
                event.addEntry(widget);
            }
        }

        ResourceLocation choppingSpeedRl = new ResourceLocation(Skilling.MODID, "choppingspeed");
        if(event.skill == SkillManager.SkillKeys.FORESTRY_SKILL.get()){
            for(int i = 15; i< SkillManager.SkillKeys.FORESTRY_SKILL.get().getMaxLevel(); i+=15){
                SkillPageWidget widget = new SkillPageWidget(new ActionItem(ForgeRegistries.ITEMS.getKey(Items.OAK_SAPLING), null,
                        new ActionIcon(ForgeRegistries.ITEMS.getKey(Items.OAK_SAPLING)).setDisplayName(choppingSpeedRl.toString(), true)));
                widget.addRequirement(new ActionData.Requirement(SkillManager.SkillKeys.FORESTRY_SKILL.get(), choppingSpeedRl, i));
                event.addEntry(widget);
            }
        }

        ResourceLocation cookingSpeedRl = new ResourceLocation(Skilling.MODID, "cookingspeed");
        if(event.skill == SkillManager.SkillKeys.COOKING_SKILL.get()){
            for(int i = 15; i< SkillManager.SkillKeys.COOKING_SKILL.get().getMaxLevel(); i+=15){
                SkillPageWidget widget = new SkillPageWidget(new ActionItem(ForgeRegistries.BLOCKS.getKey(Blocks.CAMPFIRE),
                        null, new ActionIcon(ForgeRegistries.BLOCKS.getKey(Blocks.CAMPFIRE)).setDisplayName(cookingSpeedRl.toString(), true)));
                widget.addRequirement(new ActionData.Requirement(SkillManager.SkillKeys.COOKING_SKILL.get(), cookingSpeedRl, i));
                event.addEntry(widget);
            }
        }

        ResourceLocation fishingSpeedRl = new ResourceLocation(Skilling.MODID, "fishingspeed");
        if(event.skill == SkillManager.SkillKeys.FISHING_SKILL.get()){
            for(int i = 15; i< SkillManager.SkillKeys.FISHING_SKILL.get().getMaxLevel(); i+=15){
                SkillPageWidget widget = new SkillPageWidget(new ActionItem(ForgeRegistries.ITEMS.getKey(Items.FISHING_ROD),
                        null, new ActionIcon(ForgeRegistries.ITEMS.getKey(Items.FISHING_ROD)).setDisplayName(fishingSpeedRl.toString(), true)));
                widget.addRequirement(new ActionData.Requirement(SkillManager.SkillKeys.FISHING_SKILL.get(), fishingSpeedRl, i));
                event.addEntry(widget);
            }
        }

        ResourceLocation miningSpeedRl = new ResourceLocation(Skilling.MODID, "miningspeed");
        if(event.skill == SkillManager.SkillKeys.MINING_SKILL.get()){
            for(int i = 15; i< SkillManager.SkillKeys.MINING_SKILL.get().getMaxLevel(); i+=15){
                SkillPageWidget widget = new SkillPageWidget(new ActionItem(ForgeRegistries.ITEMS.getKey(Items.DIAMOND_PICKAXE),
                        null, new ActionIcon(ForgeRegistries.ITEMS.getKey(Items.DIAMOND_PICKAXE)).setDisplayName(miningSpeedRl.toString(), true)));
                widget.addRequirement(new ActionData.Requirement(SkillManager.SkillKeys.MINING_SKILL.get(), miningSpeedRl, i));
                event.addEntry(widget);
            }
        }

        ResourceLocation smithingSpeedRl = new ResourceLocation(Skilling.MODID, "smithingspeed");
        if(event.skill == SkillManager.SkillKeys.SMITHING_SKILL.get()){
            for(int i = 15; i< SkillManager.SkillKeys.SMITHING_SKILL.get().getMaxLevel(); i+=15){
                SkillPageWidget widget = new SkillPageWidget(new ActionItem(ForgeRegistries.BLOCKS.getKey(Blocks.ANVIL),
                        null, new ActionIcon(ForgeRegistries.BLOCKS.getKey(Blocks.ANVIL)).setDisplayName(smithingSpeedRl.toString(), true)));
                widget.addRequirement(new ActionData.Requirement(SkillManager.SkillKeys.SMITHING_SKILL.get(), smithingSpeedRl, i));
                event.addEntry(widget);
            }
        }
    }

    /**
     * Sends the skill update packet to the client.
     * @param skill
     * @param currentLevel
     * @param currentXp
     */
    public static void updateClient(Skill skill, int currentLevel, double currentXp, boolean display) {
        Player player = Minecraft.getInstance().player;
        if(player.level.isClientSide){
            SkillManager.setXp(player, skill, currentXp);
            SkillManager.setLevel(player, skill, currentLevel);
            if(display)
                OverlayListener.XP_BUBBLE_OVERLAY.addXpGain(skill, currentLevel, currentXp);

            if(skill != null && skill == SkillManager.SkillKeys.EXPLORING_SKILL.get())
                updateSkillBookVisibility(player);
        }
    }

    /**
     * Called to update the client players located biomes list.
     * @param locatedBiomes
     */
    public static void updateClientLocatedBiomes(String[] locatedBiomes){
        SkillManager.setLocatedBiomes(Minecraft.getInstance().player, locatedBiomes);
        updateSkillBookVisibility(Minecraft.getInstance().player);
    }
}
