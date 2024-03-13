package com.walkercase.skilling.event;

import com.walkercase.skilling.api.action.ActionItem;
import net.minecraftforge.eventbus.api.Cancelable;
import net.minecraftforge.eventbus.api.Event;

/**
 * Called after an action has been loaded but before it is registered.
 */
@Cancelable
public class ActionLoadedEvent extends Event {

    public final ActionItem item;
    public ActionLoadedEvent(ActionItem item){
        this.item = item;
    }

}
