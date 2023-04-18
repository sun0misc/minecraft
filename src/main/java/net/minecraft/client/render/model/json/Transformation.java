package net.minecraft.client.render.model.json;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import java.lang.reflect.Type;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.math.MathHelper;
import org.joml.Quaternionf;
import org.joml.Vector3f;

@Environment(EnvType.CLIENT)
public class Transformation {
   public static final Transformation IDENTITY = new Transformation(new Vector3f(), new Vector3f(), new Vector3f(1.0F, 1.0F, 1.0F));
   public final Vector3f rotation;
   public final Vector3f translation;
   public final Vector3f scale;

   public Transformation(Vector3f rotation, Vector3f translation, Vector3f scale) {
      this.rotation = new Vector3f(rotation);
      this.translation = new Vector3f(translation);
      this.scale = new Vector3f(scale);
   }

   public void apply(boolean leftHanded, MatrixStack matrices) {
      if (this != IDENTITY) {
         float f = this.rotation.x();
         float g = this.rotation.y();
         float h = this.rotation.z();
         if (leftHanded) {
            g = -g;
            h = -h;
         }

         int i = leftHanded ? -1 : 1;
         matrices.translate((float)i * this.translation.x(), this.translation.y(), this.translation.z());
         matrices.multiply((new Quaternionf()).rotationXYZ(f * 0.017453292F, g * 0.017453292F, h * 0.017453292F));
         matrices.scale(this.scale.x(), this.scale.y(), this.scale.z());
      }
   }

   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else if (this.getClass() != o.getClass()) {
         return false;
      } else {
         Transformation lv = (Transformation)o;
         return this.rotation.equals(lv.rotation) && this.scale.equals(lv.scale) && this.translation.equals(lv.translation);
      }
   }

   public int hashCode() {
      int i = this.rotation.hashCode();
      i = 31 * i + this.translation.hashCode();
      i = 31 * i + this.scale.hashCode();
      return i;
   }

   @Environment(EnvType.CLIENT)
   protected static class Deserializer implements JsonDeserializer {
      private static final Vector3f DEFAULT_ROTATION = new Vector3f(0.0F, 0.0F, 0.0F);
      private static final Vector3f DEFAULT_TRANSLATION = new Vector3f(0.0F, 0.0F, 0.0F);
      private static final Vector3f DEFAULT_SCALE = new Vector3f(1.0F, 1.0F, 1.0F);
      public static final float field_32808 = 5.0F;
      public static final float field_32809 = 4.0F;

      public Transformation deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
         JsonObject jsonObject = jsonElement.getAsJsonObject();
         Vector3f vector3f = this.parseVector3f(jsonObject, "rotation", DEFAULT_ROTATION);
         Vector3f vector3f2 = this.parseVector3f(jsonObject, "translation", DEFAULT_TRANSLATION);
         vector3f2.mul(0.0625F);
         vector3f2.set(MathHelper.clamp(vector3f2.x, -5.0F, 5.0F), MathHelper.clamp(vector3f2.y, -5.0F, 5.0F), MathHelper.clamp(vector3f2.z, -5.0F, 5.0F));
         Vector3f vector3f3 = this.parseVector3f(jsonObject, "scale", DEFAULT_SCALE);
         vector3f3.set(MathHelper.clamp(vector3f3.x, -4.0F, 4.0F), MathHelper.clamp(vector3f3.y, -4.0F, 4.0F), MathHelper.clamp(vector3f3.z, -4.0F, 4.0F));
         return new Transformation(vector3f, vector3f2, vector3f3);
      }

      private Vector3f parseVector3f(JsonObject json, String key, Vector3f fallback) {
         if (!json.has(key)) {
            return fallback;
         } else {
            JsonArray jsonArray = JsonHelper.getArray(json, key);
            if (jsonArray.size() != 3) {
               throw new JsonParseException("Expected 3 " + key + " values, found: " + jsonArray.size());
            } else {
               float[] fs = new float[3];

               for(int i = 0; i < fs.length; ++i) {
                  fs[i] = JsonHelper.asFloat(jsonArray.get(i), key + "[" + i + "]");
               }

               return new Vector3f(fs[0], fs[1], fs[2]);
            }
         }
      }

      // $FF: synthetic method
      public Object deserialize(JsonElement functionJson, Type unused, JsonDeserializationContext context) throws JsonParseException {
         return this.deserialize(functionJson, unused, context);
      }
   }
}
