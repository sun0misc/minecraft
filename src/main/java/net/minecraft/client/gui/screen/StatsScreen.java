package net.minecraft.client.gui.screen;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.mojang.blaze3d.systems.RenderSystem;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectListIterator;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.Block;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.widget.AlwaysSelectedEntryListWidget;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.EntityType;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.ClientStatusC2SPacket;
import net.minecraft.registry.Registries;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stat;
import net.minecraft.stat.StatHandler;
import net.minecraft.stat.StatType;
import net.minecraft.stat.Stats;
import net.minecraft.text.StringVisitable;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import org.jetbrains.annotations.Nullable;

@Environment(EnvType.CLIENT)
public class StatsScreen extends Screen implements StatsListener {
   private static final Text DOWNLOADING_STATS_TEXT = Text.translatable("multiplayer.downloadingStats");
   protected final Screen parent;
   private GeneralStatsListWidget generalStats;
   ItemStatsListWidget itemStats;
   private EntityStatsListWidget mobStats;
   final StatHandler statHandler;
   @Nullable
   private AlwaysSelectedEntryListWidget selectedList;
   private boolean downloadingStats = true;
   private static final int field_32281 = 128;
   private static final int field_32282 = 18;
   private static final int field_32283 = 20;
   private static final int field_32284 = 1;
   private static final int field_32285 = 1;
   private static final int field_32274 = 2;
   private static final int field_32275 = 2;
   private static final int field_32276 = 40;
   private static final int field_32277 = 5;
   private static final int field_32278 = 0;
   private static final int field_32279 = -1;
   private static final int field_32280 = 1;

   public StatsScreen(Screen parent, StatHandler statHandler) {
      super(Text.translatable("gui.stats"));
      this.parent = parent;
      this.statHandler = statHandler;
   }

   protected void init() {
      this.downloadingStats = true;
      this.client.getNetworkHandler().sendPacket(new ClientStatusC2SPacket(ClientStatusC2SPacket.Mode.REQUEST_STATS));
   }

   public void createLists() {
      this.generalStats = new GeneralStatsListWidget(this.client);
      this.itemStats = new ItemStatsListWidget(this.client);
      this.mobStats = new EntityStatsListWidget(this.client);
   }

   public void createButtons() {
      this.addDrawableChild(ButtonWidget.builder(Text.translatable("stat.generalButton"), (button) -> {
         this.selectStatList(this.generalStats);
      }).dimensions(this.width / 2 - 120, this.height - 52, 80, 20).build());
      ButtonWidget lv = (ButtonWidget)this.addDrawableChild(ButtonWidget.builder(Text.translatable("stat.itemsButton"), (button) -> {
         this.selectStatList(this.itemStats);
      }).dimensions(this.width / 2 - 40, this.height - 52, 80, 20).build());
      ButtonWidget lv2 = (ButtonWidget)this.addDrawableChild(ButtonWidget.builder(Text.translatable("stat.mobsButton"), (button) -> {
         this.selectStatList(this.mobStats);
      }).dimensions(this.width / 2 + 40, this.height - 52, 80, 20).build());
      this.addDrawableChild(ButtonWidget.builder(ScreenTexts.DONE, (button) -> {
         this.client.setScreen(this.parent);
      }).dimensions(this.width / 2 - 100, this.height - 28, 200, 20).build());
      if (this.itemStats.children().isEmpty()) {
         lv.active = false;
      }

      if (this.mobStats.children().isEmpty()) {
         lv2.active = false;
      }

   }

   public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
      if (this.downloadingStats) {
         this.renderBackground(matrices);
         drawCenteredTextWithShadow(matrices, this.textRenderer, DOWNLOADING_STATS_TEXT, this.width / 2, this.height / 2, 16777215);
         TextRenderer var10001 = this.textRenderer;
         String var10002 = PROGRESS_BAR_STAGES[(int)(Util.getMeasuringTimeMs() / 150L % (long)PROGRESS_BAR_STAGES.length)];
         int var10003 = this.width / 2;
         int var10004 = this.height / 2;
         Objects.requireNonNull(this.textRenderer);
         drawCenteredTextWithShadow(matrices, var10001, var10002, var10003, var10004 + 9 * 2, 16777215);
      } else {
         this.getSelectedStatList().render(matrices, mouseX, mouseY, delta);
         drawCenteredTextWithShadow(matrices, this.textRenderer, this.title, this.width / 2, 20, 16777215);
         super.render(matrices, mouseX, mouseY, delta);
      }

   }

   public void onStatsReady() {
      if (this.downloadingStats) {
         this.createLists();
         this.createButtons();
         this.selectStatList(this.generalStats);
         this.downloadingStats = false;
      }

   }

   public boolean shouldPause() {
      return !this.downloadingStats;
   }

   @Nullable
   public AlwaysSelectedEntryListWidget getSelectedStatList() {
      return this.selectedList;
   }

   public void selectStatList(@Nullable AlwaysSelectedEntryListWidget list) {
      if (this.selectedList != null) {
         this.remove(this.selectedList);
      }

      if (list != null) {
         this.addSelectableChild(list);
         this.selectedList = list;
      }

   }

   static String getStatTranslationKey(Stat stat) {
      String var10000 = ((Identifier)stat.getValue()).toString();
      return "stat." + var10000.replace(':', '.');
   }

   int getColumnX(int index) {
      return 115 + 40 * index;
   }

   void renderStatItem(MatrixStack matrices, int x, int y, Item item) {
      this.renderIcon(matrices, x + 1, y + 1, 0, 0);
      this.itemRenderer.renderGuiItemIcon(matrices, item.getDefaultStack(), x + 2, y + 2);
   }

   void renderIcon(MatrixStack matrices, int x, int y, int u, int v) {
      RenderSystem.setShaderTexture(0, STATS_ICON_TEXTURE);
      drawTexture(matrices, x, y, 0, (float)u, (float)v, 18, 18, 128, 128);
   }

   @Environment(EnvType.CLIENT)
   class GeneralStatsListWidget extends AlwaysSelectedEntryListWidget {
      public GeneralStatsListWidget(MinecraftClient client) {
         super(client, StatsScreen.this.width, StatsScreen.this.height, 32, StatsScreen.this.height - 64, 10);
         ObjectArrayList objectArrayList = new ObjectArrayList(Stats.CUSTOM.iterator());
         objectArrayList.sort(Comparator.comparing((stat) -> {
            return I18n.translate(StatsScreen.getStatTranslationKey(stat));
         }));
         ObjectListIterator var4 = objectArrayList.iterator();

         while(var4.hasNext()) {
            Stat lv = (Stat)var4.next();
            this.addEntry(new Entry(lv));
         }

      }

      protected void renderBackground(MatrixStack matrices) {
         StatsScreen.this.renderBackground(matrices);
      }

      @Environment(EnvType.CLIENT)
      private class Entry extends AlwaysSelectedEntryListWidget.Entry {
         private final Stat stat;
         private final Text displayName;

         Entry(Stat stat) {
            this.stat = stat;
            this.displayName = Text.translatable(StatsScreen.getStatTranslationKey(stat));
         }

         private String getFormatted() {
            return this.stat.format(StatsScreen.this.statHandler.getStat(this.stat));
         }

         public void render(MatrixStack matrices, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
            DrawableHelper.drawTextWithShadow(matrices, StatsScreen.this.textRenderer, this.displayName, x + 2, y + 1, index % 2 == 0 ? 16777215 : 9474192);
            String string = this.getFormatted();
            DrawableHelper.drawTextWithShadow(matrices, StatsScreen.this.textRenderer, string, x + 2 + 213 - StatsScreen.this.textRenderer.getWidth(string), y + 1, index % 2 == 0 ? 16777215 : 9474192);
         }

         public Text getNarration() {
            return Text.translatable("narrator.select", Text.empty().append(this.displayName).append(ScreenTexts.SPACE).append(this.getFormatted()));
         }
      }
   }

   @Environment(EnvType.CLIENT)
   private class ItemStatsListWidget extends AlwaysSelectedEntryListWidget {
      protected final List blockStatTypes = Lists.newArrayList();
      protected final List itemStatTypes;
      private final int[] HEADER_ICON_SPRITE_INDICES = new int[]{3, 4, 1, 2, 5, 6};
      protected int selectedHeaderColumn = -1;
      protected final Comparator comparator = new ItemComparator();
      @Nullable
      protected StatType selectedStatType;
      protected int listOrder;

      public ItemStatsListWidget(MinecraftClient client) {
         super(client, StatsScreen.this.width, StatsScreen.this.height, 32, StatsScreen.this.height - 64, 20);
         this.blockStatTypes.add(Stats.MINED);
         this.itemStatTypes = Lists.newArrayList(new StatType[]{Stats.BROKEN, Stats.CRAFTED, Stats.USED, Stats.PICKED_UP, Stats.DROPPED});
         this.setRenderHeader(true, 20);
         Set set = Sets.newIdentityHashSet();
         Iterator var4 = Registries.ITEM.iterator();

         Item lv;
         boolean bl;
         Iterator var7;
         StatType lv2;
         while(var4.hasNext()) {
            lv = (Item)var4.next();
            bl = false;
            var7 = this.itemStatTypes.iterator();

            while(var7.hasNext()) {
               lv2 = (StatType)var7.next();
               if (lv2.hasStat(lv) && StatsScreen.this.statHandler.getStat(lv2.getOrCreateStat(lv)) > 0) {
                  bl = true;
               }
            }

            if (bl) {
               set.add(lv);
            }
         }

         var4 = Registries.BLOCK.iterator();

         while(var4.hasNext()) {
            Block lv3 = (Block)var4.next();
            bl = false;
            var7 = this.blockStatTypes.iterator();

            while(var7.hasNext()) {
               lv2 = (StatType)var7.next();
               if (lv2.hasStat(lv3) && StatsScreen.this.statHandler.getStat(lv2.getOrCreateStat(lv3)) > 0) {
                  bl = true;
               }
            }

            if (bl) {
               set.add(lv3.asItem());
            }
         }

         set.remove(Items.AIR);
         var4 = set.iterator();

         while(var4.hasNext()) {
            lv = (Item)var4.next();
            this.addEntry(new Entry(lv));
         }

      }

      protected void renderHeader(MatrixStack matrices, int x, int y) {
         if (!this.client.mouse.wasLeftButtonClicked()) {
            this.selectedHeaderColumn = -1;
         }

         int k;
         for(k = 0; k < this.HEADER_ICON_SPRITE_INDICES.length; ++k) {
            StatsScreen.this.renderIcon(matrices, x + StatsScreen.this.getColumnX(k) - 18, y + 1, 0, this.selectedHeaderColumn == k ? 0 : 18);
         }

         int l;
         if (this.selectedStatType != null) {
            k = StatsScreen.this.getColumnX(this.getHeaderIndex(this.selectedStatType)) - 36;
            l = this.listOrder == 1 ? 2 : 1;
            StatsScreen.this.renderIcon(matrices, x + k, y + 1, 18 * l, 0);
         }

         for(k = 0; k < this.HEADER_ICON_SPRITE_INDICES.length; ++k) {
            l = this.selectedHeaderColumn == k ? 1 : 0;
            StatsScreen.this.renderIcon(matrices, x + StatsScreen.this.getColumnX(k) - 18 + l, y + 1 + l, 18 * this.HEADER_ICON_SPRITE_INDICES[k], 18);
         }

      }

      public int getRowWidth() {
         return 375;
      }

      protected int getScrollbarPositionX() {
         return this.width / 2 + 140;
      }

      protected void renderBackground(MatrixStack matrices) {
         StatsScreen.this.renderBackground(matrices);
      }

      protected void clickedHeader(int x, int y) {
         this.selectedHeaderColumn = -1;

         for(int k = 0; k < this.HEADER_ICON_SPRITE_INDICES.length; ++k) {
            int l = x - StatsScreen.this.getColumnX(k);
            if (l >= -36 && l <= 0) {
               this.selectedHeaderColumn = k;
               break;
            }
         }

         if (this.selectedHeaderColumn >= 0) {
            this.selectStatType(this.getStatType(this.selectedHeaderColumn));
            this.client.getSoundManager().play(PositionedSoundInstance.master((RegistryEntry)SoundEvents.UI_BUTTON_CLICK, 1.0F));
         }

      }

      private StatType getStatType(int headerColumn) {
         return headerColumn < this.blockStatTypes.size() ? (StatType)this.blockStatTypes.get(headerColumn) : (StatType)this.itemStatTypes.get(headerColumn - this.blockStatTypes.size());
      }

      private int getHeaderIndex(StatType statType) {
         int i = this.blockStatTypes.indexOf(statType);
         if (i >= 0) {
            return i;
         } else {
            int j = this.itemStatTypes.indexOf(statType);
            return j >= 0 ? j + this.blockStatTypes.size() : -1;
         }
      }

      protected void renderDecorations(MatrixStack matrices, int mouseX, int mouseY) {
         if (mouseY >= this.top && mouseY <= this.bottom) {
            Entry lv = (Entry)this.getHoveredEntry();
            int k = (this.width - this.getRowWidth()) / 2;
            if (lv != null) {
               if (mouseX < k + 40 || mouseX > k + 40 + 20) {
                  return;
               }

               Item lv2 = lv.getItem();
               this.render(matrices, this.getText(lv2), mouseX, mouseY);
            } else {
               Text lv3 = null;
               int l = mouseX - k;

               for(int m = 0; m < this.HEADER_ICON_SPRITE_INDICES.length; ++m) {
                  int n = StatsScreen.this.getColumnX(m);
                  if (l >= n - 18 && l <= n) {
                     lv3 = this.getStatType(m).getName();
                     break;
                  }
               }

               this.render(matrices, lv3, mouseX, mouseY);
            }

         }
      }

      protected void render(MatrixStack matrices, @Nullable Text text, int mouseX, int mouseY) {
         if (text != null) {
            int k = mouseX + 12;
            int l = mouseY - 12;
            int m = StatsScreen.this.textRenderer.getWidth((StringVisitable)text);
            fillGradient(matrices, k - 3, l - 3, k + m + 3, l + 8 + 3, -1073741824, -1073741824);
            matrices.push();
            matrices.translate(0.0F, 0.0F, 400.0F);
            StatsScreen.this.textRenderer.drawWithShadow(matrices, (Text)text, (float)k, (float)l, -1);
            matrices.pop();
         }
      }

      protected Text getText(Item item) {
         return item.getName();
      }

      protected void selectStatType(StatType statType) {
         if (statType != this.selectedStatType) {
            this.selectedStatType = statType;
            this.listOrder = -1;
         } else if (this.listOrder == -1) {
            this.listOrder = 1;
         } else {
            this.selectedStatType = null;
            this.listOrder = 0;
         }

         this.children().sort(this.comparator);
      }

      @Environment(EnvType.CLIENT)
      private class ItemComparator implements Comparator {
         ItemComparator() {
         }

         public int compare(Entry arg, Entry arg2) {
            Item lv = arg.getItem();
            Item lv2 = arg2.getItem();
            int i;
            int j;
            if (ItemStatsListWidget.this.selectedStatType == null) {
               i = 0;
               j = 0;
            } else {
               StatType lv3;
               if (ItemStatsListWidget.this.blockStatTypes.contains(ItemStatsListWidget.this.selectedStatType)) {
                  lv3 = ItemStatsListWidget.this.selectedStatType;
                  i = lv instanceof BlockItem ? StatsScreen.this.statHandler.getStat(lv3, ((BlockItem)lv).getBlock()) : -1;
                  j = lv2 instanceof BlockItem ? StatsScreen.this.statHandler.getStat(lv3, ((BlockItem)lv2).getBlock()) : -1;
               } else {
                  lv3 = ItemStatsListWidget.this.selectedStatType;
                  i = StatsScreen.this.statHandler.getStat(lv3, lv);
                  j = StatsScreen.this.statHandler.getStat(lv3, lv2);
               }
            }

            return i == j ? ItemStatsListWidget.this.listOrder * Integer.compare(Item.getRawId(lv), Item.getRawId(lv2)) : ItemStatsListWidget.this.listOrder * Integer.compare(i, j);
         }

         // $FF: synthetic method
         public int compare(Object a, Object b) {
            return this.compare((Entry)a, (Entry)b);
         }
      }

      @Environment(EnvType.CLIENT)
      class Entry extends AlwaysSelectedEntryListWidget.Entry {
         private final Item item;

         Entry(Item item) {
            this.item = item;
         }

         public Item getItem() {
            return this.item;
         }

         public void render(MatrixStack matrices, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
            StatsScreen.this.renderStatItem(matrices, x + 40, y, this.item);

            int p;
            for(p = 0; p < StatsScreen.this.itemStats.blockStatTypes.size(); ++p) {
               Stat lv;
               if (this.item instanceof BlockItem) {
                  lv = ((StatType)StatsScreen.this.itemStats.blockStatTypes.get(p)).getOrCreateStat(((BlockItem)this.item).getBlock());
               } else {
                  lv = null;
               }

               this.render(matrices, lv, x + StatsScreen.this.getColumnX(p), y, index % 2 == 0);
            }

            for(p = 0; p < StatsScreen.this.itemStats.itemStatTypes.size(); ++p) {
               this.render(matrices, ((StatType)StatsScreen.this.itemStats.itemStatTypes.get(p)).getOrCreateStat(this.item), x + StatsScreen.this.getColumnX(p + StatsScreen.this.itemStats.blockStatTypes.size()), y, index % 2 == 0);
            }

         }

         protected void render(MatrixStack matrices, @Nullable Stat stat, int x, int y, boolean white) {
            String string = stat == null ? "-" : stat.format(StatsScreen.this.statHandler.getStat(stat));
            DrawableHelper.drawTextWithShadow(matrices, StatsScreen.this.textRenderer, string, x - StatsScreen.this.textRenderer.getWidth(string), y + 5, white ? 16777215 : 9474192);
         }

         public Text getNarration() {
            return Text.translatable("narrator.select", this.item.getName());
         }
      }
   }

   @Environment(EnvType.CLIENT)
   private class EntityStatsListWidget extends AlwaysSelectedEntryListWidget {
      public EntityStatsListWidget(MinecraftClient client) {
         int var10002 = StatsScreen.this.width;
         int var10003 = StatsScreen.this.height;
         int var10005 = StatsScreen.this.height - 64;
         Objects.requireNonNull(StatsScreen.this.textRenderer);
         super(client, var10002, var10003, 32, var10005, 9 * 4);
         Iterator var3 = Registries.ENTITY_TYPE.iterator();

         while(true) {
            EntityType lv;
            do {
               if (!var3.hasNext()) {
                  return;
               }

               lv = (EntityType)var3.next();
            } while(StatsScreen.this.statHandler.getStat(Stats.KILLED.getOrCreateStat(lv)) <= 0 && StatsScreen.this.statHandler.getStat(Stats.KILLED_BY.getOrCreateStat(lv)) <= 0);

            this.addEntry(new Entry(lv));
         }
      }

      protected void renderBackground(MatrixStack matrices) {
         StatsScreen.this.renderBackground(matrices);
      }

      @Environment(EnvType.CLIENT)
      class Entry extends AlwaysSelectedEntryListWidget.Entry {
         private final Text entityTypeName;
         private final Text killedText;
         private final boolean killedAny;
         private final Text killedByText;
         private final boolean killedByAny;

         public Entry(EntityType entityType) {
            this.entityTypeName = entityType.getName();
            int i = StatsScreen.this.statHandler.getStat(Stats.KILLED.getOrCreateStat(entityType));
            if (i == 0) {
               this.killedText = Text.translatable("stat_type.minecraft.killed.none", this.entityTypeName);
               this.killedAny = false;
            } else {
               this.killedText = Text.translatable("stat_type.minecraft.killed", i, this.entityTypeName);
               this.killedAny = true;
            }

            int j = StatsScreen.this.statHandler.getStat(Stats.KILLED_BY.getOrCreateStat(entityType));
            if (j == 0) {
               this.killedByText = Text.translatable("stat_type.minecraft.killed_by.none", this.entityTypeName);
               this.killedByAny = false;
            } else {
               this.killedByText = Text.translatable("stat_type.minecraft.killed_by", this.entityTypeName, j);
               this.killedByAny = true;
            }

         }

         public void render(MatrixStack matrices, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
            DrawableHelper.drawTextWithShadow(matrices, StatsScreen.this.textRenderer, this.entityTypeName, x + 2, y + 1, 16777215);
            TextRenderer var10001 = StatsScreen.this.textRenderer;
            Text var10002 = this.killedText;
            int var10003 = x + 2 + 10;
            int var10004 = y + 1;
            Objects.requireNonNull(StatsScreen.this.textRenderer);
            DrawableHelper.drawTextWithShadow(matrices, var10001, var10002, var10003, var10004 + 9, this.killedAny ? 9474192 : 6316128);
            var10001 = StatsScreen.this.textRenderer;
            var10002 = this.killedByText;
            var10003 = x + 2 + 10;
            var10004 = y + 1;
            Objects.requireNonNull(StatsScreen.this.textRenderer);
            DrawableHelper.drawTextWithShadow(matrices, var10001, var10002, var10003, var10004 + 9 * 2, this.killedByAny ? 9474192 : 6316128);
         }

         public Text getNarration() {
            return Text.translatable("narrator.select", ScreenTexts.joinSentences(this.killedText, this.killedByText));
         }
      }
   }
}
