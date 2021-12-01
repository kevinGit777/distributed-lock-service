//Message needs to be serializable in order to send it using sockets
public class Message implements java.io.Serializable
{
	//ID of the node sending the message
	NodeID source;
	
	//Payload of the message
	byte[] data;

	int timestamp;
	
	//Constructor
	public Message(NodeID source, byte[] data, int timestamp)
	{
		this.source = source;
		this.data = data;
		this.timestamp = timestamp;
	}

}
