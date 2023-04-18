package net.minecraft.enchantment;

import java.util.Iterator;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.FrostedIceBlock;
import net.minecraft.block.ShapeContext;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

public class FrostWalkerEnchantment extends Enchantment {
   public FrostWalkerEnchantment(Enchantment.Rarity weight, EquipmentSlot... slotTypes) {
      super(weight, EnchantmentTarget.ARMOR_FEET, slotTypes);
   }

   public int getMinPower(int level) {
      return level * 10;
   }

   public int getMaxPower(int level) {
      return this.getMinPower(level) + 15;
   }

   public boolean isTreasure() {
      return true;
   }

   public int getMaxLevel() {
      return 2;
   }

   public static void freezeWater(LivingEntity entity, World world, BlockPos blockPos, int level) {
      if (entity.isOnGround()) {
         BlockState lv = Blocks.FROSTED_ICE.getDefaultState();
         int j = Math.min(16, 2 + level);
         BlockPos.Mutable lv2 = new BlockPos.Mutable();
         Iterator var7 = BlockPos.iterate(blockPos.add(-j, -1, -j), blockPos.add(j, -1, j)).iterator();

         while(var7.hasNext()) {
            BlockPos lv3 = (BlockPos)var7.next();
            if (lv3.isWithinDistance(entity.getPos(), (double)j)) {
               lv2.set(lv3.getX(), lv3.getY() + 1, lv3.getZ());
               BlockState lv4 = world.getBlockState(lv2);
               if (lv4.isAir()) {
                  BlockState lv5 = world.getBlockState(lv3);
                  if (lv5 == FrostedIceBlock.getMeltedState() && lv.canPlaceAt(world, lv3) && world.canPlace(lv, lv3, ShapeContext.absent())) {
                     world.setBlockState(lv3, lv);
                     world.scheduleBlockTick(lv3, Blocks.FROSTED_ICE, MathHelper.nextInt(entity.getRandom(), 60, 120));
                  }
               }
            }
         }

      }
   }

   public boolean canAccept(Enchantment other) {
      return super.canAccept(other) && other != Enchantments.DEPTH_STRIDER;
   }
}
