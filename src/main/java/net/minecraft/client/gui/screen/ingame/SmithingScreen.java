/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.client.gui.screen.ingame;

import java.util.List;
import java.util.Optional;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.CyclingSlotIcon;
import net.minecraft.client.gui.screen.ingame.ForgingScreen;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SmithingTemplateItem;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.SmithingScreenHandler;
import net.minecraft.text.StringVisitable;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;
import org.joml.Vector3f;

@Environment(value=EnvType.CLIENT)
public class SmithingScreen
extends ForgingScreen<SmithingScreenHandler> {
    private static final Identifier ERROR_TEXTURE = Identifier.method_60656("container/smithing/error");
    private static final Identifier EMPTY_SLOT_SMITHING_TEMPLATE_ARMOR_TRIM_TEXTURE = Identifier.method_60656("item/empty_slot_smithing_template_armor_trim");
    private static final Identifier EMPTY_SLOT_SMITHING_TEMPLATE_NETHERITE_UPGRADE_TEXTURE = Identifier.method_60656("item/empty_slot_smithing_template_netherite_upgrade");
    private static final Text MISSING_TEMPLATE_TOOLTIP = Text.translatable("container.upgrade.missing_template_tooltip");
    private static final Text ERROR_TOOLTIP = Text.translatable("container.upgrade.error_tooltip");
    private static final List<Identifier> EMPTY_SLOT_TEXTURES = List.of(EMPTY_SLOT_SMITHING_TEMPLATE_ARMOR_TRIM_TEXTURE, EMPTY_SLOT_SMITHING_TEMPLATE_NETHERITE_UPGRADE_TEXTURE);
    private static final int field_42057 = 44;
    private static final int field_42058 = 15;
    private static final int field_42059 = 28;
    private static final int field_42060 = 21;
    private static final int field_42061 = 65;
    private static final int field_42062 = 46;
    private static final int field_42063 = 115;
    private static final int field_42068 = 210;
    private static final int field_42047 = 25;
    private static final Vector3f field_45497 = new Vector3f();
    private static final Quaternionf ARMOR_STAND_ROTATION = new Quaternionf().rotationXYZ(0.43633232f, 0.0f, (float)Math.PI);
    private static final int field_42049 = 25;
    private static final int field_42050 = 75;
    private static final int field_42051 = 141;
    private final CyclingSlotIcon templateSlotIcon = new CyclingSlotIcon(0);
    private final CyclingSlotIcon baseSlotIcon = new CyclingSlotIcon(1);
    private final CyclingSlotIcon additionsSlotIcon = new CyclingSlotIcon(2);
    @Nullable
    private ArmorStandEntity armorStand;

    public SmithingScreen(SmithingScreenHandler handler, PlayerInventory playerInventory, Text title) {
        super(handler, playerInventory, title, Identifier.method_60656("textures/gui/container/smithing.png"));
        this.titleX = 44;
        this.titleY = 15;
    }

    @Override
    protected void setup() {
        this.armorStand = new ArmorStandEntity(this.client.world, 0.0, 0.0, 0.0);
        this.armorStand.setHideBasePlate(true);
        this.armorStand.setShowArms(true);
        this.armorStand.bodyYaw = 210.0f;
        this.armorStand.setPitch(25.0f);
        this.armorStand.headYaw = this.armorStand.getYaw();
        this.armorStand.prevHeadYaw = this.armorStand.getYaw();
        this.equipArmorStand(((SmithingScreenHandler)this.handler).getSlot(3).getStack());
    }

    @Override
    public void handledScreenTick() {
        super.handledScreenTick();
        Optional<SmithingTemplateItem> optional = this.getSmithingTemplate();
        this.templateSlotIcon.updateTexture(EMPTY_SLOT_TEXTURES);
        this.baseSlotIcon.updateTexture(optional.map(SmithingTemplateItem::getEmptyBaseSlotTextures).orElse(List.of()));
        this.additionsSlotIcon.updateTexture(optional.map(SmithingTemplateItem::getEmptyAdditionsSlotTextures).orElse(List.of()));
    }

    private Optional<SmithingTemplateItem> getSmithingTemplate() {
        Item item;
        ItemStack lv = ((SmithingScreenHandler)this.handler).getSlot(0).getStack();
        if (!lv.isEmpty() && (item = lv.getItem()) instanceof SmithingTemplateItem) {
            SmithingTemplateItem lv2 = (SmithingTemplateItem)item;
            return Optional.of(lv2);
        }
        return Optional.empty();
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        this.renderSlotTooltip(context, mouseX, mouseY);
    }

    @Override
    protected void drawBackground(DrawContext context, float delta, int mouseX, int mouseY) {
        super.drawBackground(context, delta, mouseX, mouseY);
        this.templateSlotIcon.render(this.handler, context, delta, this.x, this.y);
        this.baseSlotIcon.render(this.handler, context, delta, this.x, this.y);
        this.additionsSlotIcon.render(this.handler, context, delta, this.x, this.y);
        InventoryScreen.drawEntity(context, this.x + 141, this.y + 75, 25.0f, field_45497, ARMOR_STAND_ROTATION, null, this.armorStand);
    }

    @Override
    public void onSlotUpdate(ScreenHandler handler, int slotId, ItemStack stack) {
        if (slotId == 3) {
            this.equipArmorStand(stack);
        }
    }

    private void equipArmorStand(ItemStack stack) {
        if (this.armorStand == null) {
            return;
        }
        for (EquipmentSlot lv : EquipmentSlot.values()) {
            this.armorStand.equipStack(lv, ItemStack.EMPTY);
        }
        if (!stack.isEmpty()) {
            ItemStack lv2 = stack.copy();
            Item item = stack.getItem();
            if (item instanceof ArmorItem) {
                ArmorItem lv3 = (ArmorItem)item;
                this.armorStand.equipStack(lv3.getSlotType(), lv2);
            } else {
                this.armorStand.equipStack(EquipmentSlot.OFFHAND, lv2);
            }
        }
    }

    @Override
    protected void drawInvalidRecipeArrow(DrawContext context, int x, int y) {
        if (this.hasInvalidRecipe()) {
            context.drawGuiTexture(ERROR_TEXTURE, x + 65, y + 46, 28, 21);
        }
    }

    private void renderSlotTooltip(DrawContext context, int mouseX, int mouseY) {
        Optional<Text> optional = Optional.empty();
        if (this.hasInvalidRecipe() && this.isPointWithinBounds(65, 46, 28, 21, mouseX, mouseY)) {
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
                Item item = lv.getItem();
                if (item instanceof SmithingTemplateItem) {
                    SmithingTemplateItem lv3 = (SmithingTemplateItem)item;
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
        optional.ifPresent(text -> context.drawOrderedTooltip(this.textRenderer, this.textRenderer.wrapLines((StringVisitable)text, 115), mouseX, mouseY));
    }

    private boolean hasInvalidRecipe() {
        return ((SmithingScreenHandler)this.handler).getSlot(0).hasStack() && ((SmithingScreenHandler)this.handler).getSlot(1).hasStack() && ((SmithingScreenHandler)this.handler).getSlot(2).hasStack() && !((SmithingScreenHandler)this.handler).getSlot(((SmithingScreenHandler)this.handler).getResultSlotIndex()).hasStack();
    }
}

