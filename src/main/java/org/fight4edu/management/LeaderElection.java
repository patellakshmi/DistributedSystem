package org.fight4edu.management;

import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.List;


public class LeaderElection implements Watcher {

    public static String ZK_ADDRESS = "localhost:2181";
    public static String ELECTION_NAMESPACE = "/election";
    public static String TARGET_ZNODE= "/target_znode";
    private ZooKeeper zooKeeper;
    public static int ZK_SESSION = 3000;
    public String currentZNodeName;

    private OnElectionCallback onElectionCallback;



    public LeaderElection(ZooKeeper zooKeeper, OnElectionCallback onElectionCallback){
        this.zooKeeper = zooKeeper;
        this.onElectionCallback = onElectionCallback;
    }

    public void volunteerForLeadership() throws KeeperException, InterruptedException {
        String zooPrefix = ELECTION_NAMESPACE+"/c_";
        String fullPath = zooKeeper.create(zooPrefix, new byte[]{}, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL);

        System.out.println("Path:"+fullPath);
        currentZNodeName = fullPath.replace(ELECTION_NAMESPACE+"/", "");
    }

    public void reElectLeader() throws KeeperException, InterruptedException, UnknownHostException {
        String predecessor = "";
        Stat predStat = null;
        while (predStat == null){
            List<String> children = zooKeeper.getChildren(ELECTION_NAMESPACE, false);
            Collections.sort(children);
            String smallChild = children.get(0);
            if( smallChild.equals(currentZNodeName)){
                System.out.println("I am the leader");
                onElectionCallback.onElectionToBeLeader();
                return;
            }else{
                System.out.println("I am not leader");
                int predIndex = Collections.binarySearch(children, currentZNodeName)-1;
                predecessor = children.get(predIndex);
                predStat = zooKeeper.exists(ELECTION_NAMESPACE+"/"+predecessor, this);
            }
        }

        onElectionCallback.onWorker();

        System.out.println("Watching node-"+predecessor);


    }

    public void run() throws InterruptedException {
        synchronized (zooKeeper){
            zooKeeper.wait();
        }
    }

    public void close() throws InterruptedException {
        zooKeeper.close();
    }

    @Override
    public void process(WatchedEvent event) {
        switch (event.getType()){
            case None:
                if( event.getState() == Event.KeeperState.SyncConnected ){
                    System.out.println("Successfully connected to zoo-keeper");
                }else{
                    synchronized (zooKeeper){
                        System.out.println("zoo-keeper: disconnected event");
                        zooKeeper.notifyAll();
                    }
                }
            case NodeCreated:
                System.out.println(TARGET_ZNODE+"created");
                break;
            case NodeDeleted:
                System.out.println(TARGET_ZNODE+"deleted");
                try {
                    reElectLeader();
                } catch (KeeperException e) {
                    e.printStackTrace();
                } catch (InterruptedException | UnknownHostException e) {
                    e.printStackTrace();
                }
                break;
            case NodeDataChanged:
                System.out.println(TARGET_ZNODE+"data changed");
                break;
            case NodeChildrenChanged:
                System.out.println(TARGET_ZNODE+"children changed");
                break;
        }


    }
}
