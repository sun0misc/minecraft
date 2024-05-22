/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.entity.damage;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageScaling;
import net.minecraft.entity.damage.DamageType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

public class DamageSource {
    private final RegistryEntry<DamageType> type;
    @Nullable
    private final Entity attacker;
    @Nullable
    private final Entity source;
    @Nullable
    private final Vec3d position;

    public String toString() {
        return "DamageSource (" + this.getType().msgId() + ")";
    }

    public float getExhaustion() {
        return this.getType().exhaustion();
    }

    public boolean isDirect() {
        return this.attacker == this.source;
    }

    private DamageSource(RegistryEntry<DamageType> type, @Nullable Entity source, @Nullable Entity attacker, @Nullable Vec3d position) {
        this.type = type;
        this.attacker = attacker;
        this.source = source;
        this.position = position;
    }

    public DamageSource(RegistryEntry<DamageType> type, @Nullable Entity source, @Nullable Entity attacker) {
        this(type, source, attacker, null);
    }

    public DamageSource(RegistryEntry<DamageType> type, Vec3d position) {
        this(type, null, null, position);
    }

    public DamageSource(RegistryEntry<DamageType> type, @Nullable Entity attacker) {
        this(type, attacker, attacker);
    }

    public DamageSource(RegistryEntry<DamageType> type) {
        this(type, null, null, null);
    }

    @Nullable
    public Entity getSource() {
        return this.source;
    }

    @Nullable
    public Entity getAttacker() {
        return this.attacker;
    }

    public Text getDeathMessage(LivingEntity killed) {
        String string = "death.attack." + this.getType().msgId();
        if (this.attacker != null || this.source != null) {
            ItemStack lv3;
            Text lv = this.attacker == null ? this.source.getDisplayName() : this.attacker.getDisplayName();
            Entity entity = this.attacker;
            if (entity instanceof LivingEntity) {
                LivingEntity lv2 = (LivingEntity)entity;
                v0 = lv2.getMainHandStack();
            } else {
                v0 = lv3 = ItemStack.EMPTY;
            }
            if (!lv3.isEmpty() && lv3.contains(DataComponentTypes.CUSTOM_NAME)) {
                return Text.translatable(string + ".item", killed.getDisplayName(), lv, lv3.toHoverableText());
            }
            return Text.translatable(string, killed.getDisplayName(), lv);
        }
        LivingEntity lv4 = killed.getPrimeAdversary();
        String string2 = string + ".player";
        if (lv4 != null) {
            return Text.translatable(string2, killed.getDisplayName(), lv4.getDisplayName());
        }
        return Text.translatable(string, killed.getDisplayName());
    }

    public String getName() {
        return this.getType().msgId();
    }

    public boolean isScaledWithDifficulty() {
        return switch (this.getType().scaling()) {
            default -> throw new MatchException(null, null);
            case DamageScaling.NEVER -> false;
            case DamageScaling.WHEN_CAUSED_BY_LIVING_NON_PLAYER -> {
                if (this.attacker instanceof LivingEntity && !(this.attacker instanceof PlayerEntity)) {
                    yield true;
                }
                yield false;
            }
            case DamageScaling.ALWAYS -> true;
        };
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    public boolean isSourceCreativePlayer() {
        Entity entity = this.getAttacker();
        if (!(entity instanceof PlayerEntity)) return false;
        PlayerEntity lv = (PlayerEntity)entity;
        if (!lv.getAbilities().creativeMode) return false;
        return true;
    }

    @Nullable
    public Vec3d getPosition() {
        if (this.position != null) {
            return this.position;
        }
        if (this.source != null) {
            return this.source.getPos();
        }
        return null;
    }

    @Nullable
    public Vec3d getStoredPosition() {
        return this.position;
    }

    public boolean isIn(TagKey<DamageType> tag) {
        return this.type.isIn(tag);
    }

    public boolean isOf(RegistryKey<DamageType> typeKey) {
        return this.type.matchesKey(typeKey);
    }

    public DamageType getType() {
        return this.type.value();
    }

    public RegistryEntry<DamageType> getTypeRegistryEntry() {
        return this.type;
    }
}

