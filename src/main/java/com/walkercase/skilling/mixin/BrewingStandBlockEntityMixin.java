package com.walkercase.skilling.mixin;

import com.walkercase.skilling.event.BrewingStandTickEvent;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BrewingStandBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.MinecraftForge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BrewingStandBlockEntity.class)
public class BrewingStandBlockEntityMixin {

    @Inject(method = "serverTick", at = @At(value = "HEAD", target = "Lnet/minecraft/world/level/block/entity/BrewingStandBlockEntity;serverTick(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/block/entity/BrewingStandBlockEntity;)V"), cancellable = true)
    private static void serverTick(Level level, BlockPos pos, BlockState state, BrewingStandBlockEntity brewingStandBlockEntity, CallbackInfo ci) {
        if(!level.isClientSide){
            BrewingStandTickEvent event = new BrewingStandTickEvent(level, pos, state, brewingStandBlockEntity);
            MinecraftForge.EVENT_BUS.post(event);
            if(event.isCanceled()){
                brewingStandBlockEntity.brewTime --;
                ci.cancel();
            }
        }
    }
}