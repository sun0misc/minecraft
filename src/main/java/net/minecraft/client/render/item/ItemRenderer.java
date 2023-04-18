package net.minecraft.client.render.item;

import com.google.common.collect.Sets;
import com.mojang.blaze3d.systems.RenderSystem;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.StainedGlassPaneBlock;
import net.minecraft.block.TransparentBlock;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.color.item.ItemColors;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.DiffuseLighting;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.OverlayVertexConsumer;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderLayers;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.TexturedRenderLayers;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.VertexConsumers;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.BakedModelManager;
import net.minecraft.client.render.model.BakedQuad;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.texture.TextureManager;
import net.minecraft.client.util.ModelIdentifier;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.SynchronousResourceReloader;
import net.minecraft.util.Identifier;
import net.minecraft.util.crash.CrashException;
import net.minecraft.util.crash.CrashReport;
import net.minecraft.util.crash.CrashReportSection;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.MatrixUtil;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;

@Environment(EnvType.CLIENT)
public class ItemRenderer implements SynchronousResourceReloader {
   public static final Identifier ENTITY_ENCHANTMENT_GLINT = new Identifier("textures/misc/enchanted_glint_entity.png");
   public static final Identifier ITEM_ENCHANTMENT_GLINT = new Identifier("textures/misc/enchanted_glint_item.png");
   private static final Set WITHOUT_MODELS;
   private static final int field_32937 = 8;
   private static final int field_32938 = 8;
   public static final int field_32934 = 200;
   public static final float COMPASS_WITH_GLINT_GUI_MODEL_MULTIPLIER = 0.5F;
   public static final float COMPASS_WITH_GLINT_FIRST_PERSON_MODEL_MULTIPLIER = 0.75F;
   public static final float field_41120 = 0.0078125F;
   private static final ModelIdentifier TRIDENT;
   public static final ModelIdentifier TRIDENT_IN_HAND;
   private static final ModelIdentifier SPYGLASS;
   public static final ModelIdentifier SPYGLASS_IN_HAND;
   private final MinecraftClient client;
   private final ItemModels models;
   private final TextureManager textureManager;
   private final ItemColors colors;
   private final BuiltinModelItemRenderer builtinModelItemRenderer;

   public ItemRenderer(MinecraftClient client, TextureManager manager, BakedModelManager bakery, ItemColors colors, BuiltinModelItemRenderer builtinModelItemRenderer) {
      this.client = client;
      this.textureManager = manager;
      this.models = new ItemModels(bakery);
      this.builtinModelItemRenderer = builtinModelItemRenderer;
      Iterator var6 = Registries.ITEM.iterator();

      while(var6.hasNext()) {
         Item lv = (Item)var6.next();
         if (!WITHOUT_MODELS.contains(lv)) {
            this.models.putModel(lv, new ModelIdentifier(Registries.ITEM.getId(lv), "inventory"));
         }
      }

      this.colors = colors;
   }

   public ItemModels getModels() {
      return this.models;
   }

   private void renderBakedItemModel(BakedModel model, ItemStack stack, int light, int overlay, MatrixStack matrices, VertexConsumer vertices) {
      Random lv = Random.create();
      long l = 42L;
      Direction[] var10 = Direction.values();
      int var11 = var10.length;

      for(int var12 = 0; var12 < var11; ++var12) {
         Direction lv2 = var10[var12];
         lv.setSeed(42L);
         this.renderBakedItemQuads(matrices, vertices, model.getQuads((BlockState)null, lv2, lv), stack, light, overlay);
      }

      lv.setSeed(42L);
      this.renderBakedItemQuads(matrices, vertices, model.getQuads((BlockState)null, (Direction)null, lv), stack, light, overlay);
   }

   public void renderItem(ItemStack stack, ModelTransformationMode renderMode, boolean leftHanded, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay, BakedModel model) {
      if (!stack.isEmpty()) {
         matrices.push();
         boolean bl2 = renderMode == ModelTransformationMode.GUI || renderMode == ModelTransformationMode.GROUND || renderMode == ModelTransformationMode.FIXED;
         if (bl2) {
            if (stack.isOf(Items.TRIDENT)) {
               model = this.models.getModelManager().getModel(TRIDENT);
            } else if (stack.isOf(Items.SPYGLASS)) {
               model = this.models.getModelManager().getModel(SPYGLASS);
            }
         }

         model.getTransformation().getTransformation(renderMode).apply(leftHanded, matrices);
         matrices.translate(-0.5F, -0.5F, -0.5F);
         if (!model.isBuiltin() && (!stack.isOf(Items.TRIDENT) || bl2)) {
            boolean bl3;
            if (renderMode != ModelTransformationMode.GUI && !renderMode.isFirstPerson() && stack.getItem() instanceof BlockItem) {
               Block lv = ((BlockItem)stack.getItem()).getBlock();
               bl3 = !(lv instanceof TransparentBlock) && !(lv instanceof StainedGlassPaneBlock);
            } else {
               bl3 = true;
            }

            RenderLayer lv2 = RenderLayers.getItemLayer(stack, bl3);
            VertexConsumer lv4;
            if (stack.isIn(ItemTags.COMPASSES) && stack.hasGlint()) {
               matrices.push();
               MatrixStack.Entry lv3 = matrices.peek();
               if (renderMode == ModelTransformationMode.GUI) {
                  MatrixUtil.scale(lv3.getPositionMatrix(), 0.5F);
               } else if (renderMode.isFirstPerson()) {
                  MatrixUtil.scale(lv3.getPositionMatrix(), 0.75F);
               }

               if (bl3) {
                  lv4 = getDirectCompassGlintConsumer(vertexConsumers, lv2, lv3);
               } else {
                  lv4 = getCompassGlintConsumer(vertexConsumers, lv2, lv3);
               }

               matrices.pop();
            } else if (bl3) {
               lv4 = getDirectItemGlintConsumer(vertexConsumers, lv2, true, stack.hasGlint());
            } else {
               lv4 = getItemGlintConsumer(vertexConsumers, lv2, true, stack.hasGlint());
            }

            this.renderBakedItemModel(model, stack, light, overlay, matrices, lv4);
         } else {
            this.builtinModelItemRenderer.render(stack, renderMode, matrices, vertexConsumers, light, overlay);
         }

         matrices.pop();
      }
   }

   public static VertexConsumer getArmorGlintConsumer(VertexConsumerProvider provider, RenderLayer layer, boolean solid, boolean glint) {
      return glint ? VertexConsumers.union(provider.getBuffer(solid ? RenderLayer.getArmorGlint() : RenderLayer.getArmorEntityGlint()), provider.getBuffer(layer)) : provider.getBuffer(layer);
   }

   public static VertexConsumer getCompassGlintConsumer(VertexConsumerProvider provider, RenderLayer layer, MatrixStack.Entry entry) {
      return VertexConsumers.union(new OverlayVertexConsumer(provider.getBuffer(RenderLayer.getGlint()), entry.getPositionMatrix(), entry.getNormalMatrix(), 0.0078125F), provider.getBuffer(layer));
   }

   public static VertexConsumer getDirectCompassGlintConsumer(VertexConsumerProvider provider, RenderLayer layer, MatrixStack.Entry entry) {
      return VertexConsumers.union(new OverlayVertexConsumer(provider.getBuffer(RenderLayer.getDirectGlint()), entry.getPositionMatrix(), entry.getNormalMatrix(), 0.0078125F), provider.getBuffer(layer));
   }

   public static VertexConsumer getItemGlintConsumer(VertexConsumerProvider vertexConsumers, RenderLayer layer, boolean solid, boolean glint) {
      if (glint) {
         return MinecraftClient.isFabulousGraphicsOrBetter() && layer == TexturedRenderLayers.getItemEntityTranslucentCull() ? VertexConsumers.union(vertexConsumers.getBuffer(RenderLayer.getGlintTranslucent()), vertexConsumers.getBuffer(layer)) : VertexConsumers.union(vertexConsumers.getBuffer(solid ? RenderLayer.getGlint() : RenderLayer.getEntityGlint()), vertexConsumers.getBuffer(layer));
      } else {
         return vertexConsumers.getBuffer(layer);
      }
   }

   public static VertexConsumer getDirectItemGlintConsumer(VertexConsumerProvider provider, RenderLayer layer, boolean solid, boolean glint) {
      return glint ? VertexConsumers.union(provider.getBuffer(solid ? RenderLayer.getDirectGlint() : RenderLayer.getDirectEntityGlint()), provider.getBuffer(layer)) : provider.getBuffer(layer);
   }

   private void renderBakedItemQuads(MatrixStack matrices, VertexConsumer vertices, List quads, ItemStack stack, int light, int overlay) {
      boolean bl = !stack.isEmpty();
      MatrixStack.Entry lv = matrices.peek();
      Iterator var9 = quads.iterator();

      while(var9.hasNext()) {
         BakedQuad lv2 = (BakedQuad)var9.next();
         int k = -1;
         if (bl && lv2.hasColor()) {
            k = this.colors.getColor(stack, lv2.getColorIndex());
         }

         float f = (float)(k >> 16 & 255) / 255.0F;
         float g = (float)(k >> 8 & 255) / 255.0F;
         float h = (float)(k & 255) / 255.0F;
         vertices.quad(lv, lv2, f, g, h, light, overlay);
      }

   }

   public BakedModel getModel(ItemStack stack, @Nullable World world, @Nullable LivingEntity entity, int seed) {
      BakedModel lv;
      if (stack.isOf(Items.TRIDENT)) {
         lv = this.models.getModelManager().getModel(TRIDENT_IN_HAND);
      } else if (stack.isOf(Items.SPYGLASS)) {
         lv = this.models.getModelManager().getModel(SPYGLASS_IN_HAND);
      } else {
         lv = this.models.getModel(stack);
      }

      ClientWorld lv2 = world instanceof ClientWorld ? (ClientWorld)world : null;
      BakedModel lv3 = lv.getOverrides().apply(lv, stack, lv2, entity, seed);
      return lv3 == null ? this.models.getModelManager().getMissingModel() : lv3;
   }

   public void renderItem(ItemStack stack, ModelTransformationMode transformationType, int light, int overlay, MatrixStack matrices, VertexConsumerProvider vertexConsumers, @Nullable World world, int seed) {
      this.renderItem((LivingEntity)null, stack, transformationType, false, matrices, vertexConsumers, world, light, overlay, seed);
   }

   public void renderItem(@Nullable LivingEntity entity, ItemStack item, ModelTransformationMode renderMode, boolean leftHanded, MatrixStack matrices, VertexConsumerProvider vertexConsumers, @Nullable World world, int light, int overlay, int seed) {
      if (!item.isEmpty()) {
         BakedModel lv = this.getModel(item, world, entity, seed);
         this.renderItem(item, renderMode, leftHanded, matrices, vertexConsumers, light, overlay, lv);
      }
   }

   public void renderGuiItemIcon(MatrixStack matrices, ItemStack stack, int x, int y) {
      this.renderGuiItemModel(matrices, stack, x, y, this.getModel(stack, (World)null, (LivingEntity)null, 0));
   }

   protected void renderGuiItemModel(MatrixStack matrices, ItemStack stack, int x, int y, BakedModel model) {
      matrices.push();
      matrices.translate((float)x, (float)y, 100.0F);
      matrices.translate(8.0F, 8.0F, 0.0F);
      matrices.multiplyPositionMatrix((new Matrix4f()).scaling(1.0F, -1.0F, 1.0F));
      matrices.scale(16.0F, 16.0F, 16.0F);
      VertexConsumerProvider.Immediate lv = this.client.getBufferBuilders().getEntityVertexConsumers();
      boolean bl = !model.isSideLit();
      if (bl) {
         DiffuseLighting.disableGuiDepthLighting();
      }

      this.renderItem(stack, ModelTransformationMode.GUI, false, matrices, lv, 15728880, OverlayTexture.DEFAULT_UV, model);
      lv.draw();
      RenderSystem.enableDepthTest();
      if (bl) {
         DiffuseLighting.enableGuiDepthLighting();
      }

      matrices.pop();
   }

   public void renderInGuiWithOverrides(MatrixStack matrices, ItemStack stack, int x, int y) {
      this.innerRenderInGui(matrices, this.client.player, this.client.world, stack, x, y, 0);
   }

   public void renderInGuiWithOverrides(MatrixStack matrices, ItemStack stack, int x, int y, int seed) {
      this.innerRenderInGui(matrices, this.client.player, this.client.world, stack, x, y, seed);
   }

   public void renderInGuiWithOverrides(MatrixStack matrices, ItemStack stack, int x, int y, int seed, int depth) {
      this.innerRenderInGui(matrices, this.client.player, this.client.world, stack, x, y, seed, depth);
   }

   public void renderInGui(MatrixStack matrices, ItemStack stack, int x, int y) {
      this.innerRenderInGui(matrices, (LivingEntity)null, this.client.world, stack, x, y, 0);
   }

   public void renderInGuiWithOverrides(MatrixStack matrices, LivingEntity entity, ItemStack stack, int x, int y, int seed) {
      this.innerRenderInGui(matrices, entity, entity.world, stack, x, y, seed);
   }

   private void innerRenderInGui(MatrixStack matrices, @Nullable LivingEntity entity, @Nullable World world, ItemStack stack, int x, int y, int seed) {
      this.innerRenderInGui(matrices, entity, world, stack, x, y, seed, 0);
   }

   private void innerRenderInGui(MatrixStack matrices, @Nullable LivingEntity entity, @Nullable World world, ItemStack stack, int x, int y, int seed, int depth) {
      if (!stack.isEmpty()) {
         BakedModel lv = this.getModel(stack, world, entity, seed);
         matrices.push();
         matrices.translate(0.0F, 0.0F, (float)(50 + (lv.hasDepth() ? depth : 0)));

         try {
            this.renderGuiItemModel(matrices, stack, x, y, lv);
         } catch (Throwable var13) {
            CrashReport lv2 = CrashReport.create(var13, "Rendering item");
            CrashReportSection lv3 = lv2.addElement("Item being rendered");
            lv3.add("Item Type", () -> {
               return String.valueOf(stack.getItem());
            });
            lv3.add("Item Damage", () -> {
               return String.valueOf(stack.getDamage());
            });
            lv3.add("Item NBT", () -> {
               return String.valueOf(stack.getNbt());
            });
            lv3.add("Item Foil", () -> {
               return String.valueOf(stack.hasGlint());
            });
            throw new CrashException(lv2);
         }

         matrices.pop();
      }
   }

   public void renderGuiItemOverlay(MatrixStack matrices, TextRenderer textRenderer, ItemStack stack, int x, int y) {
      this.renderGuiItemOverlay(matrices, textRenderer, stack, x, y, (String)null);
   }

   public void renderGuiItemOverlay(MatrixStack matrices, TextRenderer textRenderer, ItemStack stack, int x, int y, @Nullable String countLabel) {
      if (!stack.isEmpty()) {
         matrices.push();
         if (stack.getCount() != 1 || countLabel != null) {
            String string2 = countLabel == null ? String.valueOf(stack.getCount()) : countLabel;
            matrices.translate(0.0F, 0.0F, 200.0F);
            VertexConsumerProvider.Immediate lv = VertexConsumerProvider.immediate(Tessellator.getInstance().getBuffer());
            textRenderer.draw((String)string2, (float)(x + 19 - 2 - textRenderer.getWidth(string2)), (float)(y + 6 + 3), 16777215, true, matrices.peek().getPositionMatrix(), lv, TextRenderer.TextLayerType.NORMAL, 0, 15728880);
            lv.draw();
         }

         int m;
         int n;
         if (stack.isItemBarVisible()) {
            RenderSystem.disableDepthTest();
            int k = stack.getItemBarStep();
            int l = stack.getItemBarColor();
            m = x + 2;
            n = y + 13;
            DrawableHelper.fill(matrices, m, n, m + 13, n + 2, -16777216);
            DrawableHelper.fill(matrices, m, n, m + k, n + 1, l | -16777216);
            RenderSystem.enableDepthTest();
         }

         ClientPlayerEntity lv2 = this.client.player;
         float f = lv2 == null ? 0.0F : lv2.getItemCooldownManager().getCooldownProgress(stack.getItem(), this.client.getTickDelta());
         if (f > 0.0F) {
            RenderSystem.disableDepthTest();
            m = y + MathHelper.floor(16.0F * (1.0F - f));
            n = m + MathHelper.ceil(16.0F * f);
            DrawableHelper.fill(matrices, x, m, x + 16, n, Integer.MAX_VALUE);
            RenderSystem.enableDepthTest();
         }

         matrices.pop();
      }
   }

   public void reload(ResourceManager manager) {
      this.models.reloadModels();
   }

   static {
      WITHOUT_MODELS = Sets.newHashSet(new Item[]{Items.AIR});
      TRIDENT = ModelIdentifier.ofVanilla("trident", "inventory");
      TRIDENT_IN_HAND = ModelIdentifier.ofVanilla("trident_in_hand", "inventory");
      SPYGLASS = ModelIdentifier.ofVanilla("spyglass", "inventory");
      SPYGLASS_IN_HAND = ModelIdentifier.ofVanilla("spyglass_in_hand", "inventory");
   }
}
