package net.minecraft.item;

import java.util.List;
import java.util.Optional;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.decoration.AbstractDecorationEntity;
import net.minecraft.entity.decoration.GlowItemFrameEntity;
import net.minecraft.entity.decoration.ItemFrameEntity;
import net.minecraft.entity.decoration.painting.PaintingEntity;
import net.minecraft.entity.decoration.painting.PaintingVariant;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;
import org.jetbrains.annotations.Nullable;

public class DecorationItem extends Item {
   private static final Text RANDOM_TEXT;
   private final EntityType entityType;

   public DecorationItem(EntityType type, Item.Settings settings) {
      super(settings);
      this.entityType = type;
   }

   public ActionResult useOnBlock(ItemUsageContext context) {
      BlockPos lv = context.getBlockPos();
      Direction lv2 = context.getSide();
      BlockPos lv3 = lv.offset(lv2);
      PlayerEntity lv4 = context.getPlayer();
      ItemStack lv5 = context.getStack();
      if (lv4 != null && !this.canPlaceOn(lv4, lv2, lv5, lv3)) {
         return ActionResult.FAIL;
      } else {
         World lv6 = context.getWorld();
         Object lv7;
         if (this.entityType == EntityType.PAINTING) {
            Optional optional = PaintingEntity.placePainting(lv6, lv3, lv2);
            if (optional.isEmpty()) {
               return ActionResult.CONSUME;
            }

            lv7 = (AbstractDecorationEntity)optional.get();
         } else if (this.entityType == EntityType.ITEM_FRAME) {
            lv7 = new ItemFrameEntity(lv6, lv3, lv2);
         } else {
            if (this.entityType != EntityType.GLOW_ITEM_FRAME) {
               return ActionResult.success(lv6.isClient);
            }

            lv7 = new GlowItemFrameEntity(lv6, lv3, lv2);
         }

         NbtCompound lv8 = lv5.getNbt();
         if (lv8 != null) {
            EntityType.loadFromEntityNbt(lv6, lv4, (Entity)lv7, lv8);
         }

         if (((AbstractDecorationEntity)lv7).canStayAttached()) {
            if (!lv6.isClient) {
               ((AbstractDecorationEntity)lv7).onPlace();
               lv6.emitGameEvent(lv4, GameEvent.ENTITY_PLACE, ((AbstractDecorationEntity)lv7).getPos());
               lv6.spawnEntity((Entity)lv7);
            }

            lv5.decrement(1);
            return ActionResult.success(lv6.isClient);
         } else {
            return ActionResult.CONSUME;
         }
      }
   }

   protected boolean canPlaceOn(PlayerEntity player, Direction side, ItemStack stack, BlockPos pos) {
      return !side.getAxis().isVertical() && player.canPlaceOn(pos, side, stack);
   }

   public void appendTooltip(ItemStack stack, @Nullable World world, List tooltip, TooltipContext context) {
      super.appendTooltip(stack, world, tooltip, context);
      if (this.entityType == EntityType.PAINTING) {
         NbtCompound lv = stack.getNbt();
         if (lv != null && lv.contains("EntityTag", NbtElement.COMPOUND_TYPE)) {
            NbtCompound lv2 = lv.getCompound("EntityTag");
            PaintingEntity.readVariantFromNbt(lv2).ifPresentOrElse((variant) -> {
               variant.getKey().ifPresent((key) -> {
                  tooltip.add(Text.translatable(key.getValue().toTranslationKey("painting", "title")).formatted(Formatting.YELLOW));
                  tooltip.add(Text.translatable(key.getValue().toTranslationKey("painting", "author")).formatted(Formatting.GRAY));
               });
               tooltip.add(Text.translatable("painting.dimensions", MathHelper.ceilDiv(((PaintingVariant)variant.value()).getWidth(), 16), MathHelper.ceilDiv(((PaintingVariant)variant.value()).getHeight(), 16)));
            }, () -> {
               tooltip.add(RANDOM_TEXT);
            });
         } else if (context.isCreative()) {
            tooltip.add(RANDOM_TEXT);
         }
      }

   }

   static {
      RANDOM_TEXT = Text.translatable("painting.random").formatted(Formatting.GRAY);
   }
}
