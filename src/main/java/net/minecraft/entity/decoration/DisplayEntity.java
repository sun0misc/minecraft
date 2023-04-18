package net.minecraft.entity.decoration;

import com.mojang.datafixers.util.Pair;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import it.unimi.dsi.fastutil.ints.IntSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.IntFunction;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.piston.PistonBehavior;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.inventory.StackReference;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;
import net.minecraft.text.Texts;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.Util;
import net.minecraft.util.function.ValueLists;
import net.minecraft.util.math.AffineTransformation;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.ColorHelper;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.slf4j.Logger;

public abstract class DisplayEntity extends Entity {
   static final Logger field_42397 = LogUtils.getLogger();
   public static final int field_42384 = -1;
   private static final TrackedData START_INTERPOLATION;
   private static final TrackedData INTERPOLATION_DURATION;
   private static final TrackedData TRANSLATION;
   private static final TrackedData SCALE;
   private static final TrackedData LEFT_ROTATION;
   private static final TrackedData RIGHT_ROTATION;
   private static final TrackedData BILLBOARD;
   private static final TrackedData BRIGHTNESS;
   private static final TrackedData VIEW_RANGE;
   private static final TrackedData SHADOW_RADIUS;
   private static final TrackedData SHADOW_STRENGTH;
   private static final TrackedData WIDTH;
   private static final TrackedData HEIGHT;
   private static final TrackedData GLOW_COLOR_OVERRIDE;
   private static final IntSet RENDERING_DATA_IDS;
   private static final float field_42376 = 0.0F;
   private static final float field_42377 = 1.0F;
   private static final int field_42378 = -1;
   public static final String INTERPOLATION_DURATION_NBT_KEY = "interpolation_duration";
   public static final String START_INTERPOLATION_KEY = "start_interpolation";
   public static final String TRANSFORMATION_NBT_KEY = "transformation";
   public static final String BILLBOARD_NBT_KEY = "billboard";
   public static final String BRIGHTNESS_NBT_KEY = "brightness";
   public static final String VIEW_RANGE_NBT_KEY = "view_range";
   public static final String SHADOW_RADIUS_NBT_KEY = "shadow_radius";
   public static final String SHADOW_STRENGTH_NBT_KEY = "shadow_strength";
   public static final String WIDTH_NBT_KEY = "width";
   public static final String HEIGHT_NBT_KEY = "height";
   public static final String GLOW_COLOR_OVERRIDE_NBT_KEY = "glow_color_override";
   private final Quaternionf fixedRotation = new Quaternionf();
   private long interpolationStart = -2147483648L;
   private int interpolationDuration;
   private float lerpProgress;
   private Box visibilityBoundingBox;
   protected boolean renderingDataSet;
   private boolean startInterpolationSet;
   private boolean interpolationDurationSet;
   @Nullable
   private RenderState renderState;

   public DisplayEntity(EntityType arg, World arg2) {
      super(arg, arg2);
      this.noClip = true;
      this.ignoreCameraFrustum = true;
      this.visibilityBoundingBox = this.getBoundingBox();
   }

   public void onTrackedDataSet(TrackedData data) {
      super.onTrackedDataSet(data);
      if (HEIGHT.equals(data) || WIDTH.equals(data)) {
         this.updateVisibilityBoundingBox();
      }

      if (START_INTERPOLATION.equals(data)) {
         this.startInterpolationSet = true;
      }

      if (INTERPOLATION_DURATION.equals(data)) {
         this.interpolationDurationSet = true;
      }

      if (RENDERING_DATA_IDS.contains(data.getId())) {
         this.renderingDataSet = true;
      }

   }

   private static AffineTransformation getTransformation(DataTracker dataTracker) {
      Vector3f vector3f = (Vector3f)dataTracker.get(TRANSLATION);
      Quaternionf quaternionf = (Quaternionf)dataTracker.get(LEFT_ROTATION);
      Vector3f vector3f2 = (Vector3f)dataTracker.get(SCALE);
      Quaternionf quaternionf2 = (Quaternionf)dataTracker.get(RIGHT_ROTATION);
      return new AffineTransformation(vector3f, quaternionf, vector3f2, quaternionf2);
   }

   public void tick() {
      Entity lv = this.getVehicle();
      if (lv != null && lv.isRemoved()) {
         this.stopRiding();
      }

      if (this.world.isClient) {
         if (this.startInterpolationSet) {
            this.startInterpolationSet = false;
            int i = this.getStartInterpolation();
            this.interpolationStart = (long)(this.age + i);
         }

         if (this.interpolationDurationSet) {
            this.interpolationDurationSet = false;
            this.interpolationDuration = this.getInterpolationDuration();
         }

         if (this.renderingDataSet) {
            this.renderingDataSet = false;
            boolean bl = this.interpolationDuration != 0;
            if (bl && this.renderState != null) {
               this.renderState = this.getLerpedRenderState(this.renderState, this.lerpProgress);
            } else {
               this.renderState = this.copyRenderState();
            }

            this.refreshData(bl, this.lerpProgress);
         }
      }

   }

   protected abstract void refreshData(boolean shouldLerp, float lerpProgress);

   protected void initDataTracker() {
      this.dataTracker.startTracking(START_INTERPOLATION, 0);
      this.dataTracker.startTracking(INTERPOLATION_DURATION, 0);
      this.dataTracker.startTracking(TRANSLATION, new Vector3f());
      this.dataTracker.startTracking(SCALE, new Vector3f(1.0F, 1.0F, 1.0F));
      this.dataTracker.startTracking(RIGHT_ROTATION, new Quaternionf());
      this.dataTracker.startTracking(LEFT_ROTATION, new Quaternionf());
      this.dataTracker.startTracking(BILLBOARD, DisplayEntity.BillboardMode.FIXED.getIndex());
      this.dataTracker.startTracking(BRIGHTNESS, -1);
      this.dataTracker.startTracking(VIEW_RANGE, 1.0F);
      this.dataTracker.startTracking(SHADOW_RADIUS, 0.0F);
      this.dataTracker.startTracking(SHADOW_STRENGTH, 1.0F);
      this.dataTracker.startTracking(WIDTH, 0.0F);
      this.dataTracker.startTracking(HEIGHT, 0.0F);
      this.dataTracker.startTracking(GLOW_COLOR_OVERRIDE, -1);
   }

   protected void readCustomDataFromNbt(NbtCompound nbt) {
      DataResult var10000;
      Logger var10002;
      if (nbt.contains("transformation")) {
         var10000 = AffineTransformation.ANY_CODEC.decode(NbtOps.INSTANCE, nbt.get("transformation"));
         var10002 = field_42397;
         Objects.requireNonNull(var10002);
         var10000.resultOrPartial(Util.addPrefix("Display entity", var10002::error)).ifPresent((pair) -> {
            this.setTransformation((AffineTransformation)pair.getFirst());
         });
      }

      int i;
      if (nbt.contains("interpolation_duration", NbtElement.NUMBER_TYPE)) {
         i = nbt.getInt("interpolation_duration");
         this.setInterpolationDuration(i);
      }

      if (nbt.contains("start_interpolation", NbtElement.NUMBER_TYPE)) {
         i = nbt.getInt("start_interpolation");
         this.setStartInterpolation(i);
      }

      if (nbt.contains("billboard", NbtElement.STRING_TYPE)) {
         var10000 = DisplayEntity.BillboardMode.CODEC.decode(NbtOps.INSTANCE, nbt.get("billboard"));
         var10002 = field_42397;
         Objects.requireNonNull(var10002);
         var10000.resultOrPartial(Util.addPrefix("Display entity", var10002::error)).ifPresent((pair) -> {
            this.setBillboardMode((BillboardMode)pair.getFirst());
         });
      }

      if (nbt.contains("view_range", NbtElement.NUMBER_TYPE)) {
         this.setViewRange(nbt.getFloat("view_range"));
      }

      if (nbt.contains("shadow_radius", NbtElement.NUMBER_TYPE)) {
         this.setShadowRadius(nbt.getFloat("shadow_radius"));
      }

      if (nbt.contains("shadow_strength", NbtElement.NUMBER_TYPE)) {
         this.setShadowStrength(nbt.getFloat("shadow_strength"));
      }

      if (nbt.contains("width", NbtElement.NUMBER_TYPE)) {
         this.setDisplayWidth(nbt.getFloat("width"));
      }

      if (nbt.contains("height", NbtElement.NUMBER_TYPE)) {
         this.setDisplayHeight(nbt.getFloat("height"));
      }

      if (nbt.contains("glow_color_override", NbtElement.NUMBER_TYPE)) {
         this.setGlowColorOverride(nbt.getInt("glow_color_override"));
      }

      if (nbt.contains("brightness", NbtElement.COMPOUND_TYPE)) {
         var10000 = Brightness.CODEC.decode(NbtOps.INSTANCE, nbt.get("brightness"));
         var10002 = field_42397;
         Objects.requireNonNull(var10002);
         var10000.resultOrPartial(Util.addPrefix("Display entity", var10002::error)).ifPresent((pair) -> {
            this.setBrightness((Brightness)pair.getFirst());
         });
      } else {
         this.setBrightness((Brightness)null);
      }

   }

   private void setTransformation(AffineTransformation transformation) {
      this.dataTracker.set(TRANSLATION, transformation.getTranslation());
      this.dataTracker.set(LEFT_ROTATION, transformation.getLeftRotation());
      this.dataTracker.set(SCALE, transformation.getScale());
      this.dataTracker.set(RIGHT_ROTATION, transformation.getRightRotation());
   }

   protected void writeCustomDataToNbt(NbtCompound nbt) {
      AffineTransformation.ANY_CODEC.encodeStart(NbtOps.INSTANCE, getTransformation(this.dataTracker)).result().ifPresent((transformations) -> {
         nbt.put("transformation", transformations);
      });
      DisplayEntity.BillboardMode.CODEC.encodeStart(NbtOps.INSTANCE, this.getBillboardMode()).result().ifPresent((billboard) -> {
         nbt.put("billboard", billboard);
      });
      nbt.putInt("interpolation_duration", this.getInterpolationDuration());
      nbt.putFloat("view_range", this.getViewRange());
      nbt.putFloat("shadow_radius", this.getShadowRadius());
      nbt.putFloat("shadow_strength", this.getShadowStrength());
      nbt.putFloat("width", this.getDisplayWidth());
      nbt.putFloat("height", this.getDisplayHeight());
      nbt.putInt("glow_color_override", this.getGlowColorOverride());
      Brightness lv = this.getBrightnessUnpacked();
      if (lv != null) {
         Brightness.CODEC.encodeStart(NbtOps.INSTANCE, lv).result().ifPresent((brightness) -> {
            nbt.put("brightness", brightness);
         });
      }

   }

   public Packet createSpawnPacket() {
      return new EntitySpawnS2CPacket(this);
   }

   public Box getVisibilityBoundingBox() {
      return this.visibilityBoundingBox;
   }

   public PistonBehavior getPistonBehavior() {
      return PistonBehavior.IGNORE;
   }

   public Quaternionf getFixedRotation() {
      return this.fixedRotation;
   }

   @Nullable
   public RenderState getRenderState() {
      return this.renderState;
   }

   private void setInterpolationDuration(int interpolationDuration) {
      this.dataTracker.set(INTERPOLATION_DURATION, interpolationDuration);
   }

   private int getInterpolationDuration() {
      return (Integer)this.dataTracker.get(INTERPOLATION_DURATION);
   }

   private void setStartInterpolation(int startInterpolation) {
      this.dataTracker.set(START_INTERPOLATION, startInterpolation, true);
   }

   private int getStartInterpolation() {
      return (Integer)this.dataTracker.get(START_INTERPOLATION);
   }

   private void setBillboardMode(BillboardMode billboardMode) {
      this.dataTracker.set(BILLBOARD, billboardMode.getIndex());
   }

   private BillboardMode getBillboardMode() {
      return (BillboardMode)DisplayEntity.BillboardMode.FROM_INDEX.apply((Byte)this.dataTracker.get(BILLBOARD));
   }

   private void setBrightness(@Nullable Brightness brightness) {
      this.dataTracker.set(BRIGHTNESS, brightness != null ? brightness.pack() : -1);
   }

   @Nullable
   private Brightness getBrightnessUnpacked() {
      int i = (Integer)this.dataTracker.get(BRIGHTNESS);
      return i != -1 ? Brightness.unpack(i) : null;
   }

   private int getBrightness() {
      return (Integer)this.dataTracker.get(BRIGHTNESS);
   }

   private void setViewRange(float viewRange) {
      this.dataTracker.set(VIEW_RANGE, viewRange);
   }

   private float getViewRange() {
      return (Float)this.dataTracker.get(VIEW_RANGE);
   }

   private void setShadowRadius(float shadowRadius) {
      this.dataTracker.set(SHADOW_RADIUS, shadowRadius);
   }

   private float getShadowRadius() {
      return (Float)this.dataTracker.get(SHADOW_RADIUS);
   }

   private void setShadowStrength(float shadowStrength) {
      this.dataTracker.set(SHADOW_STRENGTH, shadowStrength);
   }

   private float getShadowStrength() {
      return (Float)this.dataTracker.get(SHADOW_STRENGTH);
   }

   private void setDisplayWidth(float width) {
      this.dataTracker.set(WIDTH, width);
   }

   private float getDisplayWidth() {
      return (Float)this.dataTracker.get(WIDTH);
   }

   private void setDisplayHeight(float height) {
      this.dataTracker.set(HEIGHT, height);
   }

   private int getGlowColorOverride() {
      return (Integer)this.dataTracker.get(GLOW_COLOR_OVERRIDE);
   }

   private void setGlowColorOverride(int glowColorOverride) {
      this.dataTracker.set(GLOW_COLOR_OVERRIDE, glowColorOverride);
   }

   public float getLerpProgress(float delta) {
      int i = this.interpolationDuration;
      if (i <= 0) {
         return 1.0F;
      } else {
         float g = (float)((long)this.age - this.interpolationStart);
         float h = g + delta;
         float j = MathHelper.clamp(MathHelper.getLerpProgress(h, 0.0F, (float)i), 0.0F, 1.0F);
         this.lerpProgress = j;
         return j;
      }
   }

   private float getDisplayHeight() {
      return (Float)this.dataTracker.get(HEIGHT);
   }

   public void setPosition(double x, double y, double z) {
      super.setPosition(x, y, z);
      this.updateVisibilityBoundingBox();
   }

   private void updateVisibilityBoundingBox() {
      float f = this.getDisplayWidth();
      float g = this.getDisplayHeight();
      if (f != 0.0F && g != 0.0F) {
         this.ignoreCameraFrustum = false;
         float h = f / 2.0F;
         double d = this.getX();
         double e = this.getY();
         double i = this.getZ();
         this.visibilityBoundingBox = new Box(d - (double)h, e, i - (double)h, d + (double)h, e + (double)g, i + (double)h);
      } else {
         this.ignoreCameraFrustum = true;
      }

   }

   public void setPitch(float pitch) {
      super.setPitch(pitch);
      this.updateFixedRotation();
   }

   public void setYaw(float yaw) {
      super.setYaw(yaw);
      this.updateFixedRotation();
   }

   private void updateFixedRotation() {
      this.fixedRotation.rotationYXZ(-0.017453292F * this.getYaw(), 0.017453292F * this.getPitch(), 0.0F);
   }

   public boolean shouldRender(double distance) {
      return distance < MathHelper.square((double)this.getViewRange() * 64.0 * getRenderDistanceMultiplier());
   }

   public int getTeamColorValue() {
      int i = this.getGlowColorOverride();
      return i != -1 ? i : super.getTeamColorValue();
   }

   private RenderState copyRenderState() {
      return new RenderState(DisplayEntity.AbstractInterpolator.constant(getTransformation(this.dataTracker)), this.getBillboardMode(), this.getBrightness(), DisplayEntity.FloatLerper.constant(this.getShadowRadius()), DisplayEntity.FloatLerper.constant(this.getShadowStrength()), this.getGlowColorOverride());
   }

   private RenderState getLerpedRenderState(RenderState state, float lerpProgress) {
      AffineTransformation lv = (AffineTransformation)state.transformation.interpolate(lerpProgress);
      float g = state.shadowRadius.lerp(lerpProgress);
      float h = state.shadowStrength.lerp(lerpProgress);
      return new RenderState(new AffineTransformationInterpolator(lv, getTransformation(this.dataTracker)), this.getBillboardMode(), this.getBrightness(), new FloatLerperImpl(g, this.getShadowRadius()), new FloatLerperImpl(h, this.getShadowStrength()), this.getGlowColorOverride());
   }

   static {
      START_INTERPOLATION = DataTracker.registerData(DisplayEntity.class, TrackedDataHandlerRegistry.INTEGER);
      INTERPOLATION_DURATION = DataTracker.registerData(DisplayEntity.class, TrackedDataHandlerRegistry.INTEGER);
      TRANSLATION = DataTracker.registerData(DisplayEntity.class, TrackedDataHandlerRegistry.VECTOR3F);
      SCALE = DataTracker.registerData(DisplayEntity.class, TrackedDataHandlerRegistry.VECTOR3F);
      LEFT_ROTATION = DataTracker.registerData(DisplayEntity.class, TrackedDataHandlerRegistry.QUATERNIONF);
      RIGHT_ROTATION = DataTracker.registerData(DisplayEntity.class, TrackedDataHandlerRegistry.QUATERNIONF);
      BILLBOARD = DataTracker.registerData(DisplayEntity.class, TrackedDataHandlerRegistry.BYTE);
      BRIGHTNESS = DataTracker.registerData(DisplayEntity.class, TrackedDataHandlerRegistry.INTEGER);
      VIEW_RANGE = DataTracker.registerData(DisplayEntity.class, TrackedDataHandlerRegistry.FLOAT);
      SHADOW_RADIUS = DataTracker.registerData(DisplayEntity.class, TrackedDataHandlerRegistry.FLOAT);
      SHADOW_STRENGTH = DataTracker.registerData(DisplayEntity.class, TrackedDataHandlerRegistry.FLOAT);
      WIDTH = DataTracker.registerData(DisplayEntity.class, TrackedDataHandlerRegistry.FLOAT);
      HEIGHT = DataTracker.registerData(DisplayEntity.class, TrackedDataHandlerRegistry.FLOAT);
      GLOW_COLOR_OVERRIDE = DataTracker.registerData(DisplayEntity.class, TrackedDataHandlerRegistry.INTEGER);
      RENDERING_DATA_IDS = IntSet.of(new int[]{TRANSLATION.getId(), SCALE.getId(), LEFT_ROTATION.getId(), RIGHT_ROTATION.getId(), BILLBOARD.getId(), BRIGHTNESS.getId(), SHADOW_RADIUS.getId(), SHADOW_STRENGTH.getId()});
   }

   public static record RenderState(AbstractInterpolator transformation, BillboardMode billboardConstraints, int brightnessOverride, FloatLerper shadowRadius, FloatLerper shadowStrength, int glowColorOverride) {
      final AbstractInterpolator transformation;
      final FloatLerper shadowRadius;
      final FloatLerper shadowStrength;

      public RenderState(AbstractInterpolator arg, BillboardMode arg2, int i, FloatLerper arg3, FloatLerper arg4, int j) {
         this.transformation = arg;
         this.billboardConstraints = arg2;
         this.brightnessOverride = i;
         this.shadowRadius = arg3;
         this.shadowStrength = arg4;
         this.glowColorOverride = j;
      }

      public AbstractInterpolator transformation() {
         return this.transformation;
      }

      public BillboardMode billboardConstraints() {
         return this.billboardConstraints;
      }

      public int brightnessOverride() {
         return this.brightnessOverride;
      }

      public FloatLerper shadowRadius() {
         return this.shadowRadius;
      }

      public FloatLerper shadowStrength() {
         return this.shadowStrength;
      }

      public int glowColorOverride() {
         return this.glowColorOverride;
      }
   }

   public static enum BillboardMode implements StringIdentifiable {
      FIXED((byte)0, "fixed"),
      VERTICAL((byte)1, "vertical"),
      HORIZONTAL((byte)2, "horizontal"),
      CENTER((byte)3, "center");

      public static final Codec CODEC = StringIdentifiable.createCodec(BillboardMode::values);
      public static final IntFunction FROM_INDEX = ValueLists.createIdToValueFunction(BillboardMode::getIndex, values(), (ValueLists.OutOfBoundsHandling)ValueLists.OutOfBoundsHandling.ZERO);
      private final byte index;
      private final String name;

      private BillboardMode(byte index, String name) {
         this.name = name;
         this.index = index;
      }

      public String asString() {
         return this.name;
      }

      byte getIndex() {
         return this.index;
      }

      // $FF: synthetic method
      private static BillboardMode[] method_48882() {
         return new BillboardMode[]{FIXED, VERTICAL, HORIZONTAL, CENTER};
      }
   }

   @FunctionalInterface
   public interface AbstractInterpolator {
      static AbstractInterpolator constant(Object value) {
         return (delta) -> {
            return value;
         };
      }

      Object interpolate(float delta);
   }

   @FunctionalInterface
   public interface FloatLerper {
      static FloatLerper constant(float value) {
         return (delta) -> {
            return value;
         };
      }

      float lerp(float delta);
   }

   static record AffineTransformationInterpolator(AffineTransformation previous, AffineTransformation current) implements AbstractInterpolator {
      AffineTransformationInterpolator(AffineTransformation arg, AffineTransformation arg2) {
         this.previous = arg;
         this.current = arg2;
      }

      public AffineTransformation interpolate(float f) {
         return (double)f >= 1.0 ? this.current : this.previous.interpolate(this.current, f);
      }

      public AffineTransformation previous() {
         return this.previous;
      }

      public AffineTransformation current() {
         return this.current;
      }

      // $FF: synthetic method
      public Object interpolate(float delta) {
         return this.interpolate(delta);
      }
   }

   static record FloatLerperImpl(float previous, float current) implements FloatLerper {
      FloatLerperImpl(float f, float g) {
         this.previous = f;
         this.current = g;
      }

      public float lerp(float delta) {
         return MathHelper.lerp(delta, this.previous, this.current);
      }

      public float previous() {
         return this.previous;
      }

      public float current() {
         return this.current;
      }
   }

   private static record ArgbLerper(int previous, int current) implements IntLerper {
      ArgbLerper(int i, int j) {
         this.previous = i;
         this.current = j;
      }

      public int lerp(float delta) {
         return ColorHelper.Argb.lerp(delta, this.previous, this.current);
      }

      public int previous() {
         return this.previous;
      }

      public int current() {
         return this.current;
      }
   }

   static record IntLerperImpl(int previous, int current) implements IntLerper {
      IntLerperImpl(int i, int j) {
         this.previous = i;
         this.current = j;
      }

      public int lerp(float delta) {
         return MathHelper.lerp(delta, this.previous, this.current);
      }

      public int previous() {
         return this.previous;
      }

      public int current() {
         return this.current;
      }
   }

   @FunctionalInterface
   public interface IntLerper {
      static IntLerper constant(int value) {
         return (delta) -> {
            return value;
         };
      }

      int lerp(float delta);
   }

   public static class TextDisplayEntity extends DisplayEntity {
      public static final String TEXT_NBT_KEY = "text";
      private static final String LINE_WIDTH_NBT_KEY = "line_width";
      private static final String TEXT_OPACITY_NBT_KEY = "text_opacity";
      private static final String BACKGROUND_NBT_KEY = "background";
      private static final String SHADOW_NBT_KEY = "shadow";
      private static final String SEE_THROUGH_NBT_KEY = "see_through";
      private static final String DEFAULT_BACKGROUND_NBT_KEY = "default_background";
      private static final String ALIGNMENT_NBT_KEY = "alignment";
      public static final byte SHADOW_FLAG = 1;
      public static final byte SEE_THROUGH_FLAG = 2;
      public static final byte DEFAULT_BACKGROUND_FLAG = 4;
      public static final byte LEFT_ALIGNMENT_FLAG = 8;
      public static final byte RIGHT_ALIGNMENT_FLAG = 16;
      private static final byte INITIAL_TEXT_OPACITY = -1;
      public static final int INITIAL_BACKGROUND = 1073741824;
      private static final TrackedData TEXT;
      private static final TrackedData LINE_WIDTH;
      private static final TrackedData BACKGROUND;
      private static final TrackedData TEXT_OPACITY;
      private static final TrackedData TEXT_DISPLAY_FLAGS;
      private static final IntSet TEXT_RENDERING_DATA_IDS;
      @Nullable
      private TextLines textLines;
      @Nullable
      private Data data;

      public TextDisplayEntity(EntityType arg, World arg2) {
         super(arg, arg2);
      }

      protected void initDataTracker() {
         super.initDataTracker();
         this.dataTracker.startTracking(TEXT, Text.empty());
         this.dataTracker.startTracking(LINE_WIDTH, 200);
         this.dataTracker.startTracking(BACKGROUND, 1073741824);
         this.dataTracker.startTracking(TEXT_OPACITY, -1);
         this.dataTracker.startTracking(TEXT_DISPLAY_FLAGS, (byte)0);
      }

      public void onTrackedDataSet(TrackedData data) {
         super.onTrackedDataSet(data);
         if (TEXT_RENDERING_DATA_IDS.contains(data.getId())) {
            this.renderingDataSet = true;
         }

      }

      private Text getText() {
         return (Text)this.dataTracker.get(TEXT);
      }

      private void setText(Text text) {
         this.dataTracker.set(TEXT, text);
      }

      private int getLineWidth() {
         return (Integer)this.dataTracker.get(LINE_WIDTH);
      }

      private void setLineWidth(int lineWidth) {
         this.dataTracker.set(LINE_WIDTH, lineWidth);
      }

      private byte getTextOpacity() {
         return (Byte)this.dataTracker.get(TEXT_OPACITY);
      }

      private void setTextOpacity(byte textOpacity) {
         this.dataTracker.set(TEXT_OPACITY, textOpacity);
      }

      private int getBackground() {
         return (Integer)this.dataTracker.get(BACKGROUND);
      }

      private void setBackground(int background) {
         this.dataTracker.set(BACKGROUND, background);
      }

      private byte getDisplayFlags() {
         return (Byte)this.dataTracker.get(TEXT_DISPLAY_FLAGS);
      }

      private void setDisplayFlags(byte flags) {
         this.dataTracker.set(TEXT_DISPLAY_FLAGS, flags);
      }

      private static byte readFlag(byte flags, NbtCompound nbt, String nbtKey, byte flag) {
         return nbt.getBoolean(nbtKey) ? (byte)(flags | flag) : flags;
      }

      protected void readCustomDataFromNbt(NbtCompound nbt) {
         super.readCustomDataFromNbt(nbt);
         if (nbt.contains("line_width", NbtElement.NUMBER_TYPE)) {
            this.setLineWidth(nbt.getInt("line_width"));
         }

         if (nbt.contains("text_opacity", NbtElement.NUMBER_TYPE)) {
            this.setTextOpacity(nbt.getByte("text_opacity"));
         }

         if (nbt.contains("background", NbtElement.NUMBER_TYPE)) {
            this.setBackground(nbt.getInt("background"));
         }

         byte b = readFlag((byte)0, nbt, "shadow", (byte)1);
         b = readFlag(b, nbt, "see_through", (byte)2);
         b = readFlag(b, nbt, "default_background", (byte)4);
         DataResult var10000 = DisplayEntity.TextDisplayEntity.TextAlignment.CODEC.decode(NbtOps.INSTANCE, nbt.get("alignment"));
         Logger var10002 = DisplayEntity.field_42397;
         Objects.requireNonNull(var10002);
         Optional optional = var10000.resultOrPartial(Util.addPrefix("Display entity", var10002::error)).map(Pair::getFirst);
         if (optional.isPresent()) {
            byte var9;
            switch ((TextAlignment)optional.get()) {
               case CENTER:
                  var9 = b;
                  break;
               case LEFT:
                  var9 = (byte)(b | 8);
                  break;
               case RIGHT:
                  var9 = (byte)(b | 16);
                  break;
               default:
                  throw new IncompatibleClassChangeError();
            }

            b = var9;
         }

         this.setDisplayFlags(b);
         if (nbt.contains("text", NbtElement.STRING_TYPE)) {
            String string = nbt.getString("text");

            try {
               Text lv = Text.Serializer.fromJson(string);
               if (lv != null) {
                  ServerCommandSource lv2 = this.getCommandSource().withLevel(2);
                  Text lv3 = Texts.parse(lv2, (Text)lv, this, 0);
                  this.setText(lv3);
               } else {
                  this.setText(Text.empty());
               }
            } catch (Exception var8) {
               DisplayEntity.field_42397.warn("Failed to parse display entity text {}", string, var8);
            }
         }

      }

      private static void writeFlag(byte flags, NbtCompound nbt, String nbtKey, byte flag) {
         nbt.putBoolean(nbtKey, (flags & flag) != 0);
      }

      protected void writeCustomDataToNbt(NbtCompound nbt) {
         super.writeCustomDataToNbt(nbt);
         nbt.putString("text", Text.Serializer.toJson(this.getText()));
         nbt.putInt("line_width", this.getLineWidth());
         nbt.putInt("background", this.getBackground());
         nbt.putByte("text_opacity", this.getTextOpacity());
         byte b = this.getDisplayFlags();
         writeFlag(b, nbt, "shadow", (byte)1);
         writeFlag(b, nbt, "see_through", (byte)2);
         writeFlag(b, nbt, "default_background", (byte)4);
         DisplayEntity.TextDisplayEntity.TextAlignment.CODEC.encodeStart(NbtOps.INSTANCE, getAlignment(b)).result().ifPresent((arg2) -> {
            nbt.put("alignment", arg2);
         });
      }

      protected void refreshData(boolean shouldLerp, float lerpProgress) {
         if (shouldLerp && this.data != null) {
            this.data = this.getLerpedRenderState(this.data, lerpProgress);
         } else {
            this.data = this.copyData();
         }

         this.textLines = null;
      }

      @Nullable
      public Data getData() {
         return this.data;
      }

      private Data copyData() {
         return new Data(this.getText(), this.getLineWidth(), DisplayEntity.IntLerper.constant(this.getTextOpacity()), DisplayEntity.IntLerper.constant(this.getBackground()), this.getDisplayFlags());
      }

      private Data getLerpedRenderState(Data data, float lerpProgress) {
         int i = data.backgroundColor.lerp(lerpProgress);
         int j = data.textOpacity.lerp(lerpProgress);
         return new Data(this.getText(), this.getLineWidth(), new IntLerperImpl(j, this.getTextOpacity()), new ArgbLerper(i, this.getBackground()), this.getDisplayFlags());
      }

      public TextLines splitLines(LineSplitter splitter) {
         if (this.textLines == null) {
            if (this.data != null) {
               this.textLines = splitter.split(this.data.text(), this.data.lineWidth());
            } else {
               this.textLines = new TextLines(List.of(), 0);
            }
         }

         return this.textLines;
      }

      public static TextAlignment getAlignment(byte flags) {
         if ((flags & 8) != 0) {
            return DisplayEntity.TextDisplayEntity.TextAlignment.LEFT;
         } else {
            return (flags & 16) != 0 ? DisplayEntity.TextDisplayEntity.TextAlignment.RIGHT : DisplayEntity.TextDisplayEntity.TextAlignment.CENTER;
         }
      }

      static {
         TEXT = DataTracker.registerData(TextDisplayEntity.class, TrackedDataHandlerRegistry.TEXT_COMPONENT);
         LINE_WIDTH = DataTracker.registerData(TextDisplayEntity.class, TrackedDataHandlerRegistry.INTEGER);
         BACKGROUND = DataTracker.registerData(TextDisplayEntity.class, TrackedDataHandlerRegistry.INTEGER);
         TEXT_OPACITY = DataTracker.registerData(TextDisplayEntity.class, TrackedDataHandlerRegistry.BYTE);
         TEXT_DISPLAY_FLAGS = DataTracker.registerData(TextDisplayEntity.class, TrackedDataHandlerRegistry.BYTE);
         TEXT_RENDERING_DATA_IDS = IntSet.of(new int[]{TEXT.getId(), LINE_WIDTH.getId(), BACKGROUND.getId(), TEXT_OPACITY.getId(), TEXT_DISPLAY_FLAGS.getId()});
      }

      public static enum TextAlignment implements StringIdentifiable {
         CENTER("center"),
         LEFT("left"),
         RIGHT("right");

         public static final Codec CODEC = StringIdentifiable.createCodec(TextAlignment::values);
         private final String name;

         private TextAlignment(String name) {
            this.name = name;
         }

         public String asString() {
            return this.name;
         }

         // $FF: synthetic method
         private static TextAlignment[] method_48920() {
            return new TextAlignment[]{CENTER, LEFT, RIGHT};
         }
      }

      public static record Data(Text text, int lineWidth, IntLerper textOpacity, IntLerper backgroundColor, byte flags) {
         final IntLerper textOpacity;
         final IntLerper backgroundColor;

         public Data(Text arg, int i, IntLerper arg2, IntLerper arg3, byte b) {
            this.text = arg;
            this.lineWidth = i;
            this.textOpacity = arg2;
            this.backgroundColor = arg3;
            this.flags = b;
         }

         public Text text() {
            return this.text;
         }

         public int lineWidth() {
            return this.lineWidth;
         }

         public IntLerper textOpacity() {
            return this.textOpacity;
         }

         public IntLerper backgroundColor() {
            return this.backgroundColor;
         }

         public byte flags() {
            return this.flags;
         }
      }

      public static record TextLines(List lines, int width) {
         public TextLines(List list, int i) {
            this.lines = list;
            this.width = i;
         }

         public List lines() {
            return this.lines;
         }

         public int width() {
            return this.width;
         }
      }

      @FunctionalInterface
      public interface LineSplitter {
         TextLines split(Text text, int lineWidth);
      }

      public static record TextLine(OrderedText contents, int width) {
         public TextLine(OrderedText arg, int i) {
            this.contents = arg;
            this.width = i;
         }

         public OrderedText contents() {
            return this.contents;
         }

         public int width() {
            return this.width;
         }
      }
   }

   public static class BlockDisplayEntity extends DisplayEntity {
      public static final String BLOCK_STATE_NBT_KEY = "block_state";
      private static final TrackedData BLOCK_STATE;
      @Nullable
      private Data data;

      public BlockDisplayEntity(EntityType arg, World arg2) {
         super(arg, arg2);
      }

      protected void initDataTracker() {
         super.initDataTracker();
         this.dataTracker.startTracking(BLOCK_STATE, Blocks.AIR.getDefaultState());
      }

      public void onTrackedDataSet(TrackedData data) {
         super.onTrackedDataSet(data);
         if (data.equals(BLOCK_STATE)) {
            this.renderingDataSet = true;
         }

      }

      private BlockState getBlockState() {
         return (BlockState)this.dataTracker.get(BLOCK_STATE);
      }

      private void setBlockState(BlockState state) {
         this.dataTracker.set(BLOCK_STATE, state);
      }

      protected void readCustomDataFromNbt(NbtCompound nbt) {
         super.readCustomDataFromNbt(nbt);
         this.setBlockState(NbtHelper.toBlockState(this.world.createCommandRegistryWrapper(RegistryKeys.BLOCK), nbt.getCompound("block_state")));
      }

      protected void writeCustomDataToNbt(NbtCompound nbt) {
         super.writeCustomDataToNbt(nbt);
         nbt.put("block_state", NbtHelper.fromBlockState(this.getBlockState()));
      }

      @Nullable
      public Data getData() {
         return this.data;
      }

      protected void refreshData(boolean shouldLerp, float lerpProgress) {
         this.data = new Data(this.getBlockState());
      }

      static {
         BLOCK_STATE = DataTracker.registerData(BlockDisplayEntity.class, TrackedDataHandlerRegistry.BLOCK_STATE);
      }

      public static record Data(BlockState blockState) {
         public Data(BlockState arg) {
            this.blockState = arg;
         }

         public BlockState blockState() {
            return this.blockState;
         }
      }
   }

   public static class ItemDisplayEntity extends DisplayEntity {
      private static final String ITEM_NBT_KEY = "item";
      private static final String ITEM_DISPLAY_NBT_KEY = "item_display";
      private static final TrackedData ITEM;
      private static final TrackedData ITEM_DISPLAY;
      private final StackReference stackReference = new StackReference() {
         public ItemStack get() {
            return ItemDisplayEntity.this.getItemStack();
         }

         public boolean set(ItemStack stack) {
            ItemDisplayEntity.this.setItemStack(stack);
            return true;
         }
      };
      @Nullable
      private Data data;

      public ItemDisplayEntity(EntityType arg, World arg2) {
         super(arg, arg2);
      }

      protected void initDataTracker() {
         super.initDataTracker();
         this.dataTracker.startTracking(ITEM, ItemStack.EMPTY);
         this.dataTracker.startTracking(ITEM_DISPLAY, ModelTransformationMode.NONE.getIndex());
      }

      public void onTrackedDataSet(TrackedData data) {
         super.onTrackedDataSet(data);
         if (ITEM.equals(data) || ITEM_DISPLAY.equals(data)) {
            this.renderingDataSet = true;
         }

      }

      ItemStack getItemStack() {
         return (ItemStack)this.dataTracker.get(ITEM);
      }

      void setItemStack(ItemStack stack) {
         this.dataTracker.set(ITEM, stack);
      }

      private void setTransformationMode(ModelTransformationMode transformationMode) {
         this.dataTracker.set(ITEM_DISPLAY, transformationMode.getIndex());
      }

      private ModelTransformationMode getTransformationMode() {
         return (ModelTransformationMode)ModelTransformationMode.FROM_INDEX.apply((Byte)this.dataTracker.get(ITEM_DISPLAY));
      }

      protected void readCustomDataFromNbt(NbtCompound nbt) {
         super.readCustomDataFromNbt(nbt);
         this.setItemStack(ItemStack.fromNbt(nbt.getCompound("item")));
         if (nbt.contains("item_display", NbtElement.STRING_TYPE)) {
            DataResult var10000 = ModelTransformationMode.CODEC.decode(NbtOps.INSTANCE, nbt.get("item_display"));
            Logger var10002 = DisplayEntity.field_42397;
            Objects.requireNonNull(var10002);
            var10000.resultOrPartial(Util.addPrefix("Display entity", var10002::error)).ifPresent((mode) -> {
               this.setTransformationMode((ModelTransformationMode)mode.getFirst());
            });
         }

      }

      protected void writeCustomDataToNbt(NbtCompound nbt) {
         super.writeCustomDataToNbt(nbt);
         nbt.put("item", this.getItemStack().writeNbt(new NbtCompound()));
         ModelTransformationMode.CODEC.encodeStart(NbtOps.INSTANCE, this.getTransformationMode()).result().ifPresent((nbtx) -> {
            nbt.put("item_display", nbtx);
         });
      }

      public StackReference getStackReference(int mappedIndex) {
         return mappedIndex == 0 ? this.stackReference : StackReference.EMPTY;
      }

      @Nullable
      public Data getData() {
         return this.data;
      }

      protected void refreshData(boolean shouldLerp, float lerpProgress) {
         this.data = new Data(this.getItemStack(), this.getTransformationMode());
      }

      static {
         ITEM = DataTracker.registerData(ItemDisplayEntity.class, TrackedDataHandlerRegistry.ITEM_STACK);
         ITEM_DISPLAY = DataTracker.registerData(ItemDisplayEntity.class, TrackedDataHandlerRegistry.BYTE);
      }

      public static record Data(ItemStack itemStack, ModelTransformationMode itemTransform) {
         public Data(ItemStack arg, ModelTransformationMode arg2) {
            this.itemStack = arg;
            this.itemTransform = arg2;
         }

         public ItemStack itemStack() {
            return this.itemStack;
         }

         public ModelTransformationMode itemTransform() {
            return this.itemTransform;
         }
      }
   }
}
