package net.minecraft.world.gen.feature;

import java.util.Iterator;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.WallTorchBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.gen.feature.util.FeatureContext;

public class EndPortalFeature extends Feature {
   public static final int field_31503 = 4;
   public static final int field_31504 = 4;
   public static final int field_31505 = 1;
   public static final float field_31506 = 0.5F;
   public static final BlockPos ORIGIN;
   private final boolean open;

   public EndPortalFeature(boolean open) {
      super(DefaultFeatureConfig.CODEC);
      this.open = open;
   }

   public boolean generate(FeatureContext context) {
      BlockPos lv = context.getOrigin();
      StructureWorldAccess lv2 = context.getWorld();
      Iterator var4 = BlockPos.iterate(new BlockPos(lv.getX() - 4, lv.getY() - 1, lv.getZ() - 4), new BlockPos(lv.getX() + 4, lv.getY() + 32, lv.getZ() + 4)).iterator();

      while(true) {
         BlockPos lv3;
         boolean bl;
         do {
            if (!var4.hasNext()) {
               for(int i = 0; i < 4; ++i) {
                  this.setBlockState(lv2, lv.up(i), Blocks.BEDROCK.getDefaultState());
               }

               BlockPos lv4 = lv.up(2);
               Iterator var9 = Direction.Type.HORIZONTAL.iterator();

               while(var9.hasNext()) {
                  Direction lv5 = (Direction)var9.next();
                  this.setBlockState(lv2, lv4.offset(lv5), (BlockState)Blocks.WALL_TORCH.getDefaultState().with(WallTorchBlock.FACING, lv5));
               }

               return true;
            }

            lv3 = (BlockPos)var4.next();
            bl = lv3.isWithinDistance(lv, 2.5);
         } while(!bl && !lv3.isWithinDistance(lv, 3.5));

         if (lv3.getY() < lv.getY()) {
            if (bl) {
               this.setBlockState(lv2, lv3, Blocks.BEDROCK.getDefaultState());
            } else if (lv3.getY() < lv.getY()) {
               this.setBlockState(lv2, lv3, Blocks.END_STONE.getDefaultState());
            }
         } else if (lv3.getY() > lv.getY()) {
            this.setBlockState(lv2, lv3, Blocks.AIR.getDefaultState());
         } else if (!bl) {
            this.setBlockState(lv2, lv3, Blocks.BEDROCK.getDefaultState());
         } else if (this.open) {
            this.setBlockState(lv2, new BlockPos(lv3), Blocks.END_PORTAL.getDefaultState());
         } else {
            this.setBlockState(lv2, new BlockPos(lv3), Blocks.AIR.getDefaultState());
         }
      }
   }

   static {
      ORIGIN = BlockPos.ORIGIN;
   }
}
