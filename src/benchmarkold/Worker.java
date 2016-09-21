package benchmarkold;

import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;

public class Worker {

    public static void main(String [] args) {
        com.hazelcast.config.Config hzconfig = new com.hazelcast.config.Config();
        hzconfig.getGroupConfig().setName(Config.GROUP_NAME).setPassword(Config.GROUP_PASS);
        HazelcastInstance instance = Hazelcast.newHazelcastInstance(hzconfig);
    }
}
