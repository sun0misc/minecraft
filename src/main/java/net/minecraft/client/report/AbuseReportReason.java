package net.minecraft.client.report;

import java.util.Locale;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.text.Text;

@Environment(EnvType.CLIENT)
public enum AbuseReportReason {
   HATE_SPEECH("hate_speech"),
   TERRORISM_OR_VIOLENT_EXTREMISM("terrorism_or_violent_extremism"),
   CHILD_SEXUAL_EXPLOITATION_OR_ABUSE("child_sexual_exploitation_or_abuse"),
   IMMINENT_HARM("imminent_harm"),
   NON_CONSENSUAL_INTIMATE_IMAGERY("non_consensual_intimate_imagery"),
   HARASSMENT_OR_BULLYING("harassment_or_bullying"),
   DEFAMATION_IMPERSONATION_FALSE_INFORMATION("defamation_impersonation_false_information"),
   SELF_HARM_OR_SUICIDE("self_harm_or_suicide"),
   ALCOHOL_TOBACCO_DRUGS("alcohol_tobacco_drugs");

   private final String id;
   private final Text text;
   private final Text description;

   private AbuseReportReason(String id) {
      this.id = id.toUpperCase(Locale.ROOT);
      String string3 = "gui.abuseReport.reason." + id;
      this.text = Text.translatable(string3);
      this.description = Text.translatable(string3 + ".description");
   }

   public String getId() {
      return this.id;
   }

   public Text getText() {
      return this.text;
   }

   public Text getDescription() {
      return this.description;
   }

   // $FF: synthetic method
   private static AbuseReportReason[] method_44597() {
      return new AbuseReportReason[]{HATE_SPEECH, TERRORISM_OR_VIOLENT_EXTREMISM, CHILD_SEXUAL_EXPLOITATION_OR_ABUSE, IMMINENT_HARM, NON_CONSENSUAL_INTIMATE_IMAGERY, HARASSMENT_OR_BULLYING, DEFAMATION_IMPERSONATION_FALSE_INFORMATION, SELF_HARM_OR_SUICIDE, ALCOHOL_TOBACCO_DRUGS};
   }
}
