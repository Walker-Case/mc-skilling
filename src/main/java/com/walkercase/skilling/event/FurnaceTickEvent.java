package com.walkercase.skilling.event;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.eventbus.api.Cancelable;
import net.minecraftforge.eventbus.api.Event;

/**
 * Called when a furnace is being ticked.
 * This event is only called on the server.
 */
@Cancelable
public class FurnaceTickEvent extends Event {

    public final Level level;
    public final BlockPos blockPos;
    public final BlockState blockState;
    public final Recipe<?> recipe;
    public ItemStack top;
    public ItemStack fuel;
    public ItemStack result;
    public AbstractFurnaceBlockEntity blockEntity;

    public FurnaceTickEvent(Level level, BlockPos blockPos, BlockState blockState, AbstractFurnaceBlockEntity abstractFurnaceBlockEntity, ItemStack itemstack0, ItemStack itemstack1, ItemStack itemstack2, Recipe<?> recipe) {
        this.level = level;
        this.blockPos = blockPos;
        this.blockState = blockState;
        this.top = itemstack0;
        this.fuel = itemstack1;
        this.result = itemstack2;
        this.blockEntity = abstractFurnaceBlockEntity;
        this.recipe = recipe;
    }
}
