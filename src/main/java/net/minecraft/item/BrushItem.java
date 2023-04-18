package net.minecraft.item;

import java.util.function.Consumer;
import java.util.function.Predicate;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.BrushableBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BrushableBlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.particle.BlockStateParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.UseAction;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

public class BrushItem extends Item {
   public static final int field_43390 = 10;
   private static final int field_42683 = 200;

   public BrushItem(Item.Settings arg) {
      super(arg);
   }

   public ActionResult useOnBlock(ItemUsageContext context) {
      PlayerEntity lv = context.getPlayer();
      if (lv != null) {
         if (this.getHitResult(lv).getType() != HitResult.Type.BLOCK) {
            return ActionResult.FAIL;
         }

         lv.setCurrentHand(context.getHand());
      }

      return ActionResult.CONSUME;
   }

   private @NotNull HitResult getHitResult(LivingEntity user) {
      return ProjectileUtil.getCollision(user, Predicate.not(Entity::isSpectator), Math.sqrt(ServerPlayNetworkHandler.MAX_BREAK_SQUARED_DISTANCE) - 1.0);
   }

   public UseAction getUseAction(ItemStack stack) {
      return UseAction.BRUSH;
   }

   public int getMaxUseTime(ItemStack stack) {
      return 200;
   }

   public void usageTick(World world, LivingEntity user, ItemStack stack, int remainingUseTicks) {
      if (remainingUseTicks >= 0 && user instanceof PlayerEntity lv) {
         HitResult lv2 = this.getHitResult(user);
         if (lv2 instanceof BlockHitResult lv3) {
            if (lv2.getType() == HitResult.Type.BLOCK) {
               int j = this.getMaxUseTime(stack) - remainingUseTicks + 1;
               boolean bl = j % 10 == 5;
               if (bl) {
                  BlockPos lv4 = lv3.getBlockPos();
                  BlockState lv5 = world.getBlockState(lv4);
                  this.addDustParticles(world, lv3, lv5, user.getRotationVec(0.0F), user.getMainHandStack().equals(stack));
                  Block var14 = lv5.getBlock();
                  SoundEvent lv7;
                  if (var14 instanceof BrushableBlock) {
                     BrushableBlock lv6 = (BrushableBlock)var14;
                     lv7 = lv6.getBrushingSound();
                  } else {
                     lv7 = SoundEvents.ITEM_BRUSH_BRUSHING_GENERIC;
                  }

                  world.playSound(lv, lv4, lv7, SoundCategory.BLOCKS);
                  if (!world.isClient()) {
                     BlockEntity var17 = world.getBlockEntity(lv4);
                     if (var17 instanceof BrushableBlockEntity) {
                        BrushableBlockEntity lv8 = (BrushableBlockEntity)var17;
                        boolean bl2 = lv8.brush(world.getTime(), lv, lv3.getSide());
                        if (bl2) {
                           EquipmentSlot lv9 = stack.equals(lv.getEquippedStack(EquipmentSlot.OFFHAND)) ? EquipmentSlot.OFFHAND : EquipmentSlot.MAINHAND;
                           stack.damage(1, (LivingEntity)user, (Consumer)((userx) -> {
                              userx.sendEquipmentBreakStatus(lv9);
                           }));
                        }
                     }
                  }
               }

               return;
            }
         }

         user.stopUsingItem();
      } else {
         user.stopUsingItem();
      }
   }

   public void addDustParticles(World world, BlockHitResult hitResult, BlockState state, Vec3d userRotation, boolean mainHand) {
      double d = 3.0;
      int i = mainHand ? 1 : -1;
      int j = world.getRandom().nextBetweenExclusive(7, 12);
      BlockStateParticleEffect lv = new BlockStateParticleEffect(ParticleTypes.BLOCK, state);
      Direction lv2 = hitResult.getSide();
      DustParticlesOffset lv3 = BrushItem.DustParticlesOffset.fromSide(userRotation, lv2);
      Vec3d lv4 = hitResult.getPos();

      for(int k = 0; k < j; ++k) {
         world.addParticle(lv, lv4.x - (double)(lv2 == Direction.WEST ? 1.0E-6F : 0.0F), lv4.y, lv4.z - (double)(lv2 == Direction.NORTH ? 1.0E-6F : 0.0F), lv3.xd() * (double)i * 3.0 * world.getRandom().nextDouble(), 0.0, lv3.zd() * (double)i * 3.0 * world.getRandom().nextDouble());
      }

   }

   private static record DustParticlesOffset(double xd, double yd, double zd) {
      private static final double field_42685 = 1.0;
      private static final double field_42686 = 0.1;

      private DustParticlesOffset(double d, double e, double f) {
         this.xd = d;
         this.yd = e;
         this.zd = f;
      }

      public static DustParticlesOffset fromSide(Vec3d userRotation, Direction side) {
         double d = 0.0;
         DustParticlesOffset var10000;
         switch (side) {
            case DOWN:
            case UP:
               var10000 = new DustParticlesOffset(userRotation.getZ(), 0.0, -userRotation.getX());
               break;
            case NORTH:
               var10000 = new DustParticlesOffset(1.0, 0.0, -0.1);
               break;
            case SOUTH:
               var10000 = new DustParticlesOffset(-1.0, 0.0, 0.1);
               break;
            case WEST:
               var10000 = new DustParticlesOffset(-0.1, 0.0, -1.0);
               break;
            case EAST:
               var10000 = new DustParticlesOffset(0.1, 0.0, 1.0);
               break;
            default:
               throw new IncompatibleClassChangeError();
         }

         return var10000;
      }

      public double xd() {
         return this.xd;
      }

      public double yd() {
         return this.yd;
      }

      public double zd() {
         return this.zd;
      }
   }
}
