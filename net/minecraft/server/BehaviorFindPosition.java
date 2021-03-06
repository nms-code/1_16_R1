package net.minecraft.server;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import java.util.Iterator;
import java.util.Random;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class BehaviorFindPosition extends Behavior<EntityCreature> {

    private final VillagePlaceType b;
    private final MemoryModuleType<GlobalPos> c;
    private final boolean d;
    private long e;
    private final Long2ObjectMap<BehaviorFindPosition.a> f;

    public BehaviorFindPosition(VillagePlaceType villageplacetype, MemoryModuleType<GlobalPos> memorymoduletype, MemoryModuleType<GlobalPos> memorymoduletype1, boolean flag) {
        super(a(memorymoduletype, memorymoduletype1));
        this.f = new Long2ObjectOpenHashMap();
        this.b = villageplacetype;
        this.c = memorymoduletype1;
        this.d = flag;
    }

    public BehaviorFindPosition(VillagePlaceType villageplacetype, MemoryModuleType<GlobalPos> memorymoduletype, boolean flag) {
        this(villageplacetype, memorymoduletype, memorymoduletype, flag);
    }

    private static ImmutableMap<MemoryModuleType<?>, MemoryStatus> a(MemoryModuleType<GlobalPos> memorymoduletype, MemoryModuleType<GlobalPos> memorymoduletype1) {
        Builder<MemoryModuleType<?>, MemoryStatus> builder = ImmutableMap.builder();

        builder.put(memorymoduletype, MemoryStatus.VALUE_ABSENT);
        if (memorymoduletype1 != memorymoduletype) {
            builder.put(memorymoduletype1, MemoryStatus.VALUE_ABSENT);
        }

        return builder.build();
    }

    protected boolean a(WorldServer worldserver, EntityCreature entitycreature) {
        if (this.d && entitycreature.isBaby()) {
            return false;
        } else if (this.e == 0L) {
            this.e = entitycreature.world.getTime() + (long) worldserver.random.nextInt(20);
            return false;
        } else {
            return worldserver.getTime() >= this.e;
        }
    }

    protected void a(WorldServer worldserver, EntityCreature entitycreature, long i) {
        this.e = i + 20L + (long) worldserver.getRandom().nextInt(20);
        VillagePlace villageplace = worldserver.x();

        this.f.long2ObjectEntrySet().removeIf((entry) -> {
            return !((BehaviorFindPosition.a) entry.getValue()).b(i);
        });
        Predicate<BlockPosition> predicate = (blockposition) -> {
            BehaviorFindPosition.a behaviorfindposition_a = (BehaviorFindPosition.a) this.f.get(blockposition.asLong());

            if (behaviorfindposition_a == null) {
                return true;
            } else if (!behaviorfindposition_a.c(i)) {
                return false;
            } else {
                behaviorfindposition_a.a(i);
                return true;
            }
        };
        Set<BlockPosition> set = (Set) villageplace.a(this.b.c(), predicate, entitycreature.getChunkCoordinates(), 48, VillagePlace.Occupancy.HAS_SPACE).limit(5L).collect(Collectors.toSet());
        PathEntity pathentity = entitycreature.getNavigation().a(set, this.b.d());

        if (pathentity != null && pathentity.i()) {
            BlockPosition blockposition = pathentity.m();

            villageplace.c(blockposition).ifPresent((villageplacetype) -> {
                villageplace.a(this.b.c(), (blockposition1) -> {
                    return blockposition1.equals(blockposition);
                }, blockposition, 1);
                entitycreature.getBehaviorController().setMemory(this.c, (Object) GlobalPos.create(worldserver.getDimensionKey(), blockposition));
                this.f.clear();
                PacketDebug.c(worldserver, blockposition);
            });
        } else {
            Iterator iterator = set.iterator();

            while (iterator.hasNext()) {
                BlockPosition blockposition1 = (BlockPosition) iterator.next();

                this.f.computeIfAbsent(blockposition1.asLong(), (j) -> {
                    return new BehaviorFindPosition.a(entitycreature.world.random, i);
                });
            }
        }

    }

    static class a {

        private final Random a;
        private long b;
        private long c;
        private int d;

        a(Random random, long i) {
            this.a = random;
            this.a(i);
        }

        public void a(long i) {
            this.b = i;
            int j = this.d + this.a.nextInt(40) + 40;

            this.d = Math.min(j, 400);
            this.c = i + (long) this.d;
        }

        public boolean b(long i) {
            return i - this.b < 400L;
        }

        public boolean c(long i) {
            return i >= this.c;
        }

        public String toString() {
            return "RetryMarker{, previousAttemptAt=" + this.b + ", nextScheduledAttemptAt=" + this.c + ", currentDelay=" + this.d + '}';
        }
    }
}
