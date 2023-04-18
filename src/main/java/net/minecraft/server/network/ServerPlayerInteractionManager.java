package net.minecraft.server.network;

import com.mojang.logging.LogUtils;
import java.util.Objects;
import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.OperatorBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.network.packet.s2c.play.BlockUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerListS2CPacket;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameMode;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

public class ServerPlayerInteractionManager {
   private static final Logger LOGGER = LogUtils.getLogger();
   protected ServerWorld world;
   protected final ServerPlayerEntity player;
   private GameMode gameMode;
   @Nullable
   private GameMode previousGameMode;
   private boolean mining;
   private int startMiningTime;
   private BlockPos miningPos;
   private int tickCounter;
   private boolean failedToMine;
   private BlockPos failedMiningPos;
   private int failedStartMiningTime;
   private int blockBreakingProgress;

   public ServerPlayerInteractionManager(ServerPlayerEntity player) {
      this.gameMode = GameMode.DEFAULT;
      this.miningPos = BlockPos.ORIGIN;
      this.failedMiningPos = BlockPos.ORIGIN;
      this.blockBreakingProgress = -1;
      this.player = player;
      this.world = player.getWorld();
   }

   public boolean changeGameMode(GameMode gameMode) {
      if (gameMode == this.gameMode) {
         return false;
      } else {
         this.setGameMode(gameMode, this.previousGameMode);
         this.player.sendAbilitiesUpdate();
         this.player.server.getPlayerManager().sendToAll(new PlayerListS2CPacket(PlayerListS2CPacket.Action.UPDATE_GAME_MODE, this.player));
         this.world.updateSleepingPlayers();
         return true;
      }
   }

   protected void setGameMode(GameMode gameMode, @Nullable GameMode previousGameMode) {
      this.previousGameMode = previousGameMode;
      this.gameMode = gameMode;
      gameMode.setAbilities(this.player.getAbilities());
   }

   public GameMode getGameMode() {
      return this.gameMode;
   }

   @Nullable
   public GameMode getPreviousGameMode() {
      return this.previousGameMode;
   }

   public boolean isSurvivalLike() {
      return this.gameMode.isSurvivalLike();
   }

   public boolean isCreative() {
      return this.gameMode.isCreative();
   }

   public void update() {
      ++this.tickCounter;
      BlockState lv;
      if (this.failedToMine) {
         lv = this.world.getBlockState(this.failedMiningPos);
         if (lv.isAir()) {
            this.failedToMine = false;
         } else {
            float f = this.continueMining(lv, this.failedMiningPos, this.failedStartMiningTime);
            if (f >= 1.0F) {
               this.failedToMine = false;
               this.tryBreakBlock(this.failedMiningPos);
            }
         }
      } else if (this.mining) {
         lv = this.world.getBlockState(this.miningPos);
         if (lv.isAir()) {
            this.world.setBlockBreakingInfo(this.player.getId(), this.miningPos, -1);
            this.blockBreakingProgress = -1;
            this.mining = false;
         } else {
            this.continueMining(lv, this.miningPos, this.startMiningTime);
         }
      }

   }

   private float continueMining(BlockState state, BlockPos pos, int failedStartMiningTime) {
      int j = this.tickCounter - failedStartMiningTime;
      float f = state.calcBlockBreakingDelta(this.player, this.player.world, pos) * (float)(j + 1);
      int k = (int)(f * 10.0F);
      if (k != this.blockBreakingProgress) {
         this.world.setBlockBreakingInfo(this.player.getId(), pos, k);
         this.blockBreakingProgress = k;
      }

      return f;
   }

   private void method_41250(BlockPos pos, boolean success, int sequence, String reason) {
   }

   public void processBlockBreakingAction(BlockPos pos, PlayerActionC2SPacket.Action action, Direction direction, int worldHeight, int sequence) {
      if (this.player.getEyePos().squaredDistanceTo(Vec3d.ofCenter(pos)) > ServerPlayNetworkHandler.MAX_BREAK_SQUARED_DISTANCE) {
         this.method_41250(pos, false, sequence, "too far");
      } else if (pos.getY() >= worldHeight) {
         this.player.networkHandler.sendPacket(new BlockUpdateS2CPacket(pos, this.world.getBlockState(pos)));
         this.method_41250(pos, false, sequence, "too high");
      } else {
         BlockState lv;
         if (action == PlayerActionC2SPacket.Action.START_DESTROY_BLOCK) {
            if (!this.world.canPlayerModifyAt(this.player, pos)) {
               this.player.networkHandler.sendPacket(new BlockUpdateS2CPacket(pos, this.world.getBlockState(pos)));
               this.method_41250(pos, false, sequence, "may not interact");
               return;
            }

            if (this.isCreative()) {
               this.finishMining(pos, sequence, "creative destroy");
               return;
            }

            if (this.player.isBlockBreakingRestricted(this.world, pos, this.gameMode)) {
               this.player.networkHandler.sendPacket(new BlockUpdateS2CPacket(pos, this.world.getBlockState(pos)));
               this.method_41250(pos, false, sequence, "block action restricted");
               return;
            }

            this.startMiningTime = this.tickCounter;
            float f = 1.0F;
            lv = this.world.getBlockState(pos);
            if (!lv.isAir()) {
               lv.onBlockBreakStart(this.world, pos, this.player);
               f = lv.calcBlockBreakingDelta(this.player, this.player.world, pos);
            }

            if (!lv.isAir() && f >= 1.0F) {
               this.finishMining(pos, sequence, "insta mine");
            } else {
               if (this.mining) {
                  this.player.networkHandler.sendPacket(new BlockUpdateS2CPacket(this.miningPos, this.world.getBlockState(this.miningPos)));
                  this.method_41250(pos, false, sequence, "abort destroying since another started (client insta mine, server disagreed)");
               }

               this.mining = true;
               this.miningPos = pos.toImmutable();
               int k = (int)(f * 10.0F);
               this.world.setBlockBreakingInfo(this.player.getId(), pos, k);
               this.method_41250(pos, true, sequence, "actual start of destroying");
               this.blockBreakingProgress = k;
            }
         } else if (action == PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK) {
            if (pos.equals(this.miningPos)) {
               int l = this.tickCounter - this.startMiningTime;
               lv = this.world.getBlockState(pos);
               if (!lv.isAir()) {
                  float g = lv.calcBlockBreakingDelta(this.player, this.player.world, pos) * (float)(l + 1);
                  if (g >= 0.7F) {
                     this.mining = false;
                     this.world.setBlockBreakingInfo(this.player.getId(), pos, -1);
                     this.finishMining(pos, sequence, "destroyed");
                     return;
                  }

                  if (!this.failedToMine) {
                     this.mining = false;
                     this.failedToMine = true;
                     this.failedMiningPos = pos;
                     this.failedStartMiningTime = this.startMiningTime;
                  }
               }
            }

            this.method_41250(pos, true, sequence, "stopped destroying");
         } else if (action == PlayerActionC2SPacket.Action.ABORT_DESTROY_BLOCK) {
            this.mining = false;
            if (!Objects.equals(this.miningPos, pos)) {
               LOGGER.warn("Mismatch in destroy block pos: {} {}", this.miningPos, pos);
               this.world.setBlockBreakingInfo(this.player.getId(), this.miningPos, -1);
               this.method_41250(pos, true, sequence, "aborted mismatched destroying");
            }

            this.world.setBlockBreakingInfo(this.player.getId(), pos, -1);
            this.method_41250(pos, true, sequence, "aborted destroying");
         }

      }
   }

   public void finishMining(BlockPos pos, int sequence, String reason) {
      if (this.tryBreakBlock(pos)) {
         this.method_41250(pos, true, sequence, reason);
      } else {
         this.player.networkHandler.sendPacket(new BlockUpdateS2CPacket(pos, this.world.getBlockState(pos)));
         this.method_41250(pos, false, sequence, reason);
      }

   }

   public boolean tryBreakBlock(BlockPos pos) {
      BlockState lv = this.world.getBlockState(pos);
      if (!this.player.getMainHandStack().getItem().canMine(lv, this.world, pos, this.player)) {
         return false;
      } else {
         BlockEntity lv2 = this.world.getBlockEntity(pos);
         Block lv3 = lv.getBlock();
         if (lv3 instanceof OperatorBlock && !this.player.isCreativeLevelTwoOp()) {
            this.world.updateListeners(pos, lv, lv, Block.NOTIFY_ALL);
            return false;
         } else if (this.player.isBlockBreakingRestricted(this.world, pos, this.gameMode)) {
            return false;
         } else {
            lv3.onBreak(this.world, pos, lv, this.player);
            boolean bl = this.world.removeBlock(pos, false);
            if (bl) {
               lv3.onBroken(this.world, pos, lv);
            }

            if (this.isCreative()) {
               return true;
            } else {
               ItemStack lv4 = this.player.getMainHandStack();
               ItemStack lv5 = lv4.copy();
               boolean bl2 = this.player.canHarvest(lv);
               lv4.postMine(this.world, lv, pos, this.player);
               if (bl && bl2) {
                  lv3.afterBreak(this.world, this.player, pos, lv, lv2, lv5);
               }

               return true;
            }
         }
      }
   }

   public ActionResult interactItem(ServerPlayerEntity player, World world, ItemStack stack, Hand hand) {
      if (this.gameMode == GameMode.SPECTATOR) {
         return ActionResult.PASS;
      } else if (player.getItemCooldownManager().isCoolingDown(stack.getItem())) {
         return ActionResult.PASS;
      } else {
         int i = stack.getCount();
         int j = stack.getDamage();
         TypedActionResult lv = stack.use(world, player, hand);
         ItemStack lv2 = (ItemStack)lv.getValue();
         if (lv2 == stack && lv2.getCount() == i && lv2.getMaxUseTime() <= 0 && lv2.getDamage() == j) {
            return lv.getResult();
         } else if (lv.getResult() == ActionResult.FAIL && lv2.getMaxUseTime() > 0 && !player.isUsingItem()) {
            return lv.getResult();
         } else {
            if (stack != lv2) {
               player.setStackInHand(hand, lv2);
            }

            if (this.isCreative() && lv2 != ItemStack.EMPTY) {
               lv2.setCount(i);
               if (lv2.isDamageable() && lv2.getDamage() != j) {
                  lv2.setDamage(j);
               }
            }

            if (lv2.isEmpty()) {
               player.setStackInHand(hand, ItemStack.EMPTY);
            }

            if (!player.isUsingItem()) {
               player.playerScreenHandler.syncState();
            }

            return lv.getResult();
         }
      }
   }

   public ActionResult interactBlock(ServerPlayerEntity player, World world, ItemStack stack, Hand hand, BlockHitResult hitResult) {
      BlockPos lv = hitResult.getBlockPos();
      BlockState lv2 = world.getBlockState(lv);
      if (!lv2.getBlock().isEnabled(world.getEnabledFeatures())) {
         return ActionResult.FAIL;
      } else if (this.gameMode == GameMode.SPECTATOR) {
         NamedScreenHandlerFactory lv3 = lv2.createScreenHandlerFactory(world, lv);
         if (lv3 != null) {
            player.openHandledScreen(lv3);
            return ActionResult.SUCCESS;
         } else {
            return ActionResult.PASS;
         }
      } else {
         boolean bl = !player.getMainHandStack().isEmpty() || !player.getOffHandStack().isEmpty();
         boolean bl2 = player.shouldCancelInteraction() && bl;
         ItemStack lv4 = stack.copy();
         if (!bl2) {
            ActionResult lv5 = lv2.onUse(world, player, hand, hitResult);
            if (lv5.isAccepted()) {
               Criteria.ITEM_USED_ON_BLOCK.trigger(player, lv, lv4);
               return lv5;
            }
         }

         if (!stack.isEmpty() && !player.getItemCooldownManager().isCoolingDown(stack.getItem())) {
            ItemUsageContext lv6 = new ItemUsageContext(player, hand, hitResult);
            ActionResult lv7;
            if (this.isCreative()) {
               int i = stack.getCount();
               lv7 = stack.useOnBlock(lv6);
               stack.setCount(i);
            } else {
               lv7 = stack.useOnBlock(lv6);
            }

            if (lv7.isAccepted()) {
               Criteria.ITEM_USED_ON_BLOCK.trigger(player, lv, lv4);
            }

            return lv7;
         } else {
            return ActionResult.PASS;
         }
      }
   }

   public void setWorld(ServerWorld world) {
      this.world = world;
   }
}
