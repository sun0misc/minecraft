package net.minecraft.scoreboard;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.Nullable;

public abstract class AbstractTeam {
   public boolean isEqual(@Nullable AbstractTeam team) {
      if (team == null) {
         return false;
      } else {
         return this == team;
      }
   }

   public abstract String getName();

   public abstract MutableText decorateName(Text name);

   public abstract boolean shouldShowFriendlyInvisibles();

   public abstract boolean isFriendlyFireAllowed();

   public abstract VisibilityRule getNameTagVisibilityRule();

   public abstract Formatting getColor();

   public abstract Collection getPlayerList();

   public abstract VisibilityRule getDeathMessageVisibilityRule();

   public abstract CollisionRule getCollisionRule();

   public static enum CollisionRule {
      ALWAYS("always", 0),
      NEVER("never", 1),
      PUSH_OTHER_TEAMS("pushOtherTeams", 2),
      PUSH_OWN_TEAM("pushOwnTeam", 3);

      private static final Map COLLISION_RULES = (Map)Arrays.stream(values()).collect(Collectors.toMap((collisionRule) -> {
         return collisionRule.name;
      }, (arg) -> {
         return arg;
      }));
      public final String name;
      public final int value;

      @Nullable
      public static CollisionRule getRule(String name) {
         return (CollisionRule)COLLISION_RULES.get(name);
      }

      private CollisionRule(String name, int value) {
         this.name = name;
         this.value = value;
      }

      public Text getDisplayName() {
         return Text.translatable("team.collision." + this.name);
      }

      // $FF: synthetic method
      private static CollisionRule[] method_36797() {
         return new CollisionRule[]{ALWAYS, NEVER, PUSH_OTHER_TEAMS, PUSH_OWN_TEAM};
      }
   }

   public static enum VisibilityRule {
      ALWAYS("always", 0),
      NEVER("never", 1),
      HIDE_FOR_OTHER_TEAMS("hideForOtherTeams", 2),
      HIDE_FOR_OWN_TEAM("hideForOwnTeam", 3);

      private static final Map VISIBILITY_RULES = (Map)Arrays.stream(values()).collect(Collectors.toMap((visibilityRule) -> {
         return visibilityRule.name;
      }, (arg) -> {
         return arg;
      }));
      public final String name;
      public final int value;

      public static String[] getKeys() {
         return (String[])VISIBILITY_RULES.keySet().toArray(new String[0]);
      }

      @Nullable
      public static VisibilityRule getRule(String name) {
         return (VisibilityRule)VISIBILITY_RULES.get(name);
      }

      private VisibilityRule(String name, int value) {
         this.name = name;
         this.value = value;
      }

      public Text getDisplayName() {
         return Text.translatable("team.visibility." + this.name);
      }

      // $FF: synthetic method
      private static VisibilityRule[] method_36798() {
         return new VisibilityRule[]{ALWAYS, NEVER, HIDE_FOR_OTHER_TEAMS, HIDE_FOR_OWN_TEAM};
      }
   }
}
