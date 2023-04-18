package net.minecraft.data.report;

import com.google.common.collect.UnmodifiableIterator;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.concurrent.CompletableFuture;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.data.DataOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.DataWriter;
import net.minecraft.registry.Registries;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.Property;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;

public class BlockListProvider implements DataProvider {
   private final DataOutput output;

   public BlockListProvider(DataOutput output) {
      this.output = output;
   }

   public CompletableFuture run(DataWriter writer) {
      JsonObject jsonObject = new JsonObject();
      Iterator var3 = Registries.BLOCK.iterator();

      while(var3.hasNext()) {
         Block lv = (Block)var3.next();
         Identifier lv2 = Registries.BLOCK.getId(lv);
         JsonObject jsonObject2 = new JsonObject();
         StateManager lv3 = lv.getStateManager();
         if (!lv3.getProperties().isEmpty()) {
            JsonObject jsonObject3 = new JsonObject();
            Iterator var9 = lv3.getProperties().iterator();

            while(true) {
               if (!var9.hasNext()) {
                  jsonObject2.add("properties", jsonObject3);
                  break;
               }

               Property lv4 = (Property)var9.next();
               JsonArray jsonArray = new JsonArray();
               Iterator var12 = lv4.getValues().iterator();

               while(var12.hasNext()) {
                  Comparable comparable = (Comparable)var12.next();
                  jsonArray.add(Util.getValueAsString(lv4, comparable));
               }

               jsonObject3.add(lv4.getName(), jsonArray);
            }
         }

         JsonArray jsonArray2 = new JsonArray();

         JsonObject jsonObject4;
         for(UnmodifiableIterator var17 = lv3.getStates().iterator(); var17.hasNext(); jsonArray2.add(jsonObject4)) {
            BlockState lv5 = (BlockState)var17.next();
            jsonObject4 = new JsonObject();
            JsonObject jsonObject5 = new JsonObject();
            Iterator var21 = lv3.getProperties().iterator();

            while(var21.hasNext()) {
               Property lv6 = (Property)var21.next();
               jsonObject5.addProperty(lv6.getName(), Util.getValueAsString(lv6, lv5.get(lv6)));
            }

            if (jsonObject5.size() > 0) {
               jsonObject4.add("properties", jsonObject5);
            }

            jsonObject4.addProperty("id", Block.getRawIdFromState(lv5));
            if (lv5 == lv.getDefaultState()) {
               jsonObject4.addProperty("default", true);
            }
         }

         jsonObject2.add("states", jsonArray2);
         jsonObject.add(lv2.toString(), jsonObject2);
      }

      Path path = this.output.resolvePath(DataOutput.OutputType.REPORTS).resolve("blocks.json");
      return DataProvider.writeToPath(writer, jsonObject, path);
   }

   public final String getName() {
      return "Block List";
   }
}
