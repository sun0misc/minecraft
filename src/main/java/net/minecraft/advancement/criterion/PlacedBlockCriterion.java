package net.minecraft.advancement.criterion;

import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.predicate.StatePredicate;
import net.minecraft.predicate.entity.AdvancementEntityPredicateDeserializer;
import net.minecraft.predicate.entity.AdvancementEntityPredicateSerializer;
import net.minecraft.predicate.entity.EntityPredicate;
import net.minecraft.predicate.entity.LocationPredicate;
import net.minecraft.predicate.item.ItemPredicate;
import net.minecraft.registry.Registries;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;

public class PlacedBlockCriterion extends AbstractCriterion {
   static final Identifier ID = new Identifier("placed_block");

   public Identifier getId() {
      return ID;
   }

   public Conditions conditionsFromJson(JsonObject jsonObject, EntityPredicate.Extended arg, AdvancementEntityPredicateDeserializer arg2) {
      Block lv = getBlock(jsonObject);
      StatePredicate lv2 = StatePredicate.fromJson(jsonObject.get("state"));
      if (lv != null) {
         lv2.check(lv.getStateManager(), (name) -> {
            throw new JsonSyntaxException("Block " + lv + " has no property " + name + ":");
         });
      }

      LocationPredicate lv3 = LocationPredicate.fromJson(jsonObject.get("location"));
      ItemPredicate lv4 = ItemPredicate.fromJson(jsonObject.get("item"));
      return new Conditions(arg, lv, lv2, lv3, lv4);
   }

   @Nullable
   private static Block getBlock(JsonObject obj) {
      if (obj.has("block")) {
         Identifier lv = new Identifier(JsonHelper.getString(obj, "block"));
         return (Block)Registries.BLOCK.getOrEmpty(lv).orElseThrow(() -> {
            return new JsonSyntaxException("Unknown block type '" + lv + "'");
         });
      } else {
         return null;
      }
   }

   public void trigger(ServerPlayerEntity player, BlockPos blockPos, ItemStack stack) {
      BlockState lv = player.getWorld().getBlockState(blockPos);
      this.trigger(player, (conditions) -> {
         return conditions.matches(lv, blockPos, player.getWorld(), stack);
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
      private final LocationPredicate location;
      private final ItemPredicate item;

      public Conditions(EntityPredicate.Extended player, @Nullable Block block, StatePredicate state, LocationPredicate location, ItemPredicate item) {
         super(PlacedBlockCriterion.ID, player);
         this.block = block;
         this.state = state;
         this.location = location;
         this.item = item;
      }

      public static Conditions block(Block block) {
         return new Conditions(EntityPredicate.Extended.EMPTY, block, StatePredicate.ANY, LocationPredicate.ANY, ItemPredicate.ANY);
      }

      public boolean matches(BlockState state, BlockPos pos, ServerWorld world, ItemStack stack) {
         if (this.block != null && !state.isOf(this.block)) {
            return false;
         } else if (!this.state.test(state)) {
            return false;
         } else if (!this.location.test(world, (double)pos.getX(), (double)pos.getY(), (double)pos.getZ())) {
            return false;
         } else {
            return this.item.test(stack);
         }
      }

      public JsonObject toJson(AdvancementEntityPredicateSerializer predicateSerializer) {
         JsonObject jsonObject = super.toJson(predicateSerializer);
         if (this.block != null) {
            jsonObject.addProperty("block", Registries.BLOCK.getId(this.block).toString());
         }

         jsonObject.add("state", this.state.toJson());
         jsonObject.add("location", this.location.toJson());
         jsonObject.add("item", this.item.toJson());
         return jsonObject;
      }
   }
}
