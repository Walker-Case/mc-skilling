package com.walkercase.skilling.event;

import net.minecraft.world.entity.player.Player;
import net.minecraftforge.eventbus.api.Cancelable;
import net.minecraftforge.eventbus.api.Event;

/**
 * Called when a player updates a map item.
 * This is called on the server only.
 */
@Cancelable
public class MapUpdatedEvent extends Event {

    public final Player player;
    public final int mapChunkX;
    public final int mapChunkY;
    public final byte color;
    public MapUpdatedEvent(Player player, int mapChunkX, int mapChunkY, byte color){
        this.player = player;
        this.mapChunkX = mapChunkX;
        this.mapChunkY = mapChunkY;
        this.color = color;
    }

}
