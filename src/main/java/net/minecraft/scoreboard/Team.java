package net.minecraft.scoreboard;

import com.google.common.collect.Sets;
import java.util.Collection;
import java.util.Set;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.Texts;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.Nullable;

public class Team extends AbstractTeam {
   private static final int field_31884 = 0;
   private static final int field_31885 = 1;
   private final Scoreboard scoreboard;
   private final String name;
   private final Set playerList = Sets.newHashSet();
   private Text displayName;
   private Text prefix;
   private Text suffix;
   private boolean friendlyFire;
   private boolean showFriendlyInvisibles;
   private AbstractTeam.VisibilityRule nameTagVisibilityRule;
   private AbstractTeam.VisibilityRule deathMessageVisibilityRule;
   private Formatting color;
   private AbstractTeam.CollisionRule collisionRule;
   private final Style nameStyle;

   public Team(Scoreboard scoreboard, String name) {
      this.prefix = ScreenTexts.EMPTY;
      this.suffix = ScreenTexts.EMPTY;
      this.friendlyFire = true;
      this.showFriendlyInvisibles = true;
      this.nameTagVisibilityRule = AbstractTeam.VisibilityRule.ALWAYS;
      this.deathMessageVisibilityRule = AbstractTeam.VisibilityRule.ALWAYS;
      this.color = Formatting.RESET;
      this.collisionRule = AbstractTeam.CollisionRule.ALWAYS;
      this.scoreboard = scoreboard;
      this.name = name;
      this.displayName = Text.literal(name);
      this.nameStyle = Style.EMPTY.withInsertion(name).withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.literal(name)));
   }

   public Scoreboard getScoreboard() {
      return this.scoreboard;
   }

   public String getName() {
      return this.name;
   }

   public Text getDisplayName() {
      return this.displayName;
   }

   public MutableText getFormattedName() {
      MutableText lv = Texts.bracketed(this.displayName.copy().fillStyle(this.nameStyle));
      Formatting lv2 = this.getColor();
      if (lv2 != Formatting.RESET) {
         lv.formatted(lv2);
      }

      return lv;
   }

   public void setDisplayName(Text displayName) {
      if (displayName == null) {
         throw new IllegalArgumentException("Name cannot be null");
      } else {
         this.displayName = displayName;
         this.scoreboard.updateScoreboardTeam(this);
      }
   }

   public void setPrefix(@Nullable Text prefix) {
      this.prefix = prefix == null ? ScreenTexts.EMPTY : prefix;
      this.scoreboard.updateScoreboardTeam(this);
   }

   public Text getPrefix() {
      return this.prefix;
   }

   public void setSuffix(@Nullable Text suffix) {
      this.suffix = suffix == null ? ScreenTexts.EMPTY : suffix;
      this.scoreboard.updateScoreboardTeam(this);
   }

   public Text getSuffix() {
      return this.suffix;
   }

   public Collection getPlayerList() {
      return this.playerList;
   }

   public MutableText decorateName(Text name) {
      MutableText lv = Text.empty().append(this.prefix).append(name).append(this.suffix);
      Formatting lv2 = this.getColor();
      if (lv2 != Formatting.RESET) {
         lv.formatted(lv2);
      }

      return lv;
   }

   public static MutableText decorateName(@Nullable AbstractTeam team, Text name) {
      return team == null ? name.copy() : team.decorateName(name);
   }

   public boolean isFriendlyFireAllowed() {
      return this.friendlyFire;
   }

   public void setFriendlyFireAllowed(boolean friendlyFire) {
      this.friendlyFire = friendlyFire;
      this.scoreboard.updateScoreboardTeam(this);
   }

   public boolean shouldShowFriendlyInvisibles() {
      return this.showFriendlyInvisibles;
   }

   public void setShowFriendlyInvisibles(boolean showFriendlyInvisible) {
      this.showFriendlyInvisibles = showFriendlyInvisible;
      this.scoreboard.updateScoreboardTeam(this);
   }

   public AbstractTeam.VisibilityRule getNameTagVisibilityRule() {
      return this.nameTagVisibilityRule;
   }

   public AbstractTeam.VisibilityRule getDeathMessageVisibilityRule() {
      return this.deathMessageVisibilityRule;
   }

   public void setNameTagVisibilityRule(AbstractTeam.VisibilityRule nameTagVisibilityRule) {
      this.nameTagVisibilityRule = nameTagVisibilityRule;
      this.scoreboard.updateScoreboardTeam(this);
   }

   public void setDeathMessageVisibilityRule(AbstractTeam.VisibilityRule deathMessageVisibilityRule) {
      this.deathMessageVisibilityRule = deathMessageVisibilityRule;
      this.scoreboard.updateScoreboardTeam(this);
   }

   public AbstractTeam.CollisionRule getCollisionRule() {
      return this.collisionRule;
   }

   public void setCollisionRule(AbstractTeam.CollisionRule collisionRule) {
      this.collisionRule = collisionRule;
      this.scoreboard.updateScoreboardTeam(this);
   }

   public int getFriendlyFlagsBitwise() {
      int i = 0;
      if (this.isFriendlyFireAllowed()) {
         i |= 1;
      }

      if (this.shouldShowFriendlyInvisibles()) {
         i |= 2;
      }

      return i;
   }

   public void setFriendlyFlagsBitwise(int flags) {
      this.setFriendlyFireAllowed((flags & 1) > 0);
      this.setShowFriendlyInvisibles((flags & 2) > 0);
   }

   public void setColor(Formatting color) {
      this.color = color;
      this.scoreboard.updateScoreboardTeam(this);
   }

   public Formatting getColor() {
      return this.color;
   }
}
