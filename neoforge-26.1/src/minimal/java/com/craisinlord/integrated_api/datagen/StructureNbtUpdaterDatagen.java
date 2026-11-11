package com.craisinlord.integrated_api.datagen;

import com.craisinlord.integrated_api.IntegratedAPI;
import net.minecraft.data.DataGenerator;
import net.minecraft.server.packs.PackType;
import net.neoforged.fml.common.EventBusSubscriber;

@EventBusSubscriber(modid = IntegratedAPI.MODID)
public class StructureNbtUpdaterDatagen {

    @net.neoforged.bus.api.SubscribeEvent
    public static void gatherData(net.neoforged.neoforge.data.event.GatherDataEvent.Server event) {
        DataGenerator gen = event.getGenerator();
        final var output = gen.getPackOutput();
        gen.addProvider(true, new StructureNbtUpdater("structure", IntegratedAPI.MODID, event.getResourceManager(PackType.SERVER_DATA), output));
    }
}
