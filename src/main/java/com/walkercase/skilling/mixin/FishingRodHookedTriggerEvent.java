package com.walkercase.skilling.mixin;

import com.walkercase.skilling.event.ItemFishedEvent;
import net.minecraft.advancements.critereon.FishingRodHookedTrigger;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Collection;

@Mixin(FishingRodHookedTrigger.class)
public abstract class FishingRodHookedTriggerEvent extends SimpleCriterionTrigger<FishingRodHookedTrigger.TriggerInstance> {

    @Inject(method = "trigger", at = @At(value = "HEAD", target = "Lnet/minecraft/advancements/critereon/FishingRodHookedTrigger;trigger(Lnet/minecraft/server/level/ServerPlayer;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/entity/projectile/FishingHook;Ljava/util/Collection;)V"), cancellable = true)
    public void trigger(ServerPlayer player, ItemStack is, FishingHook hook, Collection<ItemStack> drops, CallbackInfo ci) {
        if(!player.level.isClientSide){
            ItemFishedEvent event = new ItemFishedEvent(player, is, hook, drops);
            MinecraftForge.EVENT_BUS.post(event);
        }
    }
}
