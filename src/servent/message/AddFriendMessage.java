package servent.message;

public class AddFriendMessage extends BasicMessage {

    private static final long serialVersionUID = 5163039589888734471L;
    public AddFriendMessage(String senderIpAddress, int senderPort, String receiverIpAddress, int receiverPort,String tekst) {
        super(MessageType.ADD_FRIEND, senderIpAddress, senderPort, receiverIpAddress, receiverPort, tekst);
    }
}
