package com.walkercase.skilling;

import net.minecraftforge.common.ForgeConfigSpec;
import org.apache.commons.lang3.tuple.Pair;

public class SkillingConfig {

    static {
        final Pair<SkillingConfigMain, ForgeConfigSpec> commonSpecPair = new ForgeConfigSpec.Builder().configure(SkillingConfigMain::new);
        commonSpec = commonSpecPair.getRight();
        COMMON = commonSpecPair.getLeft();
    }

    public static final ForgeConfigSpec commonSpec;
    public static final SkillingConfigMain COMMON;

    public static class SkillingConfigMain {
        public Client CLIENT;

        public SkillingConfigMain(ForgeConfigSpec.Builder builder) {
            CLIENT = new Client(builder);
        }
    }

    public static class Client{
        public final ForgeConfigSpec.ConfigValue<Boolean> showAllEntries;
        public Client(ForgeConfigSpec.Builder builder){
            builder.push("client");
            {
                this.showAllEntries = builder.comment("Enabled to show all skill book entries..").define("showAllEntries", false);
            }
            builder.pop();
        }
    }
}
