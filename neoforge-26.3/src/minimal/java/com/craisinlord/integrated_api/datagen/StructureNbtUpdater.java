package com.craisinlord.integrated_api.datagen;

import com.craisinlord.integrated_api.IntegratedAPI;
import com.google.common.hash.Hashing;
import com.mojang.datafixers.DataFixer;
import com.mojang.datafixers.DataFixerUpper;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.util.datafix.DataFixers;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;

public class StructureNbtUpdater implements DataProvider {
    private final String basePath;
    private final String modid;
    private final PackOutput output;
    private final ResourceManager resources;

    public StructureNbtUpdater(String basePath, String modid, ResourceManager resources, PackOutput output) {
        this.basePath = basePath;
        this.modid = modid;
        this.output = output;
        this.resources = resources;
    }

    @Override
    public @NotNull CompletableFuture<?> run(@NotNull CachedOutput cache) {
        try {
            for (var entry : resources.listResources(basePath, $ -> true).entrySet()) {
                if (entry.getKey().getNamespace().equals(modid)) {
                    process(entry.getKey(), entry.getValue(), cache);
                }
            }
            return CompletableFuture.completedFuture(null);
        }
        catch (IOException x) {
            return CompletableFuture.failedFuture(x);
        }
    }

    private void process(Identifier loc, Resource resource, CachedOutput cache) throws IOException {
        CompoundTag inputNBT;
        try (InputStream stream = resource.open()) {
            inputNBT = NbtIo.readCompressed(stream, NbtAccounter.unlimitedHeap());
        }
        CompoundTag converted = updateNBT(inputNBT);
        if (!converted.equals(inputNBT)) {
            IntegratedAPI.LOGGER.info("Found outdated NBT file: {}", loc);
            Class<? extends DataFixer> fixerClass = DataFixers.getDataFixer().getClass();
            if (!fixerClass.equals(DataFixerUpper.class)) {
                throw new RuntimeException("Structures are not up to date, but unknown data fixer is in use: " + fixerClass.getName());
            }
            writeNBTTo(loc, converted, cache);
        }
    }

    private void writeNBTTo(Identifier loc, CompoundTag data, CachedOutput cache) throws IOException {
        ByteArrayOutputStream bytearrayoutputstream = new ByteArrayOutputStream();
        NbtIo.writeCompressed(data, bytearrayoutputstream);
        byte[] bytes = bytearrayoutputstream.toByteArray();
        Path outputPath = output.getOutputFolder().resolve("data/" + loc.getNamespace() + "/" + loc.getPath());
        cache.writeIfNeeded(outputPath, bytes, Hashing.sha1().hashBytes(bytes));
    }

    private static CompoundTag updateNBT(CompoundTag nbt) {
        final CompoundTag updatedNBT = DataFixTypes.STRUCTURE.updateToCurrentVersion(
                DataFixers.getDataFixer(), nbt, nbt.getIntOr("DataVersion", 0)
        );
        StructureTemplate template = new StructureTemplate();
        template.load(BuiltInRegistries.BLOCK, updatedNBT);
        return template.save(new CompoundTag());
    }

    @NotNull
    @Override
    public String getName() {
        return "Update structure files in " + basePath;
    }
}
