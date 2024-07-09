package servent.message;

public class TellBackupMessage extends BasicMessage{
    private static final long serialVersionUID = 2261615800740631147L;

    public TellBackupMessage(String senderIpAddress, int senderPort, String receiverIpAddress, int receiverPort) {
        super(MessageType.TELL_BACKUP, senderIpAddress, senderPort, receiverIpAddress, receiverPort);
    }
}
