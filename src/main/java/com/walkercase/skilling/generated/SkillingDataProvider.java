package com.walkercase.skilling.generated;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.walkercase.skilling.Skilling;
import com.walkercase.skilling.api.action.ActionAPI;
import com.walkercase.skilling.api.action.ActionData;
import com.walkercase.skilling.api.action.ActionIcon;
import com.walkercase.skilling.api.action.ActionItem;
import com.walkercase.skilling.api.action.skill.SkillManager;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;

public class SkillingDataProvider implements DataProvider {

    protected final PackOutput.PathProvider mainPath;
    protected final PackOutput.PathProvider actionsPath;
    private final String MODID;

    private List<CompletableFuture<?>> futures = new ArrayList();
    private HashMap<String, ArrayList<ResourceLocation>> modidItemMap = new HashMap<>();

    public SkillingDataProvider(PackOutput p_248933_, String MODID) {
        this.mainPath = p_248933_.createPathProvider(PackOutput.Target.DATA_PACK, "");
        this.actionsPath = p_248933_.createPathProvider(PackOutput.Target.DATA_PACK, "actions");
        this.MODID = MODID;
    }
    @Override
    public CompletableFuture<?> run(CachedOutput cachedOutput) {

        createConstructionLogGenerators(cachedOutput);

        createChoppingLogGenerators(cachedOutput);
        createChoppingPlanksCraftingGenerators(cachedOutput);

        createStairAndOtherGenerators(cachedOutput);

        createSimpleGenerators(cachedOutput);

        modidItemMap.forEach((key, list)->{
            JsonArray arr= new JsonArray();
            for (ResourceLocation rl : list) {
                arr.add(rl.toString().replace(":", "_"));
            }
            futures.add(save(cachedOutput, mainPath, new ResourceLocation(key + "_generated", "actions"), arr));
        });

        return CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new));
    }

    public void createSimpleGenerators(CachedOutput cachedOutput){
        createSimpleConstructionGenerator(cachedOutput, 10, "bed", 2, "bedrock");
        createSimpleConstructionGenerator(cachedOutput, 5, "candle", 2, "cake");
        createSimpleConstructionGenerator(cachedOutput, 10, "glass", 2);
        createSimpleConstructionGenerator(cachedOutput, 20, "carpet", 2);
        createSimpleConstructionGenerator(cachedOutput, 25, "terracotta", 2);
        createSimpleConstructionGenerator(cachedOutput, 30, "concrete", 2);
        createSimpleConstructionGenerator(cachedOutput, 50, "banner", 2);

        createSimpleEngineeringGenerator(cachedOutput, 5, "button", 1);
        createSimpleEngineeringGenerator(cachedOutput, 10, "pressure_plate", 1);
        createSimpleEngineeringGenerator(cachedOutput, 70, "shulker_box", 1);

        createSimpleJewelerGenerators(cachedOutput, 0);
    }

    public void createSimpleJewelerGenerators(CachedOutput cachedOutput, int beginLevel){
        AtomicInteger level = new AtomicInteger(beginLevel);
        String[] prefixes = new String[]{"copper", "iron", "gold", "diamond", "netherite"};
        String[] suffixes = new String[]{"chisel", "brush", "ring", "amulet", "bracers"}; //add _mould later

        for(String prefix : prefixes){
            int currLevel = level.get();
            for(String suffix : suffixes){
                for(String mouldSuffix : new String[]{"", "_mould"}){
                    boolean isMould = !StringUtils.isEmpty(mouldSuffix);
                    ResourceLocation rl = new ResourceLocation("jeweler", prefix + "_" + suffix + mouldSuffix);

                    if(!modidItemMap.containsKey(rl.getNamespace()))
                        modidItemMap.put(rl.getNamespace(), new ArrayList<>());
                    modidItemMap.get(rl.getNamespace()).add(rl);

                    double xp = (((double) level.get() / 5) + (0.10 * level.get()) + 0.10);
                    if(isMould)
                        xp /= 2;
                    xp = Math.floor(xp * 100) / 100.0;

                    ArrayList<ActionData> actionDataList = new ArrayList<>();
                    {
                        ResourceLocation actionKey = ActionAPI.ActionKeys.MINECRAFT_CRAFTING;
                        ArrayList<ActionData.XpData> xpData = new ArrayList<>();
                        if(isMould)
                            xpData.add(new ActionData.XpData(SkillManager.SkillKeys.SMITHING_SKILL.get(), xp));
                        xpData.add(new ActionData.XpData(SkillManager.SkillKeys.JEWLELER_PROSPECTING.get(), xp));


                        ArrayList<ActionData.Requirement> requirements = new ArrayList<>();
                        if(isMould)
                            requirements.add(new ActionData.Requirement(SkillManager.SkillKeys.SMITHING_SKILL.get(), actionKey, level.get() + 10));
                        requirements.add(new ActionData.Requirement(SkillManager.SkillKeys.JEWLELER_PROSPECTING.get(), actionKey, level.get()));

                        actionDataList.add(new ActionData(actionKey, xpData.toArray(new ActionData.XpData[0]), requirements.toArray(new ActionData.Requirement[0])));
                    }
                    ActionIcon icon = new ActionIcon(rl);

                    ActionItem item = new ActionItem(rl, actionDataList.toArray(new ActionData[0]), icon);

                    futures.add(save(cachedOutput, actionsPath, new ResourceLocation(rl.getNamespace() + "_generated", rl.toString().replace(":", "_")), Skilling.getGson().toJsonTree(item, ActionItem.class)));

                }
            }
            level.addAndGet(25);
        }
    }

    public void createSimpleEngineeringGenerator(CachedOutput cachedOutput, int beginLevel, String nameContains, int addEach, String... blacklist){
        AtomicInteger level = new AtomicInteger(beginLevel);
        ForgeRegistries.BLOCKS.forEach(block->{
            ResourceLocation rl = ForgeRegistries.BLOCKS.getKey(block);
            boolean pass = rl.getPath().contains(nameContains);
            for(String s : blacklist)
                if(rl.getPath().contains(s))
                    pass = false;

            if(pass){
                level.addAndGet(addEach);
                if(!modidItemMap.containsKey(rl.getNamespace()))
                    modidItemMap.put(rl.getNamespace(), new ArrayList<>());
                modidItemMap.get(rl.getNamespace()).add(rl);

                double xp = (((double) level.get() / 5) + (0.10 * level.get()) + 0.10);
                xp = Math.floor(xp * 100) / 100.0;

                ArrayList<ActionData> actionDataList = new ArrayList<>();
                {
                    ResourceLocation actionKey = ActionAPI.ActionKeys.MINECRAFT_CRAFTING;
                    ArrayList<ActionData.XpData> xpData = new ArrayList<>();
                    xpData.add(new ActionData.XpData(SkillManager.SkillKeys.ENGINEERING_SKILL.get(), xp));

                    ArrayList<ActionData.Requirement> requirements = new ArrayList<>();
                    requirements.add(new ActionData.Requirement(SkillManager.SkillKeys.ENGINEERING_SKILL.get(), actionKey, level.get()));

                    actionDataList.add(new ActionData(actionKey, xpData.toArray(new ActionData.XpData[0]), requirements.toArray(new ActionData.Requirement[0])));
                }
                {
                    ResourceLocation actionKey = ActionAPI.ActionKeys.MINECRAFT_BREAK_BLOCK;
                    ArrayList<ActionData.XpData> xpData = new ArrayList<>();

                    ArrayList<ActionData.Requirement> requirements = new ArrayList<>();
                    requirements.add(new ActionData.Requirement(SkillManager.SkillKeys.ENGINEERING_SKILL.get(), actionKey, level.get()));

                    actionDataList.add(new ActionData(actionKey, xpData.toArray(new ActionData.XpData[0]), requirements.toArray(new ActionData.Requirement[0])));
                }
                ActionIcon icon = new ActionIcon(rl);

                ActionItem item = new ActionItem(rl, actionDataList.toArray(new ActionData[0]), icon);

                futures.add(save(cachedOutput, actionsPath, new ResourceLocation(rl.getNamespace() + "_generated", rl.toString().replace(":", "_")), Skilling.getGson().toJsonTree(item, ActionItem.class)));
            }
        });
    }

    public void createSimpleConstructionGenerator(CachedOutput cachedOutput, int beginLevel, String nameContains, int addEach, String... blacklist){
        AtomicInteger level = new AtomicInteger(beginLevel);
        ForgeRegistries.BLOCKS.forEach(block->{
            ResourceLocation rl = ForgeRegistries.BLOCKS.getKey(block);
            boolean pass = rl.getPath().contains(nameContains);
            for(String s : blacklist)
                if(rl.getPath().contains(s))
                    pass = false;

            if(pass){
                level.addAndGet(addEach);
                if(!modidItemMap.containsKey(rl.getNamespace()))
                    modidItemMap.put(rl.getNamespace(), new ArrayList<>());
                modidItemMap.get(rl.getNamespace()).add(rl);

                double xp = (((double) level.get() / 5) + (0.10 * level.get()) + 0.10);
                xp = Math.floor(xp * 100) / 100.0;

                ArrayList<ActionData> actionDataList = new ArrayList<>();
                {
                    ResourceLocation actionKey = ActionAPI.ActionKeys.MINECRAFT_CRAFTING;
                    ArrayList<ActionData.XpData> xpData = new ArrayList<>();
                    xpData.add(new ActionData.XpData(SkillManager.SkillKeys.CONSTRUCTION_SKILL.get(), xp));

                    ArrayList<ActionData.Requirement> requirements = new ArrayList<>();
                    requirements.add(new ActionData.Requirement(SkillManager.SkillKeys.CONSTRUCTION_SKILL.get(), actionKey, level.get()));

                    actionDataList.add(new ActionData(actionKey, xpData.toArray(new ActionData.XpData[0]), requirements.toArray(new ActionData.Requirement[0])));
                }
                {
                    ResourceLocation actionKey = ActionAPI.ActionKeys.MINECRAFT_BREAK_BLOCK;
                    ArrayList<ActionData.XpData> xpData = new ArrayList<>();

                    ArrayList<ActionData.Requirement> requirements = new ArrayList<>();
                    requirements.add(new ActionData.Requirement(SkillManager.SkillKeys.CONSTRUCTION_SKILL.get(), actionKey, level.get()));

                    actionDataList.add(new ActionData(actionKey, xpData.toArray(new ActionData.XpData[0]), requirements.toArray(new ActionData.Requirement[0])));
                }
                ActionIcon icon = new ActionIcon(rl);

                ActionItem item = new ActionItem(rl, actionDataList.toArray(new ActionData[0]), icon);

                futures.add(save(cachedOutput, actionsPath, new ResourceLocation(rl.getNamespace() + "_generated", rl.toString().replace(":", "_")), Skilling.getGson().toJsonTree(item, ActionItem.class)));
            }
        });
    }

    public void createStairAndOtherGenerators(CachedOutput cachedOutput){
        AtomicInteger level = new AtomicInteger(10);
        ForgeRegistries.BLOCKS.forEach(block->{
            ArrayList<Block> toAdd = new ArrayList<>();
            ResourceLocation rl = ForgeRegistries.BLOCKS.getKey(block);

            if (rl.getPath().contains("brick")) {
                toAdd.add(block);
            }

            String[] suffixes = new String[]{"stairs", "slab", "wall"};

            for(String suffix : suffixes){
                Block located = ForgeRegistries.BLOCKS.getValue(new ResourceLocation(rl.getNamespace(), rl.getPath() + "_" + suffix));
                if(located != null && located != Blocks.AIR){
                    toAdd.add(located);
                }
            }

            if(toAdd.size() == 1)
                toAdd.remove(0);

            //If anything exists this is a valid block
            if(!toAdd.isEmpty()){
                for(Block b : toAdd){
                    ResourceLocation newRl = ForgeRegistries.BLOCKS.getKey(b);
                    if(!modidItemMap.containsKey(newRl.getNamespace()))
                        modidItemMap.put(newRl.getNamespace(), new ArrayList<>());
                    modidItemMap.get(newRl.getNamespace()).add(newRl);

                    int currLevel = level.addAndGet(1);
                    double xp = (((double) currLevel / 5) + (0.10 * currLevel) + 0.10);
                    xp = Math.floor(xp * 100) / 100.0;

                    ArrayList<ActionData> actionDataList = new ArrayList<>();
                    {
                        ResourceLocation actionKey = ActionAPI.ActionKeys.MINECRAFT_CRAFTING;
                        ArrayList<ActionData.XpData> xpData = new ArrayList<>();
                        xpData.add(new ActionData.XpData(SkillManager.SkillKeys.CONSTRUCTION_SKILL.get(), xp));

                        ArrayList<ActionData.Requirement> requirements = new ArrayList<>();
                        requirements.add(new ActionData.Requirement(SkillManager.SkillKeys.CONSTRUCTION_SKILL.get(), actionKey, currLevel));

                        actionDataList.add(new ActionData(actionKey, xpData.toArray(new ActionData.XpData[0]), requirements.toArray(new ActionData.Requirement[0])));
                    }
                    {
                        ResourceLocation actionKey = ActionAPI.ActionKeys.MINECRAFT_BREAK_BLOCK;
                        ArrayList<ActionData.XpData> xpData = new ArrayList<>();

                        ArrayList<ActionData.Requirement> requirements = new ArrayList<>();
                        requirements.add(new ActionData.Requirement(SkillManager.SkillKeys.CONSTRUCTION_SKILL.get(), actionKey, currLevel));

                        actionDataList.add(new ActionData(actionKey, xpData.toArray(new ActionData.XpData[0]), requirements.toArray(new ActionData.Requirement[0])));
                    }
                    ActionIcon icon = new ActionIcon(newRl);

                    ActionItem item = new ActionItem(newRl, actionDataList.toArray(new ActionData[0]), icon);

                    futures.add(save(cachedOutput, actionsPath, new ResourceLocation(newRl.getNamespace() + "_generated", newRl.toString().replace(":", "_")), Skilling.getGson().toJsonTree(item, ActionItem.class)));
                }
            }
        });
    }

    /**
     * Returns the skill 'chopping' log generated data.
     * @param cachedOutput
     * @return
     */
    public void createChoppingLogGenerators(CachedOutput cachedOutput){
        ArrayList<ResourceLocation> woodList = new ArrayList<>();
        ForgeRegistries.BLOCKS.forEach(block->{
            ResourceLocation blockKey = ForgeRegistries.BLOCKS.getKey(block);

            if(blockKey.toString().contains("_log") && !blockKey.toString().contains("stripped"))
                woodList.add(blockKey);
        });
        int level = 0;
        String lastModid = "";
        for(ResourceLocation rl : woodList){
            if(lastModid.isEmpty())
                lastModid = rl.getNamespace();

            if(!lastModid.equals(rl.getNamespace())){
                lastModid = rl.getNamespace();
                level = 20;
            }
            String[] suffixes = new String[]{"_log", "_leaves"};
            for(String s : suffixes){
                ResourceLocation newRl = new ResourceLocation(rl.toString().replace("_log", s));
                if(!modidItemMap.containsKey(newRl.getNamespace()))
                    modidItemMap.put(newRl.getNamespace(), new ArrayList<>());
                modidItemMap.get(newRl.getNamespace()).add(newRl);

                double xp = (((double) level / 5) + (0.10 * level) + 0.10);
                xp = Math.floor(xp * 100) / 100.0;
                if(s.equals("_leaves"))
                    xp = 0;

                ArrayList<ActionData> actionDataList = new ArrayList<>();
                {
                    ResourceLocation actionKey = ActionAPI.ActionKeys.SKILLING_CHOPPING;
                    ArrayList<ActionData.XpData> xpData = new ArrayList<>();
                    xpData.add(new ActionData.XpData(SkillManager.SkillKeys.FORESTRY_SKILL.get(), xp));

                    ArrayList<ActionData.Requirement> requirements = new ArrayList<>();
                    requirements.add(new ActionData.Requirement(SkillManager.SkillKeys.FORESTRY_SKILL.get(), actionKey, level));

                    actionDataList.add(new ActionData(actionKey, xpData.toArray(new ActionData.XpData[0]), requirements.toArray(new ActionData.Requirement[0])));
                }
                ActionIcon icon = new ActionIcon(newRl);

                ActionItem item = new ActionItem(newRl, actionDataList.toArray(new ActionData[0]), icon);

                futures.add(save(cachedOutput, actionsPath, new ResourceLocation(newRl.getNamespace() + "_generated", newRl.toString().replace(":", "_")), Skilling.getGson().toJsonTree(item, ActionItem.class)));

            }
            level += 10;
        }
    }

    public void createChoppingPlanksCraftingGenerators(CachedOutput cachedOutput){
        ArrayList<ResourceLocation> woodList = new ArrayList<>();
        ForgeRegistries.BLOCKS.forEach(block->{
            ResourceLocation blockKey = ForgeRegistries.BLOCKS.getKey(block);

            if(blockKey.toString().contains("_log") && !blockKey.toString().contains("stripped"))
                woodList.add(blockKey);
        });
        int level = 0;
        String lastModid = "";
        for(ResourceLocation rl : woodList){
            if(lastModid.isEmpty())
                lastModid = rl.getNamespace();

            if(!lastModid.equals(rl.getNamespace())){
                lastModid = rl.getNamespace();
                level = 20;
            }
            String[] suffixes = new String[]{"_planks"};
            for(String s : suffixes){
                ResourceLocation newRl = new ResourceLocation(rl.toString().replace("_log", s));
                if(!modidItemMap.containsKey(newRl.getNamespace()))
                    modidItemMap.put(newRl.getNamespace(), new ArrayList<>());
                modidItemMap.get(newRl.getNamespace()).add(newRl);

                double xp = (((double) level / 5) + (0.10 * level) + 0.10);
                xp = Math.floor(xp * 100) / 100.0;
                xp /= 4;

                ArrayList<ActionData> actionDataList = new ArrayList<>();
                {
                    ResourceLocation actionKey = ActionAPI.ActionKeys.MINECRAFT_CRAFTING;
                    ArrayList<ActionData.XpData> xpData = new ArrayList<>();
                    xpData.add(new ActionData.XpData(SkillManager.SkillKeys.FORESTRY_SKILL.get(), xp/2));
                    xpData.add(new ActionData.XpData(SkillManager.SkillKeys.CRAFTING_SKILL.get(), xp/2));

                    ArrayList<ActionData.Requirement> requirements = new ArrayList<>();
                    requirements.add(new ActionData.Requirement(SkillManager.SkillKeys.CRAFTING_SKILL.get(), actionKey, level));
                    requirements.add(new ActionData.Requirement(SkillManager.SkillKeys.FORESTRY_SKILL.get(), actionKey, level));

                    actionDataList.add(new ActionData(actionKey, xpData.toArray(new ActionData.XpData[0]), requirements.toArray(new ActionData.Requirement[0])));
                }
                ActionIcon icon = new ActionIcon(newRl);

                ActionItem item = new ActionItem(newRl, actionDataList.toArray(new ActionData[0]), icon);

                futures.add(save(cachedOutput, actionsPath, new ResourceLocation(newRl.getNamespace() + "_generated", newRl.toString().replace(":", "_")), Skilling.getGson().toJsonTree(item, ActionItem.class)));

            }
            level += 10;
        }
    }

    public void createConstructionLogGenerators(CachedOutput cachedOutput){
        ArrayList<ResourceLocation> woodList = new ArrayList<>();

        ForgeRegistries.BLOCKS.forEach(block->{
            ResourceLocation blockKey = ForgeRegistries.BLOCKS.getKey(block);

            if(blockKey.toString().contains("_log") && !blockKey.toString().contains("stripped"))
                woodList.add(blockKey);
        });
        int currentLevel = 0;
        String[] suffixes = new String[]{"door", "fence", "fence_gate", "stairs", "slab", "trapdoor", "sign", "boat", "chest_boat"};

        String lastModid = "";
        for (ResourceLocation blockKey : woodList) {
            if(!lastModid.equals(blockKey.getNamespace())) {
                lastModid = blockKey.getNamespace();
                currentLevel = 0;
            }

            int level = currentLevel;

            for (String string : suffixes) {
                level++;
                ResourceLocation rl = new ResourceLocation(blockKey.toString().replace("_log", "") + "_" + string);
                if(!modidItemMap.containsKey(rl.getNamespace()))
                    modidItemMap.put(rl.getNamespace(), new ArrayList<>());
                modidItemMap.get(rl.getNamespace()).add(rl);

                double xp = (((double) level / 5) + (0.10 * level) + 0.10);
                xp = Math.floor(xp * 100) / 100.0;

                ArrayList<ActionData> actionDataList = new ArrayList<>();
                {
                    ResourceLocation actionKey = ActionAPI.ActionKeys.MINECRAFT_CRAFTING;
                    ArrayList<ActionData.XpData> xpData = new ArrayList<>();
                    xpData.add(new ActionData.XpData(SkillManager.SkillKeys.CONSTRUCTION_SKILL.get(), xp));

                    ArrayList<ActionData.Requirement> requirements = new ArrayList<>();
                    requirements.add(new ActionData.Requirement(SkillManager.SkillKeys.CONSTRUCTION_SKILL.get(), actionKey, level));

                    actionDataList.add(new ActionData(actionKey, xpData.toArray(new ActionData.XpData[0]), requirements.toArray(new ActionData.Requirement[0])));
                }
                {
                    ResourceLocation actionKey = ActionAPI.ActionKeys.MINECRAFT_BREAK_BLOCK;
                    ArrayList<ActionData.Requirement> requirements = new ArrayList<>();
                    requirements.add(new ActionData.Requirement(SkillManager.SkillKeys.CONSTRUCTION_SKILL.get(), actionKey, level));

                    actionDataList.add(new ActionData(actionKey, new ActionData.XpData[0], requirements.toArray(new ActionData.Requirement[0])));
                }
                ActionIcon icon = new ActionIcon(rl);

                ActionItem item = new ActionItem(rl, actionDataList.toArray(new ActionData[0]), icon);

                futures.add(save(cachedOutput, actionsPath, new ResourceLocation(rl.getNamespace() + "_generated", rl.toString().replace(":", "_")), Skilling.getGson().toJsonTree(item, ActionItem.class)));
            }

            currentLevel += 5;
        }
    }

    protected CompletableFuture<?> save(CachedOutput cachedOutput, PackOutput.PathProvider path, ResourceLocation location, JsonElement object) {
        Skilling.LOGGER.info("Saving object " + location.toString() );
        return DataProvider.saveStable(cachedOutput, object, path.json(location));
    }

    @Override
    public String getName() {
        return Skilling.MODID +  "_construction_data";
    }
}
