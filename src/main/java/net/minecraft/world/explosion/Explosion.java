package net.minecraft.world.explosion;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectListIterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import net.minecraft.block.AbstractFireBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.enchantment.ProtectionEnchantment;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.TntEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Util;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;
import org.jetbrains.annotations.Nullable;

public class Explosion {
   private static final ExplosionBehavior DEFAULT_BEHAVIOR = new ExplosionBehavior();
   private static final int field_30960 = 16;
   private final boolean createFire;
   private final DestructionType destructionType;
   private final Random random;
   private final World world;
   private final double x;
   private final double y;
   private final double z;
   @Nullable
   private final Entity entity;
   private final float power;
   private final DamageSource damageSource;
   private final ExplosionBehavior behavior;
   private final ObjectArrayList affectedBlocks;
   private final Map affectedPlayers;

   public Explosion(World world, @Nullable Entity entity, double x, double y, double z, float power, List affectedBlocks) {
      this(world, entity, x, y, z, power, false, Explosion.DestructionType.DESTROY_WITH_DECAY, affectedBlocks);
   }

   public Explosion(World world, @Nullable Entity entity, double x, double y, double z, float power, boolean createFire, DestructionType destructionType, List affectedBlocks) {
      this(world, entity, x, y, z, power, createFire, destructionType);
      this.affectedBlocks.addAll(affectedBlocks);
   }

   public Explosion(World world, @Nullable Entity entity, double x, double y, double z, float power, boolean createFire, DestructionType destructionType) {
      this(world, entity, (DamageSource)null, (ExplosionBehavior)null, x, y, z, power, createFire, destructionType);
   }

   public Explosion(World world, @Nullable Entity entity, @Nullable DamageSource damageSource, @Nullable ExplosionBehavior behavior, double x, double y, double z, float power, boolean createFire, DestructionType destructionType) {
      this.random = Random.create();
      this.affectedBlocks = new ObjectArrayList();
      this.affectedPlayers = Maps.newHashMap();
      this.world = world;
      this.entity = entity;
      this.power = power;
      this.x = x;
      this.y = y;
      this.z = z;
      this.createFire = createFire;
      this.destructionType = destructionType;
      this.damageSource = damageSource == null ? world.getDamageSources().explosion(this) : damageSource;
      this.behavior = behavior == null ? this.chooseBehavior(entity) : behavior;
   }

   private ExplosionBehavior chooseBehavior(@Nullable Entity entity) {
      return (ExplosionBehavior)(entity == null ? DEFAULT_BEHAVIOR : new EntityExplosionBehavior(entity));
   }

   public static float getExposure(Vec3d source, Entity entity) {
      Box lv = entity.getBoundingBox();
      double d = 1.0 / ((lv.maxX - lv.minX) * 2.0 + 1.0);
      double e = 1.0 / ((lv.maxY - lv.minY) * 2.0 + 1.0);
      double f = 1.0 / ((lv.maxZ - lv.minZ) * 2.0 + 1.0);
      double g = (1.0 - Math.floor(1.0 / d) * d) / 2.0;
      double h = (1.0 - Math.floor(1.0 / f) * f) / 2.0;
      if (!(d < 0.0) && !(e < 0.0) && !(f < 0.0)) {
         int i = 0;
         int j = 0;

         for(double k = 0.0; k <= 1.0; k += d) {
            for(double l = 0.0; l <= 1.0; l += e) {
               for(double m = 0.0; m <= 1.0; m += f) {
                  double n = MathHelper.lerp(k, lv.minX, lv.maxX);
                  double o = MathHelper.lerp(l, lv.minY, lv.maxY);
                  double p = MathHelper.lerp(m, lv.minZ, lv.maxZ);
                  Vec3d lv2 = new Vec3d(n + g, o, p + h);
                  if (entity.world.raycast(new RaycastContext(lv2, source, RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, entity)).getType() == HitResult.Type.MISS) {
                     ++i;
                  }

                  ++j;
               }
            }
         }

         return (float)i / (float)j;
      } else {
         return 0.0F;
      }
   }

   public void collectBlocksAndDamageEntities() {
      this.world.emitGameEvent(this.entity, GameEvent.EXPLODE, new Vec3d(this.x, this.y, this.z));
      Set set = Sets.newHashSet();
      int i = true;

      int k;
      int l;
      for(int j = 0; j < 16; ++j) {
         for(k = 0; k < 16; ++k) {
            for(l = 0; l < 16; ++l) {
               if (j == 0 || j == 15 || k == 0 || k == 15 || l == 0 || l == 15) {
                  double d = (double)((float)j / 15.0F * 2.0F - 1.0F);
                  double e = (double)((float)k / 15.0F * 2.0F - 1.0F);
                  double f = (double)((float)l / 15.0F * 2.0F - 1.0F);
                  double g = Math.sqrt(d * d + e * e + f * f);
                  d /= g;
                  e /= g;
                  f /= g;
                  float h = this.power * (0.7F + this.world.random.nextFloat() * 0.6F);
                  double m = this.x;
                  double n = this.y;
                  double o = this.z;

                  for(float p = 0.3F; h > 0.0F; h -= 0.22500001F) {
                     BlockPos lv = BlockPos.ofFloored(m, n, o);
                     BlockState lv2 = this.world.getBlockState(lv);
                     FluidState lv3 = this.world.getFluidState(lv);
                     if (!this.world.isInBuildLimit(lv)) {
                        break;
                     }

                     Optional optional = this.behavior.getBlastResistance(this, this.world, lv, lv2, lv3);
                     if (optional.isPresent()) {
                        h -= ((Float)optional.get() + 0.3F) * 0.3F;
                     }

                     if (h > 0.0F && this.behavior.canDestroyBlock(this, this.world, lv, lv2, h)) {
                        set.add(lv);
                     }

                     m += d * 0.30000001192092896;
                     n += e * 0.30000001192092896;
                     o += f * 0.30000001192092896;
                  }
               }
            }
         }
      }

      this.affectedBlocks.addAll(set);
      float q = this.power * 2.0F;
      k = MathHelper.floor(this.x - (double)q - 1.0);
      l = MathHelper.floor(this.x + (double)q + 1.0);
      int r = MathHelper.floor(this.y - (double)q - 1.0);
      int s = MathHelper.floor(this.y + (double)q + 1.0);
      int t = MathHelper.floor(this.z - (double)q - 1.0);
      int u = MathHelper.floor(this.z + (double)q + 1.0);
      List list = this.world.getOtherEntities(this.entity, new Box((double)k, (double)r, (double)t, (double)l, (double)s, (double)u));
      Vec3d lv4 = new Vec3d(this.x, this.y, this.z);

      for(int v = 0; v < list.size(); ++v) {
         Entity lv5 = (Entity)list.get(v);
         if (!lv5.isImmuneToExplosion()) {
            double w = Math.sqrt(lv5.squaredDistanceTo(lv4)) / (double)q;
            if (w <= 1.0) {
               double x = lv5.getX() - this.x;
               double y = (lv5 instanceof TntEntity ? lv5.getY() : lv5.getEyeY()) - this.y;
               double z = lv5.getZ() - this.z;
               double aa = Math.sqrt(x * x + y * y + z * z);
               if (aa != 0.0) {
                  x /= aa;
                  y /= aa;
                  z /= aa;
                  double ab = (double)getExposure(lv4, lv5);
                  double ac = (1.0 - w) * ab;
                  lv5.damage(this.getDamageSource(), (float)((int)((ac * ac + ac) / 2.0 * 7.0 * (double)q + 1.0)));
                  double ad;
                  if (lv5 instanceof LivingEntity) {
                     LivingEntity lv6 = (LivingEntity)lv5;
                     ad = ProtectionEnchantment.transformExplosionKnockback(lv6, ac);
                  } else {
                     ad = ac;
                  }

                  x *= ad;
                  y *= ad;
                  z *= ad;
                  Vec3d lv7 = new Vec3d(x, y, z);
                  lv5.setVelocity(lv5.getVelocity().add(lv7));
                  if (lv5 instanceof PlayerEntity) {
                     PlayerEntity lv8 = (PlayerEntity)lv5;
                     if (!lv8.isSpectator() && (!lv8.isCreative() || !lv8.getAbilities().flying)) {
                        this.affectedPlayers.put(lv8, lv7);
                     }
                  }
               }
            }
         }
      }

   }

   public void affectWorld(boolean particles) {
      if (this.world.isClient) {
         this.world.playSound(this.x, this.y, this.z, SoundEvents.ENTITY_GENERIC_EXPLODE, SoundCategory.BLOCKS, 4.0F, (1.0F + (this.world.random.nextFloat() - this.world.random.nextFloat()) * 0.2F) * 0.7F, false);
      }

      boolean bl2 = this.shouldDestroy();
      if (particles) {
         if (!(this.power < 2.0F) && bl2) {
            this.world.addParticle(ParticleTypes.EXPLOSION_EMITTER, this.x, this.y, this.z, 1.0, 0.0, 0.0);
         } else {
            this.world.addParticle(ParticleTypes.EXPLOSION, this.x, this.y, this.z, 1.0, 0.0, 0.0);
         }
      }

      if (bl2) {
         ObjectArrayList objectArrayList = new ObjectArrayList();
         boolean bl3 = this.getCausingEntity() instanceof PlayerEntity;
         Util.shuffle(this.affectedBlocks, this.world.random);
         ObjectListIterator var5 = this.affectedBlocks.iterator();

         while(var5.hasNext()) {
            BlockPos lv = (BlockPos)var5.next();
            BlockState lv2 = this.world.getBlockState(lv);
            Block lv3 = lv2.getBlock();
            if (!lv2.isAir()) {
               BlockPos lv4 = lv.toImmutable();
               this.world.getProfiler().push("explosion_blocks");
               if (lv3.shouldDropItemsOnExplosion(this)) {
                  World var11 = this.world;
                  if (var11 instanceof ServerWorld) {
                     ServerWorld lv5 = (ServerWorld)var11;
                     BlockEntity lv6 = lv2.hasBlockEntity() ? this.world.getBlockEntity(lv) : null;
                     LootContext.Builder lv7 = (new LootContext.Builder(lv5)).random(this.world.random).parameter(LootContextParameters.ORIGIN, Vec3d.ofCenter(lv)).parameter(LootContextParameters.TOOL, ItemStack.EMPTY).optionalParameter(LootContextParameters.BLOCK_ENTITY, lv6).optionalParameter(LootContextParameters.THIS_ENTITY, this.entity);
                     if (this.destructionType == Explosion.DestructionType.DESTROY_WITH_DECAY) {
                        lv7.parameter(LootContextParameters.EXPLOSION_RADIUS, this.power);
                     }

                     lv2.onStacksDropped(lv5, lv, ItemStack.EMPTY, bl3);
                     lv2.getDroppedStacks(lv7).forEach((stack) -> {
                        tryMergeStack(objectArrayList, stack, lv4);
                     });
                  }
               }

               this.world.setBlockState(lv, Blocks.AIR.getDefaultState(), Block.NOTIFY_ALL);
               lv3.onDestroyedByExplosion(this.world, lv, this);
               this.world.getProfiler().pop();
            }
         }

         var5 = objectArrayList.iterator();

         while(var5.hasNext()) {
            Pair pair = (Pair)var5.next();
            Block.dropStack(this.world, (BlockPos)pair.getSecond(), (ItemStack)pair.getFirst());
         }
      }

      if (this.createFire) {
         ObjectListIterator var13 = this.affectedBlocks.iterator();

         while(var13.hasNext()) {
            BlockPos lv8 = (BlockPos)var13.next();
            if (this.random.nextInt(3) == 0 && this.world.getBlockState(lv8).isAir() && this.world.getBlockState(lv8.down()).isOpaqueFullCube(this.world, lv8.down())) {
               this.world.setBlockState(lv8, AbstractFireBlock.getState(this.world, lv8));
            }
         }
      }

   }

   public boolean shouldDestroy() {
      return this.destructionType != Explosion.DestructionType.KEEP;
   }

   private static void tryMergeStack(ObjectArrayList stacks, ItemStack stack, BlockPos pos) {
      int i = stacks.size();

      for(int j = 0; j < i; ++j) {
         Pair pair = (Pair)stacks.get(j);
         ItemStack lv = (ItemStack)pair.getFirst();
         if (ItemEntity.canMerge(lv, stack)) {
            ItemStack lv2 = ItemEntity.merge(lv, stack, 16);
            stacks.set(j, Pair.of(lv2, (BlockPos)pair.getSecond()));
            if (stack.isEmpty()) {
               return;
            }
         }
      }

      stacks.add(Pair.of(stack, pos));
   }

   public DamageSource getDamageSource() {
      return this.damageSource;
   }

   public Map getAffectedPlayers() {
      return this.affectedPlayers;
   }

   @Nullable
   public LivingEntity getCausingEntity() {
      if (this.entity == null) {
         return null;
      } else {
         Entity lv4 = this.entity;
         if (lv4 instanceof TntEntity) {
            TntEntity lv = (TntEntity)lv4;
            return lv.getOwner();
         } else {
            lv4 = this.entity;
            if (lv4 instanceof LivingEntity) {
               LivingEntity lv2 = (LivingEntity)lv4;
               return lv2;
            } else {
               lv4 = this.entity;
               if (lv4 instanceof ProjectileEntity) {
                  ProjectileEntity lv3 = (ProjectileEntity)lv4;
                  lv4 = lv3.getOwner();
                  if (lv4 instanceof LivingEntity) {
                     LivingEntity lv5 = (LivingEntity)lv4;
                     return lv5;
                  }
               }

               return null;
            }
         }
      }
   }

   @Nullable
   public Entity getEntity() {
      return this.entity;
   }

   public void clearAffectedBlocks() {
      this.affectedBlocks.clear();
   }

   public List getAffectedBlocks() {
      return this.affectedBlocks;
   }

   public static enum DestructionType {
      KEEP,
      DESTROY,
      DESTROY_WITH_DECAY;

      // $FF: synthetic method
      private static DestructionType[] method_36693() {
         return new DestructionType[]{KEEP, DESTROY, DESTROY_WITH_DECAY};
      }
   }
}
