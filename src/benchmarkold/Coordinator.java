package benchmarkold;

import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.ICountDownLatch;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.*;

public class Coordinator {

    private static Coordinator coordinator = new Coordinator();

    private Config config;
    private BenchmarkMode mode;
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

    private Coordinator() {
        config = new Config();
        config.load();
        mode = config.getBenchmarkMode();
    }

    public static Coordinator get() {
        return coordinator;
    }

    public void init() {
        int n = config.getClients();
        clients = new Client[n];
        switch (mode) {
            case CENTRALIZED:
                executor = Executors.newFixedThreadPool(n);
                ready = new CountDownLatch(n);
                start = new CountDownLatch(1);
                for (int i = 0; i < n; i++) {
                    clients[i] = new LocalClient();
                    clients[i].setId(i);
                    clients[i].setAddress(config.getAddress());
                    clients[i].setConfig(config);
                    clients[i].setMin(1);
                    clients[i].setMax(config.getRecordCount());
                }
                break;
            case DISTRIBUTED:
                com.hazelcast.config.Config hzconfig = new com.hazelcast.config.Config();
                hzconfig.setLiteMember(true);
                hzconfig.getGroupConfig().setName(Config.GROUP_NAME).setPassword(Config.GROUP_PASS);
                instance = Hazelcast.newHazelcastInstance(hzconfig);
                instance.getReplicatedMap(Config.MAP_NAME).putAll(config.asMap());
                executor = instance.getExecutorService("executor");
                dready = instance.getCountDownLatch("ready");
                dready.trySetCount(n);
                dstart = instance.getCountDownLatch("start");
                dstart.trySetCount(1);
                for (int i = 0; i < n; i++) {
                    clients[i] = new RemoteClient();
                    clients[i].setId(i);
                    clients[i].setAddress(config.getAddress());
                    clients[i].setMin(1);
                    clients[i].setMax(config.getRecordCount());
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

    public void load() {
        System.out.printf("Loading %d keys into DB %s with data size %d... \n", config.getRecordCount(), config.getDB(), config.getDataSize());
        long startTime = System.nanoTime();

        for (Client client : clients) {
            executor.submit(client);
        }

        // todo ailidani how to wait till finished?

    }

    public void run() {
        System.out.println("Running benchmarkold for " + config.getTotalTime() + " seconds... ");
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
                System.err.printf("%s has error.", client);
            }
        }

        System.out.println(stats);
        System.out.printf("Overall Throughput = %f (ops/s)\n", (double)stats.getOperationCount() / (double)config.getTotalTime());

    }

    public void shutdown() {
        System.out.println("Shutting down...");
        executor.shutdown();
        System.out.println("Done.");
    }


    public static void main(String [] args) {
        Coordinator coordinator = Coordinator.get();
        coordinator.init();
        coordinator.run();
        coordinator.shutdown();
        System.exit(0);
    }
}
