package net.minecraft.block;

import java.util.Iterator;
import java.util.List;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;

public class PressurePlateBlock extends AbstractPressurePlateBlock {
   public static final BooleanProperty POWERED;
   private final ActivationRule type;

   protected PressurePlateBlock(ActivationRule type, AbstractBlock.Settings settings, BlockSetType blockSetType) {
      super(settings, blockSetType);
      this.setDefaultState((BlockState)((BlockState)this.stateManager.getDefaultState()).with(POWERED, false));
      this.type = type;
   }

   protected int getRedstoneOutput(BlockState state) {
      return (Boolean)state.get(POWERED) ? 15 : 0;
   }

   protected BlockState setRedstoneOutput(BlockState state, int rsOut) {
      return (BlockState)state.with(POWERED, rsOut > 0);
   }

   protected int getRedstoneOutput(World world, BlockPos pos) {
      Box lv = BOX.offset(pos);
      List list;
      switch (this.type) {
         case EVERYTHING:
            list = world.getOtherEntities((Entity)null, lv);
            break;
         case MOBS:
            list = world.getNonSpectatingEntities(LivingEntity.class, lv);
            break;
         default:
            return 0;
      }

      if (!list.isEmpty()) {
         Iterator var5 = list.iterator();

         while(var5.hasNext()) {
            Entity lv2 = (Entity)var5.next();
            if (!lv2.canAvoidTraps()) {
               return 15;
            }
         }
      }

      return 0;
   }

   protected void appendProperties(StateManager.Builder builder) {
      builder.add(POWERED);
   }

   static {
      POWERED = Properties.POWERED;
   }

   public static enum ActivationRule {
      EVERYTHING,
      MOBS;

      // $FF: synthetic method
      private static ActivationRule[] method_36707() {
         return new ActivationRule[]{EVERYTHING, MOBS};
      }
   }
}
