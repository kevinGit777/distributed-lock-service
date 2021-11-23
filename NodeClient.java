import java.io.IOException;
import java.io.Serializable;
import java.net.*;
import java.util.*;

class NodeClient implements Runnable, Serializable {
	private NodeID source;
	private NodeID destination;
	private String host;
	private Integer port;
	private boolean connected;
	private Thread t;
	Hashtable<NodeID, Socket> sockets;

	public NodeClient(NodeID source, NodeID destination, String host, int port, Hashtable<NodeID, Socket> sockets) {
		this.source = source;
		this.destination = destination;
		this.host = host; // remove for testing + ".utdallas.edu";
		this.port = port;
		this.sockets = sockets;
		connected = false;
	}

	@Override
	public void run() {
		// Attempt connection to Neighbor Node until successful
		Socket soc = connectToServer(this.host, this.port);
		try {
			System.out.println(
					"Connected to Node |" + this.destination.getID() + "| from Node |" + this.source.getID() + "|\n");
			soc.setKeepAlive(true);
			sockets.put(this.destination, soc);
			connected = true;
		} catch (Exception ex) {
			ex.printStackTrace();
		}

	}

	public Socket connectToServer(String host, int port) {

		Socket soc = null;
		while (true)
			try {
				//System.out.println("Trying to connect..." + host + port);
				Thread.sleep(1000); // Wait 1sec before retry
				// System.out.println("Try to connect to server.");
				soc = new Socket(host, port);
				break;
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (IOException exception) {
				continue;
			}
		return soc;
	}

	public void start() {
		System.out.println("Starting client from Node |" + this.source.getID() + "| to Node |"
				+ this.destination.getID() + "|...\n");
		if (t == null) {
			t = new Thread(this, "client-s" + this.source.getID() + "-d" + this.destination.getID());
			t.start();
		}
	}

	public boolean isConnected() {
		return connected;
	}
}