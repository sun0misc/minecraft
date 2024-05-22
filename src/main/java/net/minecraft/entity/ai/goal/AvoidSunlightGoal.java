/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.entity.ai.goal;

import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.ai.NavigationConditions;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.ai.pathing.MobNavigation;
import net.minecraft.entity.mob.PathAwareEntity;

public class AvoidSunlightGoal
extends Goal {
    private final PathAwareEntity mob;

    public AvoidSunlightGoal(PathAwareEntity mob) {
        this.mob = mob;
    }

    @Override
    public boolean canStart() {
        return this.mob.getWorld().isDay() && this.mob.getEquippedStack(EquipmentSlot.HEAD).isEmpty() && NavigationConditions.hasMobNavigation(this.mob);
    }

    @Override
    public void start() {
        ((MobNavigation)this.mob.getNavigation()).setAvoidSunlight(true);
    }

    @Override
    public void stop() {
        if (NavigationConditions.hasMobNavigation(this.mob)) {
            ((MobNavigation)this.mob.getNavigation()).setAvoidSunlight(false);
        }
    }
}

