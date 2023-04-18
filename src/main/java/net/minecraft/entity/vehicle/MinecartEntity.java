package net.minecraft.entity.vehicle;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.world.World;

public class MinecartEntity extends AbstractMinecartEntity {
   public MinecartEntity(EntityType arg, World arg2) {
      super(arg, arg2);
   }

   public MinecartEntity(World world, double x, double y, double z) {
      super(EntityType.MINECART, world, x, y, z);
   }

   public ActionResult interact(PlayerEntity player, Hand hand) {
      if (player.shouldCancelInteraction()) {
         return ActionResult.PASS;
      } else if (this.hasPassengers()) {
         return ActionResult.PASS;
      } else if (!this.world.isClient) {
         return player.startRiding(this) ? ActionResult.CONSUME : ActionResult.PASS;
      } else {
         return ActionResult.SUCCESS;
      }
   }

   protected Item getItem() {
      return Items.MINECART;
   }

   public void onActivatorRail(int x, int y, int z, boolean powered) {
      if (powered) {
         if (this.hasPassengers()) {
            this.removeAllPassengers();
         }

         if (this.getDamageWobbleTicks() == 0) {
            this.setDamageWobbleSide(-this.getDamageWobbleSide());
            this.setDamageWobbleTicks(10);
            this.setDamageWobbleStrength(50.0F);
            this.scheduleVelocityUpdate();
         }
      }

   }

   public AbstractMinecartEntity.Type getMinecartType() {
      return AbstractMinecartEntity.Type.RIDEABLE;
   }
}
