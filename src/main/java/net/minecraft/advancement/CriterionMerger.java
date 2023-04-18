package net.minecraft.advancement;

import java.util.Collection;
import java.util.Iterator;

public interface CriterionMerger {
   CriterionMerger AND = (criteriaNames) -> {
      String[][] strings = new String[criteriaNames.size()][];
      int i = 0;

      String string;
      for(Iterator var3 = criteriaNames.iterator(); var3.hasNext(); strings[i++] = new String[]{string}) {
         string = (String)var3.next();
      }

      return strings;
   };
   CriterionMerger OR = (criteriaNames) -> {
      return new String[][]{(String[])criteriaNames.toArray(new String[0])};
   };

   String[][] createRequirements(Collection criteriaNames);
}
