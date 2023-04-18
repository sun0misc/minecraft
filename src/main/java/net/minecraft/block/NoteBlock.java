package net.minecraft.block;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.SkullBlockEntity;
import net.minecraft.block.enums.Instrument;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.stat.Stats;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.IntProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.event.GameEvent;
import org.jetbrains.annotations.Nullable;

public class NoteBlock extends Block {
   public static final EnumProperty INSTRUMENT;
   public static final BooleanProperty POWERED;
   public static final IntProperty NOTE;
   public static final int field_41678 = 3;

   public NoteBlock(AbstractBlock.Settings arg) {
      super(arg);
      this.setDefaultState((BlockState)((BlockState)((BlockState)((BlockState)this.stateManager.getDefaultState()).with(INSTRUMENT, Instrument.HARP)).with(NOTE, 0)).with(POWERED, false));
   }

   private BlockState getStateWithInstrument(WorldAccess world, BlockPos pos, BlockState state) {
      BlockState lv = world.getBlockState(pos.up());
      return (BlockState)state.with(INSTRUMENT, (Instrument)Instrument.fromAboveState(lv).orElseGet(() -> {
         return Instrument.fromBelowState(world.getBlockState(pos.down()));
      }));
   }

   public BlockState getPlacementState(ItemPlacementContext ctx) {
      return this.getStateWithInstrument(ctx.getWorld(), ctx.getBlockPos(), this.getDefaultState());
   }

   public BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState neighborState, WorldAccess world, BlockPos pos, BlockPos neighborPos) {
      boolean bl = direction.getAxis() == Direction.Axis.Y;
      return bl ? this.getStateWithInstrument(world, pos, state) : super.getStateForNeighborUpdate(state, direction, neighborState, world, pos, neighborPos);
   }

   public void neighborUpdate(BlockState state, World world, BlockPos pos, Block sourceBlock, BlockPos sourcePos, boolean notify) {
      boolean bl2 = world.isReceivingRedstonePower(pos);
      if (bl2 != (Boolean)state.get(POWERED)) {
         if (bl2) {
            this.playNote((Entity)null, state, world, pos);
         }

         world.setBlockState(pos, (BlockState)state.with(POWERED, bl2), Block.NOTIFY_ALL);
      }

   }

   private void playNote(@Nullable Entity entity, BlockState state, World world, BlockPos pos) {
      if (!((Instrument)state.get(INSTRUMENT)).shouldRequireAirAbove() || world.getBlockState(pos.up()).isAir()) {
         world.addSyncedBlockEvent(pos, this, 0, 0);
         world.emitGameEvent(entity, GameEvent.NOTE_BLOCK_PLAY, pos);
      }

   }

   public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
      ItemStack lv = player.getStackInHand(hand);
      if (lv.isIn(ItemTags.NOTEBLOCK_TOP_INSTRUMENTS) && hit.getSide() == Direction.UP) {
         return ActionResult.PASS;
      } else if (world.isClient) {
         return ActionResult.SUCCESS;
      } else {
         state = (BlockState)state.cycle(NOTE);
         world.setBlockState(pos, state, Block.NOTIFY_ALL);
         this.playNote(player, state, world, pos);
         player.incrementStat(Stats.TUNE_NOTEBLOCK);
         return ActionResult.CONSUME;
      }
   }

   public void onBlockBreakStart(BlockState state, World world, BlockPos pos, PlayerEntity player) {
      if (!world.isClient) {
         this.playNote(player, state, world, pos);
         player.incrementStat(Stats.PLAY_NOTEBLOCK);
      }
   }

   public static float getNotePitch(int note) {
      return (float)Math.pow(2.0, (double)(note - 12) / 12.0);
   }

   public boolean onSyncedBlockEvent(BlockState state, World world, BlockPos pos, int type, int data) {
      Instrument lv = (Instrument)state.get(INSTRUMENT);
      float f;
      if (lv.shouldSpawnNoteParticles()) {
         int k = (Integer)state.get(NOTE);
         f = getNotePitch(k);
         world.addParticle(ParticleTypes.NOTE, (double)pos.getX() + 0.5, (double)pos.getY() + 1.2, (double)pos.getZ() + 0.5, (double)k / 24.0, 0.0, 0.0);
      } else {
         f = 1.0F;
      }

      RegistryEntry lv3;
      if (lv.hasCustomSound()) {
         Identifier lv2 = this.getCustomSound(world, pos);
         if (lv2 == null) {
            return false;
         }

         lv3 = RegistryEntry.of(SoundEvent.of(lv2));
      } else {
         lv3 = lv.getSound();
      }

      world.playSound((PlayerEntity)null, (double)pos.getX() + 0.5, (double)pos.getY() + 0.5, (double)pos.getZ() + 0.5, (RegistryEntry)lv3, SoundCategory.RECORDS, 3.0F, f, world.random.nextLong());
      return true;
   }

   @Nullable
   private Identifier getCustomSound(World world, BlockPos pos) {
      BlockEntity var4 = world.getBlockEntity(pos.up());
      if (var4 instanceof SkullBlockEntity lv) {
         return lv.getNoteBlockSound();
      } else {
         return null;
      }
   }

   protected void appendProperties(StateManager.Builder builder) {
      builder.add(INSTRUMENT, POWERED, NOTE);
   }

   static {
      INSTRUMENT = Properties.INSTRUMENT;
      POWERED = Properties.POWERED;
      NOTE = Properties.NOTE;
   }
}
