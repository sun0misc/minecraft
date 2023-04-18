package net.minecraft.block;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import java.util.Map;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.state.property.Property;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;

public class ConnectingBlock extends Block {
   private static final Direction[] FACINGS = Direction.values();
   public static final BooleanProperty NORTH;
   public static final BooleanProperty EAST;
   public static final BooleanProperty SOUTH;
   public static final BooleanProperty WEST;
   public static final BooleanProperty UP;
   public static final BooleanProperty DOWN;
   public static final Map FACING_PROPERTIES;
   protected final VoxelShape[] connectionsToShape;

   protected ConnectingBlock(float radius, AbstractBlock.Settings settings) {
      super(settings);
      this.connectionsToShape = this.generateFacingsToShapeMap(radius);
   }

   private VoxelShape[] generateFacingsToShapeMap(float radius) {
      float g = 0.5F - radius;
      float h = 0.5F + radius;
      VoxelShape lv = Block.createCuboidShape((double)(g * 16.0F), (double)(g * 16.0F), (double)(g * 16.0F), (double)(h * 16.0F), (double)(h * 16.0F), (double)(h * 16.0F));
      VoxelShape[] lvs = new VoxelShape[FACINGS.length];

      for(int i = 0; i < FACINGS.length; ++i) {
         Direction lv2 = FACINGS[i];
         lvs[i] = VoxelShapes.cuboid(0.5 + Math.min((double)(-radius), (double)lv2.getOffsetX() * 0.5), 0.5 + Math.min((double)(-radius), (double)lv2.getOffsetY() * 0.5), 0.5 + Math.min((double)(-radius), (double)lv2.getOffsetZ() * 0.5), 0.5 + Math.max((double)radius, (double)lv2.getOffsetX() * 0.5), 0.5 + Math.max((double)radius, (double)lv2.getOffsetY() * 0.5), 0.5 + Math.max((double)radius, (double)lv2.getOffsetZ() * 0.5));
      }

      VoxelShape[] lvs2 = new VoxelShape[64];

      for(int j = 0; j < 64; ++j) {
         VoxelShape lv3 = lv;

         for(int k = 0; k < FACINGS.length; ++k) {
            if ((j & 1 << k) != 0) {
               lv3 = VoxelShapes.union(lv3, lvs[k]);
            }
         }

         lvs2[j] = lv3;
      }

      return lvs2;
   }

   public boolean isTransparent(BlockState state, BlockView world, BlockPos pos) {
      return false;
   }

   public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
      return this.connectionsToShape[this.getConnectionMask(state)];
   }

   protected int getConnectionMask(BlockState state) {
      int i = 0;

      for(int j = 0; j < FACINGS.length; ++j) {
         if ((Boolean)state.get((Property)FACING_PROPERTIES.get(FACINGS[j]))) {
            i |= 1 << j;
         }
      }

      return i;
   }

   static {
      NORTH = Properties.NORTH;
      EAST = Properties.EAST;
      SOUTH = Properties.SOUTH;
      WEST = Properties.WEST;
      UP = Properties.UP;
      DOWN = Properties.DOWN;
      FACING_PROPERTIES = ImmutableMap.copyOf((Map)Util.make(Maps.newEnumMap(Direction.class), (directions) -> {
         directions.put(Direction.NORTH, NORTH);
         directions.put(Direction.EAST, EAST);
         directions.put(Direction.SOUTH, SOUTH);
         directions.put(Direction.WEST, WEST);
         directions.put(Direction.UP, UP);
         directions.put(Direction.DOWN, DOWN);
      }));
   }
}
