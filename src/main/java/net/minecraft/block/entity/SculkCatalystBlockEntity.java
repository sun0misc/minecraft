package net.minecraft.block.entity;

import com.google.common.annotations.VisibleForTesting;
import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.block.BlockState;
import net.minecraft.block.SculkCatalystBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.event.BlockPositionSource;
import net.minecraft.world.event.GameEvent;
import net.minecraft.world.event.PositionSource;
import net.minecraft.world.event.listener.GameEventListener;

public class SculkCatalystBlockEntity extends BlockEntity implements GameEventListener {
   private final BlockPositionSource positionSource;
   private final SculkSpreadManager spreadManager;

   public SculkCatalystBlockEntity(BlockPos pos, BlockState state) {
      super(BlockEntityType.SCULK_CATALYST, pos, state);
      this.positionSource = new BlockPositionSource(this.pos);
      this.spreadManager = SculkSpreadManager.create();
   }

   public PositionSource getPositionSource() {
      return this.positionSource;
   }

   public int getRange() {
      return 8;
   }

   public GameEventListener.TriggerOrder getTriggerOrder() {
      return GameEventListener.TriggerOrder.BY_DISTANCE;
   }

   public boolean listen(ServerWorld world, GameEvent event, GameEvent.Emitter emitter, Vec3d emitterPos) {
      if (event == GameEvent.ENTITY_DIE) {
         Entity var6 = emitter.sourceEntity();
         if (var6 instanceof LivingEntity) {
            LivingEntity lv = (LivingEntity)var6;
            if (!lv.isExperienceDroppingDisabled()) {
               int i = lv.getXpToDrop();
               if (lv.shouldDropXp() && i > 0) {
                  this.spreadManager.spread(BlockPos.ofFloored(emitterPos.offset(Direction.UP, 0.5)), i);
                  this.triggerCriteria(lv);
               }

               lv.disableExperienceDropping();
               SculkCatalystBlock.bloom(world, this.pos, this.getCachedState(), world.getRandom());
            }

            return true;
         }
      }

      return false;
   }

   private void triggerCriteria(LivingEntity deadEntity) {
      LivingEntity lv = deadEntity.getAttacker();
      if (lv instanceof ServerPlayerEntity lv2) {
         DamageSource lv3 = deadEntity.getRecentDamageSource() == null ? this.world.getDamageSources().playerAttack(lv2) : deadEntity.getRecentDamageSource();
         Criteria.KILL_MOB_NEAR_SCULK_CATALYST.trigger(lv2, deadEntity, lv3);
      }

   }

   public static void tick(World world, BlockPos pos, BlockState state, SculkCatalystBlockEntity blockEntity) {
      blockEntity.spreadManager.tick(world, pos, world.getRandom(), true);
   }

   public void readNbt(NbtCompound nbt) {
      super.readNbt(nbt);
      this.spreadManager.readNbt(nbt);
   }

   protected void writeNbt(NbtCompound nbt) {
      this.spreadManager.writeNbt(nbt);
      super.writeNbt(nbt);
   }

   @VisibleForTesting
   public SculkSpreadManager getSpreadManager() {
      return this.spreadManager;
   }

   // $FF: synthetic method
   private static Integer method_41518(SculkSpreadManager.Cursor arg) {
      return 1;
   }
}
