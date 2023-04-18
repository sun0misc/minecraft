package net.minecraft.network.message;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;
import java.util.Optional;
import org.jetbrains.annotations.Nullable;

public class AcknowledgmentValidator {
   private final int size;
   private final ObjectList messages = new ObjectArrayList();
   @Nullable
   private MessageSignatureData lastSignature;

   public AcknowledgmentValidator(int size) {
      this.size = size;

      for(int j = 0; j < size; ++j) {
         this.messages.add((Object)null);
      }

   }

   public void addPending(MessageSignatureData signature) {
      if (!signature.equals(this.lastSignature)) {
         this.messages.add(new AcknowledgedMessage(signature, true));
         this.lastSignature = signature;
      }

   }

   public int getMessageCount() {
      return this.messages.size();
   }

   public boolean removeUntil(int index) {
      int j = this.messages.size() - this.size;
      if (index >= 0 && index <= j) {
         this.messages.removeElements(0, index);
         return true;
      } else {
         return false;
      }
   }

   public Optional validate(LastSeenMessageList.Acknowledgment acknowledgment) {
      if (!this.removeUntil(acknowledgment.offset())) {
         return Optional.empty();
      } else {
         ObjectList objectList = new ObjectArrayList(acknowledgment.acknowledged().cardinality());
         if (acknowledgment.acknowledged().length() > this.size) {
            return Optional.empty();
         } else {
            for(int i = 0; i < this.size; ++i) {
               boolean bl = acknowledgment.acknowledged().get(i);
               AcknowledgedMessage lv = (AcknowledgedMessage)this.messages.get(i);
               if (bl) {
                  if (lv == null) {
                     return Optional.empty();
                  }

                  this.messages.set(i, lv.unmarkAsPending());
                  objectList.add(lv.signature());
               } else {
                  if (lv != null && !lv.pending()) {
                     return Optional.empty();
                  }

                  this.messages.set(i, (Object)null);
               }
            }

            return Optional.of(new LastSeenMessageList(objectList));
         }
      }
   }
}
