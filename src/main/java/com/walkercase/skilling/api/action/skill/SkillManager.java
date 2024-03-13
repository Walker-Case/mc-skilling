package com.walkercase.skilling.api.action.skill;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.walkercase.skilling.Skilling;
import com.walkercase.skilling.api.action.ActionData;
import com.walkercase.skilling.api.action.ActionIcon;
import com.walkercase.skilling.event.AddPlayerXpEvent;
import com.walkercase.skilling.event.PlayerLevelSetEvent;
import com.walkercase.skilling.network.NetworkManager;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.FireworkRocketEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.network.PacketDistributor;
import org.codehaus.plexus.util.StringUtils;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;
import java.util.function.Supplier;

import static com.walkercase.skilling.api.action.ActionAPI.readRequirements;

/**
 * Helper class with Skills.
 */
public class SkillManager {

    private static final ArrayList<Skill> SKILLS = new ArrayList<>();

    /**
     * Contains skilling defined skill keys.
     * These may be null in data, if this is the case the method will return NULL_POINTER_SKILL.
     */
    public static class SkillKeys{
        public static final Supplier<Skill> COMBAT_SKILL =
                ()->SkillManager.getSkill(new ResourceLocation(Skilling.MODID, "combat"));
        public static final Supplier<Skill> COOKING_SKILL =
                ()->SkillManager.getSkill(new ResourceLocation(Skilling.MODID, "cooking"));
        public static final Supplier<Skill> CRAFTING_SKILL =
                ()->SkillManager.getSkill(new ResourceLocation(Skilling.MODID, "crafting"));
        public static final Supplier<Skill> MINING_SKILL =
                ()->SkillManager.getSkill(new ResourceLocation(Skilling.MODID, "mining"));
        public static final Supplier<Skill> FARMING_SKILL =
                ()->SkillManager.getSkill(new ResourceLocation(Skilling.MODID, "farming"));
        public static final Supplier<Skill> FISHING_SKILL =
                ()->SkillManager.getSkill(new ResourceLocation(Skilling.MODID, "fishing"));
        public static final Supplier<Skill> FORESTRY_SKILL =
                ()->SkillManager.getSkill(new ResourceLocation(Skilling.MODID, "forestry"));
        public static final Supplier<Skill> SMITHING_SKILL =
                ()->SkillManager.getSkill(new ResourceLocation(Skilling.MODID, "smithing"));
        public static final Supplier<Skill> CONSTRUCTION_SKILL =
                ()->SkillManager.getSkill(new ResourceLocation(Skilling.MODID, "construction"));
        public static final Supplier<Skill> EXPLORING_SKILL =
                ()->SkillManager.getSkill(new ResourceLocation(Skilling.MODID, "exploring"));
        public static final Supplier<Skill> ENGINEERING_SKILL =
                ()->SkillManager.getSkill(new ResourceLocation(Skilling.MODID, "engineering"));

        public static final Supplier<Skill> JEWLELER_PROSPECTING =
                ()->SkillManager.getSkill(new ResourceLocation("jeweler", "prospecting"));
        public static final Skill NULL_POINTER_SKILL = new Skill(new ResourceLocation(Skilling.MODID, "null_pointer_skill"), new ActionIcon(), 1);
    }

    /**
     * Called to load the skills from the given JsonObject.
     * @param obj
     */
    public static void loadSkills(JsonObject obj){
        obj.keySet().forEach(skillKey->{
            try{
                JsonObject skillData = obj.get(skillKey).getAsJsonObject();

                ResourceLocation rl = new ResourceLocation(skillKey);
                int maxLevel = skillData.has("maxLevel") ? skillData.get("maxlevel").getAsInt() : 100;
                double xpModifier = skillData.has("xpModifier") ? skillData.get("xpModifier").getAsDouble() : 1.0d;
                ActionIcon icon = Skilling.getGson().fromJson(skillData.getAsJsonObject("icon"), ActionIcon.class);

                int[] fireworkPrimary = null;
                int[] fireworkSecondary = null;
                if(skillData.has("fireworks")){
                    JsonObject fireworks = skillData.getAsJsonObject("fireworks");
                    if(fireworks.has("primary")){
                        JsonArray primary = fireworks.getAsJsonArray("primary");
                        fireworkPrimary = new int[primary.size()];
                        for(int i=0;i<fireworkPrimary.length;i++){
                            fireworkPrimary[i] = Integer.decode(primary.get(i).getAsString());
                        }

                        if(fireworks.has("secondary")){
                            JsonArray secondary = fireworks.getAsJsonArray("secondary");
                            fireworkSecondary = new int[secondary.size()];
                            for(int i=0;i<fireworkSecondary.length;i++){
                                fireworkSecondary[i] = Integer.decode(secondary.get(i).getAsString());
                            }
                        }
                    }
                }

                registerSkill(new Skill(rl, icon, xpModifier, maxLevel, fireworkPrimary, fireworkSecondary));
            }catch(NullPointerException | NumberFormatException | JsonSyntaxException e){
                Skilling.LOGGER.error("Failed to load skill " + skillKey, e);
            }
        });
    }

    /**
     * Loads skill requirements from the given JsonObject.
     * @param skillsObject
     */
    public static synchronized void loadSkillRequirements(JsonObject skillsObject){
        skillsObject.keySet().forEach(skillKey->{
            Skilling.LOGGER.debug("Loading requirements for skill " + skillKey);
            try{
                JsonObject skillData = skillsObject.get(skillKey).getAsJsonObject();
                ArrayList<ActionData.Requirement> requirements = new ArrayList<>();
                if(skillData.has("requirements")){
                    requirements = readRequirements(skillKey, new ResourceLocation("minecraft", "requirements"), skillData.getAsJsonObject("requirements"));
                }
                SkillManager.getSkill(new ResourceLocation(skillKey)).addRequirements(requirements);;
            }catch(NullPointerException | NumberFormatException | JsonSyntaxException e){
                Skilling.LOGGER.error("Failed to load skill requirements " + skillKey, e);
            }
        });

    }

    /**
     * Called to register a given skill.
     *
     * @param skill ISkill
     */
    public static synchronized void registerSkill(Skill skill) {
        Skilling.LOGGER.info("Registered skill: " + skill.getUnlocalizedName());
        SKILLS.add(skill);
    }

    /**
     * Returns a list of all registered skills.
     *
     * @return list of all registered skills
     */
    public static Skill[] getSkills() {
        return SKILLS.toArray(new Skill[0]);
    }

    /**
     * Returns the Optional containing the skill for the given ResourceLocation.
     *
     * @param name Skill unlocalized name
     * @return ISkill optional
     */
    @Nullable
    public static Skill getSkill(ResourceLocation name) {
        Optional<Skill> opt = Arrays.stream(getSkills()).filter(skill -> skill.getUnlocalizedName().equals(name)).findFirst();
        return opt.orElse(SkillKeys.NULL_POINTER_SKILL);
    }

    /**
     * Adds a level to the player.
     *
     * @param player Player
     * @param skill  ISkill
     * @param amount levels to add
     */
    public static void addLevel(Player player, Skill skill, int amount) {
        setLevel(player, skill, getLevel(player, skill) + amount);

        createFirework(player, skill.getPrimaryFireworkColor(), skill.getFadeFireworkColor());
    }

    /**
     * Called to create a firework at the players location/
     *
     * @param player       Player
     * @param primaryColor Primary firework colors
     * @param fadeColor    Fade firework colors
     */
    private static void createFirework(Player player, int[] primaryColor, int[] fadeColor) {
        if (!player.level.isClientSide) {

            boolean cancelled = false;
            for(double y=player.getY();y<player.getY() + 12;y++){
                if(player.level.getBlockState(new BlockPos((int)player.getX(), (int)y, (int)player.getZ())).getBlock() != Blocks.AIR){
                    cancelled = true;
                    break;
                }
            }

            CompoundTag modTag = SkillManager.getSkillsModNBT(player);
            if(!modTag.contains("lastFirework")){
                modTag.putLong("lastFirework", System.currentTimeMillis() + 5000);
            }
            if(modTag.getLong("lastFirework") < System.currentTimeMillis())
                cancelled = true;


            if(!cancelled){
                modTag.putLong("lastFirework", System.currentTimeMillis() + 5000);

                ItemStack stack = new ItemStack(Items.FIREWORK_ROCKET);
                CompoundTag tag = stack.getOrCreateTag();
                tag.put("Fireworks", new CompoundTag());
                CompoundTag fireworks = tag.getCompound("Fireworks");

                fireworks.put("Explosions", new ListTag());
                ListTag explosions = fireworks.getList("Explosions", 0);
                explosions.add(new CompoundTag());
                CompoundTag exp = explosions.getCompound(0);
                exp.putInt("Type", 2);
                exp.putInt("Trail", 1);
                exp.putIntArray("Colors", primaryColor);
                exp.putIntArray("FadeColors", fadeColor);

                fireworks.putInt("Flight", 1);

                FireworkRocketEntity fireworkrocketentity = new FireworkRocketEntity(player.level, player,
                        player.getX(), player.getY() + 0.15D, player.getZ(), stack);
                player.level.addFreshEntity(fireworkrocketentity);
            }else{
                player.level.playSound(player, player.getX(), player.getY(), player.getZ(), SoundEvents.FIREWORK_ROCKET_LAUNCH, SoundSource.AMBIENT, 3.0F, 1.0F);
            }
        }
    }

    /**
     * Called to set the players skill level.
     *
     * @param player Player
     * @param skill  ISkill
     * @param level  level
     */
    public static void setLevel(Player player, Skill skill, int level) {
        PlayerLevelSetEvent event = new PlayerLevelSetEvent(player, skill, level);
        MinecraftForge.EVENT_BUS.post(event);
        if(!event.isCanceled()){
            CompoundTag tag = getSkillNBT(player, skill);
            tag.putInt("level", event.level);

            sendUpdatePacket(player, skill);
        }
    }

    /**
     * Returns the players current skill for the provided level.
     *
     * @param player Player
     * @param skill  ISkill
     * @return level
     */
    public static int getLevel(Player player, Skill skill) {
        return getSkillNBT(player, skill).getInt("level");
    }

    /**
     * Adds xp to the given player. Automatically levels up.
     * @param player
     * @param actionData
     */
    public static void addXp(Player player, ActionData actionData){
        for(ActionData.Requirement requirement : actionData.requirements){
            if(!SkillManager.playerPassesRequirement(player, requirement))
                return;
        }
        for (ActionData.XpData xpDatum : actionData.xpData) {
            SkillManager.addXp(player, xpDatum.skill(), xpDatum.xp());
        }
    }

    /**
     * Adds xp to the given player. Automatically levels up.
     *
     * @param player Player
     * @param skill  ISkill
     * @param xp     xp amount
     */
    public static void addXp(Player player, Skill skill, double xp) {
        AddPlayerXpEvent event = new AddPlayerXpEvent(player, skill, xp);
        MinecraftForge.EVENT_BUS.post(event);
        if(event.isCanceled())
            return;

        double curr = getXp(player, skill);

        double next = skill.getXpForLevel(getLevel(player, skill) + 1);
        if (getXp(player, skill) + event.xp >= next) {
            setXp(player, skill, 0);
            addLevel(player, skill, 1);
        } else {
            setXp(player, skill, curr + event.xp);
        }

        sendUpdatePacket(player, skill);
    }

    /**
     * Called to set the players skill xp.
     *
     * @param player Player
     * @param skill  ISkill
     * @param xp     xp amount
     */
    public static void setXp(Player player, Skill skill, double xp) {
        CompoundTag tag = getSkillNBT(player, skill);
        tag.putDouble("xp", xp);
    }

    /**
     * Returns the players current xp for the given skill.
     *
     * @param player Player
     * @param skill  ISkill
     * @return xp amount
     */
    public static double getXp(Player player, Skill skill) {
        return getSkillNBT(player, skill).getDouble("xp");
    }

    /**
     * Returns the skill specific CompoundTag.
     *
     * @param player Player
     * @param skill  ISkill
     * @return skill specific CompoundTag
     */
    public static CompoundTag getSkillNBT(Player player, Skill skill) {
        CompoundTag tag = getSkillsModNBT(player);
        String key = skill.getUnlocalizedName().getNamespace() + "." + skill.getUnlocalizedName().getPath();
        if (!tag.contains(key))
            tag.put(key, new CompoundTag());

        if (!tag.getCompound(key).contains("xp")) {
            tag.getCompound(key).putDouble("xp", 0);
            tag.getCompound(key).putInt("level", 1);
        }
        return tag.getCompound(key);
    }

    /**
     * Returns the Skilling main CompoundTag.
     *
     * @param player Player
     * @return mod main CompoundTag
     */
    public static CompoundTag getSkillsModNBT(Player player) {
        CompoundTag tag = player.getPersistentData();
        if (!tag.contains("skilling"))
            tag.put("skilling", new CompoundTag());

        return tag.getCompound("skilling");
    }

    /**
     * Returns the Skilling main CompoundTag.
     *
     * @param tag
     * @return mod main CompoundTag
     */
    public static CompoundTag getSkillsModNBT(CompoundTag tag){
        if(!tag.contains("skilling")){
            tag.put("skilling", new CompoundTag());
        }
        return tag.getCompound("skilling");
    }

    /**
     * Called to send the skill update packet to the player.
     *
     * @param player Player
     * @param skill  ISkill
     */
    private static void sendUpdatePacket(Player player, Skill skill) {
        if (!player.level.isClientSide) {
            NetworkManager.INSTANCE.send(PacketDistributor.PLAYER.with(() -> (ServerPlayer) player),
                    new NetworkManager.SkillXpUpdatePacket(skill, getLevel(player, skill), getXp(player, skill), true));
        }
    }

    /**
     * Returns a list of biomes that have been located by the player.
     * @param player
     * @return
     */
    public static String[] getLocatedBiomes(Player player){
        CompoundTag skillsModNBT = getSkillsModNBT(player);
        if(!skillsModNBT.contains("located-biomes")){
            skillsModNBT.putString("located-biomes", "");
        }

        if(StringUtils.isEmpty(skillsModNBT.getString("located-biomes")))
            return new String[0];
        return skillsModNBT.getString("located-biomes").split(";");
    }

    /**
     * Returns true if the player has visited the given biome.
     * @param player
     * @param biome
     * @return
     */
    public static boolean playerHasVisitedBiome(Player player, ResourceLocation biome){
        for (String locatedBiome : getLocatedBiomes(player)) {
            if(biome.toString().equals(locatedBiome))
                return true;
        }
        return false;
    }

    /**
     * Adds a located biome to the players biome list.
     * @param player
     * @param biome
     */
    public static void addLocatedBiome(Player player, ResourceLocation biome){
        ArrayList<String> biomes = new ArrayList<>();
        for(String s : getLocatedBiomes(player))
            biomes.add(s);
        biomes.add(biome.toString());
        setLocatedBiomes(player, biomes.toArray(new String[0]));
    }

    /**
     * Called to directly set the players biome.
     * addLocatedBiome should be favored instead.
     * @param player
     * @param biomes
     */
    public static void setLocatedBiomes(Player player, String[] biomes){
        StringBuilder sb = new StringBuilder();
        for (String s : biomes) {
            sb.append(s + ";");
        }
        String f = sb.toString().substring(0, sb.toString().length()-1);
        CompoundTag skillsModNBT = getSkillsModNBT(player);
        skillsModNBT.putString("located-biomes", f);

        if(!player.level.isClientSide)
            NetworkManager.INSTANCE.send(PacketDistributor.PLAYER.with(() -> (ServerPlayer) player), new NetworkManager.SkillBiomeUpdate(biomes));
    }

    /**
     * Returns true if the player passes the given requirement.
     * @param player
     * @param requirement
     * @return
     */
    public static boolean playerPassesRequirement(Player player, ActionData.Requirement requirement){
        return getLevel(player, requirement.skill()) >= requirement.minLevel();
    }
}
