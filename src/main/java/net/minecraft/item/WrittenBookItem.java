package net.minecraft.item;

import java.util.Iterator;
import java.util.List;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.LecternBlock;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.stat.Stats;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.text.Texts;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.StringHelper;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class WrittenBookItem extends Item {
   public static final int MAX_TITLE_EDIT_LENGTH = 16;
   public static final int MAX_TITLE_VIEW_LENGTH = 32;
   public static final int MAX_PAGE_EDIT_LENGTH = 1024;
   public static final int MAX_PAGE_VIEW_LENGTH = 32767;
   public static final int MAX_PAGES = 100;
   public static final int field_30934 = 2;
   public static final String TITLE_KEY = "title";
   public static final String FILTERED_TITLE_KEY = "filtered_title";
   public static final String AUTHOR_KEY = "author";
   public static final String PAGES_KEY = "pages";
   public static final String FILTERED_PAGES_KEY = "filtered_pages";
   public static final String GENERATION_KEY = "generation";
   public static final String RESOLVED_KEY = "resolved";

   public WrittenBookItem(Item.Settings arg) {
      super(arg);
   }

   public static boolean isValid(@Nullable NbtCompound nbt) {
      if (!WritableBookItem.isValid(nbt)) {
         return false;
      } else if (!nbt.contains("title", NbtElement.STRING_TYPE)) {
         return false;
      } else {
         String string = nbt.getString("title");
         return string.length() > 32 ? false : nbt.contains("author", NbtElement.STRING_TYPE);
      }
   }

   public static int getGeneration(ItemStack stack) {
      return stack.getNbt().getInt("generation");
   }

   public static int getPageCount(ItemStack stack) {
      NbtCompound lv = stack.getNbt();
      return lv != null ? lv.getList("pages", NbtElement.STRING_TYPE).size() : 0;
   }

   public Text getName(ItemStack stack) {
      NbtCompound lv = stack.getNbt();
      if (lv != null) {
         String string = lv.getString("title");
         if (!StringHelper.isEmpty(string)) {
            return Text.literal(string);
         }
      }

      return super.getName(stack);
   }

   public void appendTooltip(ItemStack stack, @Nullable World world, List tooltip, TooltipContext context) {
      if (stack.hasNbt()) {
         NbtCompound lv = stack.getNbt();
         String string = lv.getString("author");
         if (!StringHelper.isEmpty(string)) {
            tooltip.add(Text.translatable("book.byAuthor", string).formatted(Formatting.GRAY));
         }

         tooltip.add(Text.translatable("book.generation." + lv.getInt("generation")).formatted(Formatting.GRAY));
      }

   }

   public ActionResult useOnBlock(ItemUsageContext context) {
      World lv = context.getWorld();
      BlockPos lv2 = context.getBlockPos();
      BlockState lv3 = lv.getBlockState(lv2);
      if (lv3.isOf(Blocks.LECTERN)) {
         return LecternBlock.putBookIfAbsent(context.getPlayer(), lv, lv2, lv3, context.getStack()) ? ActionResult.success(lv.isClient) : ActionResult.PASS;
      } else {
         return ActionResult.PASS;
      }
   }

   public TypedActionResult use(World world, PlayerEntity user, Hand hand) {
      ItemStack lv = user.getStackInHand(hand);
      user.useBook(lv, hand);
      user.incrementStat(Stats.USED.getOrCreateStat(this));
      return TypedActionResult.success(lv, world.isClient());
   }

   public static boolean resolve(ItemStack book, @Nullable ServerCommandSource commandSource, @Nullable PlayerEntity player) {
      NbtCompound lv = book.getNbt();
      if (lv != null && !lv.getBoolean("resolved")) {
         lv.putBoolean("resolved", true);
         if (!isValid(lv)) {
            return false;
         } else {
            NbtList lv2 = lv.getList("pages", NbtElement.STRING_TYPE);
            NbtList lv3 = new NbtList();

            for(int i = 0; i < lv2.size(); ++i) {
               String string = textToJson(commandSource, player, lv2.getString(i));
               if (string.length() > 32767) {
                  return false;
               }

               lv3.add(i, (NbtElement)NbtString.of(string));
            }

            if (lv.contains("filtered_pages", NbtElement.COMPOUND_TYPE)) {
               NbtCompound lv4 = lv.getCompound("filtered_pages");
               NbtCompound lv5 = new NbtCompound();
               Iterator var8 = lv4.getKeys().iterator();

               while(var8.hasNext()) {
                  String string2 = (String)var8.next();
                  String string3 = textToJson(commandSource, player, lv4.getString(string2));
                  if (string3.length() > 32767) {
                     return false;
                  }

                  lv5.putString(string2, string3);
               }

               lv.put("filtered_pages", lv5);
            }

            lv.put("pages", lv3);
            return true;
         }
      } else {
         return false;
      }
   }

   private static String textToJson(@Nullable ServerCommandSource commandSource, @Nullable PlayerEntity player, String text) {
      MutableText lv;
      try {
         lv = Text.Serializer.fromLenientJson(text);
         lv = Texts.parse(commandSource, (Text)lv, player, 0);
      } catch (Exception var5) {
         lv = Text.literal(text);
      }

      return Text.Serializer.toJson(lv);
   }

   public boolean hasGlint(ItemStack stack) {
      return true;
   }
}
