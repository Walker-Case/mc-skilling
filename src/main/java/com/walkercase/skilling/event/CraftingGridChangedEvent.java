package com.walkercase.skilling.event;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.inventory.ResultContainer;
import net.minecraft.world.level.Level;
import net.minecraftforge.eventbus.api.Cancelable;
import net.minecraftforge.eventbus.api.Event;

/**
 * Called when a crafting grid is changed.
 * This event is fired on the MinecraftForge event bus and only on the server side.
 *
 * This event is cancellable.
 */
@Cancelable
public class CraftingGridChangedEvent extends Event {

    public final AbstractContainerMenu menu;
    public final Level level;
    public final Player player;
    public final CraftingContainer craftingContainer;
    public final ResultContainer resultContainer;

    /**
     * Called when a crafting grid is changed.
     * This event is fired on the MinecraftForge event bus and only on the server side.
     * @param menu
     * @param level
     * @param player
     * @param craftingContainer
     * @param resultContainer
     */
    public CraftingGridChangedEvent(AbstractContainerMenu menu, Level level, Player player, CraftingContainer craftingContainer, ResultContainer resultContainer){
        this.menu = menu;
        this.level = level;
        this.player = player;
        this.craftingContainer = craftingContainer;
        this.resultContainer = resultContainer;
    }

}
