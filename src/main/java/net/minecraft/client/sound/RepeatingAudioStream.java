/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.sound;

import java.io.BufferedInputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import javax.sound.sampled.AudioFormat;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.sound.AudioStream;

@Environment(value=EnvType.CLIENT)
public class RepeatingAudioStream
implements AudioStream {
    private final DelegateFactory delegateFactory;
    private AudioStream delegate;
    private final BufferedInputStream inputStream;

    public RepeatingAudioStream(DelegateFactory delegateFactory, InputStream inputStream) throws IOException {
        this.delegateFactory = delegateFactory;
        this.inputStream = new BufferedInputStream(inputStream);
        this.inputStream.mark(Integer.MAX_VALUE);
        this.delegate = delegateFactory.create(new ReusableInputStream(this.inputStream));
    }

    @Override
    public AudioFormat getFormat() {
        return this.delegate.getFormat();
    }

    @Override
    public ByteBuffer read(int size) throws IOException {
        ByteBuffer byteBuffer = this.delegate.read(size);
        if (!byteBuffer.hasRemaining()) {
            this.delegate.close();
            this.inputStream.reset();
            this.delegate = this.delegateFactory.create(new ReusableInputStream(this.inputStream));
            byteBuffer = this.delegate.read(size);
        }
        return byteBuffer;
    }

    @Override
    public void close() throws IOException {
        this.delegate.close();
        this.inputStream.close();
    }

    @FunctionalInterface
    @Environment(value=EnvType.CLIENT)
    public static interface DelegateFactory {
        public AudioStream create(InputStream var1) throws IOException;
    }

    @Environment(value=EnvType.CLIENT)
    static class ReusableInputStream
    extends FilterInputStream {
        ReusableInputStream(InputStream stream) {
            super(stream);
        }

        @Override
        public void close() {
        }
    }
}

