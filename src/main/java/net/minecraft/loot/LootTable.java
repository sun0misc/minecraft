package net.minecraft.loot;

import com.google.common.collect.Lists;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectListIterator;
import java.lang.reflect.Type;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.context.LootContextType;
import net.minecraft.loot.context.LootContextTypes;
import net.minecraft.loot.function.LootFunction;
import net.minecraft.loot.function.LootFunctionConsumingBuilder;
import net.minecraft.loot.function.LootFunctionTypes;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.Util;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.random.Random;
import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;

public class LootTable {
   static final Logger LOGGER = LogUtils.getLogger();
   public static final LootTable EMPTY;
   public static final LootContextType GENERIC;
   final LootContextType type;
   final LootPool[] pools;
   final LootFunction[] functions;
   private final BiFunction combinedFunction;

   LootTable(LootContextType type, LootPool[] pools, LootFunction[] functions) {
      this.type = type;
      this.pools = pools;
      this.functions = functions;
      this.combinedFunction = LootFunctionTypes.join(functions);
   }

   public static Consumer processStacks(LootContext context, Consumer consumer) {
      return (stack) -> {
         if (stack.isItemEnabled(context.getWorld().getEnabledFeatures())) {
            if (stack.getCount() < stack.getMaxCount()) {
               consumer.accept(stack);
            } else {
               int i = stack.getCount();

               while(i > 0) {
                  ItemStack lv = stack.copyWithCount(Math.min(stack.getMaxCount(), i));
                  i -= lv.getCount();
                  consumer.accept(lv);
               }
            }

         }
      };
   }

   public void generateUnprocessedLoot(LootContext context, Consumer lootConsumer) {
      LootContext.Entry lv = LootContext.table(this);
      if (context.markActive(lv)) {
         Consumer consumer2 = LootFunction.apply(this.combinedFunction, lootConsumer, context);
         LootPool[] var5 = this.pools;
         int var6 = var5.length;

         for(int var7 = 0; var7 < var6; ++var7) {
            LootPool lv2 = var5[var7];
            lv2.addGeneratedLoot(consumer2, context);
         }

         context.markInactive(lv);
      } else {
         LOGGER.warn("Detected infinite loop in loot tables");
      }

   }

   public void generateLoot(LootContext context, Consumer lootConsumer) {
      this.generateUnprocessedLoot(context, processStacks(context, lootConsumer));
   }

   public ObjectArrayList generateLoot(LootContext context) {
      ObjectArrayList objectArrayList = new ObjectArrayList();
      Objects.requireNonNull(objectArrayList);
      this.generateLoot(context, objectArrayList::add);
      return objectArrayList;
   }

   public LootContextType getType() {
      return this.type;
   }

   public void validate(LootTableReporter reporter) {
      int i;
      for(i = 0; i < this.pools.length; ++i) {
         this.pools[i].validate(reporter.makeChild(".pools[" + i + "]"));
      }

      for(i = 0; i < this.functions.length; ++i) {
         this.functions[i].validate(reporter.makeChild(".functions[" + i + "]"));
      }

   }

   public void supplyInventory(Inventory inventory, LootContext context) {
      ObjectArrayList objectArrayList = this.generateLoot(context);
      Random lv = context.getRandom();
      List list = this.getFreeSlots(inventory, lv);
      this.shuffle(objectArrayList, list.size(), lv);
      ObjectListIterator var6 = objectArrayList.iterator();

      while(var6.hasNext()) {
         ItemStack lv2 = (ItemStack)var6.next();
         if (list.isEmpty()) {
            LOGGER.warn("Tried to over-fill a container");
            return;
         }

         if (lv2.isEmpty()) {
            inventory.setStack((Integer)list.remove(list.size() - 1), ItemStack.EMPTY);
         } else {
            inventory.setStack((Integer)list.remove(list.size() - 1), lv2);
         }
      }

   }

   private void shuffle(ObjectArrayList drops, int freeSlots, Random random) {
      List list = Lists.newArrayList();
      Iterator iterator = drops.iterator();

      while(iterator.hasNext()) {
         ItemStack lv = (ItemStack)iterator.next();
         if (lv.isEmpty()) {
            iterator.remove();
         } else if (lv.getCount() > 1) {
            list.add(lv);
            iterator.remove();
         }
      }

      while(freeSlots - drops.size() - list.size() > 0 && !list.isEmpty()) {
         ItemStack lv2 = (ItemStack)list.remove(MathHelper.nextInt(random, 0, list.size() - 1));
         int j = MathHelper.nextInt(random, 1, lv2.getCount() / 2);
         ItemStack lv3 = lv2.split(j);
         if (lv2.getCount() > 1 && random.nextBoolean()) {
            list.add(lv2);
         } else {
            drops.add(lv2);
         }

         if (lv3.getCount() > 1 && random.nextBoolean()) {
            list.add(lv3);
         } else {
            drops.add(lv3);
         }
      }

      drops.addAll(list);
      Util.shuffle(drops, random);
   }

   private List getFreeSlots(Inventory inventory, Random random) {
      ObjectArrayList objectArrayList = new ObjectArrayList();

      for(int i = 0; i < inventory.size(); ++i) {
         if (inventory.getStack(i).isEmpty()) {
            objectArrayList.add(i);
         }
      }

      Util.shuffle(objectArrayList, random);
      return objectArrayList;
   }

   public static Builder builder() {
      return new Builder();
   }

   static {
      EMPTY = new LootTable(LootContextTypes.EMPTY, new LootPool[0], new LootFunction[0]);
      GENERIC = LootContextTypes.GENERIC;
   }

   public static class Builder implements LootFunctionConsumingBuilder {
      private final List pools = Lists.newArrayList();
      private final List functions = Lists.newArrayList();
      private LootContextType type;

      public Builder() {
         this.type = LootTable.GENERIC;
      }

      public Builder pool(LootPool.Builder poolBuilder) {
         this.pools.add(poolBuilder.build());
         return this;
      }

      public Builder type(LootContextType context) {
         this.type = context;
         return this;
      }

      public Builder apply(LootFunction.Builder arg) {
         this.functions.add(arg.build());
         return this;
      }

      public Builder getThisFunctionConsumingBuilder() {
         return this;
      }

      public LootTable build() {
         return new LootTable(this.type, (LootPool[])this.pools.toArray(new LootPool[0]), (LootFunction[])this.functions.toArray(new LootFunction[0]));
      }

      // $FF: synthetic method
      public LootFunctionConsumingBuilder getThisFunctionConsumingBuilder() {
         return this.getThisFunctionConsumingBuilder();
      }

      // $FF: synthetic method
      public LootFunctionConsumingBuilder apply(LootFunction.Builder function) {
         return this.apply(function);
      }
   }

   public static class Serializer implements JsonDeserializer, JsonSerializer {
      public LootTable deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
         JsonObject jsonObject = JsonHelper.asObject(jsonElement, "loot table");
         LootPool[] lvs = (LootPool[])JsonHelper.deserialize(jsonObject, "pools", new LootPool[0], jsonDeserializationContext, LootPool[].class);
         LootContextType lv = null;
         if (jsonObject.has("type")) {
            String string = JsonHelper.getString(jsonObject, "type");
            lv = LootContextTypes.get(new Identifier(string));
         }

         LootFunction[] lvs2 = (LootFunction[])JsonHelper.deserialize(jsonObject, "functions", new LootFunction[0], jsonDeserializationContext, LootFunction[].class);
         return new LootTable(lv != null ? lv : LootContextTypes.GENERIC, lvs, lvs2);
      }

      public JsonElement serialize(LootTable arg, Type type, JsonSerializationContext jsonSerializationContext) {
         JsonObject jsonObject = new JsonObject();
         if (arg.type != LootTable.GENERIC) {
            Identifier lv = LootContextTypes.getId(arg.type);
            if (lv != null) {
               jsonObject.addProperty("type", lv.toString());
            } else {
               LootTable.LOGGER.warn("Failed to find id for param set {}", arg.type);
            }
         }

         if (arg.pools.length > 0) {
            jsonObject.add("pools", jsonSerializationContext.serialize(arg.pools));
         }

         if (!ArrayUtils.isEmpty(arg.functions)) {
            jsonObject.add("functions", jsonSerializationContext.serialize(arg.functions));
         }

         return jsonObject;
      }

      // $FF: synthetic method
      public JsonElement serialize(Object supplier, Type unused, JsonSerializationContext context) {
         return this.serialize((LootTable)supplier, unused, context);
      }

      // $FF: synthetic method
      public Object deserialize(JsonElement json, Type unused, JsonDeserializationContext context) throws JsonParseException {
         return this.deserialize(json, unused, context);
      }
   }
}
