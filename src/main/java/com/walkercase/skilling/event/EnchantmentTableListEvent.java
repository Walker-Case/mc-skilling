package com.walkercase.skilling.event;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import net.minecraftforge.eventbus.api.Cancelable;
import net.minecraftforge.eventbus.api.Event;

import java.util.List;

/**
 * Fired on EnchantmentMenu#getEnchantmentList.
 * Allows for dynamic enchantment selection changes.
 * Only fired on the server.
 */
@Cancelable
public class EnchantmentTableListEvent extends Event {

    public final Player player;
    public final ItemStack itemStack;
    public final int slot;
    public int amplifier;
    public final List<EnchantmentInstance> list;
    public EnchantmentTableListEvent(Player player, ItemStack itemStack, int slot, int amplifier, List<EnchantmentInstance> list){
        this.player = player;
        this.itemStack = itemStack;
        this.slot = slot;
        this.amplifier = amplifier;
        this.list = list;
    }
}
