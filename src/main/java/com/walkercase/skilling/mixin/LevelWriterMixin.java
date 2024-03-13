package com.walkercase.skilling.mixin;

import com.walkercase.skilling.event.EntityAddedToLevelEvent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.LevelWriter;
import net.minecraftforge.common.MinecraftForge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(LevelWriter.class)
public interface LevelWriterMixin {

    /**
     * @author
     * @reason
     */
    @Overwrite
    default boolean addFreshEntity(Entity entity) {
        EntityAddedToLevelEvent event = new EntityAddedToLevelEvent(entity);
        MinecraftForge.EVENT_BUS.post(event);
        return false;
    }
}
