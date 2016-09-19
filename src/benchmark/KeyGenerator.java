package benchmark;

import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicLong;

public class KeyGenerator {

    public enum Distribution { Constant, Sequence, Uniform, Hotspot, Exponential, Zipfian }

    private long min;
    private long max;
    private Distribution distribution;
    private Generator generator;

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
                generator = new Hotspot();
                break;
            case Zipfian:
                generator = new Zipfian();
                break;
        }
    }

    public long next() {
        return generator.next();
    }

    public void setParameter(double parameter) {
        if (generator instanceof Hotspot) {
            ((Hotspot) generator).set(parameter);
        } else if (generator instanceof Zipfian) {
            ((Zipfian) generator).set(parameter);
        }
    }

    private interface Generator {
        long next();
    }

    private class Constant implements Generator {
        private long k = ThreadLocalRandom.current().nextLong(min, max);

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
        @Override
        public long next() {
            return ThreadLocalRandom.current().nextLong(min, max);
        }
    }

    private class Hotspot implements Generator {
        private ThreadLocalRandom random = ThreadLocalRandom.current();
        private double photset = 0.2;
        private double photops = 1 - photset;
        private long hotset = (long) ((max - min + 1) * photset);
        private long coldset = (max - min + 1) - hotset;

        public void set(double hotdata) {
            photset = hotdata;
            photops = 1 - photset;
            hotset = (long) ((max - min + 1) * photset);
            coldset = (max - min + 1) - hotset;
        }

        @Override
        public long next() {
            if (random.nextDouble() < photops) {
                return random.nextLong(hotset) + min;
            } else {
                return random.nextLong(coldset) + min + hotset;
            }
        }
    }

    private class Zipfian implements Generator {
        public static final double ZIPFIAN_CONSTANT = 0.99;
        private ThreadLocalRandom random = ThreadLocalRandom.current();
        private long items = max - min + 1;
        /**
         * Computed parameters for generating the distribution.
         */
        private double alpha, zetan, eta, theta, zeta2theta;

        /**
         * The number of items used to compute zetan the last time.
         */
        private long countforzeta;

        public void set(double zetan) {
            theta = ZIPFIAN_CONSTANT;
            zeta2theta = zeta(2, theta);
            alpha = 1.0 / (1.0 - theta);
            this.zetan = zetan;
            countforzeta = items;
            eta = (1 - Math.pow(2.0 / items, 1 - theta)) / (1 - zeta2theta / zetan);
        }

        double zeta(long n, double theta) {
            countforzeta = n;
            return zetastatic(n, theta);
        }

        double zetastatic(long n, double theta) {
            return zetastatic(0, n, theta, 0);
        }

        double zeta(long st, long n, double theta, double initialsum) {
            countforzeta = n;
            return zetastatic(st, n, theta, initialsum);
        }

        double zetastatic(long st, long n, double theta, double initialsum) {
            double sum = initialsum;
            for (long i = st; i < n; i++) {
                sum += 1 / (Math.pow(i + 1, theta));
            }
            return sum;
        }

        @Override
        public long next() {
            //from "Quickly Generating Billion-Record Synthetic Databases", Jim Gray et al, SIGMOD 1994
            if (items != countforzeta) {
                // have to recompute zetan and eta, since they depend on itemcount
                synchronized(this) {
                    if (items > countforzeta) {
                        // we have added more items. can compute zetan incrementally, which is cheaper
                        zetan = zeta(countforzeta, items, theta, zetan);
                        eta = (1 - Math.pow(2.0 / items, 1 - theta)) / (1 - zeta2theta / zetan);
                    } else if (items < countforzeta) {
                        //have to start over with zetan
                        //note : for large itemsets, this is very slow. so don't do it!

                        //TODO: can also have a negative incremental computation, e.g. if you decrease the number of items, then just subtract
                        //the zeta sequence terms for the items that went away. This would be faster than recomputing from scratch when the number of items
                        //decreases

                        System.err.println("WARNING: Recomputing Zipfian distribtion. This is slow and should be avoided. (itemcount=" + items + " countforzeta=" + countforzeta + ")");

                        zetan = zeta(items, theta);
                        eta = (1 - Math.pow(2.0 / items, 1 - theta)) / (1 - zeta2theta / zetan);
                    }
                }
            }
            double u = random.nextDouble();
            double uz = u * zetan;
            if (uz < 1.0) { return min; }
            if (uz < 1.0 + Math.pow(0.5, theta)) { return min + 1; }
            long ret = min + (long)((items) * Math.pow(eta * u - eta + 1, alpha));
            return ret;
        }
    }


    public static void main(String[] args) {
        long min = 0;
        long max = 1000;
        KeyGenerator keys = new KeyGenerator(min, max, Distribution.Uniform);
        for (long i = min; i < max; i++) {
            System.out.println(keys.next());
        }
    }
}
