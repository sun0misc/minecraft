package net.minecraft.block.dispenser;

import com.mojang.logging.LogUtils;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import net.minecraft.block.AbstractFireBlock;
import net.minecraft.block.BeehiveBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.CampfireBlock;
import net.minecraft.block.CandleBlock;
import net.minecraft.block.CandleCakeBlock;
import net.minecraft.block.CarvedPumpkinBlock;
import net.minecraft.block.DispenserBlock;
import net.minecraft.block.FluidDrainable;
import net.minecraft.block.RespawnAnchorBlock;
import net.minecraft.block.ShulkerBoxBlock;
import net.minecraft.block.SkullBlock;
import net.minecraft.block.TntBlock;
import net.minecraft.block.WitherSkullBlock;
import net.minecraft.block.entity.BeehiveBlockEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.DispenserBlockEntity;
import net.minecraft.block.entity.SkullBlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.Saddleable;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.TntEntity;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.entity.passive.AbstractDonkeyEntity;
import net.minecraft.entity.passive.AbstractHorseEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ArrowEntity;
import net.minecraft.entity.projectile.FireworkRocketEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.entity.projectile.SmallFireballEntity;
import net.minecraft.entity.projectile.SpectralArrowEntity;
import net.minecraft.entity.projectile.thrown.EggEntity;
import net.minecraft.entity.projectile.thrown.ExperienceBottleEntity;
import net.minecraft.entity.projectile.thrown.PotionEntity;
import net.minecraft.entity.projectile.thrown.SnowballEntity;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.BoneMealItem;
import net.minecraft.item.FluidModificationItem;
import net.minecraft.item.HoneycombItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.SpawnEggItem;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.potion.PotionUtil;
import net.minecraft.potion.Potions;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.property.Properties;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Util;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPointer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Position;
import net.minecraft.util.math.RotationPropertyHelper;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldEvents;
import net.minecraft.world.event.GameEvent;
import org.slf4j.Logger;

public interface DispenserBehavior {
   Logger LOGGER = LogUtils.getLogger();
   DispenserBehavior NOOP = (pointer, stack) -> {
      return stack;
   };

   ItemStack dispense(BlockPointer pointer, ItemStack stack);

   static void registerDefaults() {
      DispenserBlock.registerBehavior(Items.ARROW, new ProjectileDispenserBehavior() {
         protected ProjectileEntity createProjectile(World world, Position position, ItemStack stack) {
            ArrowEntity lv = new ArrowEntity(world, position.getX(), position.getY(), position.getZ());
            lv.pickupType = PersistentProjectileEntity.PickupPermission.ALLOWED;
            return lv;
         }
      });
      DispenserBlock.registerBehavior(Items.TIPPED_ARROW, new ProjectileDispenserBehavior() {
         protected ProjectileEntity createProjectile(World world, Position position, ItemStack stack) {
            ArrowEntity lv = new ArrowEntity(world, position.getX(), position.getY(), position.getZ());
            lv.initFromStack(stack);
            lv.pickupType = PersistentProjectileEntity.PickupPermission.ALLOWED;
            return lv;
         }
      });
      DispenserBlock.registerBehavior(Items.SPECTRAL_ARROW, new ProjectileDispenserBehavior() {
         protected ProjectileEntity createProjectile(World world, Position position, ItemStack stack) {
            PersistentProjectileEntity lv = new SpectralArrowEntity(world, position.getX(), position.getY(), position.getZ());
            lv.pickupType = PersistentProjectileEntity.PickupPermission.ALLOWED;
            return lv;
         }
      });
      DispenserBlock.registerBehavior(Items.EGG, new ProjectileDispenserBehavior() {
         protected ProjectileEntity createProjectile(World world, Position position, ItemStack stack) {
            return (ProjectileEntity)Util.make(new EggEntity(world, position.getX(), position.getY(), position.getZ()), (entity) -> {
               entity.setItem(stack);
            });
         }
      });
      DispenserBlock.registerBehavior(Items.SNOWBALL, new ProjectileDispenserBehavior() {
         protected ProjectileEntity createProjectile(World world, Position position, ItemStack stack) {
            return (ProjectileEntity)Util.make(new SnowballEntity(world, position.getX(), position.getY(), position.getZ()), (entity) -> {
               entity.setItem(stack);
            });
         }
      });
      DispenserBlock.registerBehavior(Items.EXPERIENCE_BOTTLE, new ProjectileDispenserBehavior() {
         protected ProjectileEntity createProjectile(World world, Position position, ItemStack stack) {
            return (ProjectileEntity)Util.make(new ExperienceBottleEntity(world, position.getX(), position.getY(), position.getZ()), (entity) -> {
               entity.setItem(stack);
            });
         }

         protected float getVariation() {
            return super.getVariation() * 0.5F;
         }

         protected float getForce() {
            return super.getForce() * 1.25F;
         }
      });
      DispenserBlock.registerBehavior(Items.SPLASH_POTION, new DispenserBehavior() {
         public ItemStack dispense(BlockPointer arg, ItemStack arg2) {
            return (new ProjectileDispenserBehavior() {
               protected ProjectileEntity createProjectile(World world, Position position, ItemStack stack) {
                  return (ProjectileEntity)Util.make(new PotionEntity(world, position.getX(), position.getY(), position.getZ()), (entity) -> {
                     entity.setItem(stack);
                  });
               }

               protected float getVariation() {
                  return super.getVariation() * 0.5F;
               }

               protected float getForce() {
                  return super.getForce() * 1.25F;
               }
            }).dispense(arg, arg2);
         }
      });
      DispenserBlock.registerBehavior(Items.LINGERING_POTION, new DispenserBehavior() {
         public ItemStack dispense(BlockPointer arg, ItemStack arg2) {
            return (new ProjectileDispenserBehavior() {
               protected ProjectileEntity createProjectile(World world, Position position, ItemStack stack) {
                  return (ProjectileEntity)Util.make(new PotionEntity(world, position.getX(), position.getY(), position.getZ()), (entity) -> {
                     entity.setItem(stack);
                  });
               }

               protected float getVariation() {
                  return super.getVariation() * 0.5F;
               }

               protected float getForce() {
                  return super.getForce() * 1.25F;
               }
            }).dispense(arg, arg2);
         }
      });
      ItemDispenserBehavior lv = new ItemDispenserBehavior() {
         public ItemStack dispenseSilently(BlockPointer pointer, ItemStack stack) {
            Direction lv = (Direction)pointer.getBlockState().get(DispenserBlock.FACING);
            EntityType lv2 = ((SpawnEggItem)stack.getItem()).getEntityType(stack.getNbt());

            try {
               lv2.spawnFromItemStack(pointer.getWorld(), stack, (PlayerEntity)null, pointer.getPos().offset(lv), SpawnReason.DISPENSER, lv != Direction.UP, false);
            } catch (Exception var6) {
               LOGGER.error("Error while dispensing spawn egg from dispenser at {}", pointer.getPos(), var6);
               return ItemStack.EMPTY;
            }

            stack.decrement(1);
            pointer.getWorld().emitGameEvent((Entity)null, GameEvent.ENTITY_PLACE, pointer.getPos());
            return stack;
         }
      };
      Iterator var1 = SpawnEggItem.getAll().iterator();

      while(var1.hasNext()) {
         SpawnEggItem lv2 = (SpawnEggItem)var1.next();
         DispenserBlock.registerBehavior(lv2, lv);
      }

      DispenserBlock.registerBehavior(Items.ARMOR_STAND, new ItemDispenserBehavior() {
         public ItemStack dispenseSilently(BlockPointer pointer, ItemStack stack) {
            Direction lv = (Direction)pointer.getBlockState().get(DispenserBlock.FACING);
            BlockPos lv2 = pointer.getPos().offset(lv);
            ServerWorld lv3 = pointer.getWorld();
            Consumer consumer = EntityType.copier((arg2) -> {
               arg2.setYaw(lv.asRotation());
            }, lv3, stack, (PlayerEntity)null);
            ArmorStandEntity lv4 = (ArmorStandEntity)EntityType.ARMOR_STAND.spawn(lv3, stack.getNbt(), consumer, lv2, SpawnReason.DISPENSER, false, false);
            if (lv4 != null) {
               stack.decrement(1);
            }

            return stack;
         }
      });
      DispenserBlock.registerBehavior(Items.SADDLE, new FallibleItemDispenserBehavior() {
         public ItemStack dispenseSilently(BlockPointer pointer, ItemStack stack) {
            BlockPos lv = pointer.getPos().offset((Direction)pointer.getBlockState().get(DispenserBlock.FACING));
            List list = pointer.getWorld().getEntitiesByClass(LivingEntity.class, new Box(lv), (entity) -> {
               if (!(entity instanceof Saddleable lv)) {
                  return false;
               } else {
                  return !lv.isSaddled() && lv.canBeSaddled();
               }
            });
            if (!list.isEmpty()) {
               ((Saddleable)list.get(0)).saddle(SoundCategory.BLOCKS);
               stack.decrement(1);
               this.setSuccess(true);
               return stack;
            } else {
               return super.dispenseSilently(pointer, stack);
            }
         }
      });
      ItemDispenserBehavior lv3 = new FallibleItemDispenserBehavior() {
         protected ItemStack dispenseSilently(BlockPointer pointer, ItemStack stack) {
            BlockPos lv = pointer.getPos().offset((Direction)pointer.getBlockState().get(DispenserBlock.FACING));
            List list = pointer.getWorld().getEntitiesByClass(AbstractHorseEntity.class, new Box(lv), (entity) -> {
               return entity.isAlive() && entity.hasArmorSlot();
            });
            Iterator var5 = list.iterator();

            AbstractHorseEntity lv2;
            do {
               if (!var5.hasNext()) {
                  return super.dispenseSilently(pointer, stack);
               }

               lv2 = (AbstractHorseEntity)var5.next();
            } while(!lv2.isHorseArmor(stack) || lv2.hasArmorInSlot() || !lv2.isTame());

            lv2.getStackReference(401).set(stack.split(1));
            this.setSuccess(true);
            return stack;
         }
      };
      DispenserBlock.registerBehavior(Items.LEATHER_HORSE_ARMOR, lv3);
      DispenserBlock.registerBehavior(Items.IRON_HORSE_ARMOR, lv3);
      DispenserBlock.registerBehavior(Items.GOLDEN_HORSE_ARMOR, lv3);
      DispenserBlock.registerBehavior(Items.DIAMOND_HORSE_ARMOR, lv3);
      DispenserBlock.registerBehavior(Items.WHITE_CARPET, lv3);
      DispenserBlock.registerBehavior(Items.ORANGE_CARPET, lv3);
      DispenserBlock.registerBehavior(Items.CYAN_CARPET, lv3);
      DispenserBlock.registerBehavior(Items.BLUE_CARPET, lv3);
      DispenserBlock.registerBehavior(Items.BROWN_CARPET, lv3);
      DispenserBlock.registerBehavior(Items.BLACK_CARPET, lv3);
      DispenserBlock.registerBehavior(Items.GRAY_CARPET, lv3);
      DispenserBlock.registerBehavior(Items.GREEN_CARPET, lv3);
      DispenserBlock.registerBehavior(Items.LIGHT_BLUE_CARPET, lv3);
      DispenserBlock.registerBehavior(Items.LIGHT_GRAY_CARPET, lv3);
      DispenserBlock.registerBehavior(Items.LIME_CARPET, lv3);
      DispenserBlock.registerBehavior(Items.MAGENTA_CARPET, lv3);
      DispenserBlock.registerBehavior(Items.PINK_CARPET, lv3);
      DispenserBlock.registerBehavior(Items.PURPLE_CARPET, lv3);
      DispenserBlock.registerBehavior(Items.RED_CARPET, lv3);
      DispenserBlock.registerBehavior(Items.YELLOW_CARPET, lv3);
      DispenserBlock.registerBehavior(Items.CHEST, new FallibleItemDispenserBehavior() {
         public ItemStack dispenseSilently(BlockPointer pointer, ItemStack stack) {
            BlockPos lv = pointer.getPos().offset((Direction)pointer.getBlockState().get(DispenserBlock.FACING));
            List list = pointer.getWorld().getEntitiesByClass(AbstractDonkeyEntity.class, new Box(lv), (entity) -> {
               return entity.isAlive() && !entity.hasChest();
            });
            Iterator var5 = list.iterator();

            AbstractDonkeyEntity lv2;
            do {
               if (!var5.hasNext()) {
                  return super.dispenseSilently(pointer, stack);
               }

               lv2 = (AbstractDonkeyEntity)var5.next();
            } while(!lv2.isTame() || !lv2.getStackReference(499).set(stack));

            stack.decrement(1);
            this.setSuccess(true);
            return stack;
         }
      });
      DispenserBlock.registerBehavior(Items.FIREWORK_ROCKET, new ItemDispenserBehavior() {
         public ItemStack dispenseSilently(BlockPointer pointer, ItemStack stack) {
            Direction lv = (Direction)pointer.getBlockState().get(DispenserBlock.FACING);
            FireworkRocketEntity lv2 = new FireworkRocketEntity(pointer.getWorld(), stack, pointer.getX(), pointer.getY(), pointer.getX(), true);
            DispenserBehavior.setEntityPosition(pointer, lv2, lv);
            lv2.setVelocity((double)lv.getOffsetX(), (double)lv.getOffsetY(), (double)lv.getOffsetZ(), 0.5F, 1.0F);
            pointer.getWorld().spawnEntity(lv2);
            stack.decrement(1);
            return stack;
         }

         protected void playSound(BlockPointer pointer) {
            pointer.getWorld().syncWorldEvent(WorldEvents.FIREWORK_ROCKET_SHOOTS, pointer.getPos(), 0);
         }
      });
      DispenserBlock.registerBehavior(Items.FIRE_CHARGE, new ItemDispenserBehavior() {
         public ItemStack dispenseSilently(BlockPointer pointer, ItemStack stack) {
            Direction lv = (Direction)pointer.getBlockState().get(DispenserBlock.FACING);
            Position lv2 = DispenserBlock.getOutputLocation(pointer);
            double d = lv2.getX() + (double)((float)lv.getOffsetX() * 0.3F);
            double e = lv2.getY() + (double)((float)lv.getOffsetY() * 0.3F);
            double f = lv2.getZ() + (double)((float)lv.getOffsetZ() * 0.3F);
            World lv3 = pointer.getWorld();
            Random lv4 = lv3.random;
            double g = lv4.nextTriangular((double)lv.getOffsetX(), 0.11485000000000001);
            double h = lv4.nextTriangular((double)lv.getOffsetY(), 0.11485000000000001);
            double i = lv4.nextTriangular((double)lv.getOffsetZ(), 0.11485000000000001);
            SmallFireballEntity lv5 = new SmallFireballEntity(lv3, d, e, f, g, h, i);
            lv3.spawnEntity((Entity)Util.make(lv5, (entity) -> {
               entity.setItem(stack);
            }));
            stack.decrement(1);
            return stack;
         }

         protected void playSound(BlockPointer pointer) {
            pointer.getWorld().syncWorldEvent(WorldEvents.BLAZE_SHOOTS, pointer.getPos(), 0);
         }
      });
      DispenserBlock.registerBehavior(Items.OAK_BOAT, new BoatDispenserBehavior(BoatEntity.Type.OAK));
      DispenserBlock.registerBehavior(Items.SPRUCE_BOAT, new BoatDispenserBehavior(BoatEntity.Type.SPRUCE));
      DispenserBlock.registerBehavior(Items.BIRCH_BOAT, new BoatDispenserBehavior(BoatEntity.Type.BIRCH));
      DispenserBlock.registerBehavior(Items.JUNGLE_BOAT, new BoatDispenserBehavior(BoatEntity.Type.JUNGLE));
      DispenserBlock.registerBehavior(Items.DARK_OAK_BOAT, new BoatDispenserBehavior(BoatEntity.Type.DARK_OAK));
      DispenserBlock.registerBehavior(Items.ACACIA_BOAT, new BoatDispenserBehavior(BoatEntity.Type.ACACIA));
      DispenserBlock.registerBehavior(Items.CHERRY_BOAT, new BoatDispenserBehavior(BoatEntity.Type.CHERRY));
      DispenserBlock.registerBehavior(Items.MANGROVE_BOAT, new BoatDispenserBehavior(BoatEntity.Type.MANGROVE));
      DispenserBlock.registerBehavior(Items.BAMBOO_RAFT, new BoatDispenserBehavior(BoatEntity.Type.BAMBOO));
      DispenserBlock.registerBehavior(Items.OAK_CHEST_BOAT, new BoatDispenserBehavior(BoatEntity.Type.OAK, true));
      DispenserBlock.registerBehavior(Items.SPRUCE_CHEST_BOAT, new BoatDispenserBehavior(BoatEntity.Type.SPRUCE, true));
      DispenserBlock.registerBehavior(Items.BIRCH_CHEST_BOAT, new BoatDispenserBehavior(BoatEntity.Type.BIRCH, true));
      DispenserBlock.registerBehavior(Items.JUNGLE_CHEST_BOAT, new BoatDispenserBehavior(BoatEntity.Type.JUNGLE, true));
      DispenserBlock.registerBehavior(Items.DARK_OAK_CHEST_BOAT, new BoatDispenserBehavior(BoatEntity.Type.DARK_OAK, true));
      DispenserBlock.registerBehavior(Items.ACACIA_CHEST_BOAT, new BoatDispenserBehavior(BoatEntity.Type.ACACIA, true));
      DispenserBlock.registerBehavior(Items.CHERRY_CHEST_BOAT, new BoatDispenserBehavior(BoatEntity.Type.CHERRY, true));
      DispenserBlock.registerBehavior(Items.MANGROVE_CHEST_BOAT, new BoatDispenserBehavior(BoatEntity.Type.MANGROVE, true));
      DispenserBlock.registerBehavior(Items.BAMBOO_CHEST_RAFT, new BoatDispenserBehavior(BoatEntity.Type.BAMBOO, true));
      DispenserBehavior lv4 = new ItemDispenserBehavior() {
         private final ItemDispenserBehavior fallbackBehavior = new ItemDispenserBehavior();

         public ItemStack dispenseSilently(BlockPointer pointer, ItemStack stack) {
            FluidModificationItem lv = (FluidModificationItem)stack.getItem();
            BlockPos lv2 = pointer.getPos().offset((Direction)pointer.getBlockState().get(DispenserBlock.FACING));
            World lv3 = pointer.getWorld();
            if (lv.placeFluid((PlayerEntity)null, lv3, lv2, (BlockHitResult)null)) {
               lv.onEmptied((PlayerEntity)null, lv3, stack, lv2);
               return new ItemStack(Items.BUCKET);
            } else {
               return this.fallbackBehavior.dispense(pointer, stack);
            }
         }
      };
      DispenserBlock.registerBehavior(Items.LAVA_BUCKET, lv4);
      DispenserBlock.registerBehavior(Items.WATER_BUCKET, lv4);
      DispenserBlock.registerBehavior(Items.POWDER_SNOW_BUCKET, lv4);
      DispenserBlock.registerBehavior(Items.SALMON_BUCKET, lv4);
      DispenserBlock.registerBehavior(Items.COD_BUCKET, lv4);
      DispenserBlock.registerBehavior(Items.PUFFERFISH_BUCKET, lv4);
      DispenserBlock.registerBehavior(Items.TROPICAL_FISH_BUCKET, lv4);
      DispenserBlock.registerBehavior(Items.AXOLOTL_BUCKET, lv4);
      DispenserBlock.registerBehavior(Items.TADPOLE_BUCKET, lv4);
      DispenserBlock.registerBehavior(Items.BUCKET, new ItemDispenserBehavior() {
         private final ItemDispenserBehavior fallbackBehavior = new ItemDispenserBehavior();

         public ItemStack dispenseSilently(BlockPointer pointer, ItemStack stack) {
            WorldAccess lv = pointer.getWorld();
            BlockPos lv2 = pointer.getPos().offset((Direction)pointer.getBlockState().get(DispenserBlock.FACING));
            BlockState lv3 = lv.getBlockState(lv2);
            Block lv4 = lv3.getBlock();
            if (lv4 instanceof FluidDrainable) {
               ItemStack lv5 = ((FluidDrainable)lv4).tryDrainFluid(lv, lv2, lv3);
               if (lv5.isEmpty()) {
                  return super.dispenseSilently(pointer, stack);
               } else {
                  lv.emitGameEvent((Entity)null, (GameEvent)GameEvent.FLUID_PICKUP, (BlockPos)lv2);
                  Item lv6 = lv5.getItem();
                  stack.decrement(1);
                  if (stack.isEmpty()) {
                     return new ItemStack(lv6);
                  } else {
                     if (((DispenserBlockEntity)pointer.getBlockEntity()).addToFirstFreeSlot(new ItemStack(lv6)) < 0) {
                        this.fallbackBehavior.dispense(pointer, new ItemStack(lv6));
                     }

                     return stack;
                  }
               }
            } else {
               return super.dispenseSilently(pointer, stack);
            }
         }
      });
      DispenserBlock.registerBehavior(Items.FLINT_AND_STEEL, new FallibleItemDispenserBehavior() {
         protected ItemStack dispenseSilently(BlockPointer pointer, ItemStack stack) {
            World lv = pointer.getWorld();
            this.setSuccess(true);
            Direction lv2 = (Direction)pointer.getBlockState().get(DispenserBlock.FACING);
            BlockPos lv3 = pointer.getPos().offset(lv2);
            BlockState lv4 = lv.getBlockState(lv3);
            if (AbstractFireBlock.canPlaceAt(lv, lv3, lv2)) {
               lv.setBlockState(lv3, AbstractFireBlock.getState(lv, lv3));
               lv.emitGameEvent((Entity)null, GameEvent.BLOCK_PLACE, lv3);
            } else if (!CampfireBlock.canBeLit(lv4) && !CandleBlock.canBeLit(lv4) && !CandleCakeBlock.canBeLit(lv4)) {
               if (lv4.getBlock() instanceof TntBlock) {
                  TntBlock.primeTnt(lv, lv3);
                  lv.removeBlock(lv3, false);
               } else {
                  this.setSuccess(false);
               }
            } else {
               lv.setBlockState(lv3, (BlockState)lv4.with(Properties.LIT, true));
               lv.emitGameEvent((Entity)null, GameEvent.BLOCK_CHANGE, lv3);
            }

            if (this.isSuccess() && stack.damage(1, (Random)lv.random, (ServerPlayerEntity)null)) {
               stack.setCount(0);
            }

            return stack;
         }
      });
      DispenserBlock.registerBehavior(Items.BONE_MEAL, new FallibleItemDispenserBehavior() {
         protected ItemStack dispenseSilently(BlockPointer pointer, ItemStack stack) {
            this.setSuccess(true);
            World lv = pointer.getWorld();
            BlockPos lv2 = pointer.getPos().offset((Direction)pointer.getBlockState().get(DispenserBlock.FACING));
            if (!BoneMealItem.useOnFertilizable(stack, lv, lv2) && !BoneMealItem.useOnGround(stack, lv, lv2, (Direction)null)) {
               this.setSuccess(false);
            } else if (!lv.isClient) {
               lv.syncWorldEvent(WorldEvents.BONE_MEAL_USED, lv2, 0);
            }

            return stack;
         }
      });
      DispenserBlock.registerBehavior(Blocks.TNT, new ItemDispenserBehavior() {
         protected ItemStack dispenseSilently(BlockPointer pointer, ItemStack stack) {
            World lv = pointer.getWorld();
            BlockPos lv2 = pointer.getPos().offset((Direction)pointer.getBlockState().get(DispenserBlock.FACING));
            TntEntity lv3 = new TntEntity(lv, (double)lv2.getX() + 0.5, (double)lv2.getY(), (double)lv2.getZ() + 0.5, (LivingEntity)null);
            lv.spawnEntity(lv3);
            lv.playSound((PlayerEntity)null, lv3.getX(), lv3.getY(), lv3.getZ(), SoundEvents.ENTITY_TNT_PRIMED, SoundCategory.BLOCKS, 1.0F, 1.0F);
            lv.emitGameEvent((Entity)null, GameEvent.ENTITY_PLACE, lv2);
            stack.decrement(1);
            return stack;
         }
      });
      DispenserBehavior lv5 = new FallibleItemDispenserBehavior() {
         protected ItemStack dispenseSilently(BlockPointer pointer, ItemStack stack) {
            this.setSuccess(ArmorItem.dispenseArmor(pointer, stack));
            return stack;
         }
      };
      DispenserBlock.registerBehavior(Items.CREEPER_HEAD, lv5);
      DispenserBlock.registerBehavior(Items.ZOMBIE_HEAD, lv5);
      DispenserBlock.registerBehavior(Items.DRAGON_HEAD, lv5);
      DispenserBlock.registerBehavior(Items.SKELETON_SKULL, lv5);
      DispenserBlock.registerBehavior(Items.PIGLIN_HEAD, lv5);
      DispenserBlock.registerBehavior(Items.PLAYER_HEAD, lv5);
      DispenserBlock.registerBehavior(Items.WITHER_SKELETON_SKULL, new FallibleItemDispenserBehavior() {
         protected ItemStack dispenseSilently(BlockPointer pointer, ItemStack stack) {
            World lv = pointer.getWorld();
            Direction lv2 = (Direction)pointer.getBlockState().get(DispenserBlock.FACING);
            BlockPos lv3 = pointer.getPos().offset(lv2);
            if (lv.isAir(lv3) && WitherSkullBlock.canDispense(lv, lv3, stack)) {
               lv.setBlockState(lv3, (BlockState)Blocks.WITHER_SKELETON_SKULL.getDefaultState().with(SkullBlock.ROTATION, RotationPropertyHelper.fromDirection(lv2)), Block.NOTIFY_ALL);
               lv.emitGameEvent((Entity)null, GameEvent.BLOCK_PLACE, lv3);
               BlockEntity lv4 = lv.getBlockEntity(lv3);
               if (lv4 instanceof SkullBlockEntity) {
                  WitherSkullBlock.onPlaced(lv, lv3, (SkullBlockEntity)lv4);
               }

               stack.decrement(1);
               this.setSuccess(true);
            } else {
               this.setSuccess(ArmorItem.dispenseArmor(pointer, stack));
            }

            return stack;
         }
      });
      DispenserBlock.registerBehavior(Blocks.CARVED_PUMPKIN, new FallibleItemDispenserBehavior() {
         protected ItemStack dispenseSilently(BlockPointer pointer, ItemStack stack) {
            World lv = pointer.getWorld();
            BlockPos lv2 = pointer.getPos().offset((Direction)pointer.getBlockState().get(DispenserBlock.FACING));
            CarvedPumpkinBlock lv3 = (CarvedPumpkinBlock)Blocks.CARVED_PUMPKIN;
            if (lv.isAir(lv2) && lv3.canDispense(lv, lv2)) {
               if (!lv.isClient) {
                  lv.setBlockState(lv2, lv3.getDefaultState(), Block.NOTIFY_ALL);
                  lv.emitGameEvent((Entity)null, GameEvent.BLOCK_PLACE, lv2);
               }

               stack.decrement(1);
               this.setSuccess(true);
            } else {
               this.setSuccess(ArmorItem.dispenseArmor(pointer, stack));
            }

            return stack;
         }
      });
      DispenserBlock.registerBehavior(Blocks.SHULKER_BOX.asItem(), new BlockPlacementDispenserBehavior());
      DyeColor[] var4 = DyeColor.values();
      int var5 = var4.length;

      for(int var6 = 0; var6 < var5; ++var6) {
         DyeColor lv6 = var4[var6];
         DispenserBlock.registerBehavior(ShulkerBoxBlock.get(lv6).asItem(), new BlockPlacementDispenserBehavior());
      }

      DispenserBlock.registerBehavior(Items.GLASS_BOTTLE.asItem(), new FallibleItemDispenserBehavior() {
         private final ItemDispenserBehavior fallbackBehavior = new ItemDispenserBehavior();

         private ItemStack tryPutFilledBottle(BlockPointer pointer, ItemStack emptyBottleStack, ItemStack filledBottleStack) {
            emptyBottleStack.decrement(1);
            if (emptyBottleStack.isEmpty()) {
               pointer.getWorld().emitGameEvent((Entity)null, GameEvent.FLUID_PICKUP, pointer.getPos());
               return filledBottleStack.copy();
            } else {
               if (((DispenserBlockEntity)pointer.getBlockEntity()).addToFirstFreeSlot(filledBottleStack.copy()) < 0) {
                  this.fallbackBehavior.dispense(pointer, filledBottleStack.copy());
               }

               return emptyBottleStack;
            }
         }

         public ItemStack dispenseSilently(BlockPointer pointer, ItemStack stack) {
            this.setSuccess(false);
            ServerWorld lv = pointer.getWorld();
            BlockPos lv2 = pointer.getPos().offset((Direction)pointer.getBlockState().get(DispenserBlock.FACING));
            BlockState lv3 = lv.getBlockState(lv2);
            if (lv3.isIn(BlockTags.BEEHIVES, (state) -> {
               return state.contains(BeehiveBlock.HONEY_LEVEL) && state.getBlock() instanceof BeehiveBlock;
            }) && (Integer)lv3.get(BeehiveBlock.HONEY_LEVEL) >= 5) {
               ((BeehiveBlock)lv3.getBlock()).takeHoney(lv, lv3, lv2, (PlayerEntity)null, BeehiveBlockEntity.BeeState.BEE_RELEASED);
               this.setSuccess(true);
               return this.tryPutFilledBottle(pointer, stack, new ItemStack(Items.HONEY_BOTTLE));
            } else if (lv.getFluidState(lv2).isIn(FluidTags.WATER)) {
               this.setSuccess(true);
               return this.tryPutFilledBottle(pointer, stack, PotionUtil.setPotion(new ItemStack(Items.POTION), Potions.WATER));
            } else {
               return super.dispenseSilently(pointer, stack);
            }
         }
      });
      DispenserBlock.registerBehavior(Items.GLOWSTONE, new FallibleItemDispenserBehavior() {
         public ItemStack dispenseSilently(BlockPointer pointer, ItemStack stack) {
            Direction lv = (Direction)pointer.getBlockState().get(DispenserBlock.FACING);
            BlockPos lv2 = pointer.getPos().offset(lv);
            World lv3 = pointer.getWorld();
            BlockState lv4 = lv3.getBlockState(lv2);
            this.setSuccess(true);
            if (lv4.isOf(Blocks.RESPAWN_ANCHOR)) {
               if ((Integer)lv4.get(RespawnAnchorBlock.CHARGES) != 4) {
                  RespawnAnchorBlock.charge((Entity)null, lv3, lv2, lv4);
                  stack.decrement(1);
               } else {
                  this.setSuccess(false);
               }

               return stack;
            } else {
               return super.dispenseSilently(pointer, stack);
            }
         }
      });
      DispenserBlock.registerBehavior(Items.SHEARS.asItem(), new ShearsDispenserBehavior());
      DispenserBlock.registerBehavior(Items.HONEYCOMB, new FallibleItemDispenserBehavior() {
         public ItemStack dispenseSilently(BlockPointer pointer, ItemStack stack) {
            BlockPos lv = pointer.getPos().offset((Direction)pointer.getBlockState().get(DispenserBlock.FACING));
            World lv2 = pointer.getWorld();
            BlockState lv3 = lv2.getBlockState(lv);
            Optional optional = HoneycombItem.getWaxedState(lv3);
            if (optional.isPresent()) {
               lv2.setBlockState(lv, (BlockState)optional.get());
               lv2.syncWorldEvent(WorldEvents.BLOCK_WAXED, lv, 0);
               stack.decrement(1);
               this.setSuccess(true);
               return stack;
            } else {
               return super.dispenseSilently(pointer, stack);
            }
         }
      });
      DispenserBlock.registerBehavior(Items.POTION, new ItemDispenserBehavior() {
         private final ItemDispenserBehavior fallback = new ItemDispenserBehavior();

         public ItemStack dispenseSilently(BlockPointer pointer, ItemStack stack) {
            if (PotionUtil.getPotion(stack) != Potions.WATER) {
               return this.fallback.dispense(pointer, stack);
            } else {
               ServerWorld lv = pointer.getWorld();
               BlockPos lv2 = pointer.getPos();
               BlockPos lv3 = pointer.getPos().offset((Direction)pointer.getBlockState().get(DispenserBlock.FACING));
               if (!lv.getBlockState(lv3).isIn(BlockTags.CONVERTABLE_TO_MUD)) {
                  return this.fallback.dispense(pointer, stack);
               } else {
                  if (!lv.isClient) {
                     for(int i = 0; i < 5; ++i) {
                        lv.spawnParticles(ParticleTypes.SPLASH, (double)lv2.getX() + lv.random.nextDouble(), (double)(lv2.getY() + 1), (double)lv2.getZ() + lv.random.nextDouble(), 1, 0.0, 0.0, 0.0, 1.0);
                     }
                  }

                  lv.playSound((PlayerEntity)null, lv2, SoundEvents.ITEM_BOTTLE_EMPTY, SoundCategory.BLOCKS, 1.0F, 1.0F);
                  lv.emitGameEvent((Entity)null, GameEvent.FLUID_PLACE, lv2);
                  lv.setBlockState(lv3, Blocks.MUD.getDefaultState());
                  return new ItemStack(Items.GLASS_BOTTLE);
               }
            }
         }
      });
   }

   static void setEntityPosition(BlockPointer pointer, Entity entity, Direction direction) {
      entity.setPosition(pointer.getX() + (double)direction.getOffsetX() * (0.5000099999997474 - (double)entity.getWidth() / 2.0), pointer.getY() + (double)direction.getOffsetY() * (0.5000099999997474 - (double)entity.getHeight() / 2.0) - (double)entity.getHeight() / 2.0, pointer.getZ() + (double)direction.getOffsetZ() * (0.5000099999997474 - (double)entity.getWidth() / 2.0));
   }
}
