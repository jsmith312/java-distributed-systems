import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

public class LeaderElection implements Watcher {

    private static final String ZOOKEEPER_ADDRESS = "localhost:2181";
    private static final int SESSION_TIMEOUT = 3000;
    private static final String ELECTION_NAMESPACE = "/election";
    private static final String TARGETZNODE = "/target_znode";
    private ZooKeeper zooKeeper;
    private String currentZnodeName;

    public static void main(String[] args) throws IOException, InterruptedException, KeeperException {
        LeaderElection leaderElection = new LeaderElection();
        leaderElection.connectToZookeeper();
        leaderElection.volunteerForLeadership();
        leaderElection.reelectLeader();
        leaderElection.run(); // in a wait state
        leaderElection.close(); // shutdown
        System.out.println("Disconnected from zookeeper, exiting application.");
    }

    public void volunteerForLeadership() throws KeeperException, InterruptedException {
        String _zNodePrefix = ELECTION_NAMESPACE + "/c_";
        String zNodeFullPath = zooKeeper.create(_zNodePrefix, new byte[]{}, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL);

        System.out.println("znode name: " + zNodeFullPath);
        this.currentZnodeName = zNodeFullPath.replace(ELECTION_NAMESPACE + "/", "");
    }

    public void reelectLeader() throws KeeperException, InterruptedException {
        Stat predecessorStat = null;
        String predecessorZnode = "";
        while (predecessorStat == null) {
            List<String> children = zooKeeper.getChildren(ELECTION_NAMESPACE, false);
            Collections.sort(children);
            String smallestChild = children.get(0);
            if (smallestChild.equals(currentZnodeName)) {
                System.out.println("I am the leader.");
                return;
            } else {
                System.out.println("I am not the leader");
                int predecessorIndex = Collections.binarySearch(children, currentZnodeName) - 1;
                predecessorZnode = children.get(predecessorIndex);
                predecessorStat = zooKeeper.exists(ELECTION_NAMESPACE + "/" + predecessorZnode, this);
            }
        }
        System.out.println("Watching znode " + predecessorZnode);
        System.out.println();
    }

    public void connectToZookeeper() throws IOException {
        this.zooKeeper = new ZooKeeper(ZOOKEEPER_ADDRESS, SESSION_TIMEOUT, this);
    }

    public void run() throws InterruptedException {
        synchronized (zooKeeper) {
            zooKeeper.wait();
        }
    }

    public void close() throws InterruptedException {
        zooKeeper.close();
    }

    public void watchTargetZnode() throws KeeperException, InterruptedException {
        Stat stat = zooKeeper.exists(TARGETZNODE, this);
        if (stat == null) {
            return;
        }

        byte[] data = zooKeeper.getData(TARGETZNODE, this, stat);
        List<String> children = zooKeeper.getChildren(TARGETZNODE, this);

        System.out.println("Data: " + new String(data) + " children " + children);
    }

    public void process(WatchedEvent watchedEvent) {
        switch (watchedEvent.getType()) {
            case None:
                if (watchedEvent.getState() == Event.KeeperState.SyncConnected) {
                     System.out.println("Connected to zookeeper instance.");
                } else {
                    synchronized (zooKeeper) {
                        System.out.println("Disconnected from zookeeper");
                        zooKeeper.notifyAll();
                    }
                }
                break;
            case NodeCreated:
                System.out.println(TARGETZNODE + " created.");
                break;
            case NodeDeleted:
                System.out.println(TARGETZNODE + " deleted.");
                try {
                    reelectLeader();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (KeeperException e) {
                    e.printStackTrace();
                }
                break;
            case NodeDataChanged:
                System.out.println(TARGETZNODE + " data changed.");
                break;
            case NodeChildrenChanged:
                System.out.println(TARGETZNODE + " children changed.");
                break;
        }
        try {
            // get all updated data after changes and print to screen
            // and re-register to watch further chages.
            watchTargetZnode();
        } catch (KeeperException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
