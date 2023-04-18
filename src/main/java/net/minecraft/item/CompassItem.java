package net.minecraft.item;

import com.mojang.logging.LogUtils;
import com.mojang.serialization.DataResult;
import java.util.Objects;
import java.util.Optional;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.nbt.NbtOps;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.GlobalPos;
import net.minecraft.world.World;
import net.minecraft.world.poi.PointOfInterestTypes;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

public class CompassItem extends Item implements Vanishable {
   private static final Logger LOGGER = LogUtils.getLogger();
   public static final String LODESTONE_POS_KEY = "LodestonePos";
   public static final String LODESTONE_DIMENSION_KEY = "LodestoneDimension";
   public static final String LODESTONE_TRACKED_KEY = "LodestoneTracked";

   public CompassItem(Item.Settings arg) {
      super(arg);
   }

   public static boolean hasLodestone(ItemStack stack) {
      NbtCompound lv = stack.getNbt();
      return lv != null && (lv.contains("LodestoneDimension") || lv.contains("LodestonePos"));
   }

   private static Optional getLodestoneDimension(NbtCompound nbt) {
      return World.CODEC.parse(NbtOps.INSTANCE, nbt.get("LodestoneDimension")).result();
   }

   @Nullable
   public static GlobalPos createLodestonePos(NbtCompound nbt) {
      boolean bl = nbt.contains("LodestonePos");
      boolean bl2 = nbt.contains("LodestoneDimension");
      if (bl && bl2) {
         Optional optional = getLodestoneDimension(nbt);
         if (optional.isPresent()) {
            BlockPos lv = NbtHelper.toBlockPos(nbt.getCompound("LodestonePos"));
            return GlobalPos.create((RegistryKey)optional.get(), lv);
         }
      }

      return null;
   }

   @Nullable
   public static GlobalPos createSpawnPos(World world) {
      return world.getDimension().natural() ? GlobalPos.create(world.getRegistryKey(), world.getSpawnPos()) : null;
   }

   public boolean hasGlint(ItemStack stack) {
      return hasLodestone(stack) || super.hasGlint(stack);
   }

   public void inventoryTick(ItemStack stack, World world, Entity entity, int slot, boolean selected) {
      if (!world.isClient) {
         if (hasLodestone(stack)) {
            NbtCompound lv = stack.getOrCreateNbt();
            if (lv.contains("LodestoneTracked") && !lv.getBoolean("LodestoneTracked")) {
               return;
            }

            Optional optional = getLodestoneDimension(lv);
            if (optional.isPresent() && optional.get() == world.getRegistryKey() && lv.contains("LodestonePos")) {
               BlockPos lv2 = NbtHelper.toBlockPos(lv.getCompound("LodestonePos"));
               if (!world.isInBuildLimit(lv2) || !((ServerWorld)world).getPointOfInterestStorage().hasTypeAt(PointOfInterestTypes.LODESTONE, lv2)) {
                  lv.remove("LodestonePos");
               }
            }
         }

      }
   }

   public ActionResult useOnBlock(ItemUsageContext context) {
      BlockPos lv = context.getBlockPos();
      World lv2 = context.getWorld();
      if (!lv2.getBlockState(lv).isOf(Blocks.LODESTONE)) {
         return super.useOnBlock(context);
      } else {
         lv2.playSound((PlayerEntity)null, lv, SoundEvents.ITEM_LODESTONE_COMPASS_LOCK, SoundCategory.PLAYERS, 1.0F, 1.0F);
         PlayerEntity lv3 = context.getPlayer();
         ItemStack lv4 = context.getStack();
         boolean bl = !lv3.getAbilities().creativeMode && lv4.getCount() == 1;
         if (bl) {
            this.writeNbt(lv2.getRegistryKey(), lv, lv4.getOrCreateNbt());
         } else {
            ItemStack lv5 = new ItemStack(Items.COMPASS, 1);
            NbtCompound lv6 = lv4.hasNbt() ? lv4.getNbt().copy() : new NbtCompound();
            lv5.setNbt(lv6);
            if (!lv3.getAbilities().creativeMode) {
               lv4.decrement(1);
            }

            this.writeNbt(lv2.getRegistryKey(), lv, lv6);
            if (!lv3.getInventory().insertStack(lv5)) {
               lv3.dropItem(lv5, false);
            }
         }

         return ActionResult.success(lv2.isClient);
      }
   }

   private void writeNbt(RegistryKey worldKey, BlockPos pos, NbtCompound nbt) {
      nbt.put("LodestonePos", NbtHelper.fromBlockPos(pos));
      DataResult var10000 = World.CODEC.encodeStart(NbtOps.INSTANCE, worldKey);
      Logger var10001 = LOGGER;
      Objects.requireNonNull(var10001);
      var10000.resultOrPartial(var10001::error).ifPresent((arg2) -> {
         nbt.put("LodestoneDimension", arg2);
      });
      nbt.putBoolean("LodestoneTracked", true);
   }

   public String getTranslationKey(ItemStack stack) {
      return hasLodestone(stack) ? "item.minecraft.lodestone_compass" : super.getTranslationKey(stack);
   }
}
