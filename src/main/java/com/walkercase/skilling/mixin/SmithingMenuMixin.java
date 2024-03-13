package com.walkercase.skilling.mixin;

import com.google.common.collect.ImmutableList;
import com.walkercase.skilling.event.SmithingMenuChangedEvent;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SmithingRecipe;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.MinecraftForge;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import javax.annotation.Nullable;
import java.util.List;

@Mixin(SmithingMenu.class)
public abstract class SmithingMenuMixin extends ItemCombinerMenu{

    @Shadow
    @Final
    public final Level level = null;

    @Nullable
    @Shadow
    public SmithingRecipe selectedRecipe;


    public SmithingMenuMixin(@org.jetbrains.annotations.Nullable MenuType<?> p_39773_, int p_39774_, Inventory p_39775_, ContainerLevelAccess p_39776_) {
        super(p_39773_, p_39774_, p_39775_, p_39776_);
    }

    @SuppressWarnings("ConstantConditions")
    @Inject(method = "createResult", at = @At(value = "RETURN", target = "Lnet/minecraft/world/inventory/SmithingMenu;createResult()V"), cancellable = true)
    public void createResult(CallbackInfo ci) {
        if(this.level != null && !this.level.isClientSide){
            Player player = this.player;
            Level level = this.level;
            List<SmithingRecipe> list = level.getRecipeManager().getRecipesFor(RecipeType.SMITHING, this.inputSlots, this.level);
            ImmutableList<SmithingRecipe> recipeList = ImmutableList.copyOf(list.toArray(new SmithingRecipe[0]));
            
            ItemStack result = this.resultSlots.getItem(0);
            ItemStack slot1 = this.inputSlots.getItem(0);
            ItemStack slot2 = this.inputSlots.getItem(1);
            ItemStack slot3 = this.inputSlots.getItem(2);

            SmithingMenuChangedEvent event = new SmithingMenuChangedEvent(level, player, recipeList, slot1, slot2, slot3, result);
            MinecraftForge.EVENT_BUS.post(event);

            this.inputSlots.setItem(0, event.slot1);
            this.inputSlots.setItem(1, event.slot2);
            this.inputSlots.setItem(2, event.slot3);
            this.inputSlots.setItem(0, event.result);

            if(event.isCanceled())
                event.result = ItemStack.EMPTY;
        }

    }

    @Override
    protected ItemCombinerMenuSlotDefinition createInputSlotDefinitions() {
        return null;
    }

}
