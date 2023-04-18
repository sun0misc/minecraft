package net.minecraft.block.entity;

import com.mojang.logging.LogUtils;
import net.minecraft.block.BlockState;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.packet.Packet;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.minecraft.util.crash.CrashReportSection;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

public abstract class BlockEntity {
   private static final Logger LOGGER = LogUtils.getLogger();
   private final BlockEntityType type;
   @Nullable
   protected World world;
   protected final BlockPos pos;
   protected boolean removed;
   private BlockState cachedState;

   public BlockEntity(BlockEntityType type, BlockPos pos, BlockState state) {
      this.type = type;
      this.pos = pos.toImmutable();
      this.cachedState = state;
   }

   public static BlockPos posFromNbt(NbtCompound nbt) {
      return new BlockPos(nbt.getInt("x"), nbt.getInt("y"), nbt.getInt("z"));
   }

   @Nullable
   public World getWorld() {
      return this.world;
   }

   public void setWorld(World world) {
      this.world = world;
   }

   public boolean hasWorld() {
      return this.world != null;
   }

   public void readNbt(NbtCompound nbt) {
   }

   protected void writeNbt(NbtCompound nbt) {
   }

   public final NbtCompound createNbtWithIdentifyingData() {
      NbtCompound lv = this.createNbt();
      this.writeIdentifyingData(lv);
      return lv;
   }

   public final NbtCompound createNbtWithId() {
      NbtCompound lv = this.createNbt();
      this.writeIdToNbt(lv);
      return lv;
   }

   public final NbtCompound createNbt() {
      NbtCompound lv = new NbtCompound();
      this.writeNbt(lv);
      return lv;
   }

   private void writeIdToNbt(NbtCompound nbt) {
      Identifier lv = BlockEntityType.getId(this.getType());
      if (lv == null) {
         throw new RuntimeException(this.getClass() + " is missing a mapping! This is a bug!");
      } else {
         nbt.putString("id", lv.toString());
      }
   }

   public static void writeIdToNbt(NbtCompound nbt, BlockEntityType type) {
      nbt.putString("id", BlockEntityType.getId(type).toString());
   }

   public void setStackNbt(ItemStack stack) {
      BlockItem.setBlockEntityNbt(stack, this.getType(), this.createNbt());
   }

   private void writeIdentifyingData(NbtCompound nbt) {
      this.writeIdToNbt(nbt);
      nbt.putInt("x", this.pos.getX());
      nbt.putInt("y", this.pos.getY());
      nbt.putInt("z", this.pos.getZ());
   }

   @Nullable
   public static BlockEntity createFromNbt(BlockPos pos, BlockState state, NbtCompound nbt) {
      String string = nbt.getString("id");
      Identifier lv = Identifier.tryParse(string);
      if (lv == null) {
         LOGGER.error("Block entity has invalid type: {}", string);
         return null;
      } else {
         return (BlockEntity)Registries.BLOCK_ENTITY_TYPE.getOrEmpty(lv).map((type) -> {
            try {
               return type.instantiate(pos, state);
            } catch (Throwable var5) {
               LOGGER.error("Failed to create block entity {}", string, var5);
               return null;
            }
         }).map((blockEntity) -> {
            try {
               blockEntity.readNbt(nbt);
               return blockEntity;
            } catch (Throwable var4) {
               LOGGER.error("Failed to load data for block entity {}", string, var4);
               return null;
            }
         }).orElseGet(() -> {
            LOGGER.warn("Skipping BlockEntity with id {}", string);
            return null;
         });
      }
   }

   public void markDirty() {
      if (this.world != null) {
         markDirty(this.world, this.pos, this.cachedState);
      }

   }

   protected static void markDirty(World world, BlockPos pos, BlockState state) {
      world.markDirty(pos);
      if (!state.isAir()) {
         world.updateComparators(pos, state.getBlock());
      }

   }

   public BlockPos getPos() {
      return this.pos;
   }

   public BlockState getCachedState() {
      return this.cachedState;
   }

   @Nullable
   public Packet toUpdatePacket() {
      return null;
   }

   public NbtCompound toInitialChunkDataNbt() {
      return new NbtCompound();
   }

   public boolean isRemoved() {
      return this.removed;
   }

   public void markRemoved() {
      this.removed = true;
   }

   public void cancelRemoval() {
      this.removed = false;
   }

   public boolean onSyncedBlockEvent(int type, int data) {
      return false;
   }

   public void populateCrashReport(CrashReportSection crashReportSection) {
      crashReportSection.add("Name", () -> {
         Identifier var10000 = Registries.BLOCK_ENTITY_TYPE.getId(this.getType());
         return "" + var10000 + " // " + this.getClass().getCanonicalName();
      });
      if (this.world != null) {
         CrashReportSection.addBlockInfo(crashReportSection, this.world, this.pos, this.getCachedState());
         CrashReportSection.addBlockInfo(crashReportSection, this.world, this.pos, this.world.getBlockState(this.pos));
      }
   }

   public boolean copyItemDataRequiresOperator() {
      return false;
   }

   public BlockEntityType getType() {
      return this.type;
   }

   /** @deprecated */
   @Deprecated
   public void setCachedState(BlockState state) {
      this.cachedState = state;
   }
}
