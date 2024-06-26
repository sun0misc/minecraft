/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.data.report;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;
import net.minecraft.component.ComponentMap;
import net.minecraft.data.DataOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.DataWriter;
import net.minecraft.item.Item;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryOps;
import net.minecraft.registry.RegistryWrapper;

public class ItemListProvider
implements DataProvider {
    private final DataOutput output;
    private final CompletableFuture<RegistryWrapper.WrapperLookup> registryLookupFuture;

    public ItemListProvider(DataOutput output, CompletableFuture<RegistryWrapper.WrapperLookup> registryLookupFuture) {
        this.output = output;
        this.registryLookupFuture = registryLookupFuture;
    }

    @Override
    public CompletableFuture<?> run(DataWriter writer) {
        Path path = this.output.resolvePath(DataOutput.OutputType.REPORTS).resolve("items.json");
        return this.registryLookupFuture.thenCompose(registryLookup -> {
            JsonObject jsonObject = new JsonObject();
            RegistryOps<JsonElement> lv = registryLookup.getOps(JsonOps.INSTANCE);
            registryLookup.getWrapperOrThrow(RegistryKeys.ITEM).streamEntries().forEach(entry -> {
                JsonObject jsonObject2 = new JsonObject();
                jsonObject2.add("components", (JsonElement)ComponentMap.CODEC.encodeStart(lv, ((Item)entry.value()).getComponents()).getOrThrow(components -> new IllegalStateException("Failed to encode components: " + components)));
                jsonObject.add(entry.getIdAsString(), jsonObject2);
            });
            return DataProvider.writeToPath(writer, jsonObject, path);
        });
    }

    @Override
    public final String getName() {
        return "Item List";
    }
}

