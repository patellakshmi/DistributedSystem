package org.fight4edu.management;

import org.apache.zookeeper.KeeperException;

import java.net.UnknownHostException;

public interface OnElectionCallback {
    void onElectionToBeLeader() throws KeeperException, InterruptedException, UnknownHostException;
    void onWorker();
}
