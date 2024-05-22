/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package com.mojang.blaze3d.systems;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.systems.RenderCall;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(value=EnvType.CLIENT)
public class RenderCallStorage {
    private final List<ConcurrentLinkedQueue<RenderCall>> recordingQueues = ImmutableList.of(new ConcurrentLinkedQueue(), new ConcurrentLinkedQueue(), new ConcurrentLinkedQueue(), new ConcurrentLinkedQueue());
    private volatile boolean recording;
    private volatile int recordingIndex = this.processingIndex = this.lastProcessedIndex + 1;
    private volatile boolean processing;
    private volatile int processingIndex;
    private volatile int lastProcessedIndex;

    public boolean canRecord() {
        return !this.recording && this.recordingIndex == this.processingIndex;
    }

    public boolean startRecording() {
        if (this.recording) {
            throw new RuntimeException("ALREADY RECORDING !!!");
        }
        if (this.canRecord()) {
            this.recordingIndex = (this.processingIndex + 1) % this.recordingQueues.size();
            this.recording = true;
            return true;
        }
        return false;
    }

    public void record(RenderCall call) {
        if (!this.recording) {
            throw new RuntimeException("NOT RECORDING !!!");
        }
        ConcurrentLinkedQueue<RenderCall> concurrentLinkedQueue = this.getRecordingQueue();
        concurrentLinkedQueue.add(call);
    }

    public void stopRecording() {
        if (!this.recording) {
            throw new RuntimeException("NOT RECORDING !!!");
        }
        this.recording = false;
    }

    public boolean canProcess() {
        return !this.processing && this.recordingIndex != this.processingIndex;
    }

    public boolean startProcessing() {
        if (this.processing) {
            throw new RuntimeException("ALREADY PROCESSING !!!");
        }
        if (this.canProcess()) {
            this.processing = true;
            return true;
        }
        return false;
    }

    public void process() {
        if (!this.processing) {
            throw new RuntimeException("NOT PROCESSING !!!");
        }
    }

    public void stopProcessing() {
        if (!this.processing) {
            throw new RuntimeException("NOT PROCESSING !!!");
        }
        this.processing = false;
        this.lastProcessedIndex = this.processingIndex;
        this.processingIndex = this.recordingIndex;
    }

    public ConcurrentLinkedQueue<RenderCall> getLastProcessedQueue() {
        return this.recordingQueues.get(this.lastProcessedIndex);
    }

    public ConcurrentLinkedQueue<RenderCall> getRecordingQueue() {
        return this.recordingQueues.get(this.recordingIndex);
    }

    public ConcurrentLinkedQueue<RenderCall> getProcessingQueue() {
        return this.recordingQueues.get(this.processingIndex);
    }
}

