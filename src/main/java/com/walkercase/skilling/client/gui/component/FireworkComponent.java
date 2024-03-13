package com.walkercase.skilling.client.gui.component;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.walkercase.skilling.Skilling;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import org.joml.Vector2d;

import java.util.ArrayList;
import java.util.Random;

import static com.walkercase.skilling.client.gui.component.SkillIconDisplayWidget.ATLAS;

public class FireworkComponent extends AbstractWidget implements Renderable {

    public Vector2d startVec;
    public Vector2d endVec;
    private long startTime;
    private long timeToTravel = 500;
    private int popCount = 50;
    private int popSpread = 128;
    private boolean popped = false;
    private long popTTL = 2000;
    private long popStartTime;
    private boolean isDead = false;
    private ArrayList<SinglePixel> poppedList = new ArrayList<>();

    public FireworkComponent(int startX, int startY, int endX, int endY, Component p_93633_) {
        super(startX, startY, 16, 16, p_93633_);

        startVec = new Vector2d(startX, startY);
        endVec = new Vector2d(endX, endY);
        startTime = System.currentTimeMillis();
    }

    @Override
    public void renderWidget(PoseStack poseStack, int p_268034_, int p_268009_, float p_268085_) {
        if(isDead)
            return;

        if(!popped){
            long end = startTime+timeToTravel;
            long now = System.currentTimeMillis();
            double percentTraveled =  (double) Math.round((1 - ((double) (((end - now) * 100) / (end - startTime))) / 100) * 100) / 100;
            Vector2d newPos = new Vector2d(startVec).lerp(endVec, percentTraveled);

            Skilling.LOGGER.info(percentTraveled + "%" + "(" + startVec.x + "," + startVec.y + ") (" + endVec.x + "," + endVec.y + ") newPos: " + newPos.x + "," + newPos.y);

            double angle = 90 + startVec.angle(endVec);

            poseStack.pushPose();

            RenderSystem.setShaderTexture(0, ATLAS);
            poseStack.translate(newPos.x, newPos.y, 0);
            poseStack.mulPose(Axis.ZP.rotation((float)angle));
            GuiComponent.blit(poseStack,0, 0, 16, 32, 16, 16, 256, 256);
            if(percentTraveled >= 1.0d)
                pop();

            poseStack.popPose();
        }else{
            long end = popStartTime + popTTL;
            long now = System.currentTimeMillis();
            double ttl =  1 - ((double)(((end-now) * 100)/(end-popStartTime)))/100;

            for (SinglePixel pixel : this.poppedList) {
                poseStack.pushPose();
                Vector2d newPos = new Vector2d(pixel.startVec).lerp(pixel.endVec, ttl);

                float[] prevColor = RenderSystem.getShaderColor();
                RenderSystem.setShaderColor((float)pixel.r, (float)pixel.g, (float)pixel.b, 1);

                RenderSystem.setShaderTexture(0, ATLAS);
                GuiComponent.blit(poseStack, (int)newPos.x(), (int)newPos.y(), 16 + (16 * pixel.texIndex), 49 + (ttl > 0.5d ? 16 : 0), 16, 16, 256, 256);

                RenderSystem.setShaderColor(prevColor[0], prevColor[1], prevColor[2], prevColor[3]);
                poseStack.popPose();
            }

            if(ttl >= 1){
                this.isDead = true;
            }
        }
    }

    public boolean isDead(){
        return this.isDead;
    }

    private static final Random RANDOM = new Random();
    private void pop(){
        this.popped = true;
        this.popStartTime = System.currentTimeMillis();
        int textureIndex = RANDOM.nextInt(3);

        Minecraft.getInstance().player.playSound(SoundEvents.FIREWORK_ROCKET_BLAST);

        double r = RANDOM.nextDouble();
        double g = RANDOM.nextDouble();
        double b = RANDOM.nextDouble();

        double r1 = RANDOM.nextDouble();
        double g1 = RANDOM.nextDouble();
        double b1 = RANDOM.nextDouble();
        //populate sprite array
        for(int i=0;i<this.popCount;i++){
            int newX = ((RANDOM.nextBoolean() ? 1 : -1) *  RANDOM.nextInt(popSpread)) + (int)this.endVec.x();
            int newY = ((RANDOM.nextBoolean() ? 1 : -1) *  RANDOM.nextInt(popSpread)) + (int)this.endVec.y();

            int newXX = ((RANDOM.nextBoolean() ? 1 : -1) *  RANDOM.nextInt(8)) + newX;
            int newYY = ((RANDOM.nextBoolean() ? 1 : -1) *  RANDOM.nextInt(8)) + newY;

            double r2 = RANDOM.nextBoolean() ? r : r1;
            double g2 = RANDOM.nextBoolean() ? g : g1;
            double b2 = RANDOM.nextBoolean() ? b : b1;
            this.poppedList.add(new SinglePixel(textureIndex, new Vector2d(newX, newY), new Vector2d(newXX, newYY), r2, g2, b2));
        }
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput p_259858_) {

    }

    record SinglePixel(int texIndex, Vector2d startVec, Vector2d endVec, double r, double g, double b){}
}
