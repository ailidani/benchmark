package benchmark;

import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;

public class KeyGenerator {

    public enum Distribution { Constant, Sequence, Uniform, Hotspot, Exponential, Zipfian }

    private long min;
    private long max;
    private Distribution distribution;
    private Generator generator;
    private float hot;

    public KeyGenerator(long min, long max, Distribution distribution) {
        this.min = min;
        this.max = max;
        this.distribution = distribution;
        switch (distribution) {
            case Constant:
                generator = new Constant();
                break;
            case Sequence:
                generator = new Sequence();
                break;
            case Uniform:
                generator = new Uniform();
                break;
            case Hotspot:

        }
    }

    public long next() {
        return generator.next();
    }

    private interface Generator {
        long next();
    }

    private class Constant implements Generator {
        private long k = new Random().nextLong() % (max - min + 1) + min;

        @Override
        public long next() {
            return k;
        }
    }

    private class Sequence implements Generator {
        private AtomicLong k = new AtomicLong(min);

        @Override
        public long next() {
            return k.getAndIncrement();
        }
    }

    private class Uniform implements Generator {
        Random random = new Random();
        long n = max - min + 1;

        @Override
        public long next() {
            return random.nextLong() % n + min;
        }
    }

    private class Hotspot implements Generator {

        @Override
        public long next() {
            return 0;
        }
    }
}
