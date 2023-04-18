package net.minecraft.server.network;

import com.google.common.collect.Lists;
import com.mojang.logging.LogUtils;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.network.packet.s2c.play.UnlockRecipesS2CPacket;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeManager;
import net.minecraft.recipe.book.RecipeBook;
import net.minecraft.recipe.book.RecipeBookOptions;
import net.minecraft.util.Identifier;
import net.minecraft.util.InvalidIdentifierException;
import org.slf4j.Logger;

public class ServerRecipeBook extends RecipeBook {
   public static final String RECIPE_BOOK_KEY = "recipeBook";
   private static final Logger LOGGER = LogUtils.getLogger();

   public int unlockRecipes(Collection recipes, ServerPlayerEntity player) {
      List list = Lists.newArrayList();
      int i = 0;
      Iterator var5 = recipes.iterator();

      while(var5.hasNext()) {
         Recipe lv = (Recipe)var5.next();
         Identifier lv2 = lv.getId();
         if (!this.recipes.contains(lv2) && !lv.isIgnoredInRecipeBook()) {
            this.add(lv2);
            this.display(lv2);
            list.add(lv2);
            Criteria.RECIPE_UNLOCKED.trigger(player, lv);
            ++i;
         }
      }

      this.sendUnlockRecipesPacket(UnlockRecipesS2CPacket.Action.ADD, player, list);
      return i;
   }

   public int lockRecipes(Collection recipes, ServerPlayerEntity player) {
      List list = Lists.newArrayList();
      int i = 0;
      Iterator var5 = recipes.iterator();

      while(var5.hasNext()) {
         Recipe lv = (Recipe)var5.next();
         Identifier lv2 = lv.getId();
         if (this.recipes.contains(lv2)) {
            this.remove(lv2);
            list.add(lv2);
            ++i;
         }
      }

      this.sendUnlockRecipesPacket(UnlockRecipesS2CPacket.Action.REMOVE, player, list);
      return i;
   }

   private void sendUnlockRecipesPacket(UnlockRecipesS2CPacket.Action action, ServerPlayerEntity player, List recipeIds) {
      player.networkHandler.sendPacket(new UnlockRecipesS2CPacket(action, recipeIds, Collections.emptyList(), this.getOptions()));
   }

   public NbtCompound toNbt() {
      NbtCompound lv = new NbtCompound();
      this.getOptions().writeNbt(lv);
      NbtList lv2 = new NbtList();
      Iterator var3 = this.recipes.iterator();

      while(var3.hasNext()) {
         Identifier lv3 = (Identifier)var3.next();
         lv2.add(NbtString.of(lv3.toString()));
      }

      lv.put("recipes", lv2);
      NbtList lv4 = new NbtList();
      Iterator var7 = this.toBeDisplayed.iterator();

      while(var7.hasNext()) {
         Identifier lv5 = (Identifier)var7.next();
         lv4.add(NbtString.of(lv5.toString()));
      }

      lv.put("toBeDisplayed", lv4);
      return lv;
   }

   public void readNbt(NbtCompound nbt, RecipeManager recipeManager) {
      this.setOptions(RecipeBookOptions.fromNbt(nbt));
      NbtList lv = nbt.getList("recipes", NbtElement.STRING_TYPE);
      this.handleList(lv, this::add, recipeManager);
      NbtList lv2 = nbt.getList("toBeDisplayed", NbtElement.STRING_TYPE);
      this.handleList(lv2, this::display, recipeManager);
   }

   private void handleList(NbtList list, Consumer handler, RecipeManager recipeManager) {
      for(int i = 0; i < list.size(); ++i) {
         String string = list.getString(i);

         try {
            Identifier lv = new Identifier(string);
            Optional optional = recipeManager.get(lv);
            if (!optional.isPresent()) {
               LOGGER.error("Tried to load unrecognized recipe: {} removed now.", lv);
            } else {
               handler.accept((Recipe)optional.get());
            }
         } catch (InvalidIdentifierException var8) {
            LOGGER.error("Tried to load improperly formatted recipe: {} removed now.", string);
         }
      }

   }

   public void sendInitRecipesPacket(ServerPlayerEntity player) {
      player.networkHandler.sendPacket(new UnlockRecipesS2CPacket(UnlockRecipesS2CPacket.Action.INIT, this.recipes, this.toBeDisplayed, this.getOptions()));
   }
}
