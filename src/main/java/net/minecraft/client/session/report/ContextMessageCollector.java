/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.session.report;

import it.unimi.dsi.fastutil.ints.IntCollection;
import it.unimi.dsi.fastutil.ints.IntRBTreeSet;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.session.report.log.ChatLog;
import net.minecraft.client.session.report.log.ChatLogEntry;
import net.minecraft.client.session.report.log.ReceivedMessage;
import net.minecraft.network.message.MessageSignatureData;
import net.minecraft.network.message.SignedMessage;

@Environment(value=EnvType.CLIENT)
public class ContextMessageCollector {
    final int leadingContextMessageCount;
    private final List<ContextMessage> contextMessages = new ArrayList<ContextMessage>();

    public ContextMessageCollector(int leadingContextMessageCount) {
        this.leadingContextMessageCount = leadingContextMessageCount;
    }

    public void add(ChatLog log, IntCollection selections, IndexedMessageConsumer consumer) {
        IntRBTreeSet intSortedSet = new IntRBTreeSet(selections);
        for (int i = intSortedSet.lastInt(); i >= log.getMinIndex() && (this.hasContextMessage() || !intSortedSet.isEmpty()); --i) {
            ChatLogEntry chatLogEntry = log.get(i);
            if (!(chatLogEntry instanceof ReceivedMessage.ChatMessage)) continue;
            ReceivedMessage.ChatMessage lv = (ReceivedMessage.ChatMessage)chatLogEntry;
            boolean bl = this.tryLink(lv.message());
            if (intSortedSet.remove(i)) {
                this.add(lv.message());
                consumer.accept(i, lv);
                continue;
            }
            if (!bl) continue;
            consumer.accept(i, lv);
        }
    }

    public void add(SignedMessage message) {
        this.contextMessages.add(new ContextMessage(message));
    }

    public boolean tryLink(SignedMessage message) {
        boolean bl = false;
        Iterator<ContextMessage> iterator = this.contextMessages.iterator();
        while (iterator.hasNext()) {
            ContextMessage lv = iterator.next();
            if (!lv.linkTo(message)) continue;
            bl = true;
            if (!lv.isInvalid()) continue;
            iterator.remove();
        }
        return bl;
    }

    public boolean hasContextMessage() {
        return !this.contextMessages.isEmpty();
    }

    @Environment(value=EnvType.CLIENT)
    public static interface IndexedMessageConsumer {
        public void accept(int var1, ReceivedMessage.ChatMessage var2);
    }

    @Environment(value=EnvType.CLIENT)
    class ContextMessage {
        private final Set<MessageSignatureData> lastSeenEntries;
        private SignedMessage message;
        private boolean linkSuccessful = true;
        private int count;

        ContextMessage(SignedMessage message) {
            this.lastSeenEntries = new ObjectOpenHashSet<MessageSignatureData>(message.signedBody().lastSeenMessages().entries());
            this.message = message;
        }

        boolean linkTo(SignedMessage message) {
            if (message.equals(this.message)) {
                return false;
            }
            boolean bl = this.lastSeenEntries.remove(message.signature());
            if (this.linkSuccessful && this.message.getSender().equals(message.getSender())) {
                if (this.message.link().linksTo(message.link())) {
                    bl = true;
                    this.message = message;
                } else {
                    this.linkSuccessful = false;
                }
            }
            if (bl) {
                ++this.count;
            }
            return bl;
        }

        boolean isInvalid() {
            return this.count >= ContextMessageCollector.this.leadingContextMessageCount || !this.linkSuccessful && this.lastSeenEntries.isEmpty();
        }
    }
}

