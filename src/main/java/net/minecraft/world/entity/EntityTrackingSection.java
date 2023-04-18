package net.minecraft.world.entity;

import com.mojang.logging.LogUtils;
import java.util.Collection;
import java.util.Iterator;
import java.util.stream.Stream;
import net.minecraft.util.TypeFilter;
import net.minecraft.util.annotation.Debug;
import net.minecraft.util.collection.TypeFilterableList;
import net.minecraft.util.function.LazyIterationConsumer;
import net.minecraft.util.math.Box;
import org.slf4j.Logger;

public class EntityTrackingSection {
   private static final Logger LOGGER = LogUtils.getLogger();
   private final TypeFilterableList collection;
   private EntityTrackingStatus status;

   public EntityTrackingSection(Class entityClass, EntityTrackingStatus status) {
      this.status = status;
      this.collection = new TypeFilterableList(entityClass);
   }

   public void add(EntityLike entity) {
      this.collection.add(entity);
   }

   public boolean remove(EntityLike entity) {
      return this.collection.remove(entity);
   }

   public LazyIterationConsumer.NextIteration forEach(Box box, LazyIterationConsumer consumer) {
      Iterator var3 = this.collection.iterator();

      EntityLike lv;
      do {
         if (!var3.hasNext()) {
            return LazyIterationConsumer.NextIteration.CONTINUE;
         }

         lv = (EntityLike)var3.next();
      } while(!lv.getBoundingBox().intersects(box) || !consumer.accept(lv).shouldAbort());

      return LazyIterationConsumer.NextIteration.ABORT;
   }

   public LazyIterationConsumer.NextIteration forEach(TypeFilter type, Box box, LazyIterationConsumer consumer) {
      Collection collection = this.collection.getAllOfType(type.getBaseClass());
      if (collection.isEmpty()) {
         return LazyIterationConsumer.NextIteration.CONTINUE;
      } else {
         Iterator var5 = collection.iterator();

         EntityLike lv;
         EntityLike lv2;
         do {
            if (!var5.hasNext()) {
               return LazyIterationConsumer.NextIteration.CONTINUE;
            }

            lv = (EntityLike)var5.next();
            lv2 = (EntityLike)type.downcast(lv);
         } while(lv2 == null || !lv.getBoundingBox().intersects(box) || !consumer.accept(lv2).shouldAbort());

         return LazyIterationConsumer.NextIteration.ABORT;
      }
   }

   public boolean isEmpty() {
      return this.collection.isEmpty();
   }

   public Stream stream() {
      return this.collection.stream();
   }

   public EntityTrackingStatus getStatus() {
      return this.status;
   }

   public EntityTrackingStatus swapStatus(EntityTrackingStatus status) {
      EntityTrackingStatus lv = this.status;
      this.status = status;
      return lv;
   }

   @Debug
   public int size() {
      return this.collection.size();
   }
}
