package com.lisz;

import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;

public class WatcherCallback implements AsyncCallback.StringCallback, AsyncCallback.Children2Callback, AsyncCallback.StatCallback, Watcher {
    private ZooKeeper zk;
    private CountDownLatch latch = new CountDownLatch(1);
    private String threadName;

    public void setThreadName(String threadName) {
        this.threadName = threadName;
    }

    private String pathName;

    public WatcherCallback(ZooKeeper zk) {
        this.zk = zk;
    }

    @Override
    public void processResult(int rc, String path, Object ctx, List<String> children, Stat stat) {
        System.out.println(threadName + " is looking for lock");
        Collections.sort(children);
        int index = children.indexOf(pathName.substring(1));
        if (index == 0) {
            System.out.println(threadName + " is the first and gets the lock");
            latch.countDown();
        } else {
            zk.exists("/" + children.get(index - 1), this, this, "asd");
        }
    }

    @Override
    public void processResult(int rc, String path, Object ctx, Stat stat) {

    }

    @Override
    public void processResult(int rc, String path, Object ctx, String name) {
        if (name != null) {
            pathName = name;
            zk.getChildren("/", false, this, "asd");
        }
    }

    @Override
    public void process(WatchedEvent event) {
        Event.EventType type = event.getType();
        switch (type) {
            case None:
                break;
            case NodeCreated:
                break;
            case NodeDeleted:
                zk.getChildren("/", false, this, "asd");
                break;
            case NodeDataChanged:
                break;
            case NodeChildrenChanged:
                break;
            case DataWatchRemoved:
                break;
            case ChildWatchRemoved:
                break;
            case PersistentWatchRemoved:
                break;
        }
    }

    public void tryLock() {
        zk.create("/lock4", threadName.getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL, this, "cxt");
        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void unLock() {
        try {
            zk.delete(pathName, -1);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (KeeperException e) {
            e.printStackTrace();
        }
        latch = new CountDownLatch(1);
    }
}
