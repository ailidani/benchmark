package benchmark;

import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.ICountDownLatch;

import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.*;

public class Coordinator {

    private static Coordinator coordinator = new Coordinator();

    private Config config;
    private Mode mode;
    private Client[] clients;

    /**
     * Centralized executor has same number of threads as number of clients.
     */
    private ExecutorService executor;
    private Map<Client, Future<Stats>> futures = new HashMap<>();
    private CountDownLatch ready;
    private CountDownLatch start;
    private ICountDownLatch dready;
    private ICountDownLatch dstart;

    private HazelcastInstance instance;
    private WebServer webServer;

    private Coordinator() {
        config = new Config();
        config.load();
        mode = config.getBenchmarkMode();
        try {
            webServer = new WebServer();
            webServer.start();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }

    public static Coordinator get() {
        return coordinator;
    }

    public void init() {
        int n = config.getClients();
        float p = config.getOverlap();
        long size = config.getRecordCount();
        /**
         * |----------|~~~|
         *            |----------|~~~|
         *                       |----------|~~~|
         * |------------------------------------|
         */
        long shift = (long) (size * (1 - p) / n);
        clients = new Client[n];
        switch (mode) {
            case CENTRALIZED:
                executor = Executors.newFixedThreadPool(n);
                ready = new CountDownLatch(n);
                start = new CountDownLatch(1);
                for (int i = 0; i < n; i++) {
                    long min = 1 + i * shift;
                    long max = min + size - 1;
                    clients[i] = new LocalClient(i, min, max, config.getAddress());
                    clients[i].setConfig(config);
                }
                break;
            case DISTRIBUTED:
                com.hazelcast.config.Config hzconfig = new com.hazelcast.config.Config();
                hzconfig.setLiteMember(true);
                hzconfig.getGroupConfig().setName(Config.GROUP_NAME).setPassword(Config.GROUP_PASS);
                instance = Hazelcast.newHazelcastInstance(hzconfig);
                //instance.getReplicatedMap(Config.MAP_NAME).putAll(config.asMap());
                executor = instance.getExecutorService("executor");
                dready = instance.getCountDownLatch("ready");
                dready.trySetCount(n);
                dstart = instance.getCountDownLatch("start");
                dstart.trySetCount(1);
                for (int i = 0; i < n; i++) {
                    long min = 1 + i * shift;
                    long max = min + size - 1;
                    clients[i] = new RemoteClient(i, min, max, config.getAddress());
                    clients[i].setConfig(config);
                }
                break;
            default:
                System.err.println("Unknown mode.");
                System.exit(1);
        }
    }

    private void start() throws InterruptedException {
        switch (mode) {
            case CENTRALIZED:
                ready.await();
                start.countDown();
                break;
            case DISTRIBUTED:
                dready.await(10, TimeUnit.MINUTES);
                dstart.countDown();
                break;
        }
    }

    /**
     * Called by local clients
     */
    public void ready() throws InterruptedException {
        ready.countDown();
        start.await();
    }

    /**
     * Called by local client
     * @param stat current interval stat
     */
    public void publish(Stat stat) {
        this.webServer.sendToAll(String.valueOf(stat.getThroughput()));
    }

    public void load() {
        System.out.printf("Loading %d keys into DB %s with data size %d... \n", config.getRecordCount(), config.getDB(), config.getDataSize());
        int n = config.getClients();
        clients = new Client[n];
        long interval = config.getRecordCount() / n;
        switch (mode) {
            case CENTRALIZED:
                executor = Executors.newFixedThreadPool(n);
                break;
            case DISTRIBUTED:
                com.hazelcast.config.Config hzconfig = new com.hazelcast.config.Config();
                hzconfig.setLiteMember(true);
                hzconfig.getGroupConfig().setName(Config.GROUP_NAME).setPassword(Config.GROUP_PASS);
                instance = Hazelcast.newHazelcastInstance(hzconfig);
                instance.getReplicatedMap(Config.MAP_NAME).putAll(config.asMap());
                executor = instance.getExecutorService("executor");
                break;
        }

        for (int i = 0; i < n; i++) {
            long start = i * interval;
            long end = start + interval;
            clients[i] = new LoadClient(i, start, end, config.getAddress());
            clients[i].setConfig(config);
        }

        for (Client client : clients) {
            futures.put(client, executor.submit(client));
        }

        Stats stats = new Stats();
        for (Client client : clients) {
            try {
                Stats result = futures.get(client).get();
                stats.add(result);
            } catch (InterruptedException e) {
                // ignore
            } catch (ExecutionException e) {
                System.err.printf("%s has error.\n", client);
            }
        }

        System.out.println(stats);
        System.out.printf("Overall : %s\n", stats.overall());

    }

    public void run() {
        init();
        System.out.println("Running benchmark for " + (config.getTotalTime() + config.getWarmupTime()) + " seconds... ");
        long startTime = System.nanoTime();

        for (Client client : clients) {
            futures.put(client, executor.submit(client));
        }

        try {
            start();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        Stats stats = new Stats();
        for (Client client : clients) {
            try {
                Stats result = futures.get(client).get();
                stats.add(result);
            } catch (InterruptedException e) {
                // ignore
            } catch (ExecutionException e) {
                System.err.printf("%s has error.\n", client);
            }
        }

        System.out.println(stats);
        System.out.printf("Overall : %s\n", stats.overall());

    }

    public void shutdown() {
        System.out.println("Shutting down...");
        executor.shutdown();
        System.out.println("Done.");
    }


    public static void main(String [] args) {
        Coordinator coordinator = Coordinator.get();

        if (args.length > 0 && args[0].equalsIgnoreCase("load")) {
            coordinator.load();
        } else {
            coordinator.run();
        }

        coordinator.shutdown();
        System.exit(0);
    }
}
