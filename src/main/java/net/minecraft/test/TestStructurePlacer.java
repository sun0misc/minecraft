/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.test;

import java.util.Optional;
import net.minecraft.test.GameTestState;
import net.minecraft.test.StructureTestUtil;
import net.minecraft.test.TestRunContext;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;

public class TestStructurePlacer
implements TestRunContext.TestStructureSpawner {
    private static final int MARGIN_X = 5;
    private static final int MARGIN_Z = 6;
    private final int testsPerRow;
    private int testsInCurrentRow;
    private Box box;
    private final BlockPos.Mutable mutablePos;
    private final BlockPos origin;

    public TestStructurePlacer(BlockPos origin, int testsPerRow) {
        this.testsPerRow = testsPerRow;
        this.mutablePos = origin.mutableCopy();
        this.box = new Box(this.mutablePos);
        this.origin = origin;
    }

    @Override
    public Optional<GameTestState> spawnStructure(GameTestState arg) {
        BlockPos lv = new BlockPos(this.mutablePos);
        arg.setBoxMinPos(lv);
        arg.init();
        Box lv2 = StructureTestUtil.getStructureBoundingBox(arg.getStructureBlockBlockEntity());
        this.box = this.box.union(lv2);
        this.mutablePos.move((int)lv2.getLengthX() + 5, 0, 0);
        if (++this.testsInCurrentRow >= this.testsPerRow) {
            this.testsInCurrentRow = 0;
            this.mutablePos.move(0, 0, (int)this.box.getLengthZ() + 6);
            this.mutablePos.setX(this.origin.getX());
            this.box = new Box(this.mutablePos);
        }
        return Optional.of(arg);
    }
}

