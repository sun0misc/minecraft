package net.minecraft.datafixer.fix;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFixUtils;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Dynamic;
import java.util.Optional;
import net.minecraft.datafixer.TypeReferences;

public class EntityPaintingFieldsRenameFix extends ChoiceFix {
   public EntityPaintingFieldsRenameFix(Schema schema) {
      super(schema, false, "EntityPaintingFieldsRenameFix", TypeReferences.ENTITY, "minecraft:painting");
   }

   public Dynamic rename(Dynamic dynamic) {
      return this.rename(this.rename(dynamic, "Motive", "variant"), "Facing", "facing");
   }

   private Dynamic rename(Dynamic dynamic, String oldKey, String newKey) {
      Optional optional = dynamic.get(oldKey).result();
      Optional optional2 = optional.map((value) -> {
         return dynamic.remove(oldKey).set(newKey, value);
      });
      return (Dynamic)DataFixUtils.orElse(optional2, dynamic);
   }

   protected Typed transform(Typed inputType) {
      return inputType.update(DSL.remainderFinder(), this::rename);
   }
}
