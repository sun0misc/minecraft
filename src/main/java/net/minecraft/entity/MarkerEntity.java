package net.minecraft.entity;

import net.minecraft.block.piston.PistonBehavior;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.packet.Packet;
import net.minecraft.world.World;

public class MarkerEntity extends Entity {
   private static final String DATA_KEY = "data";
   private NbtCompound data = new NbtCompound();

   public MarkerEntity(EntityType arg, World arg2) {
      super(arg, arg2);
      this.noClip = true;
   }

   public void tick() {
   }

   protected void initDataTracker() {
   }

   protected void readCustomDataFromNbt(NbtCompound nbt) {
      this.data = nbt.getCompound("data");
   }

   protected void writeCustomDataToNbt(NbtCompound nbt) {
      nbt.put("data", this.data.copy());
   }

   public Packet createSpawnPacket() {
      throw new IllegalStateException("Markers should never be sent");
   }

   protected boolean canAddPassenger(Entity passenger) {
      return false;
   }

   protected boolean couldAcceptPassenger() {
      return false;
   }

   protected void addPassenger(Entity passenger) {
      throw new IllegalStateException("Should never addPassenger without checking couldAcceptPassenger()");
   }

   public PistonBehavior getPistonBehavior() {
      return PistonBehavior.IGNORE;
   }
}
