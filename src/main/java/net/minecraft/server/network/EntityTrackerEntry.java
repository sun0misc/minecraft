package net.minecraft.server.network;

import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Pair;
import com.mojang.logging.LogUtils;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Stream;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.TrackedPosition;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.decoration.ItemFrameEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.item.FilledMapItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.map.MapState;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BundleS2CPacket;
import net.minecraft.network.packet.s2c.play.EntitiesDestroyS2CPacket;
import net.minecraft.network.packet.s2c.play.EntityAttachS2CPacket;
import net.minecraft.network.packet.s2c.play.EntityAttributesS2CPacket;
import net.minecraft.network.packet.s2c.play.EntityEquipmentUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.EntityPassengersSetS2CPacket;
import net.minecraft.network.packet.s2c.play.EntityPositionS2CPacket;
import net.minecraft.network.packet.s2c.play.EntityS2CPacket;
import net.minecraft.network.packet.s2c.play.EntitySetHeadYawS2CPacket;
import net.minecraft.network.packet.s2c.play.EntityStatusEffectS2CPacket;
import net.minecraft.network.packet.s2c.play.EntityTrackerUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.EntityVelocityUpdateS2CPacket;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

public class EntityTrackerEntry {
   private static final Logger LOGGER = LogUtils.getLogger();
   private static final int field_29767 = 1;
   private final ServerWorld world;
   private final Entity entity;
   private final int tickInterval;
   private final boolean alwaysUpdateVelocity;
   private final Consumer receiver;
   private final TrackedPosition trackedPos = new TrackedPosition();
   private int lastYaw;
   private int lastPitch;
   private int lastHeadPitch;
   private Vec3d velocity;
   private int trackingTick;
   private int updatesWithoutVehicle;
   private List lastPassengers;
   private boolean hadVehicle;
   private boolean lastOnGround;
   @Nullable
   private List changedEntries;

   public EntityTrackerEntry(ServerWorld world, Entity entity, int tickInterval, boolean alwaysUpdateVelocity, Consumer receiver) {
      this.velocity = Vec3d.ZERO;
      this.lastPassengers = Collections.emptyList();
      this.world = world;
      this.receiver = receiver;
      this.entity = entity;
      this.tickInterval = tickInterval;
      this.alwaysUpdateVelocity = alwaysUpdateVelocity;
      this.trackedPos.setPos(entity.getSyncedPos());
      this.lastYaw = MathHelper.floor(entity.getYaw() * 256.0F / 360.0F);
      this.lastPitch = MathHelper.floor(entity.getPitch() * 256.0F / 360.0F);
      this.lastHeadPitch = MathHelper.floor(entity.getHeadYaw() * 256.0F / 360.0F);
      this.lastOnGround = entity.isOnGround();
      this.changedEntries = entity.getDataTracker().getChangedEntries();
   }

   public void tick() {
      List list = this.entity.getPassengerList();
      if (!list.equals(this.lastPassengers)) {
         this.receiver.accept(new EntityPassengersSetS2CPacket(this.entity));
         streamChangedPassengers(list, this.lastPassengers).forEach((passenger) -> {
            if (passenger instanceof ServerPlayerEntity lv) {
               lv.networkHandler.requestTeleport(lv.getX(), lv.getY(), lv.getZ(), lv.getYaw(), lv.getPitch());
            }

         });
         this.lastPassengers = list;
      }

      Entity var3 = this.entity;
      if (var3 instanceof ItemFrameEntity lv) {
         if (this.trackingTick % 10 == 0) {
            ItemStack lv2 = lv.getHeldItemStack();
            if (lv2.getItem() instanceof FilledMapItem) {
               Integer integer = FilledMapItem.getMapId(lv2);
               MapState lv3 = FilledMapItem.getMapState((Integer)integer, this.world);
               if (lv3 != null) {
                  Iterator var6 = this.world.getPlayers().iterator();

                  while(var6.hasNext()) {
                     ServerPlayerEntity lv4 = (ServerPlayerEntity)var6.next();
                     lv3.update(lv4, lv2);
                     Packet lv5 = lv3.getPlayerMarkerPacket(integer, lv4);
                     if (lv5 != null) {
                        lv4.networkHandler.sendPacket(lv5);
                     }
                  }
               }
            }

            this.syncEntityData();
         }
      }

      if (this.trackingTick % this.tickInterval == 0 || this.entity.velocityDirty || this.entity.getDataTracker().isDirty()) {
         int i;
         int j;
         if (this.entity.hasVehicle()) {
            i = MathHelper.floor(this.entity.getYaw() * 256.0F / 360.0F);
            j = MathHelper.floor(this.entity.getPitch() * 256.0F / 360.0F);
            boolean bl = Math.abs(i - this.lastYaw) >= 1 || Math.abs(j - this.lastPitch) >= 1;
            if (bl) {
               this.receiver.accept(new EntityS2CPacket.Rotate(this.entity.getId(), (byte)i, (byte)j, this.entity.isOnGround()));
               this.lastYaw = i;
               this.lastPitch = j;
            }

            this.trackedPos.setPos(this.entity.getSyncedPos());
            this.syncEntityData();
            this.hadVehicle = true;
         } else {
            ++this.updatesWithoutVehicle;
            i = MathHelper.floor(this.entity.getYaw() * 256.0F / 360.0F);
            j = MathHelper.floor(this.entity.getPitch() * 256.0F / 360.0F);
            Vec3d lv6 = this.entity.getSyncedPos();
            boolean bl2 = this.trackedPos.subtract(lv6).lengthSquared() >= 7.62939453125E-6;
            Packet lv7 = null;
            boolean bl3 = bl2 || this.trackingTick % 60 == 0;
            boolean bl4 = Math.abs(i - this.lastYaw) >= 1 || Math.abs(j - this.lastPitch) >= 1;
            boolean bl5 = false;
            boolean bl6 = false;
            if (this.trackingTick > 0 || this.entity instanceof PersistentProjectileEntity) {
               long l = this.trackedPos.getDeltaX(lv6);
               long m = this.trackedPos.getDeltaY(lv6);
               long n = this.trackedPos.getDeltaZ(lv6);
               boolean bl7 = l < -32768L || l > 32767L || m < -32768L || m > 32767L || n < -32768L || n > 32767L;
               if (!bl7 && this.updatesWithoutVehicle <= 400 && !this.hadVehicle && this.lastOnGround == this.entity.isOnGround()) {
                  if ((!bl3 || !bl4) && !(this.entity instanceof PersistentProjectileEntity)) {
                     if (bl3) {
                        lv7 = new EntityS2CPacket.MoveRelative(this.entity.getId(), (short)((int)l), (short)((int)m), (short)((int)n), this.entity.isOnGround());
                        bl5 = true;
                     } else if (bl4) {
                        lv7 = new EntityS2CPacket.Rotate(this.entity.getId(), (byte)i, (byte)j, this.entity.isOnGround());
                        bl6 = true;
                     }
                  } else {
                     lv7 = new EntityS2CPacket.RotateAndMoveRelative(this.entity.getId(), (short)((int)l), (short)((int)m), (short)((int)n), (byte)i, (byte)j, this.entity.isOnGround());
                     bl5 = true;
                     bl6 = true;
                  }
               } else {
                  this.lastOnGround = this.entity.isOnGround();
                  this.updatesWithoutVehicle = 0;
                  lv7 = new EntityPositionS2CPacket(this.entity);
                  bl5 = true;
                  bl6 = true;
               }
            }

            if ((this.alwaysUpdateVelocity || this.entity.velocityDirty || this.entity instanceof LivingEntity && ((LivingEntity)this.entity).isFallFlying()) && this.trackingTick > 0) {
               Vec3d lv8 = this.entity.getVelocity();
               double d = lv8.squaredDistanceTo(this.velocity);
               if (d > 1.0E-7 || d > 0.0 && lv8.lengthSquared() == 0.0) {
                  this.velocity = lv8;
                  this.receiver.accept(new EntityVelocityUpdateS2CPacket(this.entity.getId(), this.velocity));
               }
            }

            if (lv7 != null) {
               this.receiver.accept(lv7);
            }

            this.syncEntityData();
            if (bl5) {
               this.trackedPos.setPos(lv6);
            }

            if (bl6) {
               this.lastYaw = i;
               this.lastPitch = j;
            }

            this.hadVehicle = false;
         }

         i = MathHelper.floor(this.entity.getHeadYaw() * 256.0F / 360.0F);
         if (Math.abs(i - this.lastHeadPitch) >= 1) {
            this.receiver.accept(new EntitySetHeadYawS2CPacket(this.entity, (byte)i));
            this.lastHeadPitch = i;
         }

         this.entity.velocityDirty = false;
      }

      ++this.trackingTick;
      if (this.entity.velocityModified) {
         this.sendSyncPacket(new EntityVelocityUpdateS2CPacket(this.entity));
         this.entity.velocityModified = false;
      }

   }

   private static Stream streamChangedPassengers(List passengers, List lastPassengers) {
      return lastPassengers.stream().filter((passenger) -> {
         return !passengers.contains(passenger);
      });
   }

   public void stopTracking(ServerPlayerEntity player) {
      this.entity.onStoppedTrackingBy(player);
      player.networkHandler.sendPacket(new EntitiesDestroyS2CPacket(new int[]{this.entity.getId()}));
   }

   public void startTracking(ServerPlayerEntity player) {
      List list = new ArrayList();
      Objects.requireNonNull(list);
      this.sendPackets(list::add);
      player.networkHandler.sendPacket(new BundleS2CPacket(list));
      this.entity.onStartedTrackingBy(player);
   }

   public void sendPackets(Consumer sender) {
      if (this.entity.isRemoved()) {
         LOGGER.warn("Fetching packet for removed entity {}", this.entity);
      }

      Packet lv = this.entity.createSpawnPacket();
      this.lastHeadPitch = MathHelper.floor(this.entity.getHeadYaw() * 256.0F / 360.0F);
      sender.accept(lv);
      if (this.changedEntries != null) {
         sender.accept(new EntityTrackerUpdateS2CPacket(this.entity.getId(), this.changedEntries));
      }

      boolean bl = this.alwaysUpdateVelocity;
      if (this.entity instanceof LivingEntity) {
         Collection collection = ((LivingEntity)this.entity).getAttributes().getAttributesToSend();
         if (!collection.isEmpty()) {
            sender.accept(new EntityAttributesS2CPacket(this.entity.getId(), collection));
         }

         if (((LivingEntity)this.entity).isFallFlying()) {
            bl = true;
         }
      }

      this.velocity = this.entity.getVelocity();
      if (bl && !(this.entity instanceof LivingEntity)) {
         sender.accept(new EntityVelocityUpdateS2CPacket(this.entity.getId(), this.velocity));
      }

      if (this.entity instanceof LivingEntity) {
         List list = Lists.newArrayList();
         EquipmentSlot[] var5 = EquipmentSlot.values();
         int var6 = var5.length;

         for(int var7 = 0; var7 < var6; ++var7) {
            EquipmentSlot lv2 = var5[var7];
            ItemStack lv3 = ((LivingEntity)this.entity).getEquippedStack(lv2);
            if (!lv3.isEmpty()) {
               list.add(Pair.of(lv2, lv3.copy()));
            }
         }

         if (!list.isEmpty()) {
            sender.accept(new EntityEquipmentUpdateS2CPacket(this.entity.getId(), list));
         }
      }

      if (this.entity instanceof LivingEntity) {
         LivingEntity lv4 = (LivingEntity)this.entity;
         Iterator var13 = lv4.getStatusEffects().iterator();

         while(var13.hasNext()) {
            StatusEffectInstance lv5 = (StatusEffectInstance)var13.next();
            sender.accept(new EntityStatusEffectS2CPacket(this.entity.getId(), lv5));
         }
      }

      if (!this.entity.getPassengerList().isEmpty()) {
         sender.accept(new EntityPassengersSetS2CPacket(this.entity));
      }

      if (this.entity.hasVehicle()) {
         sender.accept(new EntityPassengersSetS2CPacket(this.entity.getVehicle()));
      }

      if (this.entity instanceof MobEntity) {
         MobEntity lv6 = (MobEntity)this.entity;
         if (lv6.isLeashed()) {
            sender.accept(new EntityAttachS2CPacket(lv6, lv6.getHoldingEntity()));
         }
      }

   }

   private void syncEntityData() {
      DataTracker lv = this.entity.getDataTracker();
      List list = lv.getDirtyEntries();
      if (list != null) {
         this.changedEntries = lv.getChangedEntries();
         this.sendSyncPacket(new EntityTrackerUpdateS2CPacket(this.entity.getId(), list));
      }

      if (this.entity instanceof LivingEntity) {
         Set set = ((LivingEntity)this.entity).getAttributes().getTracked();
         if (!set.isEmpty()) {
            this.sendSyncPacket(new EntityAttributesS2CPacket(this.entity.getId(), set));
         }

         set.clear();
      }

   }

   private void sendSyncPacket(Packet packet) {
      this.receiver.accept(packet);
      if (this.entity instanceof ServerPlayerEntity) {
         ((ServerPlayerEntity)this.entity).networkHandler.sendPacket(packet);
      }

   }
}
