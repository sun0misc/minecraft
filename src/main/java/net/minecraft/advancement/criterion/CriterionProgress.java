package net.minecraft.advancement.criterion;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSyntaxException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import net.minecraft.network.PacketByteBuf;
import org.jetbrains.annotations.Nullable;

public class CriterionProgress {
   private static final SimpleDateFormat FORMAT;
   @Nullable
   private Date obtainedDate;

   public boolean isObtained() {
      return this.obtainedDate != null;
   }

   public void obtain() {
      this.obtainedDate = new Date();
   }

   public void reset() {
      this.obtainedDate = null;
   }

   @Nullable
   public Date getObtainedDate() {
      return this.obtainedDate;
   }

   public String toString() {
      Object var10000 = this.obtainedDate == null ? "false" : this.obtainedDate;
      return "CriterionProgress{obtained=" + var10000 + "}";
   }

   public void toPacket(PacketByteBuf buf) {
      buf.writeNullable(this.obtainedDate, PacketByteBuf::writeDate);
   }

   public JsonElement toJson() {
      return (JsonElement)(this.obtainedDate != null ? new JsonPrimitive(FORMAT.format(this.obtainedDate)) : JsonNull.INSTANCE);
   }

   public static CriterionProgress fromPacket(PacketByteBuf buf) {
      CriterionProgress lv = new CriterionProgress();
      lv.obtainedDate = (Date)buf.readNullable(PacketByteBuf::readDate);
      return lv;
   }

   public static CriterionProgress obtainedAt(String datetime) {
      CriterionProgress lv = new CriterionProgress();

      try {
         lv.obtainedDate = FORMAT.parse(datetime);
         return lv;
      } catch (ParseException var3) {
         throw new JsonSyntaxException("Invalid datetime: " + datetime, var3);
      }
   }

   static {
      FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss Z", Locale.ROOT);
   }
}
