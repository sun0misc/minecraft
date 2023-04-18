package net.minecraft.predicate;

import com.google.common.collect.ImmutableSet;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import java.util.Iterator;
import java.util.Set;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;

public class BlockPredicate {
   public static final BlockPredicate ANY;
   @Nullable
   private final TagKey tag;
   @Nullable
   private final Set blocks;
   private final StatePredicate state;
   private final NbtPredicate nbt;

   public BlockPredicate(@Nullable TagKey tag, @Nullable Set blocks, StatePredicate state, NbtPredicate nbt) {
      this.tag = tag;
      this.blocks = blocks;
      this.state = state;
      this.nbt = nbt;
   }

   public boolean test(ServerWorld world, BlockPos pos) {
      if (this == ANY) {
         return true;
      } else if (!world.canSetBlock(pos)) {
         return false;
      } else {
         BlockState lv = world.getBlockState(pos);
         if (this.tag != null && !lv.isIn(this.tag)) {
            return false;
         } else if (this.blocks != null && !this.blocks.contains(lv.getBlock())) {
            return false;
         } else if (!this.state.test(lv)) {
            return false;
         } else {
            if (this.nbt != NbtPredicate.ANY) {
               BlockEntity lv2 = world.getBlockEntity(pos);
               if (lv2 == null || !this.nbt.test((NbtElement)lv2.createNbtWithIdentifyingData())) {
                  return false;
               }
            }

            return true;
         }
      }
   }

   public static BlockPredicate fromJson(@Nullable JsonElement json) {
      if (json != null && !json.isJsonNull()) {
         JsonObject jsonObject = JsonHelper.asObject(json, "block");
         NbtPredicate lv = NbtPredicate.fromJson(jsonObject.get("nbt"));
         Set set = null;
         JsonArray jsonArray = JsonHelper.getArray(jsonObject, "blocks", (JsonArray)null);
         if (jsonArray != null) {
            ImmutableSet.Builder builder = ImmutableSet.builder();
            Iterator var6 = jsonArray.iterator();

            while(var6.hasNext()) {
               JsonElement jsonElement2 = (JsonElement)var6.next();
               Identifier lv2 = new Identifier(JsonHelper.asString(jsonElement2, "block"));
               builder.add((Block)Registries.BLOCK.getOrEmpty(lv2).orElseThrow(() -> {
                  return new JsonSyntaxException("Unknown block id '" + lv2 + "'");
               }));
            }

            set = builder.build();
         }

         TagKey lv3 = null;
         if (jsonObject.has("tag")) {
            Identifier lv4 = new Identifier(JsonHelper.getString(jsonObject, "tag"));
            lv3 = TagKey.of(RegistryKeys.BLOCK, lv4);
         }

         StatePredicate lv5 = StatePredicate.fromJson(jsonObject.get("state"));
         return new BlockPredicate(lv3, set, lv5, lv);
      } else {
         return ANY;
      }
   }

   public JsonElement toJson() {
      if (this == ANY) {
         return JsonNull.INSTANCE;
      } else {
         JsonObject jsonObject = new JsonObject();
         if (this.blocks != null) {
            JsonArray jsonArray = new JsonArray();
            Iterator var3 = this.blocks.iterator();

            while(var3.hasNext()) {
               Block lv = (Block)var3.next();
               jsonArray.add(Registries.BLOCK.getId(lv).toString());
            }

            jsonObject.add("blocks", jsonArray);
         }

         if (this.tag != null) {
            jsonObject.addProperty("tag", this.tag.id().toString());
         }

         jsonObject.add("nbt", this.nbt.toJson());
         jsonObject.add("state", this.state.toJson());
         return jsonObject;
      }
   }

   static {
      ANY = new BlockPredicate((TagKey)null, (Set)null, StatePredicate.ANY, NbtPredicate.ANY);
   }

   public static class Builder {
      @Nullable
      private Set blocks;
      @Nullable
      private TagKey tag;
      private StatePredicate state;
      private NbtPredicate nbt;

      private Builder() {
         this.state = StatePredicate.ANY;
         this.nbt = NbtPredicate.ANY;
      }

      public static Builder create() {
         return new Builder();
      }

      public Builder blocks(Block... blocks) {
         this.blocks = ImmutableSet.copyOf(blocks);
         return this;
      }

      public Builder blocks(Iterable blocks) {
         this.blocks = ImmutableSet.copyOf(blocks);
         return this;
      }

      public Builder tag(TagKey tag) {
         this.tag = tag;
         return this;
      }

      public Builder nbt(NbtCompound nbt) {
         this.nbt = new NbtPredicate(nbt);
         return this;
      }

      public Builder state(StatePredicate state) {
         this.state = state;
         return this;
      }

      public BlockPredicate build() {
         return new BlockPredicate(this.tag, this.blocks, this.state, this.nbt);
      }
   }
}
