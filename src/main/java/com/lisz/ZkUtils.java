package com.lisz;

import org.apache.zookeeper.ZooKeeper;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;

public class ZkUtils {
    private static String address = "192.168.1.131:2181,192.168.1.132:2181,192.168.1.133:2181,192.168.1.134:2181/testLock4";
    private static CountDownLatch latch = new CountDownLatch(1);
    private static DefaultWatcher watcher = new DefaultWatcher(latch);

    public static ZooKeeper getZk() {
        ZooKeeper zk = null;
        try {
            zk = new ZooKeeper(address, 1000, watcher);
            latch.await();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        return zk;
    }
}
