package com.walkercase.skilling.client.gui.overlay;

import com.walkercase.skilling.Skilling;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Skilling.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class OverlayListener {

    public static final XpBubbleOverlay XP_BUBBLE_OVERLAY = new XpBubbleOverlay();

    @SubscribeEvent
    public static void registerOverlays(RegisterGuiOverlaysEvent event) {
        event.registerAboveAll("skillingxpbubbles", XP_BUBBLE_OVERLAY);
    }

}
