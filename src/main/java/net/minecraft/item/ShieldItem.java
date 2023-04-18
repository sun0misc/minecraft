package net.minecraft.item;

import java.util.List;
import net.minecraft.block.DispenserBlock;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.UseAction;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class ShieldItem extends Item implements Equipment {
   public static final int field_30918 = 5;
   public static final float MIN_DAMAGE_AMOUNT_TO_BREAK = 3.0F;
   public static final String BASE_KEY = "Base";

   public ShieldItem(Item.Settings arg) {
      super(arg);
      DispenserBlock.registerBehavior(this, ArmorItem.DISPENSER_BEHAVIOR);
   }

   public String getTranslationKey(ItemStack stack) {
      if (BlockItem.getBlockEntityNbt(stack) != null) {
         String var10000 = this.getTranslationKey();
         return var10000 + "." + getColor(stack).getName();
      } else {
         return super.getTranslationKey(stack);
      }
   }

   public void appendTooltip(ItemStack stack, @Nullable World world, List tooltip, TooltipContext context) {
      BannerItem.appendBannerTooltip(stack, tooltip);
   }

   public UseAction getUseAction(ItemStack stack) {
      return UseAction.BLOCK;
   }

   public int getMaxUseTime(ItemStack stack) {
      return 72000;
   }

   public TypedActionResult use(World world, PlayerEntity user, Hand hand) {
      ItemStack lv = user.getStackInHand(hand);
      user.setCurrentHand(hand);
      return TypedActionResult.consume(lv);
   }

   public boolean canRepair(ItemStack stack, ItemStack ingredient) {
      return ingredient.isIn(ItemTags.PLANKS) || super.canRepair(stack, ingredient);
   }

   public static DyeColor getColor(ItemStack stack) {
      NbtCompound lv = BlockItem.getBlockEntityNbt(stack);
      return lv != null ? DyeColor.byId(lv.getInt("Base")) : DyeColor.WHITE;
   }

   public EquipmentSlot getSlotType() {
      return EquipmentSlot.OFFHAND;
   }
}
