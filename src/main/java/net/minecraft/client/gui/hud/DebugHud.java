package net.minecraft.client.gui.hud;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.UnmodifiableIterator;
import com.mojang.blaze3d.platform.GlDebugInfo;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.datafixers.DataFixUtils;
import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.longs.LongSets;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.util.EnumMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.SharedConstants;
import net.minecraft.block.BlockState;
import net.minecraft.client.ClientBrandRetriever;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gl.PostEffectProcessor;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.fluid.FluidState;
import net.minecraft.network.ClientConnection;
import net.minecraft.registry.Registries;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.integrated.IntegratedServer;
import net.minecraft.server.world.ServerChunkManager;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.property.Property;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.MetricsData;
import net.minecraft.util.Util;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.AffineTransformation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.Heightmap;
import net.minecraft.world.LightType;
import net.minecraft.world.LocalDifficulty;
import net.minecraft.world.SpawnHelper;
import net.minecraft.world.World;
import net.minecraft.world.biome.source.BiomeSource;
import net.minecraft.world.biome.source.util.MultiNoiseUtil;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.chunk.WorldChunk;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.gen.noise.NoiseConfig;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;

@Environment(EnvType.CLIENT)
public class DebugHud extends DrawableHelper {
   private static final int TEXT_COLOR = 14737632;
   private static final int field_32188 = 2;
   private static final int field_32189 = 2;
   private static final int field_32190 = 2;
   private static final Map HEIGHT_MAP_TYPES = (Map)Util.make(new EnumMap(Heightmap.Type.class), (types) -> {
      types.put(Heightmap.Type.WORLD_SURFACE_WG, "SW");
      types.put(Heightmap.Type.WORLD_SURFACE, "S");
      types.put(Heightmap.Type.OCEAN_FLOOR_WG, "OW");
      types.put(Heightmap.Type.OCEAN_FLOOR, "O");
      types.put(Heightmap.Type.MOTION_BLOCKING, "M");
      types.put(Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, "ML");
   });
   private final MinecraftClient client;
   private final AllocationRateCalculator allocationRateCalculator;
   private final TextRenderer textRenderer;
   private HitResult blockHit;
   private HitResult fluidHit;
   @Nullable
   private ChunkPos pos;
   @Nullable
   private WorldChunk chunk;
   @Nullable
   private CompletableFuture chunkFuture;
   private static final int field_32191 = -65536;
   private static final int field_32192 = -256;
   private static final int field_32193 = -16711936;

   public DebugHud(MinecraftClient client) {
      this.client = client;
      this.allocationRateCalculator = new AllocationRateCalculator();
      this.textRenderer = client.textRenderer;
   }

   public void resetChunk() {
      this.chunkFuture = null;
      this.chunk = null;
   }

   public void render(MatrixStack matrices) {
      this.client.getProfiler().push("debug");
      Entity lv = this.client.getCameraEntity();
      this.blockHit = lv.raycast(20.0, 0.0F, false);
      this.fluidHit = lv.raycast(20.0, 0.0F, true);
      this.renderLeftText(matrices);
      this.renderRightText(matrices);
      if (this.client.options.debugTpsEnabled) {
         int i = this.client.getWindow().getScaledWidth();
         this.drawMetricsData(matrices, this.client.getMetricsData(), 0, i / 2, true);
         IntegratedServer lv2 = this.client.getServer();
         if (lv2 != null) {
            this.drawMetricsData(matrices, lv2.getMetricsData(), i - Math.min(i / 2, 240), i / 2, false);
         }
      }

      this.client.getProfiler().pop();
   }

   protected void renderLeftText(MatrixStack matrices) {
      List list = this.getLeftText();
      list.add("");
      boolean bl = this.client.getServer() != null;
      String var10001 = this.client.options.debugProfilerEnabled ? "visible" : "hidden";
      list.add("Debug: Pie [shift]: " + var10001 + (bl ? " FPS + TPS" : " FPS") + " [alt]: " + (this.client.options.debugTpsEnabled ? "visible" : "hidden"));
      list.add("For help: press F3 + Q");

      for(int i = 0; i < list.size(); ++i) {
         String string = (String)list.get(i);
         if (!Strings.isNullOrEmpty(string)) {
            Objects.requireNonNull(this.textRenderer);
            int j = 9;
            int k = this.textRenderer.getWidth(string);
            int l = true;
            int m = 2 + j * i;
            fill(matrices, 1, m - 1, 2 + k + 1, m + j - 1, -1873784752);
            this.textRenderer.draw(matrices, string, 2.0F, (float)m, 14737632);
         }
      }

   }

   protected void renderRightText(MatrixStack matrices) {
      List list = this.getRightText();

      for(int i = 0; i < list.size(); ++i) {
         String string = (String)list.get(i);
         if (!Strings.isNullOrEmpty(string)) {
            Objects.requireNonNull(this.textRenderer);
            int j = 9;
            int k = this.textRenderer.getWidth(string);
            int l = this.client.getWindow().getScaledWidth() - 2 - k;
            int m = 2 + j * i;
            fill(matrices, l - 1, m - 1, l + k + 1, m + j - 1, -1873784752);
            this.textRenderer.draw(matrices, string, (float)l, (float)m, 14737632);
         }
      }

   }

   protected List getLeftText() {
      IntegratedServer lv = this.client.getServer();
      ClientConnection lv2 = this.client.getNetworkHandler().getConnection();
      float f = lv2.getAveragePacketsSent();
      float g = lv2.getAveragePacketsReceived();
      String string;
      if (lv != null) {
         string = String.format(Locale.ROOT, "Integrated server @ %.0f ms ticks, %.0f tx, %.0f rx", lv.getTickTime(), f, g);
      } else {
         string = String.format(Locale.ROOT, "\"%s\" server, %.0f tx, %.0f rx", this.client.player.getServerBrand(), f, g);
      }

      BlockPos lv3 = this.client.getCameraEntity().getBlockPos();
      String[] var10000;
      String var10003;
      if (this.client.hasReducedDebugInfo()) {
         var10000 = new String[9];
         var10003 = SharedConstants.getGameVersion().getName();
         var10000[0] = "Minecraft " + var10003 + " (" + this.client.getGameVersion() + "/" + ClientBrandRetriever.getClientModName() + ")";
         var10000[1] = this.client.fpsDebugString;
         var10000[2] = string;
         var10000[3] = this.client.worldRenderer.getChunksDebugString();
         var10000[4] = this.client.worldRenderer.getEntitiesDebugString();
         var10003 = this.client.particleManager.getDebugString();
         var10000[5] = "P: " + var10003 + ". T: " + this.client.world.getRegularEntityCount();
         var10000[6] = this.client.world.asString();
         var10000[7] = "";
         var10000[8] = String.format(Locale.ROOT, "Chunk-relative: %d %d %d", lv3.getX() & 15, lv3.getY() & 15, lv3.getZ() & 15);
         return Lists.newArrayList(var10000);
      } else {
         Entity lv4 = this.client.getCameraEntity();
         Direction lv5 = lv4.getHorizontalFacing();
         String string2;
         switch (lv5) {
            case NORTH:
               string2 = "Towards negative Z";
               break;
            case SOUTH:
               string2 = "Towards positive Z";
               break;
            case WEST:
               string2 = "Towards negative X";
               break;
            case EAST:
               string2 = "Towards positive X";
               break;
            default:
               string2 = "Invalid";
         }

         ChunkPos lv6 = new ChunkPos(lv3);
         if (!Objects.equals(this.pos, lv6)) {
            this.pos = lv6;
            this.resetChunk();
         }

         World lv7 = this.getWorld();
         LongSet longSet = lv7 instanceof ServerWorld ? ((ServerWorld)lv7).getForcedChunks() : LongSets.EMPTY_SET;
         var10000 = new String[7];
         var10003 = SharedConstants.getGameVersion().getName();
         var10000[0] = "Minecraft " + var10003 + " (" + this.client.getGameVersion() + "/" + ClientBrandRetriever.getClientModName() + ("release".equalsIgnoreCase(this.client.getVersionType()) ? "" : "/" + this.client.getVersionType()) + ")";
         var10000[1] = this.client.fpsDebugString;
         var10000[2] = string;
         var10000[3] = this.client.worldRenderer.getChunksDebugString();
         var10000[4] = this.client.worldRenderer.getEntitiesDebugString();
         var10003 = this.client.particleManager.getDebugString();
         var10000[5] = "P: " + var10003 + ". T: " + this.client.world.getRegularEntityCount();
         var10000[6] = this.client.world.asString();
         List list = Lists.newArrayList(var10000);
         String string3 = this.getServerWorldDebugString();
         if (string3 != null) {
            list.add(string3);
         }

         Identifier var10001 = this.client.world.getRegistryKey().getValue();
         list.add("" + var10001 + " FC: " + ((LongSet)longSet).size());
         list.add("");
         list.add(String.format(Locale.ROOT, "XYZ: %.3f / %.5f / %.3f", this.client.getCameraEntity().getX(), this.client.getCameraEntity().getY(), this.client.getCameraEntity().getZ()));
         list.add(String.format(Locale.ROOT, "Block: %d %d %d [%d %d %d]", lv3.getX(), lv3.getY(), lv3.getZ(), lv3.getX() & 15, lv3.getY() & 15, lv3.getZ() & 15));
         list.add(String.format(Locale.ROOT, "Chunk: %d %d %d [%d %d in r.%d.%d.mca]", lv6.x, ChunkSectionPos.getSectionCoord(lv3.getY()), lv6.z, lv6.getRegionRelativeX(), lv6.getRegionRelativeZ(), lv6.getRegionX(), lv6.getRegionZ()));
         list.add(String.format(Locale.ROOT, "Facing: %s (%s) (%.1f / %.1f)", lv5, string2, MathHelper.wrapDegrees(lv4.getYaw()), MathHelper.wrapDegrees(lv4.getPitch())));
         WorldChunk lv8 = this.getClientChunk();
         if (lv8.isEmpty()) {
            list.add("Waiting for chunk...");
         } else {
            int i = this.client.world.getChunkManager().getLightingProvider().getLight(lv3, 0);
            int j = this.client.world.getLightLevel(LightType.SKY, lv3);
            int k = this.client.world.getLightLevel(LightType.BLOCK, lv3);
            list.add("Client Light: " + i + " (" + j + " sky, " + k + " block)");
            WorldChunk lv9 = this.getChunk();
            StringBuilder stringBuilder = new StringBuilder("CH");
            Heightmap.Type[] var21 = Heightmap.Type.values();
            int var22 = var21.length;

            int var23;
            Heightmap.Type lv10;
            for(var23 = 0; var23 < var22; ++var23) {
               lv10 = var21[var23];
               if (lv10.shouldSendToClient()) {
                  stringBuilder.append(" ").append((String)HEIGHT_MAP_TYPES.get(lv10)).append(": ").append(lv8.sampleHeightmap(lv10, lv3.getX(), lv3.getZ()));
               }
            }

            list.add(stringBuilder.toString());
            stringBuilder.setLength(0);
            stringBuilder.append("SH");
            var21 = Heightmap.Type.values();
            var22 = var21.length;

            for(var23 = 0; var23 < var22; ++var23) {
               lv10 = var21[var23];
               if (lv10.isStoredServerSide()) {
                  stringBuilder.append(" ").append((String)HEIGHT_MAP_TYPES.get(lv10)).append(": ");
                  if (lv9 != null) {
                     stringBuilder.append(lv9.sampleHeightmap(lv10, lv3.getX(), lv3.getZ()));
                  } else {
                     stringBuilder.append("??");
                  }
               }
            }

            list.add(stringBuilder.toString());
            if (lv3.getY() >= this.client.world.getBottomY() && lv3.getY() < this.client.world.getTopY()) {
               RegistryEntry var27 = this.client.world.getBiome(lv3);
               list.add("Biome: " + getBiomeString(var27));
               long l = 0L;
               float h = 0.0F;
               if (lv9 != null) {
                  h = lv7.getMoonSize();
                  l = lv9.getInhabitedTime();
               }

               LocalDifficulty lv11 = new LocalDifficulty(lv7.getDifficulty(), lv7.getTimeOfDay(), l, h);
               list.add(String.format(Locale.ROOT, "Local Difficulty: %.2f // %.2f (Day %d)", lv11.getLocalDifficulty(), lv11.getClampedLocalDifficulty(), this.client.world.getTimeOfDay() / 24000L));
            }

            if (lv9 != null && lv9.usesOldNoise()) {
               list.add("Blending: Old");
            }
         }

         ServerWorld lv12 = this.getServerWorld();
         if (lv12 != null) {
            ServerChunkManager lv13 = lv12.getChunkManager();
            ChunkGenerator lv14 = lv13.getChunkGenerator();
            NoiseConfig lv15 = lv13.getNoiseConfig();
            lv14.getDebugHudText(list, lv15, lv3);
            MultiNoiseUtil.MultiNoiseSampler lv16 = lv15.getMultiNoiseSampler();
            BiomeSource lv17 = lv14.getBiomeSource();
            lv17.addDebugInfo(list, lv3, lv16);
            SpawnHelper.Info lv18 = lv13.getSpawnInfo();
            if (lv18 != null) {
               Object2IntMap object2IntMap = lv18.getGroupToCount();
               int m = lv18.getSpawningChunkCount();
               list.add("SC: " + m + ", " + (String)Stream.of(SpawnGroup.values()).map((group) -> {
                  char var10000 = Character.toUpperCase(group.getName().charAt(0));
                  return "" + var10000 + ": " + object2IntMap.getInt(group);
               }).collect(Collectors.joining(", ")));
            } else {
               list.add("SC: N/A");
            }
         }

         PostEffectProcessor lv19 = this.client.gameRenderer.getPostProcessor();
         if (lv19 != null) {
            list.add("Shader: " + lv19.getName());
         }

         String var29 = this.client.getSoundManager().getDebugString();
         list.add(var29 + String.format(Locale.ROOT, " (Mood %d%%)", Math.round(this.client.player.getMoodPercentage() * 100.0F)));
         return list;
      }
   }

   private static String getBiomeString(RegistryEntry biome) {
      return (String)biome.getKeyOrValue().map((biomeKey) -> {
         return biomeKey.getValue().toString();
      }, (biome_) -> {
         return "[unregistered " + biome_ + "]";
      });
   }

   @Nullable
   private ServerWorld getServerWorld() {
      IntegratedServer lv = this.client.getServer();
      return lv != null ? lv.getWorld(this.client.world.getRegistryKey()) : null;
   }

   @Nullable
   private String getServerWorldDebugString() {
      ServerWorld lv = this.getServerWorld();
      return lv != null ? lv.asString() : null;
   }

   private World getWorld() {
      return (World)DataFixUtils.orElse(Optional.ofNullable(this.client.getServer()).flatMap((server) -> {
         return Optional.ofNullable(server.getWorld(this.client.world.getRegistryKey()));
      }), this.client.world);
   }

   @Nullable
   private WorldChunk getChunk() {
      if (this.chunkFuture == null) {
         ServerWorld lv = this.getServerWorld();
         if (lv != null) {
            this.chunkFuture = lv.getChunkManager().getChunkFutureSyncOnMainThread(this.pos.x, this.pos.z, ChunkStatus.FULL, false).thenApply((either) -> {
               return (WorldChunk)either.map((chunk) -> {
                  return (WorldChunk)chunk;
               }, (unloaded) -> {
                  return null;
               });
            });
         }

         if (this.chunkFuture == null) {
            this.chunkFuture = CompletableFuture.completedFuture(this.getClientChunk());
         }
      }

      return (WorldChunk)this.chunkFuture.getNow((Object)null);
   }

   private WorldChunk getClientChunk() {
      if (this.chunk == null) {
         this.chunk = this.client.world.getChunk(this.pos.x, this.pos.z);
      }

      return this.chunk;
   }

   protected List getRightText() {
      long l = Runtime.getRuntime().maxMemory();
      long m = Runtime.getRuntime().totalMemory();
      long n = Runtime.getRuntime().freeMemory();
      long o = m - n;
      List list = Lists.newArrayList(new String[]{String.format(Locale.ROOT, "Java: %s %dbit", System.getProperty("java.version"), this.client.is64Bit() ? 64 : 32), String.format(Locale.ROOT, "Mem: % 2d%% %03d/%03dMB", o * 100L / l, toMiB(o), toMiB(l)), String.format(Locale.ROOT, "Allocation rate: %03dMB /s", toMiB(this.allocationRateCalculator.get(o))), String.format(Locale.ROOT, "Allocated: % 2d%% %03dMB", m * 100L / l, toMiB(m)), "", String.format(Locale.ROOT, "CPU: %s", GlDebugInfo.getCpuInfo()), "", String.format(Locale.ROOT, "Display: %dx%d (%s)", MinecraftClient.getInstance().getWindow().getFramebufferWidth(), MinecraftClient.getInstance().getWindow().getFramebufferHeight(), GlDebugInfo.getVendor()), GlDebugInfo.getRenderer(), GlDebugInfo.getVersion()});
      if (this.client.hasReducedDebugInfo()) {
         return list;
      } else {
         BlockPos lv;
         UnmodifiableIterator var12;
         Map.Entry entry;
         Stream var10000;
         Formatting var10001;
         if (this.blockHit.getType() == HitResult.Type.BLOCK) {
            lv = ((BlockHitResult)this.blockHit).getBlockPos();
            BlockState lv2 = this.client.world.getBlockState(lv);
            list.add("");
            var10001 = Formatting.UNDERLINE;
            list.add("" + var10001 + "Targeted Block: " + lv.getX() + ", " + lv.getY() + ", " + lv.getZ());
            list.add(String.valueOf(Registries.BLOCK.getId(lv2.getBlock())));
            var12 = lv2.getEntries().entrySet().iterator();

            while(var12.hasNext()) {
               entry = (Map.Entry)var12.next();
               list.add(this.propertyToString(entry));
            }

            var10000 = lv2.streamTags().map((tag) -> {
               return "#" + tag.id();
            });
            Objects.requireNonNull(list);
            var10000.forEach(list::add);
         }

         if (this.fluidHit.getType() == HitResult.Type.BLOCK) {
            lv = ((BlockHitResult)this.fluidHit).getBlockPos();
            FluidState lv3 = this.client.world.getFluidState(lv);
            list.add("");
            var10001 = Formatting.UNDERLINE;
            list.add("" + var10001 + "Targeted Fluid: " + lv.getX() + ", " + lv.getY() + ", " + lv.getZ());
            list.add(String.valueOf(Registries.FLUID.getId(lv3.getFluid())));
            var12 = lv3.getEntries().entrySet().iterator();

            while(var12.hasNext()) {
               entry = (Map.Entry)var12.next();
               list.add(this.propertyToString(entry));
            }

            var10000 = lv3.streamTags().map((tag) -> {
               return "#" + tag.id();
            });
            Objects.requireNonNull(list);
            var10000.forEach(list::add);
         }

         Entity lv4 = this.client.targetedEntity;
         if (lv4 != null) {
            list.add("");
            list.add(Formatting.UNDERLINE + "Targeted Entity");
            list.add(String.valueOf(Registries.ENTITY_TYPE.getId(lv4.getType())));
         }

         return list;
      }
   }

   private String propertyToString(Map.Entry propEntry) {
      Property lv = (Property)propEntry.getKey();
      Comparable comparable = (Comparable)propEntry.getValue();
      String string = Util.getValueAsString(lv, comparable);
      if (Boolean.TRUE.equals(comparable)) {
         string = Formatting.GREEN + string;
      } else if (Boolean.FALSE.equals(comparable)) {
         string = Formatting.RED + string;
      }

      String var10000 = lv.getName();
      return var10000 + ": " + string;
   }

   private void drawMetricsData(MatrixStack matrices, MetricsData metricsData, int x, int width, boolean showFps) {
      RenderSystem.disableDepthTest();
      int k = metricsData.getStartIndex();
      int l = metricsData.getCurrentIndex();
      long[] ls = metricsData.getSamples();
      int n = x;
      int o = Math.max(0, ls.length - width);
      int p = ls.length - o;
      int m = metricsData.wrapIndex(k + o);
      long q = 0L;
      int r = Integer.MAX_VALUE;
      int s = Integer.MIN_VALUE;

      int t;
      for(t = 0; t < p; ++t) {
         int u = (int)(ls[metricsData.wrapIndex(m + t)] / 1000000L);
         r = Math.min(r, u);
         s = Math.max(s, u);
         q += (long)u;
      }

      t = this.client.getWindow().getScaledHeight();
      fill(matrices, x, t - 60, x + p, t, -1873784752);
      RenderSystem.setShader(GameRenderer::getPositionColorProgram);
      BufferBuilder lv = Tessellator.getInstance().getBuffer();
      RenderSystem.enableBlend();
      RenderSystem.defaultBlendFunc();
      lv.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);

      int v;
      for(Matrix4f matrix4f = AffineTransformation.identity().getMatrix(); m != l; m = metricsData.wrapIndex(m + 1)) {
         v = metricsData.scaleSample(ls[m], showFps ? 30 : 60, showFps ? 60 : 20);
         int w = showFps ? 100 : 60;
         int x = this.getMetricsLineColor(MathHelper.clamp(v, 0, w), 0, w / 2, w);
         int y = x >> 24 & 255;
         int z = x >> 16 & 255;
         int aa = x >> 8 & 255;
         int ab = x & 255;
         lv.vertex(matrix4f, (float)(n + 1), (float)t, 0.0F).color(z, aa, ab, y).next();
         lv.vertex(matrix4f, (float)(n + 1), (float)(t - v + 1), 0.0F).color(z, aa, ab, y).next();
         lv.vertex(matrix4f, (float)n, (float)(t - v + 1), 0.0F).color(z, aa, ab, y).next();
         lv.vertex(matrix4f, (float)n, (float)t, 0.0F).color(z, aa, ab, y).next();
         ++n;
      }

      BufferRenderer.drawWithGlobalProgram(lv.end());
      RenderSystem.disableBlend();
      if (showFps) {
         fill(matrices, x + 1, t - 30 + 1, x + 14, t - 30 + 10, -1873784752);
         this.textRenderer.draw(matrices, "60 FPS", (float)(x + 2), (float)(t - 30 + 2), 14737632);
         drawHorizontalLine(matrices, x, x + p - 1, t - 30, -1);
         fill(matrices, x + 1, t - 60 + 1, x + 14, t - 60 + 10, -1873784752);
         this.textRenderer.draw(matrices, "30 FPS", (float)(x + 2), (float)(t - 60 + 2), 14737632);
         drawHorizontalLine(matrices, x, x + p - 1, t - 60, -1);
      } else {
         fill(matrices, x + 1, t - 60 + 1, x + 14, t - 60 + 10, -1873784752);
         this.textRenderer.draw(matrices, "20 TPS", (float)(x + 2), (float)(t - 60 + 2), 14737632);
         drawHorizontalLine(matrices, x, x + p - 1, t - 60, -1);
      }

      drawHorizontalLine(matrices, x, x + p - 1, t - 1, -1);
      drawVerticalLine(matrices, x, t - 60, t, -1);
      drawVerticalLine(matrices, x + p - 1, t - 60, t, -1);
      v = (Integer)this.client.options.getMaxFps().getValue();
      if (showFps && v > 0 && v <= 250) {
         drawHorizontalLine(matrices, x, x + p - 1, t - 1 - (int)(1800.0 / (double)v), -16711681);
      }

      String string = "" + r + " ms min";
      String string2 = q / (long)p + " ms avg";
      String string3 = "" + s + " ms max";
      TextRenderer var10000 = this.textRenderer;
      float var10003 = (float)(x + 2);
      int var10004 = t - 60;
      Objects.requireNonNull(this.textRenderer);
      var10000.drawWithShadow(matrices, string, var10003, (float)(var10004 - 9), 14737632);
      var10000 = this.textRenderer;
      var10003 = (float)(x + p / 2 - this.textRenderer.getWidth(string2) / 2);
      var10004 = t - 60;
      Objects.requireNonNull(this.textRenderer);
      var10000.drawWithShadow(matrices, string2, var10003, (float)(var10004 - 9), 14737632);
      var10000 = this.textRenderer;
      var10003 = (float)(x + p - this.textRenderer.getWidth(string3));
      var10004 = t - 60;
      Objects.requireNonNull(this.textRenderer);
      var10000.drawWithShadow(matrices, string3, var10003, (float)(var10004 - 9), 14737632);
      RenderSystem.enableDepthTest();
   }

   private int getMetricsLineColor(int value, int greenValue, int yellowValue, int redValue) {
      return value < yellowValue ? this.interpolateColor(-16711936, -256, (float)value / (float)yellowValue) : this.interpolateColor(-256, -65536, (float)(value - yellowValue) / (float)(redValue - yellowValue));
   }

   private int interpolateColor(int color1, int color2, float dt) {
      int k = color1 >> 24 & 255;
      int l = color1 >> 16 & 255;
      int m = color1 >> 8 & 255;
      int n = color1 & 255;
      int o = color2 >> 24 & 255;
      int p = color2 >> 16 & 255;
      int q = color2 >> 8 & 255;
      int r = color2 & 255;
      int s = MathHelper.clamp((int)MathHelper.lerp(dt, (float)k, (float)o), 0, 255);
      int t = MathHelper.clamp((int)MathHelper.lerp(dt, (float)l, (float)p), 0, 255);
      int u = MathHelper.clamp((int)MathHelper.lerp(dt, (float)m, (float)q), 0, 255);
      int v = MathHelper.clamp((int)MathHelper.lerp(dt, (float)n, (float)r), 0, 255);
      return s << 24 | t << 16 | u << 8 | v;
   }

   private static long toMiB(long bytes) {
      return bytes / 1024L / 1024L;
   }

   @Environment(EnvType.CLIENT)
   private static class AllocationRateCalculator {
      private static final int INTERVAL = 500;
      private static final List GARBAGE_COLLECTORS = ManagementFactory.getGarbageCollectorMXBeans();
      private long lastCalculated = 0L;
      private long allocatedBytes = -1L;
      private long collectionCount = -1L;
      private long allocationRate = 0L;

      AllocationRateCalculator() {
      }

      long get(long allocatedBytes) {
         long m = System.currentTimeMillis();
         if (m - this.lastCalculated < 500L) {
            return this.allocationRate;
         } else {
            long n = getCollectionCount();
            if (this.lastCalculated != 0L && n == this.collectionCount) {
               double d = (double)TimeUnit.SECONDS.toMillis(1L) / (double)(m - this.lastCalculated);
               long o = allocatedBytes - this.allocatedBytes;
               this.allocationRate = Math.round((double)o * d);
            }

            this.lastCalculated = m;
            this.allocatedBytes = allocatedBytes;
            this.collectionCount = n;
            return this.allocationRate;
         }
      }

      private static long getCollectionCount() {
         long l = 0L;

         GarbageCollectorMXBean garbageCollectorMXBean;
         for(Iterator var2 = GARBAGE_COLLECTORS.iterator(); var2.hasNext(); l += garbageCollectorMXBean.getCollectionCount()) {
            garbageCollectorMXBean = (GarbageCollectorMXBean)var2.next();
         }

         return l;
      }
   }
}
