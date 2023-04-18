package net.minecraft.entity.decoration;

import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Objects;
import java.util.UUID;
import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.block.piston.PistonBehavior;
import net.minecraft.entity.Attackable;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.Targeter;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtOps;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Util;
import net.minecraft.util.Uuids;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

public class InteractionEntity extends Entity implements Attackable, Targeter {
   private static final Logger field_42624 = LogUtils.getLogger();
   private static final TrackedData WIDTH;
   private static final TrackedData HEIGHT;
   private static final TrackedData RESPONSE;
   private static final String WIDTH_KEY = "width";
   private static final String HEIGHT_KEY = "height";
   private static final String ATTACK_KEY = "attack";
   private static final String INTERACTION_KEY = "interaction";
   private static final String RESPONSE_KEY = "response";
   @Nullable
   private Interaction attack;
   @Nullable
   private Interaction interaction;

   public InteractionEntity(EntityType arg, World arg2) {
      super(arg, arg2);
      this.noClip = true;
   }

   protected void initDataTracker() {
      this.dataTracker.startTracking(WIDTH, 1.0F);
      this.dataTracker.startTracking(HEIGHT, 1.0F);
      this.dataTracker.startTracking(RESPONSE, false);
   }

   protected void readCustomDataFromNbt(NbtCompound nbt) {
      if (nbt.contains("width", NbtElement.NUMBER_TYPE)) {
         this.setInteractionWidth(nbt.getFloat("width"));
      }

      if (nbt.contains("height", NbtElement.NUMBER_TYPE)) {
         this.setInteractionHeight(nbt.getFloat("height"));
      }

      DataResult var10000;
      Logger var10002;
      if (nbt.contains("attack")) {
         var10000 = InteractionEntity.Interaction.CODEC.decode(NbtOps.INSTANCE, nbt.get("attack"));
         var10002 = field_42624;
         Objects.requireNonNull(var10002);
         var10000.resultOrPartial(Util.addPrefix("Interaction entity", var10002::error)).ifPresent((pair) -> {
            this.attack = (Interaction)pair.getFirst();
         });
      } else {
         this.attack = null;
      }

      if (nbt.contains("interaction")) {
         var10000 = InteractionEntity.Interaction.CODEC.decode(NbtOps.INSTANCE, nbt.get("interaction"));
         var10002 = field_42624;
         Objects.requireNonNull(var10002);
         var10000.resultOrPartial(Util.addPrefix("Interaction entity", var10002::error)).ifPresent((pair) -> {
            this.interaction = (Interaction)pair.getFirst();
         });
      } else {
         this.interaction = null;
      }

      this.setResponse(nbt.getBoolean("response"));
      this.setBoundingBox(this.calculateBoundingBox());
   }

   protected void writeCustomDataToNbt(NbtCompound nbt) {
      nbt.putFloat("width", this.getInteractionWidth());
      nbt.putFloat("height", this.getInteractionHeight());
      if (this.attack != null) {
         InteractionEntity.Interaction.CODEC.encodeStart(NbtOps.INSTANCE, this.attack).result().ifPresent((arg2) -> {
            nbt.put("attack", arg2);
         });
      }

      if (this.interaction != null) {
         InteractionEntity.Interaction.CODEC.encodeStart(NbtOps.INSTANCE, this.interaction).result().ifPresent((arg2) -> {
            nbt.put("interaction", arg2);
         });
      }

      nbt.putBoolean("response", this.shouldRespond());
   }

   public void onTrackedDataSet(TrackedData data) {
      super.onTrackedDataSet(data);
      if (HEIGHT.equals(data) || WIDTH.equals(data)) {
         this.setBoundingBox(this.calculateBoundingBox());
      }

   }

   public boolean canBeHitByProjectile() {
      return false;
   }

   public boolean canHit() {
      return true;
   }

   public PistonBehavior getPistonBehavior() {
      return PistonBehavior.IGNORE;
   }

   public boolean handleAttack(Entity attacker) {
      if (attacker instanceof PlayerEntity lv) {
         this.attack = new Interaction(lv.getUuid(), this.world.getTime());
         if (lv instanceof ServerPlayerEntity lv2) {
            Criteria.PLAYER_HURT_ENTITY.trigger(lv2, this, lv.getDamageSources().generic(), 1.0F, 1.0F, false);
         }

         return !this.shouldRespond();
      } else {
         return false;
      }
   }

   public ActionResult interact(PlayerEntity player, Hand hand) {
      if (this.world.isClient) {
         return this.shouldRespond() ? ActionResult.SUCCESS : ActionResult.CONSUME;
      } else {
         this.interaction = new Interaction(player.getUuid(), this.world.getTime());
         return ActionResult.CONSUME;
      }
   }

   public void tick() {
   }

   @Nullable
   public LivingEntity getLastAttacker() {
      return this.attack != null ? this.world.getPlayerByUuid(this.attack.player()) : null;
   }

   @Nullable
   public LivingEntity getTarget() {
      return this.interaction != null ? this.world.getPlayerByUuid(this.interaction.player()) : null;
   }

   private void setInteractionWidth(float width) {
      this.dataTracker.set(WIDTH, width);
   }

   private float getInteractionWidth() {
      return (Float)this.dataTracker.get(WIDTH);
   }

   private void setInteractionHeight(float height) {
      this.dataTracker.set(HEIGHT, height);
   }

   private float getInteractionHeight() {
      return (Float)this.dataTracker.get(HEIGHT);
   }

   private void setResponse(boolean response) {
      this.dataTracker.set(RESPONSE, response);
   }

   private boolean shouldRespond() {
      return (Boolean)this.dataTracker.get(RESPONSE);
   }

   private EntityDimensions getDimensions() {
      return EntityDimensions.changing(this.getInteractionWidth(), this.getInteractionHeight());
   }

   public EntityDimensions getDimensions(EntityPose pose) {
      return this.getDimensions();
   }

   protected Box calculateBoundingBox() {
      return this.getDimensions().getBoxAt(this.getPos());
   }

   static {
      WIDTH = DataTracker.registerData(InteractionEntity.class, TrackedDataHandlerRegistry.FLOAT);
      HEIGHT = DataTracker.registerData(InteractionEntity.class, TrackedDataHandlerRegistry.FLOAT);
      RESPONSE = DataTracker.registerData(InteractionEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
   }

   static record Interaction(UUID player, long timestamp) {
      public static final Codec CODEC = RecordCodecBuilder.create((instance) -> {
         return instance.group(Uuids.INT_STREAM_CODEC.fieldOf("player").forGetter(Interaction::player), Codec.LONG.fieldOf("timestamp").forGetter(Interaction::timestamp)).apply(instance, Interaction::new);
      });

      Interaction(UUID uUID, long l) {
         this.player = uUID;
         this.timestamp = l;
      }

      public UUID player() {
         return this.player;
      }

      public long timestamp() {
         return this.timestamp;
      }
   }
}
