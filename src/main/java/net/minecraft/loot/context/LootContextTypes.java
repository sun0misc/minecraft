package net.minecraft.loot.context;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import java.util.function.Consumer;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

public class LootContextTypes {
   private static final BiMap MAP = HashBiMap.create();
   public static final LootContextType EMPTY = register("empty", (builder) -> {
   });
   public static final LootContextType CHEST = register("chest", (builder) -> {
      builder.require(LootContextParameters.ORIGIN).allow(LootContextParameters.THIS_ENTITY);
   });
   public static final LootContextType COMMAND = register("command", (builder) -> {
      builder.require(LootContextParameters.ORIGIN).allow(LootContextParameters.THIS_ENTITY);
   });
   public static final LootContextType SELECTOR = register("selector", (builder) -> {
      builder.require(LootContextParameters.ORIGIN).require(LootContextParameters.THIS_ENTITY);
   });
   public static final LootContextType FISHING = register("fishing", (builder) -> {
      builder.require(LootContextParameters.ORIGIN).require(LootContextParameters.TOOL).allow(LootContextParameters.THIS_ENTITY);
   });
   public static final LootContextType ENTITY = register("entity", (builder) -> {
      builder.require(LootContextParameters.THIS_ENTITY).require(LootContextParameters.ORIGIN).require(LootContextParameters.DAMAGE_SOURCE).allow(LootContextParameters.KILLER_ENTITY).allow(LootContextParameters.DIRECT_KILLER_ENTITY).allow(LootContextParameters.LAST_DAMAGE_PLAYER);
   });
   public static final LootContextType ARCHAEOLOGY = register("archaeology", (builder) -> {
      builder.require(LootContextParameters.ORIGIN).allow(LootContextParameters.THIS_ENTITY);
   });
   public static final LootContextType GIFT = register("gift", (builder) -> {
      builder.require(LootContextParameters.ORIGIN).require(LootContextParameters.THIS_ENTITY);
   });
   public static final LootContextType BARTER = register("barter", (builder) -> {
      builder.require(LootContextParameters.THIS_ENTITY);
   });
   public static final LootContextType ADVANCEMENT_REWARD = register("advancement_reward", (builder) -> {
      builder.require(LootContextParameters.THIS_ENTITY).require(LootContextParameters.ORIGIN);
   });
   public static final LootContextType ADVANCEMENT_ENTITY = register("advancement_entity", (builder) -> {
      builder.require(LootContextParameters.THIS_ENTITY).require(LootContextParameters.ORIGIN);
   });
   public static final LootContextType GENERIC = register("generic", (builder) -> {
      builder.require(LootContextParameters.THIS_ENTITY).require(LootContextParameters.LAST_DAMAGE_PLAYER).require(LootContextParameters.DAMAGE_SOURCE).require(LootContextParameters.KILLER_ENTITY).require(LootContextParameters.DIRECT_KILLER_ENTITY).require(LootContextParameters.ORIGIN).require(LootContextParameters.BLOCK_STATE).require(LootContextParameters.BLOCK_ENTITY).require(LootContextParameters.TOOL).require(LootContextParameters.EXPLOSION_RADIUS);
   });
   public static final LootContextType BLOCK = register("block", (builder) -> {
      builder.require(LootContextParameters.BLOCK_STATE).require(LootContextParameters.ORIGIN).require(LootContextParameters.TOOL).allow(LootContextParameters.THIS_ENTITY).allow(LootContextParameters.BLOCK_ENTITY).allow(LootContextParameters.EXPLOSION_RADIUS);
   });

   private static LootContextType register(String name, Consumer type) {
      LootContextType.Builder lv = new LootContextType.Builder();
      type.accept(lv);
      LootContextType lv2 = lv.build();
      Identifier lv3 = new Identifier(name);
      LootContextType lv4 = (LootContextType)MAP.put(lv3, lv2);
      if (lv4 != null) {
         throw new IllegalStateException("Loot table parameter set " + lv3 + " is already registered");
      } else {
         return lv2;
      }
   }

   @Nullable
   public static LootContextType get(Identifier id) {
      return (LootContextType)MAP.get(id);
   }

   @Nullable
   public static Identifier getId(LootContextType type) {
      return (Identifier)MAP.inverse().get(type);
   }
}
