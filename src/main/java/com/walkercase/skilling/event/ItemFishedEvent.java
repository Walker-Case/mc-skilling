package com.walkercase.skilling.event;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.Event;

import java.util.Collection;

/**
 * Called when an item is about to be fished. Allows for drops modification.
 * This event is only called on the server side.
 * Uses the MinecraftForge event bus.
 */
public class ItemFishedEvent extends Event {

    public final ServerPlayer player;
    public final ItemStack itemStack;
    public final FishingHook hook;
    public final Collection<ItemStack> drops;
    public ItemFishedEvent(ServerPlayer player, ItemStack is, FishingHook hook, Collection<ItemStack> drops){
        this.player = player;
        this.itemStack = is;
        this.hook = hook;
        this.drops = drops;
    }

}
