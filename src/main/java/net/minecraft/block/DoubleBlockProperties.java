package net.minecraft.block;

import java.util.function.BiPredicate;
import java.util.function.Function;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.WorldAccess;

public class DoubleBlockProperties {
   public static PropertySource toPropertySource(BlockEntityType blockEntityType, Function typeMapper, Function function2, DirectionProperty arg2, BlockState state, WorldAccess world, BlockPos pos, BiPredicate fallbackTester) {
      BlockEntity lv = blockEntityType.get(world, pos);
      if (lv == null) {
         return PropertyRetriever::getFallback;
      } else if (fallbackTester.test(world, pos)) {
         return PropertyRetriever::getFallback;
      } else {
         Type lv2 = (Type)typeMapper.apply(state);
         boolean bl = lv2 == DoubleBlockProperties.Type.SINGLE;
         boolean bl2 = lv2 == DoubleBlockProperties.Type.FIRST;
         if (bl) {
            return new PropertySource.Single(lv);
         } else {
            BlockPos lv3 = pos.offset((Direction)function2.apply(state));
            BlockState lv4 = world.getBlockState(lv3);
            if (lv4.isOf(state.getBlock())) {
               Type lv5 = (Type)typeMapper.apply(lv4);
               if (lv5 != DoubleBlockProperties.Type.SINGLE && lv2 != lv5 && lv4.get(arg2) == state.get(arg2)) {
                  if (fallbackTester.test(world, lv3)) {
                     return PropertyRetriever::getFallback;
                  }

                  BlockEntity lv6 = blockEntityType.get(world, lv3);
                  if (lv6 != null) {
                     BlockEntity lv7 = bl2 ? lv : lv6;
                     BlockEntity lv8 = bl2 ? lv6 : lv;
                     return new PropertySource.Pair(lv7, lv8);
                  }
               }
            }

            return new PropertySource.Single(lv);
         }
      }
   }

   public interface PropertySource {
      Object apply(PropertyRetriever retriever);

      public static final class Single implements PropertySource {
         private final Object single;

         public Single(Object single) {
            this.single = single;
         }

         public Object apply(PropertyRetriever arg) {
            return arg.getFrom(this.single);
         }
      }

      public static final class Pair implements PropertySource {
         private final Object first;
         private final Object second;

         public Pair(Object first, Object second) {
            this.first = first;
            this.second = second;
         }

         public Object apply(PropertyRetriever arg) {
            return arg.getFromBoth(this.first, this.second);
         }
      }
   }

   public static enum Type {
      SINGLE,
      FIRST,
      SECOND;

      // $FF: synthetic method
      private static Type[] method_36705() {
         return new Type[]{SINGLE, FIRST, SECOND};
      }
   }

   public interface PropertyRetriever {
      Object getFromBoth(Object first, Object second);

      Object getFrom(Object single);

      Object getFallback();
   }
}
