public class NodeMessage implements java.io.Serializable
{
    // 0 = broken
    // 1 = recieve
    int type;  
    Message message;
    public NodeMessage(int type, Message message){
        
        this.type = type;
        this.message=message;
    }
}
