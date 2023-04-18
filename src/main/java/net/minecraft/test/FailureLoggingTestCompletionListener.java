package net.minecraft.test;

import com.mojang.logging.LogUtils;
import net.minecraft.util.Util;
import org.slf4j.Logger;

public class FailureLoggingTestCompletionListener implements TestCompletionListener {
   private static final Logger LOGGER = LogUtils.getLogger();

   public void onTestFailed(GameTestState test) {
      if (test.isRequired()) {
         LOGGER.error("{} failed! {}", test.getTemplatePath(), Util.getInnermostMessage(test.getThrowable()));
      } else {
         LOGGER.warn("(optional) {} failed. {}", test.getTemplatePath(), Util.getInnermostMessage(test.getThrowable()));
      }

   }

   public void onTestPassed(GameTestState test) {
   }
}
