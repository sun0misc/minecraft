package net.minecraft.data.client;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;
import java.util.stream.Stream;
import net.minecraft.block.Block;
import net.minecraft.state.StateManager;

public class MultipartBlockStateSupplier implements BlockStateSupplier {
   private final Block block;
   private final List multiparts = Lists.newArrayList();

   private MultipartBlockStateSupplier(Block block) {
      this.block = block;
   }

   public Block getBlock() {
      return this.block;
   }

   public static MultipartBlockStateSupplier create(Block block) {
      return new MultipartBlockStateSupplier(block);
   }

   public MultipartBlockStateSupplier with(List variants) {
      this.multiparts.add(new Multipart(variants));
      return this;
   }

   public MultipartBlockStateSupplier with(BlockStateVariant variant) {
      return this.with((List)ImmutableList.of(variant));
   }

   public MultipartBlockStateSupplier with(When condition, List variants) {
      this.multiparts.add(new ConditionalMultipart(condition, variants));
      return this;
   }

   public MultipartBlockStateSupplier with(When condition, BlockStateVariant... variants) {
      return this.with(condition, (List)ImmutableList.copyOf(variants));
   }

   public MultipartBlockStateSupplier with(When condition, BlockStateVariant variant) {
      return this.with(condition, (List)ImmutableList.of(variant));
   }

   public JsonElement get() {
      StateManager lv = this.block.getStateManager();
      this.multiparts.forEach((multipart) -> {
         multipart.validate(lv);
      });
      JsonArray jsonArray = new JsonArray();
      Stream var10000 = this.multiparts.stream().map(Multipart::get);
      Objects.requireNonNull(jsonArray);
      var10000.forEach(jsonArray::add);
      JsonObject jsonObject = new JsonObject();
      jsonObject.add("multipart", jsonArray);
      return jsonObject;
   }

   // $FF: synthetic method
   public Object get() {
      return this.get();
   }

   private static class Multipart implements Supplier {
      private final List variants;

      Multipart(List variants) {
         this.variants = variants;
      }

      public void validate(StateManager stateManager) {
      }

      public void extraToJson(JsonObject json) {
      }

      public JsonElement get() {
         JsonObject jsonObject = new JsonObject();
         this.extraToJson(jsonObject);
         jsonObject.add("apply", BlockStateVariant.toJson(this.variants));
         return jsonObject;
      }

      // $FF: synthetic method
      public Object get() {
         return this.get();
      }
   }

   private static class ConditionalMultipart extends Multipart {
      private final When when;

      ConditionalMultipart(When when, List variants) {
         super(variants);
         this.when = when;
      }

      public void validate(StateManager stateManager) {
         this.when.validate(stateManager);
      }

      public void extraToJson(JsonObject json) {
         json.add("when", (JsonElement)this.when.get());
      }
   }
}
