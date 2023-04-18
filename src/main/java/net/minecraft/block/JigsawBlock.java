package net.minecraft.block;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.JigsawBlockEntity;
import net.minecraft.block.enums.JigsawOrientation;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.structure.StructureTemplate;
import net.minecraft.util.ActionResult;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

public class JigsawBlock extends Block implements BlockEntityProvider, OperatorBlock {
   public static final EnumProperty ORIENTATION;

   protected JigsawBlock(AbstractBlock.Settings arg) {
      super(arg);
      this.setDefaultState((BlockState)((BlockState)this.stateManager.getDefaultState()).with(ORIENTATION, JigsawOrientation.NORTH_UP));
   }

   protected void appendProperties(StateManager.Builder builder) {
      builder.add(ORIENTATION);
   }

   public BlockState rotate(BlockState state, BlockRotation rotation) {
      return (BlockState)state.with(ORIENTATION, rotation.getDirectionTransformation().mapJigsawOrientation((JigsawOrientation)state.get(ORIENTATION)));
   }

   public BlockState mirror(BlockState state, BlockMirror mirror) {
      return (BlockState)state.with(ORIENTATION, mirror.getDirectionTransformation().mapJigsawOrientation((JigsawOrientation)state.get(ORIENTATION)));
   }

   public BlockState getPlacementState(ItemPlacementContext ctx) {
      Direction lv = ctx.getSide();
      Direction lv2;
      if (lv.getAxis() == Direction.Axis.Y) {
         lv2 = ctx.getHorizontalPlayerFacing().getOpposite();
      } else {
         lv2 = Direction.UP;
      }

      return (BlockState)this.getDefaultState().with(ORIENTATION, JigsawOrientation.byDirections(lv, lv2));
   }

   public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
      return new JigsawBlockEntity(pos, state);
   }

   public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
      BlockEntity lv = world.getBlockEntity(pos);
      if (lv instanceof JigsawBlockEntity && player.isCreativeLevelTwoOp()) {
         player.openJigsawScreen((JigsawBlockEntity)lv);
         return ActionResult.success(world.isClient);
      } else {
         return ActionResult.PASS;
      }
   }

   public static boolean attachmentMatches(StructureTemplate.StructureBlockInfo info1, StructureTemplate.StructureBlockInfo info2) {
      Direction lv = getFacing(info1.state());
      Direction lv2 = getFacing(info2.state());
      Direction lv3 = getRotation(info1.state());
      Direction lv4 = getRotation(info2.state());
      JigsawBlockEntity.Joint lv5 = (JigsawBlockEntity.Joint)JigsawBlockEntity.Joint.byName(info1.nbt().getString("joint")).orElseGet(() -> {
         return lv.getAxis().isHorizontal() ? JigsawBlockEntity.Joint.ALIGNED : JigsawBlockEntity.Joint.ROLLABLE;
      });
      boolean bl = lv5 == JigsawBlockEntity.Joint.ROLLABLE;
      return lv == lv2.getOpposite() && (bl || lv3 == lv4) && info1.nbt().getString("target").equals(info2.nbt().getString("name"));
   }

   public static Direction getFacing(BlockState state) {
      return ((JigsawOrientation)state.get(ORIENTATION)).getFacing();
   }

   public static Direction getRotation(BlockState state) {
      return ((JigsawOrientation)state.get(ORIENTATION)).getRotation();
   }

   static {
      ORIENTATION = Properties.ORIENTATION;
   }
}
