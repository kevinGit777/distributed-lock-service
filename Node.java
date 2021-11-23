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
 * Friday, October 8th, 2021
 */

import java.io.*;
import java.net.*;
import java.util.*;

//Object to reprsents a node in the distributed system
class Node implements Serializable {
	// node identifier
	private NodeID identifier;
	private String config;
	private Listener listen;

	int node_num;

	private Hashtable<NodeID, String> hosts;
	private Hashtable<NodeID, Integer> ports;
	private Hashtable<NodeID, ArrayList<NodeID>> neighborsArray;
	private Hashtable<NodeID, Socket> sockets;

	private NodeServer server;
	private Vector<NodeClient> clients;
	boolean alive;

	// constructor
	public Node(NodeID identifier, String configFile, Listener listener) {

		// 1) Initialize vairables:
		this.identifier = identifier;
		this.config = configFile;
		this.listen = listener;
		clients = new Vector<>();
		sockets = new Hashtable<>();
		alive = true;

		// 2) Parse config
		parse_config(config);

		// 3) Start SERVER thread

		this.server = new NodeServer(this.identifier, ports.get(this.identifier), this.listen, neighborsArray);
		this.server.start();

		// 4) Start CLIENT socket for each neighbor
		for (NodeID neighbor : neighborsArray.get(this.identifier)) {
			NodeClient nc = new NodeClient(this.identifier, neighbor, hosts.get(neighbor), ports.get(neighbor),
					sockets);
			clients.add(nc);
			nc.start();
		}

		for (int i = 0; i < neighborsArray.get(this.identifier).size(); i++) {
			while (!clients.get(i).isConnected()) {
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}

	}

	public void parse_config(String configFile) {
		File file = new File(configFile);
		BufferedReader reader = null;

		// processed string: delete the comments
		StringBuffer stringBuf = new StringBuffer();
		try {
			reader = new BufferedReader(new FileReader(file));
			String tempString;

			// line by line, until meets null
			while ((tempString = reader.readLine()) != null) {

				// delete the comments in the end of line
				String contentLine = tempString;
				if (-1 != tempString.indexOf("#")) {
					contentLine = tempString.substring(0, tempString.indexOf("#"));

					// ignore the blank line.
					if ("".equals(contentLine)) {
						continue;
					}
				}
				stringBuf.append(contentLine + "\n");
			}
			reader.close();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e1) {
				}
			}
		}

		String processedStr = new String(stringBuf);

		// delete the null string in arrays.
		ArrayList<String> para = new ArrayList<>(Arrays.asList(processedStr.split("\n| ")));
		for (int i = para.size() - 1; i >= 0; i--) {
			if ("".equals(para.get(i))) {
				para.remove(i);
			}
		}

		// first elements is the num of Nodes
		node_num = Integer.parseInt(para.get(0));
		NodeID[] nodeIDs = new NodeID[node_num];
		hosts = new Hashtable<>();
		ports = new Hashtable<>();
		neighborsArray = new Hashtable<>();

		builtNodeIDs(nodeIDs, para, node_num);

		for (int i = 0; i < node_num; i++) {
			hosts.put(nodeIDs[i], para.get(1 + i * 3 + 1));
			ports.put(nodeIDs[i], Integer.parseInt(para.get(1 + i * 3 + 2)));
		}
		ArrayList<String> paraNbr = new ArrayList<>(Arrays.asList(processedStr.split("\\n+\\s*\\n*")));

		// if the prefix matches the regex, there will be a NULL at the index 0. delete it if exist.
		if ("".equals(paraNbr.get(0))){
			paraNbr.remove(0);
		}
		ArrayList<NodeID> nodeNbr = new ArrayList<>();
		for (int i = 0; i < node_num; i++) {
			String[] nodeNbrStr = paraNbr.get(i + 1 + node_num).split("\\s+");
			for (int c = 0; c < nodeNbrStr.length; c++) {
				nodeNbr.add(findNodeID(Integer.parseInt(nodeNbrStr[c]), nodeIDs));
			}
			neighborsArray.put(nodeIDs[i], new ArrayList<NodeID>(nodeNbr));
			nodeNbr.removeAll(nodeNbr);
		}

	}

	NodeID findNodeID(int id, NodeID[] ids) {
		for (int i = 0; i < ids.length; i++) {
			if (id == ids[i].getID())
				return ids[i];
		}
		return null;
	}

	private void builtNodeIDs(NodeID[] IDs, ArrayList<String> para, int node_num) {
		for (int i = 0; i < node_num; i++) {
			int id = Integer.parseInt(para.get(1 + i * 3));
			if (id == this.identifier.getID()) {
				IDs[i] = this.identifier;
			} else {
				IDs[i] = new NodeID(id);
			}
		}
	}

	public NodeID[] getNeighbors() {
		return neighborsArray.get(identifier).toArray(new NodeID[neighborsArray.get(identifier).size()]);
	}

	public void send(Message message, NodeID destination) {
		if (neighborsArray.get(identifier).contains(destination)) {
			NodeMessage mes = new NodeMessage(1, message); // 1 is for "recieve"
			try {
				sendNodeMessage(mes, sockets.get(destination).getOutputStream());
			} catch (IOException e) {
				System.out.println(destination.getID() + "is broken.");
				listen.broken(destination);
				e.printStackTrace();
			}
		}

	}

	public void sendToAll(Message message) {
		for (NodeID neighbor : neighborsArray.get(identifier)) {
			NodeMessage mes = new NodeMessage(1, message); // 1 is for "recieve"
			try {
				sendNodeMessage(mes, sockets.get(neighbor).getOutputStream());
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private void sendNodeMessage(NodeMessage message, OutputStream out) {
		if (out == null) {
			listen.broken(message.message.source);
		}

		ObjectOutputStream outputStream = null;
		try {
			outputStream = new ObjectOutputStream(out);
			outputStream.writeObject(message);
			outputStream.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public void tearDown() {
		// Close all client Sockets with
		if (!alive)
			return;
		alive = false;

		for (NodeID neighbor : neighborsArray.get(this.identifier)) {
			try {
				sendNodeMessage(new NodeMessage(0, new Message(this.identifier, null)),
						sockets.get(neighbor).getOutputStream());
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
		// Terminate the Server
		try {
			server.terminate();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

}
