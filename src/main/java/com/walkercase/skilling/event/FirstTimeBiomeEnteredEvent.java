package com.walkercase.skilling.event;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.biome.Biome;
import net.minecraftforge.eventbus.api.Event;

/**
 * Called the first time the player enters a biome.
 * This event is immutable and non-cancellable.
 * Only called on the server.
 */
public class FirstTimeBiomeEnteredEvent extends Event {

    public final Player player;
    public final Biome biome;
    public final ResourceLocation key;
    public FirstTimeBiomeEnteredEvent(Player player, Biome biome, ResourceLocation key){
        this.player = player;
        this.biome = biome;
        this.key = key;
    }

}
