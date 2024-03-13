package com.walkercase.skilling.event;

import com.walkercase.skilling.api.action.skill.Skill;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.eventbus.api.Cancelable;
import net.minecraftforge.eventbus.api.Event;

/**
 * Called before the players level is about to change.
 * Fired on both the client and server side.
 */
@Cancelable
public class PlayerLevelSetEvent extends Event {

    public final Player player;
    public final Skill skill;
    public int level;
    public PlayerLevelSetEvent(Player player, Skill skill, int level){
        this.player = player;
        this.skill = skill;
        this.level = level;
    }
}
