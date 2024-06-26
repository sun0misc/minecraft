/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.entity.ai.brain;

import com.google.common.collect.Lists;
import java.util.List;
import java.util.stream.Collectors;
import net.minecraft.entity.ai.brain.Activity;
import net.minecraft.entity.ai.brain.Schedule;

public class ScheduleBuilder {
    private final Schedule schedule;
    private final List<ActivityEntry> activities = Lists.newArrayList();

    public ScheduleBuilder(Schedule schedule) {
        this.schedule = schedule;
    }

    public ScheduleBuilder withActivity(int startTime, Activity activity) {
        this.activities.add(new ActivityEntry(startTime, activity));
        return this;
    }

    public Schedule build() {
        this.activities.stream().map(ActivityEntry::getActivity).collect(Collectors.toSet()).forEach(this.schedule::addActivity);
        this.activities.forEach(activity -> {
            Activity lv = activity.getActivity();
            this.schedule.getOtherRules(lv).forEach(rule -> rule.add(activity.getStartTime(), 0.0f));
            this.schedule.getRule(lv).add(activity.getStartTime(), 1.0f);
        });
        return this.schedule;
    }

    static class ActivityEntry {
        private final int startTime;
        private final Activity activity;

        public ActivityEntry(int startTime, Activity activity) {
            this.startTime = startTime;
            this.activity = activity;
        }

        public int getStartTime() {
            return this.startTime;
        }

        public Activity getActivity() {
            return this.activity;
        }
    }
}

