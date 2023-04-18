package net.minecraft.entity.damage;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record DamageType(String msgId, DamageScaling scaling, float exhaustion, DamageEffects effects, DeathMessageType deathMessageType) {
   public static final Codec CODEC = RecordCodecBuilder.create((instance) -> {
      return instance.group(Codec.STRING.fieldOf("message_id").forGetter(DamageType::msgId), DamageScaling.CODEC.fieldOf("scaling").forGetter(DamageType::scaling), Codec.FLOAT.fieldOf("exhaustion").forGetter(DamageType::exhaustion), DamageEffects.CODEC.optionalFieldOf("effects", DamageEffects.HURT).forGetter(DamageType::effects), DeathMessageType.CODEC.optionalFieldOf("death_message_type", DeathMessageType.DEFAULT).forGetter(DamageType::deathMessageType)).apply(instance, DamageType::new);
   });

   public DamageType(String msgId, DamageScaling scaling, float exhaustion) {
      this(msgId, scaling, exhaustion, DamageEffects.HURT, DeathMessageType.DEFAULT);
   }

   public DamageType(String msgId, DamageScaling scaling, float exhaustion, DamageEffects effects) {
      this(msgId, scaling, exhaustion, effects, DeathMessageType.DEFAULT);
   }

   public DamageType(String msgId, float exhaustion, DamageEffects effects) {
      this(msgId, DamageScaling.WHEN_CAUSED_BY_LIVING_NON_PLAYER, exhaustion, effects);
   }

   public DamageType(String msgId, float exhaustion) {
      this(msgId, DamageScaling.WHEN_CAUSED_BY_LIVING_NON_PLAYER, exhaustion);
   }

   public DamageType(String string, DamageScaling arg, float f, DamageEffects arg2, DeathMessageType arg3) {
      this.msgId = string;
      this.scaling = arg;
      this.exhaustion = f;
      this.effects = arg2;
      this.deathMessageType = arg3;
   }

   public String msgId() {
      return this.msgId;
   }

   public DamageScaling scaling() {
      return this.scaling;
   }

   public float exhaustion() {
      return this.exhaustion;
   }

   public DamageEffects effects() {
      return this.effects;
   }

   public DeathMessageType deathMessageType() {
      return this.deathMessageType;
   }
}
