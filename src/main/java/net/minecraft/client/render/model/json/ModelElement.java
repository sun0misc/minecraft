package net.minecraft.client.render.model.json;

import com.google.common.collect.Maps;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import java.lang.reflect.Type;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

@Environment(EnvType.CLIENT)
public class ModelElement {
   private static final boolean field_32785 = false;
   private static final float field_32786 = -16.0F;
   private static final float field_32787 = 32.0F;
   public final Vector3f from;
   public final Vector3f to;
   public final Map faces;
   public final ModelRotation rotation;
   public final boolean shade;

   public ModelElement(Vector3f from, Vector3f to, Map faces, @Nullable ModelRotation rotation, boolean shade) {
      this.from = from;
      this.to = to;
      this.faces = faces;
      this.rotation = rotation;
      this.shade = shade;
      this.initTextures();
   }

   private void initTextures() {
      Iterator var1 = this.faces.entrySet().iterator();

      while(var1.hasNext()) {
         Map.Entry entry = (Map.Entry)var1.next();
         float[] fs = this.getRotatedMatrix((Direction)entry.getKey());
         ((ModelElementFace)entry.getValue()).textureData.setUvs(fs);
      }

   }

   private float[] getRotatedMatrix(Direction direction) {
      switch (direction) {
         case DOWN:
            return new float[]{this.from.x(), 16.0F - this.to.z(), this.to.x(), 16.0F - this.from.z()};
         case UP:
            return new float[]{this.from.x(), this.from.z(), this.to.x(), this.to.z()};
         case NORTH:
         default:
            return new float[]{16.0F - this.to.x(), 16.0F - this.to.y(), 16.0F - this.from.x(), 16.0F - this.from.y()};
         case SOUTH:
            return new float[]{this.from.x(), 16.0F - this.to.y(), this.to.x(), 16.0F - this.from.y()};
         case WEST:
            return new float[]{this.from.z(), 16.0F - this.to.y(), this.to.z(), 16.0F - this.from.y()};
         case EAST:
            return new float[]{16.0F - this.to.z(), 16.0F - this.to.y(), 16.0F - this.from.z(), 16.0F - this.from.y()};
      }
   }

   @Environment(EnvType.CLIENT)
   protected static class Deserializer implements JsonDeserializer {
      private static final boolean DEFAULT_SHADE = true;

      public ModelElement deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
         JsonObject jsonObject = jsonElement.getAsJsonObject();
         Vector3f vector3f = this.deserializeFrom(jsonObject);
         Vector3f vector3f2 = this.deserializeTo(jsonObject);
         ModelRotation lv = this.deserializeRotation(jsonObject);
         Map map = this.deserializeFacesValidating(jsonDeserializationContext, jsonObject);
         if (jsonObject.has("shade") && !JsonHelper.hasBoolean(jsonObject, "shade")) {
            throw new JsonParseException("Expected shade to be a Boolean");
         } else {
            boolean bl = JsonHelper.getBoolean(jsonObject, "shade", true);
            return new ModelElement(vector3f, vector3f2, map, lv, bl);
         }
      }

      @Nullable
      private ModelRotation deserializeRotation(JsonObject object) {
         ModelRotation lv = null;
         if (object.has("rotation")) {
            JsonObject jsonObject2 = JsonHelper.getObject(object, "rotation");
            Vector3f vector3f = this.deserializeVec3f(jsonObject2, "origin");
            vector3f.mul(0.0625F);
            Direction.Axis lv2 = this.deserializeAxis(jsonObject2);
            float f = this.deserializeRotationAngle(jsonObject2);
            boolean bl = JsonHelper.getBoolean(jsonObject2, "rescale", false);
            lv = new ModelRotation(vector3f, lv2, f, bl);
         }

         return lv;
      }

      private float deserializeRotationAngle(JsonObject object) {
         float f = JsonHelper.getFloat(object, "angle");
         if (f != 0.0F && MathHelper.abs(f) != 22.5F && MathHelper.abs(f) != 45.0F) {
            throw new JsonParseException("Invalid rotation " + f + " found, only -45/-22.5/0/22.5/45 allowed");
         } else {
            return f;
         }
      }

      private Direction.Axis deserializeAxis(JsonObject object) {
         String string = JsonHelper.getString(object, "axis");
         Direction.Axis lv = Direction.Axis.fromName(string.toLowerCase(Locale.ROOT));
         if (lv == null) {
            throw new JsonParseException("Invalid rotation axis: " + string);
         } else {
            return lv;
         }
      }

      private Map deserializeFacesValidating(JsonDeserializationContext context, JsonObject object) {
         Map map = this.deserializeFaces(context, object);
         if (map.isEmpty()) {
            throw new JsonParseException("Expected between 1 and 6 unique faces, got 0");
         } else {
            return map;
         }
      }

      private Map deserializeFaces(JsonDeserializationContext context, JsonObject object) {
         Map map = Maps.newEnumMap(Direction.class);
         JsonObject jsonObject2 = JsonHelper.getObject(object, "faces");
         Iterator var5 = jsonObject2.entrySet().iterator();

         while(var5.hasNext()) {
            Map.Entry entry = (Map.Entry)var5.next();
            Direction lv = this.getDirection((String)entry.getKey());
            map.put(lv, (ModelElementFace)context.deserialize((JsonElement)entry.getValue(), ModelElementFace.class));
         }

         return map;
      }

      private Direction getDirection(String name) {
         Direction lv = Direction.byName(name);
         if (lv == null) {
            throw new JsonParseException("Unknown facing: " + name);
         } else {
            return lv;
         }
      }

      private Vector3f deserializeTo(JsonObject object) {
         Vector3f vector3f = this.deserializeVec3f(object, "to");
         if (!(vector3f.x() < -16.0F) && !(vector3f.y() < -16.0F) && !(vector3f.z() < -16.0F) && !(vector3f.x() > 32.0F) && !(vector3f.y() > 32.0F) && !(vector3f.z() > 32.0F)) {
            return vector3f;
         } else {
            throw new JsonParseException("'to' specifier exceeds the allowed boundaries: " + vector3f);
         }
      }

      private Vector3f deserializeFrom(JsonObject object) {
         Vector3f vector3f = this.deserializeVec3f(object, "from");
         if (!(vector3f.x() < -16.0F) && !(vector3f.y() < -16.0F) && !(vector3f.z() < -16.0F) && !(vector3f.x() > 32.0F) && !(vector3f.y() > 32.0F) && !(vector3f.z() > 32.0F)) {
            return vector3f;
         } else {
            throw new JsonParseException("'from' specifier exceeds the allowed boundaries: " + vector3f);
         }
      }

      private Vector3f deserializeVec3f(JsonObject object, String name) {
         JsonArray jsonArray = JsonHelper.getArray(object, name);
         if (jsonArray.size() != 3) {
            throw new JsonParseException("Expected 3 " + name + " values, found: " + jsonArray.size());
         } else {
            float[] fs = new float[3];

            for(int i = 0; i < fs.length; ++i) {
               fs[i] = JsonHelper.asFloat(jsonArray.get(i), name + "[" + i + "]");
            }

            return new Vector3f(fs[0], fs[1], fs[2]);
         }
      }

      // $FF: synthetic method
      public Object deserialize(JsonElement json, Type type, JsonDeserializationContext context) throws JsonParseException {
         return this.deserialize(json, type, context);
      }
   }
}
