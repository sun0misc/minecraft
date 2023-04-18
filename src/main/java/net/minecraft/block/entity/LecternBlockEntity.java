package net.minecraft.block.entity;

import net.minecraft.block.BlockState;
import net.minecraft.block.LecternBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.WrittenBookItem;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.screen.LecternScreenHandler;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.PropertyDelegate;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.command.CommandOutput;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Clearable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

public class LecternBlockEntity extends BlockEntity implements Clearable, NamedScreenHandlerFactory {
   public static final int field_31348 = 0;
   public static final int field_31349 = 1;
   public static final int field_31350 = 0;
   public static final int field_31351 = 1;
   private final Inventory inventory = new Inventory() {
      public int size() {
         return 1;
      }

      public boolean isEmpty() {
         return LecternBlockEntity.this.book.isEmpty();
      }

      public ItemStack getStack(int slot) {
         return slot == 0 ? LecternBlockEntity.this.book : ItemStack.EMPTY;
      }

      public ItemStack removeStack(int slot, int amount) {
         if (slot == 0) {
            ItemStack lv = LecternBlockEntity.this.book.split(amount);
            if (LecternBlockEntity.this.book.isEmpty()) {
               LecternBlockEntity.this.onBookRemoved();
            }

            return lv;
         } else {
            return ItemStack.EMPTY;
         }
      }

      public ItemStack removeStack(int slot) {
         if (slot == 0) {
            ItemStack lv = LecternBlockEntity.this.book;
            LecternBlockEntity.this.book = ItemStack.EMPTY;
            LecternBlockEntity.this.onBookRemoved();
            return lv;
         } else {
            return ItemStack.EMPTY;
         }
      }

      public void setStack(int slot, ItemStack stack) {
      }

      public int getMaxCountPerStack() {
         return 1;
      }

      public void markDirty() {
         LecternBlockEntity.this.markDirty();
      }

      public boolean canPlayerUse(PlayerEntity player) {
         return Inventory.canPlayerUse(LecternBlockEntity.this, player) && LecternBlockEntity.this.hasBook();
      }

      public boolean isValid(int slot, ItemStack stack) {
         return false;
      }

      public void clear() {
      }
   };
   private final PropertyDelegate propertyDelegate = new PropertyDelegate() {
      public int get(int index) {
         return index == 0 ? LecternBlockEntity.this.currentPage : 0;
      }

      public void set(int index, int value) {
         if (index == 0) {
            LecternBlockEntity.this.setCurrentPage(value);
         }

      }

      public int size() {
         return 1;
      }
   };
   ItemStack book;
   int currentPage;
   private int pageCount;

   public LecternBlockEntity(BlockPos pos, BlockState state) {
      super(BlockEntityType.LECTERN, pos, state);
      this.book = ItemStack.EMPTY;
   }

   public ItemStack getBook() {
      return this.book;
   }

   public boolean hasBook() {
      return this.book.isOf(Items.WRITABLE_BOOK) || this.book.isOf(Items.WRITTEN_BOOK);
   }

   public void setBook(ItemStack book) {
      this.setBook(book, (PlayerEntity)null);
   }

   void onBookRemoved() {
      this.currentPage = 0;
      this.pageCount = 0;
      LecternBlock.setHasBook((Entity)null, this.getWorld(), this.getPos(), this.getCachedState(), false);
   }

   public void setBook(ItemStack book, @Nullable PlayerEntity player) {
      this.book = this.resolveBook(book, player);
      this.currentPage = 0;
      this.pageCount = WrittenBookItem.getPageCount(this.book);
      this.markDirty();
   }

   void setCurrentPage(int currentPage) {
      int j = MathHelper.clamp(currentPage, 0, this.pageCount - 1);
      if (j != this.currentPage) {
         this.currentPage = j;
         this.markDirty();
         LecternBlock.setPowered(this.getWorld(), this.getPos(), this.getCachedState());
      }

   }

   public int getCurrentPage() {
      return this.currentPage;
   }

   public int getComparatorOutput() {
      float f = this.pageCount > 1 ? (float)this.getCurrentPage() / ((float)this.pageCount - 1.0F) : 1.0F;
      return MathHelper.floor(f * 14.0F) + (this.hasBook() ? 1 : 0);
   }

   private ItemStack resolveBook(ItemStack book, @Nullable PlayerEntity player) {
      if (this.world instanceof ServerWorld && book.isOf(Items.WRITTEN_BOOK)) {
         WrittenBookItem.resolve(book, this.getCommandSource(player), player);
      }

      return book;
   }

   private ServerCommandSource getCommandSource(@Nullable PlayerEntity player) {
      String string;
      Object lv;
      if (player == null) {
         string = "Lectern";
         lv = Text.literal("Lectern");
      } else {
         string = player.getName().getString();
         lv = player.getDisplayName();
      }

      Vec3d lv2 = Vec3d.ofCenter(this.pos);
      return new ServerCommandSource(CommandOutput.DUMMY, lv2, Vec2f.ZERO, (ServerWorld)this.world, 2, string, (Text)lv, this.world.getServer(), player);
   }

   public boolean copyItemDataRequiresOperator() {
      return true;
   }

   public void readNbt(NbtCompound nbt) {
      super.readNbt(nbt);
      if (nbt.contains("Book", NbtElement.COMPOUND_TYPE)) {
         this.book = this.resolveBook(ItemStack.fromNbt(nbt.getCompound("Book")), (PlayerEntity)null);
      } else {
         this.book = ItemStack.EMPTY;
      }

      this.pageCount = WrittenBookItem.getPageCount(this.book);
      this.currentPage = MathHelper.clamp(nbt.getInt("Page"), 0, this.pageCount - 1);
   }

   protected void writeNbt(NbtCompound nbt) {
      super.writeNbt(nbt);
      if (!this.getBook().isEmpty()) {
         nbt.put("Book", this.getBook().writeNbt(new NbtCompound()));
         nbt.putInt("Page", this.currentPage);
      }

   }

   public void clear() {
      this.setBook(ItemStack.EMPTY);
   }

   public ScreenHandler createMenu(int i, PlayerInventory arg, PlayerEntity arg2) {
      return new LecternScreenHandler(i, this.inventory, this.propertyDelegate);
   }

   public Text getDisplayName() {
      return Text.translatable("container.lectern");
   }
}
