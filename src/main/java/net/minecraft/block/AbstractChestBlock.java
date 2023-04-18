package net.minecraft.block;

import java.util.function.Supplier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public abstract class AbstractChestBlock extends BlockWithEntity {
   protected final Supplier entityTypeRetriever;

   protected AbstractChestBlock(AbstractBlock.Settings settings, Supplier entityTypeSupplier) {
      super(settings);
      this.entityTypeRetriever = entityTypeSupplier;
   }

   public abstract DoubleBlockProperties.PropertySource getBlockEntitySource(BlockState state, World world, BlockPos pos, boolean ignoreBlocked);
}
