package com.walkercase.skilling.item;

import com.walkercase.skilling.network.NetworkManager;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.PacketDistributor;

import javax.annotation.Nullable;
import java.util.List;

public class SkillingBook extends Item {
    public SkillingBook() {
        super(new Item.Properties().stacksTo(1).fireResistant().rarity(Rarity.EPIC));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        if(!level.isClientSide && hand == InteractionHand.MAIN_HAND){
                NetworkManager.INSTANCE.send(PacketDistributor.PLAYER.with(() -> (ServerPlayer) player),
                        new NetworkManager.OpenSkillBookPacket());
        }
        return InteractionResultHolder.sidedSuccess(player.getItemInHand(hand), true);
    }

    @Override
    public void appendHoverText(ItemStack is, @Nullable Level level, List<Component> list, TooltipFlag p_41424_) {
        super.appendHoverText(is, level, list, p_41424_);

        list.add(Component.translatable("skilling.item.skilling_book.tooltip").withStyle(ChatFormatting.LIGHT_PURPLE));
    }

}
