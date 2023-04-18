package net.minecraft.command.argument;

import java.util.Iterator;
import java.util.Set;
import java.util.function.Predicate;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.pattern.CachedBlockPosition;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.property.Property;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;

public class BlockStateArgument implements Predicate {
   private final BlockState state;
   private final Set properties;
   @Nullable
   private final NbtCompound data;

   public BlockStateArgument(BlockState state, Set properties, @Nullable NbtCompound data) {
      this.state = state;
      this.properties = properties;
      this.data = data;
   }

   public BlockState getBlockState() {
      return this.state;
   }

   public Set getProperties() {
      return this.properties;
   }

   public boolean test(CachedBlockPosition arg) {
      BlockState lv = arg.getBlockState();
      if (!lv.isOf(this.state.getBlock())) {
         return false;
      } else {
         Iterator var3 = this.properties.iterator();

         while(var3.hasNext()) {
            Property lv2 = (Property)var3.next();
            if (lv.get(lv2) != this.state.get(lv2)) {
               return false;
            }
         }

         if (this.data == null) {
            return true;
         } else {
            BlockEntity lv3 = arg.getBlockEntity();
            return lv3 != null && NbtHelper.matches(this.data, lv3.createNbtWithIdentifyingData(), true);
         }
      }
   }

   public boolean test(ServerWorld world, BlockPos pos) {
      return this.test(new CachedBlockPosition(world, pos, false));
   }

   public boolean setBlockState(ServerWorld world, BlockPos pos, int flags) {
      BlockState lv = Block.postProcessState(this.state, world, pos);
      if (lv.isAir()) {
         lv = this.state;
      }

      if (!world.setBlockState(pos, lv, flags)) {
         return false;
      } else {
         if (this.data != null) {
            BlockEntity lv2 = world.getBlockEntity(pos);
            if (lv2 != null) {
               lv2.readNbt(this.data);
            }
         }

         return true;
      }
   }

   // $FF: synthetic method
   public boolean test(Object context) {
      return this.test((CachedBlockPosition)context);
   }
}
