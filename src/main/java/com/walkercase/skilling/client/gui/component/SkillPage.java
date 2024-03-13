package com.walkercase.skilling.client.gui.component;

import com.walkercase.skilling.api.action.ActionAPI;
import com.walkercase.skilling.api.action.ActionData;
import com.walkercase.skilling.api.action.ActionItem;
import com.walkercase.skilling.api.action.skill.Skill;
import com.walkercase.skilling.client.event.SkillPageRequirements;
import net.minecraftforge.common.MinecraftForge;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public interface SkillPage {

    Skill skill();
    default SkillPageWidget[] requirements(){
        ArrayList<SkillPageWidget> ARR = new ArrayList<>();

        for (ActionItem action : ActionAPI.getActionItems()) {
            populateFor(skill(), action, action.actions, ARR);
        }

        SkillPageRequirements event = new SkillPageRequirements(skill());
        MinecraftForge.EVENT_BUS.post(event);

        SkillPageWidget[] list = event.getEntries();
        if(list != null){
            ARR.addAll(List.of(list));
        }

        return ARR.toArray(new SkillPageWidget[0]);
    }

    private static void populateFor(Skill skill, ActionItem actionItem, ActionData[] actionData, ArrayList<SkillPageWidget> list){
            SkillPageWidget spw = new SkillPageWidget(actionItem);

            boolean readActionData = false;
            for(ActionData action : actionData){
                if(action.requirements != null){
                    if(Arrays.stream(action.requirements).anyMatch(x-> x.skill() == skill)){
                        for (ActionData.Requirement requirement : action.requirements) {
                            readActionData = true;
                        }
                    }
                }
            }

            if(readActionData)
                for(ActionData action : actionData)
                    if(action.requirements != null) {
                        for (ActionData.Requirement requirement : action.requirements)
                            spw.addRequirement(requirement);
                    }

            if(!spw.requirements.isEmpty())
                list.add(spw);
    }


}