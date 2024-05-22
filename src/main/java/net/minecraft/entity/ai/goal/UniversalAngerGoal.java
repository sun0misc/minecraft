/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.entity.ai.goal;

import java.util.List;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.mob.Angerable;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.util.math.Box;
import net.minecraft.world.GameRules;

public class UniversalAngerGoal<T extends MobEntity>
extends Goal {
    private static final int BOX_VERTICAL_EXPANSION = 10;
    private final T mob;
    private final boolean triggerOthers;
    private int lastAttackedTime;

    public UniversalAngerGoal(T mob, boolean triggerOthers) {
        this.mob = mob;
        this.triggerOthers = triggerOthers;
    }

    @Override
    public boolean canStart() {
        return ((Entity)this.mob).getWorld().getGameRules().getBoolean(GameRules.UNIVERSAL_ANGER) && this.canStartUniversalAnger();
    }

    private boolean canStartUniversalAnger() {
        return ((LivingEntity)this.mob).getAttacker() != null && ((LivingEntity)this.mob).getAttacker().getType() == EntityType.PLAYER && ((LivingEntity)this.mob).getLastAttackedTime() > this.lastAttackedTime;
    }

    @Override
    public void start() {
        this.lastAttackedTime = ((LivingEntity)this.mob).getLastAttackedTime();
        ((Angerable)this.mob).universallyAnger();
        if (this.triggerOthers) {
            this.getOthersInRange().stream().filter(entity -> entity != this.mob).map(entity -> (Angerable)((Object)entity)).forEach(Angerable::universallyAnger);
        }
        super.start();
    }

    private List<? extends MobEntity> getOthersInRange() {
        double d = ((LivingEntity)this.mob).getAttributeValue(EntityAttributes.GENERIC_FOLLOW_RANGE);
        Box lv = Box.from(((Entity)this.mob).getPos()).expand(d, 10.0, d);
        return ((Entity)this.mob).getWorld().getEntitiesByClass(this.mob.getClass(), lv, EntityPredicates.EXCEPT_SPECTATOR);
    }
}

