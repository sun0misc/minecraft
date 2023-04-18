package net.minecraft.client.gui.screen.ingame;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.datafixers.util.Pair;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Stream;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.option.HotbarStorage;
import net.minecraft.client.option.HotbarStorageEntry;
import net.minecraft.client.search.SearchManager;
import net.minecraft.client.search.SearchProvider;
import net.minecraft.client.util.InputUtil;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemGroups;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.resource.featuretoggle.FeatureSet;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

@Environment(EnvType.CLIENT)
public class CreativeInventoryScreen extends AbstractInventoryScreen {
   private static final Identifier TEXTURE = new Identifier("textures/gui/container/creative_inventory/tabs.png");
   private static final String TAB_TEXTURE_PREFIX = "textures/gui/container/creative_inventory/tab_";
   private static final String CUSTOM_CREATIVE_LOCK_KEY = "CustomCreativeLock";
   private static final int ROWS_COUNT = 5;
   private static final int COLUMNS_COUNT = 9;
   private static final int TAB_WIDTH = 26;
   private static final int TAB_HEIGHT = 32;
   private static final int SCROLLBAR_WIDTH = 12;
   private static final int SCROLLBAR_HEIGHT = 15;
   static final SimpleInventory INVENTORY = new SimpleInventory(45);
   private static final Text DELETE_ITEM_SLOT_TEXT = Text.translatable("inventory.binSlot");
   private static final int WHITE = 16777215;
   private static ItemGroup selectedTab = ItemGroups.getDefaultTab();
   private float scrollPosition;
   private boolean scrolling;
   private TextFieldWidget searchBox;
   @Nullable
   private List slots;
   @Nullable
   private Slot deleteItemSlot;
   private CreativeInventoryListener listener;
   private boolean ignoreTypedCharacter;
   private boolean lastClickOutsideBounds;
   private final Set searchResultTags = new HashSet();
   private final boolean operatorTabEnabled;

   public CreativeInventoryScreen(PlayerEntity player, FeatureSet enabledFeatures, boolean operatorTabEnabled) {
      super(new CreativeScreenHandler(player), player.getInventory(), ScreenTexts.EMPTY);
      player.currentScreenHandler = this.handler;
      this.passEvents = true;
      this.backgroundHeight = 136;
      this.backgroundWidth = 195;
      this.operatorTabEnabled = operatorTabEnabled;
      ItemGroups.updateDisplayContext(enabledFeatures, this.shouldShowOperatorTab(player), player.world.getRegistryManager());
   }

   private boolean shouldShowOperatorTab(PlayerEntity player) {
      return player.isCreativeLevelTwoOp() && this.operatorTabEnabled;
   }

   private void updateDisplayParameters(FeatureSet enabledFeatures, boolean showOperatorTab, RegistryWrapper.WrapperLookup arg2) {
      if (ItemGroups.updateDisplayContext(enabledFeatures, showOperatorTab, arg2)) {
         Iterator var4 = ItemGroups.getGroups().iterator();

         while(true) {
            while(true) {
               ItemGroup lv;
               Collection collection;
               do {
                  if (!var4.hasNext()) {
                     return;
                  }

                  lv = (ItemGroup)var4.next();
                  collection = lv.getDisplayStacks();
               } while(lv != selectedTab);

               if (lv.getType() == ItemGroup.Type.CATEGORY && collection.isEmpty()) {
                  this.setSelectedTab(ItemGroups.getDefaultTab());
               } else {
                  this.refreshSelectedTab(collection);
               }
            }
         }
      }
   }

   private void refreshSelectedTab(Collection displayStacks) {
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

   public void handledScreenTick() {
      super.handledScreenTick();
      if (this.client != null) {
         if (this.client.player != null) {
            this.updateDisplayParameters(this.client.player.networkHandler.getEnabledFeatures(), this.shouldShowOperatorTab(this.client.player), this.client.player.world.getRegistryManager());
         }

         if (!this.client.interactionManager.hasCreativeInventory()) {
            this.client.setScreen(new InventoryScreen(this.client.player));
         } else {
            this.searchBox.tick();
         }

      }
   }

   protected void onMouseClick(@Nullable Slot slot, int slotId, int button, SlotActionType actionType) {
      if (this.isCreativeInventorySlot(slot)) {
         this.searchBox.setCursorToEnd();
         this.searchBox.setSelectionEnd(0);
      }

      boolean bl = actionType == SlotActionType.QUICK_MOVE;
      actionType = slotId == -999 && actionType == SlotActionType.PICKUP ? SlotActionType.THROW : actionType;
      ItemStack lv;
      if (slot == null && selectedTab.getType() != ItemGroup.Type.INVENTORY && actionType != SlotActionType.QUICK_CRAFT) {
         if (!((CreativeScreenHandler)this.handler).getCursorStack().isEmpty() && this.lastClickOutsideBounds) {
            if (button == 0) {
               this.client.player.dropItem(((CreativeScreenHandler)this.handler).getCursorStack(), true);
               this.client.interactionManager.dropCreativeStack(((CreativeScreenHandler)this.handler).getCursorStack());
               ((CreativeScreenHandler)this.handler).setCursorStack(ItemStack.EMPTY);
            }

            if (button == 1) {
               lv = ((CreativeScreenHandler)this.handler).getCursorStack().split(1);
               this.client.player.dropItem(lv, true);
               this.client.interactionManager.dropCreativeStack(lv);
            }
         }
      } else {
         if (slot != null && !slot.canTakeItems(this.client.player)) {
            return;
         }

         if (slot == this.deleteItemSlot && bl) {
            for(int k = 0; k < this.client.player.playerScreenHandler.getStacks().size(); ++k) {
               this.client.interactionManager.clickCreativeStack(ItemStack.EMPTY, k);
            }
         } else {
            ItemStack lv2;
            if (selectedTab.getType() == ItemGroup.Type.INVENTORY) {
               if (slot == this.deleteItemSlot) {
                  ((CreativeScreenHandler)this.handler).setCursorStack(ItemStack.EMPTY);
               } else if (actionType == SlotActionType.THROW && slot != null && slot.hasStack()) {
                  lv = slot.takeStack(button == 0 ? 1 : slot.getStack().getMaxCount());
                  lv2 = slot.getStack();
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
            } else {
               int l;
               if (actionType != SlotActionType.QUICK_CRAFT && slot.inventory == INVENTORY) {
                  lv = ((CreativeScreenHandler)this.handler).getCursorStack();
                  lv2 = slot.getStack();
                  if (actionType == SlotActionType.SWAP) {
                     if (!lv2.isEmpty()) {
                        this.client.player.getInventory().setStack(button, lv2.copyWithCount(lv2.getMaxCount()));
                        this.client.player.playerScreenHandler.sendContentUpdates();
                     }

                     return;
                  }

                  ItemStack lv3;
                  if (actionType == SlotActionType.CLONE) {
                     if (((CreativeScreenHandler)this.handler).getCursorStack().isEmpty() && slot.hasStack()) {
                        lv3 = slot.getStack();
                        ((CreativeScreenHandler)this.handler).setCursorStack(lv3.copyWithCount(lv3.getMaxCount()));
                     }

                     return;
                  }

                  if (actionType == SlotActionType.THROW) {
                     if (!lv2.isEmpty()) {
                        lv3 = lv2.copyWithCount(button == 0 ? 1 : lv2.getMaxCount());
                        this.client.player.dropItem(lv3, true);
                        this.client.interactionManager.dropCreativeStack(lv3);
                     }

                     return;
                  }

                  if (!lv.isEmpty() && !lv2.isEmpty() && lv.isItemEqual(lv2) && ItemStack.areNbtEqual(lv, lv2)) {
                     if (button == 0) {
                        if (bl) {
                           lv.setCount(lv.getMaxCount());
                        } else if (lv.getCount() < lv.getMaxCount()) {
                           lv.increment(1);
                        }
                     } else {
                        lv.decrement(1);
                     }
                  } else if (!lv2.isEmpty() && lv.isEmpty()) {
                     l = bl ? lv2.getMaxCount() : lv2.getCount();
                     ((CreativeScreenHandler)this.handler).setCursorStack(lv2.copyWithCount(l));
                  } else if (button == 0) {
                     ((CreativeScreenHandler)this.handler).setCursorStack(ItemStack.EMPTY);
                  } else if (!((CreativeScreenHandler)this.handler).getCursorStack().isEmpty()) {
                     ((CreativeScreenHandler)this.handler).getCursorStack().decrement(1);
                  }
               } else if (this.handler != null) {
                  lv = slot == null ? ItemStack.EMPTY : ((CreativeScreenHandler)this.handler).getSlot(slot.id).getStack();
                  ((CreativeScreenHandler)this.handler).onSlotClick(slot == null ? slotId : slot.id, button, actionType, this.client.player);
                  if (ScreenHandler.unpackQuickCraftStage(button) == 2) {
                     for(int m = 0; m < 9; ++m) {
                        this.client.interactionManager.clickCreativeStack(((CreativeScreenHandler)this.handler).getSlot(45 + m).getStack(), 36 + m);
                     }
                  } else if (slot != null) {
                     lv2 = ((CreativeScreenHandler)this.handler).getSlot(slot.id).getStack();
                     this.client.interactionManager.clickCreativeStack(lv2, slot.id - ((CreativeScreenHandler)this.handler).slots.size() + 9 + 36);
                     l = 45 + button;
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
            }
         }
      }

   }

   private boolean isCreativeInventorySlot(@Nullable Slot slot) {
      return slot != null && slot.inventory == INVENTORY;
   }

   protected void init() {
      if (this.client.interactionManager.hasCreativeInventory()) {
         super.init();
         TextRenderer var10003 = this.textRenderer;
         int var10004 = this.x + 82;
         int var10005 = this.y + 6;
         Objects.requireNonNull(this.textRenderer);
         this.searchBox = new TextFieldWidget(var10003, var10004, var10005, 80, 9, Text.translatable("itemGroup.search"));
         this.searchBox.setMaxLength(50);
         this.searchBox.setDrawsBackground(false);
         this.searchBox.setVisible(false);
         this.searchBox.setEditableColor(16777215);
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

   public void removed() {
      super.removed();
      if (this.client.player != null && this.client.player.getInventory() != null) {
         this.client.player.playerScreenHandler.removeListener(this.listener);
      }

   }

   public boolean charTyped(char chr, int modifiers) {
      if (this.ignoreTypedCharacter) {
         return false;
      } else if (selectedTab.getType() != ItemGroup.Type.SEARCH) {
         return false;
      } else {
         String string = this.searchBox.getText();
         if (this.searchBox.charTyped(chr, modifiers)) {
            if (!Objects.equals(string, this.searchBox.getText())) {
               this.search();
            }

            return true;
         } else {
            return false;
         }
      }
   }

   public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
      this.ignoreTypedCharacter = false;
      if (selectedTab.getType() != ItemGroup.Type.SEARCH) {
         if (this.client.options.chatKey.matchesKey(keyCode, scanCode)) {
            this.ignoreTypedCharacter = true;
            this.setSelectedTab(ItemGroups.getSearchGroup());
            return true;
         } else {
            return super.keyPressed(keyCode, scanCode, modifiers);
         }
      } else {
         boolean bl = !this.isCreativeInventorySlot(this.focusedSlot) || this.focusedSlot.hasStack();
         boolean bl2 = InputUtil.fromKeyCode(keyCode, scanCode).toInt().isPresent();
         if (bl && bl2 && this.handleHotbarKeyPressed(keyCode, scanCode)) {
            this.ignoreTypedCharacter = true;
            return true;
         } else {
            String string = this.searchBox.getText();
            if (this.searchBox.keyPressed(keyCode, scanCode, modifiers)) {
               if (!Objects.equals(string, this.searchBox.getText())) {
                  this.search();
               }

               return true;
            } else {
               return this.searchBox.isFocused() && this.searchBox.isVisible() && keyCode != GLFW.GLFW_KEY_ESCAPE ? true : super.keyPressed(keyCode, scanCode, modifiers);
            }
         }
      }
   }

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
         SearchProvider lv;
         if (string.startsWith("#")) {
            string = string.substring(1);
            lv = this.client.getSearchProvider(SearchManager.ITEM_TAG);
            this.searchForTags(string);
         } else {
            lv = this.client.getSearchProvider(SearchManager.ITEM_TOOLTIP);
         }

         ((CreativeScreenHandler)this.handler).itemList.addAll(lv.findAll(string.toLowerCase(Locale.ROOT)));
      }

      this.scrollPosition = 0.0F;
      ((CreativeScreenHandler)this.handler).scrollItems(0.0F);
   }

   private void searchForTags(String id) {
      int i = id.indexOf(58);
      Predicate predicate;
      if (i == -1) {
         predicate = (idx) -> {
            return idx.getPath().contains(id);
         };
      } else {
         String string2 = id.substring(0, i).trim();
         String string3 = id.substring(i + 1).trim();
         predicate = (idx) -> {
            return idx.getNamespace().contains(string2) && idx.getPath().contains(string3);
         };
      }

      Stream var10000 = Registries.ITEM.streamTags().filter((tag) -> {
         return predicate.test(tag.id());
      });
      Set var10001 = this.searchResultTags;
      Objects.requireNonNull(var10001);
      var10000.forEach(var10001::add);
   }

   protected void drawForeground(MatrixStack matrices, int mouseX, int mouseY) {
      if (selectedTab.shouldRenderName()) {
         this.textRenderer.draw(matrices, selectedTab.getDisplayName(), 8.0F, 6.0F, 4210752);
      }

   }

   public boolean mouseClicked(double mouseX, double mouseY, int button) {
      if (button == 0) {
         double f = mouseX - (double)this.x;
         double g = mouseY - (double)this.y;
         Iterator var10 = ItemGroups.getGroupsToDisplay().iterator();

         while(var10.hasNext()) {
            ItemGroup lv = (ItemGroup)var10.next();
            if (this.isClickInTab(lv, f, g)) {
               return true;
            }
         }

         if (selectedTab.getType() != ItemGroup.Type.INVENTORY && this.isClickInScrollbar(mouseX, mouseY)) {
            this.scrolling = this.hasScrollbar();
            return true;
         }
      }

      return super.mouseClicked(mouseX, mouseY, button);
   }

   public boolean mouseReleased(double mouseX, double mouseY, int button) {
      if (button == 0) {
         double f = mouseX - (double)this.x;
         double g = mouseY - (double)this.y;
         this.scrolling = false;
         Iterator var10 = ItemGroups.getGroupsToDisplay().iterator();

         while(var10.hasNext()) {
            ItemGroup lv = (ItemGroup)var10.next();
            if (this.isClickInTab(lv, f, g)) {
               this.setSelectedTab(lv);
               return true;
            }
         }
      }

      return super.mouseReleased(mouseX, mouseY, button);
   }

   private boolean hasScrollbar() {
      return selectedTab.hasScrollbar() && ((CreativeScreenHandler)this.handler).shouldShowScrollbar();
   }

   private void setSelectedTab(ItemGroup group) {
      ItemGroup lv = selectedTab;
      selectedTab = group;
      this.cursorDragSlots.clear();
      ((CreativeScreenHandler)this.handler).itemList.clear();
      this.endTouchDrag();
      int i;
      int j;
      if (selectedTab.getType() == ItemGroup.Type.HOTBAR) {
         HotbarStorage lv2 = this.client.getCreativeHotbarStorage();

         for(i = 0; i < 9; ++i) {
            HotbarStorageEntry lv3 = lv2.getSavedHotbar(i);
            if (lv3.isEmpty()) {
               for(j = 0; j < 9; ++j) {
                  if (j == i) {
                     ItemStack lv4 = new ItemStack(Items.PAPER);
                     lv4.getOrCreateSubNbt("CustomCreativeLock");
                     Text lv5 = this.client.options.hotbarKeys[i].getBoundKeyLocalizedText();
                     Text lv6 = this.client.options.saveToolbarActivatorKey.getBoundKeyLocalizedText();
                     lv4.setCustomName(Text.translatable("inventory.hotbarInfo", lv6, lv5));
                     ((CreativeScreenHandler)this.handler).itemList.add(lv4);
                  } else {
                     ((CreativeScreenHandler)this.handler).itemList.add(ItemStack.EMPTY);
                  }
               }
            } else {
               ((CreativeScreenHandler)this.handler).itemList.addAll(lv3);
            }
         }
      } else if (selectedTab.getType() == ItemGroup.Type.CATEGORY) {
         ((CreativeScreenHandler)this.handler).itemList.addAll(selectedTab.getDisplayStacks());
      }

      if (selectedTab.getType() == ItemGroup.Type.INVENTORY) {
         ScreenHandler lv7 = this.client.player.playerScreenHandler;
         if (this.slots == null) {
            this.slots = ImmutableList.copyOf(((CreativeScreenHandler)this.handler).slots);
         }

         ((CreativeScreenHandler)this.handler).slots.clear();

         for(i = 0; i < lv7.slots.size(); ++i) {
            int n;
            int k;
            int l;
            int m;
            if (i >= 5 && i < 9) {
               k = i - 5;
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
               k = i - 9;
               l = k % 9;
               m = k / 9;
               n = 9 + l * 18;
               if (i >= 36) {
                  j = 112;
               } else {
                  j = 54 + m * 18;
               }
            }

            Slot lv8 = new CreativeSlot((Slot)lv7.slots.get(i), i, n, j);
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

      this.scrollPosition = 0.0F;
      ((CreativeScreenHandler)this.handler).scrollItems(0.0F);
   }

   public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
      if (!this.hasScrollbar()) {
         return false;
      } else {
         this.scrollPosition = ((CreativeScreenHandler)this.handler).getScrollPosition(this.scrollPosition, amount);
         ((CreativeScreenHandler)this.handler).scrollItems(this.scrollPosition);
         return true;
      }
   }

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

   public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
      if (this.scrolling) {
         int j = this.y + 18;
         int k = j + 112;
         this.scrollPosition = ((float)mouseY - (float)j - 7.5F) / ((float)(k - j) - 15.0F);
         this.scrollPosition = MathHelper.clamp(this.scrollPosition, 0.0F, 1.0F);
         ((CreativeScreenHandler)this.handler).scrollItems(this.scrollPosition);
         return true;
      } else {
         return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
      }
   }

   public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
      this.renderBackground(matrices);
      super.render(matrices, mouseX, mouseY, delta);
      Iterator var5 = ItemGroups.getGroupsToDisplay().iterator();

      while(var5.hasNext()) {
         ItemGroup lv = (ItemGroup)var5.next();
         if (this.renderTabTooltipIfHovered(matrices, lv, mouseX, mouseY)) {
            break;
         }
      }

      if (this.deleteItemSlot != null && selectedTab.getType() == ItemGroup.Type.INVENTORY && this.isPointWithinBounds(this.deleteItemSlot.x, this.deleteItemSlot.y, 16, 16, (double)mouseX, (double)mouseY)) {
         this.renderTooltip(matrices, DELETE_ITEM_SLOT_TEXT, mouseX, mouseY);
      }

      this.drawMouseoverTooltip(matrices, mouseX, mouseY);
   }

   protected void renderTooltip(MatrixStack matrices, ItemStack stack, int x, int y) {
      boolean bl = this.focusedSlot != null && this.focusedSlot instanceof LockableSlot;
      boolean bl2 = selectedTab.getType() == ItemGroup.Type.CATEGORY;
      boolean bl3 = selectedTab.getType() == ItemGroup.Type.SEARCH;
      TooltipContext.Default lv = this.client.options.advancedItemTooltips ? TooltipContext.Default.ADVANCED : TooltipContext.Default.BASIC;
      TooltipContext lv2 = bl ? lv.withCreative() : lv;
      List list = stack.getTooltip(this.client.player, lv2);
      Object list2;
      if (bl2 && bl) {
         list2 = list;
      } else {
         list2 = Lists.newArrayList(list);
         if (bl3 && bl) {
            this.searchResultTags.forEach((tag) -> {
               if (stack.isIn(tag)) {
                  list2.add(1, Text.literal("#" + tag.id()).formatted(Formatting.DARK_PURPLE));
               }

            });
         }

         int k = 1;
         Iterator var13 = ItemGroups.getGroupsToDisplay().iterator();

         while(var13.hasNext()) {
            ItemGroup lv3 = (ItemGroup)var13.next();
            if (lv3.getType() != ItemGroup.Type.SEARCH && lv3.contains(stack)) {
               ((List)list2).add(k++, lv3.getDisplayName().copy().formatted(Formatting.BLUE));
            }
         }
      }

      this.renderTooltip(matrices, (List)list2, stack.getTooltipData(), x, y);
   }

   protected void drawBackground(MatrixStack matrices, float delta, int mouseX, int mouseY) {
      Iterator var5 = ItemGroups.getGroupsToDisplay().iterator();

      while(var5.hasNext()) {
         ItemGroup lv = (ItemGroup)var5.next();
         RenderSystem.setShaderTexture(0, TEXTURE);
         if (lv != selectedTab) {
            this.renderTabIcon(matrices, lv);
         }
      }

      RenderSystem.setShaderTexture(0, new Identifier("textures/gui/container/creative_inventory/tab_" + selectedTab.getTexture()));
      drawTexture(matrices, this.x, this.y, 0, 0, this.backgroundWidth, this.backgroundHeight);
      this.searchBox.render(matrices, mouseX, mouseY, delta);
      int k = this.x + 175;
      int l = this.y + 18;
      int m = l + 112;
      RenderSystem.setShaderTexture(0, TEXTURE);
      if (selectedTab.hasScrollbar()) {
         drawTexture(matrices, k, l + (int)((float)(m - l - 17) * this.scrollPosition), 232 + (this.hasScrollbar() ? 0 : 12), 0, 12, 15);
      }

      this.renderTabIcon(matrices, selectedTab);
      if (selectedTab.getType() == ItemGroup.Type.INVENTORY) {
         InventoryScreen.drawEntity(matrices, this.x + 88, this.y + 45, 20, (float)(this.x + 88 - mouseX), (float)(this.y + 45 - 30 - mouseY), this.client.player);
      }

   }

   private int getTabX(ItemGroup group) {
      int i = group.getColumn();
      int j = true;
      int k = 27 * i;
      if (group.isSpecial()) {
         k = this.backgroundWidth - 27 * (7 - i) + 1;
      }

      return k;
   }

   private int getTabY(ItemGroup group) {
      int i = 0;
      if (group.getRow() == ItemGroup.Row.TOP) {
         i -= 32;
      } else {
         i += this.backgroundHeight;
      }

      return i;
   }

   protected boolean isClickInTab(ItemGroup group, double mouseX, double mouseY) {
      int i = this.getTabX(group);
      int j = this.getTabY(group);
      return mouseX >= (double)i && mouseX <= (double)(i + 26) && mouseY >= (double)j && mouseY <= (double)(j + 32);
   }

   protected boolean renderTabTooltipIfHovered(MatrixStack matrices, ItemGroup group, int mouseX, int mouseY) {
      int k = this.getTabX(group);
      int l = this.getTabY(group);
      if (this.isPointWithinBounds(k + 3, l + 3, 21, 27, (double)mouseX, (double)mouseY)) {
         this.renderTooltip(matrices, group.getDisplayName(), mouseX, mouseY);
         return true;
      } else {
         return false;
      }
   }

   protected void renderTabIcon(MatrixStack matrices, ItemGroup group) {
      boolean bl = group == selectedTab;
      boolean bl2 = group.getRow() == ItemGroup.Row.TOP;
      int i = group.getColumn();
      int j = i * 26;
      int k = 0;
      int l = this.x + this.getTabX(group);
      int m = this.y;
      int n = true;
      if (bl) {
         k += 32;
      }

      if (bl2) {
         m -= 28;
      } else {
         k += 64;
         m += this.backgroundHeight - 4;
      }

      drawTexture(matrices, l, m, j, k, 26, 32);
      matrices.push();
      matrices.translate(0.0F, 0.0F, 100.0F);
      l += 5;
      m += 8 + (bl2 ? 1 : -1);
      ItemStack lv = group.getIcon();
      this.itemRenderer.renderInGuiWithOverrides(matrices, lv, l, m);
      this.itemRenderer.renderGuiItemOverlay(matrices, this.textRenderer, lv, l, m);
      matrices.pop();
   }

   public boolean isInventoryTabSelected() {
      return selectedTab.getType() == ItemGroup.Type.INVENTORY;
   }

   public static void onHotbarKeyPress(MinecraftClient client, int index, boolean restore, boolean save) {
      ClientPlayerEntity lv = client.player;
      HotbarStorage lv2 = client.getCreativeHotbarStorage();
      HotbarStorageEntry lv3 = lv2.getSavedHotbar(index);
      int j;
      if (restore) {
         for(j = 0; j < PlayerInventory.getHotbarSize(); ++j) {
            ItemStack lv4 = (ItemStack)lv3.get(j);
            ItemStack lv5 = lv4.isItemEnabled(lv.world.getEnabledFeatures()) ? lv4.copy() : ItemStack.EMPTY;
            lv.getInventory().setStack(j, lv5);
            client.interactionManager.clickCreativeStack(lv5, 36 + j);
         }

         lv.playerScreenHandler.sendContentUpdates();
      } else if (save) {
         for(j = 0; j < PlayerInventory.getHotbarSize(); ++j) {
            lv3.set(j, lv.getInventory().getStack(j).copy());
         }

         Text lv6 = client.options.hotbarKeys[index].getBoundKeyLocalizedText();
         Text lv7 = client.options.loadToolbarActivatorKey.getBoundKeyLocalizedText();
         Text lv8 = Text.translatable("inventory.hotbarSaved", lv7, lv6);
         client.inGameHud.setOverlayMessage(lv8, false);
         client.getNarratorManager().narrate((Text)lv8);
         lv2.save();
      }

   }

   @Environment(EnvType.CLIENT)
   public static class CreativeScreenHandler extends ScreenHandler {
      public final DefaultedList itemList = DefaultedList.of();
      private final ScreenHandler parent;

      public CreativeScreenHandler(PlayerEntity player) {
         super((ScreenHandlerType)null, 0);
         this.parent = player.playerScreenHandler;
         PlayerInventory lv = player.getInventory();

         int i;
         for(i = 0; i < 5; ++i) {
            for(int j = 0; j < 9; ++j) {
               this.addSlot(new LockableSlot(CreativeInventoryScreen.INVENTORY, i * 9 + j, 9 + j * 18, 18 + i * 18));
            }
         }

         for(i = 0; i < 9; ++i) {
            this.addSlot(new Slot(lv, i, 9 + i * 18, 112));
         }

         this.scrollItems(0.0F);
      }

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
         return MathHelper.clamp((float)row / (float)this.getOverflowRows(), 0.0F, 1.0F);
      }

      protected float getScrollPosition(float current, double amount) {
         return MathHelper.clamp(current - (float)(amount / (double)this.getOverflowRows()), 0.0F, 1.0F);
      }

      public void scrollItems(float position) {
         int i = this.getRow(position);

         for(int j = 0; j < 5; ++j) {
            for(int k = 0; k < 9; ++k) {
               int l = k + (j + i) * 9;
               if (l >= 0 && l < this.itemList.size()) {
                  CreativeInventoryScreen.INVENTORY.setStack(k + j * 9, (ItemStack)this.itemList.get(l));
               } else {
                  CreativeInventoryScreen.INVENTORY.setStack(k + j * 9, ItemStack.EMPTY);
               }
            }
         }

      }

      public boolean shouldShowScrollbar() {
         return this.itemList.size() > 45;
      }

      public ItemStack quickMove(PlayerEntity player, int slot) {
         if (slot >= this.slots.size() - 9 && slot < this.slots.size()) {
            Slot lv = (Slot)this.slots.get(slot);
            if (lv != null && lv.hasStack()) {
               lv.setStack(ItemStack.EMPTY);
            }
         }

         return ItemStack.EMPTY;
      }

      public boolean canInsertIntoSlot(ItemStack stack, Slot slot) {
         return slot.inventory != CreativeInventoryScreen.INVENTORY;
      }

      public boolean canInsertIntoSlot(Slot slot) {
         return slot.inventory != CreativeInventoryScreen.INVENTORY;
      }

      public ItemStack getCursorStack() {
         return this.parent.getCursorStack();
      }

      public void setCursorStack(ItemStack stack) {
         this.parent.setCursorStack(stack);
      }
   }

   @Environment(EnvType.CLIENT)
   private static class CreativeSlot extends Slot {
      final Slot slot;

      public CreativeSlot(Slot slot, int invSlot, int x, int y) {
         super(slot.inventory, invSlot, x, y);
         this.slot = slot;
      }

      public void onTakeItem(PlayerEntity player, ItemStack stack) {
         this.slot.onTakeItem(player, stack);
      }

      public boolean canInsert(ItemStack stack) {
         return this.slot.canInsert(stack);
      }

      public ItemStack getStack() {
         return this.slot.getStack();
      }

      public boolean hasStack() {
         return this.slot.hasStack();
      }

      public void setStack(ItemStack stack) {
         this.slot.setStack(stack);
      }

      public void setStackNoCallbacks(ItemStack stack) {
         this.slot.setStackNoCallbacks(stack);
      }

      public void markDirty() {
         this.slot.markDirty();
      }

      public int getMaxItemCount() {
         return this.slot.getMaxItemCount();
      }

      public int getMaxItemCount(ItemStack stack) {
         return this.slot.getMaxItemCount(stack);
      }

      @Nullable
      public Pair getBackgroundSprite() {
         return this.slot.getBackgroundSprite();
      }

      public ItemStack takeStack(int amount) {
         return this.slot.takeStack(amount);
      }

      public boolean isEnabled() {
         return this.slot.isEnabled();
      }

      public boolean canTakeItems(PlayerEntity playerEntity) {
         return this.slot.canTakeItems(playerEntity);
      }
   }

   @Environment(EnvType.CLIENT)
   static class LockableSlot extends Slot {
      public LockableSlot(Inventory arg, int i, int j, int k) {
         super(arg, i, j, k);
      }

      public boolean canTakeItems(PlayerEntity playerEntity) {
         ItemStack lv = this.getStack();
         if (super.canTakeItems(playerEntity) && !lv.isEmpty()) {
            return lv.isItemEnabled(playerEntity.world.getEnabledFeatures()) && lv.getSubNbt("CustomCreativeLock") == null;
         } else {
            return lv.isEmpty();
         }
      }
   }
}
