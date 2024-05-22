/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.command;

import net.minecraft.command.CommandAction;
import net.minecraft.command.CommandExecutionContext;
import net.minecraft.command.Frame;
import net.minecraft.server.command.AbstractServerCommandSource;

public class FallthroughCommandAction<T extends AbstractServerCommandSource<T>>
implements CommandAction<T> {
    private static final FallthroughCommandAction<? extends AbstractServerCommandSource<?>> INSTANCE = new FallthroughCommandAction();

    public static <T extends AbstractServerCommandSource<T>> CommandAction<T> getInstance() {
        return INSTANCE;
    }

    @Override
    public void execute(CommandExecutionContext<T> arg, Frame arg2) {
        arg2.fail();
        arg2.doReturn();
    }
}

