package com.walkercase.skilling.event;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BrewingStandBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.eventbus.api.Event;

/**
 * Fired every tick for the brewing stand.
 */
public class BrewingStandTickEvent extends Event {

    public final Level level;
    public final BlockState blockState;
    public final BrewingStandBlockEntity blockEntity;
    public final BlockPos blockPos;
    public BrewingStandTickEvent(Level level, BlockPos blockPos, BlockState blockState, BrewingStandBlockEntity blockEntity){
        this.level = level;
        this.blockState = blockState;
        this.blockEntity = blockEntity;
        this.blockPos = blockPos;
    }

}
