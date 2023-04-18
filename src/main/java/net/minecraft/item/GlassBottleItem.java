package net.minecraft.item;

import java.util.List;
import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.entity.AreaEffectCloudEntity;
import net.minecraft.entity.boss.dragon.EnderDragonEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.potion.PotionUtil;
import net.minecraft.potion.Potions;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stats;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;

public class GlassBottleItem extends Item {
   public GlassBottleItem(Item.Settings arg) {
      super(arg);
   }

   public TypedActionResult use(World world, PlayerEntity user, Hand hand) {
      List list = world.getEntitiesByClass(AreaEffectCloudEntity.class, user.getBoundingBox().expand(2.0), (entity) -> {
         return entity != null && entity.isAlive() && entity.getOwner() instanceof EnderDragonEntity;
      });
      ItemStack lv = user.getStackInHand(hand);
      if (!list.isEmpty()) {
         AreaEffectCloudEntity lv2 = (AreaEffectCloudEntity)list.get(0);
         lv2.setRadius(lv2.getRadius() - 0.5F);
         world.playSound((PlayerEntity)null, user.getX(), user.getY(), user.getZ(), SoundEvents.ITEM_BOTTLE_FILL_DRAGONBREATH, SoundCategory.NEUTRAL, 1.0F, 1.0F);
         world.emitGameEvent(user, GameEvent.FLUID_PICKUP, user.getPos());
         if (user instanceof ServerPlayerEntity) {
            ServerPlayerEntity lv3 = (ServerPlayerEntity)user;
            Criteria.PLAYER_INTERACTED_WITH_ENTITY.trigger(lv3, lv, lv2);
         }

         return TypedActionResult.success(this.fill(lv, user, new ItemStack(Items.DRAGON_BREATH)), world.isClient());
      } else {
         HitResult lv4 = raycast(world, user, RaycastContext.FluidHandling.SOURCE_ONLY);
         if (lv4.getType() == HitResult.Type.MISS) {
            return TypedActionResult.pass(lv);
         } else {
            if (lv4.getType() == HitResult.Type.BLOCK) {
               BlockPos lv5 = ((BlockHitResult)lv4).getBlockPos();
               if (!world.canPlayerModifyAt(user, lv5)) {
                  return TypedActionResult.pass(lv);
               }

               if (world.getFluidState(lv5).isIn(FluidTags.WATER)) {
                  world.playSound(user, user.getX(), user.getY(), user.getZ(), SoundEvents.ITEM_BOTTLE_FILL, SoundCategory.NEUTRAL, 1.0F, 1.0F);
                  world.emitGameEvent(user, GameEvent.FLUID_PICKUP, lv5);
                  return TypedActionResult.success(this.fill(lv, user, PotionUtil.setPotion(new ItemStack(Items.POTION), Potions.WATER)), world.isClient());
               }
            }

            return TypedActionResult.pass(lv);
         }
      }
   }

   protected ItemStack fill(ItemStack stack, PlayerEntity player, ItemStack outputStack) {
      player.incrementStat(Stats.USED.getOrCreateStat(this));
      return ItemUsage.exchangeStack(stack, player, outputStack);
   }
}
