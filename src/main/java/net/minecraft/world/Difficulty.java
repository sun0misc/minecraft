package net.minecraft.world;

import java.util.function.IntFunction;
import net.minecraft.text.Text;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.function.ValueLists;
import org.jetbrains.annotations.Nullable;

public enum Difficulty implements StringIdentifiable {
   PEACEFUL(0, "peaceful"),
   EASY(1, "easy"),
   NORMAL(2, "normal"),
   HARD(3, "hard");

   public static final StringIdentifiable.Codec CODEC = StringIdentifiable.createCodec(Difficulty::values);
   private static final IntFunction BY_ID = ValueLists.createIdToValueFunction(Difficulty::getId, values(), (ValueLists.OutOfBoundsHandling)ValueLists.OutOfBoundsHandling.WRAP);
   private final int id;
   private final String name;

   private Difficulty(int id, String name) {
      this.id = id;
      this.name = name;
   }

   public int getId() {
      return this.id;
   }

   public Text getTranslatableName() {
      return Text.translatable("options.difficulty." + this.name);
   }

   public Text getInfo() {
      return Text.translatable("options.difficulty." + this.name + ".info");
   }

   public static Difficulty byId(int id) {
      return (Difficulty)BY_ID.apply(id);
   }

   @Nullable
   public static Difficulty byName(String name) {
      return (Difficulty)CODEC.byId(name);
   }

   public String getName() {
      return this.name;
   }

   public String asString() {
      return this.name;
   }

   // $FF: synthetic method
   private static Difficulty[] method_36597() {
      return new Difficulty[]{PEACEFUL, EASY, NORMAL, HARD};
   }
}
