package servent.message;

import java.io.Serial;

public class AskFromBackupMessage extends BasicMessage{
    @Serial
    private static final long serialVersionUID = 2261615800740637825L;

    private final int port;
    public AskFromBackupMessage(String senderIpAddress, int senderPort, String receiverIpAddress, int receiverPort, int port) {
        super(MessageType.ASK_FROM_BACKUP, senderIpAddress, senderPort, receiverIpAddress, receiverPort);
        this.port = port;
    }

    public int getPort() {
        return port;
    }
}
