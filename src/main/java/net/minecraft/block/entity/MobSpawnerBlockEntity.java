package net.minecraft.block.entity;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.EntityType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.MobSpawnerEntry;
import net.minecraft.world.MobSpawnerLogic;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class MobSpawnerBlockEntity extends BlockEntity {
   private final MobSpawnerLogic logic = new MobSpawnerLogic() {
      public void sendStatus(World world, BlockPos pos, int status) {
         world.addSyncedBlockEvent(pos, Blocks.SPAWNER, status, 0);
      }

      public void setSpawnEntry(@Nullable World world, BlockPos pos, MobSpawnerEntry spawnEntry) {
         super.setSpawnEntry(world, pos, spawnEntry);
         if (world != null) {
            BlockState lv = world.getBlockState(pos);
            world.updateListeners(pos, lv, lv, Block.NO_REDRAW);
         }

      }
   };

   public MobSpawnerBlockEntity(BlockPos pos, BlockState state) {
      super(BlockEntityType.MOB_SPAWNER, pos, state);
   }

   public void readNbt(NbtCompound nbt) {
      super.readNbt(nbt);
      this.logic.readNbt(this.world, this.pos, nbt);
   }

   protected void writeNbt(NbtCompound nbt) {
      super.writeNbt(nbt);
      this.logic.writeNbt(nbt);
   }

   public static void clientTick(World world, BlockPos pos, BlockState state, MobSpawnerBlockEntity blockEntity) {
      blockEntity.logic.clientTick(world, pos);
   }

   public static void serverTick(World world, BlockPos pos, BlockState state, MobSpawnerBlockEntity blockEntity) {
      blockEntity.logic.serverTick((ServerWorld)world, pos);
   }

   public BlockEntityUpdateS2CPacket toUpdatePacket() {
      return BlockEntityUpdateS2CPacket.create(this);
   }

   public NbtCompound toInitialChunkDataNbt() {
      NbtCompound lv = this.createNbt();
      lv.remove("SpawnPotentials");
      return lv;
   }

   public boolean onSyncedBlockEvent(int type, int data) {
      return this.logic.handleStatus(this.world, type) ? true : super.onSyncedBlockEvent(type, data);
   }

   public boolean copyItemDataRequiresOperator() {
      return true;
   }

   public void setEntityType(EntityType entityType, Random random) {
      this.logic.setEntityId(entityType, this.world, random, this.pos);
   }

   public MobSpawnerLogic getLogic() {
      return this.logic;
   }

   // $FF: synthetic method
   public Packet toUpdatePacket() {
      return this.toUpdatePacket();
   }
}
