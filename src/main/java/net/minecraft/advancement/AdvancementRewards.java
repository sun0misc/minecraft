package net.minecraft.advancement;

import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import it.unimi.dsi.fastutil.objects.ObjectListIterator;
import java.util.Arrays;
import java.util.List;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.loot.context.LootContextTypes;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.function.CommandFunction;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import org.jetbrains.annotations.Nullable;

public class AdvancementRewards {
   public static final AdvancementRewards NONE;
   private final int experience;
   private final Identifier[] loot;
   private final Identifier[] recipes;
   private final CommandFunction.LazyContainer function;

   public AdvancementRewards(int experience, Identifier[] loot, Identifier[] recipes, CommandFunction.LazyContainer function) {
      this.experience = experience;
      this.loot = loot;
      this.recipes = recipes;
      this.function = function;
   }

   public Identifier[] getRecipes() {
      return this.recipes;
   }

   public void apply(ServerPlayerEntity player) {
      player.addExperience(this.experience);
      LootContext lv = (new LootContext.Builder(player.getWorld())).parameter(LootContextParameters.THIS_ENTITY, player).parameter(LootContextParameters.ORIGIN, player.getPos()).random(player.getRandom()).build(LootContextTypes.ADVANCEMENT_REWARD);
      boolean bl = false;
      Identifier[] var4 = this.loot;
      int var5 = var4.length;

      for(int var6 = 0; var6 < var5; ++var6) {
         Identifier lv2 = var4[var6];
         ObjectListIterator var8 = player.server.getLootManager().getLootTable(lv2).generateLoot(lv).iterator();

         while(var8.hasNext()) {
            ItemStack lv3 = (ItemStack)var8.next();
            if (player.giveItemStack(lv3)) {
               player.world.playSound((PlayerEntity)null, player.getX(), player.getY(), player.getZ(), SoundEvents.ENTITY_ITEM_PICKUP, SoundCategory.PLAYERS, 0.2F, ((player.getRandom().nextFloat() - player.getRandom().nextFloat()) * 0.7F + 1.0F) * 2.0F);
               bl = true;
            } else {
               ItemEntity lv4 = player.dropItem(lv3, false);
               if (lv4 != null) {
                  lv4.resetPickupDelay();
                  lv4.setOwner(player.getUuid());
               }
            }
         }
      }

      if (bl) {
         player.currentScreenHandler.sendContentUpdates();
      }

      if (this.recipes.length > 0) {
         player.unlockRecipes(this.recipes);
      }

      MinecraftServer minecraftServer = player.server;
      this.function.get(minecraftServer.getCommandFunctionManager()).ifPresent((function) -> {
         minecraftServer.getCommandFunctionManager().execute(function, player.getCommandSource().withSilent().withLevel(2));
      });
   }

   public String toString() {
      int var10000 = this.experience;
      return "AdvancementRewards{experience=" + var10000 + ", loot=" + Arrays.toString(this.loot) + ", recipes=" + Arrays.toString(this.recipes) + ", function=" + this.function + "}";
   }

   public JsonElement toJson() {
      if (this == NONE) {
         return JsonNull.INSTANCE;
      } else {
         JsonObject jsonObject = new JsonObject();
         if (this.experience != 0) {
            jsonObject.addProperty("experience", this.experience);
         }

         JsonArray jsonArray;
         Identifier[] var3;
         int var4;
         int var5;
         Identifier lv;
         if (this.loot.length > 0) {
            jsonArray = new JsonArray();
            var3 = this.loot;
            var4 = var3.length;

            for(var5 = 0; var5 < var4; ++var5) {
               lv = var3[var5];
               jsonArray.add(lv.toString());
            }

            jsonObject.add("loot", jsonArray);
         }

         if (this.recipes.length > 0) {
            jsonArray = new JsonArray();
            var3 = this.recipes;
            var4 = var3.length;

            for(var5 = 0; var5 < var4; ++var5) {
               lv = var3[var5];
               jsonArray.add(lv.toString());
            }

            jsonObject.add("recipes", jsonArray);
         }

         if (this.function.getId() != null) {
            jsonObject.addProperty("function", this.function.getId().toString());
         }

         return jsonObject;
      }
   }

   public static AdvancementRewards fromJson(JsonObject json) throws JsonParseException {
      int i = JsonHelper.getInt(json, "experience", 0);
      JsonArray jsonArray = JsonHelper.getArray(json, "loot", new JsonArray());
      Identifier[] lvs = new Identifier[jsonArray.size()];

      for(int j = 0; j < lvs.length; ++j) {
         lvs[j] = new Identifier(JsonHelper.asString(jsonArray.get(j), "loot[" + j + "]"));
      }

      JsonArray jsonArray2 = JsonHelper.getArray(json, "recipes", new JsonArray());
      Identifier[] lvs2 = new Identifier[jsonArray2.size()];

      for(int k = 0; k < lvs2.length; ++k) {
         lvs2[k] = new Identifier(JsonHelper.asString(jsonArray2.get(k), "recipes[" + k + "]"));
      }

      CommandFunction.LazyContainer lv;
      if (json.has("function")) {
         lv = new CommandFunction.LazyContainer(new Identifier(JsonHelper.getString(json, "function")));
      } else {
         lv = CommandFunction.LazyContainer.EMPTY;
      }

      return new AdvancementRewards(i, lvs, lvs2, lv);
   }

   static {
      NONE = new AdvancementRewards(0, new Identifier[0], new Identifier[0], CommandFunction.LazyContainer.EMPTY);
   }

   public static class Builder {
      private int experience;
      private final List loot = Lists.newArrayList();
      private final List recipes = Lists.newArrayList();
      @Nullable
      private Identifier function;

      public static Builder experience(int experience) {
         return (new Builder()).setExperience(experience);
      }

      public Builder setExperience(int experience) {
         this.experience += experience;
         return this;
      }

      public static Builder loot(Identifier loot) {
         return (new Builder()).addLoot(loot);
      }

      public Builder addLoot(Identifier loot) {
         this.loot.add(loot);
         return this;
      }

      public static Builder recipe(Identifier recipe) {
         return (new Builder()).addRecipe(recipe);
      }

      public Builder addRecipe(Identifier recipe) {
         this.recipes.add(recipe);
         return this;
      }

      public static Builder function(Identifier function) {
         return (new Builder()).setFunction(function);
      }

      public Builder setFunction(Identifier function) {
         this.function = function;
         return this;
      }

      public AdvancementRewards build() {
         return new AdvancementRewards(this.experience, (Identifier[])this.loot.toArray(new Identifier[0]), (Identifier[])this.recipes.toArray(new Identifier[0]), this.function == null ? CommandFunction.LazyContainer.EMPTY : new CommandFunction.LazyContainer(this.function));
      }
   }
}
