package net.minecraft.client.render.item;

import com.mojang.authlib.GameProfile;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.AbstractBannerBlock;
import net.minecraft.block.AbstractSkullBlock;
import net.minecraft.block.BedBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.ShulkerBoxBlock;
import net.minecraft.block.SkullBlock;
import net.minecraft.block.entity.BannerBlockEntity;
import net.minecraft.block.entity.BedBlockEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.block.entity.ConduitBlockEntity;
import net.minecraft.block.entity.DecoratedPotBlockEntity;
import net.minecraft.block.entity.EnderChestBlockEntity;
import net.minecraft.block.entity.ShulkerBoxBlockEntity;
import net.minecraft.block.entity.SkullBlockEntity;
import net.minecraft.block.entity.TrappedChestBlockEntity;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BannerBlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRenderDispatcher;
import net.minecraft.client.render.block.entity.SkullBlockEntityModel;
import net.minecraft.client.render.block.entity.SkullBlockEntityRenderer;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.entity.model.EntityModelLoader;
import net.minecraft.client.render.entity.model.ShieldEntityModel;
import net.minecraft.client.render.entity.model.TridentEntityModel;
import net.minecraft.client.render.model.ModelLoader;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.SpriteIdentifier;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.ShieldItem;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.SynchronousResourceReloader;
import net.minecraft.util.DyeColor;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import org.apache.commons.lang3.StringUtils;

@Environment(EnvType.CLIENT)
public class BuiltinModelItemRenderer implements SynchronousResourceReloader {
   private static final ShulkerBoxBlockEntity[] RENDER_SHULKER_BOX_DYED = (ShulkerBoxBlockEntity[])Arrays.stream(DyeColor.values()).sorted(Comparator.comparingInt(DyeColor::getId)).map((color) -> {
      return new ShulkerBoxBlockEntity(color, BlockPos.ORIGIN, Blocks.SHULKER_BOX.getDefaultState());
   }).toArray((i) -> {
      return new ShulkerBoxBlockEntity[i];
   });
   private static final ShulkerBoxBlockEntity RENDER_SHULKER_BOX;
   private final ChestBlockEntity renderChestNormal;
   private final ChestBlockEntity renderChestTrapped;
   private final EnderChestBlockEntity renderChestEnder;
   private final BannerBlockEntity renderBanner;
   private final BedBlockEntity renderBed;
   private final ConduitBlockEntity renderConduit;
   private final DecoratedPotBlockEntity renderDecoratedPot;
   private ShieldEntityModel modelShield;
   private TridentEntityModel modelTrident;
   private Map skullModels;
   private final BlockEntityRenderDispatcher blockEntityRenderDispatcher;
   private final EntityModelLoader entityModelLoader;

   public BuiltinModelItemRenderer(BlockEntityRenderDispatcher blockEntityRenderDispatcher, EntityModelLoader entityModelLoader) {
      this.renderChestNormal = new ChestBlockEntity(BlockPos.ORIGIN, Blocks.CHEST.getDefaultState());
      this.renderChestTrapped = new TrappedChestBlockEntity(BlockPos.ORIGIN, Blocks.TRAPPED_CHEST.getDefaultState());
      this.renderChestEnder = new EnderChestBlockEntity(BlockPos.ORIGIN, Blocks.ENDER_CHEST.getDefaultState());
      this.renderBanner = new BannerBlockEntity(BlockPos.ORIGIN, Blocks.WHITE_BANNER.getDefaultState());
      this.renderBed = new BedBlockEntity(BlockPos.ORIGIN, Blocks.RED_BED.getDefaultState());
      this.renderConduit = new ConduitBlockEntity(BlockPos.ORIGIN, Blocks.CONDUIT.getDefaultState());
      this.renderDecoratedPot = new DecoratedPotBlockEntity(BlockPos.ORIGIN, Blocks.DECORATED_POT.getDefaultState());
      this.blockEntityRenderDispatcher = blockEntityRenderDispatcher;
      this.entityModelLoader = entityModelLoader;
   }

   public void reload(ResourceManager manager) {
      this.modelShield = new ShieldEntityModel(this.entityModelLoader.getModelPart(EntityModelLayers.SHIELD));
      this.modelTrident = new TridentEntityModel(this.entityModelLoader.getModelPart(EntityModelLayers.TRIDENT));
      this.skullModels = SkullBlockEntityRenderer.getModels(this.entityModelLoader);
   }

   public void render(ItemStack stack, ModelTransformationMode mode, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
      Item lv = stack.getItem();
      if (lv instanceof BlockItem) {
         Block lv2 = ((BlockItem)lv).getBlock();
         if (lv2 instanceof AbstractSkullBlock) {
            GameProfile gameProfile = null;
            if (stack.hasNbt()) {
               NbtCompound lv3 = stack.getNbt();
               if (lv3.contains("SkullOwner", NbtElement.COMPOUND_TYPE)) {
                  gameProfile = NbtHelper.toGameProfile(lv3.getCompound("SkullOwner"));
               } else if (lv3.contains("SkullOwner", NbtElement.STRING_TYPE) && !StringUtils.isBlank(lv3.getString("SkullOwner"))) {
                  gameProfile = new GameProfile((UUID)null, lv3.getString("SkullOwner"));
                  lv3.remove("SkullOwner");
                  SkullBlockEntity.loadProperties(gameProfile, (profile) -> {
                     lv3.put("SkullOwner", NbtHelper.writeGameProfile(new NbtCompound(), profile));
                  });
               }
            }

            SkullBlock.SkullType lv4 = ((AbstractSkullBlock)lv2).getSkullType();
            SkullBlockEntityModel lv5 = (SkullBlockEntityModel)this.skullModels.get(lv4);
            RenderLayer lv6 = SkullBlockEntityRenderer.getRenderLayer(lv4, gameProfile);
            SkullBlockEntityRenderer.renderSkull((Direction)null, 180.0F, 0.0F, matrices, vertexConsumers, light, lv5, lv6);
         } else {
            BlockState lv7 = lv2.getDefaultState();
            Object lv8;
            if (lv2 instanceof AbstractBannerBlock) {
               this.renderBanner.readFrom(stack, ((AbstractBannerBlock)lv2).getColor());
               lv8 = this.renderBanner;
            } else if (lv2 instanceof BedBlock) {
               this.renderBed.setColor(((BedBlock)lv2).getColor());
               lv8 = this.renderBed;
            } else if (lv7.isOf(Blocks.CONDUIT)) {
               lv8 = this.renderConduit;
            } else if (lv7.isOf(Blocks.CHEST)) {
               lv8 = this.renderChestNormal;
            } else if (lv7.isOf(Blocks.ENDER_CHEST)) {
               lv8 = this.renderChestEnder;
            } else if (lv7.isOf(Blocks.TRAPPED_CHEST)) {
               lv8 = this.renderChestTrapped;
            } else if (lv7.isOf(Blocks.DECORATED_POT)) {
               this.renderDecoratedPot.readNbtFromStack(stack);
               lv8 = this.renderDecoratedPot;
            } else {
               if (!(lv2 instanceof ShulkerBoxBlock)) {
                  return;
               }

               DyeColor lv9 = ShulkerBoxBlock.getColor(lv);
               if (lv9 == null) {
                  lv8 = RENDER_SHULKER_BOX;
               } else {
                  lv8 = RENDER_SHULKER_BOX_DYED[lv9.getId()];
               }
            }

            this.blockEntityRenderDispatcher.renderEntity((BlockEntity)lv8, matrices, vertexConsumers, light, overlay);
         }
      } else {
         if (stack.isOf(Items.SHIELD)) {
            boolean bl = BlockItem.getBlockEntityNbt(stack) != null;
            matrices.push();
            matrices.scale(1.0F, -1.0F, -1.0F);
            SpriteIdentifier lv10 = bl ? ModelLoader.SHIELD_BASE : ModelLoader.SHIELD_BASE_NO_PATTERN;
            VertexConsumer lv11 = lv10.getSprite().getTextureSpecificVertexConsumer(ItemRenderer.getDirectItemGlintConsumer(vertexConsumers, this.modelShield.getLayer(lv10.getAtlasId()), true, stack.hasGlint()));
            this.modelShield.getHandle().render(matrices, lv11, light, overlay, 1.0F, 1.0F, 1.0F, 1.0F);
            if (bl) {
               List list = BannerBlockEntity.getPatternsFromNbt(ShieldItem.getColor(stack), BannerBlockEntity.getPatternListNbt(stack));
               BannerBlockEntityRenderer.renderCanvas(matrices, vertexConsumers, light, overlay, this.modelShield.getPlate(), lv10, false, list, stack.hasGlint());
            } else {
               this.modelShield.getPlate().render(matrices, lv11, light, overlay, 1.0F, 1.0F, 1.0F, 1.0F);
            }

            matrices.pop();
         } else if (stack.isOf(Items.TRIDENT)) {
            matrices.push();
            matrices.scale(1.0F, -1.0F, -1.0F);
            VertexConsumer lv12 = ItemRenderer.getDirectItemGlintConsumer(vertexConsumers, this.modelTrident.getLayer(TridentEntityModel.TEXTURE), false, stack.hasGlint());
            this.modelTrident.render(matrices, lv12, light, overlay, 1.0F, 1.0F, 1.0F, 1.0F);
            matrices.pop();
         }

      }
   }

   static {
      RENDER_SHULKER_BOX = new ShulkerBoxBlockEntity(BlockPos.ORIGIN, Blocks.SHULKER_BOX.getDefaultState());
   }
}
