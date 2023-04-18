package net.minecraft.item;

import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.EndPortalFrameBlock;
import net.minecraft.block.pattern.BlockPattern;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EyeOfEnderEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.registry.tag.StructureTags;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stats;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;
import net.minecraft.world.WorldEvents;
import net.minecraft.world.event.GameEvent;

public class EnderEyeItem extends Item {
   public EnderEyeItem(Item.Settings arg) {
      super(arg);
   }

   public ActionResult useOnBlock(ItemUsageContext context) {
      World lv = context.getWorld();
      BlockPos lv2 = context.getBlockPos();
      BlockState lv3 = lv.getBlockState(lv2);
      if (lv3.isOf(Blocks.END_PORTAL_FRAME) && !(Boolean)lv3.get(EndPortalFrameBlock.EYE)) {
         if (lv.isClient) {
            return ActionResult.SUCCESS;
         } else {
            BlockState lv4 = (BlockState)lv3.with(EndPortalFrameBlock.EYE, true);
            Block.pushEntitiesUpBeforeBlockChange(lv3, lv4, lv, lv2);
            lv.setBlockState(lv2, lv4, Block.NOTIFY_LISTENERS);
            lv.updateComparators(lv2, Blocks.END_PORTAL_FRAME);
            context.getStack().decrement(1);
            lv.syncWorldEvent(WorldEvents.END_PORTAL_FRAME_FILLED, lv2, 0);
            BlockPattern.Result lv5 = EndPortalFrameBlock.getCompletedFramePattern().searchAround(lv, lv2);
            if (lv5 != null) {
               BlockPos lv6 = lv5.getFrontTopLeft().add(-3, 0, -3);

               for(int i = 0; i < 3; ++i) {
                  for(int j = 0; j < 3; ++j) {
                     lv.setBlockState(lv6.add(i, 0, j), Blocks.END_PORTAL.getDefaultState(), Block.NOTIFY_LISTENERS);
                  }
               }

               lv.syncGlobalEvent(WorldEvents.END_PORTAL_OPENED, lv6.add(1, 0, 1), 0);
            }

            return ActionResult.CONSUME;
         }
      } else {
         return ActionResult.PASS;
      }
   }

   public TypedActionResult use(World world, PlayerEntity user, Hand hand) {
      ItemStack lv = user.getStackInHand(hand);
      HitResult lv2 = raycast(world, user, RaycastContext.FluidHandling.NONE);
      if (lv2.getType() == HitResult.Type.BLOCK && world.getBlockState(((BlockHitResult)lv2).getBlockPos()).isOf(Blocks.END_PORTAL_FRAME)) {
         return TypedActionResult.pass(lv);
      } else {
         user.setCurrentHand(hand);
         if (world instanceof ServerWorld) {
            ServerWorld lv3 = (ServerWorld)world;
            BlockPos lv4 = lv3.locateStructure(StructureTags.EYE_OF_ENDER_LOCATED, user.getBlockPos(), 100, false);
            if (lv4 != null) {
               EyeOfEnderEntity lv5 = new EyeOfEnderEntity(world, user.getX(), user.getBodyY(0.5), user.getZ());
               lv5.setItem(lv);
               lv5.initTargetPos(lv4);
               world.emitGameEvent(GameEvent.PROJECTILE_SHOOT, lv5.getPos(), GameEvent.Emitter.of((Entity)user));
               world.spawnEntity(lv5);
               if (user instanceof ServerPlayerEntity) {
                  Criteria.USED_ENDER_EYE.trigger((ServerPlayerEntity)user, lv4);
               }

               world.playSound((PlayerEntity)null, user.getX(), user.getY(), user.getZ(), SoundEvents.ENTITY_ENDER_EYE_LAUNCH, SoundCategory.NEUTRAL, 0.5F, 0.4F / (world.getRandom().nextFloat() * 0.4F + 0.8F));
               world.syncWorldEvent((PlayerEntity)null, WorldEvents.EYE_OF_ENDER_LAUNCHES, user.getBlockPos(), 0);
               if (!user.getAbilities().creativeMode) {
                  lv.decrement(1);
               }

               user.incrementStat(Stats.USED.getOrCreateStat(this));
               user.swingHand(hand, true);
               return TypedActionResult.success(lv);
            }
         }

         return TypedActionResult.consume(lv);
      }
   }
}
