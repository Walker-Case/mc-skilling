package com.walkercase.skilling;

import com.walkercase.skilling.api.action.ActionAPI;
import com.walkercase.skilling.api.action.ActionData;
import com.walkercase.skilling.api.action.ActionItem;
import com.walkercase.skilling.api.action.EventActions;
import com.walkercase.skilling.api.action.skill.BlockWatcher;
import com.walkercase.skilling.api.action.skill.Skill;
import com.walkercase.skilling.api.action.skill.SkillManager;
import com.walkercase.skilling.event.*;
import com.walkercase.skilling.network.NetworkManager;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.brewing.BrewingRecipeRegistry;
import net.minecraftforge.event.AnvilUpdateEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.ArrowNockEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.registries.ForgeRegistries;
import oshi.util.tuples.Pair;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;
import java.util.Optional;

@Mod.EventBusSubscriber(modid = Skilling.MODID)
public class SkillEvents {

    @SubscribeEvent
    public static void entityAddedToLevelEvent(EntityAddedToLevelEvent event){
        if(!event.entity.level.isClientSide){
            if(event.entity instanceof FishingHook hook){
                Player player = hook.getPlayerOwner();
                int curr = SkillManager.getLevel(player, SkillManager.SkillKeys.FISHING_SKILL.get());

                int g = curr/15;
                if(g > 0){
                    hook.lureSpeed += g;
                }
            }
        }
    }

    @SubscribeEvent
    public static void addPlayerXpEvent(AddPlayerXpEvent event){
        if(!event.player.level.isClientSide){
            if(event.skill == SkillManager.SkillKeys.CONSTRUCTION_SKILL.get()){
                int curr = SkillManager.getLevel(event.player, event.skill) / 5;
                event.xp += (curr * 0.15d);
            }
        }
    }

    @SubscribeEvent
    public static void mapUpdated(MapUpdatedEvent e){
        if(e.player.level.isClientSide)
            return;

        Pair<Boolean, ActionData> resultPair = ActionAPI.findActionCheckRequirements(e.player,
                ForgeRegistries.ITEMS.getKey(Items.MAP),
                ActionAPI.ActionKeys.MINECRAFT_MAP_UPDATE);
        if(resultPair.getA()){
            SkillManager.addXp(e.player, resultPair.getB());
        }else if(resultPair.getB() != null){
            e.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void enterBiome(FirstTimeBiomeEnteredEvent e){
        if(!e.player.level.isClientSide){
            Pair<Boolean, ActionData> resultPair = ActionAPI.findActionCheckRequirements(e.player,
                    e.key,
                    ActionAPI.ActionKeys.MINECRAFT_FIRST_TIME_BIOME_ENTER);
            if(resultPair.getA()){
                SkillManager.addXp(e.player, resultPair.getB());
            }
        }
    }

    @SubscribeEvent
    public static void onEquip(TickEvent.PlayerTickEvent e){
        if(e.side == LogicalSide.SERVER && !e.player.isCreative()){
            Biome currentBiome = e.player.level.getBiome(BlockPos.containing(e.player.position())).get();
            ResourceLocation key = ForgeRegistries.BIOMES.containsValue(currentBiome) ? ForgeRegistries.BIOMES.getKey(currentBiome)
                :e.player.level.registryAccess().registryOrThrow(Registries.BIOME).getKey(currentBiome);
            if(key != null){
                if(!SkillManager.playerHasVisitedBiome(e.player, key)){
                    SkillManager.addLocatedBiome(e.player, key);
                    MinecraftForge.EVENT_BUS.post(new FirstTimeBiomeEnteredEvent(e.player, currentBiome, key));
                    NetworkManager.INSTANCE.send(PacketDistributor.PLAYER.with(() -> (ServerPlayer) e.player),
                            new NetworkManager.SkillBiomeUpdate(SkillManager.getLocatedBiomes(e.player)));
                }
            }

            for(EquipmentSlot slot : EquipmentSlot.values()){
                Optional<ActionItem> opt = ActionAPI.getActionItem(ForgeRegistries.ITEMS.getKey(e.player.getItemBySlot(slot).getItem()));
                if(opt.isPresent()){
                    ActionItem actionItem = opt.get();
                    ActionData action = ActionAPI.getAction(actionItem, ActionAPI.ActionKeys.MINECRAFT_EQUIP);
                    if(action != null){
                        if(!ActionAPI.playerPassesRequirements(e.player, action)){
                            e.player.drop(e.player.getItemBySlot(slot).copy(), true);
                            e.player.setItemSlot(slot, ItemStack.EMPTY);
                        }
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public static void arrowNock(ArrowNockEvent e){
        if(!e.getEntity().level.isClientSide && !e.getEntity().isCreative()){
            ItemStack bow = e.getBow();
            ItemStack arrow = e.getEntity().getProjectile(bow);

            for(ItemStack is : new ItemStack[]{bow, arrow}){
                Item item = is.getItem();
                Pair<Boolean, ActionData> resultPair = ActionAPI.findActionCheckRequirements(e.getEntity(),
                        ForgeRegistries.ITEMS.getKey(item),
                        ActionAPI.ActionKeys.MINECRAFT_EQUIP);
                if(!resultPair.getA() && resultPair.getB() != null){
                    Warning.warnRequirement(e.getEntity(), Warning.GENERIC, resultPair.getB());
                    e.setCanceled(true);
                }
            }
        }
    }

    @SubscribeEvent
    public static void onHurt(LivingHurtEvent e){
        if(!e.getEntity().level.isClientSide){
            if(e.getSource().getEntity() instanceof Player player){
                ResourceLocation rl = ForgeRegistries.ENTITY_TYPES.getKey(e.getEntity().getType());
                Pair<Boolean, ActionData> resultPair = ActionAPI.findActionCheckRequirements(player,
                        rl,
                        ActionAPI.ActionKeys.MINECRAFT_KILLED);
                if(!resultPair.getA() && resultPair.getB() != null){
                    Warning.warnRequirement(player, Warning.GENERIC, resultPair.getB());
                    e.setCanceled(true);
                }
            }
        }
    }

    @SubscribeEvent
    public static void onDeath(LivingDeathEvent event){
        if(!event.getEntity().level.isClientSide){
            ResourceLocation rl = ForgeRegistries.ENTITY_TYPES.getKey(event.getEntity().getType());
            Entity source = event.getSource().getEntity();
            if(source instanceof Player player){
                Pair<Boolean, ActionData> resultPair = ActionAPI.findActionCheckRequirements(player,
                        rl,
                        ActionAPI.ActionKeys.MINECRAFT_KILLED);
                if(resultPair.getA()){
                    SkillManager.addXp(player, resultPair.getB());
                }
            }
        }
    }

    @SubscribeEvent
    public static void getEnchantmentList(EnchantmentTableListEvent event){
        if(!event.player.level.isClientSide){
            ArrayList<EnchantmentInstance> toRemove = new ArrayList<>();
            event.list.forEach(ea->{
                ResourceLocation rl = new ResourceLocation(ForgeRegistries.ENCHANTMENTS.getKey(ea.enchantment).toString() + "_" + ea.level);
                if(rl != null){
                    Pair<Boolean, ActionData> resultPair = ActionAPI.findActionCheckRequirements(event.player,
                            rl,
                            ActionAPI.ActionKeys.MINECRAFT_ENCHANTING);
                    if(!resultPair.getA() && resultPair.getB() != null){
                        toRemove.add(ea);
                    }
                }
            });
            event.list.removeAll(toRemove);
        }
    }

    @SubscribeEvent
    public static void itemSmelted(PlayerEvent.ItemSmeltedEvent e){
        if(!e.getEntity().level.isClientSide){
            ArrayList<EventActions.ActionHolder> ACTION_LIST = EventActions.getActionHolders(EventActions.EventKeys.SMELTING);

            for (EventActions.ActionHolder action : ACTION_LIST) {
                Pair<Boolean, ActionData> resultPair = ActionAPI.findActionCheckRequirements(e.getEntity(),
                        ForgeRegistries.ITEMS.getKey(e.getSmelting().getItem()),
                        action.actionKey);
                if(resultPair.getA()){
                    SkillManager.addXp(e.getEntity(), resultPair.getB());
                }
            }
        }
    }

    @SubscribeEvent
    public static void anvilUpdate(AnvilUpdateEvent e){
        if(!e.getPlayer().level.isClientSide){
            ArrayList<EventActions.ActionHolder> ACTION_LIST = EventActions.getActionHolders(EventActions.EventKeys.SMITHING_CHANGED);

            for (EventActions.ActionHolder furnaceSmeltingAction : ACTION_LIST) {
                Pair<Boolean, ActionData> resultPair = ActionAPI.findActionCheckRequirements(e.getPlayer(),
                        ForgeRegistries.ITEMS.getKey(e.getOutput().getItem()),
                        furnaceSmeltingAction.actionKey);
                if(resultPair.getA()){
                    SkillManager.addXp(e.getPlayer(), resultPair.getB());
                }
            }
        }
    }

    @SubscribeEvent
    public static void furnaceTick(FurnaceTickEvent e){
        if(!e.level.isClientSide){
            BlockEntity blockEntity = e.level.getBlockEntity(e.blockPos);
            if(blockEntity instanceof AbstractFurnaceBlockEntity furnaceBlockEntity){
                CompoundTag tag = SkillManager.getSkillsModNBT(blockEntity.getPersistentData());
                boolean cancelled = false;
                Player player = getBlockCurrentUser(e.level, e.blockState.getBlock(), e.blockPos);

                if(player != null){
                    ArrayList<EventActions.ActionHolder> FURNACE_BLOCK_ACTION_LIST = EventActions.getActionHolders(EventActions.EventKeys.SMELTING);

                    for(EventActions.ActionHolder furnaceActionHolder : FURNACE_BLOCK_ACTION_LIST){
                        Pair<Boolean, ActionData> resultPair = ActionAPI.findActionCheckRequirements(player,
                                ForgeRegistries.ITEMS.getKey(e.result.getItem()),
                                furnaceActionHolder.actionKey);
                        if(!resultPair.getA() && resultPair.getB() != null){
                            e.setCanceled(true);
                        }else{
                            if(e.blockState.getBlock() == Blocks.FURNACE || e.blockState.getBlock() == Blocks.BLAST_FURNACE) {
                                Skill skill = SkillManager.getSkill(new ResourceLocation(Skilling.MODID, "smithing"));
                                if (skill != null)
                                    e.blockEntity.litTime -= SkillManager.getLevel(player, skill) / 15;
                            }
                            if(e.blockState.getBlock() == Blocks.SMOKER) {
                                Skill skill = SkillManager.getSkill(new ResourceLocation(Skilling.MODID, "cooking"));
                                if (skill != null)
                                    e.blockEntity.litTime -= SkillManager.getLevel(player, skill) / 15;
                            }
                        }
                    }
                }else{
                    cancelled = true;
                }

                if(cancelled){
                    Player p = removeLastUser(e.level, e.blockState.getBlock(), e.blockPos);
                    if(!e.top.isEmpty()) {
                        if(p == null)
                            e.level.addFreshEntity(new ItemEntity(e.level, e.blockPos.getX(), e.blockPos.getY(), e.blockPos.getZ(), e.top.copy()));
                        else
                            p.addItem(e.top.copy());
                    }
                    if(!e.fuel.isEmpty()){
                        if(p == null)
                            e.level.addFreshEntity(new ItemEntity(e.level, e.blockPos.getX(), e.blockPos.getY(), e.blockPos.getZ(), e.fuel.copy()));
                        else
                            p.addItem(e.fuel.copy());
                    }
                    if(!e.result.isEmpty()){
                        if(p == null)
                            e.level.addFreshEntity(new ItemEntity(e.level, e.blockPos.getX(), e.blockPos.getY(), e.blockPos.getZ(), e.result.copy()));
                        else
                            p.addItem(e.result.copy());
                    }

                    e.top = ItemStack.EMPTY;
                    e.fuel = ItemStack.EMPTY;
                    e.result = ItemStack.EMPTY;
                    e.setCanceled(true);
                }
            }
        }
    }

    @SubscribeEvent
    public static void itemInteract(PlayerInteractEvent.RightClickItem e){
        if(!e.getEntity().level.isClientSide){
            ArrayList<EventActions.ActionHolder> ACTIONS = EventActions.getActionHolders(EventActions.EventKeys.ITEM_USE);

            for (EventActions.ActionHolder action : ACTIONS) {
                Pair<Boolean, ActionData> resultPair = ActionAPI.findActionCheckRequirements(e.getEntity(),
                        ForgeRegistries.ITEMS.getKey(e.getItemStack().getItem()),
                        action.actionKey);
                if(resultPair.getA()){
                    SkillManager.addXp(e.getEntity(), resultPair.getB());
                }else if(resultPair.getB() != null){
                    e.setCanceled(true);
                }
            }
        }
    }

    @SubscribeEvent
    public static void entityInteract(PlayerInteractEvent.EntityInteract e){
        if(!e.getEntity().level.isClientSide){
            ArrayList<EventActions.ActionHolder> ACTIONS = EventActions.getActionHolders(EventActions.EventKeys.INTERACT_ENTITY);
            for (EventActions.ActionHolder action : ACTIONS) {
                Pair<Boolean, ActionData> resultPair = ActionAPI.findActionCheckRequirements(e.getEntity(),
                        ForgeRegistries.ITEMS.getKey(e.getItemStack().getItem()),
                        action.actionKey);
                if(resultPair.getA()){
                    SkillManager.addXp(e.getEntity(), resultPair.getB());
                }else if(resultPair.getB() != null){
                    e.setCanceled(true);
                }
            }
        }
    }

    @SubscribeEvent
    public static void blockPlace(BlockEvent.EntityPlaceEvent e){
        if(!Objects.requireNonNull(e.getEntity()).level.isClientSide){
            if(e.getEntity() instanceof Player player){
                //If you can place we need to see if you can break.
                //If you can't break the block you can't place it.
                ArrayList<EventActions.ActionHolder> BLOCK_BREAK = EventActions.getActionHolders(EventActions.EventKeys.BLOCK_BREAK_EVENT);
                for (EventActions.ActionHolder action : BLOCK_BREAK) {
                    Pair<Boolean, ActionData> resultPair = ActionAPI.findActionCheckRequirements(player,
                            ForgeRegistries.BLOCKS.getKey(e.getState().getBlock()),
                            action.actionKey);
                    if(!resultPair.getA() && resultPair.getB() != null){
                        e.setCanceled(true);
                        Warning.warnRequirement(player, Warning.GENERIC, resultPair.getB());
                    }
                }

                if(!e.isCanceled()){
                    ArrayList<EventActions.ActionHolder> ACTIONS = EventActions.getActionHolders(EventActions.EventKeys.BLOCK_PLACE_EVENT);
                    for (EventActions.ActionHolder action : ACTIONS) {
                        Pair<Boolean, ActionData> resultPair = ActionAPI.findActionCheckRequirements(player,
                                ForgeRegistries.BLOCKS.getKey(e.getState().getBlock()),
                                action.actionKey);
                        if(resultPair.getA()){
                            SkillManager.addXp(player, resultPair.getB());
                        }else if(resultPair.getB() != null){
                            e.setCanceled(true);
                            Warning.warnRequirement(player, Warning.GENERIC, resultPair.getB());
                        }
                    }

                    if(e.getEntity() instanceof Player){
                        try {
                            BlockWatcher.updatePlaceBlock(e);
                        } catch (IOException ex) {
                            Skilling.LOGGER.error("BlockWatcher#updatePlaceBlock", ex);
                        }
                    }
                }
            }
        }
    }

    private static final HashMap<BlockPos, Integer> SINGLE_USER_BLOCK_LIST = new HashMap<>();
    private static final HashMap<BlockPos, Integer> LAST_USER_LIST = new HashMap<>();

    /**
     * Set the current block user.
     * @param pos
     * @param player
     */
    private static synchronized void setBlockCurrentUser(BlockPos pos, Player player){
        if(player == null)
            SINGLE_USER_BLOCK_LIST.remove(pos);
        else {
            SINGLE_USER_BLOCK_LIST.put(pos, player.getId());
            LAST_USER_LIST.put(pos, player.getId());
        }
    }

    /**
     * Removes and returns the last player to use the given block.
     * @param level
     * @param block
     * @param blockPos
     * @return
     */
    public static synchronized Player removeLastUser(Level level, Block block, BlockPos blockPos){
        if(level.isClientSide)
            return null;
        Player player = null;

        int currentUserID = LAST_USER_LIST.getOrDefault(blockPos, -1);

        if(currentUserID != -1){
            if(level.getEntity(currentUserID) instanceof Player player1){
                if(player1.containerMenu != player1.inventoryMenu){
                    player = player1;
                    LAST_USER_LIST.remove(blockPos);
                }
            }
        }

        return player;
    }

    /**
     * Returns the current block user.
     * @param level
     * @param block
     * @param blockPos
     * @return
     */
    public static synchronized Player getBlockCurrentUser(Level level, Block block, BlockPos blockPos){
        if(level.isClientSide)
            return null;
        Player player = null;

        int currentUserID = SINGLE_USER_BLOCK_LIST.getOrDefault(blockPos, -1);

        if(currentUserID != -1){
            if(level.getEntity(currentUserID) instanceof Player player1){
                if(player1.containerMenu != player1.inventoryMenu){
                    player = player1;
                }else{
                    setBlockCurrentUser(blockPos, null);
                }
            }else{
                setBlockCurrentUser(blockPos, null);
            }
        }

        return player;
    }

    @SubscribeEvent
    public static void rightClickBlock(PlayerInteractEvent.RightClickBlock e){
        if(!e.getLevel().isClientSide){
            Block block = e.getLevel().getBlockState(e.getPos()).getBlock();

            boolean farmingFailed = false;
            ArrayList<EventActions.ActionHolder> FARMING_ACTIONS = EventActions.getActionHolders(EventActions.EventKeys.RIGHT_CLICK_BLOCK);
            for (EventActions.ActionHolder action : FARMING_ACTIONS) {
                Pair<Boolean, ActionData> resultPair = ActionAPI.findActionCheckRequirements(e.getEntity(),
                        ForgeRegistries.BLOCKS.getKey(block),
                        action.actionKey);
                if(!resultPair.getA() && resultPair.getB() != null){
                    e.setCanceled(true);
                    Warning.warnRequirement(e.getEntity(), Warning.GENERIC, resultPair.getB());
                    farmingFailed = true;
                }
            }

            if(farmingFailed)
                return;


            ArrayList<EventActions.ActionHolder> SINGLE_BLOCK_ACTIONS = EventActions.getActionHolders(EventActions.EventKeys.SINGLE_PERSON_USE_BLOCK);
            for (EventActions.ActionHolder action : SINGLE_BLOCK_ACTIONS) {
                Pair<Boolean, ActionData> resultPair = ActionAPI.findActionCheckRequirements(e.getEntity(),
                        ForgeRegistries.BLOCKS.getKey(block),
                        action.actionKey);
                if(resultPair.getA()){
                    Player player = getBlockCurrentUser(e.getLevel(), e.getLevel().getBlockState(e.getHitVec().getBlockPos()).getBlock(), e.getHitVec().getBlockPos());

                    if(player != null){
                        Warning.warnGeneric(e.getEntity(), Warning.DUPLICATE_OPEN_MENU, 500L);
                        e.setCanceled(true);
                    }else{
                        setBlockCurrentUser(e.getPos(), e.getEntity());
                    }
                }else if(resultPair.getB() != null){
                    e.setCanceled(true);
                    Warning.warnRequirement(e.getEntity(), Warning.GENERIC, resultPair.getB());
                }
            }
        }
    }

    @SubscribeEvent
    public static void smithingChanged(SmithingMenuChangedEvent e){
        if(!e.level.isClientSide){
            ArrayList<EventActions.ActionHolder> ACTION_LIST = EventActions.getActionHolders(EventActions.EventKeys.SMITHING_CHANGED);
            Item item = e.result.getItem();
            for (EventActions.ActionHolder action : ACTION_LIST) {
                Pair<Boolean, ActionData> resultPair = ActionAPI.findActionCheckRequirements(e.player,
                        ForgeRegistries.ITEMS.getKey(item),
                        action.actionKey);
                if(!resultPair.getA() && resultPair.getB() != null){
                    e.setCanceled(true);
                }else if(resultPair.getB() != null){
                    SkillManager.addXp(e.player, action.actionData);
                }
            }
        }
    }

    @SubscribeEvent
    public static void itemCrafted(PlayerEvent.ItemCraftedEvent event){
        if(!event.getEntity().level.isClientSide){
            ArrayList<EventActions.ActionHolder> ACTION_LIST = EventActions.getActionHolders(EventActions.EventKeys.CRAFTING_GRID_CHANGED);
            Item item = event.getCrafting().getItem();
            for (EventActions.ActionHolder action : ACTION_LIST) {
                Pair<Boolean, ActionData> resultPair = ActionAPI.findActionCheckRequirements(event.getEntity(),
                        ForgeRegistries.ITEMS.getKey(item),
                        action.actionKey);
                if(!resultPair.getA() && resultPair.getB() != null){
                    event.setCanceled(true);
                }else if(resultPair.getB() != null){
                    SkillManager.addXp(event.getEntity(), resultPair.getB());
                }
            }
        }
    }

    @SubscribeEvent
    public static void craftingGridChanged(CraftingGridChangedEvent event){
        if(!event.level.isClientSide){
            ArrayList<EventActions.ActionHolder> ACTION_LIST = EventActions.getActionHolders(EventActions.EventKeys.CRAFTING_GRID_CHANGED);
            Item item = event.resultContainer.getItem(0).getItem();

            for (EventActions.ActionHolder action : ACTION_LIST) {
                Pair<Boolean, ActionData> resultPair = ActionAPI.findActionCheckRequirements(event.player,
                        ForgeRegistries.ITEMS.getKey(item),
                        action.actionKey);
                if(!resultPair.getA() && resultPair.getB() != null){
                    event.setCanceled(true);
                }
            }
        }
    }

    @SubscribeEvent
    public static void onFish(ItemFishedEvent e){
        if(!e.player.getLevel().isClientSide){
            ArrayList<ItemStack> toRemove = new ArrayList<>();

            e.drops.stream().forEach(is->{
                Optional<ActionItem> opt = ActionAPI.getActionItem(ForgeRegistries.ITEMS.getKey(is.getItem()));
                if(opt.isPresent()){
                    ActionItem actionItem = opt.get();
                    ActionData action = ActionAPI.getAction(actionItem, ActionAPI.ActionKeys.MINECRAFT_FISHING);
                    if(action != null){
                        if(!ActionAPI.playerPassesRequirements(e.player, action)){
                            toRemove.add(is);
                        }else{
                            if(SkillManager.SkillKeys.FISHING_SKILL.get() != null)
                                e.hook.lureSpeed = SkillManager.getLevel(e.player, SkillManager.SkillKeys.FISHING_SKILL.get()) / 15;

                            SkillManager.addXp(e.player, action);
                        }
                    }
                }
            });

            e.drops.removeAll(toRemove);
        }
    }

    @SubscribeEvent
    public static void brewingStandTick(BrewingStandTickEvent e) {
        if(e.level.isClientSide)
            return;

        Player player = getBlockCurrentUser(e.level, e.blockState.getBlock(), e.blockPos);
        if(player != null){
            //Do stuff
            //0-2 potion
            //3 ingredient
            //4 blaze powder

            for(int i=0;i<3;i++){
                ItemStack slot = e.blockEntity.getItem(i);

                ItemStack output = BrewingRecipeRegistry.getOutput(slot, e.blockEntity.getItem(3));
                ResourceLocation potion = null;
                if(output.getTag() != null && output.getTag().contains("Potion")){
                    potion = new ResourceLocation(output.getTag().getString("Potion"));
                }

                if(potion != null){
                    Optional<ActionItem> opt = ActionAPI.getActionItem(potion);
                    if(opt.isPresent()){
                        ActionItem actionItem = opt.get();
                        ActionData action = ActionAPI.getAction(actionItem, ActionAPI.ActionKeys.MINECRAFT_BREWING);
                        if(action != null){
                            if(!ActionAPI.playerPassesRequirements(player, action)){
                                Warning.warnRequirement(player, Warning.GENERIC, action);
                                e.setCanceled(true);
                            }else{
                                SkillManager.addXp(player, action);
                            }
                        }
                    }
                }
            }
        }else{
            for(int i=0;i<5;i++){
                if(!e.blockEntity.getItem(i).isEmpty()){
                    e.level.addFreshEntity(new ItemEntity(e.level, e.blockPos.getX(), e.blockPos.getY(), e.blockPos.getZ(), e.blockEntity.getItem(i).copy()));
                    e.blockEntity.setItem(i, ItemStack.EMPTY);
                }
            }
        }
    }

    @SubscribeEvent
    public static void breakSpeed(PlayerEvent.BreakSpeed e) {
        Optional<ActionItem> opt = ActionAPI.getActionItem(ForgeRegistries.ITEMS.getKey(e.getEntity().getMainHandItem().getItem()));
        if(opt.isPresent()){
            ActionItem actionItem = opt.get();
            ActionData action = ActionAPI.getAction(actionItem, ActionAPI.ActionKeys.MINECRAFT_EQUIP);
            if(action != null){
                if(!ActionAPI.playerPassesRequirements(e.getEntity(), action)){
                    e.setNewSpeed(e.getOriginalSpeed() * 0.00000001f);
                    Warning.warnRequirement(e.getEntity(), Warning.GENERIC, action);
                }else{
                    float newSpeed = e.getOriginalSpeed();
                    for (ActionData.Requirement requirement : action.requirements) {
                        newSpeed += ((float) SkillManager.getLevel(e.getEntity(), requirement.skill()) /15);
                    }
                    e.setNewSpeed(newSpeed);
                }
            }
        }
    }

    @SubscribeEvent
    public static void blockBreakEvent(BlockEvent.BreakEvent e) {
        if (!e.getLevel().isClientSide() && !e.getPlayer().isCreative()) {
            {
                ArrayList<EventActions.ActionHolder> ACTION_LIST = EventActions.getActionHolders(EventActions.EventKeys.BLOCK_BREAK_EVENT);
                Optional<ActionItem> opt = ActionAPI.getActionItem(ForgeRegistries.BLOCKS.getKey(e.getState().getBlock()));

                if(opt.isPresent()){
                    for (EventActions.ActionHolder blockBreakAction : ACTION_LIST) {
                        ActionItem actionItem = opt.get();
                        ActionData action = ActionAPI.getAction(actionItem, blockBreakAction.actionKey);
                        blockBreakAction.actionData = action;
                        if(action != null){
                            if(!ActionAPI.playerPassesRequirements(e.getPlayer(), action)){
                                Warning.warnRequirement(e.getPlayer(), Warning.GENERIC, action);
                            }else{
                                blockBreakAction.passed = true;
                                try {
                                    if(!BlockWatcher.didPlayerPlaceBlock(e.getLevel(), e.getPos()))
                                        SkillManager.addXp(e.getPlayer(), action);

                                    BlockWatcher.updateBlockBreak(e.getLevel(), e.getPos());
                                } catch (IOException ex) {
                                    Skilling.LOGGER.error("BlockWatcher#didPlayerPlaceBlock", ex);
                                }
                            }
                        }
                    }
                }

                boolean wereAllNull = ACTION_LIST.stream().filter(x->x.actionData == null).count() >= ACTION_LIST.size();
                if(!wereAllNull){
                    boolean allFailed = ACTION_LIST.stream().filter(x->!x.passed).count() >= ACTION_LIST.size();
                    e.setCanceled(allFailed);
                }
            }
        }

        if(!e.getLevel().isClientSide() && !e.isCanceled()){
            setBlockCurrentUser(e.getPos(), null);
        }
    }
}
