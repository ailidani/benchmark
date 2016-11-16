package simulator;

import benchmark.Coordinator;

import java.util.concurrent.DelayQueue;

public class Test {

    static DelayQueue<Message> messages = new DelayQueue<>();

    public static void main(String[] args) {

        // Log.set(Log.LEVEL_INFO);

        Coordinator coordinator = Coordinator.get();

        for (int n = 3; n <= 15; n++) {
            Raft.N = n;
            coordinator.run();
            coordinator.shutdown();
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

//        Generator generator = new Generator(100, 1000, Generator.Distribution.Zipfian);
//        for (long i = 0; i < 100; i++) {
//            Message msg = new Message(String.valueOf(i));
//            messages.offer(msg.setLatency(generator.next()));
//        }
//
//        while (true) {
//            try {
//                System.out.println(messages.take());
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//        }
    }
}
