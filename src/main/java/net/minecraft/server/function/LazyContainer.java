/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.server.function;

import com.mojang.serialization.Codec;
import java.util.Optional;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.function.CommandFunction;
import net.minecraft.server.function.CommandFunctionManager;
import net.minecraft.util.Identifier;

public class LazyContainer {
    public static final Codec<LazyContainer> CODEC = Identifier.CODEC.xmap(LazyContainer::new, LazyContainer::getId);
    private final Identifier id;
    private boolean initialized;
    private Optional<CommandFunction<ServerCommandSource>> function = Optional.empty();

    public LazyContainer(Identifier id) {
        this.id = id;
    }

    public Optional<CommandFunction<ServerCommandSource>> get(CommandFunctionManager commandFunctionManager) {
        if (!this.initialized) {
            this.function = commandFunctionManager.getFunction(this.id);
            this.initialized = true;
        }
        return this.function;
    }

    public Identifier getId() {
        return this.id;
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof LazyContainer)) return false;
        LazyContainer lv = (LazyContainer)o;
        if (!this.getId().equals(lv.getId())) return false;
        return true;
    }
}

