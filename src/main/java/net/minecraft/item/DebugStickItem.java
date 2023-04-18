package net.minecraft.item;

import java.util.Collection;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.Registries;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.Property;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import org.jetbrains.annotations.Nullable;

public class DebugStickItem extends Item {
   public DebugStickItem(Item.Settings arg) {
      super(arg);
   }

   public boolean hasGlint(ItemStack stack) {
      return true;
   }

   public boolean canMine(BlockState state, World world, BlockPos pos, PlayerEntity miner) {
      if (!world.isClient) {
         this.use(miner, state, world, pos, false, miner.getStackInHand(Hand.MAIN_HAND));
      }

      return false;
   }

   public ActionResult useOnBlock(ItemUsageContext context) {
      PlayerEntity lv = context.getPlayer();
      World lv2 = context.getWorld();
      if (!lv2.isClient && lv != null) {
         BlockPos lv3 = context.getBlockPos();
         if (!this.use(lv, lv2.getBlockState(lv3), lv2, lv3, true, context.getStack())) {
            return ActionResult.FAIL;
         }
      }

      return ActionResult.success(lv2.isClient);
   }

   private boolean use(PlayerEntity player, BlockState state, WorldAccess world, BlockPos pos, boolean update, ItemStack stack) {
      if (!player.isCreativeLevelTwoOp()) {
         return false;
      } else {
         Block lv = state.getBlock();
         StateManager lv2 = lv.getStateManager();
         Collection collection = lv2.getProperties();
         String string = Registries.BLOCK.getId(lv).toString();
         if (collection.isEmpty()) {
            sendMessage(player, Text.translatable(this.getTranslationKey() + ".empty", string));
            return false;
         } else {
            NbtCompound lv3 = stack.getOrCreateSubNbt("DebugProperty");
            String string2 = lv3.getString(string);
            Property lv4 = lv2.getProperty(string2);
            if (update) {
               if (lv4 == null) {
                  lv4 = (Property)collection.iterator().next();
               }

               BlockState lv5 = cycle(state, lv4, player.shouldCancelInteraction());
               world.setBlockState(pos, lv5, Block.NOTIFY_LISTENERS | Block.FORCE_STATE);
               sendMessage(player, Text.translatable(this.getTranslationKey() + ".update", lv4.getName(), getValueString(lv5, lv4)));
            } else {
               lv4 = (Property)cycle((Iterable)collection, (Object)lv4, player.shouldCancelInteraction());
               String string3 = lv4.getName();
               lv3.putString(string, string3);
               sendMessage(player, Text.translatable(this.getTranslationKey() + ".select", string3, getValueString(state, lv4)));
            }

            return true;
         }
      }
   }

   private static BlockState cycle(BlockState state, Property property, boolean inverse) {
      return (BlockState)state.with(property, (Comparable)cycle((Iterable)property.getValues(), (Object)state.get(property), inverse));
   }

   private static Object cycle(Iterable elements, @Nullable Object current, boolean inverse) {
      return inverse ? Util.previous(elements, current) : Util.next(elements, current);
   }

   private static void sendMessage(PlayerEntity player, Text message) {
      ((ServerPlayerEntity)player).sendMessageToClient(message, true);
   }

   private static String getValueString(BlockState state, Property property) {
      return property.name(state.get(property));
   }
}
