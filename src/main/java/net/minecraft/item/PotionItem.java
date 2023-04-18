package net.minecraft.item;

import java.util.Iterator;
import java.util.List;
import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.potion.PotionUtil;
import net.minecraft.potion.Potions;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stats;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.UseAction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;
import org.jetbrains.annotations.Nullable;

public class PotionItem extends Item {
   private static final int MAX_USE_TIME = 32;

   public PotionItem(Item.Settings arg) {
      super(arg);
   }

   public ItemStack getDefaultStack() {
      return PotionUtil.setPotion(super.getDefaultStack(), Potions.WATER);
   }

   public ItemStack finishUsing(ItemStack stack, World world, LivingEntity user) {
      PlayerEntity lv = user instanceof PlayerEntity ? (PlayerEntity)user : null;
      if (lv instanceof ServerPlayerEntity) {
         Criteria.CONSUME_ITEM.trigger((ServerPlayerEntity)lv, stack);
      }

      if (!world.isClient) {
         List list = PotionUtil.getPotionEffects(stack);
         Iterator var6 = list.iterator();

         while(var6.hasNext()) {
            StatusEffectInstance lv2 = (StatusEffectInstance)var6.next();
            if (lv2.getEffectType().isInstant()) {
               lv2.getEffectType().applyInstantEffect(lv, lv, user, lv2.getAmplifier(), 1.0);
            } else {
               user.addStatusEffect(new StatusEffectInstance(lv2));
            }
         }
      }

      if (lv != null) {
         lv.incrementStat(Stats.USED.getOrCreateStat(this));
         if (!lv.getAbilities().creativeMode) {
            stack.decrement(1);
         }
      }

      if (lv == null || !lv.getAbilities().creativeMode) {
         if (stack.isEmpty()) {
            return new ItemStack(Items.GLASS_BOTTLE);
         }

         if (lv != null) {
            lv.getInventory().insertStack(new ItemStack(Items.GLASS_BOTTLE));
         }
      }

      user.emitGameEvent(GameEvent.DRINK);
      return stack;
   }

   public ActionResult useOnBlock(ItemUsageContext context) {
      World lv = context.getWorld();
      BlockPos lv2 = context.getBlockPos();
      PlayerEntity lv3 = context.getPlayer();
      ItemStack lv4 = context.getStack();
      BlockState lv5 = lv.getBlockState(lv2);
      if (context.getSide() != Direction.DOWN && lv5.isIn(BlockTags.CONVERTABLE_TO_MUD) && PotionUtil.getPotion(lv4) == Potions.WATER) {
         lv.playSound((PlayerEntity)null, lv2, SoundEvents.ENTITY_GENERIC_SPLASH, SoundCategory.BLOCKS, 1.0F, 1.0F);
         lv3.setStackInHand(context.getHand(), ItemUsage.exchangeStack(lv4, lv3, new ItemStack(Items.GLASS_BOTTLE)));
         lv3.incrementStat(Stats.USED.getOrCreateStat(lv4.getItem()));
         if (!lv.isClient) {
            ServerWorld lv6 = (ServerWorld)lv;

            for(int i = 0; i < 5; ++i) {
               lv6.spawnParticles(ParticleTypes.SPLASH, (double)lv2.getX() + lv.random.nextDouble(), (double)(lv2.getY() + 1), (double)lv2.getZ() + lv.random.nextDouble(), 1, 0.0, 0.0, 0.0, 1.0);
            }
         }

         lv.playSound((PlayerEntity)null, lv2, SoundEvents.ITEM_BOTTLE_EMPTY, SoundCategory.BLOCKS, 1.0F, 1.0F);
         lv.emitGameEvent((Entity)null, GameEvent.FLUID_PLACE, lv2);
         lv.setBlockState(lv2, Blocks.MUD.getDefaultState());
         return ActionResult.success(lv.isClient);
      } else {
         return ActionResult.PASS;
      }
   }

   public int getMaxUseTime(ItemStack stack) {
      return 32;
   }

   public UseAction getUseAction(ItemStack stack) {
      return UseAction.DRINK;
   }

   public TypedActionResult use(World world, PlayerEntity user, Hand hand) {
      return ItemUsage.consumeHeldItem(world, user, hand);
   }

   public String getTranslationKey(ItemStack stack) {
      return PotionUtil.getPotion(stack).finishTranslationKey(this.getTranslationKey() + ".effect.");
   }

   public void appendTooltip(ItemStack stack, @Nullable World world, List tooltip, TooltipContext context) {
      PotionUtil.buildTooltip(stack, tooltip, 1.0F);
   }
}
