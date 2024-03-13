package com.walkercase.skilling;

import com.google.gson.*;
import com.mojang.logging.LogUtils;
import com.walkercase.skilling.api.AssetAPI;
import com.walkercase.skilling.api.action.*;
import com.walkercase.skilling.api.action.skill.SkillManager;
import com.walkercase.skilling.generated.SkillingDataProvider;
import com.walkercase.skilling.item.SkillingItems;
import com.walkercase.skilling.network.NetworkManager;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.network.PacketDistributor;
import org.slf4j.Logger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Mod(Skilling.MODID)
public class Skilling {
    public static final String MODID = "skilling";
    public static final Logger LOGGER = LogUtils.getLogger();

    public Skilling() throws IOException {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, SkillingConfig.commonSpec, "skilling-config.toml");

        SkillingItems.ITEMS.register(modEventBus);

        loadSkillingAPI();

        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::generators);

        MinecraftForge.EVENT_BUS.register(this);
        NetworkManager.registerPackets();
    }

    /**
     * Called to load the Skilling mod API's.
     */
    private void loadSkillingAPI(){
        loadSkills();
        loadSkillRequirements();
        loadActions();
        EventActions.bindDefaultActions();
    }

    public static Gson getGson(){
        GsonBuilder gson = new GsonBuilder();
        gson.registerTypeAdapter(ActionIcon.class, new ActionIcon.Serializer());
        gson.registerTypeAdapter(ActionIcon.class, new ActionIcon.Deserializer());
        gson.registerTypeAdapter(ActionData.class, new ActionData.Serializer());
        gson.registerTypeAdapter(ActionData.class, new ActionData.Deserializer());
        gson.registerTypeAdapter(ActionItem.class, new ActionItem.Serializer());
        gson.registerTypeAdapter(ActionItem.class, new ActionItem.Deserializer());
        gson.setPrettyPrinting();
        gson.disableHtmlEscaping();
        return gson.create();
    }

    private void generators(final GatherDataEvent e) {
        e.getGenerator().addProvider(true, new SkillingDataProvider(e.getGenerator().getPackOutput(), MODID + "_generated"));
    }

    /**
     * Loads skill requirements from data. Must be called after all skills have been loaded.
     */
    private void loadSkillRequirements(){
        for(String modpack : getModpacksList()){
            JsonElement e = AssetAPI.readData(new ResourceLocation(modpack.replace("mod:", ""), "skills"));
            if(e != null) {
                Skilling.LOGGER.debug("Loading skill requirements from modpack: " + modpack);
                SkillManager.loadSkillRequirements(e.getAsJsonObject());
            }
        }
    }

    /**
     * Loads skills from data.
     */
    private void loadSkills(){
        for (String modpack : getModpacksList()) {
            JsonElement e = AssetAPI.readData(new ResourceLocation(modpack.replace("mod:", ""), "skills"));
            if(e != null) {
                Skilling.LOGGER.debug("Loading skills from modpack: " + modpack);
                SkillManager.loadSkills(e.getAsJsonObject());
            }
        }
    }

    /**
     * Called to search for modpacks and load their actions.
     */
    private void loadActions(){
        for (String modpack : getModpacksList()) {
            JsonElement actionElement = AssetAPI.readData(new ResourceLocation(modpack.substring(4), "actions"));
            if(actionElement != null){
                LOGGER.debug("Reading actions for mod " + modpack);
                JsonArray actions = actionElement.getAsJsonArray();
                actions.forEach(x->{
                    String modid = modpack.replace("mod:", "");
                    ResourceLocation rl = new ResourceLocation(x.getAsString().substring(0, x.getAsString().indexOf("_")), x.getAsString().substring(x.getAsString().indexOf("_") + 1));
                    try{
                        JsonObject action = AssetAPI.readData(new ResourceLocation(modid, "actions/" + x.getAsString())).getAsJsonObject();
                        ActionAPI.loadActionsFromJson(action, rl);
                    }catch(JsonSyntaxException | NullPointerException e1){
                        LOGGER.error("Failed to read action: " + x, e1);
                    }
                });
            }
        }
    }

    private List<String> getModpacksList(){
        List<String> modpacks = ForgeHooks.getModPacks();
        ArrayList<String> toAdd = new ArrayList<>();
        modpacks.add("mod:minecraft");

        //Check for any data under modid_generated too.
        modpacks.forEach(x->toAdd.add(x + "_generated"));

        modpacks.addAll(toAdd);
        return modpacks;
    }

    @SubscribeEvent
    public void playerLogin(PlayerEvent.PlayerLoggedInEvent e) {
        if (!e.getEntity().level.isClientSide) {
            Arrays.stream(SkillManager.getSkills()).forEach(iSkill -> {
                NetworkManager.INSTANCE.send(PacketDistributor.PLAYER.with(() -> (ServerPlayer) e.getEntity()),
                        new NetworkManager.SkillXpUpdatePacket(iSkill,
                                SkillManager.getLevel(e.getEntity(), iSkill), SkillManager.getXp(e.getEntity(), iSkill), false));
            });

            NetworkManager.INSTANCE.send(PacketDistributor.PLAYER.with(()->(ServerPlayer)e.getEntity()),
                    new NetworkManager.SkillBiomeUpdate(SkillManager.getLocatedBiomes(e.getEntity())));

            if(!e.getEntity().getPersistentData().contains("needsSkillingGuideBook")){
                e.getEntity().getPersistentData().putBoolean("needsSkillingGuideBook", true);

                e.getEntity().addItem(new ItemStack(SkillingItems.SKILLING_BOOK.get()));
            }
        }
    }

    @SubscribeEvent
    public void playerClone(PlayerEvent.Clone event) {
        if (event.isWasDeath()) {
            Player oldPlayer = event.getOriginal();
            Player newPlayer = event.getEntity();

            CompoundTag oldTag = SkillManager.getSkillsModNBT(oldPlayer);
            CompoundTag newTag = SkillManager.getSkillsModNBT(newPlayer);
            newTag.putString("located-biomes", oldTag.getString("located-biomes"));

            Arrays.stream(SkillManager.getSkills()).forEach(iSkill -> {
                SkillManager.setLevel(newPlayer, iSkill, SkillManager.getLevel(oldPlayer, iSkill));
                SkillManager.setXp(newPlayer, iSkill, SkillManager.getXp(oldPlayer, iSkill));

                NetworkManager.INSTANCE.send(PacketDistributor.PLAYER.with(() -> (ServerPlayer) newPlayer),
                        new NetworkManager.SkillXpUpdatePacket(iSkill,
                                SkillManager.getLevel(newPlayer, iSkill), SkillManager.getXp(newPlayer, iSkill), false));
            });

            if(oldPlayer.getPersistentData().contains("needsSkillingGuideBook")){
                newPlayer.getPersistentData().putBoolean("needsSkillingGuideBook", true);
            }
        }
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
    }

}
