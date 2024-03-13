package com.walkercase.skilling.api.action.skill;

import com.walkercase.skilling.api.action.ActionData;
import com.walkercase.skilling.api.action.ActionIcon;
import com.walkercase.skilling.client.gui.component.SkillPage;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

import java.util.ArrayList;

/**
 * The main skill class. Immutable.
 */
public class Skill {

    public final ResourceLocation name;
    public final ActionIcon icon;
    public final int maxLevel;
    public final int[] primaryFireworkColor;
    public final int[] secondaryFireworkColor;
    public final double xpModifier;
    private final ArrayList<ActionData.Requirement> requirements = new ArrayList<>();

    /**
     * Create a new skill. Note that skills are immutable.
     * @param name
     * @param icon
     * @param maxLevel
     */
    public Skill(ResourceLocation name, ActionIcon icon, int maxLevel){
        this(name, icon, 1, maxLevel, null, null);
    }

    /**
     * Create a new skill. Note that skills are immutable.
     * @param name
     * @param icon
     * @param maxLevel
     * @param primaryFireworkColor
     * @param secondaryFireworkColor
     */
    public Skill(ResourceLocation name, ActionIcon icon, double xpModifier, int maxLevel, int[] primaryFireworkColor, int[] secondaryFireworkColor){
        this.name = name;
        this.icon = icon;
        this.maxLevel = maxLevel;
        this.primaryFireworkColor = primaryFireworkColor;
        this.secondaryFireworkColor = secondaryFireworkColor;
        this.xpModifier = xpModifier;
    }

    /**
     * Returns true if the given player meets this skills requirements.
     * @param player
     * @return
     */
    public boolean meetsRequirements(Player player){
        if(getRequirements() != null) {
            for (ActionData.Requirement requirement : getRequirements()) {
                if (SkillManager.getLevel(player, requirement.skill()) < requirement.minLevel()) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Called to render this skills book page.
     *
     * @return
     */
    public SkillPage renderBookPage(){
        final Skill skill = this;
        return () -> skill;
    }

    /**
     * Returns this skills unlocalized name.
     *
     * @return
     */
    public ResourceLocation getUnlocalizedName(){
        return this.name;
    }

    /**
     * Returns this skills max level.
     *
     * @return
     */
    public int getMaxLevel(){
        return  this.maxLevel;
    }



    /**
     * Returns the item to use as this skills icon.
     *
     * @return
     */
    public ActionIcon getItemIcon(){
        return this.icon;
    }

    /**
     * Returns how much xp makes up a given level.
     *
     * @param level
     * @return
     */
    public double getXpForLevel(int level) {
        //.64 as you can make up to 64 items at a time.
        //.16 for additional change.
        return level * (0.64d + 0.16d + xpModifier);
    }

    /**
     * Returns the primary firework colors to use when leveling up.
     *
     * @return
     */
    public int[] getPrimaryFireworkColor() {
        return new int[]{0x04781B, 0x2AAA44};
    }

    /**
     * Returns the fade firework colors to use when leveling up.
     *
     * @return
     */
    public int[] getFadeFireworkColor() {
        return new int[]{0x84DF96};
    }

    /**
     * Adds the given requirements to this skill.
     * @param requirements
     */
    public void addRequirements(ArrayList<ActionData.Requirement> requirements) {
        this.requirements.addAll(requirements);
    }

    /**
     * Reads this skills current requirements.
     * @return
     */
    public ActionData.Requirement[] getRequirements(){
        return requirements.toArray(new ActionData.Requirement[0]);
    }
}
