package com.walkercase.skilling.event;

import com.walkercase.skilling.api.action.skill.Skill;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.eventbus.api.Cancelable;
import net.minecraftforge.eventbus.api.Event;

/**
 * Called before the player is about to receive experience in a given skill.
 */
@Cancelable
public class AddPlayerXpEvent extends Event {

    public final Player player;
    public final Skill skill;
    public double xp;
    public AddPlayerXpEvent(Player player, Skill skill, double xp){
        this.player = player;
        this.skill = skill;
        this.xp = xp;
    }
}
