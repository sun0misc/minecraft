package net.minecraft.block;

import java.util.List;
import java.util.Optional;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.entity.MobSpawnerBlockEntity;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.registry.Registries;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class SpawnerBlock extends BlockWithEntity {
   protected SpawnerBlock(AbstractBlock.Settings arg) {
      super(arg);
   }

   public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
      return new MobSpawnerBlockEntity(pos, state);
   }

   @Nullable
   public BlockEntityTicker getTicker(World world, BlockState state, BlockEntityType type) {
      return checkType(type, BlockEntityType.MOB_SPAWNER, world.isClient ? MobSpawnerBlockEntity::clientTick : MobSpawnerBlockEntity::serverTick);
   }

   public void onStacksDropped(BlockState state, ServerWorld world, BlockPos pos, ItemStack tool, boolean dropExperience) {
      super.onStacksDropped(state, world, pos, tool, dropExperience);
      if (dropExperience) {
         int i = 15 + world.random.nextInt(15) + world.random.nextInt(15);
         this.dropExperience(world, pos, i);
      }

   }

   public BlockRenderType getRenderType(BlockState state) {
      return BlockRenderType.MODEL;
   }

   public void appendTooltip(ItemStack stack, @Nullable BlockView world, List tooltip, TooltipContext options) {
      super.appendTooltip(stack, world, tooltip, options);
      Optional optional = this.getEntityNameForTooltip(stack);
      if (optional.isPresent()) {
         tooltip.add((Text)optional.get());
      } else {
         tooltip.add(ScreenTexts.EMPTY);
         tooltip.add(Text.translatable("block.minecraft.spawner.desc1").formatted(Formatting.GRAY));
         tooltip.add(ScreenTexts.space().append((Text)Text.translatable("block.minecraft.spawner.desc2").formatted(Formatting.BLUE)));
      }

   }

   private Optional getEntityNameForTooltip(ItemStack stack) {
      NbtCompound lv = BlockItem.getBlockEntityNbt(stack);
      if (lv != null && lv.contains("SpawnData", NbtElement.COMPOUND_TYPE)) {
         String string = lv.getCompound("SpawnData").getCompound("entity").getString("id");
         Identifier lv2 = Identifier.tryParse(string);
         if (lv2 != null) {
            return Registries.ENTITY_TYPE.getOrEmpty(lv2).map((entityType) -> {
               return Text.translatable(entityType.getTranslationKey()).formatted(Formatting.GRAY);
            });
         }
      }

      return Optional.empty();
   }
}
