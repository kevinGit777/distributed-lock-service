import java.util.Deque;
import java.util.concurrent.locks.Lock;

public class Dlock implements Listener {
    Node node;
    Deque<Message> queue;
    int current_request_timestamp;
    Lock lock;
    NodeID[] neighbors;
    boolean[] recieve_neighbor;
    boolean[] broken_neighbor;

    public Dlock(NodeID identifier, String configFileName) {
        this.node = new Node(identifier, configFileName, this);// Here is a error with the third parameter.
        this.current_request_timestamp = 0;
        int nodeNum = node.node_num;
        this.neighbors = node.getNeighbors();
        recieve_neighbor = new boolean[neighbors.length];
        broken_neighbor = new boolean[neighbors.length];
        for (int i = 0; i < nodeNum; i++) {
            recieve_neighbor[i] = false;
            broken_neighbor[i] = false;
        }
    }

    public void lock(int time_stamp) {
        Message requestMessage = makeMessage("request");
        node.sendToAll(requestMessage);
        waitForAllNeighborReply();
    }

    public void unlock() {
        lock.lock();
        current_request_timestamp = -1;
        for (Message message : queue) {
            node.send(makeMessage("reply"), message.source);
        }
        lock.unlock();
    }

    @Override
    synchronized public void receive(Message message) {
        String type = new String(message.data);
        if (type == "request") {
            lock.lock();
            if (current_request_timestamp == -1 || message.timestamp < current_request_timestamp) {
                node.send(makeMessage("reply"), message.source);
            } else {
                queue.push(message);
            }
            lock.unlock();
        } else // type == reply
        {
            for (int i = 0; i < recieve_neighbor.length; i++) {
                if (neighbors[i].getID() == message.source.getID()) {
                    recieve_neighbor[i] = true;
                }

            }
            notify();
        }
    }

    synchronized void waitForAllNeighborReply() {
        for (int i = 0; i < recieve_neighbor.length; i++) {
            recieve_neighbor[i] = false;
        }
        for (int i = 0; i < recieve_neighbor.length; i++) {
            while (recieve_neighbor[i]) {
                try {
                    wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    Message makeMessage(String msg) {
        return new Message(node.getNodeID(), msg.getBytes(), this.current_request_timestamp);
    }

    @Override
    public void broken(NodeID neighbor) {
        // not doing anything cuz
        for (int i = 0; i < neighbors.length; i++) {
            if (neighbor.getID() == neighbors[i].getID()) {
                broken_neighbor[i] = true;
            }
        }
        System.out.println("Site |" + neighbor.getID() + "| is now broken");
    }

    public void close() {
        this.node.tearDown();
        while (!allNeighborBroken()) {
			try {
				notify();
				System.out.println("Wait for all neighbor broke.");
				wait();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

    }

    boolean allNeighborBroken() {
		for (boolean broken : broken_neighbor) {
			if (!broken)
				return false;
		}
		return true;
	}
}
