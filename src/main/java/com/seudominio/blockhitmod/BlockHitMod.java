package com.seudominio.blockhitmod;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.common.MinecraftForge;

@Mod(modid = BlockHitMod.MODID, name = BlockHitMod.NAME, version = BlockHitMod.VERSION)
public class BlockHitMod {
    public static final String MODID = "blockhitmod";
    public static final String NAME = "BlockHit Mod";
    public static final String VERSION = "1.0";

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        MinecraftForge.EVENT_BUS.register(new BlockHitListener());
    }
}
