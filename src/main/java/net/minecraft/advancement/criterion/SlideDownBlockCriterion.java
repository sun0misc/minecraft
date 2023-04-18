package net.minecraft.advancement.criterion;

import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.predicate.StatePredicate;
import net.minecraft.predicate.entity.AdvancementEntityPredicateDeserializer;
import net.minecraft.predicate.entity.AdvancementEntityPredicateSerializer;
import net.minecraft.predicate.entity.EntityPredicate;
import net.minecraft.registry.Registries;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import org.jetbrains.annotations.Nullable;

public class SlideDownBlockCriterion extends AbstractCriterion {
   static final Identifier ID = new Identifier("slide_down_block");

   public Identifier getId() {
      return ID;
   }

   public Conditions conditionsFromJson(JsonObject jsonObject, EntityPredicate.Extended arg, AdvancementEntityPredicateDeserializer arg2) {
      Block lv = getBlock(jsonObject);
      StatePredicate lv2 = StatePredicate.fromJson(jsonObject.get("state"));
      if (lv != null) {
         lv2.check(lv.getStateManager(), (key) -> {
            throw new JsonSyntaxException("Block " + lv + " has no property " + key);
         });
      }

      return new Conditions(arg, lv, lv2);
   }

   @Nullable
   private static Block getBlock(JsonObject root) {
      if (root.has("block")) {
         Identifier lv = new Identifier(JsonHelper.getString(root, "block"));
         return (Block)Registries.BLOCK.getOrEmpty(lv).orElseThrow(() -> {
            return new JsonSyntaxException("Unknown block type '" + lv + "'");
         });
      } else {
         return null;
      }
   }

   public void trigger(ServerPlayerEntity player, BlockState state) {
      this.trigger(player, (conditions) -> {
         return conditions.test(state);
      });
   }

   // $FF: synthetic method
   public AbstractCriterionConditions conditionsFromJson(JsonObject obj, EntityPredicate.Extended playerPredicate, AdvancementEntityPredicateDeserializer predicateDeserializer) {
      return this.conditionsFromJson(obj, playerPredicate, predicateDeserializer);
   }

   public static class Conditions extends AbstractCriterionConditions {
      @Nullable
      private final Block block;
      private final StatePredicate state;

      public Conditions(EntityPredicate.Extended player, @Nullable Block block, StatePredicate state) {
         super(SlideDownBlockCriterion.ID, player);
         this.block = block;
         this.state = state;
      }

      public static Conditions create(Block block) {
         return new Conditions(EntityPredicate.Extended.EMPTY, block, StatePredicate.ANY);
      }

      public JsonObject toJson(AdvancementEntityPredicateSerializer predicateSerializer) {
         JsonObject jsonObject = super.toJson(predicateSerializer);
         if (this.block != null) {
            jsonObject.addProperty("block", Registries.BLOCK.getId(this.block).toString());
         }

         jsonObject.add("state", this.state.toJson());
         return jsonObject;
      }

      public boolean test(BlockState state) {
         if (this.block != null && !state.isOf(this.block)) {
            return false;
         } else {
            return this.state.test(state);
         }
      }
   }
}
