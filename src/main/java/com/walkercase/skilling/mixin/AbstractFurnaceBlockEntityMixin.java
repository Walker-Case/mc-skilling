package com.walkercase.skilling.mixin;

import com.walkercase.skilling.event.FurnaceTickEvent;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.MinecraftForge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractFurnaceBlockEntity.class)
public class AbstractFurnaceBlockEntityMixin {
    @Inject(method = "serverTick", at = @At(value = "HEAD", target = "Lnet/minecraft/world/level/block/entity/AbstractFurnaceBlockEntity;serverTick(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/block/entity/AbstractFurnaceBlockEntity;)V"), cancellable = true)
    private static void serverTick(Level level, BlockPos blockPos, BlockState blockState, AbstractFurnaceBlockEntity abstractFurnaceBlockEntity, CallbackInfo ci) {
        if(!level.isClientSide){
            ItemStack itemstack0 = abstractFurnaceBlockEntity.items.get(0);
            ItemStack itemstack1 = abstractFurnaceBlockEntity.items.get(1);
            ItemStack itemstack2 = abstractFurnaceBlockEntity.items.get(2);
            Recipe<?> recipe = abstractFurnaceBlockEntity.quickCheck.getRecipeFor(abstractFurnaceBlockEntity, level).orElse(null);
            FurnaceTickEvent event = new FurnaceTickEvent(level, blockPos, blockState, abstractFurnaceBlockEntity, itemstack0, itemstack1, itemstack2, recipe);

            MinecraftForge.EVENT_BUS.post(event);

            abstractFurnaceBlockEntity.items.set(0, event.top);
            abstractFurnaceBlockEntity.items.set(1, event.fuel);
            abstractFurnaceBlockEntity.items.set(2, event.result);

            if(event.isCanceled()){
                if (abstractFurnaceBlockEntity.isLit()) {
                    --abstractFurnaceBlockEntity.litTime;
                }
                ci.cancel();
            }
        }
    }
}
