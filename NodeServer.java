import java.io.*;
import java.net.*;
import java.util.*;

class NodeServer implements Runnable, Serializable {
	private NodeID source;
	private Integer port;
	private Thread t;
	private ServerSocket svrsoc;
	private boolean running;
	private Listener listener;
	private Vector<NodeServerThread> nst_vector = new Vector<>();
	Hashtable<NodeID, ArrayList<NodeID>> neighborsArray;

	public NodeServer(NodeID id, Integer port, Listener listener, Hashtable<NodeID, ArrayList<NodeID>> neighborsArray) {
		this.source = id;
		this.port = port;
		this.listener = listener;
		this.neighborsArray = neighborsArray;
		try {
			this.svrsoc = new ServerSocket(this.port);
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	@Override
	public void run() {
		try {
			System.out
					.println("Node |" + this.source.getID() + "| is ready for connection on port " + this.port + "!\n");
			while (this.running) {
				NodeServerThread nst = null;
				try {
					nst = new NodeServerThread(this.source, svrsoc.accept(), this.listener, neighborsArray);
				} catch (SocketException e) {
					System.out.println("Server " + this.source.getID() + " is close.");
				}
				//System.out.println("Add to vector.\n\n\n\n\n");
				if (nst != null) {
					nst_vector.add(nst);
					nst.start();
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public void start() {
		System.out.println("Starting server at Node |" + this.source.getID() + "|...\n");
		if (t == null) {
			this.running = true;
			t = new Thread(this, "server-" + this.source.getID());
			t.start();

		}
	}

	public synchronized void terminate() {
		this.running = false;
		try {
			/*
			for (NodeServerThread nodeServerThread : nst_vector) {
				if (nodeServerThread != null && nodeServerThread.running) {
					nodeServerThread.terminate();
				}
				nst_vector.remove(nodeServerThread);
			}
*/
			svrsoc.close();

		} catch (Exception ex) {
			ex.printStackTrace();
		}
		
	}
}

class NodeServerThread implements Runnable {
	private Socket soc;
	private Thread thread;
	private NodeID source;
	private ObjectInputStream input;
	boolean running;
	private Listener listener;
	Hashtable<NodeID, ArrayList<NodeID>> neighborsArray;
	public NodeServerThread(NodeID id, Socket s, Listener listener, Hashtable<NodeID, ArrayList<NodeID>> neighborsArray) throws IOException {
		this.source = id;
		this.soc = s;
		this.listener = listener;
		this.neighborsArray = neighborsArray;

	}

	@Override
	public void run() {
		while (this.running) {
			try {
				this.input = new ObjectInputStream(this.soc.getInputStream());
				NodeMessage msg_received = (NodeMessage) input.readObject();
				if (msg_received.type == 0) { // broken
					terminate();
					this.listener.broken(msg_received.message.source);
					break; // it ends here because 
				} else if (msg_received.type == 1) {
					this.listener.receive(msg_received.message);
				} else {
					System.out.println("Error-Message");
					break;
				}

			}catch (SocketException e) {
				break;
			}catch (Exception e) {
				e.printStackTrace();
				continue;
			}
		}
	}
	
	private void shutdownSoc(Socket soc) {
		try {
			soc.shutdownInput();
			soc.shutdownOutput();
			soc.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	

	private boolean deleteNeighbor(NodeID id) {
		ArrayList<NodeID> neis = null;
		for (NodeID nei : this.neighborsArray.keySet()) {
			if(nei.getID() == this.source.getID())
			{
				 neis = this.neighborsArray.get(nei);
			}
		}
		for (NodeID neiId : neis) {
			if(neiId.getID() == id.getID())
			{
				return neis.remove(neiId);
			}
		}
		return false;
	}

	public void start() {
		System.out.println("Connection established at Node |" + this.source.getID() + "|...\n");
		if (thread == null) {
			thread = new Thread(this, "NST-");
			this.running = true;
			thread.start();
		}
	}

	public synchronized void terminate() {
		this.running = false;
		shutdownSoc(this.soc);
	}
}
