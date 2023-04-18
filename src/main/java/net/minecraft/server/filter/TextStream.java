package net.minecraft.server.filter;

import com.google.common.collect.ImmutableList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface TextStream {
   TextStream UNFILTERED = new TextStream() {
      public void onConnect() {
      }

      public void onDisconnect() {
      }

      public CompletableFuture filterText(String text) {
         return CompletableFuture.completedFuture(FilteredMessage.permitted(text));
      }

      public CompletableFuture filterTexts(List texts) {
         return CompletableFuture.completedFuture((List)texts.stream().map(FilteredMessage::permitted).collect(ImmutableList.toImmutableList()));
      }
   };

   void onConnect();

   void onDisconnect();

   CompletableFuture filterText(String text);

   CompletableFuture filterTexts(List texts);
}
