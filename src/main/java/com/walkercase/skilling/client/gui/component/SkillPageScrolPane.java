package com.walkercase.skilling.client.gui.component;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.walkercase.skilling.SkillingConfig;
import com.walkercase.skilling.api.action.skill.SkillManager;
import com.walkercase.skilling.client.gui.screen.SkillingBookScreen;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractScrollWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

public class SkillPageScrolPane extends AbstractScrollWidget{
    private final SkillingBookScreen screen;

    public SkillPageScrolPane(SkillingBookScreen screen, int p_240025_, int p_240026_, int p_240027_, int p_240028_, Component p_240029_) {
        super(p_240025_, p_240026_, p_240027_, p_240028_, p_240029_);
        this.screen = screen;
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput p_259858_) {

    }

    @Override
    protected int getInnerHeight() {
        if (screen.currentPage != null) {
            return (((int) getSortedStream().count() / 4) * 16) + 32;
        }
        return 16;
    }

    @Override
    protected boolean scrollbarVisible() {
        return this.getInnerHeight() > this.height;
    }

    @Override
    protected double scrollRate() {
        return 9;
    }

    @Override
    public void renderWidget(PoseStack p_239793_, int p_239794_, int p_239795_, float p_239796_) {
        if (this.visible) {
            enableScissor(this.getX() + 1, this.getY() + 1, this.getX() + this.width - 1, this.getY() + this.height - 1);
            p_239793_.pushPose();
            p_239793_.translate(0.0D, -this.scrollAmount, 0.0D);
            this.renderContents(p_239793_, p_239794_, p_239795_, p_239796_);
            p_239793_.popPose();
            disableScissor();
            if (this.scrollbarVisible()) {
                this.renderScrollBar(p_239793_);
            }
        }
    }

    private void renderScrollBar(PoseStack p_265033_) {
        int i = this.getScrollBarHeight();
        int x = this.getX() + this.width;
        int y = Math.max(this.getY(), (int) this.scrollAmount * (this.height - i) / this.getMaxScrollAmount() + this.getY());
        RenderSystem.setShaderTexture(0, SkillingBookScreen.ATLAS);
        blit(p_265033_, x, y, 1, 0, 32, 10, this.getScrollBarHeight(), 256, 256);
    }

    @Override
    protected int innerPadding() {
        return 0;
    }

    public Stream<SkillPageWidget> getSortedStream() {
        return Arrays.stream(screen.currentPage.requirements()).sorted((w1, w2) -> {
                    AtomicInteger i1 = new AtomicInteger();

                    w1.requirements.forEach((reqKey, map) -> {
                        map.forEach(iSkillIntegerPair -> {
                            if (iSkillIntegerPair.getA() == screen.currentPage.skill())
                                i1.addAndGet(iSkillIntegerPair.getB());
                            else
                                i1.addAndGet(0);
                        });
                    });

                    w2.requirements.forEach((reqKey, map) -> {
                        map.forEach(iSkillIntegerPair -> {
                            if (iSkillIntegerPair.getA() == screen.currentPage.skill())
                                i1.addAndGet(-iSkillIntegerPair.getB());
                            else
                                i1.addAndGet(0);
                        });
                    });

                    return i1.get();
                })
                .filter(g -> g.getActionIcon().isSkillbookVisible())
                .filter(g -> g.getTooltipLines().stream().anyMatch(h -> h.getString().toLowerCase().contains(screen.SEARCH_BAR.getValue().toLowerCase())))
                .filter(g -> {
                    if (screen.SHOW_ALL_CRAFTABLE.getCurrentState() == 1)
                        return true;

                    AtomicBoolean pass = new AtomicBoolean(true);
                    g.requirements.forEach((g1, g2) -> {
                        g2.forEach(pair -> {
                            if (SkillManager.getLevel(Minecraft.getInstance().player, pair.getA()) < pair.getB())
                                pass.set(false);
                        });
                    });
                    return pass.get();
                })

                ;
    }

    @Override
    protected void renderContents(PoseStack poseStack, int mouseX, int mouseY, float partialTicks) {
        poseStack.pushPose();
        if (screen.currentPage != null) {
            //int startX = screen.getStartX();
            //int startY = screen.getStartY();

            //startX += 16 + 126 + 10;
            //startY += 64 + 2;

            //final int[] x = {startX};
            //final int[] y = {startY};

            final int[] x = {this.getX() + 2};
            final int[] y = {this.getY() + 2};
            int startX = this.getX() + 2;
            int startY = this.getY() + 2;

            int finalStartX = startX;
            getSortedStream().forEach(req -> {
                if (!req.getActionIcon().isEntityIcon() && (req.getActionIcon().getIconAsItem().isEmpty() && !SkillingConfig.COMMON.CLIENT.showAllEntries.get()))
                    return;

                if (req.getActionIcon().isIconItem())
                    Minecraft.getInstance().getItemRenderer().renderGuiItem(poseStack, req.getActionIcon().getIconAsItem(), x[0], y[0]);

                if (req.getActionIcon().isEntityIcon()) {
                    Entity entity = req.getActionIcon().getEntityType().create(Minecraft.getInstance().level);
                    if ((entity == null | !(entity instanceof LivingEntity)) && !SkillingConfig.COMMON.CLIENT.showAllEntries.get())
                        return;

                    if (entity instanceof LivingEntity livingEntity) {
                        InventoryScreen.renderEntityInInventoryFollowsMouse(
                                poseStack, x[0] + 8, y[0] + 16, req.getActionIcon().getScale(),
                                (float) (x[0]) - mouseX, (float) (y[0]) - mouseY,
                                livingEntity);
                    }
                }

                if (mouseX > x[0] && mouseX < x[0] + 16 && mouseY <= this.getHeight() + this.getY() && mouseY + (int) this.scrollAmount > y[0] && mouseY + (int) this.scrollAmount < y[0] + 16) {
                    SkillingBookScreen.TooltipInfo info = new SkillingBookScreen.TooltipInfo();
                    info.tooltipLines = req.getTooltipLines();
                    if (req.getActionIcon().getDisplayName() != null) {
                        info.tooltipLines.remove(0);
                        info.tooltipLines.add(0, req.getActionIcon().isDisplayNameTranslated() ?
                                Component.translatable(req.getActionIcon().getDisplayName()) : Component.literal(req.getActionIcon().getDisplayName()));
                    }
                    if (Minecraft.getInstance().options.advancedItemTooltips)
                        info.tooltipLines.add(Component.literal(req.getActionItem().item.toString()).withStyle(ChatFormatting.GRAY));
                    if (req.getActionIcon().isIconItem())
                        info.tooltipImage = req.getActionIcon().getIconAsItem().getTooltipImage();
                    else info.tooltipImage = new ItemStack(Items.AIR).getTooltipImage();
                    screen.setTooltipInfo(info);
                }

                x[0] += 16;
                if (x[0] > finalStartX + 64) {
                    x[0] = finalStartX;
                    y[0] += 16;
                }
            });
            poseStack.popPose();
        }
    }
}
