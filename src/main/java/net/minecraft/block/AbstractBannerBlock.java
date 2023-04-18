package net.minecraft.block;

import net.minecraft.block.entity.BannerBlockEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DyeColor;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public abstract class AbstractBannerBlock extends BlockWithEntity {
   private final DyeColor color;

   protected AbstractBannerBlock(DyeColor color, AbstractBlock.Settings settings) {
      super(settings);
      this.color = color;
   }

   public boolean canMobSpawnInside(BlockState state) {
      return true;
   }

   public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
      return new BannerBlockEntity(pos, state, this.color);
   }

   public void onPlaced(World world, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack itemStack) {
      if (world.isClient) {
         world.getBlockEntity(pos, BlockEntityType.BANNER).ifPresent((blockEntity) -> {
            blockEntity.readFrom(itemStack);
         });
      } else if (itemStack.hasCustomName()) {
         world.getBlockEntity(pos, BlockEntityType.BANNER).ifPresent((blockEntity) -> {
            blockEntity.setCustomName(itemStack.getName());
         });
      }

   }

   public ItemStack getPickStack(BlockView world, BlockPos pos, BlockState state) {
      BlockEntity lv = world.getBlockEntity(pos);
      return lv instanceof BannerBlockEntity ? ((BannerBlockEntity)lv).getPickStack() : super.getPickStack(world, pos, state);
   }

   public DyeColor getColor() {
      return this.color;
   }
}
