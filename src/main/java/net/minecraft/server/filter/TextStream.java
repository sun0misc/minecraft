/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.server.filter;

import com.google.common.collect.ImmutableList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import net.minecraft.server.filter.FilteredMessage;

public interface TextStream {
    public static final TextStream UNFILTERED = new TextStream(){

        @Override
        public void onConnect() {
        }

        @Override
        public void onDisconnect() {
        }

        @Override
        public CompletableFuture<FilteredMessage> filterText(String text) {
            return CompletableFuture.completedFuture(FilteredMessage.permitted(text));
        }

        @Override
        public CompletableFuture<List<FilteredMessage>> filterTexts(List<String> texts) {
            return CompletableFuture.completedFuture((List)texts.stream().map(FilteredMessage::permitted).collect(ImmutableList.toImmutableList()));
        }
    };

    public void onConnect();

    public void onDisconnect();

    public CompletableFuture<FilteredMessage> filterText(String var1);

    public CompletableFuture<List<FilteredMessage>> filterTexts(List<String> var1);
}

