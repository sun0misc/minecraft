/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.datafixer;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFixer;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import java.util.Set;
import net.minecraft.SharedConstants;
import net.minecraft.datafixer.TypeReferences;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtOps;

public enum DataFixTypes {
    LEVEL(TypeReferences.LEVEL),
    PLAYER(TypeReferences.PLAYER),
    CHUNK(TypeReferences.CHUNK),
    HOTBAR(TypeReferences.HOTBAR),
    OPTIONS(TypeReferences.OPTIONS),
    STRUCTURE(TypeReferences.STRUCTURE),
    STATS(TypeReferences.STATS),
    SAVED_DATA_COMMAND_STORAGE(TypeReferences.SAVED_DATA_COMMAND_STORAGE),
    SAVED_DATA_FORCED_CHUNKS(TypeReferences.SAVED_DATA_CHUNKS),
    SAVED_DATA_MAP_DATA(TypeReferences.SAVED_DATA_MAP_DATA),
    SAVED_DATA_MAP_INDEX(TypeReferences.SAVED_DATA_IDCOUNTS),
    SAVED_DATA_RAIDS(TypeReferences.SAVED_DATA_RAIDS),
    SAVED_DATA_RANDOM_SEQUENCES(TypeReferences.SAVED_DATA_RANDOM_SEQUENCES),
    SAVED_DATA_SCOREBOARD(TypeReferences.SAVED_DATA_SCOREBOARD),
    SAVED_DATA_STRUCTURE_FEATURE_INDICES(TypeReferences.SAVED_DATA_STRUCTURE_FEATURE_INDICES),
    ADVANCEMENTS(TypeReferences.ADVANCEMENTS),
    POI_CHUNK(TypeReferences.POI_CHUNK),
    WORLD_GEN_SETTINGS(TypeReferences.WORLD_GEN_SETTINGS),
    ENTITY_CHUNK(TypeReferences.ENTITY_CHUNK);

    public static final Set<DSL.TypeReference> REQUIRED_TYPES;
    private final DSL.TypeReference typeReference;

    private DataFixTypes(DSL.TypeReference typeReference) {
        this.typeReference = typeReference;
    }

    static int getSaveVersionId() {
        return SharedConstants.getGameVersion().getSaveVersion().getId();
    }

    public <A> Codec<A> createDataFixingCodec(final Codec<A> baseCodec, final DataFixer dataFixer, final int currentDataVersion) {
        return new Codec<A>(){

            @Override
            public <T> DataResult<T> encode(A input, DynamicOps<T> ops, T prefix) {
                return baseCodec.encode(input, ops, prefix).flatMap((? super R encoded) -> ops.mergeToMap(encoded, ops.createString("DataVersion"), ops.createInt(DataFixTypes.getSaveVersionId())));
            }

            @Override
            public <T> DataResult<Pair<A, T>> decode(DynamicOps<T> ops, T input) {
                int i = ops.get(input, "DataVersion").flatMap(ops::getNumberValue).map(Number::intValue).result().orElse(currentDataVersion);
                Dynamic<T> dynamic = new Dynamic<T>(ops, ops.remove(input, "DataVersion"));
                Dynamic<T> dynamic2 = DataFixTypes.this.update(dataFixer, dynamic, i);
                return baseCodec.decode(dynamic2);
            }
        };
    }

    public <T> Dynamic<T> update(DataFixer dataFixer, Dynamic<T> dynamic, int oldVersion, int newVersion) {
        return dataFixer.update(this.typeReference, dynamic, oldVersion, newVersion);
    }

    public <T> Dynamic<T> update(DataFixer dataFixer, Dynamic<T> dynamic, int oldVersion) {
        return this.update(dataFixer, dynamic, oldVersion, DataFixTypes.getSaveVersionId());
    }

    public NbtCompound update(DataFixer dataFixer, NbtCompound nbt, int oldVersion, int newVersion) {
        return this.update(dataFixer, new Dynamic<NbtCompound>(NbtOps.INSTANCE, nbt), oldVersion, newVersion).getValue();
    }

    public NbtCompound update(DataFixer dataFixer, NbtCompound nbt, int oldVersion) {
        return this.update(dataFixer, nbt, oldVersion, DataFixTypes.getSaveVersionId());
    }

    static {
        REQUIRED_TYPES = Set.of(DataFixTypes.LEVEL.typeReference);
    }
}

