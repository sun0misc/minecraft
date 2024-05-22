/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.entity.damage;

import com.google.common.collect.Lists;
import java.util.List;
import java.util.Objects;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageRecord;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DeathMessageType;
import net.minecraft.entity.damage.FallLocation;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.tag.DamageTypeTags;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.Texts;
import org.jetbrains.annotations.Nullable;

public class DamageTracker {
    public static final int DAMAGE_COOLDOWN = 100;
    public static final int ATTACK_DAMAGE_COOLDOWN = 300;
    private static final Style INTENTIONAL_GAME_DESIGN_ISSUE_LINK_STYLE = Style.EMPTY.withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://bugs.mojang.com/browse/MCPE-28723")).withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.literal("MCPE-28723")));
    private final List<DamageRecord> recentDamage = Lists.newArrayList();
    private final LivingEntity entity;
    private int ageOnLastDamage;
    private int ageOnLastAttacked;
    private int ageOnLastUpdate;
    private boolean recentlyAttacked;
    private boolean hasDamage;

    public DamageTracker(LivingEntity entity) {
        this.entity = entity;
    }

    public void onDamage(DamageSource damageSource, float damage) {
        this.update();
        FallLocation lv = FallLocation.fromEntity(this.entity);
        DamageRecord lv2 = new DamageRecord(damageSource, damage, lv, this.entity.fallDistance);
        this.recentDamage.add(lv2);
        this.ageOnLastDamage = this.entity.age;
        this.hasDamage = true;
        if (!this.recentlyAttacked && this.entity.isAlive() && DamageTracker.isAttackerLiving(damageSource)) {
            this.recentlyAttacked = true;
            this.ageOnLastUpdate = this.ageOnLastAttacked = this.entity.age;
            this.entity.enterCombat();
        }
    }

    private static boolean isAttackerLiving(DamageSource damageSource) {
        return damageSource.getAttacker() instanceof LivingEntity;
    }

    private Text getAttackedFallDeathMessage(Entity attacker, Text attackerDisplayName, String itemDeathTranslationKey, String deathTranslationKey) {
        ItemStack lv2;
        if (attacker instanceof LivingEntity) {
            LivingEntity lv = (LivingEntity)attacker;
            v0 = lv.getMainHandStack();
        } else {
            v0 = lv2 = ItemStack.EMPTY;
        }
        if (!lv2.isEmpty() && lv2.contains(DataComponentTypes.CUSTOM_NAME)) {
            return Text.translatable(itemDeathTranslationKey, this.entity.getDisplayName(), attackerDisplayName, lv2.toHoverableText());
        }
        return Text.translatable(deathTranslationKey, this.entity.getDisplayName(), attackerDisplayName);
    }

    private Text getFallDeathMessage(DamageRecord damageRecord, @Nullable Entity attacker) {
        DamageSource lv = damageRecord.damageSource();
        if (lv.isIn(DamageTypeTags.IS_FALL) || lv.isIn(DamageTypeTags.ALWAYS_MOST_SIGNIFICANT_FALL)) {
            FallLocation lv2 = Objects.requireNonNullElse(damageRecord.fallLocation(), FallLocation.GENERIC);
            return Text.translatable(lv2.getDeathMessageKey(), this.entity.getDisplayName());
        }
        Text lv3 = DamageTracker.getDisplayName(attacker);
        Entity lv4 = lv.getAttacker();
        Text lv5 = DamageTracker.getDisplayName(lv4);
        if (lv5 != null && !lv5.equals(lv3)) {
            return this.getAttackedFallDeathMessage(lv4, lv5, "death.fell.assist.item", "death.fell.assist");
        }
        if (lv3 != null) {
            return this.getAttackedFallDeathMessage(attacker, lv3, "death.fell.finish.item", "death.fell.finish");
        }
        return Text.translatable("death.fell.killer", this.entity.getDisplayName());
    }

    @Nullable
    private static Text getDisplayName(@Nullable Entity entity) {
        return entity == null ? null : entity.getDisplayName();
    }

    public Text getDeathMessage() {
        if (this.recentDamage.isEmpty()) {
            return Text.translatable("death.attack.generic", this.entity.getDisplayName());
        }
        DamageRecord lv = this.recentDamage.get(this.recentDamage.size() - 1);
        DamageSource lv2 = lv.damageSource();
        DamageRecord lv3 = this.getBiggestFall();
        DeathMessageType lv4 = lv2.getType().deathMessageType();
        if (lv4 == DeathMessageType.FALL_VARIANTS && lv3 != null) {
            return this.getFallDeathMessage(lv3, lv2.getAttacker());
        }
        if (lv4 == DeathMessageType.INTENTIONAL_GAME_DESIGN) {
            String string = "death.attack." + lv2.getName();
            MutableText lv5 = Texts.bracketed(Text.translatable(string + ".link")).fillStyle(INTENTIONAL_GAME_DESIGN_ISSUE_LINK_STYLE);
            return Text.translatable(string + ".message", this.entity.getDisplayName(), lv5);
        }
        return lv2.getDeathMessage(this.entity);
    }

    @Nullable
    private DamageRecord getBiggestFall() {
        DamageRecord lv = null;
        DamageRecord lv2 = null;
        float f = 0.0f;
        float g = 0.0f;
        for (int i = 0; i < this.recentDamage.size(); ++i) {
            float h;
            DamageRecord lv3 = this.recentDamage.get(i);
            DamageRecord lv4 = i > 0 ? this.recentDamage.get(i - 1) : null;
            DamageSource lv5 = lv3.damageSource();
            boolean bl = lv5.isIn(DamageTypeTags.ALWAYS_MOST_SIGNIFICANT_FALL);
            float f2 = h = bl ? Float.MAX_VALUE : lv3.fallDistance();
            if ((lv5.isIn(DamageTypeTags.IS_FALL) || bl) && h > 0.0f && (lv == null || h > g)) {
                lv = i > 0 ? lv4 : lv3;
                g = h;
            }
            if (lv3.fallLocation() == null || lv2 != null && !(lv3.damage() > f)) continue;
            lv2 = lv3;
            f = lv3.damage();
        }
        if (g > 5.0f && lv != null) {
            return lv;
        }
        if (f > 5.0f && lv2 != null) {
            return lv2;
        }
        return null;
    }

    public int getTimeSinceLastAttack() {
        if (this.recentlyAttacked) {
            return this.entity.age - this.ageOnLastAttacked;
        }
        return this.ageOnLastUpdate - this.ageOnLastAttacked;
    }

    public void update() {
        int i;
        int n = i = this.recentlyAttacked ? 300 : 100;
        if (this.hasDamage && (!this.entity.isAlive() || this.entity.age - this.ageOnLastDamage > i)) {
            boolean bl = this.recentlyAttacked;
            this.hasDamage = false;
            this.recentlyAttacked = false;
            this.ageOnLastUpdate = this.entity.age;
            if (bl) {
                this.entity.endCombat();
            }
            this.recentDamage.clear();
        }
    }
}

