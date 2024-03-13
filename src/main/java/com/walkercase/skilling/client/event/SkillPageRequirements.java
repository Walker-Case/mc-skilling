package com.walkercase.skilling.client.event;

import com.walkercase.skilling.api.action.skill.Skill;
import com.walkercase.skilling.client.gui.component.SkillPageWidget;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.eventbus.api.Event;

import java.util.ArrayList;
import java.util.List;

/**
 * Allows adding arbitrary entries to skill book pages.
 * Only fired on the client side.
 */
@OnlyIn(Dist.CLIENT)
public class SkillPageRequirements extends Event {

    public final Skill skill;
    private final ArrayList<SkillPageWidget> entries = new ArrayList<>();
    public SkillPageRequirements(Skill skill){
        this.skill = skill;
    }

    public synchronized void addEntry(SkillPageWidget... widgets){
        entries.addAll(List.of(widgets));
    }

    public SkillPageWidget[] getEntries(){
        return entries.toArray(new SkillPageWidget[0]);
    }

}
