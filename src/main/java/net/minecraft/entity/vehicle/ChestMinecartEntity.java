package net.minecraft.entity.vehicle;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.ChestBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.PiglinBrain;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;

public class ChestMinecartEntity extends StorageMinecartEntity {
   public ChestMinecartEntity(EntityType arg, World arg2) {
      super(arg, arg2);
   }

   public ChestMinecartEntity(World world, double x, double y, double z) {
      super(EntityType.CHEST_MINECART, x, y, z, world);
   }

   protected Item getItem() {
      return Items.CHEST_MINECART;
   }

   public int size() {
      return 27;
   }

   public AbstractMinecartEntity.Type getMinecartType() {
      return AbstractMinecartEntity.Type.CHEST;
   }

   public BlockState getDefaultContainedBlock() {
      return (BlockState)Blocks.CHEST.getDefaultState().with(ChestBlock.FACING, Direction.NORTH);
   }

   public int getDefaultBlockOffset() {
      return 8;
   }

   public ScreenHandler getScreenHandler(int syncId, PlayerInventory playerInventory) {
      return GenericContainerScreenHandler.createGeneric9x3(syncId, playerInventory, this);
   }

   public void onClose(PlayerEntity player) {
      this.world.emitGameEvent(GameEvent.CONTAINER_CLOSE, this.getPos(), GameEvent.Emitter.of((Entity)player));
   }

   public ActionResult interact(PlayerEntity player, Hand hand) {
      ActionResult lv = this.open(player);
      if (lv.isAccepted()) {
         this.emitGameEvent(GameEvent.CONTAINER_OPEN, player);
         PiglinBrain.onGuardedBlockInteracted(player, true);
      }

      return lv;
   }
}
