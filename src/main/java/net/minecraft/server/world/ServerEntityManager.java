/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.server.world;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Queues;
import com.google.common.collect.Sets;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMaps;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.entity.Entity;
import net.minecraft.server.world.ChunkLevelType;
import net.minecraft.util.CsvWriter;
import net.minecraft.util.annotation.Debug;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.world.entity.EntityChangeListener;
import net.minecraft.world.entity.EntityHandler;
import net.minecraft.world.entity.EntityIndex;
import net.minecraft.world.entity.EntityLike;
import net.minecraft.world.entity.EntityLookup;
import net.minecraft.world.entity.EntityTrackingSection;
import net.minecraft.world.entity.EntityTrackingStatus;
import net.minecraft.world.entity.SectionedEntityCache;
import net.minecraft.world.entity.SimpleEntityLookup;
import net.minecraft.world.storage.ChunkDataAccess;
import net.minecraft.world.storage.ChunkDataList;
import org.slf4j.Logger;

public class ServerEntityManager<T extends EntityLike>
implements AutoCloseable {
    static final Logger LOGGER = LogUtils.getLogger();
    final Set<UUID> entityUuids = Sets.newHashSet();
    final EntityHandler<T> handler;
    private final ChunkDataAccess<T> dataAccess;
    private final EntityIndex<T> index;
    final SectionedEntityCache<T> cache;
    private final EntityLookup<T> lookup;
    private final Long2ObjectMap<EntityTrackingStatus> trackingStatuses = new Long2ObjectOpenHashMap<EntityTrackingStatus>();
    private final Long2ObjectMap<Status> managedStatuses = new Long2ObjectOpenHashMap<Status>();
    private final LongSet pendingUnloads = new LongOpenHashSet();
    private final Queue<ChunkDataList<T>> loadingQueue = Queues.newConcurrentLinkedQueue();

    public ServerEntityManager(Class<T> entityClass, EntityHandler<T> handler, ChunkDataAccess<T> dataAccess) {
        this.index = new EntityIndex();
        this.cache = new SectionedEntityCache<T>(entityClass, this.trackingStatuses);
        this.trackingStatuses.defaultReturnValue(EntityTrackingStatus.HIDDEN);
        this.managedStatuses.defaultReturnValue(Status.FRESH);
        this.handler = handler;
        this.dataAccess = dataAccess;
        this.lookup = new SimpleEntityLookup<T>(this.index, this.cache);
    }

    void entityLeftSection(long sectionPos, EntityTrackingSection<T> section) {
        if (section.isEmpty()) {
            this.cache.removeSection(sectionPos);
        }
    }

    private boolean addEntityUuid(T entity) {
        if (!this.entityUuids.add(entity.getUuid())) {
            LOGGER.warn("UUID of added entity already exists: {}", (Object)entity);
            return false;
        }
        return true;
    }

    public boolean addEntity(T entity) {
        return this.addEntity(entity, false);
    }

    private boolean addEntity(T entity, boolean existing) {
        EntityTrackingStatus lv2;
        if (!this.addEntityUuid(entity)) {
            return false;
        }
        long l = ChunkSectionPos.toLong(entity.getBlockPos());
        EntityTrackingSection<T> lv = this.cache.getTrackingSection(l);
        lv.add(entity);
        entity.setChangeListener(new Listener(this, entity, l, lv));
        if (!existing) {
            this.handler.create(entity);
        }
        if ((lv2 = ServerEntityManager.getNeededLoadStatus(entity, lv.getStatus())).shouldTrack()) {
            this.startTracking(entity);
        }
        if (lv2.shouldTick()) {
            this.startTicking(entity);
        }
        return true;
    }

    static <T extends EntityLike> EntityTrackingStatus getNeededLoadStatus(T entity, EntityTrackingStatus current) {
        return entity.isPlayer() ? EntityTrackingStatus.TICKING : current;
    }

    public void loadEntities(Stream<T> entities) {
        entities.forEach(entity -> this.addEntity(entity, true));
    }

    public void addEntities(Stream<T> entities) {
        entities.forEach(entity -> this.addEntity(entity, false));
    }

    void startTicking(T entity) {
        this.handler.startTicking(entity);
    }

    void stopTicking(T entity) {
        this.handler.stopTicking(entity);
    }

    void startTracking(T entity) {
        this.index.add(entity);
        this.handler.startTracking(entity);
    }

    void stopTracking(T entity) {
        this.handler.stopTracking(entity);
        this.index.remove(entity);
    }

    public void updateTrackingStatus(ChunkPos chunkPos, ChunkLevelType levelType) {
        EntityTrackingStatus lv = EntityTrackingStatus.fromLevelType(levelType);
        this.updateTrackingStatus(chunkPos, lv);
    }

    public void updateTrackingStatus(ChunkPos chunkPos, EntityTrackingStatus trackingStatus) {
        long l = chunkPos.toLong();
        if (trackingStatus == EntityTrackingStatus.HIDDEN) {
            this.trackingStatuses.remove(l);
            this.pendingUnloads.add(l);
        } else {
            this.trackingStatuses.put(l, trackingStatus);
            this.pendingUnloads.remove(l);
            this.readIfFresh(l);
        }
        this.cache.getTrackingSections(l).forEach(group -> {
            EntityTrackingStatus lv = group.swapStatus(trackingStatus);
            boolean bl = lv.shouldTrack();
            boolean bl2 = trackingStatus.shouldTrack();
            boolean bl3 = lv.shouldTick();
            boolean bl4 = trackingStatus.shouldTick();
            if (bl3 && !bl4) {
                group.stream().filter(entity -> !entity.isPlayer()).forEach(this::stopTicking);
            }
            if (bl && !bl2) {
                group.stream().filter(entity -> !entity.isPlayer()).forEach(this::stopTracking);
            } else if (!bl && bl2) {
                group.stream().filter(entity -> !entity.isPlayer()).forEach(this::startTracking);
            }
            if (!bl3 && bl4) {
                group.stream().filter(entity -> !entity.isPlayer()).forEach(this::startTicking);
            }
        });
    }

    private void readIfFresh(long chunkPos) {
        Status lv = (Status)((Object)this.managedStatuses.get(chunkPos));
        if (lv == Status.FRESH) {
            this.scheduleRead(chunkPos);
        }
    }

    private boolean trySave(long chunkPos, Consumer<T> action) {
        Status lv = (Status)((Object)this.managedStatuses.get(chunkPos));
        if (lv == Status.PENDING) {
            return false;
        }
        List<T> list = this.cache.getTrackingSections(chunkPos).flatMap(section -> section.stream().filter(EntityLike::shouldSave)).collect(Collectors.toList());
        if (list.isEmpty()) {
            if (lv == Status.LOADED) {
                this.dataAccess.writeChunkData(new ChunkDataList(new ChunkPos(chunkPos), ImmutableList.of()));
            }
            return true;
        }
        if (lv == Status.FRESH) {
            this.scheduleRead(chunkPos);
            return false;
        }
        this.dataAccess.writeChunkData(new ChunkDataList(new ChunkPos(chunkPos), list));
        list.forEach(action);
        return true;
    }

    private void scheduleRead(long chunkPos) {
        this.managedStatuses.put(chunkPos, Status.PENDING);
        ChunkPos lv = new ChunkPos(chunkPos);
        ((CompletableFuture)this.dataAccess.readChunkData(lv).thenAccept(this.loadingQueue::add)).exceptionally(throwable -> {
            LOGGER.error("Failed to read chunk {}", (Object)lv, throwable);
            return null;
        });
    }

    private boolean unload(long chunkPos) {
        boolean bl = this.trySave(chunkPos, entity -> entity.streamPassengersAndSelf().forEach(this::unload));
        if (!bl) {
            return false;
        }
        this.managedStatuses.remove(chunkPos);
        return true;
    }

    private void unload(EntityLike entity) {
        entity.setRemoved(Entity.RemovalReason.UNLOADED_TO_CHUNK);
        entity.setChangeListener(EntityChangeListener.NONE);
    }

    private void unloadChunks() {
        this.pendingUnloads.removeIf(pos -> {
            if (this.trackingStatuses.get(pos) != EntityTrackingStatus.HIDDEN) {
                return true;
            }
            return this.unload(pos);
        });
    }

    private void loadChunks() {
        ChunkDataList<T> lv;
        while ((lv = this.loadingQueue.poll()) != null) {
            lv.stream().forEach(entity -> this.addEntity(entity, true));
            this.managedStatuses.put(lv.getChunkPos().toLong(), Status.LOADED);
        }
    }

    public void tick() {
        this.loadChunks();
        this.unloadChunks();
    }

    private LongSet getLoadedChunks() {
        LongSet longSet = this.cache.getChunkPositions();
        for (Long2ObjectMap.Entry entry : Long2ObjectMaps.fastIterable(this.managedStatuses)) {
            if (entry.getValue() != Status.LOADED) continue;
            longSet.add(entry.getLongKey());
        }
        return longSet;
    }

    public void save() {
        this.getLoadedChunks().forEach(pos -> {
            boolean bl;
            boolean bl2 = bl = this.trackingStatuses.get(pos) == EntityTrackingStatus.HIDDEN;
            if (bl) {
                this.unload(pos);
            } else {
                this.trySave(pos, entity -> {});
            }
        });
    }

    public void flush() {
        LongSet longSet = this.getLoadedChunks();
        while (!longSet.isEmpty()) {
            this.dataAccess.awaitAll(false);
            this.loadChunks();
            longSet.removeIf(pos -> {
                boolean bl = this.trackingStatuses.get(pos) == EntityTrackingStatus.HIDDEN;
                return bl ? this.unload(pos) : this.trySave(pos, entity -> {});
            });
        }
        this.dataAccess.awaitAll(true);
    }

    @Override
    public void close() throws IOException {
        this.flush();
        this.dataAccess.close();
    }

    public boolean has(UUID uuid) {
        return this.entityUuids.contains(uuid);
    }

    public EntityLookup<T> getLookup() {
        return this.lookup;
    }

    public boolean shouldTick(BlockPos pos) {
        return ((EntityTrackingStatus)((Object)this.trackingStatuses.get(ChunkPos.toLong(pos)))).shouldTick();
    }

    public boolean shouldTick(ChunkPos pos) {
        return ((EntityTrackingStatus)((Object)this.trackingStatuses.get(pos.toLong()))).shouldTick();
    }

    public boolean isLoaded(long chunkPos) {
        return this.managedStatuses.get(chunkPos) == Status.LOADED;
    }

    public void dump(Writer writer) throws IOException {
        CsvWriter lv = CsvWriter.makeHeader().addColumn("x").addColumn("y").addColumn("z").addColumn("visibility").addColumn("load_status").addColumn("entity_count").startBody(writer);
        this.cache.getChunkPositions().forEach(chunkPos -> {
            Status lv = (Status)((Object)((Object)this.managedStatuses.get(chunkPos)));
            this.cache.getSections(chunkPos).forEach(sectionPos -> {
                EntityTrackingSection<T> lv = this.cache.findTrackingSection(sectionPos);
                if (lv != null) {
                    try {
                        lv.printRow(new Object[]{ChunkSectionPos.unpackX(sectionPos), ChunkSectionPos.unpackY(sectionPos), ChunkSectionPos.unpackZ(sectionPos), lv.getStatus(), lv, lv.size()});
                    } catch (IOException iOException) {
                        throw new UncheckedIOException(iOException);
                    }
                }
            });
        });
    }

    @Debug
    public String getDebugString() {
        return this.entityUuids.size() + "," + this.index.size() + "," + this.cache.sectionCount() + "," + this.managedStatuses.size() + "," + this.trackingStatuses.size() + "," + this.loadingQueue.size() + "," + this.pendingUnloads.size();
    }

    @Debug
    public int getIndexSize() {
        return this.index.size();
    }

    static enum Status {
        FRESH,
        PENDING,
        LOADED;

    }

    static class Listener
    implements EntityChangeListener {
        private final T entity;
        private long sectionPos;
        private EntityTrackingSection<T> section;
        final /* synthetic */ ServerEntityManager manager;

        /*
         * WARNING - Possible parameter corruption
         * WARNING - void declaration
         */
        Listener(T entity, long sectionPos, EntityTrackingSection<T> section) {
            void var3_3;
            this.manager = arg;
            this.entity = entity;
            this.sectionPos = var3_3;
            this.section = section;
        }

        @Override
        public void updateEntityPosition() {
            BlockPos lv = this.entity.getBlockPos();
            long l = ChunkSectionPos.toLong(lv);
            if (l != this.sectionPos) {
                EntityTrackingStatus lv2 = this.section.getStatus();
                if (!this.section.remove(this.entity)) {
                    LOGGER.warn("Entity {} wasn't found in section {} (moving to {})", this.entity, ChunkSectionPos.from(this.sectionPos), l);
                }
                this.manager.entityLeftSection(this.sectionPos, this.section);
                EntityTrackingSection lv3 = this.manager.cache.getTrackingSection(l);
                lv3.add(this.entity);
                this.section = lv3;
                this.sectionPos = l;
                this.updateLoadStatus(lv2, lv3.getStatus());
            }
        }

        private void updateLoadStatus(EntityTrackingStatus oldStatus, EntityTrackingStatus newStatus) {
            EntityTrackingStatus lv2;
            EntityTrackingStatus lv = ServerEntityManager.getNeededLoadStatus(this.entity, oldStatus);
            if (lv == (lv2 = ServerEntityManager.getNeededLoadStatus(this.entity, newStatus))) {
                if (lv2.shouldTrack()) {
                    this.manager.handler.updateLoadStatus(this.entity);
                }
                return;
            }
            boolean bl = lv.shouldTrack();
            boolean bl2 = lv2.shouldTrack();
            if (bl && !bl2) {
                this.manager.stopTracking(this.entity);
            } else if (!bl && bl2) {
                this.manager.startTracking(this.entity);
            }
            boolean bl3 = lv.shouldTick();
            boolean bl4 = lv2.shouldTick();
            if (bl3 && !bl4) {
                this.manager.stopTicking(this.entity);
            } else if (!bl3 && bl4) {
                this.manager.startTicking(this.entity);
            }
            if (bl2) {
                this.manager.handler.updateLoadStatus(this.entity);
            }
        }

        @Override
        public void remove(Entity.RemovalReason reason) {
            EntityTrackingStatus lv;
            if (!this.section.remove(this.entity)) {
                LOGGER.warn("Entity {} wasn't found in section {} (destroying due to {})", new Object[]{this.entity, ChunkSectionPos.from(this.sectionPos), reason});
            }
            if ((lv = ServerEntityManager.getNeededLoadStatus(this.entity, this.section.getStatus())).shouldTick()) {
                this.manager.stopTicking(this.entity);
            }
            if (lv.shouldTrack()) {
                this.manager.stopTracking(this.entity);
            }
            if (reason.shouldDestroy()) {
                this.manager.handler.destroy(this.entity);
            }
            this.manager.entityUuids.remove(this.entity.getUuid());
            this.entity.setChangeListener(NONE);
            this.manager.entityLeftSection(this.sectionPos, this.section);
        }
    }
}

