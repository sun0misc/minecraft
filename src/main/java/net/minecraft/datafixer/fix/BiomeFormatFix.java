/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.datafixer.fix;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.OpticFinder;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import java.util.Arrays;
import java.util.Optional;
import java.util.stream.IntStream;
import net.minecraft.datafixer.TypeReferences;

public class BiomeFormatFix
extends DataFix {
    public BiomeFormatFix(Schema schema, boolean bl) {
        super(schema, bl);
    }

    @Override
    protected TypeRewriteRule makeRule() {
        Type<?> type = this.getInputSchema().getType(TypeReferences.CHUNK);
        OpticFinder<?> opticFinder = type.findField("Level");
        return this.fixTypeEverywhereTyped("Leaves fix", type, typed2 -> typed2.updateTyped(opticFinder, typed -> typed.update(DSL.remainderFinder(), dynamic -> {
            int i;
            Optional<IntStream> optional = dynamic.get("Biomes").asIntStreamOpt().result();
            if (optional.isEmpty()) {
                return dynamic;
            }
            int[] is = optional.get().toArray();
            if (is.length != 256) {
                return dynamic;
            }
            int[] js = new int[1024];
            for (i = 0; i < 4; ++i) {
                for (int j = 0; j < 4; ++j) {
                    int k = (j << 2) + 2;
                    int l = (i << 2) + 2;
                    int m = l << 4 | k;
                    js[i << 2 | j] = is[m];
                }
            }
            for (i = 1; i < 64; ++i) {
                System.arraycopy(js, 0, js, i * 16, 16);
            }
            return dynamic.set("Biomes", dynamic.createIntList(Arrays.stream(js)));
        })));
    }
}

