package net.minecraft.loot.function;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSyntaxException;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.condition.LootCondition;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.provider.number.LootNumberProvider;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.Util;
import net.minecraft.util.math.random.Random;
import org.jetbrains.annotations.Nullable;

public class SetAttributesLootFunction extends ConditionalLootFunction {
   final List attributes;

   SetAttributesLootFunction(LootCondition[] conditions, List attributes) {
      super(conditions);
      this.attributes = ImmutableList.copyOf(attributes);
   }

   public LootFunctionType getType() {
      return LootFunctionTypes.SET_ATTRIBUTES;
   }

   public Set getRequiredParameters() {
      return (Set)this.attributes.stream().flatMap((attribute) -> {
         return attribute.amount.getRequiredParameters().stream();
      }).collect(ImmutableSet.toImmutableSet());
   }

   public ItemStack process(ItemStack stack, LootContext context) {
      Random lv = context.getRandom();
      Iterator var4 = this.attributes.iterator();

      while(var4.hasNext()) {
         Attribute lv2 = (Attribute)var4.next();
         UUID uUID = lv2.id;
         if (uUID == null) {
            uUID = UUID.randomUUID();
         }

         EquipmentSlot lv3 = (EquipmentSlot)Util.getRandom((Object[])lv2.slots, lv);
         stack.addAttributeModifier(lv2.attribute, new EntityAttributeModifier(uUID, lv2.name, (double)lv2.amount.nextFloat(context), lv2.operation), lv3);
      }

      return stack;
   }

   public static AttributeBuilder attributeBuilder(String name, EntityAttribute attribute, EntityAttributeModifier.Operation operation, LootNumberProvider amountRange) {
      return new AttributeBuilder(name, attribute, operation, amountRange);
   }

   public static Builder builder() {
      return new Builder();
   }

   private static class Attribute {
      final String name;
      final EntityAttribute attribute;
      final EntityAttributeModifier.Operation operation;
      final LootNumberProvider amount;
      @Nullable
      final UUID id;
      final EquipmentSlot[] slots;

      Attribute(String name, EntityAttribute attribute, EntityAttributeModifier.Operation operation, LootNumberProvider amount, EquipmentSlot[] slots, @Nullable UUID id) {
         this.name = name;
         this.attribute = attribute;
         this.operation = operation;
         this.amount = amount;
         this.id = id;
         this.slots = slots;
      }

      public JsonObject serialize(JsonSerializationContext context) {
         JsonObject jsonObject = new JsonObject();
         jsonObject.addProperty("name", this.name);
         jsonObject.addProperty("attribute", Registries.ATTRIBUTE.getId(this.attribute).toString());
         jsonObject.addProperty("operation", getName(this.operation));
         jsonObject.add("amount", context.serialize(this.amount));
         if (this.id != null) {
            jsonObject.addProperty("id", this.id.toString());
         }

         if (this.slots.length == 1) {
            jsonObject.addProperty("slot", this.slots[0].getName());
         } else {
            JsonArray jsonArray = new JsonArray();
            EquipmentSlot[] var4 = this.slots;
            int var5 = var4.length;

            for(int var6 = 0; var6 < var5; ++var6) {
               EquipmentSlot lv = var4[var6];
               jsonArray.add(new JsonPrimitive(lv.getName()));
            }

            jsonObject.add("slot", jsonArray);
         }

         return jsonObject;
      }

      public static Attribute deserialize(JsonObject json, JsonDeserializationContext context) {
         String string = JsonHelper.getString(json, "name");
         Identifier lv = new Identifier(JsonHelper.getString(json, "attribute"));
         EntityAttribute lv2 = (EntityAttribute)Registries.ATTRIBUTE.get(lv);
         if (lv2 == null) {
            throw new JsonSyntaxException("Unknown attribute: " + lv);
         } else {
            EntityAttributeModifier.Operation lv3 = fromName(JsonHelper.getString(json, "operation"));
            LootNumberProvider lv4 = (LootNumberProvider)JsonHelper.deserialize(json, "amount", context, LootNumberProvider.class);
            UUID uUID = null;
            EquipmentSlot[] lvs;
            if (JsonHelper.hasString(json, "slot")) {
               lvs = new EquipmentSlot[]{EquipmentSlot.byName(JsonHelper.getString(json, "slot"))};
            } else {
               if (!JsonHelper.hasArray(json, "slot")) {
                  throw new JsonSyntaxException("Invalid or missing attribute modifier slot; must be either string or array of strings.");
               }

               JsonArray jsonArray = JsonHelper.getArray(json, "slot");
               lvs = new EquipmentSlot[jsonArray.size()];
               int i = 0;

               JsonElement jsonElement;
               for(Iterator var11 = jsonArray.iterator(); var11.hasNext(); lvs[i++] = EquipmentSlot.byName(JsonHelper.asString(jsonElement, "slot"))) {
                  jsonElement = (JsonElement)var11.next();
               }

               if (lvs.length == 0) {
                  throw new JsonSyntaxException("Invalid attribute modifier slot; must contain at least one entry.");
               }
            }

            if (json.has("id")) {
               String string2 = JsonHelper.getString(json, "id");

               try {
                  uUID = UUID.fromString(string2);
               } catch (IllegalArgumentException var13) {
                  throw new JsonSyntaxException("Invalid attribute modifier id '" + string2 + "' (must be UUID format, with dashes)");
               }
            }

            return new Attribute(string, lv2, lv3, lv4, lvs, uUID);
         }
      }

      private static String getName(EntityAttributeModifier.Operation operation) {
         switch (operation) {
            case ADDITION:
               return "addition";
            case MULTIPLY_BASE:
               return "multiply_base";
            case MULTIPLY_TOTAL:
               return "multiply_total";
            default:
               throw new IllegalArgumentException("Unknown operation " + operation);
         }
      }

      private static EntityAttributeModifier.Operation fromName(String name) {
         switch (name) {
            case "addition":
               return EntityAttributeModifier.Operation.ADDITION;
            case "multiply_base":
               return EntityAttributeModifier.Operation.MULTIPLY_BASE;
            case "multiply_total":
               return EntityAttributeModifier.Operation.MULTIPLY_TOTAL;
            default:
               throw new JsonSyntaxException("Unknown attribute modifier operation " + name);
         }
      }
   }

   public static class AttributeBuilder {
      private final String name;
      private final EntityAttribute attribute;
      private final EntityAttributeModifier.Operation operation;
      private final LootNumberProvider amount;
      @Nullable
      private UUID uuid;
      private final Set slots = EnumSet.noneOf(EquipmentSlot.class);

      public AttributeBuilder(String name, EntityAttribute attribute, EntityAttributeModifier.Operation operation, LootNumberProvider amount) {
         this.name = name;
         this.attribute = attribute;
         this.operation = operation;
         this.amount = amount;
      }

      public AttributeBuilder slot(EquipmentSlot slot) {
         this.slots.add(slot);
         return this;
      }

      public AttributeBuilder uuid(UUID uuid) {
         this.uuid = uuid;
         return this;
      }

      public Attribute build() {
         return new Attribute(this.name, this.attribute, this.operation, this.amount, (EquipmentSlot[])this.slots.toArray(new EquipmentSlot[0]), this.uuid);
      }
   }

   public static class Builder extends ConditionalLootFunction.Builder {
      private final List attributes = Lists.newArrayList();

      protected Builder getThisBuilder() {
         return this;
      }

      public Builder attribute(AttributeBuilder attribute) {
         this.attributes.add(attribute.build());
         return this;
      }

      public LootFunction build() {
         return new SetAttributesLootFunction(this.getConditions(), this.attributes);
      }

      // $FF: synthetic method
      protected ConditionalLootFunction.Builder getThisBuilder() {
         return this.getThisBuilder();
      }
   }

   public static class Serializer extends ConditionalLootFunction.Serializer {
      public void toJson(JsonObject jsonObject, SetAttributesLootFunction arg, JsonSerializationContext jsonSerializationContext) {
         super.toJson(jsonObject, (ConditionalLootFunction)arg, jsonSerializationContext);
         JsonArray jsonArray = new JsonArray();
         Iterator var5 = arg.attributes.iterator();

         while(var5.hasNext()) {
            Attribute lv = (Attribute)var5.next();
            jsonArray.add(lv.serialize(jsonSerializationContext));
         }

         jsonObject.add("modifiers", jsonArray);
      }

      public SetAttributesLootFunction fromJson(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext, LootCondition[] args) {
         JsonArray jsonArray = JsonHelper.getArray(jsonObject, "modifiers");
         List list = Lists.newArrayListWithExpectedSize(jsonArray.size());
         Iterator var6 = jsonArray.iterator();

         while(var6.hasNext()) {
            JsonElement jsonElement = (JsonElement)var6.next();
            list.add(SetAttributesLootFunction.Attribute.deserialize(JsonHelper.asObject(jsonElement, "modifier"), jsonDeserializationContext));
         }

         if (list.isEmpty()) {
            throw new JsonSyntaxException("Invalid attribute modifiers array; cannot be empty");
         } else {
            return new SetAttributesLootFunction(args, list);
         }
      }

      // $FF: synthetic method
      public ConditionalLootFunction fromJson(JsonObject json, JsonDeserializationContext context, LootCondition[] conditions) {
         return this.fromJson(json, context, conditions);
      }
   }
}
