/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.search;

import com.google.common.collect.AbstractIterator;
import com.google.common.collect.Iterators;
import com.google.common.collect.PeekingIterator;
import java.util.Comparator;
import java.util.Iterator;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(value=EnvType.CLIENT)
public class IdentifierSearchableIterator<T>
extends AbstractIterator<T> {
    private final PeekingIterator<T> namespacesIterator;
    private final PeekingIterator<T> pathsIterator;
    private final Comparator<T> lastIndexComparator;

    public IdentifierSearchableIterator(Iterator<T> namespacesIterator, Iterator<T> pathsIterator, Comparator<T> lastIndexComparator) {
        this.namespacesIterator = Iterators.peekingIterator(namespacesIterator);
        this.pathsIterator = Iterators.peekingIterator(pathsIterator);
        this.lastIndexComparator = lastIndexComparator;
    }

    @Override
    protected T computeNext() {
        while (this.namespacesIterator.hasNext() && this.pathsIterator.hasNext()) {
            int i = this.lastIndexComparator.compare(this.namespacesIterator.peek(), this.pathsIterator.peek());
            if (i == 0) {
                this.pathsIterator.next();
                return this.namespacesIterator.next();
            }
            if (i < 0) {
                this.namespacesIterator.next();
                continue;
            }
            this.pathsIterator.next();
        }
        return this.endOfData();
    }
}

