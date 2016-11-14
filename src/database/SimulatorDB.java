package database;

import benchmark.KVDB;
import benchmark.Status;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.locks.LockSupport;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import static java.util.concurrent.TimeUnit.MICROSECONDS;

public class SimulatorDB implements KVDB<Long, byte[]> {

    private Map<Long, byte[]> database = new HashMap<>();

    private static ReadWriteLock lock = new ReentrantReadWriteLock();
    private static long[] delays = {200, 1000, 10000, 50000, 200000};
    private static Random random = ThreadLocalRandom.current();
    //private Generator generator;

    private void delay() {
        double p = Math.random();
        int mod;
        if (p < 0.9) {
            mod = 0;
        } else if (p < 0.99) {
            mod = 1;
        } else if (p < 0.9999) {
            mod = 2;
        } else {
            mod = 3;
        }
        final long baseDelayNs = MICROSECONDS.toNanos(delays[mod]);
        final int delayRangeNs = (int) (MICROSECONDS.toNanos(delays[mod+1]) - baseDelayNs);
        final long delayNs = baseDelayNs + random.nextInt(delayRangeNs);
        long now = System.nanoTime();
        final long deadline = now + delayNs;
        do {
            LockSupport.parkNanos(deadline - now);
        } while ((now = System.nanoTime()) < deadline && !Thread.interrupted());
    }

    @Override
    public Map.Entry<Long, byte[]> cast(long k, byte[] v) {
        return new AbstractMap.SimpleEntry<>(k, v);
    }

    @Override
    public void init(String address, Properties properties) {
    }

    @Override
    public void cleanup() {}

    @Override
    public byte[] get(Long key) {
        delay();
        lock.readLock().lock();
        byte[] data = database.get(key);
        lock.readLock().unlock();
        return data;
    }

    @Override
    public byte[] put(Long key, byte[] value) {
        delay();
        lock.writeLock().lock();
        byte[] data = database.put(key, value);
        lock.writeLock().unlock();
        return data;
    }

    @Override
    public Status set(Long key, byte[] value) {
        delay();
        lock.writeLock().lock();
        database.put(key, value);
        lock.writeLock().unlock();
        return Status.OK;
    }

    @Override
    public Status delete(Long key) {
        delay();
        lock.writeLock().lock();
        database.remove(key);
        lock.writeLock().unlock();
        return Status.OK;
    }

    @Override
    public byte[] remove(Long key) {
        delay();
        lock.writeLock().lock();
        byte[] data = database.remove(key);
        lock.writeLock().unlock();
        return data;
    }
}
