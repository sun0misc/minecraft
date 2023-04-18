package net.minecraft.util;

public class TypedActionResult {
   private final ActionResult result;
   private final Object value;

   public TypedActionResult(ActionResult result, Object value) {
      this.result = result;
      this.value = value;
   }

   public ActionResult getResult() {
      return this.result;
   }

   public Object getValue() {
      return this.value;
   }

   public static TypedActionResult success(Object data) {
      return new TypedActionResult(ActionResult.SUCCESS, data);
   }

   public static TypedActionResult consume(Object data) {
      return new TypedActionResult(ActionResult.CONSUME, data);
   }

   public static TypedActionResult pass(Object data) {
      return new TypedActionResult(ActionResult.PASS, data);
   }

   public static TypedActionResult fail(Object data) {
      return new TypedActionResult(ActionResult.FAIL, data);
   }

   public static TypedActionResult success(Object data, boolean swingHand) {
      return swingHand ? success(data) : consume(data);
   }
}
