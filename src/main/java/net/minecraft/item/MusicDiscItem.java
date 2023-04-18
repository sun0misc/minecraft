package net.minecraft.item;

import com.google.common.collect.Maps;
import java.util.List;
import java.util.Map;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.JukeboxBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.JukeboxBlockEntity;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.sound.SoundEvent;
import net.minecraft.stat.Stats;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;
import org.jetbrains.annotations.Nullable;

public class MusicDiscItem extends Item {
   private static final Map MUSIC_DISCS = Maps.newHashMap();
   private final int comparatorOutput;
   private final SoundEvent sound;
   private final int lengthInTicks;

   protected MusicDiscItem(int comparatorOutput, SoundEvent sound, Item.Settings settings, int lengthInSeconds) {
      super(settings);
      this.comparatorOutput = comparatorOutput;
      this.sound = sound;
      this.lengthInTicks = lengthInSeconds * 20;
      MUSIC_DISCS.put(this.sound, this);
   }

   public ActionResult useOnBlock(ItemUsageContext context) {
      World lv = context.getWorld();
      BlockPos lv2 = context.getBlockPos();
      BlockState lv3 = lv.getBlockState(lv2);
      if (lv3.isOf(Blocks.JUKEBOX) && !(Boolean)lv3.get(JukeboxBlock.HAS_RECORD)) {
         ItemStack lv4 = context.getStack();
         if (!lv.isClient) {
            PlayerEntity lv5 = context.getPlayer();
            BlockEntity var8 = lv.getBlockEntity(lv2);
            if (var8 instanceof JukeboxBlockEntity) {
               JukeboxBlockEntity lv6 = (JukeboxBlockEntity)var8;
               lv6.setStack(lv4.copy());
               lv.emitGameEvent(GameEvent.BLOCK_CHANGE, lv2, GameEvent.Emitter.of(lv5, lv3));
            }

            lv4.decrement(1);
            if (lv5 != null) {
               lv5.incrementStat(Stats.PLAY_RECORD);
            }
         }

         return ActionResult.success(lv.isClient);
      } else {
         return ActionResult.PASS;
      }
   }

   public int getComparatorOutput() {
      return this.comparatorOutput;
   }

   public void appendTooltip(ItemStack stack, @Nullable World world, List tooltip, TooltipContext context) {
      tooltip.add(this.getDescription().formatted(Formatting.GRAY));
   }

   public MutableText getDescription() {
      return Text.translatable(this.getTranslationKey() + ".desc");
   }

   @Nullable
   public static MusicDiscItem bySound(SoundEvent sound) {
      return (MusicDiscItem)MUSIC_DISCS.get(sound);
   }

   public SoundEvent getSound() {
      return this.sound;
   }

   public int getSongLengthInTicks() {
      return this.lengthInTicks;
   }
}
