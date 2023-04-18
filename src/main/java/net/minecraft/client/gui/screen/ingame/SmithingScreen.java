package net.minecraft.client.gui.screen.ingame;

import java.util.List;
import java.util.Optional;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SmithingTemplateItem;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.SmithingScreenHandler;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;

@Environment(EnvType.CLIENT)
public class SmithingScreen extends ForgingScreen {
   private static final Identifier TEXTURE = new Identifier("textures/gui/container/smithing.png");
   private static final Identifier EMPTY_SLOT_SMITHING_TEMPLATE_ARMOR_TRIM_TEXTURE = new Identifier("item/empty_slot_smithing_template_armor_trim");
   private static final Identifier EMPTY_SLOT_SMITHING_TEMPLATE_NETHERITE_UPGRADE_TEXTURE = new Identifier("item/empty_slot_smithing_template_netherite_upgrade");
   private static final Text MISSING_TEMPLATE_TOOLTIP = Text.translatable("container.upgrade.missing_template_tooltip");
   private static final Text ERROR_TOOLTIP = Text.translatable("container.upgrade.error_tooltip");
   private static final List EMPTY_SLOT_TEXTURES;
   private static final int field_42057 = 44;
   private static final int field_42058 = 15;
   private static final int field_42059 = 28;
   private static final int field_42060 = 21;
   private static final int field_42061 = 65;
   private static final int field_42062 = 46;
   private static final int field_42063 = 115;
   public static final int field_42068 = 210;
   public static final int field_42047 = 25;
   public static final Quaternionf ARMOR_STAND_ROTATION;
   public static final int field_42049 = 25;
   public static final int field_42050 = 75;
   public static final int field_42051 = 141;
   private final CyclingSlotIcon templateSlotIcon = new CyclingSlotIcon(0);
   private final CyclingSlotIcon baseSlotIcon = new CyclingSlotIcon(1);
   private final CyclingSlotIcon additionsSlotIcon = new CyclingSlotIcon(2);
   @Nullable
   private ArmorStandEntity armorStand;

   public SmithingScreen(SmithingScreenHandler handler, PlayerInventory playerInventory, Text title) {
      super(handler, playerInventory, title, TEXTURE);
      this.titleX = 44;
      this.titleY = 15;
   }

   protected void setup() {
      this.armorStand = new ArmorStandEntity(this.client.world, 0.0, 0.0, 0.0);
      this.armorStand.setHideBasePlate(true);
      this.armorStand.setShowArms(true);
      this.armorStand.bodyYaw = 210.0F;
      this.armorStand.setPitch(25.0F);
      this.armorStand.headYaw = this.armorStand.getYaw();
      this.armorStand.prevHeadYaw = this.armorStand.getYaw();
      this.equipArmorStand(((SmithingScreenHandler)this.handler).getSlot(3).getStack());
   }

   public void handledScreenTick() {
      super.handledScreenTick();
      Optional optional = this.getSmithingTemplate();
      this.templateSlotIcon.updateTexture(EMPTY_SLOT_TEXTURES);
      this.baseSlotIcon.updateTexture((List)optional.map(SmithingTemplateItem::getEmptyBaseSlotTextures).orElse(List.of()));
      this.additionsSlotIcon.updateTexture((List)optional.map(SmithingTemplateItem::getEmptyAdditionsSlotTextures).orElse(List.of()));
   }

   private Optional getSmithingTemplate() {
      ItemStack lv = ((SmithingScreenHandler)this.handler).getSlot(0).getStack();
      if (!lv.isEmpty()) {
         Item var3 = lv.getItem();
         if (var3 instanceof SmithingTemplateItem) {
            SmithingTemplateItem lv2 = (SmithingTemplateItem)var3;
            return Optional.of(lv2);
         }
      }

      return Optional.empty();
   }

   public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
      super.render(matrices, mouseX, mouseY, delta);
      this.renderSlotTooltip(matrices, mouseX, mouseY);
   }

   protected void drawBackground(MatrixStack matrices, float delta, int mouseX, int mouseY) {
      super.drawBackground(matrices, delta, mouseX, mouseY);
      this.templateSlotIcon.render(this.handler, matrices, delta, this.x, this.y);
      this.baseSlotIcon.render(this.handler, matrices, delta, this.x, this.y);
      this.additionsSlotIcon.render(this.handler, matrices, delta, this.x, this.y);
      InventoryScreen.drawEntity(matrices, this.x + 141, this.y + 75, 25, ARMOR_STAND_ROTATION, (Quaternionf)null, this.armorStand);
   }

   public void onSlotUpdate(ScreenHandler handler, int slotId, ItemStack stack) {
      if (slotId == 3) {
         this.equipArmorStand(stack);
      }

   }

   private void equipArmorStand(ItemStack stack) {
      if (this.armorStand != null) {
         EquipmentSlot[] var2 = EquipmentSlot.values();
         int var3 = var2.length;

         for(int var4 = 0; var4 < var3; ++var4) {
            EquipmentSlot lv = var2[var4];
            this.armorStand.equipStack(lv, ItemStack.EMPTY);
         }

         if (!stack.isEmpty()) {
            ItemStack lv2 = stack.copy();
            Item var8 = stack.getItem();
            if (var8 instanceof ArmorItem) {
               ArmorItem lv3 = (ArmorItem)var8;
               this.armorStand.equipStack(lv3.getSlotType(), lv2);
            } else {
               this.armorStand.equipStack(EquipmentSlot.OFFHAND, lv2);
            }
         }

      }
   }

   protected void drawInvalidRecipeArrow(MatrixStack matrices, int x, int y) {
      if (this.hasInvalidRecipe()) {
         drawTexture(matrices, x + 65, y + 46, this.backgroundWidth, 0, 28, 21);
      }

   }

   private void renderSlotTooltip(MatrixStack matrices, int mouseX, int mouseY) {
      Optional optional = Optional.empty();
      if (this.hasInvalidRecipe() && this.isPointWithinBounds(65, 46, 28, 21, (double)mouseX, (double)mouseY)) {
         optional = Optional.of(ERROR_TOOLTIP);
      }

      if (this.focusedSlot != null) {
         ItemStack lv = ((SmithingScreenHandler)this.handler).getSlot(0).getStack();
         ItemStack lv2 = this.focusedSlot.getStack();
         if (lv.isEmpty()) {
            if (this.focusedSlot.id == 0) {
               optional = Optional.of(MISSING_TEMPLATE_TOOLTIP);
            }
         } else {
            Item var8 = lv.getItem();
            if (var8 instanceof SmithingTemplateItem) {
               SmithingTemplateItem lv3 = (SmithingTemplateItem)var8;
               if (lv2.isEmpty()) {
                  if (this.focusedSlot.id == 1) {
                     optional = Optional.of(lv3.getBaseSlotDescription());
                  } else if (this.focusedSlot.id == 2) {
                     optional = Optional.of(lv3.getAdditionsSlotDescription());
                  }
               }
            }
         }
      }

      optional.ifPresent((text) -> {
         this.renderOrderedTooltip(matrices, this.textRenderer.wrapLines(text, 115), mouseX, mouseY);
      });
   }

   private boolean hasInvalidRecipe() {
      return ((SmithingScreenHandler)this.handler).getSlot(0).hasStack() && ((SmithingScreenHandler)this.handler).getSlot(1).hasStack() && ((SmithingScreenHandler)this.handler).getSlot(2).hasStack() && !((SmithingScreenHandler)this.handler).getSlot(((SmithingScreenHandler)this.handler).getResultSlotIndex()).hasStack();
   }

   static {
      EMPTY_SLOT_TEXTURES = List.of(EMPTY_SLOT_SMITHING_TEMPLATE_ARMOR_TRIM_TEXTURE, EMPTY_SLOT_SMITHING_TEMPLATE_NETHERITE_UPGRADE_TEXTURE);
      ARMOR_STAND_ROTATION = (new Quaternionf()).rotationXYZ(0.43633232F, 0.0F, 3.1415927F);
   }
}
