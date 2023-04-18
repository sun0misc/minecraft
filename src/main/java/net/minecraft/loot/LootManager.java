package net.minecraft.loot;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import com.mojang.logging.LogUtils;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.BiFunction;
import java.util.function.Predicate;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.condition.LootCondition;
import net.minecraft.loot.condition.LootConditionType;
import net.minecraft.loot.condition.LootConditionTypes;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.context.LootContextTypes;
import net.minecraft.loot.function.LootFunction;
import net.minecraft.loot.function.LootFunctionType;
import net.minecraft.loot.function.LootFunctionTypes;
import net.minecraft.resource.JsonDataLoader;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceReloader;
import net.minecraft.util.profiler.Profiler;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

public class LootManager implements ResourceReloader, LootDataLookup {
   private static final Logger LOGGER = LogUtils.getLogger();
   public static final LootDataKey EMPTY_LOOT_TABLE;
   private Map keyToValue = Map.of();
   private Multimap typeToIds = ImmutableMultimap.of();

   public final CompletableFuture reload(ResourceReloader.Synchronizer synchronizer, ResourceManager manager, Profiler prepareProfiler, Profiler applyProfiler, Executor prepareExecutor, Executor applyExecutor) {
      Map map = new HashMap();
      CompletableFuture[] completableFutures = (CompletableFuture[])LootDataType.stream().map((type) -> {
         return load(type, manager, prepareExecutor, map);
      }).toArray((i) -> {
         return new CompletableFuture[i];
      });
      CompletableFuture var10000 = CompletableFuture.allOf(completableFutures);
      Objects.requireNonNull(synchronizer);
      return var10000.thenCompose(synchronizer::whenPrepared).thenAcceptAsync((v) -> {
         this.validate(map);
      }, applyExecutor);
   }

   private static CompletableFuture load(LootDataType type, ResourceManager resourceManager, Executor executor, Map results) {
      Map map2 = new HashMap();
      results.put(type, map2);
      return CompletableFuture.runAsync(() -> {
         Map map2x = new HashMap();
         JsonDataLoader.load(resourceManager, type.getId(), type.getGson(), map2x);
         map2x.forEach((id, json) -> {
            type.parse(id, json).ifPresent((value) -> {
               map2.put(id, value);
            });
         });
      }, executor);
   }

   private void validate(Map lootData) {
      Object object = ((Map)lootData.get(LootDataType.LOOT_TABLES)).remove(LootTables.EMPTY);
      if (object != null) {
         LOGGER.warn("Datapack tried to redefine {} loot table, ignoring", LootTables.EMPTY);
      }

      ImmutableMap.Builder builder = ImmutableMap.builder();
      ImmutableMultimap.Builder builder2 = ImmutableMultimap.builder();
      lootData.forEach((type, idToValue) -> {
         idToValue.forEach((id, value) -> {
            builder.put(new LootDataKey(type, id), value);
            builder2.put(type, id);
         });
      });
      builder.put(EMPTY_LOOT_TABLE, LootTable.EMPTY);
      final Map map2 = builder.build();
      LootTableReporter lv = new LootTableReporter(LootContextTypes.GENERIC, new LootDataLookup() {
         @Nullable
         public Object getElement(LootDataKey arg) {
            return map2.get(arg);
         }
      });
      map2.forEach((key, value) -> {
         validate(lv, key, value);
      });
      lv.getMessages().forEach((name, message) -> {
         LOGGER.warn("Found loot table element validation problem in {}: {}", name, message);
      });
      this.keyToValue = map2;
      this.typeToIds = builder2.build();
   }

   private static void validate(LootTableReporter reporter, LootDataKey key, Object value) {
      key.type().validate(reporter, key, value);
   }

   @Nullable
   public Object getElement(LootDataKey arg) {
      return this.keyToValue.get(arg);
   }

   public Collection getIds(LootDataType type) {
      return this.typeToIds.get(type);
   }

   public static LootCondition and(LootCondition[] predicates) {
      return new AndCondition(predicates);
   }

   public static LootFunction and(LootFunction[] modifiers) {
      return new AndFunction(modifiers);
   }

   static {
      EMPTY_LOOT_TABLE = new LootDataKey(LootDataType.LOOT_TABLES, LootTables.EMPTY);
   }

   private static class AndCondition implements LootCondition {
      private final LootCondition[] terms;
      private final Predicate predicate;

      AndCondition(LootCondition[] terms) {
         this.terms = terms;
         this.predicate = LootConditionTypes.joinAnd(terms);
      }

      public final boolean test(LootContext arg) {
         return this.predicate.test(arg);
      }

      public void validate(LootTableReporter reporter) {
         LootCondition.super.validate(reporter);

         for(int i = 0; i < this.terms.length; ++i) {
            this.terms[i].validate(reporter.makeChild(".term[" + i + "]"));
         }

      }

      public LootConditionType getType() {
         throw new UnsupportedOperationException();
      }

      // $FF: synthetic method
      public boolean test(Object context) {
         return this.test((LootContext)context);
      }
   }

   private static class AndFunction implements LootFunction {
      protected final LootFunction[] functions;
      private final BiFunction applier;

      public AndFunction(LootFunction[] functions) {
         this.functions = functions;
         this.applier = LootFunctionTypes.join(functions);
      }

      public ItemStack apply(ItemStack arg, LootContext arg2) {
         return (ItemStack)this.applier.apply(arg, arg2);
      }

      public void validate(LootTableReporter reporter) {
         LootFunction.super.validate(reporter);

         for(int i = 0; i < this.functions.length; ++i) {
            this.functions[i].validate(reporter.makeChild(".function[" + i + "]"));
         }

      }

      public LootFunctionType getType() {
         throw new UnsupportedOperationException();
      }

      // $FF: synthetic method
      public Object apply(Object stack, Object context) {
         return this.apply((ItemStack)stack, (LootContext)context);
      }
   }
}
