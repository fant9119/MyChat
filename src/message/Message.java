package message;

import java.io.Serializable;

public class Message implements Serializable{

    private static final long serialVersionUID = 1L;
    public String type;
    public String sender;
    public String content;
    public String recipient;

    public Message(String type, String sender, String content, String recipient){
        this.type = type;
        this.sender = sender;
        this.content = content;
        this.recipient = recipient;
    }

    @Override
    public String toString(){
        return "{type='" + type + "', sender='" + sender + "', content='" +
                content + "', recipient='" + recipient + "'}";
    }
}