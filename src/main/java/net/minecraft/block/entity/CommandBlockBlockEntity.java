package net.minecraft.block.entity;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.CommandBlock;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.CommandBlockExecutor;

public class CommandBlockBlockEntity extends BlockEntity {
   private boolean powered;
   private boolean auto;
   private boolean conditionMet;
   private final CommandBlockExecutor commandExecutor = new CommandBlockExecutor() {
      public void setCommand(String command) {
         super.setCommand(command);
         CommandBlockBlockEntity.this.markDirty();
      }

      public ServerWorld getWorld() {
         return (ServerWorld)CommandBlockBlockEntity.this.world;
      }

      public void markDirty() {
         BlockState lv = CommandBlockBlockEntity.this.world.getBlockState(CommandBlockBlockEntity.this.pos);
         this.getWorld().updateListeners(CommandBlockBlockEntity.this.pos, lv, lv, Block.NOTIFY_ALL);
      }

      public Vec3d getPos() {
         return Vec3d.ofCenter(CommandBlockBlockEntity.this.pos);
      }

      public ServerCommandSource getSource() {
         Direction lv = (Direction)CommandBlockBlockEntity.this.getCachedState().get(CommandBlock.FACING);
         return new ServerCommandSource(this, Vec3d.ofCenter(CommandBlockBlockEntity.this.pos), new Vec2f(0.0F, lv.asRotation()), this.getWorld(), 2, this.getCustomName().getString(), this.getCustomName(), this.getWorld().getServer(), (Entity)null);
      }
   };

   public CommandBlockBlockEntity(BlockPos pos, BlockState state) {
      super(BlockEntityType.COMMAND_BLOCK, pos, state);
   }

   protected void writeNbt(NbtCompound nbt) {
      super.writeNbt(nbt);
      this.commandExecutor.writeNbt(nbt);
      nbt.putBoolean("powered", this.isPowered());
      nbt.putBoolean("conditionMet", this.isConditionMet());
      nbt.putBoolean("auto", this.isAuto());
   }

   public void readNbt(NbtCompound nbt) {
      super.readNbt(nbt);
      this.commandExecutor.readNbt(nbt);
      this.powered = nbt.getBoolean("powered");
      this.conditionMet = nbt.getBoolean("conditionMet");
      this.setAuto(nbt.getBoolean("auto"));
   }

   public boolean copyItemDataRequiresOperator() {
      return true;
   }

   public CommandBlockExecutor getCommandExecutor() {
      return this.commandExecutor;
   }

   public void setPowered(boolean powered) {
      this.powered = powered;
   }

   public boolean isPowered() {
      return this.powered;
   }

   public boolean isAuto() {
      return this.auto;
   }

   public void setAuto(boolean auto) {
      boolean bl2 = this.auto;
      this.auto = auto;
      if (!bl2 && auto && !this.powered && this.world != null && this.getCommandBlockType() != CommandBlockBlockEntity.Type.SEQUENCE) {
         this.scheduleAutoTick();
      }

   }

   public void updateCommandBlock() {
      Type lv = this.getCommandBlockType();
      if (lv == CommandBlockBlockEntity.Type.AUTO && (this.powered || this.auto) && this.world != null) {
         this.scheduleAutoTick();
      }

   }

   private void scheduleAutoTick() {
      Block lv = this.getCachedState().getBlock();
      if (lv instanceof CommandBlock) {
         this.updateConditionMet();
         this.world.scheduleBlockTick(this.pos, lv, 1);
      }

   }

   public boolean isConditionMet() {
      return this.conditionMet;
   }

   public boolean updateConditionMet() {
      this.conditionMet = true;
      if (this.isConditionalCommandBlock()) {
         BlockPos lv = this.pos.offset(((Direction)this.world.getBlockState(this.pos).get(CommandBlock.FACING)).getOpposite());
         if (this.world.getBlockState(lv).getBlock() instanceof CommandBlock) {
            BlockEntity lv2 = this.world.getBlockEntity(lv);
            this.conditionMet = lv2 instanceof CommandBlockBlockEntity && ((CommandBlockBlockEntity)lv2).getCommandExecutor().getSuccessCount() > 0;
         } else {
            this.conditionMet = false;
         }
      }

      return this.conditionMet;
   }

   public Type getCommandBlockType() {
      BlockState lv = this.getCachedState();
      if (lv.isOf(Blocks.COMMAND_BLOCK)) {
         return CommandBlockBlockEntity.Type.REDSTONE;
      } else if (lv.isOf(Blocks.REPEATING_COMMAND_BLOCK)) {
         return CommandBlockBlockEntity.Type.AUTO;
      } else {
         return lv.isOf(Blocks.CHAIN_COMMAND_BLOCK) ? CommandBlockBlockEntity.Type.SEQUENCE : CommandBlockBlockEntity.Type.REDSTONE;
      }
   }

   public boolean isConditionalCommandBlock() {
      BlockState lv = this.world.getBlockState(this.getPos());
      return lv.getBlock() instanceof CommandBlock ? (Boolean)lv.get(CommandBlock.CONDITIONAL) : false;
   }

   public static enum Type {
      SEQUENCE,
      AUTO,
      REDSTONE;

      // $FF: synthetic method
      private static Type[] method_36715() {
         return new Type[]{SEQUENCE, AUTO, REDSTONE};
      }
   }
}
