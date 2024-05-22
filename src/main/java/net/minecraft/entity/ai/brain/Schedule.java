/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.entity.ai.brain;

import com.google.common.collect.Maps;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import net.minecraft.entity.ai.brain.Activity;
import net.minecraft.entity.ai.brain.ScheduleBuilder;
import net.minecraft.entity.ai.brain.ScheduleRule;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

public class Schedule {
    public static final int WORK_TIME = 2000;
    public static final int field_30693 = 7000;
    public static final Schedule EMPTY = Schedule.register("empty").withActivity(0, Activity.IDLE).build();
    public static final Schedule SIMPLE = Schedule.register("simple").withActivity(5000, Activity.WORK).withActivity(11000, Activity.REST).build();
    public static final Schedule VILLAGER_BABY = Schedule.register("villager_baby").withActivity(10, Activity.IDLE).withActivity(3000, Activity.PLAY).withActivity(6000, Activity.IDLE).withActivity(10000, Activity.PLAY).withActivity(12000, Activity.REST).build();
    public static final Schedule VILLAGER_DEFAULT = Schedule.register("villager_default").withActivity(10, Activity.IDLE).withActivity(2000, Activity.WORK).withActivity(9000, Activity.MEET).withActivity(11000, Activity.IDLE).withActivity(12000, Activity.REST).build();
    private final Map<Activity, ScheduleRule> scheduleRules = Maps.newHashMap();

    protected static ScheduleBuilder register(String id) {
        Schedule lv = Registry.register(Registries.SCHEDULE, id, new Schedule());
        return new ScheduleBuilder(lv);
    }

    protected void addActivity(Activity activity) {
        if (!this.scheduleRules.containsKey(activity)) {
            this.scheduleRules.put(activity, new ScheduleRule());
        }
    }

    protected ScheduleRule getRule(Activity activity) {
        return this.scheduleRules.get(activity);
    }

    protected List<ScheduleRule> getOtherRules(Activity activity) {
        return this.scheduleRules.entrySet().stream().filter(rule -> rule.getKey() != activity).map(Map.Entry::getValue).collect(Collectors.toList());
    }

    public Activity getActivityForTime(int time) {
        return this.scheduleRules.entrySet().stream().max(Comparator.comparingDouble(rule -> ((ScheduleRule)rule.getValue()).getPriority(time))).map(Map.Entry::getKey).orElse(Activity.IDLE);
    }
}

