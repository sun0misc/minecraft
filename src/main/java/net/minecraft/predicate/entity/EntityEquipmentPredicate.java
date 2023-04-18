package net.minecraft.predicate.entity;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.Items;
import net.minecraft.predicate.item.ItemPredicate;
import net.minecraft.util.JsonHelper;
import net.minecraft.village.raid.Raid;
import org.jetbrains.annotations.Nullable;

public class EntityEquipmentPredicate {
   public static final EntityEquipmentPredicate ANY;
   public static final EntityEquipmentPredicate OMINOUS_BANNER_ON_HEAD;
   private final ItemPredicate head;
   private final ItemPredicate chest;
   private final ItemPredicate legs;
   private final ItemPredicate feet;
   private final ItemPredicate mainhand;
   private final ItemPredicate offhand;

   public EntityEquipmentPredicate(ItemPredicate head, ItemPredicate chest, ItemPredicate legs, ItemPredicate feet, ItemPredicate mainhand, ItemPredicate offhand) {
      this.head = head;
      this.chest = chest;
      this.legs = legs;
      this.feet = feet;
      this.mainhand = mainhand;
      this.offhand = offhand;
   }

   public boolean test(@Nullable Entity entity) {
      if (this == ANY) {
         return true;
      } else if (!(entity instanceof LivingEntity)) {
         return false;
      } else {
         LivingEntity lv = (LivingEntity)entity;
         if (!this.head.test(lv.getEquippedStack(EquipmentSlot.HEAD))) {
            return false;
         } else if (!this.chest.test(lv.getEquippedStack(EquipmentSlot.CHEST))) {
            return false;
         } else if (!this.legs.test(lv.getEquippedStack(EquipmentSlot.LEGS))) {
            return false;
         } else if (!this.feet.test(lv.getEquippedStack(EquipmentSlot.FEET))) {
            return false;
         } else if (!this.mainhand.test(lv.getEquippedStack(EquipmentSlot.MAINHAND))) {
            return false;
         } else {
            return this.offhand.test(lv.getEquippedStack(EquipmentSlot.OFFHAND));
         }
      }
   }

   public static EntityEquipmentPredicate fromJson(@Nullable JsonElement json) {
      if (json != null && !json.isJsonNull()) {
         JsonObject jsonObject = JsonHelper.asObject(json, "equipment");
         ItemPredicate lv = ItemPredicate.fromJson(jsonObject.get("head"));
         ItemPredicate lv2 = ItemPredicate.fromJson(jsonObject.get("chest"));
         ItemPredicate lv3 = ItemPredicate.fromJson(jsonObject.get("legs"));
         ItemPredicate lv4 = ItemPredicate.fromJson(jsonObject.get("feet"));
         ItemPredicate lv5 = ItemPredicate.fromJson(jsonObject.get("mainhand"));
         ItemPredicate lv6 = ItemPredicate.fromJson(jsonObject.get("offhand"));
         return new EntityEquipmentPredicate(lv, lv2, lv3, lv4, lv5, lv6);
      } else {
         return ANY;
      }
   }

   public JsonElement toJson() {
      if (this == ANY) {
         return JsonNull.INSTANCE;
      } else {
         JsonObject jsonObject = new JsonObject();
         jsonObject.add("head", this.head.toJson());
         jsonObject.add("chest", this.chest.toJson());
         jsonObject.add("legs", this.legs.toJson());
         jsonObject.add("feet", this.feet.toJson());
         jsonObject.add("mainhand", this.mainhand.toJson());
         jsonObject.add("offhand", this.offhand.toJson());
         return jsonObject;
      }
   }

   static {
      ANY = new EntityEquipmentPredicate(ItemPredicate.ANY, ItemPredicate.ANY, ItemPredicate.ANY, ItemPredicate.ANY, ItemPredicate.ANY, ItemPredicate.ANY);
      OMINOUS_BANNER_ON_HEAD = new EntityEquipmentPredicate(ItemPredicate.Builder.create().items(Items.WHITE_BANNER).nbt(Raid.getOminousBanner().getNbt()).build(), ItemPredicate.ANY, ItemPredicate.ANY, ItemPredicate.ANY, ItemPredicate.ANY, ItemPredicate.ANY);
   }

   public static class Builder {
      private ItemPredicate head;
      private ItemPredicate chest;
      private ItemPredicate legs;
      private ItemPredicate feet;
      private ItemPredicate mainhand;
      private ItemPredicate offhand;

      public Builder() {
         this.head = ItemPredicate.ANY;
         this.chest = ItemPredicate.ANY;
         this.legs = ItemPredicate.ANY;
         this.feet = ItemPredicate.ANY;
         this.mainhand = ItemPredicate.ANY;
         this.offhand = ItemPredicate.ANY;
      }

      public static Builder create() {
         return new Builder();
      }

      public Builder head(ItemPredicate head) {
         this.head = head;
         return this;
      }

      public Builder chest(ItemPredicate chest) {
         this.chest = chest;
         return this;
      }

      public Builder legs(ItemPredicate legs) {
         this.legs = legs;
         return this;
      }

      public Builder feet(ItemPredicate feet) {
         this.feet = feet;
         return this;
      }

      public Builder mainhand(ItemPredicate mainhand) {
         this.mainhand = mainhand;
         return this;
      }

      public Builder offhand(ItemPredicate offhand) {
         this.offhand = offhand;
         return this;
      }

      public EntityEquipmentPredicate build() {
         return new EntityEquipmentPredicate(this.head, this.chest, this.legs, this.feet, this.mainhand, this.offhand);
      }
   }
}
