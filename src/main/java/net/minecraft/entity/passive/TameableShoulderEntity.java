package net.minecraft.entity.passive;

import net.minecraft.entity.EntityType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.World;

public abstract class TameableShoulderEntity extends TameableEntity {
   private static final int READY_TO_SIT_COOLDOWN = 100;
   private int ticks;

   protected TameableShoulderEntity(EntityType arg, World arg2) {
      super(arg, arg2);
   }

   public boolean mountOnto(ServerPlayerEntity player) {
      NbtCompound lv = new NbtCompound();
      lv.putString("id", this.getSavedEntityId());
      this.writeNbt(lv);
      if (player.addShoulderEntity(lv)) {
         this.discard();
         return true;
      } else {
         return false;
      }
   }

   public void tick() {
      ++this.ticks;
      super.tick();
   }

   public boolean isReadyToSitOnPlayer() {
      return this.ticks > 100;
   }
}
