package com.walkercase.skilling.client.gui.component;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.walkercase.skilling.Skilling;
import com.walkercase.skilling.SkillingConfig;
import com.walkercase.skilling.api.action.ActionData;
import com.walkercase.skilling.api.action.skill.Skill;
import com.walkercase.skilling.api.action.skill.SkillManager;
import com.walkercase.skilling.client.gui.screen.SkillingBookScreen;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;

import java.util.ArrayList;
import java.util.Objects;

/**
 * Displays an icon for a given Skill.
 */
public class SkillIconDisplayWidget extends AbstractWidget implements Renderable {
    public Skill skill;
    public int currentLevel;
    public double currentXp;
    public long entryTime;
    public boolean canDisplayLockIcon = false;

    private long levelUpAnimTime = -1;

    public static final ResourceLocation ATLAS = new ResourceLocation(Skilling.MODID, "textures/gui/atlas.png");
    private final SkillingBookScreen screen;


    /**
     * Creates a new display widget.
     * @param x
     * @param y
     * @param skill
     * @param currentLevel
     * @param currentXp
     * @param screen
     * @param canDisplayLockIcon
     */
    public SkillIconDisplayWidget(int x, int y, Skill skill, int currentLevel, double currentXp, SkillingBookScreen screen, boolean canDisplayLockIcon) {
        super(x, y, 16, 16, Component.empty());
        this.skill = skill;
        this.currentLevel = currentLevel;
        this.currentXp = currentXp;
        this.entryTime = System.currentTimeMillis();
        this.screen = screen;
        this.canDisplayLockIcon = canDisplayLockIcon;
    }

    /**
     * Called to perform the level up animation.
     * @param newLevel
     */
    public void doLevelUp(int newLevel) {
        levelUpAnimTime = System.currentTimeMillis() + 5000;
        this.currentLevel = newLevel;
    }

    public void render(PoseStack poseStack, int x, int y) {
        double ll = currentXp / skill.getXpForLevel(currentLevel + 1);

        RenderSystem.setShaderTexture(0, ATLAS);
        GuiComponent.blit(poseStack, x, y, 16 * Math.min(10, Math.round(ll * 10)), 0, 16, 16, 256, 256);

        if(skill.getItemIcon().isIconItem())
            Minecraft.getInstance().getItemRenderer().renderGuiItem(poseStack, skill.getItemIcon().getIconAsItem(), x, y);
        if(skill.getItemIcon().isEntityIcon()){
            if(skill.getItemIcon().isEntityIcon() && skill.getItemIcon().getEntityType().create(Objects.requireNonNull(Minecraft.getInstance().level)) instanceof  LivingEntity entity)
                InventoryScreen.renderEntityInInventoryFollowsMouse(
                        poseStack, x, y + 16, skill.getItemIcon().getScale(),
                        0, 0,
                        entity);
        }

        if (this.levelUpAnimTime > System.currentTimeMillis())
            Minecraft.getInstance().font.draw(poseStack, currentLevel + "", x + 18, y + 6, 0xFF0000);


        //Render lock on skill icon
        if(!skill.meetsRequirements(Minecraft.getInstance().player) && canDisplayLockIcon && !SkillingConfig.COMMON.CLIENT.showAllEntries.get()){
            RenderSystem.setShaderTexture(0, ATLAS);
            GuiComponent.blit(poseStack, x, y, 16, 16, 16, 16, 256, 256);
        }

        if(isHovered){
            RenderSystem.setShaderTexture(0, ATLAS);
            blit(poseStack, x, y, 0, 16, 16, 16);

            setTooltipDelay(10);

            ArrayList<Component> list = new ArrayList<>();
            list.add(Component.translatable(skill.getUnlocalizedName().toString()).withStyle(ChatFormatting.DARK_AQUA));

            double currXp = Math.round(SkillManager.getXp(Minecraft.getInstance().player, skill) * 100.0) / 100.0;
            double nextXp = Math.round(skill.getXpForLevel(SkillManager.getLevel(Minecraft.getInstance().player, skill) + 1) * 100.0) / 100.0;
            list.add(Component.literal(currXp + "/" + nextXp + " xp").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
            if(this.screen != null){
                SkillingBookScreen.TooltipInfo info = new SkillingBookScreen.TooltipInfo();
                if(this.skill.getRequirements() != null && !this.skill.meetsRequirements(Minecraft.getInstance().player)){
                    for (ActionData.Requirement requirement : this.skill.getRequirements()) {
                        boolean pass = SkillManager.getLevel(Minecraft.getInstance().player, requirement.skill()) >= requirement.minLevel();
                        list.add(Component.translatable(requirement.skill().name.toString()).append(Component.literal(" " + requirement.minLevel()))
                                .withStyle(pass ? ChatFormatting.GREEN : ChatFormatting.GRAY));
                    }
                }
            }

            if(this.screen != null){
                SkillingBookScreen.TooltipInfo info = new SkillingBookScreen.TooltipInfo();
                info.tooltipLines = list;
                info.tooltipImage = skill.icon.getIconAsItem().getTooltipImage();
                this.screen.setTooltipInfo(info);
            }else{
                this.setTooltip(Tooltip.create(list.get(0)));
            }
        }
    }

    @Override
    public void renderWidget(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        render(poseStack, this.getX(), this.getY());
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput p_259858_) {

    }
}