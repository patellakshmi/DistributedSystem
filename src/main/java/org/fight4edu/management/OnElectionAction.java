package org.fight4edu.management;

import org.apache.zookeeper.KeeperException;
import org.fight4edu.management.OnElectionCallback;
import org.fight4edu.management.ServiceRegistry;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class OnElectionAction implements OnElectionCallback {

    private ServiceRegistry serviceRegistry;
    private int port;
    private CoordinationRegistry coordinationRegistry;

    public OnElectionAction(ServiceRegistry serviceRegistry, CoordinationRegistry coordinationRegistry, int port){
        this.serviceRegistry = serviceRegistry;
        this.coordinationRegistry = coordinationRegistry ;
        this.port = port;

    }

    @Override
    public void onElectionToBeLeader() throws KeeperException, InterruptedException, UnknownHostException {
        String currentAdd = String.format("http://%s:%d", InetAddress.getLocalHost().getCanonicalHostName(),port);
        serviceRegistry.unregisterFromCluster();
        serviceRegistry.registerForUpdate();
        try{
        coordinationRegistry.registerWithCluster(currentAdd);
        }catch (Exception e){
            System.out.println("Registration failed");
        }

        coordinationRegistry.registerForUpdate();
    }

    @Override
    public void onWorker() {
            try{
                String currentAdd = String.format("http://%s:%d", InetAddress.getLocalHost().getCanonicalHostName(),port);
                serviceRegistry.registerWithCluster(currentAdd);
                coordinationRegistry.unregisterFromCluster();
            }catch (Exception e){

            }
    }
}
