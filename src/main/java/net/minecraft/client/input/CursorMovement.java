package net.minecraft.client.input;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public enum CursorMovement {
   ABSOLUTE,
   RELATIVE,
   END;

   // $FF: synthetic method
   private static CursorMovement[] method_44446() {
      return new CursorMovement[]{ABSOLUTE, RELATIVE, END};
   }
}
