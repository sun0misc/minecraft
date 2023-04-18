package net.minecraft.block;

import java.util.Objects;
import java.util.function.Predicate;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import org.jetbrains.annotations.Nullable;

public class EntityShapeContext implements ShapeContext {
   protected static final ShapeContext ABSENT;
   private final boolean descending;
   private final double minY;
   private final ItemStack heldItem;
   private final Predicate walkOnFluidPredicate;
   @Nullable
   private final Entity entity;

   protected EntityShapeContext(boolean descending, double minY, ItemStack heldItem, Predicate walkOnFluidPredicate, @Nullable Entity entity) {
      this.descending = descending;
      this.minY = minY;
      this.heldItem = heldItem;
      this.walkOnFluidPredicate = walkOnFluidPredicate;
      this.entity = entity;
   }

   /** @deprecated */
   @Deprecated
   protected EntityShapeContext(Entity entity) {
      boolean var10001 = entity.isDescending();
      double var10002 = entity.getY();
      ItemStack var10003 = entity instanceof LivingEntity ? ((LivingEntity)entity).getMainHandStack() : ItemStack.EMPTY;
      Predicate var2;
      if (entity instanceof LivingEntity var10004) {
         Objects.requireNonNull((LivingEntity)entity);
         var2 = var10004::canWalkOnFluid;
      } else {
         var2 = (arg) -> {
            return false;
         };
      }

      this(var10001, var10002, var10003, var2, entity);
   }

   public boolean isHolding(Item item) {
      return this.heldItem.isOf(item);
   }

   public boolean canWalkOnFluid(FluidState stateAbove, FluidState state) {
      return this.walkOnFluidPredicate.test(state) && !stateAbove.getFluid().matchesType(state.getFluid());
   }

   public boolean isDescending() {
      return this.descending;
   }

   public boolean isAbove(VoxelShape shape, BlockPos pos, boolean defaultValue) {
      return this.minY > (double)pos.getY() + shape.getMax(Direction.Axis.Y) - 9.999999747378752E-6;
   }

   @Nullable
   public Entity getEntity() {
      return this.entity;
   }

   static {
      ABSENT = new EntityShapeContext(false, -1.7976931348623157E308, ItemStack.EMPTY, (arg) -> {
         return false;
      }, (Entity)null) {
         public boolean isAbove(VoxelShape shape, BlockPos pos, boolean defaultValue) {
            return defaultValue;
         }
      };
   }
}
