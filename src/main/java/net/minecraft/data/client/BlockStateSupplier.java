package net.minecraft.data.client;

import java.util.function.Supplier;
import net.minecraft.block.Block;

public interface BlockStateSupplier extends Supplier {
   Block getBlock();
}
