package com.walkercase.skilling.mixin;

import com.walkercase.skilling.event.MapUpdatedEvent;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import net.minecraftforge.common.MinecraftForge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(MapItemSavedData.class)
public class MapItemSavedDataMixin {

    @Shadow
    public List<MapItemSavedData.HoldingPlayer> carriedBy;

    @Shadow
    public byte[] colors;

    @Inject(method = "updateColor", at = @At(value = "HEAD", target = "Lnet/minecraft/world/level/saveddata/maps/MapItemSavedData;updateColor(IIB)Z"), cancellable = true)
    public void updateColor(int mapChunkX, int mapChunkY, byte color, CallbackInfoReturnable<Boolean> cir) {
        byte b0 = this.colors[mapChunkX + mapChunkY * 128];
        if(b0 != color){
            carriedBy.forEach((data)->{
                if(!data.player.level.isClientSide){
                    MapUpdatedEvent event = new MapUpdatedEvent(data.player, mapChunkX, mapChunkY, color);
                    MinecraftForge.EVENT_BUS.post(event);
                    if(event.isCanceled()){
                        cir.setReturnValue(false);
                    }
                }
            });
        }
    }
}
