package net.minecraft.predicate;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSyntaxException;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.nbt.StringNbtReader;
import net.minecraft.util.JsonHelper;
import org.jetbrains.annotations.Nullable;

public class NbtPredicate {
   public static final NbtPredicate ANY = new NbtPredicate((NbtCompound)null);
   @Nullable
   private final NbtCompound nbt;

   public NbtPredicate(@Nullable NbtCompound nbt) {
      this.nbt = nbt;
   }

   public boolean test(ItemStack stack) {
      return this == ANY ? true : this.test((NbtElement)stack.getNbt());
   }

   public boolean test(Entity entity) {
      return this == ANY ? true : this.test((NbtElement)entityToNbt(entity));
   }

   public boolean test(@Nullable NbtElement element) {
      if (element == null) {
         return this == ANY;
      } else {
         return this.nbt == null || NbtHelper.matches(this.nbt, element, true);
      }
   }

   public JsonElement toJson() {
      return (JsonElement)(this != ANY && this.nbt != null ? new JsonPrimitive(this.nbt.toString()) : JsonNull.INSTANCE);
   }

   public static NbtPredicate fromJson(@Nullable JsonElement json) {
      if (json != null && !json.isJsonNull()) {
         NbtCompound lv;
         try {
            lv = StringNbtReader.parse(JsonHelper.asString(json, "nbt"));
         } catch (CommandSyntaxException var3) {
            throw new JsonSyntaxException("Invalid nbt tag: " + var3.getMessage());
         }

         return new NbtPredicate(lv);
      } else {
         return ANY;
      }
   }

   public static NbtCompound entityToNbt(Entity entity) {
      NbtCompound lv = entity.writeNbt(new NbtCompound());
      if (entity instanceof PlayerEntity) {
         ItemStack lv2 = ((PlayerEntity)entity).getInventory().getMainHandStack();
         if (!lv2.isEmpty()) {
            lv.put("SelectedItem", lv2.writeNbt(new NbtCompound()));
         }
      }

      return lv;
   }
}
