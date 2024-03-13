package com.walkercase.skilling.item;

import com.walkercase.skilling.Skilling;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class SkillingItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, Skilling.MODID);

    public static final RegistryObject<SkillingBook> SKILLING_BOOK = ITEMS.register("skilling_book", SkillingBook::new);


}
