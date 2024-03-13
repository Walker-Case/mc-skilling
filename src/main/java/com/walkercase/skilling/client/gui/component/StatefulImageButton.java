package com.walkercase.skilling.client.gui.component;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.walkercase.skilling.client.gui.screen.SkillingBookScreen;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.apache.commons.lang3.ArrayUtils;
import org.jetbrains.annotations.NotNull;

/**
 * Adds statefulness to the ImageButton.
 */
public class StatefulImageButton extends ImageButton {

    public record State(ResourceLocation image, int u, int v, int textureWidth, int textureHeight){}
    public record Background(ResourceLocation image, int u, int v, int highlightU, int highlightV, int textureWidth, int textureHeight){}

    private int currentState = 0;
    private final State[] availableStates;
    private Background background;
    private final SkillingBookScreen screen;

    /**
     * Create a new StatefulImageButton.
     * @param x
     * @param y
     * @param w
     * @param h
     * @param u
     * @param v
     * @param states
     * @param p_169018_
     */
    public StatefulImageButton(SkillingBookScreen screen, int x, int y, int w, int h, int u, int v, State[] states, Background background, OnPress p_169018_) {
        super(x, y, w, h, u, v, ArrayUtils.isEmpty(states) ? new ResourceLocation("") : states[0].image, p_169018_);
        this.availableStates = states;
        this.background = background;
        this.screen = screen;
    }

    /**
     * Sets the current background.
     * @param background
     */
    public void setBackground(Background background){
        this.background = background;
    }

    /**
     * Returns the current background.
     * @return
     */
    public Background getBackground(){
        return this.background;
    }

    /**
     * Returns the current state index.
     * @return
     */
    public int getCurrentState(){
        return this.currentState;
    }

    /**
     * Returns the list of available states.
     * @return
     */
    public State[] getAvailableStates(){
        return this.availableStates;
    }

    /**
     * Returns the count of available states.
     * @return
     */
    public int getStateCount(){
        return availableStates.length;
    }

    /**
     * Sets this buttons current state.
     * @param index
     */
    public void setCurrentState(int index){
        this.currentState = index;
    }

    @Override
    public void renderWidget(@NotNull PoseStack p_268099_, int p_267992_, int p_267950_, float p_268076_) {
        State state = availableStates[currentState];
        RenderSystem.setShaderTexture(0, background.image);
        blit(p_268099_, this.getX(), this.getY(),
                this.isHovered ? background.highlightU : background.u,
                this.isHovered ? background.highlightV : background.v,
                this.width, this.height, background.textureHeight, background.textureWidth);

        RenderSystem.setShaderTexture(0, state.image);
        blit(p_268099_, this.getX(), this.getY(), state.u, state.v, this.width, this.height, state.textureHeight, state.textureWidth);

        if(this.isHovered){
            SkillingBookScreen.TooltipInfo ti = new SkillingBookScreen.TooltipInfo();
            ti.tooltipLines.add(Component.translatable("gui.skillingbook.show_all"));
            screen.setTooltipInfo(ti);
        }
    }
}
