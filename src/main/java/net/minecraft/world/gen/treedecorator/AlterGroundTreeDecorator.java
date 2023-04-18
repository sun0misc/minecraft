package net.minecraft.world.gen.treedecorator;

import com.google.common.collect.Lists;
import com.mojang.serialization.Codec;
import java.util.List;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.stateprovider.BlockStateProvider;

public class AlterGroundTreeDecorator extends TreeDecorator {
   public static final Codec CODEC;
   private final BlockStateProvider provider;

   public AlterGroundTreeDecorator(BlockStateProvider provider) {
      this.provider = provider;
   }

   protected TreeDecoratorType getType() {
      return TreeDecoratorType.ALTER_GROUND;
   }

   public void generate(TreeDecorator.Generator generator) {
      List list = Lists.newArrayList();
      List list2 = generator.getRootPositions();
      List list3 = generator.getLogPositions();
      if (list2.isEmpty()) {
         list.addAll(list3);
      } else if (!list3.isEmpty() && ((BlockPos)list2.get(0)).getY() == ((BlockPos)list3.get(0)).getY()) {
         list.addAll(list3);
         list.addAll(list2);
      } else {
         list.addAll(list2);
      }

      if (!list.isEmpty()) {
         int i = ((BlockPos)list.get(0)).getY();
         list.stream().filter((pos) -> {
            return pos.getY() == i;
         }).forEach((pos) -> {
            this.setArea(generator, pos.west().north());
            this.setArea(generator, pos.east(2).north());
            this.setArea(generator, pos.west().south(2));
            this.setArea(generator, pos.east(2).south(2));

            for(int i = 0; i < 5; ++i) {
               int j = generator.getRandom().nextInt(64);
               int k = j % 8;
               int l = j / 8;
               if (k == 0 || k == 7 || l == 0 || l == 7) {
                  this.setArea(generator, pos.add(-3 + k, 0, -3 + l));
               }
            }

         });
      }
   }

   private void setArea(TreeDecorator.Generator generator, BlockPos origin) {
      for(int i = -2; i <= 2; ++i) {
         for(int j = -2; j <= 2; ++j) {
            if (Math.abs(i) != 2 || Math.abs(j) != 2) {
               this.setColumn(generator, origin.add(i, 0, j));
            }
         }
      }

   }

   private void setColumn(TreeDecorator.Generator generator, BlockPos origin) {
      for(int i = 2; i >= -3; --i) {
         BlockPos lv = origin.up(i);
         if (Feature.isSoil(generator.getWorld(), lv)) {
            generator.replace(lv, this.provider.get(generator.getRandom(), origin));
            break;
         }

         if (!generator.isAir(lv) && i < 0) {
            break;
         }
      }

   }

   static {
      CODEC = BlockStateProvider.TYPE_CODEC.fieldOf("provider").xmap(AlterGroundTreeDecorator::new, (decorator) -> {
         return decorator.provider;
      }).codec();
   }
}
