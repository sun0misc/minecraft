package net.minecraft.loot.function;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Stream;
import net.minecraft.command.argument.NbtPathArgumentType;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.condition.LootCondition;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.provider.nbt.ContextLootNbtProvider;
import net.minecraft.loot.provider.nbt.LootNbtProvider;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.util.JsonHelper;

public class CopyNbtLootFunction extends ConditionalLootFunction {
   final LootNbtProvider source;
   final List operations;

   CopyNbtLootFunction(LootCondition[] conditions, LootNbtProvider source, List operations) {
      super(conditions);
      this.source = source;
      this.operations = ImmutableList.copyOf(operations);
   }

   public LootFunctionType getType() {
      return LootFunctionTypes.COPY_NBT;
   }

   static NbtPathArgumentType.NbtPath parseNbtPath(String nbtPath) {
      try {
         return (new NbtPathArgumentType()).parse(new StringReader(nbtPath));
      } catch (CommandSyntaxException var2) {
         throw new IllegalArgumentException("Failed to parse path " + nbtPath, var2);
      }
   }

   public Set getRequiredParameters() {
      return this.source.getRequiredParameters();
   }

   public ItemStack process(ItemStack stack, LootContext context) {
      NbtElement lv = this.source.getNbt(context);
      if (lv != null) {
         this.operations.forEach((operation) -> {
            Objects.requireNonNull(stack);
            operation.execute(stack::getOrCreateNbt, lv);
         });
      }

      return stack;
   }

   public static Builder builder(LootNbtProvider source) {
      return new Builder(source);
   }

   public static Builder builder(LootContext.EntityTarget target) {
      return new Builder(ContextLootNbtProvider.fromTarget(target));
   }

   public static class Builder extends ConditionalLootFunction.Builder {
      private final LootNbtProvider source;
      private final List operations = Lists.newArrayList();

      Builder(LootNbtProvider source) {
         this.source = source;
      }

      public Builder withOperation(String source, String target, Operator operator) {
         this.operations.add(new Operation(source, target, operator));
         return this;
      }

      public Builder withOperation(String source, String target) {
         return this.withOperation(source, target, CopyNbtLootFunction.Operator.REPLACE);
      }

      protected Builder getThisBuilder() {
         return this;
      }

      public LootFunction build() {
         return new CopyNbtLootFunction(this.getConditions(), this.source, this.operations);
      }

      // $FF: synthetic method
      protected ConditionalLootFunction.Builder getThisBuilder() {
         return this.getThisBuilder();
      }
   }

   private static class Operation {
      private final String sourcePath;
      private final NbtPathArgumentType.NbtPath parsedSourcePath;
      private final String targetPath;
      private final NbtPathArgumentType.NbtPath parsedTargetPath;
      private final Operator operator;

      Operation(String sourcePath, String targetPath, Operator operator) {
         this.sourcePath = sourcePath;
         this.parsedSourcePath = CopyNbtLootFunction.parseNbtPath(sourcePath);
         this.targetPath = targetPath;
         this.parsedTargetPath = CopyNbtLootFunction.parseNbtPath(targetPath);
         this.operator = operator;
      }

      public void execute(Supplier itemNbtGetter, NbtElement sourceEntityNbt) {
         try {
            List list = this.parsedSourcePath.get(sourceEntityNbt);
            if (!list.isEmpty()) {
               this.operator.merge((NbtElement)itemNbtGetter.get(), this.parsedTargetPath, list);
            }
         } catch (CommandSyntaxException var4) {
         }

      }

      public JsonObject toJson() {
         JsonObject jsonObject = new JsonObject();
         jsonObject.addProperty("source", this.sourcePath);
         jsonObject.addProperty("target", this.targetPath);
         jsonObject.addProperty("op", this.operator.name);
         return jsonObject;
      }

      public static Operation fromJson(JsonObject json) {
         String string = JsonHelper.getString(json, "source");
         String string2 = JsonHelper.getString(json, "target");
         Operator lv = CopyNbtLootFunction.Operator.get(JsonHelper.getString(json, "op"));
         return new Operation(string, string2, lv);
      }
   }

   public static class Serializer extends ConditionalLootFunction.Serializer {
      public void toJson(JsonObject jsonObject, CopyNbtLootFunction arg, JsonSerializationContext jsonSerializationContext) {
         super.toJson(jsonObject, (ConditionalLootFunction)arg, jsonSerializationContext);
         jsonObject.add("source", jsonSerializationContext.serialize(arg.source));
         JsonArray jsonArray = new JsonArray();
         Stream var10000 = arg.operations.stream().map(Operation::toJson);
         Objects.requireNonNull(jsonArray);
         var10000.forEach(jsonArray::add);
         jsonObject.add("ops", jsonArray);
      }

      public CopyNbtLootFunction fromJson(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext, LootCondition[] args) {
         LootNbtProvider lv = (LootNbtProvider)JsonHelper.deserialize(jsonObject, "source", jsonDeserializationContext, LootNbtProvider.class);
         List list = Lists.newArrayList();
         JsonArray jsonArray = JsonHelper.getArray(jsonObject, "ops");
         Iterator var7 = jsonArray.iterator();

         while(var7.hasNext()) {
            JsonElement jsonElement = (JsonElement)var7.next();
            JsonObject jsonObject2 = JsonHelper.asObject(jsonElement, "op");
            list.add(CopyNbtLootFunction.Operation.fromJson(jsonObject2));
         }

         return new CopyNbtLootFunction(args, lv, list);
      }

      // $FF: synthetic method
      public ConditionalLootFunction fromJson(JsonObject json, JsonDeserializationContext context, LootCondition[] conditions) {
         return this.fromJson(json, context, conditions);
      }
   }

   public static enum Operator {
      REPLACE("replace") {
         public void merge(NbtElement itemNbt, NbtPathArgumentType.NbtPath targetPath, List sourceNbts) throws CommandSyntaxException {
            targetPath.put(itemNbt, (NbtElement)Iterables.getLast(sourceNbts));
         }
      },
      APPEND("append") {
         public void merge(NbtElement itemNbt, NbtPathArgumentType.NbtPath targetPath, List sourceNbts) throws CommandSyntaxException {
            List list2 = targetPath.getOrInit(itemNbt, NbtList::new);
            list2.forEach((foundNbt) -> {
               if (foundNbt instanceof NbtList) {
                  sourceNbts.forEach((sourceNbt) -> {
                     ((NbtList)foundNbt).add(sourceNbt.copy());
                  });
               }

            });
         }
      },
      MERGE("merge") {
         public void merge(NbtElement itemNbt, NbtPathArgumentType.NbtPath targetPath, List sourceNbts) throws CommandSyntaxException {
            List list2 = targetPath.getOrInit(itemNbt, NbtCompound::new);
            list2.forEach((foundNbt) -> {
               if (foundNbt instanceof NbtCompound) {
                  sourceNbts.forEach((sourceNbt) -> {
                     if (sourceNbt instanceof NbtCompound) {
                        ((NbtCompound)foundNbt).copyFrom((NbtCompound)sourceNbt);
                     }

                  });
               }

            });
         }
      };

      final String name;

      public abstract void merge(NbtElement itemNbt, NbtPathArgumentType.NbtPath targetPath, List sourceNbts) throws CommandSyntaxException;

      Operator(String name) {
         this.name = name;
      }

      public static Operator get(String name) {
         Operator[] var1 = values();
         int var2 = var1.length;

         for(int var3 = 0; var3 < var2; ++var3) {
            Operator lv = var1[var3];
            if (lv.name.equals(name)) {
               return lv;
            }
         }

         throw new IllegalArgumentException("Invalid merge strategy" + name);
      }

      // $FF: synthetic method
      private static Operator[] method_36795() {
         return new Operator[]{REPLACE, APPEND, MERGE};
      }
   }
}
