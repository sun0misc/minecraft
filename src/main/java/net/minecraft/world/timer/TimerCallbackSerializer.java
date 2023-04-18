package net.minecraft.world.timer;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Maps;
import com.mojang.logging.LogUtils;
import java.util.Map;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

public class TimerCallbackSerializer {
   private static final Logger LOGGER = LogUtils.getLogger();
   public static final TimerCallbackSerializer INSTANCE = (new TimerCallbackSerializer()).registerSerializer(new FunctionTimerCallback.Serializer()).registerSerializer(new FunctionTagTimerCallback.Serializer());
   private final Map serializersByType = Maps.newHashMap();
   private final Map serializersByClass = Maps.newHashMap();

   public TimerCallbackSerializer registerSerializer(TimerCallback.Serializer serializer) {
      this.serializersByType.put(serializer.getId(), serializer);
      this.serializersByClass.put(serializer.getCallbackClass(), serializer);
      return this;
   }

   private TimerCallback.Serializer getSerializer(Class clazz) {
      return (TimerCallback.Serializer)this.serializersByClass.get(clazz);
   }

   public NbtCompound serialize(TimerCallback callback) {
      TimerCallback.Serializer lv = this.getSerializer(callback.getClass());
      NbtCompound lv2 = new NbtCompound();
      lv.serialize(lv2, callback);
      lv2.putString("Type", lv.getId().toString());
      return lv2;
   }

   @Nullable
   public TimerCallback deserialize(NbtCompound nbt) {
      Identifier lv = Identifier.tryParse(nbt.getString("Type"));
      TimerCallback.Serializer lv2 = (TimerCallback.Serializer)this.serializersByType.get(lv);
      if (lv2 == null) {
         LOGGER.error("Failed to deserialize timer callback: {}", nbt);
         return null;
      } else {
         try {
            return lv2.deserialize(nbt);
         } catch (Exception var5) {
            LOGGER.error("Failed to deserialize timer callback: {}", nbt, var5);
            return null;
         }
      }
   }
}
