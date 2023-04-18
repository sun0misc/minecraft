package net.minecraft.item;

import com.google.common.collect.Maps;
import java.util.Map;
import net.minecraft.block.entity.SignBlockEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.passive.SheepEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Hand;
import net.minecraft.world.World;

public class DyeItem extends Item implements SignChangingItem {
   private static final Map DYES = Maps.newEnumMap(DyeColor.class);
   private final DyeColor color;

   public DyeItem(DyeColor color, Item.Settings settings) {
      super(settings);
      this.color = color;
      DYES.put(color, this);
   }

   public ActionResult useOnEntity(ItemStack stack, PlayerEntity user, LivingEntity entity, Hand hand) {
      if (entity instanceof SheepEntity lv) {
         if (lv.isAlive() && !lv.isSheared() && lv.getColor() != this.color) {
            lv.world.playSoundFromEntity(user, lv, SoundEvents.ITEM_DYE_USE, SoundCategory.PLAYERS, 1.0F, 1.0F);
            if (!user.world.isClient) {
               lv.setColor(this.color);
               stack.decrement(1);
            }

            return ActionResult.success(user.world.isClient);
         }
      }

      return ActionResult.PASS;
   }

   public DyeColor getColor() {
      return this.color;
   }

   public static DyeItem byColor(DyeColor color) {
      return (DyeItem)DYES.get(color);
   }

   public boolean useOnSign(World world, SignBlockEntity signBlockEntity, boolean front, PlayerEntity player) {
      if (signBlockEntity.changeText((text) -> {
         return text.withColor(this.getColor());
      }, front)) {
         world.playSound((PlayerEntity)null, signBlockEntity.getPos(), SoundEvents.ITEM_DYE_USE, SoundCategory.BLOCKS, 1.0F, 1.0F);
         return true;
      } else {
         return false;
      }
   }
}
