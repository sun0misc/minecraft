package net.minecraft.item;

import com.mojang.authlib.GameProfile;
import java.util.UUID;
import net.minecraft.block.Block;
import net.minecraft.block.entity.SkullBlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.text.Text;
import net.minecraft.util.math.Direction;
import org.apache.commons.lang3.StringUtils;

public class SkullItem extends VerticallyAttachableBlockItem {
   public static final String SKULL_OWNER_KEY = "SkullOwner";

   public SkullItem(Block block, Block wallBlock, Item.Settings settings) {
      super(block, wallBlock, settings, Direction.DOWN);
   }

   public Text getName(ItemStack stack) {
      if (stack.isOf(Items.PLAYER_HEAD) && stack.hasNbt()) {
         String string = null;
         NbtCompound lv = stack.getNbt();
         if (lv.contains("SkullOwner", NbtElement.STRING_TYPE)) {
            string = lv.getString("SkullOwner");
         } else if (lv.contains("SkullOwner", NbtElement.COMPOUND_TYPE)) {
            NbtCompound lv2 = lv.getCompound("SkullOwner");
            if (lv2.contains("Name", NbtElement.STRING_TYPE)) {
               string = lv2.getString("Name");
            }
         }

         if (string != null) {
            return Text.translatable(this.getTranslationKey() + ".named", string);
         }
      }

      return super.getName(stack);
   }

   public void postProcessNbt(NbtCompound nbt) {
      super.postProcessNbt(nbt);
      if (nbt.contains("SkullOwner", NbtElement.STRING_TYPE) && !StringUtils.isBlank(nbt.getString("SkullOwner"))) {
         GameProfile gameProfile = new GameProfile((UUID)null, nbt.getString("SkullOwner"));
         SkullBlockEntity.loadProperties(gameProfile, (profile) -> {
            nbt.put("SkullOwner", NbtHelper.writeGameProfile(new NbtCompound(), profile));
         });
      }

   }
}
