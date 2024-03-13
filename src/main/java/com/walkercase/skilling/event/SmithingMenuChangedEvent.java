package com.walkercase.skilling.event;

import com.google.common.collect.ImmutableList;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.SmithingRecipe;
import net.minecraft.world.level.Level;
import net.minecraftforge.eventbus.api.Event;

/**
 * Called when the smithing menu has been changed.
 * Allows for arbitrary slot modification.
 */
public class SmithingMenuChangedEvent extends Event {

    public final Level level;
    public final Player player;
    public final ImmutableList<SmithingRecipe> recipes;
    public ItemStack slot1;
    public ItemStack slot2;
    public ItemStack slot3;
    public ItemStack result;
    public SmithingMenuChangedEvent(Level level, Player player, ImmutableList<SmithingRecipe> recipeList, ItemStack slot1, ItemStack slot2, ItemStack slot3, ItemStack result){
        this.level = level;
        this.player = player;
        this.recipes = recipeList;
        this.slot1 = slot1;
        this.slot2 = slot2;
        this.slot3 = slot3;
        this.result = result;
    }

}
