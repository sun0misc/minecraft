package net.minecraft.entity.attribute;

import com.mojang.logging.LogUtils;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Supplier;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.random.Random;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

public class EntityAttributeModifier {
   private static final Logger LOGGER = LogUtils.getLogger();
   private final double value;
   private final Operation operation;
   private final Supplier nameGetter;
   private final UUID uuid;

   public EntityAttributeModifier(String name, double value, Operation operation) {
      this(MathHelper.randomUuid(Random.createLocal()), () -> {
         return name;
      }, value, operation);
   }

   public EntityAttributeModifier(UUID uuid, String name, double value, Operation operation) {
      this(uuid, () -> {
         return name;
      }, value, operation);
   }

   public EntityAttributeModifier(UUID uuid, Supplier nameGetter, double value, Operation operation) {
      this.uuid = uuid;
      this.nameGetter = nameGetter;
      this.value = value;
      this.operation = operation;
   }

   public UUID getId() {
      return this.uuid;
   }

   public String getName() {
      return (String)this.nameGetter.get();
   }

   public Operation getOperation() {
      return this.operation;
   }

   public double getValue() {
      return this.value;
   }

   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else if (o != null && this.getClass() == o.getClass()) {
         EntityAttributeModifier lv = (EntityAttributeModifier)o;
         return Objects.equals(this.uuid, lv.uuid);
      } else {
         return false;
      }
   }

   public int hashCode() {
      return this.uuid.hashCode();
   }

   public String toString() {
      double var10000 = this.value;
      return "AttributeModifier{amount=" + var10000 + ", operation=" + this.operation + ", name='" + (String)this.nameGetter.get() + "', id=" + this.uuid + "}";
   }

   public NbtCompound toNbt() {
      NbtCompound lv = new NbtCompound();
      lv.putString("Name", this.getName());
      lv.putDouble("Amount", this.value);
      lv.putInt("Operation", this.operation.getId());
      lv.putUuid("UUID", this.uuid);
      return lv;
   }

   @Nullable
   public static EntityAttributeModifier fromNbt(NbtCompound nbt) {
      try {
         UUID uUID = nbt.getUuid("UUID");
         Operation lv = EntityAttributeModifier.Operation.fromId(nbt.getInt("Operation"));
         return new EntityAttributeModifier(uUID, nbt.getString("Name"), nbt.getDouble("Amount"), lv);
      } catch (Exception var3) {
         LOGGER.warn("Unable to create attribute: {}", var3.getMessage());
         return null;
      }
   }

   public static enum Operation {
      ADDITION(0),
      MULTIPLY_BASE(1),
      MULTIPLY_TOTAL(2);

      private static final Operation[] VALUES = new Operation[]{ADDITION, MULTIPLY_BASE, MULTIPLY_TOTAL};
      private final int id;

      private Operation(int id) {
         this.id = id;
      }

      public int getId() {
         return this.id;
      }

      public static Operation fromId(int id) {
         if (id >= 0 && id < VALUES.length) {
            return VALUES[id];
         } else {
            throw new IllegalArgumentException("No operation with value " + id);
         }
      }

      // $FF: synthetic method
      private static Operation[] method_36614() {
         return new Operation[]{ADDITION, MULTIPLY_BASE, MULTIPLY_TOTAL};
      }
   }
}
