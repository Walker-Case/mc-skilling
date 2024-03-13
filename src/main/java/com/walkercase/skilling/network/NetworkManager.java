package com.walkercase.skilling.network;

import com.walkercase.skilling.Skilling;
import com.walkercase.skilling.api.action.skill.Skill;
import com.walkercase.skilling.api.action.skill.SkillManager;
import com.walkercase.skilling.client.gui.screen.SkillingBookScreen;
import com.walkercase.skilling.client.skill.SkillClientEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

import java.util.ArrayList;
import java.util.function.Supplier;

public class NetworkManager {

    private static final String PROTOCOL_VERSION = "1";
    public static final SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(Skilling.MODID, "main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );


    private static int LAST_PACKET_ID = 0;

    public static void registerPackets() {
        INSTANCE.registerMessage(LAST_PACKET_ID++, SkillXpUpdatePacket.class, SkillXpUpdatePacket::encoder, SkillXpUpdatePacket::decoder, SkillXpUpdatePacket::consumer);
        INSTANCE.registerMessage(LAST_PACKET_ID++, OpenSkillBookPacket.class, OpenSkillBookPacket::encoder, OpenSkillBookPacket::decoder, OpenSkillBookPacket::consumer);
        INSTANCE.registerMessage(LAST_PACKET_ID++, SkillBiomeUpdate.class, SkillBiomeUpdate::encoder, SkillBiomeUpdate::decoder, SkillBiomeUpdate::consumer);
    }

    public static class OpenSkillBookPacket {


        public void encoder(FriendlyByteBuf buf) {
        }

        public static OpenSkillBookPacket decoder(FriendlyByteBuf buf) {
            return new OpenSkillBookPacket();
        }

        public void consumer(Supplier<NetworkEvent.Context> contextSupplier) {
            contextSupplier.get().enqueueWork(() ->
                    DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> this::openScreen));
            contextSupplier.get().setPacketHandled(true);
        }

        @OnlyIn(Dist.CLIENT)
        public void openScreen(){
            Minecraft.getInstance().setScreen(new SkillingBookScreen());
        }
    }

    public static class SkillXpUpdatePacket {
        public Skill skill;
        public int currentLevel;
        public double currentXp;
        public boolean display = true;

        public SkillXpUpdatePacket(Skill skill, int currentLevel, double currentXp, boolean display) {
            this.skill = skill;
            this.currentLevel = currentLevel;
            this.currentXp = currentXp;
            this.display = display;
        }

        public void encoder(FriendlyByteBuf buf) {
            buf.writeBoolean(display);
            buf.writeInt(currentLevel);
            buf.writeDouble(currentXp);
            buf.writeResourceLocation(skill.getUnlocalizedName());
        }

        public static SkillXpUpdatePacket decoder(FriendlyByteBuf buf) {
            boolean display = buf.readBoolean();
            int level = buf.readInt();
            double xp = buf.readDouble();
            ResourceLocation rl = buf.readResourceLocation();

            Skill opt = SkillManager.getSkill(rl);
            if (opt != null)
                return new SkillXpUpdatePacket(opt, level, xp, display);
            else
                throw new NullPointerException("Bad skill in packet: " + rl);
        }

        public void consumer(Supplier<NetworkEvent.Context> contextSupplier) {
            contextSupplier.get().enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
                SkillClientEvents.updateClient(skill, currentLevel, currentXp, display);
            }));
            contextSupplier.get().setPacketHandled(true);
        }
    }

    public static class SkillBiomeUpdate {
        public String[] locatedBiomes;
        public SkillBiomeUpdate(String[] locatedBiomes) {
            this.locatedBiomes = locatedBiomes;
        }

        public void encoder(FriendlyByteBuf buf) {
            buf.writeInt(this.locatedBiomes.length);
            for(String s : locatedBiomes)
                buf.writeUtf(s);
        }

        public static SkillBiomeUpdate decoder(FriendlyByteBuf buf) {
            int count = buf.readInt();
            if(count == 0){
                return new SkillBiomeUpdate(new String[0]);
            }
            ArrayList<String> sb = new ArrayList<>();
            for(int i=0;i<count;i++){
                sb.add(buf.readUtf());
            }
            return new SkillBiomeUpdate(sb.toArray(new String[0]));
        }

        public void consumer(Supplier<NetworkEvent.Context> contextSupplier) {
            contextSupplier.get().enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
                SkillClientEvents.updateClientLocatedBiomes(locatedBiomes);
            }));
            contextSupplier.get().setPacketHandled(true);
        }
    }
}
