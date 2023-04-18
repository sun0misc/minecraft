package net.minecraft.client.report.log;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import java.util.ArrayList;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.jetbrains.annotations.Nullable;

@Environment(EnvType.CLIENT)
public class ChatLog {
   private final ChatLogEntry[] entries;
   private int currentIndex;

   public static Codec createCodec(int maxSize) {
      return Codec.list(ChatLogEntry.CODEC).comapFlatMap((entries) -> {
         int j = entries.size();
         return j > maxSize ? DataResult.error(() -> {
            return "Expected: a buffer of size less than or equal to " + maxSize + " but: " + j + " is greater than " + maxSize;
         }) : DataResult.success(new ChatLog(maxSize, entries));
      }, ChatLog::toList);
   }

   public ChatLog(int maxSize) {
      this.entries = new ChatLogEntry[maxSize];
   }

   private ChatLog(int size, List entries) {
      this.entries = (ChatLogEntry[])entries.toArray((currentIndex) -> {
         return new ChatLogEntry[size];
      });
      this.currentIndex = entries.size();
   }

   private List toList() {
      List list = new ArrayList(this.size());

      for(int i = this.getMinIndex(); i <= this.getMaxIndex(); ++i) {
         list.add(this.get(i));
      }

      return list;
   }

   public void add(ChatLogEntry entry) {
      this.entries[this.wrapIndex(this.currentIndex++)] = entry;
   }

   @Nullable
   public ChatLogEntry get(int index) {
      return index >= this.getMinIndex() && index <= this.getMaxIndex() ? this.entries[this.wrapIndex(index)] : null;
   }

   private int wrapIndex(int index) {
      return index % this.entries.length;
   }

   public int getMinIndex() {
      return Math.max(this.currentIndex - this.entries.length, 0);
   }

   public int getMaxIndex() {
      return this.currentIndex - 1;
   }

   private int size() {
      return this.getMaxIndex() - this.getMinIndex() + 1;
   }
}
