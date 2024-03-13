package com.walkercase.skilling.mixin;

import com.walkercase.skilling.event.EnchantmentTableListEvent;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.EnchantmentMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.common.MinecraftForge;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(EnchantmentMenu.class)
public abstract class EnchantmentMenuMixin extends AbstractContainerMenu {

    @Shadow @Final private ContainerLevelAccess access;

    @Shadow @Final public int[] enchantClue;
    @Shadow @Final public int[] costs;
    @Shadow @Final public int[] levelClue;

    @Unique
    private int _1_19_4$currentPlayer = -1;

    protected EnchantmentMenuMixin(@Nullable MenuType<?> p_38851_, int p_38852_) {
        super(p_38851_, p_38852_);
    }

    @Inject(method = "getEnchantmentList", at = @At(value = "RETURN", target = "Lnet/minecraft/world/inventory/EnchantmentMenu;getEnchantmentList(Lnet/minecraft/world/item/ItemStack;II)Ljava/util/List;"), cancellable = true)
    private void getEnchantmentList(ItemStack p_39472_, int slot, int amplifier, CallbackInfoReturnable<List<EnchantmentInstance>> cir) {
        this.access.execute((level, pos)->{
            if(!level.isClientSide){
                if(level.getEntity(_1_19_4$currentPlayer) instanceof Player player){
                    List<EnchantmentInstance> list = cir.getReturnValue();
                    EnchantmentTableListEvent event = new EnchantmentTableListEvent(player, p_39472_, slot, amplifier, list);
                    MinecraftForge.EVENT_BUS.post(event);
                    if(event.isCanceled()){
                        list.clear();
                    }
                }else{
                    _1_19_4$currentPlayer = -1;
                }
            }
        });
    }

    @Inject(method = "stillValid", at = @At(value = "RETURN", target = "Lnet/minecraft/world/inventory/EnchantmentMenu;stillValid(Lnet/minecraft/world/entity/player/Player;)Z"), cancellable = true)
    public void stillValid(Player player, CallbackInfoReturnable<Boolean> cir) {
        if(!player.level.isClientSide){
            boolean canUse = false;

            if(_1_19_4$currentPlayer == -1){
                _1_19_4$currentPlayer = player.getId();
                canUse = true;
            }else{
                if(player.level.getEntity(_1_19_4$currentPlayer) instanceof Player player1){
                    if(player1.getId() == player.getId() && player.containerMenu instanceof EnchantmentMenu){
                        canUse = true;
                    }else{
                        canUse = false;
                    }
                }else{
                    _1_19_4$currentPlayer = player.getId();
                    canUse = true;
                }
            }

            cir.setReturnValue(canUse && stillValid(this.access, player, Blocks.ENCHANTING_TABLE));
        }
    }
}