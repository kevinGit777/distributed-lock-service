//Class needs to be serializable since its a part of Message
public class NodeID implements java.io.Serializable
{
	//ID of the node
	private int identifier;
	
	//Constructor
	public NodeID(int id)
	{
		identifier = id;
	}
	
	//Getter function for ID
	public int getID()
	{
		return identifier;
	}
}
