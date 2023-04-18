package net.minecraft.item;

import com.google.common.collect.Lists;
import com.mojang.logging.LogUtils;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeManager;
import net.minecraft.stat.Stats;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;
import org.slf4j.Logger;

public class KnowledgeBookItem extends Item {
   private static final String RECIPES_KEY = "Recipes";
   private static final Logger LOGGER = LogUtils.getLogger();

   public KnowledgeBookItem(Item.Settings arg) {
      super(arg);
   }

   public TypedActionResult use(World world, PlayerEntity user, Hand hand) {
      ItemStack lv = user.getStackInHand(hand);
      NbtCompound lv2 = lv.getNbt();
      if (!user.getAbilities().creativeMode) {
         user.setStackInHand(hand, ItemStack.EMPTY);
      }

      if (lv2 != null && lv2.contains("Recipes", NbtElement.LIST_TYPE)) {
         if (!world.isClient) {
            NbtList lv3 = lv2.getList("Recipes", NbtElement.STRING_TYPE);
            List list = Lists.newArrayList();
            RecipeManager lv4 = world.getServer().getRecipeManager();

            for(int i = 0; i < lv3.size(); ++i) {
               String string = lv3.getString(i);
               Optional optional = lv4.get(new Identifier(string));
               if (!optional.isPresent()) {
                  LOGGER.error("Invalid recipe: {}", string);
                  return TypedActionResult.fail(lv);
               }

               list.add((Recipe)optional.get());
            }

            user.unlockRecipes((Collection)list);
            user.incrementStat(Stats.USED.getOrCreateStat(this));
         }

         return TypedActionResult.success(lv, world.isClient());
      } else {
         LOGGER.error("Tag not valid: {}", lv2);
         return TypedActionResult.fail(lv);
      }
   }
}
