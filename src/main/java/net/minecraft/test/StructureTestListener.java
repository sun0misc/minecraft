package net.minecraft.test;

import com.google.common.base.MoreObjects;
import java.util.Arrays;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.LecternBlock;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.server.network.DebugInfoSender;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.structure.StructureTemplate;
import net.minecraft.text.Text;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.Formatting;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import org.apache.commons.lang3.exception.ExceptionUtils;

class StructureTestListener implements TestListener {
   private final GameTestState test;
   private final TestManager testManager;
   private final BlockPos pos;
   int attempt;
   int successes;

   public StructureTestListener(GameTestState test, TestManager testManager, BlockPos pos) {
      this.test = test;
      this.testManager = testManager;
      this.pos = pos;
      this.attempt = 0;
      this.successes = 0;
   }

   public void onStarted(GameTestState test) {
      visualizeTest(this.test, Blocks.LIGHT_GRAY_STAINED_GLASS);
      ++this.attempt;
   }

   public void onPassed(GameTestState test) {
      ++this.successes;
      if (!test.isFlaky()) {
         String var10001 = test.getTemplatePath();
         passTest(test, var10001 + " passed! (" + test.getElapsedMilliseconds() + "ms)");
      } else {
         if (this.successes >= test.getRequiredSuccesses()) {
            passTest(test, "" + test + " passed " + this.successes + " times of " + this.attempt + " attempts.");
         } else {
            sendMessageToAllPlayers(this.test.getWorld(), Formatting.GREEN, "Flaky test " + this.test + " succeeded, attempt: " + this.attempt + " successes: " + this.successes);
            this.init();
         }

      }
   }

   public void onFailed(GameTestState test) {
      if (!test.isFlaky()) {
         failTest(test, test.getThrowable());
      } else {
         TestFunction lv = this.test.getTestFunction();
         GameTestState var10000 = this.test;
         String string = "Flaky test " + var10000 + " failed, attempt: " + this.attempt + "/" + lv.getMaxAttempts();
         if (lv.getRequiredSuccesses() > 1) {
            string = string + ", successes: " + this.successes + " (" + lv.getRequiredSuccesses() + " required)";
         }

         sendMessageToAllPlayers(this.test.getWorld(), Formatting.YELLOW, string);
         if (test.getMaxAttempts() - this.attempt + this.successes >= test.getRequiredSuccesses()) {
            this.init();
         } else {
            failTest(test, new NotEnoughSuccessesError(this.attempt, this.successes, test));
         }

      }
   }

   public static void passTest(GameTestState test, String output) {
      visualizeTest(test, Blocks.LIME_STAINED_GLASS);
      finishPassedTest(test, output);
   }

   private static void finishPassedTest(GameTestState test, String output) {
      sendMessageToAllPlayers(test.getWorld(), Formatting.GREEN, output);
      TestFailureLogger.passTest(test);
   }

   protected static void failTest(GameTestState test, Throwable output) {
      visualizeTest(test, test.isRequired() ? Blocks.RED_STAINED_GLASS : Blocks.ORANGE_STAINED_GLASS);
      createTestOutputLectern(test, Util.getInnermostMessage(output));
      finishFailedTest(test, output);
   }

   protected static void finishFailedTest(GameTestState test, Throwable output) {
      String var10000 = output.getMessage();
      String string = var10000 + (output.getCause() == null ? "" : " cause: " + Util.getInnermostMessage(output.getCause()));
      var10000 = test.isRequired() ? "" : "(optional) ";
      String string2 = var10000 + test.getTemplatePath() + " failed! " + string;
      sendMessageToAllPlayers(test.getWorld(), test.isRequired() ? Formatting.RED : Formatting.YELLOW, string2);
      Throwable throwable2 = (Throwable)MoreObjects.firstNonNull(ExceptionUtils.getRootCause(output), output);
      if (throwable2 instanceof PositionedException lv) {
         addGameTestMarker(test.getWorld(), lv.getPos(), lv.getDebugMessage());
      }

      TestFailureLogger.failTest(test);
   }

   private void init() {
      this.test.clearArea();
      GameTestState lv = new GameTestState(this.test.getTestFunction(), this.test.getRotation(), this.test.getWorld());
      lv.startCountdown();
      this.testManager.start(lv);
      lv.addListener(this);
      lv.init(this.pos, 2);
   }

   protected static void visualizeTest(GameTestState test, Block block) {
      ServerWorld lv = test.getWorld();
      BlockPos lv2 = test.getPos();
      BlockPos lv3 = new BlockPos(-1, -1, -1);
      BlockPos lv4 = StructureTemplate.transformAround(lv2.add(lv3), BlockMirror.NONE, test.getRotation(), lv2);
      lv.setBlockState(lv4, Blocks.BEACON.getDefaultState().rotate(test.getRotation()));
      BlockPos lv5 = lv4.add(0, 1, 0);
      lv.setBlockState(lv5, block.getDefaultState());

      for(int i = -1; i <= 1; ++i) {
         for(int j = -1; j <= 1; ++j) {
            BlockPos lv6 = lv4.add(i, -1, j);
            lv.setBlockState(lv6, Blocks.IRON_BLOCK.getDefaultState());
         }
      }

   }

   private static void createTestOutputLectern(GameTestState test, String output) {
      ServerWorld lv = test.getWorld();
      BlockPos lv2 = test.getPos();
      BlockPos lv3 = new BlockPos(-1, 1, -1);
      BlockPos lv4 = StructureTemplate.transformAround(lv2.add(lv3), BlockMirror.NONE, test.getRotation(), lv2);
      lv.setBlockState(lv4, Blocks.LECTERN.getDefaultState().rotate(test.getRotation()));
      BlockState lv5 = lv.getBlockState(lv4);
      ItemStack lv6 = createBookWithText(test.getTemplatePath(), test.isRequired(), output);
      LecternBlock.putBookIfAbsent((Entity)null, lv, lv4, lv5, lv6);
   }

   private static ItemStack createBookWithText(String text, boolean required, String output) {
      ItemStack lv = new ItemStack(Items.WRITABLE_BOOK);
      NbtList lv2 = new NbtList();
      StringBuffer stringBuffer = new StringBuffer();
      Arrays.stream(text.split("\\.")).forEach((line) -> {
         stringBuffer.append(line).append('\n');
      });
      if (!required) {
         stringBuffer.append("(optional)\n");
      }

      stringBuffer.append("-------------------\n");
      lv2.add(NbtString.of("" + stringBuffer + output));
      lv.setSubNbt("pages", lv2);
      return lv;
   }

   protected static void sendMessageToAllPlayers(ServerWorld world, Formatting formatting, String message) {
      world.getPlayers((player) -> {
         return true;
      }).forEach((player) -> {
         player.sendMessage(Text.literal(message).formatted(formatting));
      });
   }

   private static void addGameTestMarker(ServerWorld world, BlockPos pos, String message) {
      DebugInfoSender.addGameTestMarker(world, pos, message, -2130771968, Integer.MAX_VALUE);
   }
}
