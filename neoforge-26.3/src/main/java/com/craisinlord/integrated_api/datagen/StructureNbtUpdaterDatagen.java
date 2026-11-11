package com.craisinlord.integrated_api.datagen;

import com.craisinlord.integrated_api.IntegratedAPI;
import net.minecraft.data.DataGenerator;
import net.neoforged.fml.common.EventBusSubscriber;

// Source: https://github.com/BluSunrize/ImmersiveEngineering/blob/1.20.1/src/datagen/java/blusunrize/immersiveengineering/data/IEDataGenerator.java
@EventBusSubscriber(modid = IntegratedAPI.MODID, bus = EventBusSubscriber.Bus.MOD)
public class StructureNbtUpdaterDatagen {

    @net.neoforged.bus.api.SubscribeEvent
    public static void gatherData(net.neoforged.neoforge.data.event.GatherDataEvent event) {
        net.neoforged.neoforge.common.data.ExistingFileHelper exHelper = event.getExistingFileHelper();
        DataGenerator gen = event.getGenerator();
        final var output = gen.getPackOutput();

        if (event.includeServer()) {
            gen.addProvider(true, new StructureNbtUpdater("structure", IntegratedAPI.MODID, exHelper, output));
        }
    }
}
