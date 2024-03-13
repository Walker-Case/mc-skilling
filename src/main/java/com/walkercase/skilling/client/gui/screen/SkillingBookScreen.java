package com.walkercase.skilling.client.gui.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.walkercase.skilling.Skilling;
import com.walkercase.skilling.SkillingConfig;
import com.walkercase.skilling.api.action.skill.SkillManager;
import com.walkercase.skilling.client.gui.component.SkillIconDisplayWidget;
import com.walkercase.skilling.client.gui.component.SkillPage;
import com.walkercase.skilling.client.gui.component.SkillPageScrolPane;
import com.walkercase.skilling.client.gui.component.StatefulImageButton;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractScrollWidget;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

public class SkillingBookScreen extends Screen {
    public SkillingBookScreen() {
        super(Component.translatable("gui.skillingbook.title"));
    }

    public static final ResourceLocation BACKGROUND = new ResourceLocation(Skilling.MODID, "textures/gui/book.png");
    public static final ResourceLocation ATLAS = new ResourceLocation(Skilling.MODID, "textures/gui/atlas.png");

    public SkillPage currentPage = null;
    public final AbstractScrollWidget SCROLL_BAR = new SkillPageScrolPane(this, 164, 16, 96, 112, Component.empty());
    public final StatefulImageButton SHOW_ALL_CRAFTABLE =
            new StatefulImageButton(this,152, 16, 16, 16, 33, 16, new StatefulImageButton.State[]{
                    new StatefulImageButton.State(ATLAS, 32, 16, 256, 256),
                    new StatefulImageButton.State(ATLAS, 48, 16, 256, 256)
            }, new StatefulImageButton.Background(ATLAS, 32, 32, 48, 32, 256, 256), button->{
                if(button instanceof StatefulImageButton ib){
                    if(ib.getCurrentState() == 0){
                        ib.setCurrentState(1);
                    }else{
                        ib.setCurrentState(0);
                    }
                }
            });
    public final EditBox SEARCH_BAR =  new EditBox(Minecraft.getInstance().font, 154, 0, 64, 12, Component.empty()){
        @Override
        public boolean charTyped(char p_94122_, int p_94123_) {
            updatePage(currentPage);
            return super.charTyped(p_94122_, p_94123_);
        }
    };

    @Override
    public void onClose() {
        super.onClose();

        CompoundTag nbt = SkillManager.getSkillsModNBT(Minecraft.getInstance().player);
        CompoundTag screen = new CompoundTag();
        if(currentPage != null)
            screen.putString("current_screen", currentPage.skill().name.toString());
        screen.putString("search_bar", SEARCH_BAR.getValue());
        screen.putDouble("scroll_bar", SCROLL_BAR.scrollAmount);
        screen.putInt("show_craftable", SHOW_ALL_CRAFTABLE.getCurrentState());

        nbt.put("screen", screen);
    }

    @Override
    public void init(){
        int startX = (this.width/2)-126;
        int startY = (this.height/2)-126;

        SEARCH_BAR.setPosition(startX + 126 + 32, startY + 20 + 16);
        SCROLL_BAR.setPosition(startX + 126 + 16, startY + 48);
        SHOW_ALL_CRAFTABLE.setPosition(startX + 126 + 16, startY + 32);

        CompoundTag nbt = SkillManager.getSkillsModNBT(Minecraft.getInstance().player);
        if(nbt.contains("screen")){
            CompoundTag screen = nbt.getCompound("screen");
            if(currentPage == null || !currentPage.skill().name.toString().equals(screen.getString("current_screen"))){
                currentPage = ()->SkillManager.getSkill(new ResourceLocation(screen.getString("current_screen")));
            }
            SEARCH_BAR.setValue(screen.getString("search_bar"));
            SCROLL_BAR.scrollAmount = screen.getDouble("scroll_bar");
            SHOW_ALL_CRAFTABLE.setCurrentState(screen.getInt("show_craftable"));
        }

        updatePage(currentPage, SCROLL_BAR.scrollAmount);

        this.addRenderableWidget(SEARCH_BAR);
        this.addRenderableWidget(SCROLL_BAR);
        this.addRenderableWidget(SHOW_ALL_CRAFTABLE);

        AtomicInteger x = new AtomicInteger(startX + 22);
        AtomicInteger y = new AtomicInteger(startY + 30);
        Arrays.stream(SkillManager.getSkills()).filter(s->s.getItemIcon().isSkillbookVisible()).sorted((s1, s2)->{
            AtomicInteger count = new AtomicInteger();
            if(s1.getRequirements() != null)
                Arrays.stream(s1.getRequirements()).forEach(c-> count.addAndGet(c.minLevel()));
            if(s2.getRequirements() != null)
                Arrays.stream(s2.getRequirements()).forEach(c-> count.addAndGet(-c.minLevel()));
            return count.get();
        }).sorted((s1, s2)-> -Boolean.compare(s1.meetsRequirements(Minecraft.getInstance().player), s2.meetsRequirements(Minecraft.getInstance().player)))
                .forEach(skill->{
                    int currentLevel = SkillManager.getLevel(Minecraft.getInstance().player, skill);

                    SkillIconDisplayWidget bubble = new SkillIconDisplayWidget(x.get(), y.get(), skill, currentLevel, SkillManager.getXp(Minecraft.getInstance().player, skill), this, true){
                        @Override
                        public void renderWidget(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
                            this.doLevelUp(currentLevel);
                            render(poseStack, this.getX(), this.getY());
                        }
                        @Override
                        public void onClick(double x, double y){
                            if(skill.meetsRequirements(Minecraft.getInstance().player) || SkillingConfig.COMMON.CLIENT.showAllEntries.get())
                                updatePage(skill.renderBookPage());
                        }
                    };
                    bubble.setX(x.get());
                    bubble.setY(y.get());
                    this.addRenderableWidget(bubble);

                    x.addAndGet(32);
                    if(x.get() > startX + 96){
                        x.set(startX + 22);
                        y.addAndGet(24);
                    }
        });
    }

    /**
     * Returns the middle of the screen.
     * @return
     */
    public int getStartX(){
        return (this.width/2)-126;
    }

    /**
     * Returns the middle of the screen.
     * @return
     */
    public int getStartY(){
        return (this.height/2)-126;
    }

    /**
     * Called to set and update the screen to a new skill page.
     * @param page
     */
    public void updatePage(SkillPage page){
        updatePage(page, 0);
    }

    /**
     * Called to set and update the screen to a new skill page.
     * @param page
     * @param scrollAmount
     */
    public void updatePage(SkillPage page, double scrollAmount){
        currentPage = page;
        SCROLL_BAR.setScrollAmount(scrollAmount);
    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTicks) {
        setTooltipInfo(null);
        int startX = (this.width/2)-126;
        int startY = (this.height/2)-126;
        this.renderBackground(poseStack);
        RenderSystem.setShaderTexture(0, BACKGROUND);
        blit(poseStack, (this.width/2)-126, (this.height/2)-126, 0, 0, 256, 256);

        this.minecraft.font.draw(poseStack, Component.translatable("gui.skillingbook.text.header.skills")
                .withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.UNDERLINE), startX + 22, startY + 20, 1);

        super.render(poseStack, mouseX, mouseY, partialTicks);

        if(currentPage != null){
            startX += 16 + 126;
            startY += 16;
            this.minecraft.font.draw(poseStack,
                    Component.translatable(currentPage.skill().getUnlocalizedName().toString()).withStyle(ChatFormatting.DARK_AQUA, ChatFormatting.UNDERLINE),
                    startX, startY + 4, 0xFFFFFF);
        }

        if(tooltipInfo != null){
            this.renderTooltip(poseStack, tooltipInfo.tooltipLines, tooltipInfo.tooltipImage, mouseX, mouseY);
        }
    }

    private TooltipInfo tooltipInfo;
    public void setTooltipInfo(TooltipInfo info) {
        tooltipInfo = info;
    }

    public static class TooltipInfo{
        public List<Component> tooltipLines = new ArrayList<>();
        public Optional<TooltipComponent> tooltipImage = ItemStack.EMPTY.getTooltipImage();
    }
}
