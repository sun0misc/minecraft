/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.entity.ai.goal;

import java.util.EnumSet;
import java.util.List;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.TargetPredicate;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.ai.goal.TrackTargetGoal;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.util.math.Box;
import net.minecraft.world.GameRules;
import org.jetbrains.annotations.Nullable;

public class RevengeGoal
extends TrackTargetGoal {
    private static final TargetPredicate VALID_AVOIDABLES_PREDICATE = TargetPredicate.createAttackable().ignoreVisibility().ignoreDistanceScalingFactor();
    private static final int BOX_VERTICAL_EXPANSION = 10;
    private boolean groupRevenge;
    private int lastAttackedTime;
    private final Class<?>[] noRevengeTypes;
    @Nullable
    private Class<?>[] noHelpTypes;

    public RevengeGoal(PathAwareEntity mob, Class<?> ... noRevengeTypes) {
        super(mob, true);
        this.noRevengeTypes = noRevengeTypes;
        this.setControls(EnumSet.of(Goal.Control.TARGET));
    }

    @Override
    public boolean canStart() {
        int i = this.mob.getLastAttackedTime();
        LivingEntity lv = this.mob.getAttacker();
        if (i == this.lastAttackedTime || lv == null) {
            return false;
        }
        if (lv.getType() == EntityType.PLAYER && this.mob.getWorld().getGameRules().getBoolean(GameRules.UNIVERSAL_ANGER)) {
            return false;
        }
        for (Class<?> class_ : this.noRevengeTypes) {
            if (!class_.isAssignableFrom(lv.getClass())) continue;
            return false;
        }
        return this.canTrack(lv, VALID_AVOIDABLES_PREDICATE);
    }

    public RevengeGoal setGroupRevenge(Class<?> ... noHelpTypes) {
        this.groupRevenge = true;
        this.noHelpTypes = noHelpTypes;
        return this;
    }

    @Override
    public void start() {
        this.mob.setTarget(this.mob.getAttacker());
        this.target = this.mob.getTarget();
        this.lastAttackedTime = this.mob.getLastAttackedTime();
        this.maxTimeWithoutVisibility = 300;
        if (this.groupRevenge) {
            this.callSameTypeForRevenge();
        }
        super.start();
    }

    protected void callSameTypeForRevenge() {
        double d = this.getFollowRange();
        Box lv = Box.from(this.mob.getPos()).expand(d, 10.0, d);
        List<Entity> list = this.mob.getWorld().getEntitiesByClass(this.mob.getClass(), lv, EntityPredicates.EXCEPT_SPECTATOR);
        for (MobEntity mobEntity : list) {
            if (this.mob == mobEntity || mobEntity.getTarget() != null || this.mob instanceof TameableEntity && ((TameableEntity)this.mob).getOwner() != ((TameableEntity)mobEntity).getOwner() || mobEntity.isTeammate(this.mob.getAttacker())) continue;
            if (this.noHelpTypes != null) {
                boolean bl = false;
                for (Class<?> class_ : this.noHelpTypes) {
                    if (mobEntity.getClass() != class_) continue;
                    bl = true;
                    break;
                }
                if (bl) continue;
            }
            this.setMobEntityTarget(mobEntity, this.mob.getAttacker());
        }
    }

    protected void setMobEntityTarget(MobEntity mob, LivingEntity target) {
        mob.setTarget(target);
    }
}

