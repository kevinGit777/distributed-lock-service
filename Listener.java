//Listener interface in order to implement the observer design pattern
public interface Listener 
{
	//Method to call when a message is received from another node
	public void receive(Message message);
	
	//Method to call when connection to another node is broken
	public void broken(NodeID neighbor);
}