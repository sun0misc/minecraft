package net.minecraft.world.gen.feature;

import com.mojang.serialization.Codec;
import java.util.Iterator;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.EndGatewayBlockEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.gen.feature.util.FeatureContext;

public class EndGatewayFeature extends Feature {
   public EndGatewayFeature(Codec codec) {
      super(codec);
   }

   public boolean generate(FeatureContext context) {
      BlockPos lv = context.getOrigin();
      StructureWorldAccess lv2 = context.getWorld();
      EndGatewayFeatureConfig lv3 = (EndGatewayFeatureConfig)context.getConfig();
      Iterator var5 = BlockPos.iterate(lv.add(-1, -2, -1), lv.add(1, 2, 1)).iterator();

      while(true) {
         while(var5.hasNext()) {
            BlockPos lv4 = (BlockPos)var5.next();
            boolean bl = lv4.getX() == lv.getX();
            boolean bl2 = lv4.getY() == lv.getY();
            boolean bl3 = lv4.getZ() == lv.getZ();
            boolean bl4 = Math.abs(lv4.getY() - lv.getY()) == 2;
            if (bl && bl2 && bl3) {
               BlockPos lv5 = lv4.toImmutable();
               this.setBlockState(lv2, lv5, Blocks.END_GATEWAY.getDefaultState());
               lv3.getExitPos().ifPresent((pos) -> {
                  BlockEntity lv = lv2.getBlockEntity(lv5);
                  if (lv instanceof EndGatewayBlockEntity lv2x) {
                     lv2x.setExitPortalPos(pos, lv3.isExact());
                     lv.markDirty();
                  }

               });
            } else if (bl2) {
               this.setBlockState(lv2, lv4, Blocks.AIR.getDefaultState());
            } else if (bl4 && bl && bl3) {
               this.setBlockState(lv2, lv4, Blocks.BEDROCK.getDefaultState());
            } else if ((bl || bl3) && !bl4) {
               this.setBlockState(lv2, lv4, Blocks.BEDROCK.getDefaultState());
            } else {
               this.setBlockState(lv2, lv4, Blocks.AIR.getDefaultState());
            }
         }

         return true;
      }
   }
}
