package org.fight4edu.management;

import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CoordinationRegistry implements Watcher {

    public static String COORDINATE_ZNODE = "/coordinate_registry";
    public String currentZNode = null;
    public List<String> allServiceAdd = null;

    private final ZooKeeper zooKeeper;

    public CoordinationRegistry(ZooKeeper zooKeeper) {
        this.zooKeeper = zooKeeper;
        coordinateServiceRegistry();
    }


    public void coordinateServiceRegistry() {
        try {
            if (zooKeeper.exists(COORDINATE_ZNODE, false) == null) {
                zooKeeper.create(COORDINATE_ZNODE, new byte[]{}, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
            }
        } catch (Exception e) {

        }
    }

    public void registerWithCluster(String metaData) throws KeeperException, InterruptedException {
        currentZNode = zooKeeper.create(COORDINATE_ZNODE+"/n", metaData.getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL);
    }

    public void unregisterFromCluster() throws KeeperException, InterruptedException {
        if( currentZNode != null && zooKeeper.exists(currentZNode, false )!= null){
            zooKeeper.delete(currentZNode, -1);
        }
    }

    public void registerForUpdate() {
        try {
            updateAddress();
        } catch (KeeperException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    synchronized public List<String> getAllServiceAdd() throws KeeperException, InterruptedException {
        if( allServiceAdd == null){
            this.updateAddress();
        }

        return this.allServiceAdd;
    }


    private synchronized void updateAddress() throws KeeperException, InterruptedException {
        List<String> workerZnodes = zooKeeper.getChildren(COORDINATE_ZNODE,  this);
        List<String> addresses = new ArrayList<>(workerZnodes.size());
        for(String workerZnode: workerZnodes){
            String workerZnodeFullAdd = COORDINATE_ZNODE+"/"+workerZnode;
            Stat state = zooKeeper.exists(workerZnodeFullAdd, false);
            if( state == null) continue;
            byte[] data = zooKeeper.getData(workerZnodeFullAdd, false, state);
            String address = new String(data);
            addresses.add(address);
        }

        this.allServiceAdd = Collections.unmodifiableList(addresses);
        System.out.println("Coordinate Node is : "+this.allServiceAdd);
    }

    @Override
    public void process(WatchedEvent watchedEvent) {
        try {
            updateAddress();
        } catch (KeeperException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}
