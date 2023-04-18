package net.minecraft.block;

import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.block.entity.BeehiveBlockEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.TntEntity;
import net.minecraft.entity.boss.WitherEntity;
import net.minecraft.entity.mob.CreeperEntity;
import net.minecraft.entity.passive.BeeEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.WitherSkullEntity;
import net.minecraft.entity.vehicle.TntMinecartEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stats;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.state.property.IntProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.event.GameEvent;
import org.jetbrains.annotations.Nullable;

public class BeehiveBlock extends BlockWithEntity {
   public static final DirectionProperty FACING;
   public static final IntProperty HONEY_LEVEL;
   public static final int FULL_HONEY_LEVEL = 5;
   private static final int DROPPED_HONEYCOMB_COUNT = 3;

   public BeehiveBlock(AbstractBlock.Settings arg) {
      super(arg);
      this.setDefaultState((BlockState)((BlockState)((BlockState)this.stateManager.getDefaultState()).with(HONEY_LEVEL, 0)).with(FACING, Direction.NORTH));
   }

   public boolean hasComparatorOutput(BlockState state) {
      return true;
   }

   public int getComparatorOutput(BlockState state, World world, BlockPos pos) {
      return (Integer)state.get(HONEY_LEVEL);
   }

   public void afterBreak(World world, PlayerEntity player, BlockPos pos, BlockState state, @Nullable BlockEntity blockEntity, ItemStack tool) {
      super.afterBreak(world, player, pos, state, blockEntity, tool);
      if (!world.isClient && blockEntity instanceof BeehiveBlockEntity lv) {
         if (EnchantmentHelper.getLevel(Enchantments.SILK_TOUCH, tool) == 0) {
            lv.angerBees(player, state, BeehiveBlockEntity.BeeState.EMERGENCY);
            world.updateComparators(pos, this);
            this.angerNearbyBees(world, pos);
         }

         Criteria.BEE_NEST_DESTROYED.trigger((ServerPlayerEntity)player, state, tool, lv.getBeeCount());
      }

   }

   private void angerNearbyBees(World world, BlockPos pos) {
      List list = world.getNonSpectatingEntities(BeeEntity.class, (new Box(pos)).expand(8.0, 6.0, 8.0));
      if (!list.isEmpty()) {
         List list2 = world.getNonSpectatingEntities(PlayerEntity.class, (new Box(pos)).expand(8.0, 6.0, 8.0));
         int i = list2.size();
         Iterator var6 = list.iterator();

         while(var6.hasNext()) {
            BeeEntity lv = (BeeEntity)var6.next();
            if (lv.getTarget() == null) {
               lv.setTarget((LivingEntity)list2.get(world.random.nextInt(i)));
            }
         }
      }

   }

   public static void dropHoneycomb(World world, BlockPos pos) {
      dropStack(world, pos, new ItemStack(Items.HONEYCOMB, 3));
   }

   public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
      ItemStack lv = player.getStackInHand(hand);
      int i = (Integer)state.get(HONEY_LEVEL);
      boolean bl = false;
      if (i >= 5) {
         Item lv2 = lv.getItem();
         if (lv.isOf(Items.SHEARS)) {
            world.playSound(player, player.getX(), player.getY(), player.getZ(), SoundEvents.BLOCK_BEEHIVE_SHEAR, SoundCategory.BLOCKS, 1.0F, 1.0F);
            dropHoneycomb(world, pos);
            lv.damage(1, (LivingEntity)player, (Consumer)((playerx) -> {
               playerx.sendToolBreakStatus(hand);
            }));
            bl = true;
            world.emitGameEvent(player, GameEvent.SHEAR, pos);
         } else if (lv.isOf(Items.GLASS_BOTTLE)) {
            lv.decrement(1);
            world.playSound(player, player.getX(), player.getY(), player.getZ(), SoundEvents.ITEM_BOTTLE_FILL, SoundCategory.BLOCKS, 1.0F, 1.0F);
            if (lv.isEmpty()) {
               player.setStackInHand(hand, new ItemStack(Items.HONEY_BOTTLE));
            } else if (!player.getInventory().insertStack(new ItemStack(Items.HONEY_BOTTLE))) {
               player.dropItem(new ItemStack(Items.HONEY_BOTTLE), false);
            }

            bl = true;
            world.emitGameEvent(player, GameEvent.FLUID_PICKUP, pos);
         }

         if (!world.isClient() && bl) {
            player.incrementStat(Stats.USED.getOrCreateStat(lv2));
         }
      }

      if (bl) {
         if (!CampfireBlock.isLitCampfireInRange(world, pos)) {
            if (this.hasBees(world, pos)) {
               this.angerNearbyBees(world, pos);
            }

            this.takeHoney(world, state, pos, player, BeehiveBlockEntity.BeeState.EMERGENCY);
         } else {
            this.takeHoney(world, state, pos);
         }

         return ActionResult.success(world.isClient);
      } else {
         return super.onUse(state, world, pos, player, hand, hit);
      }
   }

   private boolean hasBees(World world, BlockPos pos) {
      BlockEntity lv = world.getBlockEntity(pos);
      if (lv instanceof BeehiveBlockEntity lv2) {
         return !lv2.hasNoBees();
      } else {
         return false;
      }
   }

   public void takeHoney(World world, BlockState state, BlockPos pos, @Nullable PlayerEntity player, BeehiveBlockEntity.BeeState beeState) {
      this.takeHoney(world, state, pos);
      BlockEntity lv = world.getBlockEntity(pos);
      if (lv instanceof BeehiveBlockEntity lv2) {
         lv2.angerBees(player, state, beeState);
      }

   }

   public void takeHoney(World world, BlockState state, BlockPos pos) {
      world.setBlockState(pos, (BlockState)state.with(HONEY_LEVEL, 0), Block.NOTIFY_ALL);
   }

   public void randomDisplayTick(BlockState state, World world, BlockPos pos, Random random) {
      if ((Integer)state.get(HONEY_LEVEL) >= 5) {
         for(int i = 0; i < random.nextInt(1) + 1; ++i) {
            this.spawnHoneyParticles(world, pos, state);
         }
      }

   }

   private void spawnHoneyParticles(World world, BlockPos pos, BlockState state) {
      if (state.getFluidState().isEmpty() && !(world.random.nextFloat() < 0.3F)) {
         VoxelShape lv = state.getCollisionShape(world, pos);
         double d = lv.getMax(Direction.Axis.Y);
         if (d >= 1.0 && !state.isIn(BlockTags.IMPERMEABLE)) {
            double e = lv.getMin(Direction.Axis.Y);
            if (e > 0.0) {
               this.addHoneyParticle(world, pos, lv, (double)pos.getY() + e - 0.05);
            } else {
               BlockPos lv2 = pos.down();
               BlockState lv3 = world.getBlockState(lv2);
               VoxelShape lv4 = lv3.getCollisionShape(world, lv2);
               double f = lv4.getMax(Direction.Axis.Y);
               if ((f < 1.0 || !lv3.isFullCube(world, lv2)) && lv3.getFluidState().isEmpty()) {
                  this.addHoneyParticle(world, pos, lv, (double)pos.getY() - 0.05);
               }
            }
         }

      }
   }

   private void addHoneyParticle(World world, BlockPos pos, VoxelShape shape, double height) {
      this.addHoneyParticle(world, (double)pos.getX() + shape.getMin(Direction.Axis.X), (double)pos.getX() + shape.getMax(Direction.Axis.X), (double)pos.getZ() + shape.getMin(Direction.Axis.Z), (double)pos.getZ() + shape.getMax(Direction.Axis.Z), height);
   }

   private void addHoneyParticle(World world, double minX, double maxX, double minZ, double maxZ, double height) {
      world.addParticle(ParticleTypes.DRIPPING_HONEY, MathHelper.lerp(world.random.nextDouble(), minX, maxX), height, MathHelper.lerp(world.random.nextDouble(), minZ, maxZ), 0.0, 0.0, 0.0);
   }

   public BlockState getPlacementState(ItemPlacementContext ctx) {
      return (BlockState)this.getDefaultState().with(FACING, ctx.getHorizontalPlayerFacing().getOpposite());
   }

   protected void appendProperties(StateManager.Builder builder) {
      builder.add(HONEY_LEVEL, FACING);
   }

   public BlockRenderType getRenderType(BlockState state) {
      return BlockRenderType.MODEL;
   }

   @Nullable
   public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
      return new BeehiveBlockEntity(pos, state);
   }

   @Nullable
   public BlockEntityTicker getTicker(World world, BlockState state, BlockEntityType type) {
      return world.isClient ? null : checkType(type, BlockEntityType.BEEHIVE, BeehiveBlockEntity::serverTick);
   }

   public void onBreak(World world, BlockPos pos, BlockState state, PlayerEntity player) {
      if (!world.isClient && player.isCreative() && world.getGameRules().getBoolean(GameRules.DO_TILE_DROPS)) {
         BlockEntity lv = world.getBlockEntity(pos);
         if (lv instanceof BeehiveBlockEntity) {
            BeehiveBlockEntity lv2 = (BeehiveBlockEntity)lv;
            ItemStack lv3 = new ItemStack(this);
            int i = (Integer)state.get(HONEY_LEVEL);
            boolean bl = !lv2.hasNoBees();
            if (bl || i > 0) {
               NbtCompound lv4;
               if (bl) {
                  lv4 = new NbtCompound();
                  lv4.put("Bees", lv2.getBees());
                  BlockItem.setBlockEntityNbt(lv3, BlockEntityType.BEEHIVE, lv4);
               }

               lv4 = new NbtCompound();
               lv4.putInt("honey_level", i);
               lv3.setSubNbt("BlockStateTag", lv4);
               ItemEntity lv5 = new ItemEntity(world, (double)pos.getX(), (double)pos.getY(), (double)pos.getZ(), lv3);
               lv5.setToDefaultPickupDelay();
               world.spawnEntity(lv5);
            }
         }
      }

      super.onBreak(world, pos, state, player);
   }

   public List getDroppedStacks(BlockState state, LootContext.Builder builder) {
      Entity lv = (Entity)builder.getNullable(LootContextParameters.THIS_ENTITY);
      if (lv instanceof TntEntity || lv instanceof CreeperEntity || lv instanceof WitherSkullEntity || lv instanceof WitherEntity || lv instanceof TntMinecartEntity) {
         BlockEntity lv2 = (BlockEntity)builder.getNullable(LootContextParameters.BLOCK_ENTITY);
         if (lv2 instanceof BeehiveBlockEntity) {
            BeehiveBlockEntity lv3 = (BeehiveBlockEntity)lv2;
            lv3.angerBees((PlayerEntity)null, state, BeehiveBlockEntity.BeeState.EMERGENCY);
         }
      }

      return super.getDroppedStacks(state, builder);
   }

   public BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState neighborState, WorldAccess world, BlockPos pos, BlockPos neighborPos) {
      if (world.getBlockState(neighborPos).getBlock() instanceof FireBlock) {
         BlockEntity lv = world.getBlockEntity(pos);
         if (lv instanceof BeehiveBlockEntity) {
            BeehiveBlockEntity lv2 = (BeehiveBlockEntity)lv;
            lv2.angerBees((PlayerEntity)null, state, BeehiveBlockEntity.BeeState.EMERGENCY);
         }
      }

      return super.getStateForNeighborUpdate(state, direction, neighborState, world, pos, neighborPos);
   }

   static {
      FACING = HorizontalFacingBlock.FACING;
      HONEY_LEVEL = Properties.HONEY_LEVEL;
   }
}
