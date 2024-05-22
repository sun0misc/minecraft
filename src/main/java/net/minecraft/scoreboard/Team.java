/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.scoreboard;

import com.google.common.collect.Sets;
import java.util.Collection;
import java.util.Set;
import net.minecraft.scoreboard.AbstractTeam;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.Texts;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.Nullable;

public class Team
extends AbstractTeam {
    private static final int field_31884 = 0;
    private static final int field_31885 = 1;
    private final Scoreboard scoreboard;
    private final String name;
    private final Set<String> playerList = Sets.newHashSet();
    private Text displayName;
    private Text prefix = ScreenTexts.EMPTY;
    private Text suffix = ScreenTexts.EMPTY;
    private boolean friendlyFire = true;
    private boolean showFriendlyInvisibles = true;
    private AbstractTeam.VisibilityRule nameTagVisibilityRule = AbstractTeam.VisibilityRule.ALWAYS;
    private AbstractTeam.VisibilityRule deathMessageVisibilityRule = AbstractTeam.VisibilityRule.ALWAYS;
    private Formatting color = Formatting.RESET;
    private AbstractTeam.CollisionRule collisionRule = AbstractTeam.CollisionRule.ALWAYS;
    private final Style nameStyle;

    public Team(Scoreboard scoreboard, String name) {
        this.scoreboard = scoreboard;
        this.name = name;
        this.displayName = Text.literal(name);
        this.nameStyle = Style.EMPTY.withInsertion(name).withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.literal(name)));
    }

    public Scoreboard getScoreboard() {
        return this.scoreboard;
    }

    @Override
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
        }
        this.displayName = displayName;
        this.scoreboard.updateScoreboardTeam(this);
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

    @Override
    public Collection<String> getPlayerList() {
        return this.playerList;
    }

    @Override
    public MutableText decorateName(Text name) {
        MutableText lv = Text.empty().append(this.prefix).append(name).append(this.suffix);
        Formatting lv2 = this.getColor();
        if (lv2 != Formatting.RESET) {
            lv.formatted(lv2);
        }
        return lv;
    }

    public static MutableText decorateName(@Nullable AbstractTeam team, Text name) {
        if (team == null) {
            return name.copy();
        }
        return team.decorateName(name);
    }

    @Override
    public boolean isFriendlyFireAllowed() {
        return this.friendlyFire;
    }

    public void setFriendlyFireAllowed(boolean friendlyFire) {
        this.friendlyFire = friendlyFire;
        this.scoreboard.updateScoreboardTeam(this);
    }

    @Override
    public boolean shouldShowFriendlyInvisibles() {
        return this.showFriendlyInvisibles;
    }

    public void setShowFriendlyInvisibles(boolean showFriendlyInvisible) {
        this.showFriendlyInvisibles = showFriendlyInvisible;
        this.scoreboard.updateScoreboardTeam(this);
    }

    @Override
    public AbstractTeam.VisibilityRule getNameTagVisibilityRule() {
        return this.nameTagVisibilityRule;
    }

    @Override
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

    @Override
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

    @Override
    public Formatting getColor() {
        return this.color;
    }
}

