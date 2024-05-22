/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft;

import java.nio.file.Path;
import java.util.Optional;
import net.minecraft.text.Text;

public record class_9812(Text reason, Optional<Path> report, Optional<String> bugReportLink) {
    public class_9812(Text arg) {
        this(arg, Optional.empty(), Optional.empty());
    }
}

