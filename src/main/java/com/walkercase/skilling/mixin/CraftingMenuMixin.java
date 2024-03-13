package com.walkercase.skilling.mixin;

import com.walkercase.skilling.event.CraftingGridChangedEvent;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.inventory.CraftingMenu;
import net.minecraft.world.inventory.ResultContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.MinecraftForge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CraftingMenu.class)
public class CraftingMenuMixin{
    @Inject(method = "slotChangedCraftingGrid", at = @At(value = "RETURN", target = "Lnet/minecraft/world/inventory/CraftingMenu;slotChangedCraftingGrid(Lnet/minecraft/world/inventory/AbstractContainerMenu;Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/inventory/CraftingContainer;Lnet/minecraft/world/inventory/ResultContainer;)V"), cancellable = true)
    private static void slotChangedCraftingGrid(AbstractContainerMenu menu, Level level, Player player, CraftingContainer craftingContainer, ResultContainer resultContainer, CallbackInfo ci) {
        CraftingGridChangedEvent event = new CraftingGridChangedEvent(menu, level, player, craftingContainer, resultContainer);

        if(!level.isClientSide){
            if(MinecraftForge.EVENT_BUS.post(event)){
                resultContainer.setItem(0, new ItemStack(Items.AIR));
            }
        }

    }
}