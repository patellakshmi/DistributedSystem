package org.fight4edu.management;

import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ServiceRegistry implements Watcher{

    public static String REGISTRY_ZNODE = "/service_registry";
    public String currentZNode = null;
    public List<String> allServiceAdd = null;

    private final ZooKeeper zooKeeper;

    public ServiceRegistry(ZooKeeper zooKeeper) {
        this.zooKeeper = zooKeeper;
        createServiceRegistry();
    }


    public void createServiceRegistry() {
        try {
            if (zooKeeper.exists(REGISTRY_ZNODE, false) == null) {
                zooKeeper.create(REGISTRY_ZNODE, new byte[]{}, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
            }
        } catch (Exception e) {

        }
    }

    public void registerWithCluster(String metaData) throws KeeperException, InterruptedException {
            currentZNode = zooKeeper.create(REGISTRY_ZNODE+"/n", metaData.getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL);
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
        List<String> workerZnodes = zooKeeper.getChildren(REGISTRY_ZNODE,  this);
        List<String> addresses = new ArrayList<>(workerZnodes.size());
        for(String workerZnode: workerZnodes){
                String workerZnodeFullAdd = REGISTRY_ZNODE+"/"+workerZnode;
                Stat state = zooKeeper.exists(workerZnodeFullAdd, false);
                if( state == null) continue;
                byte[] data = zooKeeper.getData(workerZnodeFullAdd, false, state);
                String address = new String(data);
                addresses.add(address);
        }

        this.allServiceAdd = Collections.unmodifiableList(addresses);
        System.out.println("All the address in our system: "+this.allServiceAdd);
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
