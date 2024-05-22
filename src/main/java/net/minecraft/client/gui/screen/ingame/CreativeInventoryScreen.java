/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.client.gui.screen.ingame;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Pair;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.AbstractInventoryScreen;
import net.minecraft.client.gui.screen.ingame.CreativeInventoryListener;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.item.TooltipType;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.option.HotbarStorage;
import net.minecraft.client.option.HotbarStorageEntry;
import net.minecraft.client.search.SearchManager;
import net.minecraft.client.search.SearchProvider;
import net.minecraft.client.util.InputUtil;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemGroups;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.resource.featuretoggle.FeatureSet;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.Unit;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

@Environment(value=EnvType.CLIENT)
public class CreativeInventoryScreen
extends AbstractInventoryScreen<CreativeScreenHandler> {
    private static final Identifier SCROLLER_TEXTURE = Identifier.method_60656("container/creative_inventory/scroller");
    private static final Identifier SCROLLER_DISABLED_TEXTURE = Identifier.method_60656("container/creative_inventory/scroller_disabled");
    private static final Identifier[] TAB_TOP_UNSELECTED_TEXTURES = new Identifier[]{Identifier.method_60656("container/creative_inventory/tab_top_unselected_1"), Identifier.method_60656("container/creative_inventory/tab_top_unselected_2"), Identifier.method_60656("container/creative_inventory/tab_top_unselected_3"), Identifier.method_60656("container/creative_inventory/tab_top_unselected_4"), Identifier.method_60656("container/creative_inventory/tab_top_unselected_5"), Identifier.method_60656("container/creative_inventory/tab_top_unselected_6"), Identifier.method_60656("container/creative_inventory/tab_top_unselected_7")};
    private static final Identifier[] TAB_TOP_SELECTED_TEXTURES = new Identifier[]{Identifier.method_60656("container/creative_inventory/tab_top_selected_1"), Identifier.method_60656("container/creative_inventory/tab_top_selected_2"), Identifier.method_60656("container/creative_inventory/tab_top_selected_3"), Identifier.method_60656("container/creative_inventory/tab_top_selected_4"), Identifier.method_60656("container/creative_inventory/tab_top_selected_5"), Identifier.method_60656("container/creative_inventory/tab_top_selected_6"), Identifier.method_60656("container/creative_inventory/tab_top_selected_7")};
    private static final Identifier[] TAB_BOTTOM_UNSELECTED_TEXTURES = new Identifier[]{Identifier.method_60656("container/creative_inventory/tab_bottom_unselected_1"), Identifier.method_60656("container/creative_inventory/tab_bottom_unselected_2"), Identifier.method_60656("container/creative_inventory/tab_bottom_unselected_3"), Identifier.method_60656("container/creative_inventory/tab_bottom_unselected_4"), Identifier.method_60656("container/creative_inventory/tab_bottom_unselected_5"), Identifier.method_60656("container/creative_inventory/tab_bottom_unselected_6"), Identifier.method_60656("container/creative_inventory/tab_bottom_unselected_7")};
    private static final Identifier[] TAB_BOTTOM_SELECTED_TEXTURES = new Identifier[]{Identifier.method_60656("container/creative_inventory/tab_bottom_selected_1"), Identifier.method_60656("container/creative_inventory/tab_bottom_selected_2"), Identifier.method_60656("container/creative_inventory/tab_bottom_selected_3"), Identifier.method_60656("container/creative_inventory/tab_bottom_selected_4"), Identifier.method_60656("container/creative_inventory/tab_bottom_selected_5"), Identifier.method_60656("container/creative_inventory/tab_bottom_selected_6"), Identifier.method_60656("container/creative_inventory/tab_bottom_selected_7")};
    private static final int ROWS_COUNT = 5;
    private static final int COLUMNS_COUNT = 9;
    private static final int TAB_WIDTH = 26;
    private static final int TAB_HEIGHT = 32;
    private static final int SCROLLBAR_WIDTH = 12;
    private static final int SCROLLBAR_HEIGHT = 15;
    static final SimpleInventory INVENTORY = new SimpleInventory(45);
    private static final Text DELETE_ITEM_SLOT_TEXT = Text.translatable("inventory.binSlot");
    private static final int WHITE = 0xFFFFFF;
    private static ItemGroup selectedTab = ItemGroups.getDefaultTab();
    private float scrollPosition;
    private boolean scrolling;
    private TextFieldWidget searchBox;
    @Nullable
    private List<Slot> slots;
    @Nullable
    private Slot deleteItemSlot;
    private CreativeInventoryListener listener;
    private boolean ignoreTypedCharacter;
    private boolean lastClickOutsideBounds;
    private final Set<TagKey<Item>> searchResultTags = new HashSet<TagKey<Item>>();
    private final boolean operatorTabEnabled;

    public CreativeInventoryScreen(ClientPlayerEntity player, FeatureSet enabledFeatures, boolean operatorTabEnabled) {
        super(new CreativeScreenHandler(player), player.getInventory(), ScreenTexts.EMPTY);
        player.currentScreenHandler = this.handler;
        this.backgroundHeight = 136;
        this.backgroundWidth = 195;
        this.operatorTabEnabled = operatorTabEnabled;
        this.populateDisplay(player.networkHandler.getSearchManager(), enabledFeatures, this.shouldShowOperatorTab(player), player.getWorld().getRegistryManager());
    }

    private boolean shouldShowOperatorTab(PlayerEntity player) {
        return player.isCreativeLevelTwoOp() && this.operatorTabEnabled;
    }

    private void updateDisplayParameters(FeatureSet enabledFeatures, boolean showOperatorTab, RegistryWrapper.WrapperLookup registryLookup) {
        ClientPlayNetworkHandler lv = this.client.getNetworkHandler();
        if (this.populateDisplay(lv != null ? lv.getSearchManager() : null, enabledFeatures, showOperatorTab, registryLookup)) {
            for (ItemGroup lv2 : ItemGroups.getGroups()) {
                Collection<ItemStack> collection = lv2.getDisplayStacks();
                if (lv2 != selectedTab) continue;
                if (lv2.getType() == ItemGroup.Type.CATEGORY && collection.isEmpty()) {
                    this.setSelectedTab(ItemGroups.getDefaultTab());
                    continue;
                }
                this.refreshSelectedTab(collection);
            }
        }
    }

    private boolean populateDisplay(@Nullable SearchManager searchManager, FeatureSet enabledFeatures, boolean showOperatorTab, RegistryWrapper.WrapperLookup registryLookup) {
        if (!ItemGroups.updateDisplayContext(enabledFeatures, showOperatorTab, registryLookup)) {
            return false;
        }
        if (searchManager != null) {
            List<ItemStack> list = List.copyOf(ItemGroups.getSearchGroup().getDisplayStacks());
            searchManager.addItemTooltipReloader(registryLookup, list);
            searchManager.addItemTagReloader(list);
        }
        return true;
    }

    private void refreshSelectedTab(Collection<ItemStack> displayStacks) {
        int i = ((CreativeScreenHandler)this.handler).getRow(this.scrollPosition);
        ((CreativeScreenHandler)this.handler).itemList.clear();
        if (selectedTab.getType() == ItemGroup.Type.SEARCH) {
            this.search();
        } else {
            ((CreativeScreenHandler)this.handler).itemList.addAll(displayStacks);
        }
        this.scrollPosition = ((CreativeScreenHandler)this.handler).getScrollPosition(i);
        ((CreativeScreenHandler)this.handler).scrollItems(this.scrollPosition);
    }

    @Override
    public void handledScreenTick() {
        super.handledScreenTick();
        if (this.client == null) {
            return;
        }
        if (this.client.player != null) {
            this.updateDisplayParameters(this.client.player.networkHandler.getEnabledFeatures(), this.shouldShowOperatorTab(this.client.player), this.client.player.getWorld().getRegistryManager());
        }
        if (!this.client.interactionManager.hasCreativeInventory()) {
            this.client.setScreen(new InventoryScreen(this.client.player));
        }
    }

    @Override
    protected void onMouseClick(@Nullable Slot slot, int slotId, int button, SlotActionType actionType) {
        if (this.isCreativeInventorySlot(slot)) {
            this.searchBox.setCursorToEnd(false);
            this.searchBox.setSelectionEnd(0);
        }
        boolean bl = actionType == SlotActionType.QUICK_MOVE;
        SlotActionType slotActionType = actionType = slotId == -999 && actionType == SlotActionType.PICKUP ? SlotActionType.THROW : actionType;
        if (slot != null || selectedTab.getType() == ItemGroup.Type.INVENTORY || actionType == SlotActionType.QUICK_CRAFT) {
            if (slot != null && !slot.canTakeItems(this.client.player)) {
                return;
            }
            if (slot == this.deleteItemSlot && bl) {
                for (int k = 0; k < this.client.player.playerScreenHandler.getStacks().size(); ++k) {
                    this.client.interactionManager.clickCreativeStack(ItemStack.EMPTY, k);
                }
            } else if (selectedTab.getType() == ItemGroup.Type.INVENTORY) {
                if (slot == this.deleteItemSlot) {
                    ((CreativeScreenHandler)this.handler).setCursorStack(ItemStack.EMPTY);
                } else if (actionType == SlotActionType.THROW && slot != null && slot.hasStack()) {
                    ItemStack lv = slot.takeStack(button == 0 ? 1 : slot.getStack().getMaxCount());
                    ItemStack lv2 = slot.getStack();
                    this.client.player.dropItem(lv, true);
                    this.client.interactionManager.dropCreativeStack(lv);
                    this.client.interactionManager.clickCreativeStack(lv2, ((CreativeSlot)slot).slot.id);
                } else if (actionType == SlotActionType.THROW && !((CreativeScreenHandler)this.handler).getCursorStack().isEmpty()) {
                    this.client.player.dropItem(((CreativeScreenHandler)this.handler).getCursorStack(), true);
                    this.client.interactionManager.dropCreativeStack(((CreativeScreenHandler)this.handler).getCursorStack());
                    ((CreativeScreenHandler)this.handler).setCursorStack(ItemStack.EMPTY);
                } else {
                    this.client.player.playerScreenHandler.onSlotClick(slot == null ? slotId : ((CreativeSlot)slot).slot.id, button, actionType, this.client.player);
                    this.client.player.playerScreenHandler.sendContentUpdates();
                }
            } else if (actionType != SlotActionType.QUICK_CRAFT && slot.inventory == INVENTORY) {
                ItemStack lv = ((CreativeScreenHandler)this.handler).getCursorStack();
                ItemStack lv2 = slot.getStack();
                if (actionType == SlotActionType.SWAP) {
                    if (!lv2.isEmpty()) {
                        this.client.player.getInventory().setStack(button, lv2.copyWithCount(lv2.getMaxCount()));
                        this.client.player.playerScreenHandler.sendContentUpdates();
                    }
                    return;
                }
                if (actionType == SlotActionType.CLONE) {
                    if (((CreativeScreenHandler)this.handler).getCursorStack().isEmpty() && slot.hasStack()) {
                        ItemStack lv3 = slot.getStack();
                        ((CreativeScreenHandler)this.handler).setCursorStack(lv3.copyWithCount(lv3.getMaxCount()));
                    }
                    return;
                }
                if (actionType == SlotActionType.THROW) {
                    if (!lv2.isEmpty()) {
                        ItemStack lv3 = lv2.copyWithCount(button == 0 ? 1 : lv2.getMaxCount());
                        this.client.player.dropItem(lv3, true);
                        this.client.interactionManager.dropCreativeStack(lv3);
                    }
                    return;
                }
                if (!lv.isEmpty() && !lv2.isEmpty() && ItemStack.areItemsAndComponentsEqual(lv, lv2)) {
                    if (button == 0) {
                        if (bl) {
                            lv.setCount(lv.getMaxCount());
                        } else if (lv.getCount() < lv.getMaxCount()) {
                            lv.increment(1);
                        }
                    } else {
                        lv.decrement(1);
                    }
                } else if (lv2.isEmpty() || !lv.isEmpty()) {
                    if (button == 0) {
                        ((CreativeScreenHandler)this.handler).setCursorStack(ItemStack.EMPTY);
                    } else if (!((CreativeScreenHandler)this.handler).getCursorStack().isEmpty()) {
                        ((CreativeScreenHandler)this.handler).getCursorStack().decrement(1);
                    }
                } else {
                    int l = bl ? lv2.getMaxCount() : lv2.getCount();
                    ((CreativeScreenHandler)this.handler).setCursorStack(lv2.copyWithCount(l));
                }
            } else if (this.handler != null) {
                ItemStack lv = slot == null ? ItemStack.EMPTY : ((CreativeScreenHandler)this.handler).getSlot(slot.id).getStack();
                ((CreativeScreenHandler)this.handler).onSlotClick(slot == null ? slotId : slot.id, button, actionType, this.client.player);
                if (ScreenHandler.unpackQuickCraftStage(button) == 2) {
                    for (int m = 0; m < 9; ++m) {
                        this.client.interactionManager.clickCreativeStack(((CreativeScreenHandler)this.handler).getSlot(45 + m).getStack(), 36 + m);
                    }
                } else if (slot != null) {
                    ItemStack lv2 = ((CreativeScreenHandler)this.handler).getSlot(slot.id).getStack();
                    this.client.interactionManager.clickCreativeStack(lv2, slot.id - ((CreativeScreenHandler)this.handler).slots.size() + 9 + 36);
                    int l = 45 + button;
                    if (actionType == SlotActionType.SWAP) {
                        this.client.interactionManager.clickCreativeStack(lv, l - ((CreativeScreenHandler)this.handler).slots.size() + 9 + 36);
                    } else if (actionType == SlotActionType.THROW && !lv.isEmpty()) {
                        ItemStack lv4 = lv.copyWithCount(button == 0 ? 1 : lv.getMaxCount());
                        this.client.player.dropItem(lv4, true);
                        this.client.interactionManager.dropCreativeStack(lv4);
                    }
                    this.client.player.playerScreenHandler.sendContentUpdates();
                }
            }
        } else if (!((CreativeScreenHandler)this.handler).getCursorStack().isEmpty() && this.lastClickOutsideBounds) {
            if (button == 0) {
                this.client.player.dropItem(((CreativeScreenHandler)this.handler).getCursorStack(), true);
                this.client.interactionManager.dropCreativeStack(((CreativeScreenHandler)this.handler).getCursorStack());
                ((CreativeScreenHandler)this.handler).setCursorStack(ItemStack.EMPTY);
            }
            if (button == 1) {
                ItemStack lv = ((CreativeScreenHandler)this.handler).getCursorStack().split(1);
                this.client.player.dropItem(lv, true);
                this.client.interactionManager.dropCreativeStack(lv);
            }
        }
    }

    private boolean isCreativeInventorySlot(@Nullable Slot slot) {
        return slot != null && slot.inventory == INVENTORY;
    }

    @Override
    protected void init() {
        if (this.client.interactionManager.hasCreativeInventory()) {
            super.init();
            this.searchBox = new TextFieldWidget(this.textRenderer, this.x + 82, this.y + 6, 80, this.textRenderer.fontHeight, Text.translatable("itemGroup.search"));
            this.searchBox.setMaxLength(50);
            this.searchBox.setDrawsBackground(false);
            this.searchBox.setVisible(false);
            this.searchBox.setEditableColor(0xFFFFFF);
            this.addSelectableChild(this.searchBox);
            ItemGroup lv = selectedTab;
            selectedTab = ItemGroups.getDefaultTab();
            this.setSelectedTab(lv);
            this.client.player.playerScreenHandler.removeListener(this.listener);
            this.listener = new CreativeInventoryListener(this.client);
            this.client.player.playerScreenHandler.addListener(this.listener);
            if (!selectedTab.shouldDisplay()) {
                this.setSelectedTab(ItemGroups.getDefaultTab());
            }
        } else {
            this.client.setScreen(new InventoryScreen(this.client.player));
        }
    }

    @Override
    public void resize(MinecraftClient client, int width, int height) {
        int k = ((CreativeScreenHandler)this.handler).getRow(this.scrollPosition);
        String string = this.searchBox.getText();
        this.init(client, width, height);
        this.searchBox.setText(string);
        if (!this.searchBox.getText().isEmpty()) {
            this.search();
        }
        this.scrollPosition = ((CreativeScreenHandler)this.handler).getScrollPosition(k);
        ((CreativeScreenHandler)this.handler).scrollItems(this.scrollPosition);
    }

    @Override
    public void removed() {
        super.removed();
        if (this.client.player != null && this.client.player.getInventory() != null) {
            this.client.player.playerScreenHandler.removeListener(this.listener);
        }
    }

    @Override
    public boolean charTyped(char chr, int modifiers) {
        if (this.ignoreTypedCharacter) {
            return false;
        }
        if (selectedTab.getType() != ItemGroup.Type.SEARCH) {
            return false;
        }
        String string = this.searchBox.getText();
        if (this.searchBox.charTyped(chr, modifiers)) {
            if (!Objects.equals(string, this.searchBox.getText())) {
                this.search();
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        this.ignoreTypedCharacter = false;
        if (selectedTab.getType() != ItemGroup.Type.SEARCH) {
            if (this.client.options.chatKey.matchesKey(keyCode, scanCode)) {
                this.ignoreTypedCharacter = true;
                this.setSelectedTab(ItemGroups.getSearchGroup());
                return true;
            }
            return super.keyPressed(keyCode, scanCode, modifiers);
        }
        boolean bl = !this.isCreativeInventorySlot(this.focusedSlot) || this.focusedSlot.hasStack();
        boolean bl2 = InputUtil.fromKeyCode(keyCode, scanCode).toInt().isPresent();
        if (bl && bl2 && this.handleHotbarKeyPressed(keyCode, scanCode)) {
            this.ignoreTypedCharacter = true;
            return true;
        }
        String string = this.searchBox.getText();
        if (this.searchBox.keyPressed(keyCode, scanCode, modifiers)) {
            if (!Objects.equals(string, this.searchBox.getText())) {
                this.search();
            }
            return true;
        }
        if (this.searchBox.isFocused() && this.searchBox.isVisible() && keyCode != GLFW.GLFW_KEY_ESCAPE) {
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        this.ignoreTypedCharacter = false;
        return super.keyReleased(keyCode, scanCode, modifiers);
    }

    private void search() {
        ((CreativeScreenHandler)this.handler).itemList.clear();
        this.searchResultTags.clear();
        String string = this.searchBox.getText();
        if (string.isEmpty()) {
            ((CreativeScreenHandler)this.handler).itemList.addAll(selectedTab.getDisplayStacks());
        } else {
            ClientPlayNetworkHandler lv = this.client.getNetworkHandler();
            if (lv != null) {
                SearchProvider<ItemStack> lv3;
                SearchManager lv2 = lv.getSearchManager();
                if (string.startsWith("#")) {
                    string = string.substring(1);
                    lv3 = lv2.getItemTagReloadFuture();
                    this.searchForTags(string);
                } else {
                    lv3 = lv2.getItemTooltipReloadFuture();
                }
                ((CreativeScreenHandler)this.handler).itemList.addAll(lv3.findAll(string.toLowerCase(Locale.ROOT)));
            }
        }
        this.scrollPosition = 0.0f;
        ((CreativeScreenHandler)this.handler).scrollItems(0.0f);
    }

    private void searchForTags(String id2) {
        Predicate<Identifier> predicate;
        int i = id2.indexOf(58);
        if (i == -1) {
            predicate = id -> id.getPath().contains(id2);
        } else {
            String string2 = id2.substring(0, i).trim();
            String string3 = id2.substring(i + 1).trim();
            predicate = id -> id.getNamespace().contains(string2) && id.getPath().contains(string3);
        }
        Registries.ITEM.streamTags().filter(tag -> predicate.test(tag.id())).forEach(this.searchResultTags::add);
    }

    @Override
    protected void drawForeground(DrawContext context, int mouseX, int mouseY) {
        if (selectedTab.shouldRenderName()) {
            context.drawText(this.textRenderer, selectedTab.getDisplayName(), 8, 6, 0x404040, false);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
            double f = mouseX - (double)this.x;
            double g = mouseY - (double)this.y;
            for (ItemGroup lv : ItemGroups.getGroupsToDisplay()) {
                if (!this.isClickInTab(lv, f, g)) continue;
                return true;
            }
            if (selectedTab.getType() != ItemGroup.Type.INVENTORY && this.isClickInScrollbar(mouseX, mouseY)) {
                this.scrolling = this.hasScrollbar();
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (button == 0) {
            double f = mouseX - (double)this.x;
            double g = mouseY - (double)this.y;
            this.scrolling = false;
            for (ItemGroup lv : ItemGroups.getGroupsToDisplay()) {
                if (!this.isClickInTab(lv, f, g)) continue;
                this.setSelectedTab(lv);
                return true;
            }
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    private boolean hasScrollbar() {
        return selectedTab.hasScrollbar() && ((CreativeScreenHandler)this.handler).shouldShowScrollbar();
    }

    private void setSelectedTab(ItemGroup group) {
        int j;
        int i;
        ItemGroup lv = selectedTab;
        selectedTab = group;
        this.cursorDragSlots.clear();
        ((CreativeScreenHandler)this.handler).itemList.clear();
        this.endTouchDrag();
        if (selectedTab.getType() == ItemGroup.Type.HOTBAR) {
            HotbarStorage lv2 = this.client.getCreativeHotbarStorage();
            for (i = 0; i < 9; ++i) {
                HotbarStorageEntry lv3 = lv2.getSavedHotbar(i);
                if (lv3.isEmpty()) {
                    for (j = 0; j < 9; ++j) {
                        if (j == i) {
                            ItemStack lv4 = new ItemStack(Items.PAPER);
                            lv4.set(DataComponentTypes.CREATIVE_SLOT_LOCK, Unit.INSTANCE);
                            Text lv5 = this.client.options.hotbarKeys[i].getBoundKeyLocalizedText();
                            Text lv6 = this.client.options.saveToolbarActivatorKey.getBoundKeyLocalizedText();
                            lv4.set(DataComponentTypes.ITEM_NAME, Text.translatable("inventory.hotbarInfo", lv6, lv5));
                            ((CreativeScreenHandler)this.handler).itemList.add(lv4);
                            continue;
                        }
                        ((CreativeScreenHandler)this.handler).itemList.add(ItemStack.EMPTY);
                    }
                    continue;
                }
                ((CreativeScreenHandler)this.handler).itemList.addAll(lv3.deserialize(this.client.world.getRegistryManager()));
            }
        } else if (selectedTab.getType() == ItemGroup.Type.CATEGORY) {
            ((CreativeScreenHandler)this.handler).itemList.addAll(selectedTab.getDisplayStacks());
        }
        if (selectedTab.getType() == ItemGroup.Type.INVENTORY) {
            PlayerScreenHandler lv7 = this.client.player.playerScreenHandler;
            if (this.slots == null) {
                this.slots = ImmutableList.copyOf(((CreativeScreenHandler)this.handler).slots);
            }
            ((CreativeScreenHandler)this.handler).slots.clear();
            for (i = 0; i < lv7.slots.size(); ++i) {
                int n;
                if (i >= 5 && i < 9) {
                    int k = i - 5;
                    l = k / 2;
                    m = k % 2;
                    n = 54 + l * 54;
                    j = 6 + m * 27;
                } else if (i >= 0 && i < 5) {
                    n = -2000;
                    j = -2000;
                } else if (i == 45) {
                    n = 35;
                    j = 20;
                } else {
                    int k = i - 9;
                    l = k % 9;
                    m = k / 9;
                    n = 9 + l * 18;
                    j = i >= 36 ? 112 : 54 + m * 18;
                }
                CreativeSlot lv8 = new CreativeSlot(lv7.slots.get(i), i, n, j);
                ((CreativeScreenHandler)this.handler).slots.add(lv8);
            }
            this.deleteItemSlot = new Slot(INVENTORY, 0, 173, 112);
            ((CreativeScreenHandler)this.handler).slots.add(this.deleteItemSlot);
        } else if (lv.getType() == ItemGroup.Type.INVENTORY) {
            ((CreativeScreenHandler)this.handler).slots.clear();
            ((CreativeScreenHandler)this.handler).slots.addAll(this.slots);
            this.slots = null;
        }
        if (selectedTab.getType() == ItemGroup.Type.SEARCH) {
            this.searchBox.setVisible(true);
            this.searchBox.setFocusUnlocked(false);
            this.searchBox.setFocused(true);
            if (lv != group) {
                this.searchBox.setText("");
            }
            this.search();
        } else {
            this.searchBox.setVisible(false);
            this.searchBox.setFocusUnlocked(true);
            this.searchBox.setFocused(false);
            this.searchBox.setText("");
        }
        this.scrollPosition = 0.0f;
        ((CreativeScreenHandler)this.handler).scrollItems(0.0f);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (!this.hasScrollbar()) {
            return false;
        }
        this.scrollPosition = ((CreativeScreenHandler)this.handler).getScrollPosition(this.scrollPosition, verticalAmount);
        ((CreativeScreenHandler)this.handler).scrollItems(this.scrollPosition);
        return true;
    }

    @Override
    protected boolean isClickOutsideBounds(double mouseX, double mouseY, int left, int top, int button) {
        boolean bl = mouseX < (double)left || mouseY < (double)top || mouseX >= (double)(left + this.backgroundWidth) || mouseY >= (double)(top + this.backgroundHeight);
        this.lastClickOutsideBounds = bl && !this.isClickInTab(selectedTab, mouseX, mouseY);
        return this.lastClickOutsideBounds;
    }

    protected boolean isClickInScrollbar(double mouseX, double mouseY) {
        int i = this.x;
        int j = this.y;
        int k = i + 175;
        int l = j + 18;
        int m = k + 14;
        int n = l + 112;
        return mouseX >= (double)k && mouseY >= (double)l && mouseX < (double)m && mouseY < (double)n;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (this.scrolling) {
            int j = this.y + 18;
            int k = j + 112;
            this.scrollPosition = ((float)mouseY - (float)j - 7.5f) / ((float)(k - j) - 15.0f);
            this.scrollPosition = MathHelper.clamp(this.scrollPosition, 0.0f, 1.0f);
            ((CreativeScreenHandler)this.handler).scrollItems(this.scrollPosition);
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        for (ItemGroup lv : ItemGroups.getGroupsToDisplay()) {
            if (this.renderTabTooltipIfHovered(context, lv, mouseX, mouseY)) break;
        }
        if (this.deleteItemSlot != null && selectedTab.getType() == ItemGroup.Type.INVENTORY && this.isPointWithinBounds(this.deleteItemSlot.x, this.deleteItemSlot.y, 16, 16, mouseX, mouseY)) {
            context.drawTooltip(this.textRenderer, DELETE_ITEM_SLOT_TEXT, mouseX, mouseY);
        }
        this.drawMouseoverTooltip(context, mouseX, mouseY);
    }

    @Override
    public List<Text> getTooltipFromItem(ItemStack stack) {
        boolean bl = this.focusedSlot != null && this.focusedSlot instanceof LockableSlot;
        boolean bl2 = selectedTab.getType() == ItemGroup.Type.CATEGORY;
        boolean bl3 = selectedTab.getType() == ItemGroup.Type.SEARCH;
        TooltipType.Default lv = this.client.options.advancedItemTooltips ? TooltipType.Default.ADVANCED : TooltipType.Default.BASIC;
        TooltipType.Default lv2 = bl ? lv.withCreative() : lv;
        List<Text> list = stack.getTooltip(Item.TooltipContext.create(this.client.world), this.client.player, lv2);
        if (!bl2 || !bl) {
            ArrayList<Text> list2 = Lists.newArrayList(list);
            if (bl3 && bl) {
                this.searchResultTags.forEach(tagKey -> {
                    if (stack.isIn((TagKey<Item>)tagKey)) {
                        list2.add(1, Text.literal("#" + String.valueOf(tagKey.id())).formatted(Formatting.DARK_PURPLE));
                    }
                });
            }
            int i = 1;
            for (ItemGroup lv3 : ItemGroups.getGroupsToDisplay()) {
                if (lv3.getType() == ItemGroup.Type.SEARCH || !lv3.contains(stack)) continue;
                list2.add(i++, lv3.getDisplayName().copy().formatted(Formatting.BLUE));
            }
            return list2;
        }
        return list;
    }

    @Override
    protected void drawBackground(DrawContext context, float delta, int mouseX, int mouseY) {
        for (ItemGroup lv : ItemGroups.getGroupsToDisplay()) {
            if (lv == selectedTab) continue;
            this.renderTabIcon(context, lv);
        }
        context.drawTexture(selectedTab.getTexture(), this.x, this.y, 0, 0, this.backgroundWidth, this.backgroundHeight);
        this.searchBox.render(context, mouseX, mouseY, delta);
        int k = this.x + 175;
        int l = this.y + 18;
        int m = l + 112;
        if (selectedTab.hasScrollbar()) {
            Identifier lv2 = this.hasScrollbar() ? SCROLLER_TEXTURE : SCROLLER_DISABLED_TEXTURE;
            context.drawGuiTexture(lv2, k, l + (int)((float)(m - l - 17) * this.scrollPosition), 12, 15);
        }
        this.renderTabIcon(context, selectedTab);
        if (selectedTab.getType() == ItemGroup.Type.INVENTORY) {
            InventoryScreen.drawEntity(context, this.x + 73, this.y + 6, this.x + 105, this.y + 49, 20, 0.0625f, mouseX, mouseY, this.client.player);
        }
    }

    private int getTabX(ItemGroup group) {
        int i = group.getColumn();
        int j = 27;
        int k = 27 * i;
        if (group.isSpecial()) {
            k = this.backgroundWidth - 27 * (7 - i) + 1;
        }
        return k;
    }

    private int getTabY(ItemGroup group) {
        int i = 0;
        i = group.getRow() == ItemGroup.Row.TOP ? (i -= 32) : (i += this.backgroundHeight);
        return i;
    }

    protected boolean isClickInTab(ItemGroup group, double mouseX, double mouseY) {
        int i = this.getTabX(group);
        int j = this.getTabY(group);
        return mouseX >= (double)i && mouseX <= (double)(i + 26) && mouseY >= (double)j && mouseY <= (double)(j + 32);
    }

    protected boolean renderTabTooltipIfHovered(DrawContext context, ItemGroup group, int mouseX, int mouseY) {
        int l;
        int k = this.getTabX(group);
        if (this.isPointWithinBounds(k + 3, (l = this.getTabY(group)) + 3, 21, 27, mouseX, mouseY)) {
            context.drawTooltip(this.textRenderer, group.getDisplayName(), mouseX, mouseY);
            return true;
        }
        return false;
    }

    protected void renderTabIcon(DrawContext context, ItemGroup group) {
        boolean bl = group == selectedTab;
        boolean bl2 = group.getRow() == ItemGroup.Row.TOP;
        int i = group.getColumn();
        int j = this.x + this.getTabX(group);
        int k = this.y - (bl2 ? 28 : -(this.backgroundHeight - 4));
        Identifier[] lvs = bl2 ? (bl ? TAB_TOP_SELECTED_TEXTURES : TAB_TOP_UNSELECTED_TEXTURES) : (bl ? TAB_BOTTOM_SELECTED_TEXTURES : TAB_BOTTOM_UNSELECTED_TEXTURES);
        context.drawGuiTexture(lvs[MathHelper.clamp(i, 0, lvs.length)], j, k, 26, 32);
        context.getMatrices().push();
        context.getMatrices().translate(0.0f, 0.0f, 100.0f);
        int n = bl2 ? 1 : -1;
        ItemStack lv = group.getIcon();
        context.drawItem(lv, j += 5, k += 8 + n);
        context.drawItemInSlot(this.textRenderer, lv, j, k);
        context.getMatrices().pop();
    }

    public boolean isInventoryTabSelected() {
        return selectedTab.getType() == ItemGroup.Type.INVENTORY;
    }

    public static void onHotbarKeyPress(MinecraftClient client, int index, boolean restore, boolean save) {
        ClientPlayerEntity lv = client.player;
        DynamicRegistryManager lv2 = lv.getWorld().getRegistryManager();
        HotbarStorage lv3 = client.getCreativeHotbarStorage();
        HotbarStorageEntry lv4 = lv3.getSavedHotbar(index);
        if (restore) {
            List<ItemStack> list = lv4.deserialize(lv2);
            for (int j = 0; j < PlayerInventory.getHotbarSize(); ++j) {
                ItemStack lv5 = list.get(j);
                lv.getInventory().setStack(j, lv5);
                client.interactionManager.clickCreativeStack(lv5, 36 + j);
            }
            lv.playerScreenHandler.sendContentUpdates();
        } else if (save) {
            lv4.serialize(lv.getInventory(), lv2);
            Text lv6 = client.options.hotbarKeys[index].getBoundKeyLocalizedText();
            Text lv7 = client.options.loadToolbarActivatorKey.getBoundKeyLocalizedText();
            MutableText lv8 = Text.translatable("inventory.hotbarSaved", lv7, lv6);
            client.inGameHud.setOverlayMessage(lv8, false);
            client.getNarratorManager().narrate(lv8);
            lv3.save();
        }
    }

    @Environment(value=EnvType.CLIENT)
    public static class CreativeScreenHandler
    extends ScreenHandler {
        public final DefaultedList<ItemStack> itemList = DefaultedList.of();
        private final ScreenHandler parent;

        public CreativeScreenHandler(PlayerEntity player) {
            super(null, 0);
            int i;
            this.parent = player.playerScreenHandler;
            PlayerInventory lv = player.getInventory();
            for (i = 0; i < 5; ++i) {
                for (int j = 0; j < 9; ++j) {
                    this.addSlot(new LockableSlot(INVENTORY, i * 9 + j, 9 + j * 18, 18 + i * 18));
                }
            }
            for (i = 0; i < 9; ++i) {
                this.addSlot(new Slot(lv, i, 9 + i * 18, 112));
            }
            this.scrollItems(0.0f);
        }

        @Override
        public boolean canUse(PlayerEntity player) {
            return true;
        }

        protected int getOverflowRows() {
            return MathHelper.ceilDiv(this.itemList.size(), 9) - 5;
        }

        protected int getRow(float scroll) {
            return Math.max((int)((double)(scroll * (float)this.getOverflowRows()) + 0.5), 0);
        }

        protected float getScrollPosition(int row) {
            return MathHelper.clamp((float)row / (float)this.getOverflowRows(), 0.0f, 1.0f);
        }

        protected float getScrollPosition(float current, double amount) {
            return MathHelper.clamp(current - (float)(amount / (double)this.getOverflowRows()), 0.0f, 1.0f);
        }

        public void scrollItems(float position) {
            int i = this.getRow(position);
            for (int j = 0; j < 5; ++j) {
                for (int k = 0; k < 9; ++k) {
                    int l = k + (j + i) * 9;
                    if (l >= 0 && l < this.itemList.size()) {
                        INVENTORY.setStack(k + j * 9, this.itemList.get(l));
                        continue;
                    }
                    INVENTORY.setStack(k + j * 9, ItemStack.EMPTY);
                }
            }
        }

        public boolean shouldShowScrollbar() {
            return this.itemList.size() > 45;
        }

        @Override
        public ItemStack quickMove(PlayerEntity player, int slot) {
            Slot lv;
            if (slot >= this.slots.size() - 9 && slot < this.slots.size() && (lv = (Slot)this.slots.get(slot)) != null && lv.hasStack()) {
                lv.setStack(ItemStack.EMPTY);
            }
            return ItemStack.EMPTY;
        }

        @Override
        public boolean canInsertIntoSlot(ItemStack stack, Slot slot) {
            return slot.inventory != INVENTORY;
        }

        @Override
        public boolean canInsertIntoSlot(Slot slot) {
            return slot.inventory != INVENTORY;
        }

        @Override
        public ItemStack getCursorStack() {
            return this.parent.getCursorStack();
        }

        @Override
        public void setCursorStack(ItemStack stack) {
            this.parent.setCursorStack(stack);
        }
    }

    @Environment(value=EnvType.CLIENT)
    static class CreativeSlot
    extends Slot {
        final Slot slot;

        public CreativeSlot(Slot slot, int invSlot, int x, int y) {
            super(slot.inventory, invSlot, x, y);
            this.slot = slot;
        }

        @Override
        public void onTakeItem(PlayerEntity player, ItemStack stack) {
            this.slot.onTakeItem(player, stack);
        }

        @Override
        public boolean canInsert(ItemStack stack) {
            return this.slot.canInsert(stack);
        }

        @Override
        public ItemStack getStack() {
            return this.slot.getStack();
        }

        @Override
        public boolean hasStack() {
            return this.slot.hasStack();
        }

        @Override
        public void setStack(ItemStack stack, ItemStack previousStack) {
            this.slot.setStack(stack, previousStack);
        }

        @Override
        public void setStackNoCallbacks(ItemStack stack) {
            this.slot.setStackNoCallbacks(stack);
        }

        @Override
        public void markDirty() {
            this.slot.markDirty();
        }

        @Override
        public int getMaxItemCount() {
            return this.slot.getMaxItemCount();
        }

        @Override
        public int getMaxItemCount(ItemStack stack) {
            return this.slot.getMaxItemCount(stack);
        }

        @Override
        @Nullable
        public Pair<Identifier, Identifier> getBackgroundSprite() {
            return this.slot.getBackgroundSprite();
        }

        @Override
        public ItemStack takeStack(int amount) {
            return this.slot.takeStack(amount);
        }

        @Override
        public boolean isEnabled() {
            return this.slot.isEnabled();
        }

        @Override
        public boolean canTakeItems(PlayerEntity playerEntity) {
            return this.slot.canTakeItems(playerEntity);
        }
    }

    @Environment(value=EnvType.CLIENT)
    static class LockableSlot
    extends Slot {
        public LockableSlot(Inventory arg, int i, int j, int k) {
            super(arg, i, j, k);
        }

        @Override
        public boolean canTakeItems(PlayerEntity playerEntity) {
            ItemStack lv = this.getStack();
            if (super.canTakeItems(playerEntity) && !lv.isEmpty()) {
                return lv.isItemEnabled(playerEntity.getWorld().getEnabledFeatures()) && !lv.contains(DataComponentTypes.CREATIVE_SLOT_LOCK);
            }
            return lv.isEmpty();
        }
    }
}

