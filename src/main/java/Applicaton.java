import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import org.apache.zookeeper.*;
import org.fight4edu.management.CoordinationRegistry;
import org.fight4edu.management.LeaderElection;
import org.fight4edu.management.OnElectionAction;
import org.fight4edu.management.ServiceRegistry;
import org.fight4edu.web.WebClient;
import org.fight4edu.web.WebServer;
import sun.net.www.http.HttpClient;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

public class Applicaton implements Watcher {

    public static String ZK_ADDRESS = "localhost:2181";
    public static String ELECTION_NAMESPACE = "/election";
    public static String TARGET_ZNODE= "/target_znode";
    private ZooKeeper zooKeeper;
    public static int ZK_SESSION = 3000;
    public static int DEFAULT_PORT = 8080;
    public String currentZNodeName;


    public static void main(String[] argv) throws IOException, InterruptedException, KeeperException {
        int currentPort = argv.length == 1 ? Integer.parseInt(argv[0]):DEFAULT_PORT;

        Applicaton applicaton = new Applicaton();
        applicaton.connectToZoo();
        leaderElectionAndReg(applicaton.zooKeeper, currentPort );
    }


    private static void leaderElectionAndReg(ZooKeeper zooKeeper, int currentPort) throws KeeperException, InterruptedException, UnknownHostException {
        ServiceRegistry serviceRegistry = new ServiceRegistry(zooKeeper);
        CoordinationRegistry coordinationRegistry = new CoordinationRegistry(zooKeeper);
        LeaderElection leaderElection = new LeaderElection(zooKeeper, new OnElectionAction( serviceRegistry, coordinationRegistry, currentPort));
        leaderElection.volunteerForLeadership();
        leaderElection.reElectLeader();

        leaderElection.run();
        leaderElection.close();
    }

    public void connectToZoo() throws IOException, InterruptedException {
        this.zooKeeper = new ZooKeeper(ZK_ADDRESS, ZK_SESSION, this);

    }

    @Override
    public void process(WatchedEvent event) {
        switch (event.getType()){
            case None:
                if( event.getState() == Watcher.Event.KeeperState.SyncConnected ){
                    System.out.println("Successfully connected to zoo-keeper");
                }else{
                    synchronized (zooKeeper){
                        System.out.println("zoo-keeper: disconnected event");
                        zooKeeper.notifyAll();
                    }
                }
            case NodeDataChanged:
                System.out.println(TARGET_ZNODE+"data changed");
                break;
            case NodeChildrenChanged:
                System.out.println(TARGET_ZNODE+"children changed");
                break;
        }

    }



}
