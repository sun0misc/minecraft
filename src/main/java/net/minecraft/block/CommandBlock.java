package net.minecraft.block;

import com.mojang.logging.LogUtils;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.CommandBlockBlockEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.Hand;
import net.minecraft.util.StringHelper;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.CommandBlockExecutor;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import org.slf4j.Logger;

public class CommandBlock extends BlockWithEntity implements OperatorBlock {
   private static final Logger LOGGER = LogUtils.getLogger();
   public static final DirectionProperty FACING;
   public static final BooleanProperty CONDITIONAL;
   private final boolean auto;

   public CommandBlock(AbstractBlock.Settings settings, boolean auto) {
      super(settings);
      this.setDefaultState((BlockState)((BlockState)((BlockState)this.stateManager.getDefaultState()).with(FACING, Direction.NORTH)).with(CONDITIONAL, false));
      this.auto = auto;
   }

   public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
      CommandBlockBlockEntity lv = new CommandBlockBlockEntity(pos, state);
      lv.setAuto(this.auto);
      return lv;
   }

   public void neighborUpdate(BlockState state, World world, BlockPos pos, Block sourceBlock, BlockPos sourcePos, boolean notify) {
      if (!world.isClient) {
         BlockEntity lv = world.getBlockEntity(pos);
         if (lv instanceof CommandBlockBlockEntity) {
            CommandBlockBlockEntity lv2 = (CommandBlockBlockEntity)lv;
            boolean bl2 = world.isReceivingRedstonePower(pos);
            boolean bl3 = lv2.isPowered();
            lv2.setPowered(bl2);
            if (!bl3 && !lv2.isAuto() && lv2.getCommandBlockType() != CommandBlockBlockEntity.Type.SEQUENCE) {
               if (bl2) {
                  lv2.updateConditionMet();
                  world.scheduleBlockTick(pos, this, 1);
               }

            }
         }
      }
   }

   public void scheduledTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
      BlockEntity lv = world.getBlockEntity(pos);
      if (lv instanceof CommandBlockBlockEntity lv2) {
         CommandBlockExecutor lv3 = lv2.getCommandExecutor();
         boolean bl = !StringHelper.isEmpty(lv3.getCommand());
         CommandBlockBlockEntity.Type lv4 = lv2.getCommandBlockType();
         boolean bl2 = lv2.isConditionMet();
         if (lv4 == CommandBlockBlockEntity.Type.AUTO) {
            lv2.updateConditionMet();
            if (bl2) {
               this.execute(state, world, pos, lv3, bl);
            } else if (lv2.isConditionalCommandBlock()) {
               lv3.setSuccessCount(0);
            }

            if (lv2.isPowered() || lv2.isAuto()) {
               world.scheduleBlockTick(pos, this, 1);
            }
         } else if (lv4 == CommandBlockBlockEntity.Type.REDSTONE) {
            if (bl2) {
               this.execute(state, world, pos, lv3, bl);
            } else if (lv2.isConditionalCommandBlock()) {
               lv3.setSuccessCount(0);
            }
         }

         world.updateComparators(pos, this);
      }

   }

   private void execute(BlockState state, World world, BlockPos pos, CommandBlockExecutor executor, boolean hasCommand) {
      if (hasCommand) {
         executor.execute(world);
      } else {
         executor.setSuccessCount(0);
      }

      executeCommandChain(world, pos, (Direction)state.get(FACING));
   }

   public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
      BlockEntity lv = world.getBlockEntity(pos);
      if (lv instanceof CommandBlockBlockEntity && player.isCreativeLevelTwoOp()) {
         player.openCommandBlockScreen((CommandBlockBlockEntity)lv);
         return ActionResult.success(world.isClient);
      } else {
         return ActionResult.PASS;
      }
   }

   public boolean hasComparatorOutput(BlockState state) {
      return true;
   }

   public int getComparatorOutput(BlockState state, World world, BlockPos pos) {
      BlockEntity lv = world.getBlockEntity(pos);
      return lv instanceof CommandBlockBlockEntity ? ((CommandBlockBlockEntity)lv).getCommandExecutor().getSuccessCount() : 0;
   }

   public void onPlaced(World world, BlockPos pos, BlockState state, LivingEntity placer, ItemStack itemStack) {
      BlockEntity lv = world.getBlockEntity(pos);
      if (lv instanceof CommandBlockBlockEntity lv2) {
         CommandBlockExecutor lv3 = lv2.getCommandExecutor();
         if (itemStack.hasCustomName()) {
            lv3.setCustomName(itemStack.getName());
         }

         if (!world.isClient) {
            if (BlockItem.getBlockEntityNbt(itemStack) == null) {
               lv3.setTrackOutput(world.getGameRules().getBoolean(GameRules.SEND_COMMAND_FEEDBACK));
               lv2.setAuto(this.auto);
            }

            if (lv2.getCommandBlockType() == CommandBlockBlockEntity.Type.SEQUENCE) {
               boolean bl = world.isReceivingRedstonePower(pos);
               lv2.setPowered(bl);
            }
         }

      }
   }

   public BlockRenderType getRenderType(BlockState state) {
      return BlockRenderType.MODEL;
   }

   public BlockState rotate(BlockState state, BlockRotation rotation) {
      return (BlockState)state.with(FACING, rotation.rotate((Direction)state.get(FACING)));
   }

   public BlockState mirror(BlockState state, BlockMirror mirror) {
      return state.rotate(mirror.getRotation((Direction)state.get(FACING)));
   }

   protected void appendProperties(StateManager.Builder builder) {
      builder.add(FACING, CONDITIONAL);
   }

   public BlockState getPlacementState(ItemPlacementContext ctx) {
      return (BlockState)this.getDefaultState().with(FACING, ctx.getPlayerLookDirection().getOpposite());
   }

   private static void executeCommandChain(World world, BlockPos pos, Direction facing) {
      BlockPos.Mutable lv = pos.mutableCopy();
      GameRules lv2 = world.getGameRules();

      int i;
      BlockState lv3;
      for(i = lv2.getInt(GameRules.MAX_COMMAND_CHAIN_LENGTH); i-- > 0; facing = (Direction)lv3.get(FACING)) {
         lv.move(facing);
         lv3 = world.getBlockState(lv);
         Block lv4 = lv3.getBlock();
         if (!lv3.isOf(Blocks.CHAIN_COMMAND_BLOCK)) {
            break;
         }

         BlockEntity lv5 = world.getBlockEntity(lv);
         if (!(lv5 instanceof CommandBlockBlockEntity)) {
            break;
         }

         CommandBlockBlockEntity lv6 = (CommandBlockBlockEntity)lv5;
         if (lv6.getCommandBlockType() != CommandBlockBlockEntity.Type.SEQUENCE) {
            break;
         }

         if (lv6.isPowered() || lv6.isAuto()) {
            CommandBlockExecutor lv7 = lv6.getCommandExecutor();
            if (lv6.updateConditionMet()) {
               if (!lv7.execute(world)) {
                  break;
               }

               world.updateComparators(lv, lv4);
            } else if (lv6.isConditionalCommandBlock()) {
               lv7.setSuccessCount(0);
            }
         }
      }

      if (i <= 0) {
         int j = Math.max(lv2.getInt(GameRules.MAX_COMMAND_CHAIN_LENGTH), 0);
         LOGGER.warn("Command Block chain tried to execute more than {} steps!", j);
      }

   }

   static {
      FACING = FacingBlock.FACING;
      CONDITIONAL = Properties.CONDITIONAL;
   }
}
