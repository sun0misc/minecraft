package net.minecraft.block.entity;

import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.text.Text;
import net.minecraft.util.Nameable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class EnchantingTableBlockEntity extends BlockEntity implements Nameable {
   public int ticks;
   public float nextPageAngle;
   public float pageAngle;
   public float flipRandom;
   public float flipTurn;
   public float nextPageTurningSpeed;
   public float pageTurningSpeed;
   public float bookRotation;
   public float lastBookRotation;
   public float targetBookRotation;
   private static final Random RANDOM = Random.create();
   private Text customName;

   public EnchantingTableBlockEntity(BlockPos pos, BlockState state) {
      super(BlockEntityType.ENCHANTING_TABLE, pos, state);
   }

   protected void writeNbt(NbtCompound nbt) {
      super.writeNbt(nbt);
      if (this.hasCustomName()) {
         nbt.putString("CustomName", Text.Serializer.toJson(this.customName));
      }

   }

   public void readNbt(NbtCompound nbt) {
      super.readNbt(nbt);
      if (nbt.contains("CustomName", NbtElement.STRING_TYPE)) {
         this.customName = Text.Serializer.fromJson(nbt.getString("CustomName"));
      }

   }

   public static void tick(World world, BlockPos pos, BlockState state, EnchantingTableBlockEntity blockEntity) {
      blockEntity.pageTurningSpeed = blockEntity.nextPageTurningSpeed;
      blockEntity.lastBookRotation = blockEntity.bookRotation;
      PlayerEntity lv = world.getClosestPlayer((double)pos.getX() + 0.5, (double)pos.getY() + 0.5, (double)pos.getZ() + 0.5, 3.0, false);
      if (lv != null) {
         double d = lv.getX() - ((double)pos.getX() + 0.5);
         double e = lv.getZ() - ((double)pos.getZ() + 0.5);
         blockEntity.targetBookRotation = (float)MathHelper.atan2(e, d);
         blockEntity.nextPageTurningSpeed += 0.1F;
         if (blockEntity.nextPageTurningSpeed < 0.5F || RANDOM.nextInt(40) == 0) {
            float f = blockEntity.flipRandom;

            do {
               blockEntity.flipRandom += (float)(RANDOM.nextInt(4) - RANDOM.nextInt(4));
            } while(f == blockEntity.flipRandom);
         }
      } else {
         blockEntity.targetBookRotation += 0.02F;
         blockEntity.nextPageTurningSpeed -= 0.1F;
      }

      while(blockEntity.bookRotation >= 3.1415927F) {
         blockEntity.bookRotation -= 6.2831855F;
      }

      while(blockEntity.bookRotation < -3.1415927F) {
         blockEntity.bookRotation += 6.2831855F;
      }

      while(blockEntity.targetBookRotation >= 3.1415927F) {
         blockEntity.targetBookRotation -= 6.2831855F;
      }

      while(blockEntity.targetBookRotation < -3.1415927F) {
         blockEntity.targetBookRotation += 6.2831855F;
      }

      float g;
      for(g = blockEntity.targetBookRotation - blockEntity.bookRotation; g >= 3.1415927F; g -= 6.2831855F) {
      }

      while(g < -3.1415927F) {
         g += 6.2831855F;
      }

      blockEntity.bookRotation += g * 0.4F;
      blockEntity.nextPageTurningSpeed = MathHelper.clamp(blockEntity.nextPageTurningSpeed, 0.0F, 1.0F);
      ++blockEntity.ticks;
      blockEntity.pageAngle = blockEntity.nextPageAngle;
      float h = (blockEntity.flipRandom - blockEntity.nextPageAngle) * 0.4F;
      float i = 0.2F;
      h = MathHelper.clamp(h, -0.2F, 0.2F);
      blockEntity.flipTurn += (h - blockEntity.flipTurn) * 0.9F;
      blockEntity.nextPageAngle += blockEntity.flipTurn;
   }

   public Text getName() {
      return (Text)(this.customName != null ? this.customName : Text.translatable("container.enchant"));
   }

   public void setCustomName(@Nullable Text customName) {
      this.customName = customName;
   }

   @Nullable
   public Text getCustomName() {
      return this.customName;
   }
}
