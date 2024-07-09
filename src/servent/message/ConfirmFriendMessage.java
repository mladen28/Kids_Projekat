package servent.message;

public class ConfirmFriendMessage extends BasicMessage{
    private static final long serialVersionUID = -6213394344524747458L;

    public ConfirmFriendMessage(String senderIpAddress, int senderPort, String receiverIpAddress, int receiverPort, String tekst) {
        super(MessageType.CONFIRM_FRIEND, senderIpAddress, senderPort, receiverIpAddress, receiverPort, tekst);
    }
}
