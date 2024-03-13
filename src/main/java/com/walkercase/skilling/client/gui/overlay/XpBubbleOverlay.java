package com.walkercase.skilling.client.gui.overlay;

import com.mojang.blaze3d.vertex.PoseStack;
import com.walkercase.skilling.api.action.skill.Skill;
import com.walkercase.skilling.client.gui.component.FireworkComponent;
import com.walkercase.skilling.client.gui.component.SkillIconDisplayWidget;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;
import org.joml.Vector2d;

import java.util.ArrayList;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

public class XpBubbleOverlay implements IGuiOverlay {
    private final ArrayList<SkillIconDisplayWidget> XP_RENDER_LIST = new ArrayList<>();
    private final ArrayList<FireworkComponent> FIREWORK_LIST = new ArrayList<>();
    private static final float BUBBLE_DURATION = 2000;
    private static final int BUBBLE_PADDING = 2;

    public synchronized void addXpGain(Skill skill, int currentLevel, double currentXp) {
        Optional<SkillIconDisplayWidget> opt = XP_RENDER_LIST.stream().filter(b -> b.skill == skill).findFirst();

        if (opt.isPresent()) {
            SkillIconDisplayWidget bubble = opt.get();
            bubble.currentXp = currentXp;
            if (currentLevel != bubble.currentLevel) {
                bubble.doLevelUp(currentLevel);

                FireworkComponent comp = new FireworkComponent(-1, -1, -1, -1, Component.empty());
                FIREWORK_LIST.add(comp);
                Minecraft.getInstance().player.playSound(SoundEvents.FIREWORK_ROCKET_LAUNCH);
            }
            bubble.entryTime = System.currentTimeMillis();
        } else {
            XP_RENDER_LIST.add(new SkillIconDisplayWidget(0, 0, skill, currentLevel, currentXp, null, false));
        }
    }

    @Override
    public void render(ForgeGui gui, PoseStack poseStack, float partialTick, int screenWidth, int screenHeight) {
        ArrayList<SkillIconDisplayWidget> removal = new ArrayList<>();
        AtomicInteger currX = new AtomicInteger(BUBBLE_PADDING);
        AtomicInteger currY = new AtomicInteger(BUBBLE_PADDING);

        ArrayList<FireworkComponent> fireworkRemovalList = new ArrayList<>();
        FIREWORK_LIST.forEach(fireworkComponent -> {
            if(fireworkComponent.endVec.x == -1 && fireworkComponent.endVec.y == -1){
                Random rand = new Random();
                int newx =  ((screenWidth / 2) + ((rand.nextBoolean() ? 1 : -1) * rand.nextInt(64)));
                int newy = ((screenHeight / 2) + ((rand.nextBoolean() ? 1 : -1) * rand.nextInt(64)));

                fireworkComponent.startVec = new Vector2d(0, rand.nextInt(screenHeight/2));
                fireworkComponent.endVec = new Vector2d(newx, newy);
            }
            if(fireworkComponent.isDead())
                fireworkRemovalList.add(fireworkComponent);
            fireworkComponent.renderWidget(poseStack, screenWidth, screenHeight, partialTick);
        });
        FIREWORK_LIST.removeAll(fireworkRemovalList);

        XP_RENDER_LIST.forEach((skillIconDisplayWidget) -> {
            skillIconDisplayWidget.setX(currX.get());
            skillIconDisplayWidget.setY(currY.get());
            skillIconDisplayWidget.render(poseStack, currX.get(), currY.get());

            int x = currX.addAndGet(16);
            if (x > 16) {
                currX.set(BUBBLE_PADDING);
                currY.addAndGet(16);
            }

            long diff = System.currentTimeMillis() - skillIconDisplayWidget.entryTime;
            float percent = Math.max(1, (float) diff / BUBBLE_DURATION);
            if (percent > 1)
                removal.add(skillIconDisplayWidget);
        });

        synchronized (XP_RENDER_LIST) {
            removal.forEach(XP_RENDER_LIST::remove);
        }
    }
}
