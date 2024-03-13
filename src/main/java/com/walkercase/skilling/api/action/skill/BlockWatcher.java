package com.walkercase.skilling.api.action.skill;

import com.walkercase.skilling.Skilling;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.ApiStatus;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicReference;

public class BlockWatcher {

    private static final ResourceLocation BLOCK_PLACE_ACTION = new ResourceLocation("minecraft", "block_place");

    /**
     * Save the block tag for the given chunk.
     * @param modid
     * @param action
     * @param tag
     * @param level
     * @param dimension
     * @param pos
     * @throws IOException
     */
    private static void saveBlockTag(String modid, ResourceLocation action, CompoundTag tag, String level, ResourceLocation dimension, ChunkPos pos) throws IOException {
        File f = getChunkFile(modid, action, level, dimension, pos);
        if(!f.exists()) {
            getDirectory(modid, action, level).mkdirs();
            f.createNewFile();
        }

        NbtIo.writeCompressed(tag, f);
    }

    /**
     * Returns the CompoundTag for the given chunk.
     * @param modid
     * @param action
     * @param level
     * @param dimension
     * @param pos
     * @return
     * @throws IOException
     */
    private static CompoundTag getBlockTag(String modid, ResourceLocation action, String level, ResourceLocation dimension, ChunkPos pos) throws IOException {
        File f = getChunkFile(modid, action, level, dimension, pos);
        return f.exists() ? NbtIo.readCompressed(f) : new CompoundTag();
    }

    /**
     * Returns the chunk file for block-specific data storage.
     * @param modid
     * @param action
     * @param level
     * @param dimension
     * @param pos
     * @return
     */
    private static File getChunkFile(String modid, ResourceLocation action, String level, ResourceLocation dimension, ChunkPos pos){
        return new File(getDirectory(modid, action, level) + "/" + dimension.getNamespace() + "_" + dimension.getPath() + "_" + pos.hashCode() + ".nbt");
    }

    /**
     * Returns the File directory for the given data.
     * @param modid
     * @param action
     * @param level
     * @return
     */
    private static File getDirectory(String modid, ResourceLocation action, String level){
        return new File("data/" + modid + "/" + level.hashCode() + "/" + action.toString().replace(":", "_") + "/");
    }

    /**
     * Returns the DimensionType ResourceKey if found otherwise null.
     * @param level
     * @return
     */
    private static ResourceKey<DimensionType> getDimensionResourceLocation(LevelAccessor level){
        AtomicReference<ResourceKey<DimensionType>> dim = new AtomicReference<>(null);
        level.holderLookup(Registries.DIMENSION_TYPE).listElements().forEach(x->{
            if(x.get() == level.dimensionType()){
                dim.set(x.unwrapKey().get());
            }
        });
        return dim.get() == null ? null : dim.get();
    }

    /**
     * Called to update the block watch data.
     * @param e
     * @throws IOException
     */
    @ApiStatus.Internal
    public static void updatePlaceBlock(BlockEvent.EntityPlaceEvent e) throws IOException {
        if(!e.isCanceled() && !e.getLevel().isClientSide() && e.getEntity() instanceof Player){
            ResourceKey<DimensionType> dimension = getDimensionResourceLocation(e.getLevel());
            if(dimension != null){
                String level = e.getLevel().getServer().getWorldData().getLevelName();
                ChunkPos chunkPos = e.getLevel().getChunk(e.getPos()).getPos();
                CompoundTag tag = getBlockTag(Skilling.MODID, BLOCK_PLACE_ACTION, level, dimension.location(), chunkPos);

                String key = e.getPos().getX() + "," + e.getPos().getY() + "," + e.getPos().getZ();

                CompoundTag entry = new CompoundTag();

                ResourceLocation block = ForgeRegistries.BLOCKS.getKey(e.getState().getBlock());

                entry.putString("block", block.toString().replace(":", "_"));

                tag.put(key, entry);

                saveBlockTag(Skilling.MODID, BLOCK_PLACE_ACTION, tag, level, dimension.location(), chunkPos);

            }
        }
    }

    /**
     * Called when a block is broken to remove data we no longer need to track.
     * @param level
     * @param blockPos
     * @throws IOException
     */
    @ApiStatus.Internal
    public static void updateBlockBreak(LevelAccessor level, BlockPos blockPos) throws IOException {
        ResourceKey<DimensionType> dimension = getDimensionResourceLocation(level);
        ChunkPos chunkPos = level.getChunk(blockPos).getPos();
        String levelName = level.getServer().getWorldData().getLevelName();

        String key = blockPos.getX() + "," + blockPos.getY() + "," + blockPos.getZ();
        CompoundTag blockTag = getBlockTag(Skilling.MODID, BLOCK_PLACE_ACTION, levelName, dimension.location(), chunkPos);
        if(blockTag.contains(key))
            blockTag.remove(key);

        saveBlockTag(Skilling.MODID, BLOCK_PLACE_ACTION, blockTag, levelName, dimension.location(), chunkPos);
    }

    /**
     * Returns true if a player placed the given block.
     * @param level
     * @param blockPos
     * @return
     * @throws IOException
     */
    public static boolean didPlayerPlaceBlock(LevelAccessor level, BlockPos blockPos) throws IOException {
        Block block = level.getBlockState(blockPos).getBlock();
        ResourceKey<DimensionType> dimension = getDimensionResourceLocation(level);
        ChunkPos chunkPos = level.getChunk(blockPos).getPos();
        String levelName = level.getServer().getWorldData().getLevelName();
        return didPlayerPlaceBlock(Skilling.MODID, levelName, dimension.location(), chunkPos, blockPos, block);
    }

    /**
     * Returns true if the given block was placed by a player.
     * @param modid
     * @param level
     * @param dimension
     * @param pos
     * @param blockPos
     * @return
     * @throws IOException
     */
    private static boolean didPlayerPlaceBlock(String modid, String level, ResourceLocation dimension, ChunkPos pos, BlockPos blockPos, Block block) throws IOException {
        CompoundTag tag = getBlockData(modid, BLOCK_PLACE_ACTION, level, dimension, pos, blockPos);
        if(tag != null && tag.contains("block") && !tag.getString("block").equals(ForgeRegistries.BLOCKS.getKey(block).toString().replace(":", "_")))
            return false;
        return tag != null;
    }

    /**
     * Returns save data if it exists.
     * @param modid
     * @param action
     * @param level
     * @param dimension
     * @param pos
     * @param blockPos
     * @return
     * @throws IOException
     */
    private static CompoundTag getBlockData(String modid, ResourceLocation action, String level, ResourceLocation dimension, ChunkPos pos, BlockPos blockPos) throws IOException {
        CompoundTag tag = getBlockTag(modid, action, level, dimension, pos);
        String key = blockPos.getX() + "," + blockPos.getY() + "," + blockPos.getZ();
        if(tag.contains(key))
            return tag.getCompound(key);
        return null;
    }
}
