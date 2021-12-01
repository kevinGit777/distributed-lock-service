import java.util.Deque;
import java.util.concurrent.locks.Lock;
public class Dlock {
    Node node;
    Deque<Message> queue;
    int current_request_timestamp;
    Lock lock;
    NodeID[] neighbors;
    boolean[] recieve_neighbor;

    public Dlock(NodeID identifier, String configFileName){
            this.node = Node(identifier, configFileName,);// Here is a error with the third parameter.
        this.current_request_timestamp = -1;
        int nodeNum = node.node_num;
        this.neighbors = node.getNeighbors();
        for (int i = 0;i < nodeNum;i++){
            recieve_neighbor[i] = false;
        }
    }

    public void lock(int time_stamp){
        Message requestMessage = makeMessage("request");
        node.sendToAll(requestMessage);
        waitForAllNeighborReply();
    }

    public void unlock(){
        lock.lock();
        current_request_timestamp = -1;
        for ( Message message : queue) {
            node.send(makeMessage("reply"), message.source);
        }
        lock.unlock();
    }

    synchronized void recieve(Message message)
    {
        String type = new String(message.data); 
        if (type == "request"){
            lock.lock();
            if(current_request_timestamp == -1 || message.timestamp < current_request_timestamp)
                {
			    node.send(makeMessage("reply"), message.source);
            }else
            {
               queue.push(message); 
            }
            lock.unlock();
        }else // type == reply
        {
            for (int i = 0; i < recieve_neighbor.length; i++) {
                if (neighbors[i].getID() == message.source.getID())
                {
                    recieve_neighbor[i] = true;
                }
                
            }
            notify();
        }
    }

    synchronized void waitForAllNeighborReply()
    {
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

    Message makeMessage(String msg)
    {
        return new Message(node.getNodeID(), msg.getBytes() , this.current_request_timestamp);
    }
}
