package net.minecraft.datafixer;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFixer;
import com.mojang.serialization.Dynamic;
import java.util.Set;
import net.minecraft.SharedConstants;
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
   SAVED_DATA(TypeReferences.SAVED_DATA),
   ADVANCEMENTS(TypeReferences.ADVANCEMENTS),
   POI_CHUNK(TypeReferences.POI_CHUNK),
   WORLD_GEN_SETTINGS(TypeReferences.WORLD_GEN_SETTINGS),
   ENTITY_CHUNK(TypeReferences.ENTITY_CHUNK);

   public static final Set REQUIRED_TYPES = Set.of(LEVEL.typeReference);
   private final DSL.TypeReference typeReference;

   private DataFixTypes(DSL.TypeReference typeReference) {
      this.typeReference = typeReference;
   }

   private static int getSaveVersionId() {
      return SharedConstants.getGameVersion().getSaveVersion().getId();
   }

   public Dynamic update(DataFixer dataFixer, Dynamic dynamic, int oldVersion, int newVersion) {
      return dataFixer.update(this.typeReference, dynamic, oldVersion, newVersion);
   }

   public Dynamic update(DataFixer dataFixer, Dynamic dynamic, int oldVersion) {
      return this.update(dataFixer, dynamic, oldVersion, getSaveVersionId());
   }

   public NbtCompound update(DataFixer dataFixer, NbtCompound nbt, int oldVersion, int newVersion) {
      return (NbtCompound)this.update(dataFixer, new Dynamic(NbtOps.INSTANCE, nbt), oldVersion, newVersion).getValue();
   }

   public NbtCompound update(DataFixer dataFixer, NbtCompound nbt, int oldVersion) {
      return this.update(dataFixer, nbt, oldVersion, getSaveVersionId());
   }

   // $FF: synthetic method
   private static DataFixTypes[] method_36589() {
      return new DataFixTypes[]{LEVEL, PLAYER, CHUNK, HOTBAR, OPTIONS, STRUCTURE, STATS, SAVED_DATA, ADVANCEMENTS, POI_CHUNK, WORLD_GEN_SETTINGS, ENTITY_CHUNK};
   }
}
