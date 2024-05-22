/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.registry;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.Lifecycle;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.stream.Stream;
import net.minecraft.loot.LootDataType;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.LootTableReporter;
import net.minecraft.loot.LootTables;
import net.minecraft.loot.context.LootContextTypes;
import net.minecraft.registry.CombinedDynamicRegistries;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.MutableRegistry;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryEntryLookup;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryOps;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.ServerDynamicRegistryType;
import net.minecraft.registry.SimpleRegistry;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.entry.RegistryEntryInfo;
import net.minecraft.resource.JsonDataLoader;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.ErrorReporter;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import org.slf4j.Logger;

public class ReloadableRegistries {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Gson GSON = new GsonBuilder().create();
    private static final RegistryEntryInfo DEFAULT_REGISTRY_ENTRY_INFO = new RegistryEntryInfo(Optional.empty(), Lifecycle.experimental());

    public static CompletableFuture<CombinedDynamicRegistries<ServerDynamicRegistryType>> reload(CombinedDynamicRegistries<ServerDynamicRegistryType> dynamicRegistries, ResourceManager resourceManager, Executor prepareExecutor) {
        DynamicRegistryManager.Immutable lv = dynamicRegistries.getPrecedingRegistryManagers(ServerDynamicRegistryType.RELOADABLE);
        RegistryOps<JsonElement> lv2 = new ReloadableWrapperLookup(lv).getOps(JsonOps.INSTANCE);
        List<CompletableFuture> list = LootDataType.stream().map(type -> ReloadableRegistries.prepare(type, lv2, resourceManager, prepareExecutor)).toList();
        CompletableFuture completableFuture = Util.combineSafe(list);
        return completableFuture.thenApplyAsync(registries -> ReloadableRegistries.apply(dynamicRegistries, registries), prepareExecutor);
    }

    private static <T> CompletableFuture<MutableRegistry<?>> prepare(LootDataType<T> type, RegistryOps<JsonElement> ops, ResourceManager resourceManager, Executor prepareExecutor) {
        return CompletableFuture.supplyAsync(() -> {
            SimpleRegistry lv = new SimpleRegistry(type.registryKey(), Lifecycle.experimental());
            HashMap<Identifier, JsonElement> map = new HashMap<Identifier, JsonElement>();
            String string = RegistryKeys.method_60915(type.registryKey());
            JsonDataLoader.load(resourceManager, string, GSON, map);
            map.forEach((id, json) -> type.parse((Identifier)id, ops, json).ifPresent(value -> lv.add(RegistryKey.of(type.registryKey(), id), value, DEFAULT_REGISTRY_ENTRY_INFO)));
            return lv;
        }, prepareExecutor);
    }

    private static CombinedDynamicRegistries<ServerDynamicRegistryType> apply(CombinedDynamicRegistries<ServerDynamicRegistryType> dynamicRegistries, List<MutableRegistry<?>> registries) {
        CombinedDynamicRegistries<ServerDynamicRegistryType> lv = ReloadableRegistries.with(dynamicRegistries, registries);
        ErrorReporter.Impl lv2 = new ErrorReporter.Impl();
        DynamicRegistryManager.Immutable lv3 = lv.getCombinedRegistryManager();
        LootTableReporter lv4 = new LootTableReporter(lv2, LootContextTypes.GENERIC, lv3.createRegistryLookup());
        LootDataType.stream().forEach(lootDataType -> ReloadableRegistries.validateLootData(lv4, lootDataType, lv3));
        lv2.getErrors().forEach((path, message) -> LOGGER.warn("Found loot table element validation problem in {}: {}", path, message));
        return lv;
    }

    private static CombinedDynamicRegistries<ServerDynamicRegistryType> with(CombinedDynamicRegistries<ServerDynamicRegistryType> dynamicRegistries, List<MutableRegistry<?>> registries) {
        DynamicRegistryManager.ImmutableImpl lv = new DynamicRegistryManager.ImmutableImpl(registries);
        ((MutableRegistry)lv.get(RegistryKeys.LOOT_TABLE)).add(LootTables.EMPTY, LootTable.EMPTY, DEFAULT_REGISTRY_ENTRY_INFO);
        return dynamicRegistries.with(ServerDynamicRegistryType.RELOADABLE, lv.toImmutable());
    }

    private static <T> void validateLootData(LootTableReporter reporter, LootDataType<T> lootDataType, DynamicRegistryManager registryManager) {
        Registry<T> lv = registryManager.get(lootDataType.registryKey());
        lv.streamEntries().forEach(entry -> lootDataType.validate(reporter, entry.registryKey(), entry.value()));
    }

    static class ReloadableWrapperLookup
    implements RegistryWrapper.WrapperLookup {
        private final DynamicRegistryManager registryManager;

        ReloadableWrapperLookup(DynamicRegistryManager registryManager) {
            this.registryManager = registryManager;
        }

        @Override
        public Stream<RegistryKey<? extends Registry<?>>> streamAllRegistryKeys() {
            return this.registryManager.streamAllRegistryKeys();
        }

        @Override
        public <T> Optional<RegistryWrapper.Impl<T>> getOptionalWrapper(RegistryKey<? extends Registry<? extends T>> registryRef) {
            return this.registryManager.getOptional(registryRef).map(Registry::getTagCreatingWrapper);
        }
    }

    public static class Lookup {
        private final DynamicRegistryManager.Immutable registryManager;

        public Lookup(DynamicRegistryManager.Immutable registryManager) {
            this.registryManager = registryManager;
        }

        public DynamicRegistryManager.Immutable getRegistryManager() {
            return this.registryManager;
        }

        public RegistryEntryLookup.RegistryLookup createRegistryLookup() {
            return this.registryManager.createRegistryLookup();
        }

        public Collection<Identifier> getIds(RegistryKey<? extends Registry<?>> registryRef) {
            return this.registryManager.getOptional(registryRef).stream().flatMap(registry -> registry.streamEntries().map(entry -> entry.registryKey().getValue())).toList();
        }

        public LootTable getLootTable(RegistryKey<LootTable> key) {
            return this.registryManager.getOptionalWrapper(RegistryKeys.LOOT_TABLE).flatMap(registryEntryLookup -> registryEntryLookup.getOptional(key)).map(RegistryEntry::value).orElse(LootTable.EMPTY);
        }
    }
}

