package net.minecraft.block;

import java.util.Iterator;
import net.minecraft.block.entity.SculkSpreadManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluids;
import net.minecraft.sound.SoundCategory;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.intprovider.ConstantIntProvider;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.WorldAccess;

public class SculkBlock extends ExperienceDroppingBlock implements SculkSpreadable {
   public SculkBlock(AbstractBlock.Settings arg) {
      super(arg, ConstantIntProvider.create(1));
   }

   public int spread(SculkSpreadManager.Cursor cursor, WorldAccess world, BlockPos catalystPos, Random random, SculkSpreadManager spreadManager, boolean shouldConvertToBlock) {
      int i = cursor.getCharge();
      if (i != 0 && random.nextInt(spreadManager.getSpreadChance()) == 0) {
         BlockPos lv = cursor.getPos();
         boolean bl2 = lv.isWithinDistance(catalystPos, (double)spreadManager.getMaxDistance());
         if (!bl2 && shouldNotDecay(world, lv)) {
            int j = spreadManager.getExtraBlockChance();
            if (random.nextInt(j) < i) {
               BlockPos lv2 = lv.up();
               BlockState lv3 = this.getExtraBlockState(world, lv2, random, spreadManager.isWorldGen());
               world.setBlockState(lv2, lv3, Block.NOTIFY_ALL);
               world.playSound((PlayerEntity)null, lv, lv3.getSoundGroup().getPlaceSound(), SoundCategory.BLOCKS, 1.0F, 1.0F);
            }

            return Math.max(0, i - j);
         } else {
            return random.nextInt(spreadManager.getDecayChance()) != 0 ? i : i - (bl2 ? 1 : getDecay(spreadManager, lv, catalystPos, i));
         }
      } else {
         return i;
      }
   }

   private static int getDecay(SculkSpreadManager spreadManager, BlockPos cursorPos, BlockPos catalystPos, int charge) {
      int j = spreadManager.getMaxDistance();
      float f = MathHelper.square((float)Math.sqrt(cursorPos.getSquaredDistance(catalystPos)) - (float)j);
      int k = MathHelper.square(24 - j);
      float g = Math.min(1.0F, f / (float)k);
      return Math.max(1, (int)((float)charge * g * 0.5F));
   }

   private BlockState getExtraBlockState(WorldAccess world, BlockPos pos, Random random, boolean allowShrieker) {
      BlockState lv;
      if (random.nextInt(11) == 0) {
         lv = (BlockState)Blocks.SCULK_SHRIEKER.getDefaultState().with(SculkShriekerBlock.CAN_SUMMON, allowShrieker);
      } else {
         lv = Blocks.SCULK_SENSOR.getDefaultState();
      }

      return lv.contains(Properties.WATERLOGGED) && !world.getFluidState(pos).isEmpty() ? (BlockState)lv.with(Properties.WATERLOGGED, true) : lv;
   }

   private static boolean shouldNotDecay(WorldAccess world, BlockPos pos) {
      BlockState lv = world.getBlockState(pos.up());
      if (lv.isAir() || lv.isOf(Blocks.WATER) && lv.getFluidState().isOf(Fluids.WATER)) {
         int i = 0;
         Iterator var4 = BlockPos.iterate(pos.add(-4, 0, -4), pos.add(4, 2, 4)).iterator();

         do {
            if (!var4.hasNext()) {
               return true;
            }

            BlockPos lv2 = (BlockPos)var4.next();
            BlockState lv3 = world.getBlockState(lv2);
            if (lv3.isOf(Blocks.SCULK_SENSOR) || lv3.isOf(Blocks.SCULK_SHRIEKER)) {
               ++i;
            }
         } while(i <= 2);

         return false;
      } else {
         return false;
      }
   }

   public boolean shouldConvertToSpreadable() {
      return false;
   }
}
