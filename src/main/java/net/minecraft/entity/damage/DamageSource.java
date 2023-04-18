package net.minecraft.entity.damage;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

public class DamageSource {
   private final RegistryEntry type;
   @Nullable
   private final Entity attacker;
   @Nullable
   private final Entity source;
   @Nullable
   private final Vec3d position;

   public String toString() {
      return "DamageSource (" + this.getType().msgId() + ")";
   }

   public float getExhaustion() {
      return this.getType().exhaustion();
   }

   public boolean isIndirect() {
      return this.attacker != this.source;
   }

   private DamageSource(RegistryEntry type, @Nullable Entity source, @Nullable Entity attacker, @Nullable Vec3d position) {
      this.type = type;
      this.attacker = attacker;
      this.source = source;
      this.position = position;
   }

   public DamageSource(RegistryEntry type, @Nullable Entity source, @Nullable Entity attacker) {
      this(type, source, attacker, (Vec3d)null);
   }

   public DamageSource(RegistryEntry type, Vec3d position) {
      this(type, (Entity)null, (Entity)null, position);
   }

   public DamageSource(RegistryEntry type, @Nullable Entity attacker) {
      this(type, attacker, attacker);
   }

   public DamageSource(RegistryEntry type) {
      this(type, (Entity)null, (Entity)null, (Vec3d)null);
   }

   @Nullable
   public Entity getSource() {
      return this.source;
   }

   @Nullable
   public Entity getAttacker() {
      return this.attacker;
   }

   public Text getDeathMessage(LivingEntity killed) {
      String string = "death.attack." + this.getType().msgId();
      if (this.attacker == null && this.source == null) {
         LivingEntity lv4 = killed.getPrimeAdversary();
         String string2 = string + ".player";
         return lv4 != null ? Text.translatable(string2, killed.getDisplayName(), lv4.getDisplayName()) : Text.translatable(string, killed.getDisplayName());
      } else {
         Text lv = this.attacker == null ? this.source.getDisplayName() : this.attacker.getDisplayName();
         Entity var6 = this.attacker;
         ItemStack var10000;
         if (var6 instanceof LivingEntity) {
            LivingEntity lv2 = (LivingEntity)var6;
            var10000 = lv2.getMainHandStack();
         } else {
            var10000 = ItemStack.EMPTY;
         }

         ItemStack lv3 = var10000;
         return !lv3.isEmpty() && lv3.hasCustomName() ? Text.translatable(string + ".item", killed.getDisplayName(), lv, lv3.toHoverableText()) : Text.translatable(string, killed.getDisplayName(), lv);
      }
   }

   public String getName() {
      return this.getType().msgId();
   }

   public boolean isScaledWithDifficulty() {
      boolean var10000;
      switch (this.getType().scaling()) {
         case NEVER:
            var10000 = false;
            break;
         case WHEN_CAUSED_BY_LIVING_NON_PLAYER:
            var10000 = this.attacker instanceof LivingEntity && !(this.attacker instanceof PlayerEntity);
            break;
         case ALWAYS:
            var10000 = true;
            break;
         default:
            throw new IncompatibleClassChangeError();
      }

      return var10000;
   }

   public boolean isSourceCreativePlayer() {
      Entity var2 = this.getAttacker();
      boolean var10000;
      if (var2 instanceof PlayerEntity lv) {
         if (lv.getAbilities().creativeMode) {
            var10000 = true;
            return var10000;
         }
      }

      var10000 = false;
      return var10000;
   }

   @Nullable
   public Vec3d getPosition() {
      if (this.position != null) {
         return this.position;
      } else {
         return this.attacker != null ? this.attacker.getPos() : null;
      }
   }

   @Nullable
   public Vec3d getStoredPosition() {
      return this.position;
   }

   public boolean isIn(TagKey tag) {
      return this.type.isIn(tag);
   }

   public boolean isOf(RegistryKey typeKey) {
      return this.type.matchesKey(typeKey);
   }

   public DamageType getType() {
      return (DamageType)this.type.value();
   }

   public RegistryEntry getTypeRegistryEntry() {
      return this.type;
   }
}
