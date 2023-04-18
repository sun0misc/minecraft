package net.minecraft.advancement;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.minecraft.advancement.criterion.CriterionProgress;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.JsonHelper;
import org.jetbrains.annotations.Nullable;

public class AdvancementProgress implements Comparable {
   final Map criteriaProgresses;
   private String[][] requirements = new String[0][];

   private AdvancementProgress(Map criteriaProgresses) {
      this.criteriaProgresses = criteriaProgresses;
   }

   public AdvancementProgress() {
      this.criteriaProgresses = Maps.newHashMap();
   }

   public void init(Map criteria, String[][] requirements) {
      Set set = criteria.keySet();
      this.criteriaProgresses.entrySet().removeIf((progress) -> {
         return !set.contains(progress.getKey());
      });
      Iterator var4 = set.iterator();

      while(var4.hasNext()) {
         String string = (String)var4.next();
         if (!this.criteriaProgresses.containsKey(string)) {
            this.criteriaProgresses.put(string, new CriterionProgress());
         }
      }

      this.requirements = requirements;
   }

   public boolean isDone() {
      if (this.requirements.length == 0) {
         return false;
      } else {
         String[][] var1 = this.requirements;
         int var2 = var1.length;

         for(int var3 = 0; var3 < var2; ++var3) {
            String[] strings = var1[var3];
            boolean bl = false;
            String[] var6 = strings;
            int var7 = strings.length;

            for(int var8 = 0; var8 < var7; ++var8) {
               String string = var6[var8];
               CriterionProgress lv = this.getCriterionProgress(string);
               if (lv != null && lv.isObtained()) {
                  bl = true;
                  break;
               }
            }

            if (!bl) {
               return false;
            }
         }

         return true;
      }
   }

   public boolean isAnyObtained() {
      Iterator var1 = this.criteriaProgresses.values().iterator();

      CriterionProgress lv;
      do {
         if (!var1.hasNext()) {
            return false;
         }

         lv = (CriterionProgress)var1.next();
      } while(!lv.isObtained());

      return true;
   }

   public boolean obtain(String name) {
      CriterionProgress lv = (CriterionProgress)this.criteriaProgresses.get(name);
      if (lv != null && !lv.isObtained()) {
         lv.obtain();
         return true;
      } else {
         return false;
      }
   }

   public boolean reset(String name) {
      CriterionProgress lv = (CriterionProgress)this.criteriaProgresses.get(name);
      if (lv != null && lv.isObtained()) {
         lv.reset();
         return true;
      } else {
         return false;
      }
   }

   public String toString() {
      Map var10000 = this.criteriaProgresses;
      return "AdvancementProgress{criteria=" + var10000 + ", requirements=" + Arrays.deepToString(this.requirements) + "}";
   }

   public void toPacket(PacketByteBuf buf) {
      buf.writeMap(this.criteriaProgresses, PacketByteBuf::writeString, (bufx, progresses) -> {
         progresses.toPacket(bufx);
      });
   }

   public static AdvancementProgress fromPacket(PacketByteBuf buf) {
      Map map = buf.readMap(PacketByteBuf::readString, CriterionProgress::fromPacket);
      return new AdvancementProgress(map);
   }

   @Nullable
   public CriterionProgress getCriterionProgress(String name) {
      return (CriterionProgress)this.criteriaProgresses.get(name);
   }

   public float getProgressBarPercentage() {
      if (this.criteriaProgresses.isEmpty()) {
         return 0.0F;
      } else {
         float f = (float)this.requirements.length;
         float g = (float)this.countObtainedRequirements();
         return g / f;
      }
   }

   @Nullable
   public String getProgressBarFraction() {
      if (this.criteriaProgresses.isEmpty()) {
         return null;
      } else {
         int i = this.requirements.length;
         if (i <= 1) {
            return null;
         } else {
            int j = this.countObtainedRequirements();
            return "" + j + "/" + i;
         }
      }
   }

   private int countObtainedRequirements() {
      int i = 0;
      String[][] var2 = this.requirements;
      int var3 = var2.length;

      for(int var4 = 0; var4 < var3; ++var4) {
         String[] strings = var2[var4];
         boolean bl = false;
         String[] var7 = strings;
         int var8 = strings.length;

         for(int var9 = 0; var9 < var8; ++var9) {
            String string = var7[var9];
            CriterionProgress lv = this.getCriterionProgress(string);
            if (lv != null && lv.isObtained()) {
               bl = true;
               break;
            }
         }

         if (bl) {
            ++i;
         }
      }

      return i;
   }

   public Iterable getUnobtainedCriteria() {
      List list = Lists.newArrayList();
      Iterator var2 = this.criteriaProgresses.entrySet().iterator();

      while(var2.hasNext()) {
         Map.Entry entry = (Map.Entry)var2.next();
         if (!((CriterionProgress)entry.getValue()).isObtained()) {
            list.add((String)entry.getKey());
         }
      }

      return list;
   }

   public Iterable getObtainedCriteria() {
      List list = Lists.newArrayList();
      Iterator var2 = this.criteriaProgresses.entrySet().iterator();

      while(var2.hasNext()) {
         Map.Entry entry = (Map.Entry)var2.next();
         if (((CriterionProgress)entry.getValue()).isObtained()) {
            list.add((String)entry.getKey());
         }
      }

      return list;
   }

   @Nullable
   public Date getEarliestProgressObtainDate() {
      Date date = null;
      Iterator var2 = this.criteriaProgresses.values().iterator();

      while(true) {
         CriterionProgress lv;
         do {
            do {
               if (!var2.hasNext()) {
                  return date;
               }

               lv = (CriterionProgress)var2.next();
            } while(!lv.isObtained());
         } while(date != null && !lv.getObtainedDate().before(date));

         date = lv.getObtainedDate();
      }
   }

   public int compareTo(AdvancementProgress arg) {
      Date date = this.getEarliestProgressObtainDate();
      Date date2 = arg.getEarliestProgressObtainDate();
      if (date == null && date2 != null) {
         return 1;
      } else if (date != null && date2 == null) {
         return -1;
      } else {
         return date == null && date2 == null ? 0 : date.compareTo(date2);
      }
   }

   // $FF: synthetic method
   public int compareTo(Object other) {
      return this.compareTo((AdvancementProgress)other);
   }

   public static class Serializer implements JsonDeserializer, JsonSerializer {
      public JsonElement serialize(AdvancementProgress arg, Type type, JsonSerializationContext jsonSerializationContext) {
         JsonObject jsonObject = new JsonObject();
         JsonObject jsonObject2 = new JsonObject();
         Iterator var6 = arg.criteriaProgresses.entrySet().iterator();

         while(var6.hasNext()) {
            Map.Entry entry = (Map.Entry)var6.next();
            CriterionProgress lv = (CriterionProgress)entry.getValue();
            if (lv.isObtained()) {
               jsonObject2.add((String)entry.getKey(), lv.toJson());
            }
         }

         if (!jsonObject2.entrySet().isEmpty()) {
            jsonObject.add("criteria", jsonObject2);
         }

         jsonObject.addProperty("done", arg.isDone());
         return jsonObject;
      }

      public AdvancementProgress deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
         JsonObject jsonObject = JsonHelper.asObject(jsonElement, "advancement");
         JsonObject jsonObject2 = JsonHelper.getObject(jsonObject, "criteria", new JsonObject());
         AdvancementProgress lv = new AdvancementProgress();
         Iterator var7 = jsonObject2.entrySet().iterator();

         while(var7.hasNext()) {
            Map.Entry entry = (Map.Entry)var7.next();
            String string = (String)entry.getKey();
            lv.criteriaProgresses.put(string, CriterionProgress.obtainedAt(JsonHelper.asString((JsonElement)entry.getValue(), string)));
         }

         return lv;
      }

      // $FF: synthetic method
      public Object deserialize(JsonElement functionJson, Type unused, JsonDeserializationContext context) throws JsonParseException {
         return this.deserialize(functionJson, unused, context);
      }

      // $FF: synthetic method
      public JsonElement serialize(Object entry, Type unused, JsonSerializationContext context) {
         return this.serialize((AdvancementProgress)entry, unused, context);
      }
   }
}
