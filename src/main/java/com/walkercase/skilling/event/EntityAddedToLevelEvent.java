package com.walkercase.skilling.event;

import net.minecraft.world.entity.Entity;
import net.minecraftforge.eventbus.api.Event;

/**
 * Called when an entity is being added to the level.
 */
public class EntityAddedToLevelEvent extends Event {

    public final Entity entity;
    public EntityAddedToLevelEvent(Entity entity){
        this.entity = entity;
    }

}
