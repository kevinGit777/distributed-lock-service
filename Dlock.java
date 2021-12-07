/*
 * Written by:
 * - Haifan Wu (hxw170013)
 * - Li Feng (lxf200001)
 * - Nicolas Amezquita (nxa200018)
 * 
 * For:
 * CS6378 Advanced Operating Systems (Fall 2021)
 * Professor Neeraj Mittal - UT Dallas
 * 
 * Monday, December 6th, 2021
 */

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Dlock implements Listener {
	Node node;
	Deque<Message> queue;
	int current_request_timestamp;
	Lock queue_lock;
	Lock CSLock;
	NodeID[] neighbors;
	boolean[] recieve_neighbor;
	boolean[] broken_neighbor;
	boolean terminating;

	public Dlock(NodeID identifier, String configFileName) {
		this.queue = new ArrayDeque<Message>();
		this.current_request_timestamp = 0;
		this.queue_lock = new ReentrantLock();
		this.CSLock = new ReentrantLock();
		int nei_num = getNeighborNum(configFileName); // assume complete graph
		this.recieve_neighbor = new boolean[nei_num];
		this.broken_neighbor = new boolean[nei_num];
		for (int i = 0; i < nei_num; i++) {
			recieve_neighbor[i] = false;
			broken_neighbor[i] = false;
		}
		terminating = false;
		queue_lock.lock(); // To prevent case, this node want to reply message but it is not finished setup
		this.node = new Node(identifier, configFileName, this);
		this.neighbors = node.getNeighbors();
		queue_lock.unlock();
		
	}

	private int getNeighborNum(String configFileName) {
		try {
			BufferedReader reader = new BufferedReader(new FileReader(configFileName));
			String str = reader.readLine();
			int num = 0;
			while (str != null) {
				if (Character.isDigit(str.charAt(0))) {
					int i;
					for (i = 1; i < str.length(); i++) {
						if (!Character.isDigit(str.charAt(i))) {
							break;
						}
					}
					num = Integer.parseInt(str.substring(0, i));
					break;
				}
				str = reader.readLine();
			}

			reader.close();
			return num - 1;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (NumberFormatException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return -1;
	}

	public void lock(int time_stamp) {
		// System.out.println("Want to lock.");
		current_request_timestamp = time_stamp;
		Message requestMessage = makeMessage("request");
		node.sendToAll(requestMessage);
		waitForAllNeighborReply();
		CSLock.lock();
	}

	public void unlock() {
		CSLock.unlock();
		queue_lock.lock();
		current_request_timestamp = -1;
		for (Message message : queue) {
			System.out.println("Pop " + message.source.getID() + " from queue");
			node.send(makeMessage("reply"), message.source);
		}
		queue.clear();
		queue_lock.unlock();
	}

	@Override
	synchronized public void receive(Message message) {
		if (terminating)
			return;
		String type = new String(message.data);
		CSLock.lock();
		CSLock.unlock();
		
		if (type.compareTo("request") == 0) {
			queue_lock.lock();
			
			//check for message from broken neighbor 
			for (int i = 0; i < neighbors.length; i++) {
				if (message.source.getID() == neighbors[i].getID() && broken_neighbor[i]) {
					queue_lock.unlock();
					return;
				}
			}
			
			if (current_request_timestamp == -1 || message.timestamp < current_request_timestamp // no unfulfilled jobs or smaller timestamp
 					|| (message.timestamp == current_request_timestamp
							&& message.source.getID() < this.node.getNodeID().getID())) {
				System.out.println("Reply to "+ message.source.getID());
				node.send(makeMessage("reply"), message.source);
			} else {
				System.out.println("Add " + message.source.getID() + " to queue");
				queue.addLast(message);
			}
			
			
			queue_lock.unlock();
		} else // type == reply
		{
			//System.out.println("Got respond from " + message.source.getID());
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
			recieve_neighbor[i] = broken_neighbor[i];
		}
		for (int i = 0; i < recieve_neighbor.length; i++) {
			while (!recieve_neighbor[i]) {
				try {
					wait();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}

	private Message makeMessage(String msg) {
		return new Message(node.getNodeID(), msg.getBytes(), this.current_request_timestamp);
	}

	@Override
	public synchronized void broken(NodeID neighbor) {
		// not doing anything cuz
		for (int i = 0; i < neighbors.length; i++) {
			if (neighbor.getID() == neighbors[i].getID()) {
				broken_neighbor[i] = true;
				//System.out.println("Neighbor "+i+" is broken.");
			}
		}
		System.out.println("Site |" + neighbor.getID() + "| is now broken");
		notify();
	}

	public synchronized void close() {
		terminating = true;
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
