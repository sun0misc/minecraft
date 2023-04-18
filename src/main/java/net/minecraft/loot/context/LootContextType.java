package net.minecraft.loot.context;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import java.util.Iterator;
import java.util.Set;
import net.minecraft.loot.LootTableReporter;

public class LootContextType {
   private final Set required;
   private final Set allowed;

   LootContextType(Set required, Set allowed) {
      this.required = ImmutableSet.copyOf(required);
      this.allowed = ImmutableSet.copyOf(Sets.union(required, allowed));
   }

   public boolean isAllowed(LootContextParameter parameter) {
      return this.allowed.contains(parameter);
   }

   public Set getRequired() {
      return this.required;
   }

   public Set getAllowed() {
      return this.allowed;
   }

   public String toString() {
      Joiner var10000 = Joiner.on(", ");
      Iterator var10001 = this.allowed.stream().map((parameter) -> {
         String var10000 = this.required.contains(parameter) ? "!" : "";
         return var10000 + parameter.getId();
      }).iterator();
      return "[" + var10000.join(var10001) + "]";
   }

   public void validate(LootTableReporter reporter, LootContextAware parameterConsumer) {
      Set set = parameterConsumer.getRequiredParameters();
      Set set2 = Sets.difference(set, this.allowed);
      if (!set2.isEmpty()) {
         reporter.report("Parameters " + set2 + " are not provided in this context");
      }

   }

   public static Builder create() {
      return new Builder();
   }

   public static class Builder {
      private final Set required = Sets.newIdentityHashSet();
      private final Set allowed = Sets.newIdentityHashSet();

      public Builder require(LootContextParameter parameter) {
         if (this.allowed.contains(parameter)) {
            throw new IllegalArgumentException("Parameter " + parameter.getId() + " is already optional");
         } else {
            this.required.add(parameter);
            return this;
         }
      }

      public Builder allow(LootContextParameter parameter) {
         if (this.required.contains(parameter)) {
            throw new IllegalArgumentException("Parameter " + parameter.getId() + " is already required");
         } else {
            this.allowed.add(parameter);
            return this;
         }
      }

      public LootContextType build() {
         return new LootContextType(this.required, this.allowed);
      }
   }
}
